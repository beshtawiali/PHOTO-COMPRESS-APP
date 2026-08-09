package com.tayf.photocompressor.data.model

import android.net.Uri

data class ImageMetadata(
    val uri: Uri,
    val fileName: String,
    val width: Int,
    val height: Int,
    val fileSize: Long,
    val mimeType: String
)

enum class CompressLevel {
    SMALL,
    BALANCED,
    HIGH_QUALITY,
    CUSTOM
}

enum class OutputFormat {
    JPEG,
    PNG,
    WEBP
}

data class ImageCompressOptions(
    val level: CompressLevel = CompressLevel.BALANCED,
    val customQuality: Int = 80, // 1 - 100
    val targetFileSizeKb: Long? = null, // e.g. 1024 KB for 1 MB
    val format: OutputFormat = OutputFormat.JPEG
)

enum class ResizePreset {
    P25,
    P50,
    P75,
    P1080P,
    P1920P,
    CUSTOM
}

data class ImageResizeOptions(
    val preset: ResizePreset = ResizePreset.P50,
    val customWidth: Int = 1080,
    val customHeight: Int = 1080,
    val lockAspectRatio: Boolean = true
)

enum class BatchItemStatus {
    READY,
    PROCESSING,
    SUCCESS,
    FAILED
}

data class BatchQueueItem(
    val id: String,
    val uri: Uri,
    val fileName: String,
    val originalSize: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val status: BatchItemStatus = BatchItemStatus.READY,
    val resultUri: Uri? = null,
    val resultSize: Long = 0,
    val resultWidth: Int = 0,
    val resultHeight: Int = 0,
    val errorMessage: String? = null
)
