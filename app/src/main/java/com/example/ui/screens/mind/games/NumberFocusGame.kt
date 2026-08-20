package com.example.ui.screens.mind.games

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

data class NumberItem(
    val value: Int,
    val isTapped: Boolean = false,
    val isWrong: Boolean = false
)

@Composable
fun NumberFocusGame(
    onGameComplete: (score: Int, accuracy: Int, isPerfect: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var currentLevel by remember { mutableIntStateOf(1) } // 1 to 5
    var score by remember { mutableIntStateOf(0) }
    var errors by remember { mutableIntStateOf(0) }
    var nextExpectedNumber by remember { mutableIntStateOf(1) }
    var numbersList by remember { mutableStateOf<List<NumberItem>>(emptyList()) }
    var timeLeft by remember { mutableIntStateOf(60) }

    fun setupRound() {
        val count = when (currentLevel) {
            1 -> 5  // Numbers 1 to 5
            2 -> 6  // 1 to 6
            3 -> 8  // 1 to 8
            4 -> 10 // 1 to 10
            else -> 12 // 1 to 12
        }
        val nums = (1..count).map { NumberItem(value = it) }.shuffled()
        numbersList = nums
        nextExpectedNumber = 1
    }

    LaunchedEffect(currentLevel) {
        setupRound()
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        val acc = (100 - (errors * 10)).coerceIn(30, 100)
        val isPerfect = errors == 0 && currentLevel >= 3
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
                    text = "Number Focus",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap numbers in ascending order: Next is [$nextExpectedNumber]",
                    fontSize = 14.sp,
                    color = Color(0xFFC6FF00),
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of Numbers
            val columns = if (numbersList.size <= 6) 3 else 4
            val rows = (numbersList.size + columns - 1) / columns

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (c in 0 until columns) {
                            val index = r * columns + c
                            if (index < numbersList.size) {
                                val item = numbersList[index]
                                val isFound = item.isTapped

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(
                                            1.dp,
                                            if (isFound) Color(0xFFC6FF00).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable(enabled = !isFound) {
                                            if (item.value == nextExpectedNumber) {
                                                try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                                numbersList = numbersList.mapIndexed { idx, itm ->
                                                    if (idx == index) itm.copy(isTapped = true) else itm
                                                }
                                                score += 10
                                                nextExpectedNumber++

                                                if (nextExpectedNumber > numbersList.size) {
                                                    // Completed current round
                                                    score += 25
                                                    if (currentLevel < 5) {
                                                        currentLevel++
                                                    } else {
                                                        val acc = (100 - (errors * 10)).coerceIn(40, 100)
                                                        onGameComplete(score, acc, errors == 0)
                                                    }
                                                }
                                            } else {
                                                try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                                                errors++
                                                score = (score - 5).coerceAtLeast(0)
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isFound) Color(0xFF142410) else Color(0xFF1E1E1E)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isFound) {
                                            Text(
                                                text = "✓",
                                                fontSize = 24.sp,
                                                color = Color(0xFFC6FF00),
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Text(
                                                text = "${item.value}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom stats bar
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
                            text = "Level $currentLevel / 5",
                            color = Color(0xFFC6FF00),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mistakes: $errors",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            val acc = (100 - (errors * 10)).coerceIn(30, 100)
                            onGameComplete(score, acc, errors == 0 && currentLevel >= 3)
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
