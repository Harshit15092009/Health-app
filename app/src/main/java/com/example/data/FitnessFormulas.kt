package com.example.data

import kotlin.math.roundToInt

object FitnessFormulas {

    data class ProportionStatus(
        val name: String,
        val actualValue: Double,
        val idealValue: Double,
        val ratio: Double, // actual / ideal
        val colorCode: String // "GREEN", "YELLOW", "RED"
    )

    data class DietPlan(
        val totalCalories: Int,
        val proteinGrams: Int,
        val carbGrams: Int,
        val fatGrams: Int,
        val hydrationTargetMl: Int,
        val meals: List<MealRecommendation>
    )

    data class MealRecommendation(
        val name: String,
        val timing: String,
        val proteinPct: Int,
        val carbPct: Int,
        val description: String
    )

    data class ExercisePlanItem(
        val name: String,
        val targetMuscle: String,
        val targetCategory: String, // "Legs", "Chest", etc.
        val sets: Int,
        val reps: String,
        val rpe: Int, // Rate of Perceived Exertion (out of 10)
        val restSeconds: Int,
        val videoSearchQuery: String,
        val isHomeFriendly: Boolean
    )

    data class WorkoutDay(
        val dayName: String,
        val title: String,
        val exercises: List<ExercisePlanItem>
    )

