package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FitnessViewModel
import com.example.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(viewModel: FitnessViewModel) {
    val caloriesBurned by viewModel.caloriesBurnedToday.collectAsState()
    val caloriesConsumed by viewModel.caloriesConsumedToday.collectAsState()
    val totalDuration by viewModel.totalDurationToday.collectAsState()
    val activities by viewModel.allActivities.collectAsState()
    val meals by viewModel.allMeals.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val activeDaysThisWeek by viewModel.activeDaysThisWeek.collectAsState()
    val isLoggedToday by viewModel.isLoggedToday.collectAsState()
    val todayLocalDate by viewModel.currentDateFlow.collectAsState()
    val selectedLocalDate by viewModel.selectedDate.collectAsState()
    val fitAiInsight by viewModel.fitAiInsight.collectAsState()
    val todayVal = todayLocalDate.dayOfWeek.value
    val currentMonday = todayLocalDate.minusDays((todayLocalDate.dayOfWeek.value - 1).toLong())
    val waterLitres by viewModel.waterLitresToday.collectAsState()
    
    val currentBurned = caloriesBurned ?: 0
    val currentConsumed = caloriesConsumed ?: 0
    val currentDuration = totalDuration ?: 0
    
    var showWaterDialog by remember { mutableStateOf(false) }

    // Estimate steps based on active duration or logged running/walking
    val estimatedSteps = activities.sumOf { activity ->
        val lowerType = activity.type.lowercase()
        if (lowerType.contains("run") || lowerType.contains("walk") || lowerType.contains("jog")) {
            activity.durationMinutes * 120
        } else {
            activity.durationMinutes * 60
        }
    }
    
    val hasData = activities.isNotEmpty() || meals.isNotEmpty()
    
    // Calculate dynamic health score
    val calorieProgress = (currentConsumed / 2200f).coerceIn(0f, 1f)
    val burnProgress = (currentBurned / 500f).coerceIn(0f, 1f)
    val healthScore = if (hasData) {
        ((calorieProgress * 50) + (burnProgress * 50)).toInt().coerceIn(1, 100)
    } else {
        0
    }

    if (showWaterDialog) {
        WaterEditDialog(
            currentWater = waterLitres,
            onDismiss = { showWaterDialog = false },
            onSave = { newAmount ->
                viewModel.setWater(newAmount)
                showWaterDialog = false
            },
            onReset = {
                viewModel.resetWater()
                showWaterDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("WELCOME BACK", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text("Fitness Tracker", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                        shape = CircleShape
                    )
                    .padding(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color(0xFF1A1A1A), CircleShape)
                )
            }
        }
        
        // Hero: Health Score Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Daily Health Score", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("$healthScore", color = MaterialTheme.colorScheme.onBackground, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.padding(top = 8.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(
                            if (hasData) "Active Progress" else "No log yet today",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(progress = { 1f }, color = Color(0xFF2D2D2D), strokeWidth = 8.dp, modifier = Modifier.fillMaxSize())
                    CircularProgressIndicator(
                        progress = { healthScore / 100f },
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Stats Grid
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("CALORIES", "$currentConsumed", "2200", calorieProgress, modifier = Modifier.weight(1f))
            StatCard("STEPS", String.format("%,d", estimatedSteps), "10k", (estimatedSteps / 10000f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
        }
        
        // FitAI Insight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF9C27B0).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧠", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "FitAI Coach Insight", 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF9C27B0), 
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        fitAiInsight, 
                        fontSize = 13.sp, 
                        lineHeight = 18.sp, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Activity & Streak Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Streak Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Current Streak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            if (isLoggedToday) "Streak active for today!" else "Log an activity or meal to maintain!",
                            fontSize = 12.sp,
                            color = if (isLoggedToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$currentStreak ${if (currentStreak == 1) "Day" else "Days"}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // 7-Day Weekly Streak Row (Mon-Sun)
                val daysOfWeek = listOf(
                    "M" to 0L,
                    "T" to 1L,
                    "W" to 2L,
                    "T" to 3L,
                    "F" to 4L,
                    "S" to 5L,
                    "S" to 6L
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { (label, offset) ->
                        val dateForColumn = currentMonday.plusDays(offset)
                        val isLogged = activeDaysThisWeek.contains(dateForColumn.dayOfWeek.value)
                        val isToday = dateForColumn == todayLocalDate
                        val isSelected = dateForColumn == selectedLocalDate

                        Column(
                            modifier = Modifier.clickable { viewModel.selectDate(dateForColumn) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = when {
                                            isLogged -> MaterialTheme.colorScheme.primary
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        },
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isLogged -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isLogged) "✓" else if (isToday && !isSelected) "•" else "",
                                    color = if (isLogged) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatBox("⚡", "$currentBurned kcal", Modifier.weight(1f))
                    MiniStatBox("⏱️", "$currentDuration min", Modifier.weight(1f))
                }
            }
        }

        // Dedicated Interactive Water Tracker Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Water Tracker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "${String.format("%.2f", waterLitres)} / 3.00 L (${(waterLitres * 1000).toInt()} mL)",
                                fontSize = 13.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { showWaterDialog = true },
                        modifier = Modifier
                            .background(Color(0xFF2196F3).copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Water Intake",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (waterLitres / 3.0f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF2196F3),
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Quick Adjustment Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addWater(-0.25f) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("250ml", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.addWater(0.25f) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add 250ml", modifier = Modifier.size(16.dp), tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+250ml", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.addWater(0.50f) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add 500ml", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+500ml", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (!hasData) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No Activities or Meals Logged Yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Use the Activity or Nutrition tabs below to log your workouts and meals. Your daily stats will update automatically!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WaterEditDialog(
    currentWater: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit,
    onReset: () -> Unit
) {
    var inputText by remember { mutableStateOf(String.format("%.2f", currentWater)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💧", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Water Intake", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text("Enter exact water intake in Liters:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Water in Liters (L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Quick Preset Actions:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = { inputText = "1.00" },
                        label = { Text("1.0 L") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { inputText = "2.00" },
                        label = { Text("2.0 L") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { inputText = "3.00" },
                        label = { Text("3.0 L") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = inputText.toFloatOrNull() ?: currentWater
                    onSave(parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReset) {
                    Text("Reset (0L)", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun StatCard(title: String, value: String, total: String, progress: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(" / $total", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(6.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color(0xFF2D2D2D), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
fun MiniStatBox(icon: String, text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.height(80.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, fontSize = 20.sp)
                Text(text, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

