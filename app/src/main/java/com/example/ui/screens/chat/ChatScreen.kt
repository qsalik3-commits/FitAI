package com.example.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.FitnessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    fitnessViewModel: FitnessViewModel,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val selectedRole by chatViewModel.selectedRole.collectAsState()
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val includeStatsContext by chatViewModel.includeStatsContext.collectAsState()

    // Fitness stats from FitnessViewModel
    val caloriesBurned by fitnessViewModel.caloriesBurnedToday.collectAsState()
    val caloriesConsumed by fitnessViewModel.caloriesConsumedToday.collectAsState()
    val waterLitres by fitnessViewModel.waterLitresToday.collectAsState()
    val currentStreak by fitnessViewModel.currentStreak.collectAsState()

    val fitnessContextString = remember(caloriesBurned, caloriesConsumed, waterLitres, currentStreak) {
        "Calories Burned Today: ${caloriesBurned ?: 0} kcal | Calories Consumed Today: ${caloriesConsumed ?: 0} kcal | Water Intake: ${String.format(java.util.Locale.US, "%.1f", waterLitres)} L | Current Streak: $currentStreak days"
    }

    var inputText by remember { mutableStateOf("") }
    var showModelBottomSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll when new messages arrive or loading state changes
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Conversation", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset the chat history?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatViewModel.clearChat()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showModelBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelBottomSheet = false },
            containerColor = Color(0xFF161616),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFFC6FF00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Gemini AI Model",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                availableModels.forEach { model ->
                    val isSelected = selectedModel == model.modelId
                    Card(
                        onClick = {
                            chatViewModel.selectModel(model.modelId)
                            showModelBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF222818) else Color(0xFF1E1E1E)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFC6FF00) else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = if (isSelected) Color(0xFFC6FF00).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = model.badge,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.description,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    lineHeight = 16.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFFC6FF00),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFC6FF00), Color(0xFF00E676))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedRole.iconEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedRole.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                            }
                            Text(
                                text = selectedRole.subtitle,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Model Selection Pill
                    Surface(
                        onClick = { showModelBottomSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF242424),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFC6FF00),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val shortName = when (selectedModel) {
                                "gemini-3.1-pro-preview" -> "3.1 Pro"
                                "gemini-3.1-flash-lite-preview" -> "Flash-Lite"
                                else -> "3.5 Flash"
                            }
                            Text(
                                text = shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC6FF00)
                            )
                        }
                    }

                    // Clear Chat Button
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101010)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Role Selection Chips
            RoleSelectionRow(
                selectedRole = selectedRole,
                onRoleSelected = { role -> chatViewModel.selectRole(role) }
            )

            // Health Context Sync Indicator Bar
            FitnessContextBar(
                includeContext = includeStatsContext,
                onToggle = { chatViewModel.toggleStatsContext() },
                caloriesBurned = caloriesBurned ?: 0,
                caloriesConsumed = caloriesConsumed ?: 0,
                waterLitres = waterLitres,
                streak = currentStreak
            )

            // Chat Messages Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                if (isLoading) {
                    item {
                        TypingIndicatorBubble(role = selectedRole)
                    }
                }
            }

            // Quick Prompt Suggestions (shown if conversation is short or user wants quick ideas)
            if (messages.size <= 2 && !isLoading) {
                SuggestedPromptsSection(
                    prompts = selectedRole.suggestedPrompts,
                    onPromptClick = { prompt ->
                        chatViewModel.sendMessage(
                            userText = prompt,
                            fitnessContext = if (includeStatsContext) fitnessContextString else null
                        )
                    }
                )
            }

            // Chat Input Bar
            ChatInputBar(
                inputText = inputText,
                onTextChanged = { inputText = it },
                isLoading = isLoading,
                onSend = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        focusManager.clearFocus()
                        chatViewModel.sendMessage(
                            userText = textToSend,
                            fitnessContext = if (includeStatsContext) fitnessContextString else null
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun RoleSelectionRow(
    selectedRole: CoachRole,
    onRoleSelected: (CoachRole) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF101010))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CoachRole.entries) { role ->
            val isSelected = selectedRole == role
            Surface(
                onClick = { onRoleSelected(role) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFFC6FF00).copy(alpha = 0.15f) else Color(0xFF1E1E1E),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = role.iconEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = role.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFFC6FF00) else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun FitnessContextBar(
    includeContext: Boolean,
    onToggle: () -> Unit,
    caloriesBurned: Int,
    caloriesConsumed: Int,
    waterLitres: Float,
    streak: Int
) {
    Surface(
        color = Color(0xFF151515),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (includeContext) Icons.Default.Analytics else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (includeContext) Color(0xFF00E676) else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (includeContext) {
                        "FitAI Context Synced: 🔥 ${caloriesBurned}kcal • 🍎 ${caloriesConsumed}kcal • 💧 ${String.format(java.util.Locale.US, "%.1f", waterLitres)}L • ⚡ ${streak}d"
                    } else {
                        "FitAI Context Off (Tap to sync your workout & calorie data)"
                    },
                    fontSize = 11.sp,
                    color = if (includeContext) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = includeContext,
                onCheckedChange = { onToggle() },
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00E676),
                    checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                    uncheckedTrackColor = Color(0xFF2A2A2A)
                )
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
                    .border(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.roleUsed?.iconEmoji ?: "🤖",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = when {
                    message.isError -> Color(0xFF331111)
                    isUser -> Color(0xFF2E3B18)
                    else -> Color(0xFF1C1C1E)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        message.isError -> Color(0xFFFF5252).copy(alpha = 0.4f)
                        isUser -> Color(0xFFC6FF00).copy(alpha = 0.3f)
                        else -> Color.White.copy(alpha = 0.08f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Message Text
                    Text(
                        text = message.text,
                        color = when {
                            message.isError -> Color(0xFFFF8A80)
                            isUser -> Color.White
                            else -> Color(0xFFE2E8F0)
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Meta timestamp + model badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isUser && message.modelUsed != null && !message.isError) {
                            Text(
                                text = "Gemini " + message.modelUsed.replace("gemini-", "").replace("-preview", ""),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC6FF00).copy(alpha = 0.7f)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC6FF00)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(role: CoachRole) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF222222))
                .border(1.dp, Color(0xFFC6FF00).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = role.iconEmoji, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            color = Color(0xFF1C1C1E),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC6FF00).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val transition = rememberInfiniteTransition(label = "pulse")
                val alpha1 by transition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse), label = "a1"
                )
                val alpha2 by transition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse), label = "a2"
                )
                val alpha3 by transition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse), label = "a3"
                )

                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC6FF00).copy(alpha = alpha1)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC6FF00).copy(alpha = alpha2)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC6FF00).copy(alpha = alpha3)))

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "FitAI is thinking...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SuggestedPromptsSection(
    prompts: List<String>,
    onPromptClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Suggested Questions",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFC6FF00),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                Surface(
                    onClick = { onPromptClick(prompt) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1A1A1A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFC6FF00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onTextChanged: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = Color(0xFF141414),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        "Ask your FitAI Coach anything...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF222222)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF222222),
                    unfocusedContainerColor = Color(0xFF222222),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isLoading) Color(0xFFC6FF00) else Color(0xFF333333)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = if (inputText.isNotBlank() && !isLoading) Color.Black else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
