package com.example.ui.screens.mind

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FitnessViewModel
import com.example.data.AchievementItem
import com.example.data.MindGameType
import com.example.data.MindStats
import com.example.ui.screens.mind.games.*

@Composable
fun MindFitnessScreen(
    viewModel: FitnessViewModel,
    onNavigateBack: () -> Unit
) {
    val mindStats by viewModel.mindStats.collectAsState()
    val repo = viewModel.mindRepository

    // Navigation state inside Mind Fitness module
    var activeGame by remember { mutableStateOf<MindGameType?>(null) }
    var isDailyChallengeRunning by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

    // Last session result details
    var lastGamePlayed by remember { mutableStateOf<MindGameType?>(null) }
    var lastScore by remember { mutableIntStateOf(0) }
    var lastAccuracy by remember { mutableIntStateOf(0) }
    var lastEarnedXp by remember { mutableIntStateOf(0) }

    val todayChallengeGame = remember(mindStats) { repo.getTodayChallengeGame() }
    val isDailyCompletedToday = remember(mindStats) { repo.isDailyChallengeCompletedToday() }
    val achievements = remember(mindStats) { repo.getAchievements() }

    fun handleGameFinished(game: MindGameType, score: Int, accuracy: Int, isPerfect: Boolean, isDaily: Boolean) {
        val earned = repo.recordGameSession(
            gameType = game,
            score = score,
            accuracy = accuracy,
            isPerfect = isPerfect,
            isDailyChallenge = isDaily,
            sessionMinutes = 1
        )
        lastGamePlayed = game
        lastScore = score
        lastAccuracy = accuracy
        lastEarnedXp = earned
        activeGame = null
        showResults = true
    }

    if (showResults && lastGamePlayed != null) {
        MindResultScreen(
            gameType = lastGamePlayed!!,
            score = lastScore,
            accuracy = lastAccuracy,
            earnedXp = lastEarnedXp,
            streak = mindStats.streak,
            isDailyChallenge = isDailyChallengeRunning,
            onPlayAgain = {
                showResults = false
                activeGame = lastGamePlayed
            },
            onBackToDashboard = {
                showResults = false
                activeGame = null
                isDailyChallengeRunning = false
            }
        )
        return
    }

    // Active Game Routing
    when (activeGame) {
        MindGameType.FOCUS_FLOW -> {
            FocusFlowGame(
                onGameComplete = { score, acc, perf ->
                    handleGameFinished(MindGameType.FOCUS_FLOW, score, acc, perf, isDailyChallengeRunning)
                },
                onBack = {
                    activeGame = null
                    isDailyChallengeRunning = false
                }
            )
            return
        }
        MindGameType.MEMORY_FLASH -> {
            MemoryFlashGame(
                onGameComplete = { score, acc, perf ->
                    handleGameFinished(MindGameType.MEMORY_FLASH, score, acc, perf, isDailyChallengeRunning)
                },
                onBack = {
                    activeGame = null
                    isDailyChallengeRunning = false
                }
            )
            return
        }
        MindGameType.COLOR_FOCUS -> {
            ColorFocusGame(
                onGameComplete = { score, acc, perf ->
                    handleGameFinished(MindGameType.COLOR_FOCUS, score, acc, perf, isDailyChallengeRunning)
                },
                onBack = {
                    activeGame = null
                    isDailyChallengeRunning = false
                }
            )
            return
        }
        MindGameType.NUMBER_FOCUS -> {
            NumberFocusGame(
                onGameComplete = { score, acc, perf ->
                    handleGameFinished(MindGameType.NUMBER_FOCUS, score, acc, perf, isDailyChallengeRunning)
                },
                onBack = {
                    activeGame = null
                    isDailyChallengeRunning = false
                }
            )
            return
        }
        MindGameType.CALM_MINUTE -> {
            CalmMinuteGame(
                onGameComplete = { score, acc, perf ->
                    handleGameFinished(MindGameType.CALM_MINUTE, score, acc, perf, isDailyChallengeRunning)
                },
                onBack = {
                    activeGame = null
                    isDailyChallengeRunning = false
                }
            )
            return
        }
        null -> {
            // Render Mind Fitness Hub
        }
    }

    // Mind Fitness Hub Dashboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
    ) {
        // Header Section with Back button & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Mind Fitness",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Subtitle and Level Badge row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Take a short break and train your mind.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Level pill - horizontal, explicit no-wrap badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF222818),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lvl ${mindStats.level} • ${mindStats.levelTitle}",
                        color = Color(0xFFC6FF00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Daily Mind Minute Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B12))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFC6FF00).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(todayChallengeGame.iconEmoji, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TODAY'S MIND MINUTE",
                                color = Color(0xFFC6FF00),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = todayChallengeGame.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isDailyCompletedToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "✅ Done",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isDailyCompletedToday) "Completed for today! Streak maintained (+20 XP)." else "Your 60-second challenge is ready. Boost your focus & streak!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isDailyChallengeRunning = true
                        activeGame = todayChallengeGame
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6FF00))
                ) {
                    Text(
                        text = if (isDailyCompletedToday) "REPLAY TODAY'S CHALLENGE" else "START TODAY'S MIND MINUTE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Your Mind Progress Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Mind Progress",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mind XP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFC6FF00).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧠", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mind XP",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${mindStats.xp} XP",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mind Streak
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFF9100).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mind Streak",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${mindStats.streak} ${if (mindStats.streak == 1) "Day" else "Days"}",
                                color = Color(0xFFFF9100),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar: Today's progress towards daily challenge
                val todayProgressFraction = if (isDailyCompletedToday) 1f else 0f
                val todayProgressPercent = if (isDailyCompletedToday) 100 else 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's progress",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$todayProgressPercent%",
                        color = Color(0xFFC6FF00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { todayProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFC6FF00),
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
            }
        }

        // Stats Overview Grid (2 Cards: Mind Score & Mind Minutes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MindStatCard(
                icon = "🎯",
                title = "MIND SCORE",
                value = "${mindStats.mindScore}",
                subtitle = "Cognitive index",
                modifier = Modifier.weight(1f)
            )
            MindStatCard(
                icon = "⏱",
                title = "MIND MINUTES",
                value = "${mindStats.totalMinutes}m",
                subtitle = "${mindStats.gamesCompleted} games completed",
                modifier = Modifier.weight(1f)
            )
        }

        // Explore Mind Games Section
        Text(
            text = "Explore Mind Games",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            MindGameType.entries.forEach { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .clickable {
                            isDailyChallengeRunning = false
                            activeGame = game
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF242424), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(game.iconEmoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = game.subtitle,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF222818)
                            ) {
                                Text(
                                    text = "${game.durationSec}s",
                                    color = Color(0xFFC6FF00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Play",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // Achievements Section
        Text(
            text = "Achievements & Badges",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            achievements.forEach { ach ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (ach.isUnlocked) Color(0xFFC6FF00).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isUnlocked) Color(0xFF192014) else Color(0xFF141414)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (ach.isUnlocked) Color(0xFFC6FF00).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ach.iconEmoji,
                                fontSize = 20.sp,
                                color = if (ach.isUnlocked) Color.Unspecified else Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                color = if (ach.isUnlocked) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = ach.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (ach.isUnlocked) Color(0xFFC6FF00).copy(alpha = 0.2f) else Color(0xFF222222)
                        ) {
                            Text(
                                text = if (ach.isUnlocked) "UNLOCKED" else ach.progress,
                                color = if (ach.isUnlocked) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MindStatCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text(icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color(0xFFC6FF00), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
