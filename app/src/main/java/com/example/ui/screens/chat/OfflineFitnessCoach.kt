package com.example.ui.screens.chat

import java.util.Locale

/**
 * Intelligent rule-based and knowledge-based fitness advisor engine.
 * Ensures the chatbot provides rich, actionable, and personalized answers
 * even when the Gemini API key is missing or offline.
 */
object OfflineFitnessCoach {

    fun generateReply(
        role: CoachRole,
        query: String,
        fitnessContext: String? = null
    ): String {
        val lower = query.lowercase(Locale.ROOT)

        // 1. Check if user is asking about their personal daily logged stats
        if (lower.contains("my stats") || lower.contains("how did i do") || lower.contains("today") || lower.contains("calories burned") || lower.contains("my progress")) {
            val statsInfo = fitnessContext ?: "No activity data synced yet today."
            return """
                📊 **Your Daily FitAI Summary**:
                $statsInfo
                
                **Coach Insights & Next Steps**:
                • **Calorie Balance**: Keep tracking each meal to ensure your energy intake aligns with your goals (deficit for fat loss, surplus for muscle gain).
                • **Hydration**: Aim for 2.5L - 3.0L daily to maintain electrolyte equilibrium and muscle fullness.
                • **Streak**: Keep up your daily logging momentum to lock in long-term habit formation!
            """.trimIndent()
        }

        // 2. Specialized knowledge based on selected coach persona and query keywords
        when (role) {
            CoachRole.MASTER_COACH -> {
                when {
                    lower.contains("split") || lower.contains("routine") || lower.contains("program") || lower.contains("workout plan") || lower.contains("schedule") -> {
                        return """
                            🏋️ **Recommended 4-Day Upper/Lower Strength Split**:
                            
                            • **Day 1: Upper Body Power**
                              - Barbell Bench Press: 4 sets × 6-8 reps
                              - Barbell Bent-Over Row: 4 sets × 6-8 reps
                              - Overhead Shoulder Press: 3 sets × 8-10 reps
                              - Pull-Ups or Lat Pulldowns: 3 sets × 10-12 reps
                            
                            • **Day 2: Lower Body Power & Core**
                              - Barbell Back Squats: 4 sets × 6-8 reps
                              - Romanian Deadlifts (RDLs): 3 sets × 8-10 reps
                              - Walking Lunges: 3 sets × 12 reps/leg
                              - Hanging Leg Raises / Plank: 3 sets × 15 reps
                            
                            • **Day 3: Active Rest / Mobility & Light Cardio**
                            
                            • **Day 4: Upper Body Hypertrophy**
                              - Incline Dumbbell Press: 3 sets × 10-12 reps
                              - Cable Rows / Face Pulls: 4 sets × 12-15 reps
                              - Lateral Raises & Bicep/Tricep superset: 3 sets × 12-15 reps
                            
                            • **Day 5: Lower Body Hypertrophy**
                              - Leg Press & Leg Curls: 3 sets × 12-15 reps
                              - Bulgarian Split Squats: 3 sets × 10 reps/leg
                              - Standing Calf Raises: 4 sets × 15 reps
                            
                            💡 *Tip: Apply progressive overload by adding 1 rep or 1-2.5 kg each week while keeping strict form.*
                        """.trimIndent()
                    }

                    lower.contains("beginner") || lower.contains("start") || lower.contains("how to start") -> {
                        return """
                            🌟 **Getting Started: Beginner Fitness Blueprint**:
                            
                            1. **Consistency Over Intensity**: Train 3 days per week (Full Body) to allow full systemic recovery.
                            2. **Master The Core Movements**: Squat, Hinge, Push, Pull, and Carry.
                            3. **Track Your Nutrition**: Log your daily meals right here in the **Nutrition** tab to build awareness of protein and calories.
                            4. **Prioritize Sleep**: 7-9 hours of restful sleep is when 95% of muscle recovery and growth occurs.
                        """.trimIndent()
                    }

                    lower.contains("plateau") || lower.contains("stuck") -> {
                        return """
                            ⚡ **Breaking Through a Strength & Weight Plateau**:
                            
                            • **Volume Check**: Add 1 extra working set to your primary compound lifts.
                            • **Tempo Variation**: Slow down the eccentric (lowering) phase to 3 seconds to increase time-under-tension.
                            • **Caloric Adjustment**: For strength plateaus, ensure you aren't in too steep a caloric deficit; for weight-loss plateaus, recalculate your TDEE in the **Calculator** tab.
                            • **Take a Deload Week**: Reduce weights by 40% for 5-7 days to dissipate cumulative CNS fatigue.
                        """.trimIndent()
                    }
                }
            }

            CoachRole.NUTRITIONIST -> {
                when {
                    lower.contains("protein") -> {
                        return """
                            🥩 **Optimal Daily Protein Guidelines**:
                            
                            • **Target**: Aim for **1.6 to 2.2 grams of protein per kilogram of body weight** (0.8 - 1.0g per pound).
                            • **Top Whole-Food Sources**:
                              - *Poultry & Fish*: Chicken breast, turkey, salmon, tuna.
                              - *Vegetarian/Vegan*: Tofu, tempeh, lentils, edamame, Greek yogurt, eggs, cottage cheese.
                              - *Convenience*: Whey isolate or plant-based protein powders.
                            • **Distribution**: Spread protein evenly across 3-4 meals (25-40g per meal) to maximize Muscle Protein Synthesis (MPS).
                        """.trimIndent()
                    }

                    lower.contains("pre-workout") || lower.contains("before workout") || lower.contains("post-workout") || lower.contains("after workout") -> {
                        return """
                            🍎 **Pre & Post-Workout Fueling Protocol**:
                            
                            • **Pre-Workout (60-90 mins before)**:
                              - Easy-to-digest complex carbs + moderate protein.
                              - *Example*: Oatmeal with banana slices and a scoop of whey, or rice cakes with peanut butter and honey.
                            
                            • **Post-Workout (Within 2 hours)**:
                              - Fast-absorbing carbs to replenish glycogen + 30-40g quality protein for muscle repair.
                              - *Example*: Grilled chicken with jasmine rice and roasted vegetables, or a protein shake with fruit and almond milk.
                        """.trimIndent()
                    }

                    lower.contains("meal") || lower.contains("recipe") || lower.contains("diet") || lower.contains("deficit") -> {
                        return """
                            🥗 **Healthy Calorie-Controlled Meal Inspiration**:
                            
                            • **Breakfast**: 3 egg omelet (or egg whites) with spinach, mushrooms, and 1 slice of whole-grain toast (~380 kcal, 28g protein).
                            • **Lunch**: Grilled chicken or baked tofu bowl with quinoa, roasted broccoli, and 1 tbsp olive oil tahini (~520 kcal, 42g protein).
                            • **Dinner**: Pan-seared salmon fillet or lentil curry with steamed sweet potato and asparagus (~550 kcal, 38g protein).
                            • **Smart Snacks**: Greek yogurt with berries, apple with peanut butter, or mixed nuts.
                        """.trimIndent()
                    }
                }
            }

            CoachRole.STRENGTH_EXPERT -> {
                when {
                    lower.contains("squat") || lower.contains("bench") || lower.contains("deadlift") || lower.contains("form") -> {
                        return """
                            💪 **The Big 3 Form & Biomechanics Checklist**:
                            
                            • **Squat Mechanics**:
                              - Brace core with diaphragmatic 360° breath into belt.
                              - Keep knees tracking over 2nd & 3rd toes.
                              - Maintain tripod foot pressure (big toe, pinky toe, heel).
                            
                            • **Bench Press Mechanics**:
                              - Retract and depress scapula (pack your lats).
                              - Touch barbell around lower sternum with wrists stacked above elbows.
                              - Drive through your legs (leg drive) to stabilize torso.
                            
                            • **Deadlift Mechanics**:
                              - Wedge hips into bar; keep shins 1 inch away before initiating pull.
                              - Pull slack out of the bar before pushing the floor away.
                              - Lock out with glutes, avoiding hyperextending the lower back.
                        """.trimIndent()
                    }

                    lower.contains("rpe") || lower.contains("reps in reserve") || lower.contains("rir") -> {
                        return """
                            📈 **Mastering RPE (Rate of Perceived Exertion) & RIR**:
                            
                            • **RPE 10 (0 RIR)**: Absolute maximum effort; zero reps left in the tank.
                            • **RPE 9 (1 RIR)**: Could perform 1 more rep with good form.
                            • **RPE 8 (2 RIR)**: Ideal sweet spot for 80% of hypertrophy & strength volume!
                            • **RPE 7 (3 RIR)**: Great for warm-up sets, speed work, and active recovery.
                        """.trimIndent()
                    }
                }
            }

            CoachRole.RECOVERY_EXPERT -> {
                when {
                    lower.contains("sleep") || lower.contains("rest") || lower.contains("doms") || lower.contains("sore") -> {
                        return """
                            🧘 **Athletic Recovery & Soreness (DOMS) Protocol**:
                            
                            1. **Sleep Optimization**: Keep bedroom temperature cool (65-68°F / 18-20°C) and reduce blue light exposure 60 mins before sleep.
                            2. **Active Recovery**: A 20-30 minute brisk walk or zone 2 cycling increases blood flow and flushes metabolic waste without stressing the CNS.
                            3. **Post-Workout Mobility**: Focus on 5-10 minutes of gentle foam rolling and dynamic hip / thoracic spine mobility stretches.
                            4. **Hydration & Electrolytes**: Drink water with a pinch of salt, potassium, and magnesium post-sweat.
                        """.trimIndent()
                    }
                }
            }
        }

        // 3. Fallback comprehensive answer matching general fitness questions
        return """
            🏋️ **FitAI Coach Guidance**:
            
            Thank you for asking about **"$query"**!
            
            Here are actionable recommendations to optimize your training & wellness:
            
            1. **Consistent Logging**: Track workouts and calories daily in the **Activity** and **Nutrition** screens to gauge your caloric surplus or deficit.
            2. **Targeted Nutrition**: Focus on whole foods, lean proteins (1.6-2.0g/kg), complex carbohydrates for energy, and healthy fats.
            3. **Progressive Overload**: Aim to gradually increase resistance, reps, or time-under-tension each week while keeping strict form.
            4. **Recovery**: Ensure adequate rest days and hydration to prevent overtraining.
            
            *(Note: To unlock live multi-turn generative AI queries with customized deep reasoning, you can also add your Gemini API key in the AI Studio Secrets panel).*
        """.trimIndent()
    }
}
