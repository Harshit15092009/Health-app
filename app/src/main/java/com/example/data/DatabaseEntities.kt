package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_logs")
data class MeasurementLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val height: Double = 175.0, // in cm
    val weight: Double = 75.0,  // in kg
    val age: Int = 25,
    val gender: String = "Male",
    val chest: Double = 95.0,   // in cm
    val waist: Double = 85.0,
    val hips: Double = 98.0,
    val shoulders: Double = 110.0,
    val biceps: Double = 32.0,
    val forearms: Double = 28.0,
    val thighs: Double = 55.0,
    val calves: Double = 36.0,
    val neck: Double = 38.0,
    val bodyFat: Double? = null // percentage
)

@Entity(tableName = "completed_workouts")
data class CompletedWorkout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val workoutName: String,
    val musclesTrained: String, // Comma separated, e.g., "Chest,Triceps"
    val difficultyFeedback: String = "Just right", // "Too easy", "Just right", "Too hard"
    val durationSeconds: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // "YYYY-MM-DD"
    val amountMl: Int = 0
)

@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetMuscle: String, // "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs"
    val notes: String = "",
    val isHomeFriendly: Boolean = false
)

@Entity(tableName = "meal_photos")
data class MealPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val photoUri: String, // file path / URI
    val mealType: String = "Meal", // "Breakfast", "Lunch", "Dinner", "Snack"
    val notes: String = ""
)

@Entity(tableName = "achievement_badges")
data class AchievementBadge(
    @PrimaryKey val id: String, // badge identifier
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedTimestamp: Long? = null
)

@Entity(tableName = "friend_challenges")
data class FriendChallenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weekCode: String,
    val opponentName: String = "GymBuddy99",
    val opponentCompletedCount: Int = 3,
    val opponentMeasurementImprovement: Double = 1.2, // in cm / inches
    val userCompletedCount: Int = 0,
    val userMeasurementImprovement: Double = 0.0,
    val active: Boolean = true
)
