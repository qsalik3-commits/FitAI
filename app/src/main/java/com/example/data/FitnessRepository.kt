package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FitnessRepository(private val dao: FitnessDao) {
    val allActivities: Flow<List<ActivityEntity>> = dao.getAllActivities()
    val allMeals: Flow<List<MealEntity>> = dao.getAllMeals()

    suspend fun insertActivity(activity: ActivityEntity) = dao.insertActivity(activity)
    suspend fun insertMeal(meal: MealEntity) = dao.insertMeal(meal)
    
    fun getCaloriesBurnedToday(): Flow<Int?> {
        val startOfDay = getStartOfDay()
        return dao.getCaloriesBurnedToday(startOfDay)
    }
    
    fun getCaloriesConsumedToday(): Flow<Int?> {
        val startOfDay = getStartOfDay()
        return dao.getCaloriesConsumedToday(startOfDay)
    }

    fun getTotalDurationToday(): Flow<Int?> {
        val startOfDay = getStartOfDay()
        return dao.getTotalDurationToday(startOfDay)
    }
    
    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
