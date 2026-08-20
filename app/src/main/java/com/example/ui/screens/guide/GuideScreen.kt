package com.example.ui.screens.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideScreen(onNavigateBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            if (onNavigateBack != null) {
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
            }
            Column {
                Text(
                    text = "App Guide",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Learn how to use Fitness Tracker",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        GuideSection(
            title = "🏠 Home & Dynamic Streak Tracker",
            description = "The Home screen gives you a quick overview of your daily progress. It displays your dynamic Health Score, real-time Current Streak counter, and 7-Day Weekly Progress visualizer (Monday–Sunday) based on local calendar dates. It also includes an interactive Water Hydration Tracker with quick +250ml/+500ml/-250ml buttons and a manual Edit dialog to set exact water intake."
        )

        GuideSection(
            title = "🧠 Mind Fitness",
            description = "Train focus, memory, and mindfulness completely offline with 5 interactive 60-second games: Focus Flow, Memory Flash, Color Focus, Number Focus, and Calm Minute. Earn XP, level up from Beginner to Mind Master, maintain your dedicated Mind Streak, and unlock achievement badges."
        )

        GuideSection(
            title = "🏃 Activity",
            description = "Log your workouts and exercises here. You can choose from various activities like Running, Cycling, or Weightlifting. Enter the duration and intensity, and the app will estimate the calories burned. Your total active minutes and calories burned contribute to your daily goals."
        )

        GuideSection(
            title = "🍎 Nutrition",
            description = "Keep track of your meals by logging them in the Nutrition tab. You can add Breakfast, Lunch, Dinner, or Snacks. Input the calories, protein, carbs, and fats to get a detailed breakdown of your daily macronutrient intake."
        )

        GuideSection(
            title = "🧮 Calculator",
            description = "Use the built-in fitness calculators to understand your body better. You can calculate your BMI (Body Mass Index) and estimate your daily caloric needs based on your activity level and goals (e.g., maintain, lose, or gain weight)."
        )

        GuideSection(
            title = "👤 Profile",
            description = "View your personal information and adjust your fitness goals. The Profile screen summarizes your journey and lets you manage your app settings."
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GuideSection(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
