package com.example.ui.screens.mind.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class ColorOption(val name: String, val color: Color)

val stroopColors = listOf(
    ColorOption("RED", Color(0xFFFF334B)),
    ColorOption("BLUE", Color(0xFF29B6F6)),
    ColorOption("GREEN", Color(0xFF00E676)),
    ColorOption("YELLOW", Color(0xFFFFD600)),
    ColorOption("PURPLE", Color(0xFFE040FB)),
    ColorOption("ORANGE", Color(0xFFFF9100))
)

@Composable
fun ColorFocusGame(
    onGameComplete: (score: Int, accuracy: Int, isPerfect: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var timeLeft by remember { mutableIntStateOf(60) }
    var score by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }

    var wordText by remember { mutableStateOf("BLUE") }
    var fontColor by remember { mutableStateOf(ColorOption("RED", Color(0xFFFF334B))) }
    var choices by remember { mutableStateOf<List<ColorOption>>(emptyList()) }

    fun nextQuestion() {
        val available = stroopColors.shuffled()
        val textOpt = available[0]
        val visualOpt = available[1] // Distinct color for the text
        wordText = textOpt.name
        fontColor = visualOpt

        // Pick 4 choices including the correct answer (visualOpt)
        val otherChoices = stroopColors.filter { it.name != visualOpt.name }.shuffled().take(3)
        choices = (otherChoices + visualOpt).shuffled()
    }

    LaunchedEffect(Unit) {
        nextQuestion()
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        val acc = if (totalAttempts > 0) ((correctAnswers.toFloat() / totalAttempts) * 100).toInt() else 0
        val isPerfect = acc >= 90 && correctAnswers >= 12
        onGameComplete(score, acc, isPerfect)
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
                        text = "Score: $score",
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
                    text = "Color Focus",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select the DISPLAY COLOR, not the written word!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            // Word display area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                border = androidx.compose.foundation.BorderStroke(1.dp, fontColor.color.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = wordText,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = fontColor.color,
                            letterSpacing = 3.sp
                        )
                        if (streak > 2) {
                            Text(
                                text = "🔥 $streak Streak! (+${streak * 2})",
                                color = Color(0xFFC6FF00),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // 4 Option Buttons (2x2 Grid)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0..1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0..1) {
                            val idx = row * 2 + col
                            if (idx < choices.size) {
                                val option = choices[idx]
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .clickable {
                                            totalAttempts++
                                            if (option.name == fontColor.name) {
                                                try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                                correctAnswers++
                                                streak++
                                                score += 10 + (streak * 2)
                                            } else {
                                                try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                                                streak = 0
                                                score = (score - 5).coerceAtLeast(0)
                                            }
                                            nextQuestion()
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(option.color, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = option.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stats footer
            Text(
                text = "Accuracy: ${if (totalAttempts > 0) ((correctAnswers * 100) / totalAttempts) else 100}%  |  Correct: $correctAnswers",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}
