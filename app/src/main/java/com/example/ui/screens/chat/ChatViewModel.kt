package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.gemini.Content
import com.example.data.gemini.GeminiClient
import com.example.data.gemini.Part
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class CoachRole(
    val title: String,
    val iconEmoji: String,
    val subtitle: String,
    val systemInstruction: String,
    val suggestedPrompts: List<String>
) {
    MASTER_COACH(
        title = "FitAI Coach",
        iconEmoji = "🏋️",
        subtitle = "Personal Trainer & Fitness Mentor",
        systemInstruction = "You are FitAI Coach, an elite certified personal fitness trainer and health mentor. You give supportive, motivating, science-backed workout and lifestyle guidance. Structure your answers clearly with bullet points, actionable tips, and positive encouragement.",
        suggestedPrompts = listOf(
            "Design a 4-day muscle building split for beginners",
            "How can I break through a bench press plateau?",
            "What's the best warm-up routine before heavy lifting?",
            "Analyze my daily workout vs caloric burn"
        )
    ),
    NUTRITIONIST(
        title = "Sports Nutritionist",
        iconEmoji = "🥗",
        subtitle = "Macros, Meal Plans & Fueling",
        systemInstruction = "You are a Registered Sports Nutritionist and Dietitian specializing in macronutrient optimization, meal planning, pre/post-workout fueling, and hydration. Provide practical, delicious, and healthy dietary advice tailored to fitness goals.",
        suggestedPrompts = listOf(
            "High-protein vegetarian dinner ideas under 500 kcal",
            "What should I eat 1 hour before a morning workout?",
            "How much protein do I need per kg of bodyweight?",
            "Best electrolyte and hydration strategy for runners"
        )
    ),
    STRENGTH_EXPERT(
        title = "Strength & Form",
        iconEmoji = "💪",
        subtitle = "Biomechanics & Progressive Overload",
        systemInstruction = "You are a Strength & Conditioning Coach and Biomechanics Specialist. You excel in progressive overload principles, compound lift mechanics (Squat, Bench, Deadlift, OHP), form corrections, and injury prevention.",
        suggestedPrompts = listOf(
            "How to fix lower back rounding during deadlifts?",
            "RPE vs Percentage-based training: which is better?",
            "Best accessory exercises to improve squat depth",
            "How to program deload weeks safely"
        )
    ),
    RECOVERY_EXPERT(
        title = "Recovery & Mobility",
        iconEmoji = "🧘",
        subtitle = "Sleep, Flexibility & Wellness",
        systemInstruction = "You are a Recovery, Mobility, and Holistic Wellness Specialist focusing on active recovery, sleep quality optimization, joint mobility, foam rolling, and athletic longevity.",
        suggestedPrompts = listOf(
            "10-minute full body evening mobility routine",
            "How to reduce severe DOMS (delayed onset muscle soreness)?",
            "Optimal sleep habits for muscle recovery and growth",
            "Cold showers vs hot baths for athletic recovery"
        )
    )
}

data class ModelOption(
    val modelId: String,
    val displayName: String,
    val badge: String,
    val description: String
)

