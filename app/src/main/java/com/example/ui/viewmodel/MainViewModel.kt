package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.BatchItemStatus
import com.example.data.model.BatchQueueItem
import com.example.data.model.CompressLevel
import com.example.data.model.ImageCompressOptions
import com.example.data.model.ImageMetadata
import com.example.data.model.ImageResizeOptions
import com.example.data.model.OutputFormat
import com.example.data.model.ProcessedImageEntity
import com.example.data.model.ResizePreset
import com.example.data.repository.HistoryRepository
import com.example.util.AnalyticsManager
import com.example.util.ImageProcessor
import com.example.util.ProcessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class SingleProcessState {
    object Idle : SingleProcessState()
    object LoadingMeta : SingleProcessState()
    data class Ready(val metadata: ImageMetadata) : SingleProcessState()
    data class Processing(val progressText: String = "Processing image...") : SingleProcessState()
    data class Success(val result: ProcessResult) : SingleProcessState()
    data class Error(val message: String) : SingleProcessState()
}

sealed class BatchProcessState {
    object Idle : BatchProcessState()
    data class QueueReady(val totalSize: Long) : BatchProcessState()
    data class Processing(val currentIndex: Int, val totalCount: Int) : BatchProcessState()
    data class Completed(
        val results: List<ProcessResult>,
        val successCount: Int,
        val failCount: Int,
        val totalSavedBytes: Long
    ) : BatchProcessState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val db = AppDatabase.getInstance(application)
    private val historyRepository = HistoryRepository(db.historyDao())

    val onboardingCompleted: StateFlow<Boolean> = preferencesManager.onboardingCompleted
    val themeMode: StateFlow<String> = preferencesManager.themeMode
    val defaultQuality: StateFlow<Int> = preferencesManager.defaultQuality
    val defaultFormat: StateFlow<String> = preferencesManager.defaultFormat

    val allHistory: StateFlow<List<ProcessedImageEntity>> = historyRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<ProcessedImageEntity>> = historyRepository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Single Image Operation State
    private val _singleProcessState = MutableStateFlow<SingleProcessState>(SingleProcessState.Idle)
    val singleProcessState: StateFlow<SingleProcessState> = _singleProcessState.asStateFlow()

    private val _selectedSingleUri = MutableStateFlow<Uri?>(null)
    val selectedSingleUri: StateFlow<Uri?> = _selectedSingleUri.asStateFlow()

    private val _compressOptions = MutableStateFlow(ImageCompressOptions())
    val compressOptions: StateFlow<ImageCompressOptions> = _compressOptions.asStateFlow()

    private val _resizeOptions = MutableStateFlow(ImageResizeOptions())
    val resizeOptions: StateFlow<ImageResizeOptions> = _resizeOptions.asStateFlow()

    // Batch Operation State
    private val _batchItems = MutableStateFlow<List<BatchQueueItem>>(emptyList())
    val batchItems: StateFlow<List<BatchQueueItem>> = _batchItems.asStateFlow()

    private val _batchState = MutableStateFlow<BatchProcessState>(BatchProcessState.Idle)
    val batchState: StateFlow<BatchProcessState> = _batchState.asStateFlow()

    private val _isBatchCompressMode = MutableStateFlow(true)
    val isBatchCompressMode: StateFlow<Boolean> = _isBatchCompressMode.asStateFlow()

    init {
        AnalyticsManager.logAppOpen()
    }

    fun completeOnboarding() {
        preferencesManager.setOnboardingCompleted(true)
        AnalyticsManager.logOnboardingCompleted()
    }

    fun setThemeMode(mode: String) {
        preferencesManager.setThemeMode(mode)
    }

    fun setDefaultQuality(quality: Int) {
        preferencesManager.setDefaultQuality(quality)
        _compressOptions.value = _compressOptions.value.copy(customQuality = quality)
    }

    fun setDefaultFormat(formatStr: String) {
        preferencesManager.setDefaultFormat(formatStr)
        val format = when (formatStr.uppercase()) {
            "PNG" -> OutputFormat.PNG
            "WEBP" -> OutputFormat.WEBP
            else -> OutputFormat.JPEG
        }
        _compressOptions.value = _compressOptions.value.copy(format = format)
    }

    // --- Single Image Processing Actions ---

    fun selectSingleImage(uri: Uri) {
        _selectedSingleUri.value = uri
        viewModelScope.launch {
            _singleProcessState.value = SingleProcessState.LoadingMeta
            try {
                val meta = ImageProcessor.getImageMetadata(getApplication(), uri)
                // Initialize default custom resize dimensions matching current image
                _resizeOptions.value = _resizeOptions.value.copy(
                    customWidth = meta.width,
                    customHeight = meta.height
                )
                _singleProcessState.value = SingleProcessState.Ready(meta)
            } catch (e: Exception) {
                _singleProcessState.value = SingleProcessState.Error("Failed to load image. It may be corrupted or unsupported.")
            }
        }
    }

