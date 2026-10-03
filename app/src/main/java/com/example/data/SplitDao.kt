package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Query("SELECT * FROM split_history ORDER BY timestamp DESC")
    fun getAllSplits(): Flow<List<SplitRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: SplitRecord)

    @Query("DELETE FROM split_history WHERE id = :id")
    suspend fun deleteSplitById(id: Int)

    @Query("DELETE FROM split_history")
    suspend fun clearAllSplits()
}
