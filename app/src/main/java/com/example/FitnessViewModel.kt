package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.ActivityEntity
import com.example.data.AppDatabase
import com.example.data.FitnessRepository
import com.example.data.MealEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "fitai-database"
    ).build()
    
    private val repository = FitnessRepository(db.fitnessDao())
    private val prefs = application.getSharedPreferences("fitai_prefs", Context.MODE_PRIVATE)

    // Dynamic ticker for local device date - automatically updates when midnight strikes
    private val _currentDateFlow = MutableStateFlow(LocalDate.now(ZoneId.systemDefault()))
    val currentDateFlow: StateFlow<LocalDate> = _currentDateFlow.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.systemDefault()))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _waterLitresToday.value = getSavedWaterForDate(date)
    }

    init {
        viewModelScope.launch {
            while (isActive) {
                val currentLocal = LocalDate.now(ZoneId.systemDefault())
                if (currentLocal != _currentDateFlow.value) {
                    _currentDateFlow.value = currentLocal
                    // If the user hasn't explicitly changed the date to a past date, keep selected date tracking today
                    if (_selectedDate.value == currentLocal.minusDays(1)) {
                        _selectedDate.value = currentLocal
                        _waterLitresToday.value = getSavedWaterForDate(currentLocal)
                    }
                }
                delay(10000L) // Refresh local date every 10 seconds
            }
        }
    }

    val allActivities: StateFlow<List<ActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allMeals: StateFlow<List<MealEntity>> = repository.allMeals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val caloriesBurnedToday: StateFlow<Int?> = combine(allActivities, selectedDate) { activities, selected ->
        val startOfDay = selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selected.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        activities.filter { it.timestamp in startOfDay until endOfDay }.sumOf { it.caloriesBurned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val caloriesConsumedToday: StateFlow<Int?> = combine(allMeals, selectedDate) { meals, selected ->
        val startOfDay = selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selected.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        meals.filter { it.timestamp in startOfDay until endOfDay }.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDurationToday: StateFlow<Int?> = combine(allActivities, selectedDate) { activities, selected ->
        val startOfDay = selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selected.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        activities.filter { it.timestamp in startOfDay until endOfDay }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // FitAI Readiness Engine & Insight Generator
    val fitAiInsight: StateFlow<String> = combine(caloriesBurnedToday, caloriesConsumedToday, totalDurationToday) { burned, consumed, duration ->
        val safeBurned = burned ?: 0
        val safeConsumed = consumed ?: 0
        val safeDuration = duration ?: 0

        when {
            safeBurned > 600 && safeDuration > 60 -> "High Strain Detected 🔴\nYou've pushed hard! Prioritize protein and deep rest today for optimal muscle recovery."
            safeBurned > 300 && safeDuration > 30 -> "Active Mode 🟢\nGreat consistency. You're maintaining a healthy rhythm. Keep up the balanced hydration."
            safeConsumed > 2500 && safeBurned < 200 -> "Energy Surplus 🟡\nYou have high stored energy. It's a perfect time for a challenging workout or heavy lifting."
            safeBurned == 0 && safeDuration == 0 -> "Recovery Day 🔵\nBody battery is recharging. A light walk or stretching session would be perfect right now."
            else -> "Balanced 🟢\nYou're in a steady state. Listen to your body and move at your own pace."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Analyzing your metrics...")

    // Persistent Water Intake (in Liters)
    private val _waterLitresToday = MutableStateFlow(0f) // Initialized in init or dynamically
    val waterLitresToday: StateFlow<Float> = _waterLitresToday.asStateFlow()

    init {
        _waterLitresToday.value = getSavedWaterForDate(_selectedDate.value)
    }

    // Dynamic Streak & Week Calculations using device local timezone
    val currentStreak: StateFlow<Int> = combine(allActivities, allMeals, currentDateFlow) { activities, meals, today ->
        calculateStreak(activities, meals, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active days this week returned as set of DayOfWeek values (1 = Mon, 2 = Tue, ..., 7 = Sun)
    val activeDaysThisWeek: StateFlow<Set<Int>> = combine(allActivities, allMeals, currentDateFlow) { activities, meals, today ->
        getWeeklyLoggedDays(activities, meals, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isLoggedToday: StateFlow<Boolean> = combine(allActivities, allMeals, selectedDate) { activities, meals, selected ->
        isTodayLogged(activities, meals, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addActivity(type: String, duration: Int, calories: Int) {
        viewModelScope.launch {
            // Log it on the *selected* date, not necessarily real-time "now"
            val targetTime = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).plusHours(12).toInstant().toEpochMilli()
            repository.insertActivity(ActivityEntity(type = type, durationMinutes = duration, caloriesBurned = calories, timestamp = targetTime))
        }
    }

    fun addMeal(name: String, calories: Int, protein: Int = 0, carbs: Int = 0, fat: Int = 0) {
        viewModelScope.launch {
            val targetTime = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).plusHours(12).toInstant().toEpochMilli()
            repository.insertMeal(MealEntity(name = name, calories = calories, protein = protein, carbs = carbs, fat = fat, timestamp = targetTime))
        }
    }

    // Water Management
    fun addWater(amount: Float) {
        val updated = (_waterLitresToday.value + amount).coerceAtLeast(0f)
        _waterLitresToday.value = updated
        saveWaterForDate(_selectedDate.value, updated)
    }

    fun setWater(amount: Float) {
        val updated = amount.coerceAtLeast(0f)
        _waterLitresToday.value = updated
        saveWaterForDate(_selectedDate.value, updated)
    }

    fun resetWater() {
        _waterLitresToday.value = 0f
        saveWaterForDate(_selectedDate.value, 0f)
    }

    private fun getSavedWaterForDate(date: LocalDate): Float {
        val dateStr = date.toString()
        return prefs.getFloat("water_$dateStr", 0f)
    }

    private fun saveWaterForDate(date: LocalDate, litres: Float) {
        val dateStr = date.toString()
        prefs.edit()
            .putFloat("water_$dateStr", litres)
            .apply()
    }

    private fun calculateStreak(activities: List<ActivityEntity>, meals: List<MealEntity>, today: LocalDate = LocalDate.now(ZoneId.systemDefault())): Int {
        val timestamps = activities.map { it.timestamp } + meals.map { it.timestamp }
        if (timestamps.isEmpty()) return 0

        val zoneId = ZoneId.systemDefault()
        val loggedDates = timestamps.map { ts ->
            Instant.ofEpochMilli(ts).atZone(zoneId).toLocalDate()
        }.toSet()

        var streak = 0
        var checkDate = today

        if (loggedDates.contains(today)) {
            while (loggedDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
        } else {
            checkDate = today.minusDays(1)
            if (loggedDates.contains(checkDate)) {
                while (loggedDates.contains(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
            }
        }
        return streak
    }

    private fun getWeeklyLoggedDays(activities: List<ActivityEntity>, meals: List<MealEntity>, today: LocalDate = LocalDate.now(ZoneId.systemDefault())): Set<Int> {
        val zoneId = ZoneId.systemDefault()
        // Monday of current week
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val sunday = monday.plusDays(6)

        val timestamps = activities.map { it.timestamp } + meals.map { it.timestamp }
        val activeDays = mutableSetOf<Int>()

        for (ts in timestamps) {
            val date = Instant.ofEpochMilli(ts).atZone(zoneId).toLocalDate()
            if (!date.isBefore(monday) && !date.isAfter(sunday)) {
                activeDays.add(date.dayOfWeek.value) // 1 = Monday, 7 = Sunday
            }
        }
        return activeDays
    }

    private fun isTodayLogged(activities: List<ActivityEntity>, meals: List<MealEntity>, today: LocalDate = LocalDate.now(ZoneId.systemDefault())): Boolean {
        val zoneId = ZoneId.systemDefault()
        val timestamps = activities.map { it.timestamp } + meals.map { it.timestamp }

        return timestamps.any { ts ->
            Instant.ofEpochMilli(ts).atZone(zoneId).toLocalDate() == today
        }
    }
}

