package com.example.soulscript.data

data class JournalTemplate(
    val title: String,
    val content: String
)

object Templates {
    val templateList = listOf(
        JournalTemplate(
            title = "Daily Gratitude",
            content = "Today, I am grateful for:\n1. \n2. \n3. \n\nOne small thing that made me smile was:\n\n"
        ),
        JournalTemplate(
            title = "End-of-Day Reflection",
            content = "What was the biggest challenge I faced today?\n\n\nWhat did I learn from it?\n\n\nHow can I make tomorrow even better?\n\n"
        ),
        JournalTemplate(
            title = "Goal Setting",
            content = "My main goal for this week is:\n\n\nThe first step I will take to achieve it is:\n\n\nI am excited about this goal because:\n\n"
        ),
        JournalTemplate(
            title = "A Moment of Self-Care",
            content = "One thing I did for myself today was:\n\n\nHow did it make me feel?\n\n\nWhat is another act of self-care I can plan for this week?\n\n"
        )
    )
}