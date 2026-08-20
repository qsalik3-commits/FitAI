package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class MindStats(
    val xp: Int = 0,
    val streak: Int = 0,
    val totalMinutes: Int = 0,
    val gamesCompleted: Int = 0,
    val focusAccuracyBest: Int = 0,
    val memoryLevelBest: Int = 0,
    val colorScoreBest: Int = 0,
    val numberScoreBest: Int = 0,
    val calmMinutesCompleted: Int = 0,
    val totalFocusHits: Int = 0,
    val lastCompletedDate: String = "",
    val dailyChallengeCompletedDate: String = ""
) {
    val level: Int
        get() = when {
            xp >= 1000 -> 5
            xp >= 600 -> 4
            xp >= 300 -> 3
            xp >= 100 -> 2
            else -> 1
        }

    val levelTitle: String
        get() = when (level) {
            1 -> "Beginner"
            2 -> "Explorer"
            3 -> "Focused"
            4 -> "Sharp"
            else -> "Mind Master"
        }

    val nextLevelXp: Int
        get() = when (level) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            else -> 1000
        }

    val currentLevelBaseXp: Int
        get() = when (level) {
            1 -> 0
            2 -> 100
            3 -> 300
            4 -> 600
            else -> 1000
        }

    val mindScore: Int
        get() {
            // Composite cognitive performance score calculated from game history & achievements
            val base = (xp / 10).coerceAtLeast(10)
            val accuracyBonus = (focusAccuracyBest * 0.4).toInt()
            val memBonus = memoryLevelBest * 15
            val colorBonus = colorScoreBest * 5
            val numBonus = numberScoreBest * 5
            val streakBonus = (streak * 10).coerceAtMost(100)
            return (base + accuracyBonus + memBonus + colorBonus + numBonus + streakBonus).coerceIn(10, 999)
        }
}

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val progress: String
)

enum class MindGameType(val id: String, val title: String, val subtitle: String, val iconEmoji: String, val durationSec: Int) {
    FOCUS_FLOW("focus_flow", "Focus Flow", "Timing & rhythm precision", "🎯", 60),
    MEMORY_FLASH("memory_flash", "Memory Flash", "Visual pattern retention", "🧩", 60),
    COLOR_FOCUS("color_focus", "Color Focus", "Stroop reaction & attention", "🎨", 60),
    NUMBER_FOCUS("number_focus", "Number Focus", "Sequential speed tracking", "🔢", 60),
    CALM_MINUTE("calm_minute", "Calm Minute", "Guided box breathing & rhythm", "🌊", 60)
}

class MindFitnessRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fitai_mind_prefs", Context.MODE_PRIVATE)

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<MindStats> = _statsFlow.asStateFlow()

    private fun loadStats(): MindStats {
        return MindStats(
            xp = prefs.getInt("mind_xp", 0),
            streak = prefs.getInt("mind_streak", 0),
            totalMinutes = prefs.getInt("mind_total_minutes", 0),
            gamesCompleted = prefs.getInt("mind_games_completed", 0),
            focusAccuracyBest = prefs.getInt("mind_focus_acc_best", 0),
            memoryLevelBest = prefs.getInt("mind_mem_level_best", 0),
            colorScoreBest = prefs.getInt("mind_color_best", 0),
            numberScoreBest = prefs.getInt("mind_number_best", 0),
            calmMinutesCompleted = prefs.getInt("mind_calm_completed", 0),
            totalFocusHits = prefs.getInt("mind_total_focus_hits", 0),
            lastCompletedDate = prefs.getString("mind_last_completed_date", "") ?: "",
            dailyChallengeCompletedDate = prefs.getString("mind_daily_challenge_date", "") ?: ""
        )
    }

    private fun saveStats(stats: MindStats) {
        prefs.edit()
            .putInt("mind_xp", stats.xp)
            .putInt("mind_streak", stats.streak)
            .putInt("mind_total_minutes", stats.totalMinutes)
            .putInt("mind_games_completed", stats.gamesCompleted)
            .putInt("mind_focus_acc_best", stats.focusAccuracyBest)
            .putInt("mind_mem_level_best", stats.memoryLevelBest)
            .putInt("mind_color_best", stats.colorScoreBest)
            .putInt("mind_number_best", stats.numberScoreBest)
            .putInt("mind_calm_completed", stats.calmMinutesCompleted)
            .putInt("mind_total_focus_hits", stats.totalFocusHits)
            .putString("mind_last_completed_date", stats.lastCompletedDate)
            .putString("mind_daily_challenge_date", stats.dailyChallengeCompletedDate)
            .apply()
        _statsFlow.value = stats
    }

    fun getTodayChallengeGame(): MindGameType {
        val today = LocalDate.now(ZoneId.systemDefault())
        val dayOfYear = today.dayOfYear
        val games = MindGameType.entries
        val index = (dayOfYear % games.size).coerceIn(0, games.size - 1)
        return games[index]
    }

    fun isDailyChallengeCompletedToday(): Boolean {
        val todayStr = LocalDate.now(ZoneId.systemDefault()).toString()
        return _statsFlow.value.dailyChallengeCompletedDate == todayStr
    }

    fun recordGameSession(
        gameType: MindGameType,
        score: Int,
        accuracy: Int,
        isPerfect: Boolean,
        isDailyChallenge: Boolean,
        sessionMinutes: Int = 1
    ): Int {
        val current = _statsFlow.value
        val today = LocalDate.now(ZoneId.systemDefault())
        val todayStr = today.toString()

        var earnedXp = 20
        if (isPerfect) earnedXp += 10
        if (isDailyChallenge && current.dailyChallengeCompletedDate != todayStr) {
            earnedXp += 20
        }

        // Calculate streak updating
        var newStreak = current.streak
        var newDailyCompletedDate = current.dailyChallengeCompletedDate

        if (isDailyChallenge && current.dailyChallengeCompletedDate != todayStr) {
            newDailyCompletedDate = todayStr
            if (current.dailyChallengeCompletedDate.isNotEmpty()) {
                try {
                    val lastDate = LocalDate.parse(current.dailyChallengeCompletedDate)
                    val daysDiff = ChronoUnit.DAYS.between(lastDate, today)
                    if (daysDiff == 1L) {
                        newStreak += 1
                    } else if (daysDiff > 1L) {
                        newStreak = 1
                    }
                } catch (e: Exception) {
                    newStreak = 1
                }
            } else {
                newStreak = 1
            }
        }

        val updatedStats = current.copy(
            xp = current.xp + earnedXp,
            streak = newStreak,
            totalMinutes = current.totalMinutes + sessionMinutes,
            gamesCompleted = current.gamesCompleted + 1,
            focusAccuracyBest = if (gameType == MindGameType.FOCUS_FLOW) maxOf(current.focusAccuracyBest, accuracy) else current.focusAccuracyBest,
            memoryLevelBest = if (gameType == MindGameType.MEMORY_FLASH) maxOf(current.memoryLevelBest, score) else current.memoryLevelBest,
            colorScoreBest = if (gameType == MindGameType.COLOR_FOCUS) maxOf(current.colorScoreBest, score) else current.colorScoreBest,
            numberScoreBest = if (gameType == MindGameType.NUMBER_FOCUS) maxOf(current.numberScoreBest, score) else current.numberScoreBest,
            calmMinutesCompleted = if (gameType == MindGameType.CALM_MINUTE) current.calmMinutesCompleted + 1 else current.calmMinutesCompleted,
            totalFocusHits = if (gameType == MindGameType.FOCUS_FLOW) current.totalFocusHits + (accuracy / 10) else current.totalFocusHits,
            lastCompletedDate = todayStr,
            dailyChallengeCompletedDate = newDailyCompletedDate
        )

        saveStats(updatedStats)
        return earnedXp
    }

    fun getAchievements(): List<AchievementItem> {
        val s = _statsFlow.value
        return listOf(
            AchievementItem(
                id = "first_minute",
                title = "First Mind Minute",
                description = "Complete your first Mind Fitness session",
                iconEmoji = "🧠",
                isUnlocked = s.gamesCompleted >= 1,
                progress = "${(s.gamesCompleted).coerceAtMost(1)} / 1"
            ),
            AchievementItem(
                id = "streak_3",
                title = "3 Day Mind Streak",
                description = "Maintain a 3-day daily challenge streak",
                iconEmoji = "🔥",
                isUnlocked = s.streak >= 3,
                progress = "${s.streak.coerceAtMost(3)} / 3 days"
            ),
            AchievementItem(
                id = "streak_7",
                title = "7 Day Mind Streak",
                description = "Complete 7 consecutive daily challenges",
                iconEmoji = "⚡",
                isUnlocked = s.streak >= 7,
                progress = "${s.streak.coerceAtMost(7)} / 7 days"
            ),
            AchievementItem(
                id = "focus_hits_100",
                title = "100 Focus Hits",
                description = "Score over 100 precision timing hits in Focus Flow",
                iconEmoji = "🎯",
                isUnlocked = s.totalFocusHits >= 100,
                progress = "${s.totalFocusHits.coerceAtMost(100)} / 100"
            ),
            AchievementItem(
                id = "memory_master",
                title = "Memory Master",
                description = "Reach Level 4 (Master) in Memory Flash",
                iconEmoji = "🧩",
                isUnlocked = s.memoryLevelBest >= 4,
                progress = "Level ${s.memoryLevelBest.coerceAtMost(4)} / 4"
            ),
            AchievementItem(
                id = "calm_minute",
                title = "Calm Minute",
                description = "Complete at least one 60-second breathing session",
                iconEmoji = "🌊",
                isUnlocked = s.calmMinutesCompleted >= 1,
                progress = "${s.calmMinutesCompleted.coerceAtMost(1)} / 1"
            ),
            AchievementItem(
                id = "xp_1000",
                title = "1,000 Mind XP",
                description = "Accumulate 1,000 total Mind XP",
                iconEmoji = "🏆",
                isUnlocked = s.xp >= 1000,
                progress = "${s.xp.coerceAtMost(1000)} / 1,000 XP"
            ),
            AchievementItem(
                id = "perfect_focus",
                title = "Perfect Focus",
                description = "Achieve 90%+ accuracy in Focus Flow or Color Focus",
                iconEmoji = "✨",
                isUnlocked = s.focusAccuracyBest >= 90 || s.colorScoreBest >= 15,
                progress = if (s.focusAccuracyBest >= 90) "Unlocked (90%+)" else "Best: ${s.focusAccuracyBest}%"
            )
        )
    }
}
