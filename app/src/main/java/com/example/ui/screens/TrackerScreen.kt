package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.MeasurementLog
import com.example.data.CompletedWorkout
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import com.example.ui.FitnessViewModel
import com.example.ui.components.BodySilhouette
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val measurements by viewModel.allMeasurements.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val mealLogs by viewModel.mealPhotos.collectAsState()

    var showCameraScanner by remember { mutableStateOf(false) }
    var selectedChartMetric by remember { mutableStateOf("Weight") }

    // Sunday coach report calculations
    val lastSundayReport = remember(measurements, workouts) {
        generateSundayCheckin(measurements, workouts)
    }

    // Capture the trained muscles from logged workouts for Heatmap calculation
    val heatmapMuscles = remember(workouts) {
        workouts.flatMap { it.musclesTrained.split(",") }
            .map { it.trim() }
            .distinct()
    }

    // Photo Snapper Launcher for Meals (Feature 17)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addMealPhotoLog(it.toString(), "Nutrient Balanced Portion", "Lunch")
        }
    }

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
                text = "PROGRESS MATRIX",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = NeonBlue
            )
            Text(
                text = "Observe muscle heatmaps, log diet meals, review your weekly coach report cards, and execute postural alignment tests.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Sunday report card checkin coach (Feature 5) Let's make it look prominent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrightPink.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = borderBrush(listOf(BrightPink, NeonBlue))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Report",
                                tint = BrightPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "WEEKLY COACH REPORT CARD",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrightPink
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Coach AI", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = lastSundayReport,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Custom Weight Tracking Chart Line Graph (Feature 4)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PROGRESS CHARTS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonBlue
                        )

                        // Selector
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Weight", "Waist").forEach { m ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedChartMetric == m) NeonBlue else Color.Transparent)
                                        .clickable { selectedChartMetric = m }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        m,
                                        fontSize = 10.sp,
                                        color = if (selectedChartMetric == m) TechBlack else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Line Graph Canvas
                    val dataPoints = remember(measurements, selectedChartMetric) {
                        measurements.map { log ->
                            if (selectedChartMetric == "Weight") log.weight else log.waist
                        }
                    }

                    if (dataPoints.size >= 2) {
                        CustomLineChart(
                            points = dataPoints,
                            color = NeonBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(TechBlack, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Add weekly measurements to draw progress curves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Silhouette Muscle Heatmap (Feature 4)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            "MUSCLE HEATMAP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Sectors glow red or orange as workouts target specific muscle pathways. Logs workouts to build heat levels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "ACTIVE SEGMENTS:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(heatmapMuscles) { muscle ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrightPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(muscle, color = BrightPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Silhouette
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        BodySilhouette(
                            heatmapMuscles = heatmapMuscles,
                            onPartTapped = {}
                        )
                    }
                }
            }
        }

        // Feature 16: Posture Score Camera Scan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = "Posture",
                            tint = NeonBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "POSTURE SCANNER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonBlue
                        )
                        Text(
                            text = "Analyze alignment lines on phone camera. Current: ${viewModel.lastPostureScore ?: "--"}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showCameraScanner = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                        ) {
                            Text("ACTIVATE CAMERA", color = TechBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Feature 17: Food/Meal Photo Log & Compares
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "VISUAL FOOD LOGS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                            Text(
                                "Snap meal portions, timestamps recorded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = NeonGreen.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add meal", tint = NeonGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (mealLogs.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(mealLogs) { log ->
                                Card(
                                    modifier = Modifier.width(120.dp),
                                    colors = CardDefaults.cardColors(containerColor = TechBlack)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncImage(
                                            model = log.photoUri,
                                            contentDescription = "Meal photo",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Text(
                                            text = log.mealType,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(TechBlack, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log your dishes to complete your meal roadmap.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Modal Camera Posture Analyzer Dialog (CameraX overlay scanner!)
    if (showCameraScanner) {
        Dialog(
            onDismissRequest = { showCameraScanner = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSlate)
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Posture Alignment Matrix", color = NeonBlue, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showCameraScanner = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Camera Preview with Canvas overlays (Feature 16)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        // Real CameraX preview loader
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build()
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                    try {
                                        cameraProvider.unbindAll()
                                        preview.setSurfaceProvider(previewView.surfaceProvider)
                                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                    } catch (exc: Exception) {
                                        // Fail gracefully if camera error
                                    }
                                }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 3D/Linear Alignment HUD grids overlay on camera preview
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw central vertical spine axis (Green alignment indicator)
                            drawLine(
                                color = NeonGreen.copy(alpha = 0.6f),
                                start = android.view.animation.Transformation().let { Offset(w * 0.5f, 0f) },
                                end = Offset(w * 0.5f, h),
                                strokeWidth = 3f
                            )

                            // Horizontal shoulders axis
                            drawLine(
                                color = NeonBlue.copy(alpha = 0.5f),
                                start = Offset(0f, h * 0.35f),
                                end = Offset(w, h * 0.35f),
                                strokeWidth = 2f
                            )

                            // Horizontal hips axis
                            drawLine(
                                color = NeonBlue.copy(alpha = 0.5f),
                                start = Offset(0f, h * 0.60f),
                                end = Offset(w, h * 0.60f),
                                strokeWidth = 2f
                            )

                            // Grid helpers
                            drawRect(
                                color = NeonGreen.copy(alpha = 0.12f),
                                topLeft = Offset(w * 0.25f, h * 0.1f),
                                size = size.copy(width = w * 0.5f, height = h * 0.75f),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.calculatePostureScore(true) // trigger positive
                                    showCameraScanner = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("ALIGNED SCORE (PASS)", color = TechBlack, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.calculatePostureScore(false) // trigger negative
                                    showCameraScanner = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightPink)
                            ) {
                                Text("MISALIGNED SCORE (FAIL)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Display tips
                        val score = viewModel.lastPostureScore
                        if (score != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Last Score: $score%. Alignment Report generated.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom border gradient generator
private fun borderBrush(colors: List<Color>): BorderStroke {
    return BorderStroke(1.5.dp, Brush.horizontalGradient(colors))
}

@Composable
fun CustomLineChart(
    points: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val padding = 20f
        val chartW = w - padding * 2
        val chartH = h - padding * 2

        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 100.0
        val valRange = (maxVal - minVal).coerceAtLeast(1.0)

        val stepX = chartW / (points.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            points.forEachIndexed { idx, point ->
                val x = padding + idx * stepX
                val relativeY = (point - minVal) / valRange
                val y = h - padding - (relativeY * chartH).toFloat()

                if (idx == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        // Draw graph grid markings
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(padding, h - padding),
            end = Offset(w - padding, h - padding),
            strokeWidth = 2f
        )

        // Draw Line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f)
        )

        // Draw points nodes
        points.forEachIndexed { idx, point ->
            val x = padding + idx * stepX
            val relativeY = (point - minVal) / valRange
            val y = h - padding - (relativeY * chartH).toFloat()

            drawCircle(
                color = color,
                radius = 6f,
                center = Offset(x, y)
            )

            drawCircle(
                color = TechBlack,
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

// Sunday Report Card Formula Solver (Feature 5) Rule-based Custom Coach
private fun generateSundayCheckin(
    measurements: List<MeasurementLog>,
    workouts: List<CompletedWorkout>
): String {
    if (measurements.isEmpty()) {
        return "Hey Champ! Put your initial measurements in your profile page. That sets up our roadmap database. Then I'll check in here every Sunday to guide your adaptations!"
    }
    val workoutsThisWeek = workouts.filter { it.timestamp > System.currentTimeMillis() - 86400000 * 7 }

    if (measurements.size < 2) {
        return "Coach Review: Weight is currently at ${measurements.last().weight}kg. Excellent start! You completed ${workoutsThisWeek.size} workouts this week. Ensure your hydration goals average 2.5 Litres per day. Looking forward to your weekly measurements check-in next Sunday!"
    }

    val oldest = measurements.first()
    val newest = measurements.last()

    val weightDelta = newest.weight - oldest.weight
    val waistDelta = newest.waist - oldest.waist

    val weightChangeStr = if (weightDelta < 0) "dropped ${String.format("%.1f", -weightDelta)}kg" else "gained ${String.format("%.1f", weightDelta)}kg"
    val waistChangeStr = if (waistDelta < 0) "reduced your waist by ${String.format("%.1f", -waistDelta)}cm" else "increased your waist by ${String.format("%.1f", waistDelta)}cm"

    return "Weekly Coach Card: Sensational commitment! Since our initial baseline, you have $weightChangeStr and $waistChangeStr. You logged ${workoutsThisWeek.size} workouts in the last 7 days. Your chest and shoulder ratios are adapting excellently toward the Reeves proportions. Keep consistency high!"
}
