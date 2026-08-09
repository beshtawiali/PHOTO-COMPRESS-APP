package com.tayf.photocompressor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class ProcessedImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val uriString: String,
    val operationType: String,
    val originalSize: Long,
    val resultSize: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val resultWidth: Int,
    val resultHeight: Int,
    val format: String,
    val savedPath: String,
    val timestamp: Long = System.currentTimeMillis()
)
