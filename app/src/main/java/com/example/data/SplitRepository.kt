package com.example.data

import kotlinx.coroutines.flow.Flow

class SplitRepository(private val splitDao: SplitDao) {
    val allSplits: Flow<List<SplitRecord>> = splitDao.getAllSplits()

    suspend fun insert(split: SplitRecord) {
        splitDao.insertSplit(split)
    }

    suspend fun deleteById(id: Int) {
        splitDao.deleteSplitById(id)
    }

    suspend fun clearAll() {
        splitDao.clearAllSplits()
    }
}
