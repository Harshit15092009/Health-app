package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FitnessFormulas
import com.example.ui.FitnessViewModel
import com.example.ui.components.YouTubePlayerDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val dietPlan by viewModel.generatedDietPlan.collectAsState()
    val workoutDays by viewModel.generatedWorkoutDays.collectAsState()
    val todayWater by viewModel.todayWater.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()

    var activeTutorialExercise by remember { mutableStateOf<FitnessFormulas.ExercisePlanItem?>(null) }
    var showWorkoutSelector by remember { mutableStateOf(false) }

    val calorieTarget = dietPlan?.totalCalories ?: 2200
    val waterTarget = dietPlan?.hydrationTargetMl ?: 2500
    val waterDrunk = todayWater?.amountMl ?: 0

    // Fake calorie intake tracker (derived from completed workouts or logged meals)
    // 500 kcal per completed workout or simple logged snacks
    val caloriesBurnt = workouts.size * 350
    val calorieProgress = (caloriesBurnt.toFloat() / calorieTarget).coerceIn(0f, 1f)
    val waterProgress = (waterDrunk.toFloat() / waterTarget).coerceIn(0f, 1f)
    val workoutProgress = if (workoutDays.isNotEmpty()) {
        (workouts.filter { it.timestamp > System.currentTimeMillis() - 86400000 * 7 }.size.toFloat() / 3f).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BODY BLUEPRINT",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = NeonBlue
                    )
                    Text(
                        text = "Today's Mission Dashboard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // GYM VS HOME MODE TOGGLE
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(CardSlate)
                        .clickable {
                            viewModel.isHomeMode = !viewModel.isHomeMode
                            viewModel.speakCoaching("Switched to ${if (viewModel.isHomeMode) "Home Bodyweight" else "Gym Equipment"} mode.")
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.isHomeMode) Icons.Default.Home else Icons.Default.FitnessCenter,
                        contentDescription = "Training Mode",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isHomeMode) "HOME" else "GYM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Active Session Card IF IN WORKOUT
        val activeSession = viewModel.activeWorkoutDay
        if (activeSession != null) {
            item {
                ActiveWorkoutPlayerCard(
                    session = activeSession,
                    viewModel = viewModel,
                    onWatchTutorial = { activeTutorialExercise = it }
                )
            }
        }

        // Feature 6: Today's Mission Progress Indicators Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S MISSION",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = BrightPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streakCount Day Streak",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Rings Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressRing(
                            progress = calorieProgress,
                            title = "Kcal Burnt",
                            valueText = "$caloriesBurnt",
                            color = BrightPink
                        )
                        ProgressRing(
                            progress = workoutProgress,
                            title = "Weekly Lift",
                            valueText = "${(workoutProgress * 3).toInt()}/3",
                            color = NeonBlue
                        )
                        ProgressRing(
                            progress = waterProgress,
                            title = "Hydration",
                            valueText = "${waterDrunk}ml",
                            color = NeonGreen
                        )
                    }
                }
            }
        }

        // Feature 7: Interactive Hydration Glass Tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HYDRATION LAB",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonBlue
                            )
                            Text(
                                text = "Target: ${waterTarget}ml (35ml per kg)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.logWaterDrink() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = NeonBlue.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Quick Add Water",
                                tint = NeonBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Liquid Glass Animation Visual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Water Cups Indicator Grid (each cup 250ml)
                        val totalRequiredCups = (waterTarget / 250f).toInt().coerceAtLeast(1)
                        val completedCups = (waterDrunk / 250f).toInt()

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(TechBlack, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Draw nice cups
                            for (c in 1..8) {
                                Icon(
                                    imageVector = Icons.Default.LocalCafe,
                                    contentDescription = "Cup",
                                    tint = if (c <= completedCups) NeonBlue else Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            viewModel.logWaterDrink()
                                        }
                                )
                                if (c < 8) Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Action Trigger to launch or preview workout plans
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (workoutDays.isNotEmpty()) {
                            showWorkoutSelector = true
                        } else {
                            viewModel.speakCoaching("Please input your body blueprint measurements to generate a workout.")
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = NeonBlue.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "START TODAY'S SESSION",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = NeonBlue
                        )
                        Text(
                            text = if (workoutDays.isNotEmpty()) "Choose from your personalized generated workout days." else "Update measurements first to unlock plan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start session",
                        tint = NeonGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Diet Recommended Menu Summary
        val plan = dietPlan
        if (plan != null) {
            item {
                Text(
                    text = "RECOMENDED MEAL SHIELD",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonGreen,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(plan.meals) { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = meal.name,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = meal.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = meal.timing,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen
                                )
                            }
                            Text(
                                text = meal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSlate, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go to the Blueprint page to enter measurements and generate your nutrition shield.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Workout Day Selector Modal
    if (showWorkoutSelector) {
        AlertDialog(
            onDismissRequest = { showWorkoutSelector = false },
            title = { Text("Choose Active Workout", color = NeonBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    workoutDays.forEach { day ->
                        Button(
                            onClick = {
                                viewModel.startWorkout(day)
                                showWorkoutSelector = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CardSlate)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day.dayName, color = NeonBlue, fontWeight = FontWeight.Bold)
                                Text(day.title, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWorkoutSelector = false }) {
                    Text("Close", color = BrightPink)
                }
            },
            containerColor = DeepSlate
        )
    }

    // YouTube Tutorial Dialog Embed
    activeTutorialExercise?.let { exercise ->
        YouTubePlayerDialog(
            exerciseName = exercise.name,
            searchQuery = exercise.videoSearchQuery,
            onDismiss = { activeTutorialExercise = null }
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    title: String,
    valueText: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx())
                )
            }
            // Colored Progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // Central Value
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f)
        )
    }
}

@Composable
fun ActiveWorkoutPlayerCard(
    session: FitnessFormulas.WorkoutDay,
    viewModel: FitnessViewModel,
    onWatchTutorial: (FitnessFormulas.ExercisePlanItem) -> Unit
) {
    val activeIdx = viewModel.activeExerciseIndex
    val currentExercise = session.exercises.getOrNull(activeIdx)

    if (currentExercise != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepSlate),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE SESSION: ${session.dayName}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }

                    // Voice Coach Alert
                    IconButton(
                        onClick = { viewModel.speakCoaching("This is your auto-talking coach guiding you through sets and rest cycles.") },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = NeonBlue.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Voice Coach Info",
                            tint = NeonBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Exercise Detail Screen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "EXERCISE ${activeIdx + 1}/${session.exercises.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonBlue
                        )
                        Text(
                            text = currentExercise.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sets: ${currentExercise.sets} | Reps: ${currentExercise.reps} | Rest: ${currentExercise.restSeconds}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature 8: Built-in Rest Timer Overlay
                if (viewModel.isRestTimerActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrightPink.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                progress = { viewModel.restSecondsRemaining.toFloat() / currentExercise.restSeconds },
                                color = BrightPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "REST TIME ACTIVE: ${viewModel.restSecondsRemaining}s",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrightPink
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Watch Tutorial Button (YouTube Data API Feature 3)
                    Button(
                        onClick = { onWatchTutorial(currentExercise) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CardSlate)
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = "Watch", tint = NeonBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YOUTUBE", color = Color.White)
                    }

                    // Done / Next Exercise Button
                    Button(
                        onClick = {
                            if (activeIdx + 1 < session.exercises.size) {
                                viewModel.navigateToNextExercise()
                            } else {
                                // Workout complete feedback trigger!
                                viewModel.speakCoaching("Congratulations on completing your session. Please rate the workout difficulty.")
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                    ) {
                        Text(
                            text = if (activeIdx + 1 < session.exercises.size) "NEXT SET/LIFT" else "FINISH WORKOUT",
                            fontWeight = FontWeight.Bold,
                            color = TechBlack
                        )
                    }
                }

                // Finished Layout Difficulty Selector (Feature 2 Feedback System)
                if (activeIdx + 1 >= session.exercises.size) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Workout Complete! Rate the difficulty:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Too easy", "Just right", "Too hard").forEach { feedback ->
                            Button(
                                onClick = { viewModel.selectDifficultFeedback(feedback) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (feedback == "Just right") NeonGreen else CardSlate
                                )
                            ) {
                                Text(
                                    feedback,
                                    fontSize = 11.sp,
                                    color = if (feedback == "Just right") TechBlack else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
