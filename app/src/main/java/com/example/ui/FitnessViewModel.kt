package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // System Date String helper
    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())

    // --- State Streams ---
    val latestMeasurement: StateFlow<MeasurementLog?> = repository.getLatestMeasurementFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMeasurements: StateFlow<List<MeasurementLog>> = repository.getAllMeasurementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<CompletedWorkout>> = repository.getAllCompletedWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customExercises: StateFlow<List<CustomExercise>> = repository.getAllCustomExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealPhotos: StateFlow<List<MealPhoto>> = repository.getAllMealPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<AchievementBadge>> = repository.getAllBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChallenge: StateFlow<FriendChallenge?> = repository.getActiveChallenge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val streakCount: StateFlow<Int> = repository.getWorkoutStreakFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayWater: StateFlow<WaterLog?> = repository.getTodayWaterAmount(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- State Variables ---
    var isHomeMode by mutableStateOf(false)
    var difficultyAdjustment by mutableStateOf("NORMAL") // "EASY", "NORMAL", "HARD"

    // Plan generators reactively re-computed
    val generatedWorkoutDays: StateFlow<List<FitnessFormulas.WorkoutDay>> = combine(
        latestMeasurement,
        snapshotFlow { isHomeMode },
        snapshotFlow { difficultyAdjustment }
    ) { log: MeasurementLog?, home: Boolean, difficulty: String ->
        if (log != null) {
            FitnessFormulas.generateWorkoutPlan(log, home, difficulty)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedDietPlan = latestMeasurement.map { log ->
        if (log != null) {
            FitnessFormulas.generateDietPlan(log, FitnessFormulas.calculateBmi(log.weight, log.height))
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Active Workout State ---
    var activeWorkoutDay: FitnessFormulas.WorkoutDay? by mutableStateOf(null)
    var activeExerciseIndex by mutableStateOf(0)
    var isRestTimerActive by mutableStateOf(false)
    var restSecondsRemaining by mutableStateOf(0)
    private var timerJob: Job? = null

    // --- Posture State ---
    var lastPostureScore by mutableStateOf<Int?>(null)
    var lastPostureTips by mutableStateOf<List<String>>(emptyList())

    // --- Voice Coach TTS ---
    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
                speakCoaching("Voice coach initialized. Welcome to Body Blueprint!")
            }
        }
    }

    fun speakCoaching(text: String) {
        if (isTtsReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // --- Actions ---
    fun saveMeasurements(log: MeasurementLog) {
        viewModelScope.launch {
            repository.insertMeasurement(log)
            speakCoaching("Blueprint saved. Updating your workouts and meal structures.")
        }
    }

    fun logWaterDrink() {
        viewModelScope.launch {
            repository.addWater(todayDateString, 250)
            speakCoaching("Water logged. Keep hydrating!")
        }
    }

    fun addCustomExercise(name: String, target: String, notes: String) {
        viewModelScope.launch {
            repository.insertCustomExercise(CustomExercise(name = name, targetMuscle = target, notes = notes))
            speakCoaching("Custom exercise $name added successfully.")
        }
    }

    fun deleteCustomExercise(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomExercise(id)
        }
    }

    fun addMealPhotoLog(path: String, notes: String, mealType: String) {
        viewModelScope.launch {
            repository.insertMealPhoto(MealPhoto(photoUri = path, notes = notes, mealType = mealType))
            speakCoaching("Meal photo logged in your food journal.")
        }
    }

    fun deleteMealLog(id: Int) {
        viewModelScope.launch {
            repository.deleteMealPhoto(id)
        }
    }

    fun calculate1RepMax(weight: Double, reps: Int): Double {
        viewModelScope.launch {
            repository.unlockBadgeSync("one_rep_max")
        }
        if (reps <= 0) return 0.0
        // Epley formula: 1RM = w * (1 + r/30)
        return weight * (1.0 + reps / 30.0)
    }

    fun initiateFriendChallenge(code: String) {
        viewModelScope.launch {
            repository.createNewChallenge(code)
            speakCoaching("New challenge initiated with your gym partner!")
        }
    }

    // --- Active Workout Engine ---
    fun startWorkout(day: FitnessFormulas.WorkoutDay) {
        activeWorkoutDay = day
        activeExerciseIndex = 0
        isRestTimerActive = false
        restSecondsRemaining = 0
        timerJob?.cancel()

        val exercises = day.exercises
        if (exercises.isNotEmpty()) {
            val first = exercises[0]
            speakCoaching("Starting session: ${day.title}. First, let's complete ${first.sets} sets of ${first.name} for ${first.reps} reps. Aim for an RPE of ${first.rpe}.")
        }
    }

    fun navigateToNextExercise() {
        val day = activeWorkoutDay ?: return
        if (activeExerciseIndex + 1 < day.exercises.size) {
            val currentExercise = day.exercises[activeExerciseIndex]
            activeExerciseIndex++
            val nextExercise = day.exercises[activeExerciseIndex]

            // Trigger rest timer
            startRestTimer(currentExercise.restSeconds) {
                speakCoaching("Next exercise is ${nextExercise.name}. Do ${nextExercise.sets} sets of ${nextExercise.reps} reps.")
            }
        } else {
            // End of workout session
            speakCoaching("Outstanding! Workout completed. Log your session to see updated heatmaps.")
        }
    }

    private fun startRestTimer(seconds: Int, onComplete: () -> Unit) {
        timerJob?.cancel()
        isRestTimerActive = true
        restSecondsRemaining = seconds
        speakCoaching("Rest timer started for $seconds seconds.")

        timerJob = viewModelScope.launch {
            while (restSecondsRemaining > 0) {
                delay(1000)
                restSecondsRemaining--
                if (restSecondsRemaining == 5) {
                    speakCoaching("Five seconds of rest remaining.")
                }
            }
            isRestTimerActive = false
            triggerVibration()
            onComplete()
        }
    }

    private fun triggerVibration() {
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(300)
    }

    fun selectDifficultFeedback(feedback: String) {
        difficultyAdjustment = when (feedback) {
            "Too easy" -> "HARD"
            "Too hard" -> "EASY"
            else -> "NORMAL" // "Just right"
        }
        val currentDay = activeWorkoutDay
        if (currentDay != null) {
            viewModelScope.launch {
                val muscles = currentDay.exercises.map { it.targetMuscle }.distinct().joinToString(",")
                repository.logWorkout(
                    name = currentDay.title,
                    muscles = muscles,
                    difficulty = feedback
                )
                activeWorkoutDay = null // close session
                speakCoaching("Workout logged as $feedback. Next week's schedule has been optimized accordingly.")
            }
        }
    }

    fun calculatePostureScore(isAligned: Boolean) {
        viewModelScope.launch {
            repository.unlockBadgeSync("posture_check")
        }
        if (isAligned) {
            lastPostureScore = (85..95).random()
            lastPostureTips = listOf(
                "Superb spinal alignment. Keep your core tight.",
                "Slight shoulder drop on left side. Maintain even traps.",
                "Stunner alignment. Your posture is primed for squats!"
            )
        } else {
            lastPostureScore = (45..62).random()
            lastPostureTips = listOf(
                "Rounded shoulders detected. Incorporate face-pulls.",
                "Forward neck position. Perform isometric neck holds.",
                "Anterior pelvic tilt detected. Focus on glute bridges and planks."
            )
        }
        speakCoaching("Posture score updated: $lastPostureScore percent. Check recommended posture correction exercises.")
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        timerJob?.cancel()
    }
}
