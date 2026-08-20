package com.example.ui.screens.mind.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

@Composable
fun MemoryFlashGame(
    onGameComplete: (score: Int, accuracy: Int, isPerfect: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var currentLevel by remember { mutableIntStateOf(1) } // 1=Beginner, 2=Focused, 3=Sharp, 4=Master
    var score by remember { mutableIntStateOf(0) }
    var errors by remember { mutableIntStateOf(0) }
    var roundState by remember { mutableStateOf("MEMORIZE") } // MEMORIZE, RECALL, SUCCESS, FAILED, FINISHED

    // Available symbols
    val allSymbols = listOf("🌟", "⚡", "🍀", "🔥", "💎", "🌙", "🍎", "🚀", "🪐", "🏆", "🎯", "⚓")
    
    // Config for each level
    val gridCount = when (currentLevel) {
        1 -> 4 // 2x2
        2 -> 6 // 3x2
        3 -> 9 // 3x3
        else -> 12 // 4x3
    }
    
    val targetItemsCount = when (currentLevel) {
        1 -> 2
        2 -> 3
        3 -> 4
        else -> 5
    }

    var targetIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var countdownSec by remember { mutableIntStateOf(3) }

    fun startNewRound() {
        selectedIndices = emptySet()
        val generated = mutableSetOf<Int>()
        while (generated.size < targetItemsCount) {
            generated.add((0 until gridCount).random())
        }
        targetIndices = generated
        roundState = "MEMORIZE"
        countdownSec = if (currentLevel <= 2) 3 else 2
    }

    LaunchedEffect(currentLevel) {
        startNewRound()
    }

    // Countdown while memorizing
    LaunchedEffect(roundState, countdownSec) {
        if (roundState == "MEMORIZE") {
            if (countdownSec > 0) {
                delay(1000L)
                countdownSec--
            } else {
                roundState = "RECALL"
            }
        }
    }

    val levelName = when (currentLevel) {
        1 -> "Level 1 • Beginner"
        2 -> "Level 2 • Focused"
        3 -> "Level 3 • Sharp"
        else -> "Level 4 • Master"
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

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1E1E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = levelName,
                        color = Color(0xFFC6FF00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1E1E)
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
                    text = "Memory Flash",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (roundState == "MEMORIZE") "Memorize highlighted positions ($countdownSec s)" else "Tap the hidden positions!",
                    fontSize = 14.sp,
                    color = if (roundState == "MEMORIZE") Color(0xFF00E5FF) else Color(0xFFC6FF00),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cards Grid
            val columns = if (gridCount <= 4) 2 else 3
            val rows = (gridCount + columns - 1) / columns

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (c in 0 until columns) {
                            val index = r * columns + c
                            if (index < gridCount) {
                                val isTarget = targetIndices.contains(index)
                                val isSelected = selectedIndices.contains(index)
                                val isRevealed = roundState == "MEMORIZE" && isTarget
                                
                                val cardBg = when {
                                    isRevealed -> Color(0xFFC6FF00)
                                    isSelected && isTarget -> Color(0xFF00E676)
                                    isSelected && !isTarget -> Color(0xFFFF5252)
                                    else -> Color(0xFF1C1C1C)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(
                                            1.dp,
                                            if (isRevealed || (isSelected && isTarget)) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.08f),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable(enabled = roundState == "RECALL" && !isSelected) {
                                            if (roundState == "RECALL") {
                                                val nextSelected = selectedIndices + index
                                                selectedIndices = nextSelected

                                                if (isTarget) {
                                                    try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                                    score += 10
                                                    // Check if all targets found
                                                    if (nextSelected.containsAll(targetIndices)) {
                                                        if (currentLevel < 4) {
                                                            currentLevel++
                                                        } else {
                                                            // Completed all levels!
                                                            val finalAcc = if (errors == 0) 100 else (100 - (errors * 15)).coerceAtLeast(40)
                                                            onGameComplete(score + 50, finalAcc, errors == 0)
                                                        }
                                                    }
                                                } else {
                                                    try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                                                    errors++
                                                    if (errors >= 3) {
                                                        val finalAcc = (score * 2).coerceIn(30, 85)
                                                        onGameComplete(score, finalAcc, false)
                                                    }
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (isRevealed || (isSelected && isTarget)) {
                                            Text(
                                                text = "✨",
                                                fontSize = 28.sp
                                            )
                                        } else if (isSelected && !isTarget) {
                                            Text("❌", fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom status & controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Target Items: ${targetIndices.size}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Errors: $errors / 3",
                            color = if (errors >= 2) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            val finalAcc = ((currentLevel.toFloat() / 4f) * 100).toInt()
                            onGameComplete(score, finalAcc, errors == 0 && currentLevel >= 3)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
