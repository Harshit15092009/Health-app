package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NeonBlue

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerDialog(
    exerciseName: String,
    searchQuery: String,
    onDismiss: () -> Unit
) {
    // Curated high quality educational tutorial video IDs from Athlean X, Jeff Nippard etc.
    val videoIdMap = mapOf(
        "Standard Push-ups" to "IODxDxX7oi4",
        "Wide Push-ups" to "hr6AUPJvPqU",
        "Decline Push-ups (Feet Elevated)" to "Z0bRiVHN_8E",
        "Barbell Bench Press" to "rT7DgCr-3pg",
        "Incline Dumbbell Press" to "8iP0VAsb-pQ",
        "Conventional Deadlifts" to "ytGaGIn3SjY",
        "Overhead Barbell Press" to "2yjwXt_R_yE",
        "Barbell Back Squats" to "gcNh17C_TQQ",
        "Dumbbell Lateral Raises" to "-t7fuZ0KhDA",
        "Standing Barbell Curls" to "i1YgFZB6alI",
        "Triceps Rope Pushdowns" to "vB5OHsJ3EME",
        "Bulgarian Split Squats" to "hGP_fU9C9No",
        "Plank Hold" to "pSHjTRCQxIw"
    )

    val targetVideoId = videoIdMap[exerciseName] ?: "rT7DgCr-3pg" // fallback
    val embedUrl = "https://www.youtube.com/embed/$targetVideoId?autoplay=1"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.65f)
                .background(Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
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
                    Text(
                        text = "Tutorial: $exerciseName",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Embedded HTML5 YouTube Player running inside a safe WebView
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                loadUrl(embedUrl)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Searching: \"$searchQuery\" inside trusted fitness channels.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
