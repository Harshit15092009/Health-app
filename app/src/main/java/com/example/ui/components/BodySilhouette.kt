package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.*
import kotlin.math.sqrt

// Define Body Part Data for Canvas Rendering
data class BodyPartShape(
    val name: String,
    val centerRelX: Float, // Relative Coordinate (0.0 to 1.0)
    val centerRelY: Float,
    val radiusRel: Float,
    val muscleGroup: String // Map to exercises
)

val BodyPartShapesList = listOf(
    BodyPartShape("Neck", 0.5f, 0.22f, 0.05f, "Neck"),
    BodyPartShape("Chest", 0.5f, 0.32f, 0.08f, "Chest"),
    BodyPartShape("Shoulders", 0.5f, 0.28f, 0.14f, "Shoulders"),
    BodyPartShape("Biceps", 0.38f, 0.38f, 0.05f, "Biceps"),
    BodyPartShape("Forearms", 0.34f, 0.48f, 0.04f, "Forearms"),
    BodyPartShape("Waist", 0.5f, 0.45f, 0.07f, "Abs"),
    BodyPartShape("Hips", 0.5f, 0.54f, 0.07f, "Legs"),
    BodyPartShape("Thighs", 0.5f, 0.68f, 0.10f, "Legs"),
    BodyPartShape("Calves", 0.5f, 0.82f, 0.08f, "Legs")
)

@Composable
fun BodySilhouette(
    modifier: Modifier = Modifier,
    partColors: Map<String, Color> = emptyMap(), // Map of part name -> Color (Green, Yellow, Red)
    heatmapMuscles: List<String> = emptyList(),  // List of muscle names that should glow red/orange
    onPartTapped: (String) -> Unit
) {
    // Pulsing Animation for glowing tapped part
    val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )
    val pulseRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    var selectedPart by remember { mutableStateOf<String?>(null) }
    var lastTapOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height

                    // Find closest body part clicked
                    var closestPart: BodyPartShape? = null
                    var minDistance = Float.MAX_VALUE

                    for (part in BodyPartShapesList) {
                        val px = part.centerRelX * width
                        val py = part.centerRelY * height
                        val distance = sqrt((offset.x - px) * (offset.x - px) + (offset.y - py) * (offset.y - py))
                        val tapRadius = part.radiusRel * width * 1.5f // Expand clickable area slightly for fingers

                        if (distance < tapRadius && distance < minDistance) {
                            closestPart = part
                            minDistance = distance
                        }
                    }

                    if (closestPart != null) {
                        selectedPart = closestPart.name
                        lastTapOffset = offset
                        onPartTapped(closestPart.name)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Background Grid System for techy look
        drawSpaceGrid(w, h)

        // 2. Draw Human Base Contour Silhouette
        drawBaseContourSilhouette(w, h)

        // 3. Draw Body Part Hotspots
        for (part in BodyPartShapesList) {
            val cx = part.centerRelX * w
            val cy = part.centerRelY * h
            val radius = part.radiusRel * w

            // Decide color based on Mode
            val color = if (heatmapMuscles.contains(part.muscleGroup) || heatmapMuscles.contains(part.name)) {
                // Glow Heatmap Overlay
                StatusRed.copy(alpha = 0.85f)
            } else {
                partColors[part.name] ?: NeonBlue.copy(alpha = 0.3f)
            }

            // Draw glowing background for trained or weak highlights
            drawCircle(
                color = color.copy(alpha = 0.15f),
                radius = radius * 1.4f,
                center = Offset(cx, cy)
            )

            // Draw inner control ring
            drawCircle(
                color = color,
                radius = radius * 0.7f,
                center = Offset(cx, cy),
                style = Stroke(width = dpToPx(3f))
            )

            // Dynamic pulsing cursor overlay on selected parts
            if (part.name == selectedPart) {
                drawCircle(
                    color = color.copy(alpha = pulseAlpha),
                    radius = radius * pulseRadiusScale,
                    center = Offset(cx, cy),
                    style = Stroke(width = dpToPx(6f))
                )
            }
        }
    }
}

private fun DrawScope.drawSpaceGrid(w: Float, h: Float) {
    val cols = 8
    val rows = 12
    val colW = w / cols
    val rowH = h / rows

    for (i in 1 until cols) {
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(i * colW, 0f),
            end = Offset(i * colW, h),
            strokeWidth = 1f
        )
    }
    for (i in 1 until rows) {
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(0f, i * rowH),
            end = Offset(w, i * rowH),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawBaseContourSilhouette(w: Float, h: Float) {
    // Elegant humanoid geometric shapes to look futuristic
    // Head & Brain
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NeonBlue.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.15f),
            radius = w * 0.13f
        ),
        radius = w * 0.09f,
        center = Offset(w * 0.5f, h * 0.15f)
    )

    // Neck
    drawRoundRect(
        color = Color.White.copy(alpha = 0.1f),
        topLeft = Offset(w * 0.46f, h * 0.20f),
        size = Size(w * 0.08f, h * 0.04f),
        cornerRadius = CornerRadius(dpToPx(4f), dpToPx(4f))
    )

    // Torso / Shoulders
    drawRoundRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = Offset(w * 0.35f, h * 0.24f),
        size = Size(w * 0.3f, h * 0.14f),
        cornerRadius = CornerRadius(dpToPx(20f), dpToPx(20f))
    )

    // Abs / Core
    drawRoundRect(
        color = Color.White.copy(alpha = 0.07f),
        topLeft = Offset(w * 0.38f, h * 0.38f),
        size = Size(w * 0.24f, h * 0.15f),
        cornerRadius = CornerRadius(dpToPx(12f), dpToPx(12f))
    )

    // Pelvis
    drawRoundRect(
        color = Color.White.copy(alpha = 0.09f),
        topLeft = Offset(w * 0.37f, h * 0.53f),
        size = Size(w * 0.26f, h * 0.06f),
        cornerRadius = CornerRadius(dpToPx(12f), dpToPx(12f))
    )

    // Left Arm
    drawRoundRect(
        color = Color.White.copy(alpha = 0.05f),
        topLeft = Offset(w * 0.28f, h * 0.24f),
        size = Size(w * 0.06f, h * 0.26f),
        cornerRadius = CornerRadius(dpToPx(10f), dpToPx(10f))
    )

    // Right Arm
    drawRoundRect(
        color = Color.White.copy(alpha = 0.05f),
        topLeft = Offset(w * 0.66f, h * 0.24f),
        size = Size(w * 0.06f, h * 0.26f),
        cornerRadius = CornerRadius(dpToPx(10f), dpToPx(10f))
    )

    // Left Leg
    drawRoundRect(
        color = Color.White.copy(alpha = 0.06f),
        topLeft = Offset(w * 0.37f, h * 0.60f),
        size = Size(w * 0.11f, h * 0.30f),
        cornerRadius = CornerRadius(dpToPx(12f), dpToPx(12f))
    )

    // Right Leg
    drawRoundRect(
        color = Color.White.copy(alpha = 0.06f),
        topLeft = Offset(w * 0.52f, h * 0.60f),
        size = Size(w * 0.11f, h * 0.30f),
        cornerRadius = CornerRadius(dpToPx(12f), dpToPx(12f))
    )
}

// Convert Dp to exact Pixel values for drawings
private fun DrawScope.dpToPx(dpVal: Float): Float {
    return dpVal * density
}
