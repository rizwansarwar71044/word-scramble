package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GameRepository(private val dao: UserStatsDao) {

    // Return stats reactively. If null, map to a default UserStats object.
    val userStats: Flow<UserStats> = dao.getUserStatsFlow().map { stats ->
        stats ?: UserStats(id = 0, score = 0, currentLevelIndex = 0, streak = 0, highScore = 0, coins = 100)
    }.flowOn(Dispatchers.IO)

    suspend fun getStatsDirect(): UserStats = withContext(Dispatchers.IO) {
        var stats = dao.getUserStats()
        if (stats == null) {
            stats = UserStats(id = 0, score = 0, currentLevelIndex = 0, streak = 0, highScore = 0, coins = 100)
            dao.insertOrUpdate(stats)
        }
        stats
    }

    suspend fun updateStats(stats: UserStats) = withContext(Dispatchers.IO) {
        dao.insertOrUpdate(stats)
    }

    suspend fun addScoreAndLevel(points: Int, completedWordId: Int) = withContext(Dispatchers.IO) {
        val currentStats = getStatsDirect()
        val newScore = currentStats.score + points
        val newHighScore = if (newScore > currentStats.highScore) newScore else currentStats.highScore
        val newStreak = currentStats.streak + 1
        val newCoins = currentStats.coins + 20 // Reward 20 coins for solving!
        val nextLevelArrIndex = (currentStats.currentLevelIndex + 1) % WordEntry.ALL_WORDS.size
        
        val updatedRaw = currentStats.completeWord(completedWordId)
        
        val updated = currentStats.copy(
            score = newScore,
            highScore = newHighScore,
            streak = newStreak,
            coins = newCoins,
            currentLevelIndex = nextLevelArrIndex,
            completedWordsRaw = updatedRaw
        )
        dao.insertOrUpdate(updated)
    }

    suspend fun skipLevel() = withContext(Dispatchers.IO) {
        val currentStats = getStatsDirect()
        val nextLevelArrIndex = (currentStats.currentLevelIndex + 1) % WordEntry.ALL_WORDS.size
        val updated = currentStats.copy(
            currentLevelIndex = nextLevelArrIndex,
            streak = 0 // Reset streak when skipping
        )
        dao.insertOrUpdate(updated)
    }

    suspend fun resetStreak() = withContext(Dispatchers.IO) {
        val currentStats = getStatsDirect()
        val updated = currentStats.copy(streak = 0)
        dao.insertOrUpdate(updated)
    }

    suspend fun deductCoins(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val currentStats = getStatsDirect()
        if (currentStats.coins >= amount) {
            val updated = currentStats.copy(coins = currentStats.coins - amount)
            dao.insertOrUpdate(updated)
            true
        } else {
            false
        }
    }
    
    suspend fun awardAdCoins(amount: Int) = withContext(Dispatchers.IO) {
        val currentStats = getStatsDirect()
        val updated = currentStats.copy(coins = currentStats.coins + amount)
        dao.insertOrUpdate(updated)
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        dao.insertOrUpdate(UserStats(id = 0, coins = 100))
    }
}
