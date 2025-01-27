package com.example.final_socialapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Post::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}