    // Calculate BMI
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0.0) return 0.0
        val heightMeters = heightCm / 100.0
        return weightKg / (heightMeters * heightMeters)
    }

    // Get ideal measurements based on standard proportion ratios (Steve Reeves proportions)
    fun getIdealProportions(heightCm: Double): Map<String, Double> {
        // Simple elegant ratios based on height
        return mapOf(
            "Neck" to heightCm * 0.22,
            "Chest" to heightCm * 0.58,
            "Waist" to heightCm * 0.41,
            "Hips" to heightCm * 0.53,
            "Shoulders" to heightCm * 0.65,
            "Biceps" to heightCm * 0.21,
            "Forearms" to heightCm * 0.17,
            "Thighs" to heightCm * 0.32,
            "Calves" to heightCm * 0.21
        )
    }

    // Evaluate current measurements against ideal proportions
    fun evaluateProportions(log: MeasurementLog): Map<String, ProportionStatus> {
        val ideals = getIdealProportions(log.height)
        val results = mutableMapOf<String, ProportionStatus>()

        val actuals = mapOf(
            "Neck" to log.neck,
            "Chest" to log.chest,
            "Waist" to log.waist,
            "Hips" to log.hips,
            "Shoulders" to log.shoulders,
            "Biceps" to log.biceps,
            "Forearms" to log.forearms,
            "Thighs" to log.thighs,
            "Calves" to log.calves
        )

        for ((pName, actual) in actuals) {
            val ideal = ideals[pName] ?: actual
            // Percent variance
            // For general muscle parts, being smaller than ideal means "needs work"
            // For waist, being larger than ideal means "needs work"
            val diffPct = if (pName == "Waist") {
                // If waist is larger than ideal, it's negative for proportions
                if (actual > ideal) (actual - ideal) / ideal else 0.0
            } else {
                // If muscle is smaller than ideal, it needs work
                if (actual < ideal) (ideal - actual) / ideal else 0.0
            }

            val color = when {
                diffPct <= 0.07 -> "GREEN"   // within 7% is awesome / on track
                diffPct <= 0.16 -> "YELLOW"  // 7-16% needs attention
                else -> "RED"                // >16% is priority focus
            }

            results[pName] = ProportionStatus(
                name = pName,
                actualValue = actual,
                idealValue = (ideal * 10.0).roundToInt() / 10.0,
                ratio = (actual / ideal),
                colorCode = color
            )
        }
        return results
    }

    // Calculate TDEE and Diet Plan
    fun generateDietPlan(log: MeasurementLog, bmi: Double): DietPlan {
        // Mifflin-St Jeor BMR
        var bmr = (10.0 * log.weight) + (6.25 * log.height) - (5.0 * log.age)
        bmr += if (log.gender.equals("Male", ignoreCase = true)) 5.0 else -161.0

        // Assume moderate exercise multiplier
        val tdee = bmr * 1.375

        // Determine goal: weight loss, muscle gain, or tone
        val goal: String = when {
            bmi >= 25.0 || log.waist > (log.height * 0.48) -> "CUT"
            bmi < 19.5 -> "BULK"
            else -> "TONE"
        }

        val targetCalories = when (goal) {
            "CUT" -> (tdee - 500.0).coerceAtLeast(1200.0).roundToInt()
            "BULK" -> (tdee + 350.0).roundToInt()
            else -> tdee.roundToInt() // Maintain / Tone
        }

        // Calorie splits
        // Proteins: 4 kcal/g, Carbs: 4 kcal/g, Fats: 9 kcal/g
        val pPct = if (goal == "CUT") 40 else if (goal == "BULK") 30 else 35
        val cPct = if (goal == "CUT") 30 else if (goal == "BULK") 50 else 40
        val fPct = if (goal == "CUT") 30 else if (goal == "BULK") 20 else 25

        val proteinGrams = ((targetCalories * (pPct / 100.0)) / 4.0).roundToInt()
        val carbGrams = ((targetCalories * (cPct / 100.0)) / 4.0).roundToInt()
        val fatGrams = ((targetCalories * (fPct / 100.0)) / 9.0).roundToInt()

        // Hydration: weight * 35 ml
        val hydrationTargetMl = (log.weight * 35.0).roundToInt().coerceAtLeast(1500)

        val meals = when (goal) {
            "CUT" -> listOf(
                MealRecommendation("Breakfast", "08:00 AM", 40, 20, "Oatmeal with whey protein, egg whites, and fresh berries. Full hydration cup."),
                MealRecommendation("Lunch", "01:00 PM", 45, 25, "Grilled lean chicken breast, steamed broccoli, half sweet potato with dark greens."),
                MealRecommendation("Snack", "04:30 PM", 40, 30, "Low-fat Greek yogurt with unroasted almonds and dynamic protein shake."),
                MealRecommendation("Dinner", "08:00 PM", 35, 25, "Pan-seared salmon fillet, asparagus spears, and a mixed seed avocado salad.")
            )
            "BULK" -> listOf(
                MealRecommendation("Breakfast", "07:30 AM", 30, 50, "4 whole scrambled eggs, whole-wheat sourdough toast, banana and rich peanut butter."),
                MealRecommendation("Lunch", "12:30 PM", 30, 45, "Flank steak, massive plate of white basmati rice, roasted stir-fry veggies with olive oil."),
                MealRecommendation("Post-Workout Snack", "04:00 PM", 30, 60, "Mass gainer protein shake blended with milk, oats, honey, and raw organic cocoa."),
                MealRecommendation("Dinner", "08:00 PM", 30, 45, "Oven-baked turkey breasts, red pasta with olive oil extra-virgin, broccoli peaks.")
            )
            else -> listOf(
                MealRecommendation("Breakfast", "08:00 AM", 35, 35, "Scrambled eggs with spinach, dynamic whey oats, and fresh berries."),
                MealRecommendation("Lunch", "01:00 PM", 35, 35, "Tuna or grilled chicken wrap with hummus, sliced sweet peppers, and standard greens."),
                MealRecommendation("Snack", "05:00 PM", 40, 30, "Cottage cheese with pineapple slices and handful of healthy walnuts."),
                MealRecommendation("Dinner", "08:00 PM", 35, 40, "Baked cod or salmon, quinoa bowl with mixed vegetables, and visual virgin olive oil.")
            )
        }

        return DietPlan(
            totalCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbGrams = carbGrams,
            fatGrams = fatGrams,
            hydrationTargetMl = hydrationTargetMl,
            meals = meals
        )
    }

    // Helper to generate custom workout plans with progressive overload and targeting weak zones (Yellow / Red body zones)
    fun generateWorkoutPlan(
        log: MeasurementLog,
        isHomeMode: Boolean,
        difficultyAdjustment: String // "EASY" (-1 rep, longer rest), "NORMAL" (default), "HARD" (+1 set or reps, shorter rest)
    ): List<WorkoutDay> {
        val proportions = evaluateProportions(log)
        val redZones = proportions.filter { it.value.colorCode == "RED" }.keys
        val yellowZones = proportions.filter { it.value.colorCode == "YELLOW" }.keys

        // Prioritize: Red zones > Yellow zones
        val primaryWeaks = if (redZones.isNotEmpty()) redZones else if (yellowZones.isNotEmpty()) yellowZones else listOf("Chest", "Back")

        // Define base exercise lists
        val exercisesMap = if (isHomeMode) {
            mapOf(
                "Chest" to listOf(
                    ExercisePlanItem("Standard Push-ups", "Chest", "Chest", 3, "12-15", 7, 60, "how to do pushups bodyweight at home", true),
                    ExercisePlanItem("Wide Push-ups", "Chest", "Chest", 3, "10-12", 8, 60, "wide grip pushups home form", true),
                    ExercisePlanItem("Decline Push-ups (Feet Elevated)", "Chest", "Chest", 3, "8-10", 9, 75, "decline pushups bodyweight targeting upper chest", true)
                ),
                "Back" to listOf(
                    ExercisePlanItem("Doorframe Rows", "Back", "Back", 3, "15-20", 7, 60, "doorframe row bodyweight exercise back", true),
                    ExercisePlanItem("Superman Extensions", "Back", "Back", 3, "12-15", 8, 45, "superman hold back exercise tutorial", true),
                    ExercisePlanItem("Prone Towel Pulldowns", "Back", "Back", 3, "12-15", 8, 60, "floor towel pulldowns back", true)
                ),
                "Shoulders" to listOf(
                    ExercisePlanItem("Pike Push-ups", "Shoulders", "Shoulders", 3, "8-10", 8, 90, "pike push up shoulder tutorial form", true),
                    ExercisePlanItem("Arm Circles (Weighted)", "Shoulders", "Shoulders", 3, "45s", 6, 45, "isometric shoulder burnout home arm circles", true),
                    ExercisePlanItem("Dolphin Push-ups", "Shoulders", "Shoulders", 3, "10-12", 8, 60, "dolphin pushup shoulders core", true)
                ),
                "Biceps" to listOf(
                    ExercisePlanItem("Towel Bicep Isometrics", "Biceps", "Biceps", 3, "20s hold", 8, 60, "towel bicep curl isometric resistance", true),
                    ExercisePlanItem("Doorway Bicep Pulls", "Biceps", "Biceps", 3, "12-15", 7, 45, "doorway single arm bicep row", true)
                ),
                "Triceps" to listOf(
                    ExercisePlanItem("Bench/Chair Dips", "Triceps", "Triceps", 3, "12-15", 7, 60, "bench dips home triceps form", true),
                    ExercisePlanItem("Diamond Push-ups", "Triceps", "Triceps", 3, "8-10", 9, 75, "diamond pushups form bicep tricep focus", true)
                ),
                "Thighs" to listOf(
                    ExercisePlanItem("Bodyweight Squats", "Thighs", "Legs", 4, "20", 7, 60, "bodyweight air squats thighs glutes", true),
                    ExercisePlanItem("Bulgarian Split Squats", "Thighs", "Legs", 3, "12-15 per leg", 8, 75, "bulgarian split squat single leg bodyweight", true),
                    ExercisePlanItem("Single-leg Glute Bridges", "Thighs", "Legs", 3, "15 per side", 6, 45, "single leg glute bridge tutorial", true)
                ),
                "Calves" to listOf(
                    ExercisePlanItem("Single-leg Calf Raises", "Calves", "Legs", 3, "15-20 per side", 7, 45, "single leg standing calf raise doorway", true),
                    ExercisePlanItem("Calf Jumps (Isometric)", "Calves", "Legs", 3, "30s", 8, 45, "calf jump bounces bodyweight", true)
                ),
                "Neck" to listOf(
                    ExercisePlanItem("Seated Isometric Neck Press", "Neck", "Neck", 3, "15s each side", 6, 30, "isometric neck resistance exercises", true)
                ),
                "Waist" to listOf(
                    ExercisePlanItem("Plank Hold", "Abs", "Abs", 3, "45-60s", 7, 45, "perfect plank alignment guide abs", true),
                    ExercisePlanItem("Bicycle Crunches", "Abs", "Abs", 3, "15 per side", 7, 45, "bicycle crunches core flat belly", true),
                    ExercisePlanItem("Hollow Body Hold", "Abs", "Abs", 3, "25s", 8, 60, "hollow body hold gymnastics abs strength", true)
                ),
                "Hips" to listOf(
                    ExercisePlanItem("Clamshells (Bodyweight)", "Hips", "Legs", 3, "15 per side", 6, 45, "clamshell hips gluteus medius", true),
                    ExercisePlanItem("Fire Hydrants", "Hips", "Legs", 3, "15-20", 7, 45, "fire hydrant legs exercise outer glute", true)
                )
            )
        } else {
            mapOf(
                "Chest" to listOf(
                    ExercisePlanItem("Barbell Bench Press", "Chest", "Chest", 4, "8-10", 8, 90, "how to bench press barbell proper form", false),
                    ExercisePlanItem("Incline Dumbbell Press", "Chest", "Chest", 3, "10-12", 8, 75, "incline dumbbell press chest tutorial", false),
                    ExercisePlanItem("Cable Crossovers", "Chest", "Chest", 3, "12-15", 7, 60, "cable fly or cable crossover standard chest form", false)
                ),
                "Back" to listOf(
                    ExercisePlanItem("Conventional Deadlifts", "Back", "Back", 3, "5", 9, 120, "how to deadlift conventionally barbell school", false),
                    ExercisePlanItem("Wide-grip Pullups/Lat Pulldown", "Back", "Back", 4, "8-10", 8, 90, "lat pulldowns back exercise gym", false),
                    ExercisePlanItem("Bent-over Barbell Rows", "Back", "Back", 3, "8-10", 8, 75, "bent over barbell rows back", false)
                ),
                "Shoulders" to listOf(
                    ExercisePlanItem("Overhead Barbell Press", "Shoulders", "Shoulders", 4, "6-8", 9, 90, "barbell overhead military press form", false),
                    ExercisePlanItem("Dumbbell Lateral Raises", "Shoulders", "Shoulders", 4, "12-15", 7, 60, "standing dumbbell lateral raise shoulders", false),
                    ExercisePlanItem("Face Pulls", "Shoulders", "Shoulders", 3, "15", 7, 60, "how to do cable face pulls athlean x", false)
                ),
                "Biceps" to listOf(
                    ExercisePlanItem("Standing Barbell Curls", "Biceps", "Biceps", 3, "10-12", 8, 60, "barbell curl bicep exercises", false),
                    ExercisePlanItem("Incline Dumbbell Curls", "Biceps", "Biceps", 3, "12", 7, 60, "incline dumbbell curl peak bicep", false)
                ),
                "Triceps" to listOf(
                    ExercisePlanItem("Triceps Rope Pushdowns", "Triceps", "Triceps", 3, "12-15", 7, 60, "cable triceps rope pushdown guide", false),
                    ExercisePlanItem("Skull Crushers", "Triceps", "Triceps", 3, "10", 8, 75, "ez bar skull crushers triceps lying", false)
                ),
                "Thighs" to listOf(
                    ExercisePlanItem("Barbell Back Squats", "Thighs", "Legs", 4, "6-8", 9, 120, "how to barbell squat heavy thigh glute", false),
                    ExercisePlanItem("Romanian Deadlifts", "Thighs", "Legs", 3, "10-12", 8, 90, "romanian deadlift rdl hamstring form", false),
                    ExercisePlanItem("Leg Press", "Thighs", "Legs", 3, "10-12", 8, 75, "gym leg press machine positioning thighs", false)
                ),
                "Calves" to listOf(
                    ExercisePlanItem("Standing Calf Raises", "Calves", "Legs", 4, "15", 8, 60, "standing calf raise machine target calves", false),
                    ExercisePlanItem("Seated Calf Raises", "Calves", "Legs", 3, "20", 7, 45, "seated calf raise soleus", false)
                ),
                "Neck" to listOf(
                    ExercisePlanItem("Lying Plate Neck Extension", "Neck", "Neck", 3, "15-20", 7, 45, "lying neck extension weight plates", false)
                ),
                "Waist" to listOf(
                    ExercisePlanItem("Hanging Knee Raises", "Abs", "Abs", 3, "12-15", 8, 60, "hanging knee raise core lower abs", false),
                    ExercisePlanItem("Kneeling Cable Crunches", "Abs", "Abs", 3, "15", 7, 45, "kneeling cable crunch abs", false)
                ),
                "Hips" to listOf(
                    ExercisePlanItem("Abductor Machine", "Hips", "Legs", 3, "15", 6, 60, "seated hip abduction machine gluteus medius", false)
                )
            )
        }

        // Adjust sets or reps based on difficulty feedback!
        fun adjustItem(item: ExercisePlanItem): ExercisePlanItem {
            return when (difficultyAdjustment) {
                "EASY" -> {
                    // Reduce intensity and increase rest slightly
                    val newReps = item.reps.toIntOrNull()?.let { (it - 2).coerceAtLeast(6).toString() } ?: item.reps
                    item.copy(
                        sets = (item.sets - 1).coerceAtLeast(2),
                        reps = newReps,
                        restSeconds = item.restSeconds + 15,
                        rpe = (item.rpe - 1).coerceAtLeast(5)
                    )
                }
                "HARD" -> {
                    // Increase sets/reps, reduce rest slightly
                    val newReps = item.reps.toIntOrNull()?.let { (it + 2).toString() } ?: item.reps
                    item.copy(
                        sets = item.sets + 1,
                        reps = newReps,
                        restSeconds = (item.restSeconds - 15).coerceAtLeast(30),
                        rpe = (item.rpe + 1).coerceAtMost(10)
                    )
                }
                else -> item // Just right / NORMAL
            }
        }

        // Define a 3-Day structured split routine: Push / Pull / Legs
        // Weak muscle groups are auto-infused into the exercise days to create "Priority focus areas"
        val workoutWeeksPlan = mutableListOf<WorkoutDay>()

        // DAY 1: Push Focus (Chest, Shoulders, Triceps, Abs)
        val day1Exercises = mutableListOf<ExercisePlanItem>()
        // Always include Chest and Shoulders, checking if they are weaks
        val chestEx = exercisesMap["Chest"]?.map { adjustItem(it) } ?: emptyList()
        val shoulderEx = exercisesMap["Shoulders"]?.map { adjustItem(it) } ?: emptyList()
        val triEx = exercisesMap["Triceps"]?.map { adjustItem(it) } ?: emptyList()

        if (primaryWeaks.contains("Chest") || primaryWeaks.contains("Waist")) {
            // Priority overload
            day1Exercises.addAll(chestEx) // 3 chest exercises
            day1Exercises.add(shoulderEx.first())
            day1Exercises.addAll(triEx)
        } else {
            day1Exercises.add(chestEx.first())
            day1Exercises.add(chestEx.getOrElse(1) { chestEx.first() })
            day1Exercises.addAll(shoulderEx.take(2))
            day1Exercises.add(triEx.first())
        }
        // Abs for Day 1
        exercisesMap["Waist"]?.firstOrNull()?.let { day1Exercises.add(adjustItem(it)) }

        workoutWeeksPlan.add(WorkoutDay("Day 1 - Monday", "Metabolic Push Focus", day1Exercises))

        // DAY 2: Pull Focus (Back, Biceps, Neck, Shoulders)
        val day2Exercises = mutableListOf<ExercisePlanItem>()
        val backEx = exercisesMap["Back"]?.map { adjustItem(it) } ?: emptyList()
        val bicepEx = exercisesMap["Biceps"]?.map { adjustItem(it) } ?: emptyList()
        val neckEx = exercisesMap["Neck"]?.map { adjustItem(it) } ?: emptyList()

        if (primaryWeaks.contains("Back") || primaryWeaks.contains("Neck")) {
            day2Exercises.addAll(backEx)
            day2Exercises.addAll(bicepEx)
            day2Exercises.addAll(neckEx)
        } else {
            day2Exercises.add(backEx.first())
            day2Exercises.add(backEx.getOrElse(1) { backEx.first() })
            day2Exercises.add(bicepEx.first())
            if (neckEx.isNotEmpty()) day2Exercises.add(neckEx.first())
            // rear delt focus if shoulder needs work
            if (shoulderEx.size > 2) day2Exercises.add(shoulderEx[2])
        }

        workoutWeeksPlan.add(WorkoutDay("Day 2 - Wednesday", "Hypertrophy Pull Focus", day2Exercises))

        // DAY 3: Legs & Core Focus (Thighs, Calves, Hips)
        val day3Exercises = mutableListOf<ExercisePlanItem>()
        val legEx = exercisesMap["Thighs"]?.map { adjustItem(it) } ?: emptyList()
        val calfEx = exercisesMap["Calves"]?.map { adjustItem(it) } ?: emptyList()
        val hipEx = exercisesMap["Hips"]?.map { adjustItem(it) } ?: emptyList()

        if (primaryWeaks.contains("Thighs") || primaryWeaks.contains("Calves") || primaryWeaks.contains("Hips")) {
            day3Exercises.addAll(legEx)
            day3Exercises.addAll(calfEx)
            day3Exercises.add(hipEx.first())
        } else {
            day3Exercises.add(legEx.first())
            day3Exercises.add(legEx.getOrElse(1) { legEx.first() })
            day3Exercises.add(calfEx.first())
            if (hipEx.isNotEmpty()) day3Exercises.add(hipEx.first())
        }
        // Abs for Day 3 too
        exercisesMap["Waist"]?.getOrNull(1)?.let { day3Exercises.add(adjustItem(it)) }

        workoutWeeksPlan.add(WorkoutDay("Day 3 - Friday", "Universal Leg Dynamics", day3Exercises))

        return workoutWeeksPlan
    }
}
