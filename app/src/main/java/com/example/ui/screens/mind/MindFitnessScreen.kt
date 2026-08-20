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
            .padding(20.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Column {
                    Text(
                        text = "Mind Fitness",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Take a short break and train your mind.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // Level pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF222818),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Lvl ${mindStats.level} • ${mindStats.levelTitle}",
                    color = Color(0xFFC6FF00),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Daily Mind Minute Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B12))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                fontSize = 12.sp,
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

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isDailyCompletedToday) "Completed for today! Streak maintained (+20 XP)." else "Your 60-second challenge is ready. Boost your focus & streak!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isDailyChallengeRunning = true
                        activeGame = todayChallengeGame
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6FF00))
                ) {
                    Text(
                        text = if (isDailyCompletedToday) "REPLAY TODAY'S CHALLENGE" else "START TODAY'S MIND MINUTE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Stats Overview Grid (4 Cards)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MindStatCard(
                icon = "🧠",
                title = "MIND SCORE",
                value = "${mindStats.mindScore}",
                subtitle = "Cognitive index",
                modifier = Modifier.weight(1f)
            )
            MindStatCard(
                icon = "🔥",
                title = "MIND STREAK",
                value = "${mindStats.streak}d",
                subtitle = "Daily consistency",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MindStatCard(
                icon = "⏱",
                title = "MIND MINUTES",
                value = "${mindStats.totalMinutes}m",
                subtitle = "${mindStats.gamesCompleted} games completed",
                modifier = Modifier.weight(1f)
            )
            MindStatCard(
                icon = "⚡",
                title = "TOTAL XP",
                value = "${mindStats.xp}",
                subtitle = "Next: ${mindStats.nextLevelXp} XP",
                modifier = Modifier.weight(1f)
            )
        }

        // 5 Offline Mini Games Section
        Text(
            text = "Training Mini-Games",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            MindGameType.entries.forEach { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .clickable {
                            isDailyChallengeRunning = false
                            activeGame = game
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFF242424), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(game.iconEmoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = game.subtitle,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF222818)
                            ) {
                                Text(
                                    text = "${game.durationSec}s",
                                    color = Color(0xFFC6FF00),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Play",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
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
