package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomExercise
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customExercises by viewModel.customExercises.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val challenge by viewModel.activeChallenge.collectAsState()

    // 1RM States
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var calculatedOneRepMax by remember { mutableStateOf(0.0) }

    // Custom Exercise Builder States
    var exerciseName by remember { mutableStateOf("") }
    var exerciseNotes by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("Chest") }
    var isHomeFriendly by remember { mutableStateOf(false) }

    // Friend Challenge States
    var lobbyCode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "UTILITY TOOLBOX",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = NeonBlue
            )
            Text(
                text = "Track your limits with 1RM formulas, design individual lifting movements, compete in active friend lobbies, and review achievements.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Feature 11: 1 Rep Max Calculator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "1-REP MAX ESTIMATOR",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonBlue
                    )
                    Text(
                        "Standard Epley formula: Weight × (1 + Reps/30)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Lift Weight (kg)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it },
                            label = { Text("Reps Completed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull() ?: 0.0
                            val r = repsInput.toIntOrNull() ?: 0
                            calculatedOneRepMax = viewModel.calculate1RepMax(w, r)
                            viewModel.speakCoaching("Estimated one-rep maximum is ${calculatedOneRepMax.toInt()} kilograms.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                    ) {
                        Text("ESTIMATE FORWARD STRENGTH", color = TechBlack, fontWeight = FontWeight.Bold)
                    }

                    if (calculatedOneRepMax > 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Your Estimated 1RM: ${String.format("%.1f", calculatedOneRepMax)}kg",
                                color = NeonGreen,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Feature 13: Custom Exercise Creator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CUSTOM EXERCISE BUILDER",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonGreen
                    )
                    Text(
                        "Design custom movements; they'll align dynamically with generated plan days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Exercise Name (e.g., Kettlebell Deadlift)", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = exerciseNotes,
                        onValueChange = { exerciseNotes = it },
                        label = { Text("Instruction Notes (e.g., Squeeze glutes at top)", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Muscle category dropdown selector representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Zone: ", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Chest", "Back", "Legs", "Abs").forEach { muscle ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedMuscle == muscle) NeonGreen else TechBlack)
                                        .clickable { selectedMuscle = muscle }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        muscle,
                                        fontSize = 11.sp,
                                        color = if (selectedMuscle == muscle) TechBlack else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (exerciseName.isNotBlank()) {
                                viewModel.addCustomExercise(exerciseName, selectedMuscle, exerciseNotes)
                                exerciseName = ""
                                exerciseNotes = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("APPEND MOVEMENT TO LIBRARY", color = TechBlack, fontWeight = FontWeight.Bold)
                    }

                    if (customExercises.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "YOUR CREATED LIFTS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        customExercises.take(3).forEach { ce ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ce.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text(ce.targetMuscle, style = MaterialTheme.typography.labelSmall, color = NeonBlue)
                                }

                                IconButton(onClick = { viewModel.deleteCustomExercise(ce.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrightPink, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Feature 15: Friend Challenge Lobby Code
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "WEEKLY LIFT CHAMPIONSHIP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonBlue
                    )
                    Text(
                        "Input a lobby code to sync and compare weekly completions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeCh = challenge
                    if (activeCh == null) {
                        OutlinedTextField(
                            value = lobbyCode,
                            onValueChange = { lobbyCode = it },
                            label = { Text("Invite/Join Code (e.g., LIFT88)", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (lobbyCode.isNotBlank()) {
                                    viewModel.initiateFriendChallenge(lobbyCode)
                                    lobbyCode = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                        ) {
                            Text("CONNECT PARTNER DATABASE", color = TechBlack, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Competition Board Active!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TechBlack, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                "ACTIVE LOBBY: ${activeCh.weekCode}",
                                fontSize = 11.sp,
                                color = NeonBlue,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("YOU", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${viewModel.allWorkouts.collectAsState().value.size} Lifts", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                }

                                Text("vs", color = Color.White.copy(alpha = 0.3f), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(activeCh.opponentName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${activeCh.opponentCompletedCount} Lifts", color = BrightPink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (viewModel.allWorkouts.collectAsState().value.size >= activeCh.opponentCompletedCount) {
                                    "👑 Leaderboard check: You are currently winning!"
                                } else {
                                    "💪 Your partner is ahead! Grind out an extra lift to equalise."
                                },
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Feature 14: Trophy Badges Grid
        item {
            Text(
                "TROPHY SHELF ACHIEVEMENTS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonGreen,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Grid container list elements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stagger badges
                    badges.chunked(2).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            chunk.forEach { badge ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (badge.unlocked) NeonGreen.copy(alpha = 0.08f) else TechBlack.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (badge.unlocked) NeonGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (badge.unlocked) NeonGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (badge.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                                contentDescription = "Badge",
                                                tint = if (badge.unlocked) NeonGreen else Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                badge.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (badge.unlocked) Color.White else Color.White.copy(alpha = 0.4f)
                                            )
                                            Text(
                                                badge.description,
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.6f),
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