    fun updateCompressLevel(level: CompressLevel) {
        _compressOptions.value = _compressOptions.value.copy(level = level)
    }

    fun updateCustomQuality(quality: Int) {
        _compressOptions.value = _compressOptions.value.copy(
            level = CompressLevel.CUSTOM,
            customQuality = quality
        )
    }

    fun updateTargetFileSizeKb(sizeKb: Long?) {
        _compressOptions.value = _compressOptions.value.copy(targetFileSizeKb = sizeKb)
    }

    fun updateOutputFormat(format: OutputFormat) {
        _compressOptions.value = _compressOptions.value.copy(format = format)
    }

    fun updateResizePreset(preset: ResizePreset, origWidth: Int, origHeight: Int) {
        val aspect = if (origHeight > 0) origWidth.toDouble() / origHeight.toDouble() else 1.0
        val (w, h) = when (preset) {
            ResizePreset.P25 -> (origWidth * 0.25).toInt() to (origHeight * 0.25).toInt()
            ResizePreset.P50 -> (origWidth * 0.50).toInt() to (origHeight * 0.50).toInt()
            ResizePreset.P75 -> (origWidth * 0.75).toInt() to (origHeight * 0.75).toInt()
            ResizePreset.P1080P -> if (origWidth >= origHeight) 1080 to (1080 / aspect).toInt() else (1080 * aspect).toInt() to 1080
            ResizePreset.P1920P -> if (origWidth >= origHeight) 1920 to (1920 / aspect).toInt() else (1920 * aspect).toInt() to 1920
            ResizePreset.CUSTOM -> origWidth to origHeight
        }
        _resizeOptions.value = _resizeOptions.value.copy(
            preset = preset,
            customWidth = w,
            customHeight = h
        )
    }

    fun updateCustomWidth(width: Int, origWidth: Int, origHeight: Int) {
        var h = _resizeOptions.value.customHeight
        if (_resizeOptions.value.lockAspectRatio && origWidth > 0 && origHeight > 0) {
            val aspect = origWidth.toDouble() / origHeight.toDouble()
            h = (width / aspect).toInt().coerceAtLeast(1)
        }
        _resizeOptions.value = _resizeOptions.value.copy(
            preset = ResizePreset.CUSTOM,
            customWidth = width,
            customHeight = h
        )
    }

    fun updateCustomHeight(height: Int, origWidth: Int, origHeight: Int) {
        var w = _resizeOptions.value.customWidth
        if (_resizeOptions.value.lockAspectRatio && origWidth > 0 && origHeight > 0) {
            val aspect = origWidth.toDouble() / origHeight.toDouble()
            w = (height * aspect).toInt().coerceAtLeast(1)
        }
        _resizeOptions.value = _resizeOptions.value.copy(
            preset = ResizePreset.CUSTOM,
            customWidth = w,
            customHeight = height
        )
    }

    fun toggleLockAspectRatio() {
        _resizeOptions.value = _resizeOptions.value.copy(
            lockAspectRatio = !_resizeOptions.value.lockAspectRatio
        )
    }

    fun executeSingleCompression(uri: Uri) {
        viewModelScope.launch {
            _singleProcessState.value = SingleProcessState.Processing("Compressing photo...")
            AnalyticsManager.logCompressionStarted(1)
            try {
                val result = ImageProcessor.processImage(
                    context = getApplication(),
                    uri = uri,
                    compressOptions = _compressOptions.value
                )
                saveResultToHistory(result, "Compress")
                val savedPercent = if (result.originalSize > 0) {
                    (((result.originalSize - result.resultSize).toDouble() / result.originalSize) * 100).toInt()
                } else 0
                AnalyticsManager.logCompressionCompleted(savedPercent)
                _singleProcessState.value = SingleProcessState.Success(result)
            } catch (e: Exception) {
                _singleProcessState.value = SingleProcessState.Error(
                    e.localizedMessage ?: "That image could not be processed. Please try another image."
                )
            }
        }
    }

