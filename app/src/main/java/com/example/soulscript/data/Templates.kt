package com.example.soulscript.data
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.ui.graphics.vector.ImageVector

data class JournalTemplate(
    val title: String,
    val description: String,
    val content: String,
    val icon: ImageVector
)

data class TemplateCategory(
    val title: String,
    val templates: List<JournalTemplate>
)

object Templates {
    val templateCategories = listOf(
        TemplateCategory(
            title = "Daily Reflection",
            templates = listOf(
                JournalTemplate(
                    title = "Daily Gratitude",
                    description = "Focus on the positive aspects of your day.",
                    content = "Today, I am grateful for:\n1. \n2. \n3. \n\nOne small thing that made me smile was:\n\n",
                    icon = Icons.Default.Favorite
                ),
                JournalTemplate(
                    title = "End-of-Day Reflection",
                    description = "Unwind and process the day's events.",
                    content = "What was the biggest challenge I faced today?\n\n\nWhat did I learn from it?\n\n\nHow can I make tomorrow even better?\n\n",
                    icon = Icons.Default.Nightlight
                )
            )
        ),
        TemplateCategory(
            title = "Productivity & Growth",
            templates = listOf(
                JournalTemplate(
                    title = "Goal Setting",
                    description = "Define your ambitions and the steps to achieve them.",
                    content = "My main goal for this week is:\n\n\nThe first step I will take to achieve it is:\n\n\nI am excited about this goal because:\n\n",
                    icon = Icons.Default.Lightbulb
                ),
                JournalTemplate(
                    title = "A Moment of Self-Care",
                    description = "Plan and reflect on your self-care activities.",
                    content = "One thing I did for myself today was:\n\n\nHow did it make me feel?\n\n\nWhat is another act of self-care I can plan for this week?\n\n",
                    icon = Icons.Default.AutoAwesome
                )
            )
        )
    )
}