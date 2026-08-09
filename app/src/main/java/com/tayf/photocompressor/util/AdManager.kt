package com.tayf.photocompressor.util

import android.content.Context
import android.util.Log

enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED
}

interface AdListener {
    fun onAdLoaded(type: AdType) {}
    fun onAdFailedToLoad(type: AdType, error: String) {}
    fun onAdDismissed(type: AdType) {}
    fun onUserEarnedReward(type: AdType, rewardAmount: Int) {}
}

/**
 * AdManager abstraction layer.
 * Prepares support for future ad network integration (e.g. Appodeal/AdMob).
 * Runs with a mock dev implementation without third-party ad SDK dependencies.
 */
class AdManager private constructor() {
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            Log.d("AdManager", "AdManager mock initialized successfully.")
        }
    }

    fun showBannerAd(context: Context) {
        // Placeholder for banner ad placement
        Log.d("AdManager", "Banner ad requested (mock)")
    }

    fun showInterstitialAd(context: Context, onAdDismissed: () -> Unit) {
        // Placeholder for interstitial ad placement after user action
        Log.d("AdManager", "Interstitial ad requested (mock)")
        onAdDismissed()
    }

    fun showRewardedAd(context: Context, onRewardEarned: (Int) -> Unit, onAdClosed: () -> Unit) {
        // Placeholder for rewarded ad placement
        Log.d("AdManager", "Rewarded ad requested (mock)")
        onRewardEarned(1)
        onAdClosed()
    }

    companion object {
        val instance: AdManager by lazy { AdManager() }
    }
}
