package com.example.ui.screens.mind.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun FocusFlowGame(
    onGameComplete: (score: Int, accuracy: Int, isPerfect: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var timeLeft by remember { mutableIntStateOf(60) }
    var totalTaps by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var perfectHits by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("Tap when the ripple hits the glowing target ring!") }
    var feedbackColor by remember { mutableStateOf(Color(0xFFC6FF00)) }

    // Pulsing circle animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val cycleDuration = remember(timeLeft) {
        // Increases speed as time decreases (60s -> 2000ms down to 1100ms)
        (1100 + (timeLeft * 15)).coerceIn(1000, 2000)
    }

    val radiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    val targetFraction = 0.65f
    val tolerance = 0.12f

    // 60-Second Timer
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        val accuracy = if (totalTaps > 0) ((hits.toFloat() / totalTaps) * 100).toInt().coerceIn(0, 100) else if (hits > 0) 80 else 40
        val isPerfect = accuracy >= 85 && hits >= 10
        onGameComplete(hits * 10, accuracy, isPerfect)
    }

    Scaffold(
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("⏱ ", fontSize = 14.sp)
                    Text(
                        "${timeLeft}s",
                        fontWeight = FontWeight.Bold,
                        color = if (timeLeft <= 10) Color(0xFFFF5252) else Color(0xFFC6FF00),
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1E1E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Score: ${hits * 10}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Focus Flow",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Sync your breath & tap with precision",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Interactive Circle Area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF151515))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            totalTaps++
                            val diff = abs(radiusFraction - targetFraction)
                            if (diff <= 0.05f) {
                                hits++
                                perfectHits++
                                feedbackText = "✨ Perfect Timing! +20"
                                feedbackColor = Color(0xFFC6FF00)
                                try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                            } else if (diff <= tolerance) {
                                hits++
                                feedbackText = "🎯 Great Hit! +10"
                                feedbackColor = Color(0xFF00E676)
                                try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                            } else {
                                feedbackText = if (radiusFraction < targetFraction) "Too early — wait for ring" else "A bit late — stay steady"
                                feedbackColor = Color(0xFFFFB300)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f

                    // 1. Target Zone Ring
                    drawCircle(
                        color = Color(0xFFC6FF00).copy(alpha = 0.25f),
                        radius = maxRadius * targetFraction,
                        center = center,
                        style = Stroke(width = 24.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFC6FF00),
                        radius = maxRadius * targetFraction,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 2. Expanding / Contracting Ripple
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFC6FF00).copy(alpha = 0.4f),
                                Color(0xFF00E676).copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxRadius * radiusFraction
                        ),
                        radius = maxRadius * radiusFraction,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFC6FF00),
                        radius = maxRadius * radiusFraction,
                        center = center,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // 3. Center Anchor
                    drawCircle(
                        color = Color(0xFFC6FF00),
                        radius = 8.dp.toPx(),
                        center = center
                    )
                }

                Text(
                    text = "TAP",
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
            }

            // Real-time Feedback banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                border = androidx.compose.foundation.BorderStroke(1.dp, feedbackColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = feedbackText,
                        color = feedbackColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hits: $hits | Perfect: $perfectHits",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
