package com.example.util

import android.os.Bundle
import android.util.Log

/**
 * Simple internal event abstraction for app analytics.
 * Uses a no-op / local logging implementation until external analytics SDK is added.
 */
object AnalyticsManager {
    private const val TAG = "AnalyticsManager"

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        val details = params?.entries?.joinToString { "${it.key}=${it.value}" } ?: "no_params"
        Log.d(TAG, "Event logged: [$eventName] -> $details")
    }

    fun logAppOpen() = logEvent("app_open")
    fun logOnboardingCompleted() = logEvent("onboarding_completed")
    fun logCompressionStarted(fileCount: Int) = logEvent("compression_started", mapOf("count" to fileCount))
    fun logCompressionCompleted(savedPercent: Int) = logEvent("compression_completed", mapOf("saved_percent" to savedPercent))
    fun logResizeStarted(fileCount: Int) = logEvent("resize_started", mapOf("count" to fileCount))
    fun logResizeCompleted() = logEvent("resize_completed")
    fun logBatchStarted(totalCount: Int) = logEvent("batch_started", mapOf("total" to totalCount))
    fun logBatchCompleted(successCount: Int, failCount: Int) = logEvent("batch_completed", mapOf("success" to successCount, "fail" to failCount))
    fun logImageSaved() = logEvent("image_saved")
    fun logImageShared() = logEvent("image_shared")
    fun logPremiumOpened() = logEvent("premium_opened")
    fun logRewardedAdRequested() = logEvent("rewarded_ad_requested")
}