val availableModels = listOf(
    ModelOption(
        modelId = "gemini-3.5-flash",
        displayName = "3.5 Flash",
        badge = "Balanced",
        description = "Fast, smart, and comprehensive for daily fitness & nutrition advice"
    ),
    ModelOption(
        modelId = "gemini-3.1-pro-preview",
        displayName = "3.1 Pro",
        badge = "Deep Reasoning",
        description = "Advanced reasoning for complex periodization, biology & biomechanics"
    ),
    ModelOption(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "3.1 Flash-Lite",
        badge = "Ultra Fast",
        description = "Instant responses for quick food lookups, tips & calorie facts"
    )
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
    val modelUsed: String? = null,
    val roleUsed: CoachRole? = null,
    val isError: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedRole = MutableStateFlow(CoachRole.MASTER_COACH)
    val selectedRole: StateFlow<CoachRole> = _selectedRole.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _includeStatsContext = MutableStateFlow(true)
    val includeStatsContext: StateFlow<Boolean> = _includeStatsContext.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        resetWithWelcomeMessage(CoachRole.MASTER_COACH)
    }

    fun selectRole(role: CoachRole) {
        _selectedRole.value = role
        if (_messages.value.size <= 1) {
            resetWithWelcomeMessage(role)
        }
    }

    fun selectModel(modelId: String) {
        _selectedModel.value = modelId
    }

    fun toggleStatsContext() {
        _includeStatsContext.value = !_includeStatsContext.value
    }

    fun clearChat() {
        resetWithWelcomeMessage(_selectedRole.value)
    }

    private fun resetWithWelcomeMessage(role: CoachRole) {
        val welcomeText = when (role) {
            CoachRole.MASTER_COACH -> "Hey there! 👋 I'm your **FitAI Coach**. Whether you need a customized workout split, form check tips, or daily motivation, I'm here to help you crush your fitness goals. What are we working on today?"
            CoachRole.NUTRITIONIST -> "Hello! 🥗 I'm your **Sports Nutritionist**. I can help you calculate macros, craft meal ideas, plan pre/post workout meals, and stay optimally fueled. What nutrition questions do you have?"
            CoachRole.STRENGTH_EXPERT -> "Welcome! 💪 I'm your **Strength & Biomechanics Specialist**. Ask me about progressive overload, compound lift technique, breaking plateaus, or injury prevention."
            CoachRole.RECOVERY_EXPERT -> "Hi there! 🧘 I'm your **Recovery & Wellness Expert**. Let's optimize your sleep, active recovery, stretching routines, and mobility so your body stays resilient."
        }

        _messages.value = listOf(
            ChatMessage(
                text = welcomeText,
                isUser = false,
                roleUsed = role,
                modelUsed = _selectedModel.value
            )
        )
    }

    fun sendMessage(userText: String, fitnessContext: String? = null) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        _errorMessage.value = null

        val userMessage = ChatMessage(
            text = trimmed,
            isUser = true
        )

        val currentList = _messages.value + userMessage
        _messages.value = currentList
        _isLoading.value = true

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

            if (!hasValidKey) {
                // Smooth simulated typing response using our smart built-in Fitness Coach engine
                delay(600L)
                val reply = OfflineFitnessCoach.generateReply(
                    role = _selectedRole.value,
                    query = trimmed,
                    fitnessContext = if (_includeStatsContext.value) fitnessContext else null
                )

                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(
                    text = reply,
                    isUser = false,
                    modelUsed = "Built-in Coach",
                    roleUsed = _selectedRole.value
                )
                return@launch
            }

            val contents = mutableListOf<Content>()
            val enhancedSystemInstruction = buildString {
                append(_selectedRole.value.systemInstruction)
                if (_includeStatsContext.value && !fitnessContext.isNullOrBlank()) {
                    append("\n\n[User's Current FitAI Health Context Today]:\n")
                    append(fitnessContext)
                }
            }

            currentList.forEach { msg ->
                val roleStr = if (msg.isUser) "user" else "model"
                if (!msg.isError) {
                    contents.add(
                        Content(
                            role = roleStr,
                            parts = listOf(Part(text = msg.text))
                        )
                    )
                }
            }

            val result = GeminiClient.generateMultiTurnReply(
                model = _selectedModel.value,
                contents = contents,
                systemInstruction = enhancedSystemInstruction
            )

            _isLoading.value = false

            result.onSuccess { replyText ->
                _messages.value = _messages.value + ChatMessage(
                    text = replyText,
                    isUser = false,
                    modelUsed = _selectedModel.value,
                    roleUsed = _selectedRole.value
                )
            }.onFailure { _ ->
                // Graceful fallback to offline smart engine if network or quota issue occurs
                val fallbackReply = OfflineFitnessCoach.generateReply(
                    role = _selectedRole.value,
                    query = trimmed,
                    fitnessContext = if (_includeStatsContext.value) fitnessContext else null
                )
                _messages.value = _messages.value + ChatMessage(
                    text = fallbackReply,
                    isUser = false,
                    modelUsed = "Built-in Coach",
                    roleUsed = _selectedRole.value
                )
            }
        }
    }
}
