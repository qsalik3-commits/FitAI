package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)
    
    @Query("SELECT SUM(caloriesBurned) FROM activities WHERE timestamp >= :startOfDay")
    fun getCaloriesBurnedToday(startOfDay: Long): Flow<Int?>
    
    @Query("SELECT SUM(calories) FROM meals WHERE timestamp >= :startOfDay")
    fun getCaloriesConsumedToday(startOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM activities WHERE timestamp >= :startOfDay")
    fun getTotalDurationToday(startOfDay: Long): Flow<Int?>
}
