package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Measurement Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(log: MeasurementLog): Long

    @Query("SELECT * FROM measurement_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMeasurementFlow(): Flow<MeasurementLog?>

    @Query("SELECT * FROM measurement_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurement(): MeasurementLog?

    @Query("SELECT * FROM measurement_logs ORDER BY timestamp ASC")
    fun getAllMeasurementsFlow(): Flow<List<MeasurementLog>>

    // --- Completed Workouts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedWorkout(workout: CompletedWorkout): Long

    @Query("SELECT * FROM completed_workouts ORDER BY timestamp DESC")
    fun getAllCompletedWorkoutsFlow(): Flow<List<CompletedWorkout>>

    @Query("SELECT * FROM completed_workouts ORDER BY timestamp DESC")
    suspend fun getAllCompletedWorkouts(): List<CompletedWorkout>

    // --- Water Logs ---
    @Query("SELECT * FROM water_logs WHERE dateString = :dateString LIMIT 1")
    suspend fun getWaterLogByDate(dateString: String): WaterLog?

    @Query("SELECT * FROM water_logs WHERE dateString = :dateString LIMIT 1")
    fun getWaterLogByDateFlow(dateString: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog): Long

    @Update
    suspend fun updateWaterLog(log: WaterLog)

    // --- Custom Exercises ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomExercise(exercise: CustomExercise): Long

    @Query("SELECT * FROM custom_exercises ORDER BY id DESC")
    fun getAllCustomExercisesFlow(): Flow<List<CustomExercise>>

    @Query("DELETE FROM custom_exercises WHERE id = :id")
    suspend fun deleteCustomExerciseById(id: Int)

    // --- Meal Photos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPhoto(photo: MealPhoto): Long

    @Query("SELECT * FROM meal_photos ORDER BY timestamp DESC")
    fun getAllMealPhotosFlow(): Flow<List<MealPhoto>>

    @Query("DELETE FROM meal_photos WHERE id = :id")
    suspend fun deleteMealPhotoById(id: Int)

    // --- Achievement Badges ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadges(badges: List<AchievementBadge>)

    @Query("SELECT * FROM achievement_badges")
    fun getAllBadgesFlow(): Flow<List<AchievementBadge>>

    @Query("UPDATE achievement_badges SET unlocked = 1, unlockedTimestamp = :timestamp WHERE id = :id")
    suspend fun unlockBadge(id: String, timestamp: Long)

    // --- Friend Challenges ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendChallenge(challenge: FriendChallenge): Long

    @Query("SELECT * FROM friend_challenges WHERE active = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveChallengeFlow(): Flow<FriendChallenge?>

    @Query("SELECT * FROM friend_challenges WHERE active = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getActiveChallenge(): FriendChallenge?

    @Update
    suspend fun updateFriendChallenge(challenge: FriendChallenge)
}
