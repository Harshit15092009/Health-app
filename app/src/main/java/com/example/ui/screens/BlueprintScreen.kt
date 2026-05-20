package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.data.FitnessFormulas
import com.example.data.MeasurementLog
import com.example.ui.FitnessViewModel
import com.example.ui.components.BodySilhouette
import com.example.ui.theme.*

@Composable
fun BlueprintScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val rawLog = latestMeasurement ?: MeasurementLog()

    var showEditorForPart by remember { mutableStateOf<String?>(null) }
    var currentWorkingLog by remember { mutableStateOf(rawLog) }

    // Resync working state when raw database data refreshes
    LaunchedEffect(latestMeasurement) {
        latestMeasurement?.let {
            currentWorkingLog = it
        }
    }

    // Evaluate current proportions to paint the vector shapes
    val proportionEvaluation = remember(rawLog) {
        FitnessFormulas.evaluateProportions(rawLog)
    }

    // Map evaluate evaluations to actual Compose Colors
    val partColors = remember(proportionEvaluation) {
        proportionEvaluation.mapValues { (_, status) ->
            when (status.colorCode) {
                "GREEN" -> StatusGreen
                "YELLOW" -> StatusYellow
                else -> StatusRed
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Blueprint header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BODY BLUEPRINT MODEL",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = NeonBlue
            )
            Text(
                text = "Tap on hotspots of the 2D vector model to log measurements. Areas are color-coded based on ideal proportions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Two Column: 2D Interactive Model on Left, Details summary on Right
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(12.dp)
                ) {
                    // Feature 1: Canvas Human Body Blueprint Silhouette
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        BodySilhouette(
                            partColors = partColors,
                            onPartTapped = { clickedPart ->
                                showEditorForPart = clickedPart
                                viewModel.speakCoaching("Editing $clickedPart dimensions.")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Detail Proportions checklist
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "COMPOSITION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )

                        // Scrollable indices
                        proportionEvaluation.values.take(6).forEach { status ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val indicatorColor = when (status.colorCode) {
                                    "GREEN" -> StatusGreen
                                    "YELLOW" -> StatusYellow
                                    else -> StatusRed
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = status.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "${status.actualValue}cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = indicatorColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trigger edit parameters overlay
                        Button(
                            onClick = { showEditorForPart = "All" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit All", modifier = Modifier.size(16.dp), tint = TechBlack)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EDIT VALUES", fontSize = 11.sp, color = TechBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Feature 10: Transformation Roadmap Timeline
        item {
            TransformationRoadmapTimeline(rawLog)
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Modal Dimension Dialog
    if (showEditorForPart != null) {
        val editingPart = showEditorForPart!!
        AlertDialog(
            onDismissRequest = { showEditorForPart = null },
            title = {
                Text(
                    text = if (editingPart == "All") "Global Metrics Logger" else "Edit $editingPart Dimensions",
                    color = NeonBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editingPart == "All") {
                        MetricInputField("Height (cm)", currentWorkingLog.height) { currentWorkingLog = currentWorkingLog.copy(height = it) }
                        MetricInputField("Weight (kg)", currentWorkingLog.weight) { currentWorkingLog = currentWorkingLog.copy(weight = it) }
                        MetricInputField("Age (Years)", currentWorkingLog.age.toDouble()) { currentWorkingLog = currentWorkingLog.copy(age = it.toInt()) }
                        MetricInputField("Body Fat % (Optional)", currentWorkingLog.bodyFat ?: 15.0) { currentWorkingLog = currentWorkingLog.copy(bodyFat = it) }
                    } else {
                        // Edit a specific muscle part
                        val initialVal = when (editingPart) {
                            "Neck" -> currentWorkingLog.neck
                            "Chest" -> currentWorkingLog.chest
                            "Waist" -> currentWorkingLog.waist
                            "Hips" -> currentWorkingLog.hips
                            "Shoulders" -> currentWorkingLog.shoulders
                            "Biceps" -> currentWorkingLog.biceps
                            "Forearms" -> currentWorkingLog.forearms
                            "Thighs" -> currentWorkingLog.thighs
                            "Calves" -> currentWorkingLog.calves
                            else -> 0.0
                        }

                        MetricInputField("$editingPart Circ. (cm)", initialVal) { newVal ->
                            currentWorkingLog = when (editingPart) {
                                "Neck" -> currentWorkingLog.copy(neck = newVal)
                                "Chest" -> currentWorkingLog.copy(chest = newVal)
                                "Waist" -> currentWorkingLog.copy(waist = newVal)
                                "Hips" -> currentWorkingLog.copy(hips = newVal)
                                "Shoulders" -> currentWorkingLog.copy(shoulders = newVal)
                                "Biceps" -> currentWorkingLog.copy(biceps = newVal)
                                "Forearms" -> currentWorkingLog.copy(forearms = newVal)
                                "Thighs" -> currentWorkingLog.copy(thighs = newVal)
                                "Calves" -> currentWorkingLog.copy(calves = newVal)
                                else -> currentWorkingLog
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveMeasurements(currentWorkingLog)
                        showEditorForPart = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("SAVE CHANGES", color = TechBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditorForPart = null }) {
                    Text("CANCEL", color = BrightPink)
                }
            },
            containerColor = DeepSlate
        )
    }
}

@Composable
fun MetricInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    var textState by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textState,
        onValueChange = {
            textState = it
            it.toDoubleOrNull()?.let { d -> onValueChange(d) }
        },
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = NeonBlue,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TransformationRoadmapTimeline(log: MeasurementLog) {
    val bmi = FitnessFormulas.calculateBmi(log.weight, log.height)
    val idealWeight = (log.height - 100) * 0.9 // Simple Miller Formula Target

    val deltaWeight = log.weight - idealWeight
    val isCutting = deltaWeight > 0

    // Pacing calculations (Safe 0.5 kg weekly adjust)
    val weeksNeeded = (kotlin.math.abs(deltaWeight) / 0.5).coerceIn(4.0, 16.0).toInt()

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
                Column {
                    Text(
                        text = "TRANSFORMATION ROADMAP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonBlue
                    )
                    Text(
                        text = "Pace: 0.5kg Adjustments Weekly",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$weeksNeeded WEEKS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create 4 milestones timeline
            val milestones = if (isCutting) {
                listOf(
                    Pair("Week 1-3", "Metabolic Kickstart: Reducing glycogen reserves, establishing high neural-adaptations push/pull routines."),
                    Pair("Week 4-7", "Fat Shield Melting: Elevated fat oxidation, visible changes in core Waist measurements."),
                    Pair("Week 8-10", "Recomposition peak: Standardized protein absorption feeds building muscles as subcutaneous fat levels decrease."),
                    Pair("Week Last", "Target Attainment: Stabilization of weight at ${idealWeight.toInt()}kg, locking in your Blueprint.")
                )
            } else {
                listOf(
                    Pair("Week 1-3", "Anabolic Signaling: Caloric surplus initiates muscle protein synthesis signaling, baseline lift progressions."),
                    Pair("Week 4-7", "Strength Explosion: Hypertrophy responses fully activate in Chest and Thigh focus sectors."),
                    Pair("Week 8-10", "Bulking Adaptation: Stabilization of new body masses. Biceps and Shoulder proportions approach Steve Reeves ideal Ratios."),
                    Pair("Week Last", "Perfect Build: Weight achieved at ${idealWeight.toInt()}kg with peak muscle density.")
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                milestones.forEachIndexed { idx, pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline bar element
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == 0) NeonGreen else Color.White.copy(alpha = 0.15f))
                            )
                            if (idx < 3) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(Color.White.copy(alpha = 0.1f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (idx == 0) NeonGreen else Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = pair.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
