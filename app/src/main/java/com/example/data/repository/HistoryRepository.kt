package com.example.data.repository

import com.example.data.local.HistoryDao
import com.example.data.model.ProcessedImageEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<ProcessedImageEntity>> = historyDao.getAllHistory()
    val recentHistory: Flow<List<ProcessedImageEntity>> = historyDao.getRecentHistory(5)

    suspend fun addHistoryEntry(item: ProcessedImageEntity) {
        historyDao.insertItem(item)
    }

    suspend fun deleteHistoryEntry(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }
}
