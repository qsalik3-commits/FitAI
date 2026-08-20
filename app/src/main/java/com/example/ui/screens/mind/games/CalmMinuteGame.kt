package com.example.ui.screens.mind.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CalmMinuteGame(
    onGameComplete: (score: Int, accuracy: Int, isPerfect: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var timeLeft by remember { mutableIntStateOf(60) }
    var breathPhase by remember { mutableStateOf("Breathe In") }
    var phaseColor by remember { mutableStateOf(Color(0xFF00E5FF)) }

    // Box breathing phases: Inhale (4s), Hold (4s), Exhale (4s), Hold (4s) -> 16s cycle
    val cycleTimeSec = 16

    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val circleScale by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 16000
                0.35f at 0
                0.90f at 4000 // Inhale 4s
                0.90f at 8000 // Hold 4s
                0.35f at 12000 // Exhale 4s
                0.35f at 16000 // Hold 4s
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "breathScale"
    )

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            val secondInCycle = (60 - timeLeft) % cycleTimeSec
            when (secondInCycle) {
                in 0..3 -> {
                    breathPhase = "Breathe In"
                    phaseColor = Color(0xFF00E5FF)
                    if (secondInCycle == 0) {
                        try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                    }
                }
                in 4..7 -> {
                    breathPhase = "Hold"
                    phaseColor = Color(0xFFC6FF00)
                }
                in 8..11 -> {
                    breathPhase = "Breathe Out"
                    phaseColor = Color(0xFF81D4FA)
                    if (secondInCycle == 8) {
                        try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                    }
                }
                else -> {
                    breathPhase = "Rest & Hold"
                    phaseColor = Color(0xFFB388FF)
                }
            }

            delay(1000L)
            timeLeft--
        }
        onGameComplete(100, 100, true)
    }

    Scaffold(
        containerColor = Color(0xFF0A0E14),
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
                        .background(Color(0xFF161F2E), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("⏱ ", fontSize = 14.sp)
                    Text(
                        "${timeLeft}s",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF161F2E)
                ) {
                    Text(
                        text = "Mindfulness",
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Calm Minute",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gentle box breathing & active presence",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Animated breathing sphere
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f

                    // Outer ambient ring
                    drawCircle(
                        color = phaseColor.copy(alpha = 0.15f),
                        radius = maxRadius * 0.95f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Expanding radial aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = 0.45f),
                                phaseColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxRadius * circleScale
                        ),
                        radius = maxRadius * circleScale,
                        center = center
                    )

                    // Inner crisp orb
                    drawCircle(
                        brush = Brush.linearGradient(
                            listOf(phaseColor, phaseColor.copy(alpha = 0.6f))
                        ),
                        radius = (maxRadius * circleScale) * 0.7f,
                        center = center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = breathPhase,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Gentle reflection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C27)),
                border = androidx.compose.foundation.BorderStroke(1.dp, phaseColor.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Follow the sphere's rhythm softly",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inhale calmly through your nose, hold steadily, and release tension on exhale.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
