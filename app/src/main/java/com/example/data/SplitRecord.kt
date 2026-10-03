package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "split_history")
data class SplitRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val tipPercent: Double,
    val peopleCount: Int,
    val perPersonAmount: Double,
    val grandTotal: Double,
    val itemsJson: String
) {
    // Utility to get items as list
    val items: List<String>
        get() = try {
            val array = JSONArray(itemsJson)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
}
