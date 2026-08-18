package com.example.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FitnessViewModel
import java.util.Locale

data class FoodInfo(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)

// Mock database for calories calculation
val foodDatabase = mapOf(
    "apple" to FoodInfo(52, 0, 14, 0),
    "banana" to FoodInfo(89, 1, 23, 0),
    "chicken breast" to FoodInfo(165, 31, 0, 4),
    "rice" to FoodInfo(130, 2, 28, 0),
    "egg" to FoodInfo(155, 13, 1, 11),
    "bread" to FoodInfo(265, 9, 49, 3),
    "pizza" to FoodInfo(266, 11, 33, 10),
    "burger" to FoodInfo(295, 17, 24, 14),
    "salad" to FoodInfo(152, 5, 10, 11),
    "salmon" to FoodInfo(208, 20, 0, 13),
    "milk" to FoodInfo(42, 3, 5, 1),
    "cheese" to FoodInfo(402, 25, 1, 33),
    "yogurt" to FoodInfo(59, 10, 3, 0),
    "oatmeal" to FoodInfo(68, 2, 12, 1),
    "pasta" to FoodInfo(131, 5, 25, 1),
    "potato" to FoodInfo(77, 2, 17, 0),
    "beef" to FoodInfo(250, 26, 0, 15),
    "pork" to FoodInfo(242, 27, 0, 14),
    "almonds" to FoodInfo(579, 21, 22, 50),
    "peanut butter" to FoodInfo(588, 25, 20, 50),
    "avocado" to FoodInfo(160, 2, 9, 15),
    "broccoli" to FoodInfo(34, 3, 7, 0),
    "spinach" to FoodInfo(23, 3, 4, 0),
    "carrot" to FoodInfo(41, 1, 10, 0),
    "tomato" to FoodInfo(18, 1, 4, 0),
    "orange" to FoodInfo(47, 1, 12, 0),
    "strawberry" to FoodInfo(32, 1, 8, 0),
    "blueberry" to FoodInfo(57, 1, 14, 0),
    "grapes" to FoodInfo(69, 1, 18, 0),
    "watermelon" to FoodInfo(30, 1, 8, 0)
)

val activityDatabase = mapOf(
    "running" to 600,
    "walking" to 250,
    "cycling" to 400,
    "swimming" to 500,
    "yoga" to 200,
    "weightlifting" to 300,
    "jumping rope" to 700,
    "dancing" to 350,
    "hiking" to 450,
    "boxing" to 600,
    "pilates" to 250,
    "aerobics" to 400,
    "rowing" to 500,
    "stair climbing" to 600,
    "tennis" to 500,
    "basketball" to 600,
    "soccer" to 600,
    "martial arts" to 700,
    "rock climbing" to 600,
    "elliptical" to 450
)

data class SearchResult(
    val name: String,
    val value: Int,
    val isActivity: Boolean,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

@Composable
fun CalculatorScreen(viewModel: FitnessViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var selectedResult by remember { mutableStateOf<SearchResult?>(null) }
    var inputValue by remember { mutableStateOf("") }

    fun performSearch(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) {
            searchResults = emptyList()
            return
        }

        val results = mutableListOf<SearchResult>()
        
        foodDatabase.forEach { (name, info) ->
            if (name.contains(q)) {
                results.add(SearchResult(
                    name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    info.calories,
                    false,
                    info.protein,
                    info.carbs,
                    info.fat
                ))
            }
        }
        
        activityDatabase.forEach { (name, cals) ->
            if (name.contains(q)) {
                results.add(SearchResult(name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, cals, true))
            }
        }
        
        searchResults = results
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Calories Calculator",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Search for food to gain or activities to burn calories.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.95f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                performSearch(it)
            },
            placeholder = { Text("e.g., Apple or Running", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) })
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (searchResults.isEmpty()) {
            if (searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type above to calculate calories.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchResults) { result ->
                    ResultCard(
                        result = result,
                        onClick = { selectedResult = result }
                    )
                }
            }
        }
    }

    if (selectedResult != null) {
        val result = selectedResult!!
        AlertDialog(
            onDismissRequest = { 
                selectedResult = null
                inputValue = ""
            },
            title = { 
                Text(
                    text = if (result.isActivity) "Calculate Burn" else "Calculate Intake",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (result.isActivity) "Base: ${result.value} kcal / hr" else "Base: ${result.value} kcal / 100g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text(if (result.isActivity) "Duration (mins)" else "Amount (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val inputNum = inputValue.toIntOrNull() ?: 0
                    val calculatedCals = if (result.isActivity) {
                        (result.value * inputNum) / 60
                    } else {
                        (result.value * inputNum) / 100
                    }
                    val calcProtein = (result.protein * inputNum) / 100
                    val calcCarbs = (result.carbs * inputNum) / 100
                    val calcFat = (result.fat * inputNum) / 100
                    
                    if (!result.isActivity) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Protein", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                                Text("${calcProtein}g", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Carbs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                                Text("${calcCarbs}g", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                                Text("${calcFat}g", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Calories:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text("$calculatedCals kcal", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputNum = inputValue.toIntOrNull() ?: 0
                        if (inputNum > 0) {
                            val calculatedCals = if (result.isActivity) {
                                (result.value * inputNum) / 60
                            } else {
                                (result.value * inputNum) / 100
                            }
                            val calcProtein = (result.protein * inputNum) / 100
                            val calcCarbs = (result.carbs * inputNum) / 100
                            val calcFat = (result.fat * inputNum) / 100
                            
                            if (result.isActivity) {
                                viewModel.addActivity(result.name, inputNum, calculatedCals)
                            } else {
                                viewModel.addMeal(result.name, calculatedCals, calcProtein, calcCarbs, calcFat)
                            }
                        }
                        selectedResult = null
                        inputValue = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log to Journal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    selectedResult = null
                    inputValue = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ResultCard(result: SearchResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (result.isActivity) "Burn ${result.value} kcal / hr" else "Gain ${result.value} kcal / 100g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (!result.isActivity) {
                    Text(
                        text = "P: ${result.protein}g • C: ${result.carbs}g • F: ${result.fat}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Calculate", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