    fun executeSingleResize(uri: Uri) {
        viewModelScope.launch {
            _singleProcessState.value = SingleProcessState.Processing("Resizing photo...")
            AnalyticsManager.logResizeStarted(1)
            try {
                val result = ImageProcessor.processImage(
                    context = getApplication(),
                    uri = uri,
                    resizeOptions = _resizeOptions.value
                )
                saveResultToHistory(result, "Resize")
                AnalyticsManager.logResizeCompleted()
                _singleProcessState.value = SingleProcessState.Success(result)
            } catch (e: Exception) {
                _singleProcessState.value = SingleProcessState.Error(
                    e.localizedMessage ?: "That image could not be processed. Please try another image."
                )
            }
        }
    }

    fun resetSingleProcessState() {
        _singleProcessState.value = SingleProcessState.Idle
    }

    // --- Batch Processing Actions ---

    fun setBatchCompressMode(isCompress: Boolean) {
        _isBatchCompressMode.value = isCompress
    }

    fun selectBatchImages(uris: List<Uri>) {
        viewModelScope.launch {
            _batchState.value = BatchProcessState.Idle
            val items = mutableListOf<BatchQueueItem>()
            var totalBytes = 0L

            for (uri in uris) {
                try {
                    val meta = ImageProcessor.getImageMetadata(getApplication(), uri)
                    totalBytes += meta.fileSize
                    items.add(
                        BatchQueueItem(
                            id = uri.toString(),
                            uri = uri,
                            fileName = meta.fileName,
                            originalSize = meta.fileSize,
                            originalWidth = meta.width,
                            originalHeight = meta.height,
                            status = BatchItemStatus.READY
                        )
                    )
                } catch (e: Exception) {
                    // skip unreadable
                }
            }

            _batchItems.value = items
            _batchState.value = BatchProcessState.QueueReady(totalBytes)
        }
    }

    fun executeBatchProcessing() {
        val currentList = _batchItems.value
        if (currentList.isEmpty()) return

        viewModelScope.launch {
            AnalyticsManager.logBatchStarted(currentList.size)
            val isCompress = _isBatchCompressMode.value
            val totalCount = currentList.size
            val results = mutableListOf<ProcessResult>()
            var successCount = 0
            var failCount = 0
            var totalSaved = 0L

            val updatedQueue = currentList.toMutableList()

            for (index in currentList.indices) {
                val item = updatedQueue[index]
                _batchState.value = BatchProcessState.Processing(index + 1, totalCount)
                updatedQueue[index] = item.copy(status = BatchItemStatus.PROCESSING)
                _batchItems.value = updatedQueue.toList()

                try {
                    val result = ImageProcessor.processImage(
                        context = getApplication(),
                        uri = item.uri,
                        compressOptions = if (isCompress) _compressOptions.value else null,
                        resizeOptions = if (!isCompress) _resizeOptions.value else null
                    )
                    results.add(result)
                    saveResultToHistory(result, if (isCompress) "Batch Compress" else "Batch Resize")
                    successCount++
                    val saved = (result.originalSize - result.resultSize).coerceAtLeast(0)
                    totalSaved += saved

                    updatedQueue[index] = item.copy(
                        status = BatchItemStatus.SUCCESS,
                        resultUri = result.outputUri,
                        resultSize = result.resultSize,
                        resultWidth = result.resultWidth,
                        resultHeight = result.resultHeight
                    )
                } catch (e: Exception) {
                    failCount++
                    updatedQueue[index] = item.copy(
                        status = BatchItemStatus.FAILED,
                        errorMessage = e.localizedMessage ?: "Failed"
                    )
                }
                _batchItems.value = updatedQueue.toList()
            }

            AnalyticsManager.logBatchCompleted(successCount, failCount)
            _batchState.value = BatchProcessState.Completed(
                results = results,
                successCount = successCount,
                failCount = failCount,
                totalSavedBytes = totalSaved
            )
        }
    }

    fun saveResultToGallery(result: ProcessResult, onSaved: (Boolean) -> Unit) {
        viewModelScope.launch {
            val savedUri = ImageProcessor.saveToGallery(
                context = getApplication(),
                tempFileUri = result.outputUri,
                fileName = result.outputFileName,
                format = result.format
            )
            val success = savedUri != null
            if (success) AnalyticsManager.logImageSaved()
            onSaved(success)
        }
    }

    private suspend fun saveResultToHistory(result: ProcessResult, opType: String) = withContext(Dispatchers.IO) {
        val entity = ProcessedImageEntity(
            fileName = result.outputFileName,
            uriString = result.outputUri.toString(),
            operationType = opType,
            originalSize = result.originalSize,
            resultSize = result.resultSize,
            originalWidth = result.originalWidth,
            originalHeight = result.originalHeight,
            resultWidth = result.resultWidth,
            resultHeight = result.resultHeight,
            format = result.format.name,
            savedPath = result.savedPath
        )
        historyRepository.addHistoryEntry(entity)
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistoryEntry(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
