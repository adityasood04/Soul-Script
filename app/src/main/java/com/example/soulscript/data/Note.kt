package com.example.soulscript.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val date: Long,
    val mood: String,
    val isImportant: Boolean = false,
    val imageUri: String? = null,
    val sketchPath: String? = null,
    val audioPath: String? = null
)
