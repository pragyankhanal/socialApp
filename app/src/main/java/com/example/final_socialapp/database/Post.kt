package com.example.final_socialapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(), // Use current time as default
    val userId: String // Add userId if needed
)
