package com.example.data

import android.content.Context
import com.example.data.FitnessFormulas.ProportionStatus
import com.example.data.FitnessFormulas.evaluateProportions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.appDao()

    // --- Measurements ---
    fun getLatestMeasurementFlow(): Flow<MeasurementLog?> = dao.getLatestMeasurementFlow()
    suspend fun getLatestMeasurement(): MeasurementLog? = dao.getLatestMeasurement()
    fun getAllMeasurementsFlow(): Flow<List<MeasurementLog>> = dao.getAllMeasurementsFlow()

    suspend fun insertMeasurement(log: MeasurementLog): Long {
        val result = dao.insertMeasurement(log)
        // Recalculate badge eligibility after inserting measurements
        checkMeasurementBadges(log)
        return result
    }

    // --- Workouts ---
    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkout>> = dao.getAllCompletedWorkoutsFlow()

    suspend fun logWorkout(
        name: String,
        muscles: String,
        difficulty: String,
        durationSec: Int = 1800,
        notes: String = ""
    ): Long {
        val workout = CompletedWorkout(
            workoutName = name,
            musclesTrained = muscles,
            difficultyFeedback = difficulty,
            durationSeconds = durationSec,
            notes = notes
        )
        val id = dao.insertCompletedWorkout(workout)
        // Check workout badges after logging
        checkWorkoutBadges()
        return id
    }

    // --- Water Logs ---
    fun getTodayWaterAmount(dateString: String): Flow<WaterLog?> = dao.getWaterLogByDateFlow(dateString)

    suspend fun addWater(dateString: String, amountMl: Int) {
        val currentLog = dao.getWaterLogByDate(dateString)
        if (currentLog != null) {
            val updated = currentLog.copy(amountMl = currentLog.amountMl + amountMl)
            dao.updateWaterLog(updated)
            checkHydrationBadge(updated.amountMl)
        } else {
            val newLog = WaterLog(dateString = dateString, amountMl = amountMl)
            dao.insertWaterLog(newLog)
            checkHydrationBadge(amountMl)
        }
    }

    // --- Custom Exercises ---
    fun getAllCustomExercises(): Flow<List<CustomExercise>> = dao.getAllCustomExercisesFlow()
    suspend fun insertCustomExercise(exercise: CustomExercise) = dao.insertCustomExercise(exercise)
    suspend fun deleteCustomExercise(id: Int) = dao.deleteCustomExerciseById(id)

    // --- Meal Photos ---
    fun getAllMealPhotos(): Flow<List<MealPhoto>> = dao.getAllMealPhotosFlow()
    suspend fun insertMealPhoto(photo: MealPhoto): Long {
        val id = dao.insertMealPhoto(photo)
        unlockBadgeSync("meal_log")
        return id
    }
    suspend fun deleteMealPhoto(id: Int) = dao.deleteMealPhotoById(id)

    // --- Accessories & Badges ---
    fun getAllBadges(): Flow<List<AchievementBadge>> = dao.getAllBadgesFlow()
    suspend fun unlockBadgeSync(id: String) {
        dao.unlockBadge(id, System.currentTimeMillis())
    }

    // --- Friend Challenges (Simulated Sync Lobby) ---
    fun getActiveChallenge(): Flow<FriendChallenge?> = dao.getActiveChallengeFlow()

    suspend fun createNewChallenge(inviteCode: String): FriendChallenge {
        // Build a simulated opponent based on the code entered
        val namesVec = listOf("BeastMode_Chris", "LiftingLydia", "IronGoddess", "CardioCarl", "ActiveJack")
        val randName = namesVec.getOrElse(inviteCode.length % namesVec.size) { "GymBuddy99" }
        val targetChallenge = FriendChallenge(
            weekCode = inviteCode,
            opponentName = randName,
            opponentCompletedCount = (3..5).random(),
            opponentMeasurementImprovement = ((10..25).random() / 10.0), // in cm
            userCompletedCount = 0,
            userMeasurementImprovement = 0.0,
            active = true
        )
        dao.insertFriendChallenge(targetChallenge)
        return targetChallenge
    }

    suspend fun updateChallengeProgress(userWorkouts: Int, userMeasurementImp: Double) {
        val challenge = dao.getActiveChallenge()
        if (challenge != null) {
            val updated = challenge.copy(
                userCompletedCount = userWorkouts,
                userMeasurementImprovement = userMeasurementImp
            )
            dao.updateFriendChallenge(updated)
        }
    }

    // --- Streak & Metric Computations ---
    fun getWorkoutStreakFlow(): Flow<Int> {
        return dao.getAllCompletedWorkoutsFlow().map { list ->
            calculateStreak(list)
        }
    }

    private fun calculateStreak(workouts: List<CompletedWorkout>): Int {
        if (workouts.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val trackedDates = mutableSetOf<String>()

        for (workout in workouts) {
            trackedDates.add(sdf.format(Date(workout.timestamp)))
        }

        val cal = Calendar.getInstance()
        var streak = 0
        var checkDate = sdf.format(cal.time)

        // If no workout today, check if yesterday had one to maintain streak
        if (!trackedDates.contains(checkDate)) {
            cal.add(Calendar.DATE, -1)
            checkDate = sdf.format(cal.time)
        }

        while (trackedDates.contains(checkDate)) {
            streak++
            cal.add(Calendar.DATE, -1)
            checkDate = sdf.format(cal.time)
        }
        return streak
    }

    // Check measurement-based badges
    private suspend fun checkMeasurementBadges(log: MeasurementLog) {
        // Lost 2 inches (~5cm) on waist: compare latest to earliest measurement log!
        val allLogs = dao.getAllMeasurementsFlow().map { it }.firstOrNull() ?: emptyList()
        if (allLogs.size >= 2) {
            val earliest = allLogs.first() // first is oldest because of order ASC
            val latest = allLogs.last()
            val waistLoss = earliest.waist - latest.waist
            if (waistLoss >= 5.0) { // 5 cm = ~2 inches
                unlockBadgeSync("lost_2_waist")
            }
        }
    }

    // Check workout-based badges
    private suspend fun checkWorkoutBadges() {
        val workouts = dao.getAllCompletedWorkouts()
        if (workouts.isNotEmpty()) {
            unlockBadgeSync("first_workout")
        }
        val streak = calculateStreak(workouts)
        if (streak >= 7) {
            unlockBadgeSync("streak_7")
        }
        if (streak >= 30) {
            unlockBadgeSync("streak_30")
        }
    }

    // Check hydration badge
    private suspend fun checkHydrationBadge(todayAmountMl: Int) {
        val latestVal = getLatestMeasurement()
        if (latestVal != null) {
            val target = (latestVal.weight * 35.0)
            if (todayAmountMl >= target) {
                unlockBadgeSync("hydration_king")
            }
        }
    }
}
