package com.example.soulscript.utils



import com.example.soulscript.data.Note
import java.util.Calendar

data class MonthlyInsight(
    val monthName: String,
    val dominantMood: String?,
    val wordCloudData: Map<String, Int>,
    val suggestion: String,
    val totalEntries: Int
)

object InsightGenerator {

    private val stopWords = setOf("a", "an", "the", "is", "in", "it", "of", "and", "to", "was", "for", "on", "with", "i", "my", "me", "you", "your", "that", "this", "at", "but", "not", "have", "had")

    fun generateInsights(notes: List<Note>, monthName: String): MonthlyInsight {
        if (notes.isEmpty()) {
            return MonthlyInsight(monthName, null, emptyMap(), "Write more this month to unlock your first insight!", 0)
        }

        val dominantMood = notes.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
        val wordCloudData = generateWordCloudData(notes)
        val suggestion = generateSuggestion(dominantMood)

        return MonthlyInsight(
            monthName = monthName,
            dominantMood = dominantMood,
            wordCloudData = wordCloudData,
            suggestion = suggestion,
            totalEntries = notes.size
        )
    }

    private fun generateWordCloudData(notes: List<Note>): Map<String, Int> {
        return notes
            .flatMap { (it.title + " " + it.content).split(Regex("\\s+")) }
            .map { it.trim().lowercase().replace(Regex("[^a-zA-Z]"), "") }
            .filter { it.isNotBlank() && it !in stopWords }
            .groupingBy { it }
            .eachCount()
    }

    private fun generateSuggestion(dominantMood: String?): String {
        if (dominantMood == null) {
            return "Keep writing to discover more about yourself! The more you write, the more personalized your insights will become."
        }

        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        val happySuggestions = listOf(
            "Happiness was your dominant mood this month. What key moments or activities contributed to this? Reflecting on them can help you cultivate even more joy.",
            "That's wonderful! It's great to see that happiness was a recurring theme. Can you identify a common thread in your happy entries?",
            "Your journal was filled with happy moments this month. Keep embracing the people and activities that bring you joy."
        )
        val stressedSuggestions = listOf(
            "It seems like this was a challenging month. Acknowledging stress is a key part of managing it. What's one small step you can take to find calm this week?",
            "Stress was a frequent mood this month. Are there patterns in your entries on those days? Identifying triggers is the first step to managing them.",
            "You navigated a lot of stress this month. Remember to be kind to yourself and schedule some time to relax and unwind."
        )
        val calmSuggestions = listOf(
            "A sense of calm was your most frequent feeling. It's great that you're finding moments of peace. What practices helped you achieve this state?",
            "Your entries show a trend towards calmness this month. What aspects of your routine are contributing to this tranquility? Let's nurture them.",
            "This month was defined by a calm mood. Reflect on what kept you grounded. Can you carry those lessons into the next month?"
        )
        val sadSuggestions = listOf(
            "It's okay to have down months. Reflecting on these feelings is a sign of strength. What's one small thing that could bring a little light to your day tomorrow?",
            "Sadness was a recurring theme this month. Your journal is a safe space to explore these feelings. Remember to be patient and kind with yourself.",
            "You were brave to document your moments of sadness. Are there any patterns or thoughts you can see that might be helpful to address?"
        )
        val defaultSuggestions = listOf(
            "This month, your entries often had a '$dominantMood' mood. Reflecting on these moments can be a powerful tool for growth.",
            "Your most frequent mood was '$dominantMood'. What does that tell you about your month? What can you learn from it?",
            "'$dominantMood' was a key feeling for you this month. Take a moment to look back at those entries. What story do they tell?"
        )

        return when (dominantMood) {
            "Happy" -> happySuggestions[dayOfMonth % happySuggestions.size]
            "Stressed" -> stressedSuggestions[dayOfMonth % stressedSuggestions.size]
            "Calm" -> calmSuggestions[dayOfMonth % calmSuggestions.size]
            "Sad" -> sadSuggestions[dayOfMonth % sadSuggestions.size]
            else -> defaultSuggestions[dayOfMonth % defaultSuggestions.size]
        }
    }
}