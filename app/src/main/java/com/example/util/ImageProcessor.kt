package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import com.example.data.model.CompressLevel
import com.example.data.model.ImageCompressOptions
import com.example.data.model.ImageMetadata
import com.example.data.model.ImageResizeOptions
import com.example.data.model.OutputFormat
import com.example.data.model.ResizePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class ProcessResult(
    val outputUri: Uri,
    val savedPath: String,
    val outputFileName: String,
    val originalSize: Long,
    val resultSize: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val resultWidth: Int,
    val resultHeight: Int,
    val format: OutputFormat
)

object ImageProcessor {

    suspend fun getImageMetadata(context: Context, uri: Uri): ImageMetadata = withContext(Dispatchers.IO) {
        var fileName = "image_${System.currentTimeMillis()}.jpg"
        var fileSize = 0L
        var mimeType = "image/jpeg"

        // Query ContentResolver for name and size
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNull_or_blank()) fileName = name
                }
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }

        val type = context.contentResolver.getType(uri)
        if (type != null) mimeType = type

        // Read bounds without loading entire bitmap into memory
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var width = options.outWidth
        var height = options.outHeight

        // Account for EXIF rotation
        val rotation = getExifOrientation(context, uri)
        if (rotation == 90 || rotation == 270) {
            val temp = width
            width = height
            height = temp
        }

        if (fileSize == 0L) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                fileSize = stream.available().toLong()
            }
        }

        ImageMetadata(
            uri = uri,
            fileName = fileName,
            width = if (width > 0) width else 1080,
            height = if (height > 0) height else 1080,
            fileSize = fileSize,
            mimeType = mimeType
        )
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun loadSampledBitmap(context: Context, uri: Uri, maxWidth: Int = 3840, maxHeight: Int = 3840): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var inSampleSize = 1
        if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalArgumentException("Could not decode image from URI: $uri")

        val rotation = getExifOrientation(context, uri)
        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
            if (rotated != decodedBitmap) {
                decodedBitmap.recycle()
            }
            rotated
        } else {
            decodedBitmap
        }
    }

    suspend fun processImage(
        context: Context,
        uri: Uri,
        compressOptions: ImageCompressOptions? = null,
        resizeOptions: ImageResizeOptions? = null
    ): ProcessResult = withContext(Dispatchers.IO) {
        val meta = getImageMetadata(context, uri)
        val sourceBitmap = loadSampledBitmap(context, uri)

        var currentBitmap = sourceBitmap

        // 1. Calculate Target Dimensions if Resizing
        if (resizeOptions != null) {
            val (newWidth, newHeight) = calculateNewDimensions(
                meta.width, meta.height, resizeOptions
            )
            if (newWidth > 0 && newHeight > 0 && (newWidth != currentBitmap.width || newHeight != currentBitmap.height)) {
                val scaled = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                if (scaled != currentBitmap) {
                    currentBitmap.recycle()
                    currentBitmap = scaled
                }
            }
        }

        // 2. Determine Output Format and Quality
        val targetFormat = compressOptions?.format ?: OutputFormat.JPEG
        val initialQuality = when (compressOptions?.level) {
            CompressLevel.SMALL -> 50
            CompressLevel.BALANCED -> 75
            CompressLevel.HIGH_QUALITY -> 90
            CompressLevel.CUSTOM -> compressOptions.customQuality
            null -> 80
        }

        // 3. Compress & Target File Size Logic
        val compressFormat = when (targetFormat) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
        }

        val targetSizeBytes = compressOptions?.targetFileSizeKb?.let { it * 1024 }
        var byteArray: ByteArray

        if (targetSizeBytes != null && targetSizeBytes > 0) {
            // Iteratively adjust quality & dimensions if needed to meet target file size
            var quality = initialQuality
            var iterationCount = 0
            val maxIterations = 15

            do {
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(compressFormat, quality, stream)
                byteArray = stream.toByteArray()
                stream.close()

                if (byteArray.size <= targetSizeBytes || quality <= 15 || iterationCount >= maxIterations) {
                    if (byteArray.size > targetSizeBytes && iterationCount < maxIterations) {
                        // Downscale bitmap dimensions by 90% step
                        val scaledW = (currentBitmap.width * 0.85).toInt().coerceAtLeast(100)
                        val scaledH = (currentBitmap.height * 0.85).toInt().coerceAtLeast(100)
                        val scaled = Bitmap.createScaledBitmap(currentBitmap, scaledW, scaledH, true)
                        if (scaled != currentBitmap) {
                            currentBitmap.recycle()
                            currentBitmap = scaled
                        }
                    } else {
                        break
                    }
                } else {
                    quality = (quality - 10).coerceAtLeast(10)
                }
                iterationCount++
            } while (true)
        } else {
            val stream = ByteArrayOutputStream()
            currentBitmap.compress(compressFormat, initialQuality, stream)
            byteArray = stream.toByteArray()
            stream.close()
        }

        val resultWidth = currentBitmap.width
        val resultHeight = currentBitmap.height
        currentBitmap.recycle()

        // 4. Save to temporary cache file first for preview
        val ext = when (targetFormat) {
            OutputFormat.JPEG -> "jpg"
            OutputFormat.PNG -> "png"
            OutputFormat.WEBP -> "webp"
        }
        val cleanName = meta.fileName.substringBeforeLast('.')
        val outputFileName = "${cleanName}_optimized.$ext"

        val cacheDir = File(context.cacheDir, "processed_photos").apply { mkdirs() }
        val outputFile = File(cacheDir, outputFileName)
        FileOutputStream(outputFile).use { fos ->
            fos.write(byteArray)
        }

        val outputUri = Uri.fromFile(outputFile)

        ProcessResult(
            outputUri = outputUri,
            savedPath = outputFile.absolutePath,
            outputFileName = outputFileName,
            originalSize = meta.fileSize,
            resultSize = byteArray.size.toLong(),
            originalWidth = meta.width,
            originalHeight = meta.height,
            resultWidth = resultWidth,
            resultHeight = resultHeight,
            format = targetFormat
        )
    }

    private fun calculateNewDimensions(
        origWidth: Int,
        origHeight: Int,
        options: ImageResizeOptions
    ): Pair<Int, Int> {
        val aspect = origWidth.toDouble() / origHeight.toDouble()
        return when (options.preset) {
            ResizePreset.P25 -> (origWidth * 0.25).toInt() to (origHeight * 0.25).toInt()
            ResizePreset.P50 -> (origWidth * 0.50).toInt() to (origHeight * 0.50).toInt()
            ResizePreset.P75 -> (origWidth * 0.75).toInt() to (origHeight * 0.75).toInt()
            ResizePreset.P1080P -> {
                if (origWidth >= origHeight) {
                    1080 to (1080 / aspect).toInt()
                } else {
                    (1080 * aspect).toInt() to 1080
                }
            }
            ResizePreset.P1920P -> {
                if (origWidth >= origHeight) {
                    1920 to (1920 / aspect).toInt()
                } else {
                    (1920 * aspect).toInt() to 1920
                }
            }
            ResizePreset.CUSTOM -> {
                var w = options.customWidth
                var h = options.customHeight
                if (options.lockAspectRatio) {
                    if (w > 0 && h == 0) {
                        h = (w / aspect).toInt()
                    } else if (h > 0 && w == 0) {
                        w = (h * aspect).toInt()
                    }
                }
                w to h
            }
        }
    }

    suspend fun saveToGallery(context: Context, tempFileUri: Uri, fileName: String, format: OutputFormat): Uri? = withContext(Dispatchers.IO) {
        val mimeType = when (format) {
            OutputFormat.JPEG -> "image/jpeg"
            OutputFormat.PNG -> "image/png"
            OutputFormat.WEBP -> "image/webp"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PhotoCompressor")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (galleryUri != null) {
            try {
                resolver.openOutputStream(galleryUri)?.use { outStream ->
                    resolver.openInputStream(tempFileUri)?.use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(galleryUri, contentValues, null, null)
                }
                return@withContext galleryUri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }

    fun shareImage(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Image")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareMultipleImages(context: Context, uris: List<Uri>) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Images")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes B"
        }
    }
}
