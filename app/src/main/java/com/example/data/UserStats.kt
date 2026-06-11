package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0,
    val score: Int = 0,
    val currentLevelIndex: Int = 0,
    val streak: Int = 0,
    val highScore: Int = 0,
    val coins: Int = 100, // Starts with 100 coins for hints!
    val completedWordsRaw: String = "" // Track completed word IDs like "1,2,5"
) {
    fun isWordCompleted(wordId: Int): Boolean {
        if (completedWordsRaw.isEmpty()) return false
        return completedWordsRaw.split(",").contains(wordId.toString())
    }

    fun completeWord(wordId: Int): String {
        val completedList = if (completedWordsRaw.isEmpty()) {
            mutableListOf()
        } else {
            completedWordsRaw.split(",").toMutableList()
        }
        if (!completedList.contains(wordId.toString())) {
            completedList.add(wordId.toString())
        }
        return completedList.joinToString(",")
    }
}
