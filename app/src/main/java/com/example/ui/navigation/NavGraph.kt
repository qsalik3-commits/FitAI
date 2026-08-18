package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.FitnessViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.activity.ActivityScreen
import com.example.ui.screens.nutrition.NutritionScreen
import com.example.ui.screens.calculator.CalculatorScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.guide.GuideScreen
import com.example.ui.screens.chat.ChatScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Activity : Screen("activity", "Activity", Icons.AutoMirrored.Filled.DirectionsRun)
    object AICoach : Screen("chat", "AI Coach", Icons.Default.AutoAwesome)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Restaurant)
    object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Guide : Screen("guide", "Guide", Icons.AutoMirrored.Filled.Help)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Activity,
    Screen.AICoach,
    Screen.Nutrition,
    Screen.Calculator
)

@Composable
fun MainScreen(fitnessViewModel: FitnessViewModel = viewModel()) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF121212),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFC6FF00),
                            selectedTextColor = Color(0xFFC6FF00),
                            indicatorColor = Color(0xFF222818)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(fitnessViewModel) }
            composable(Screen.Activity.route) { ActivityScreen(fitnessViewModel) }
            composable(Screen.AICoach.route) { ChatScreen(fitnessViewModel) }
            composable(Screen.Nutrition.route) { NutritionScreen(fitnessViewModel) }
            composable(Screen.Calculator.route) { CalculatorScreen(fitnessViewModel) }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Guide.route) { GuideScreen() }
        }
    }
}
