package com.tayf.photocompressor.util

import android.app.Activity
import android.util.Log
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdManager abstraction layer integrating Appodeal SDK 4.3.0.
 */
class AdManager private constructor() {
    private var isInitialized = false

    private val _isBannerLoaded = MutableStateFlow(false)
    val isBannerLoaded: StateFlow<Boolean> = _isBannerLoaded.asStateFlow()

    private var successfulOperationCount = 0
    private var isInterstitialPending = false

    fun initialize(activity: Activity) {
        if (!isInitialized) {
            try {
                // Enable verbose logging before initialization
                Appodeal.setLogLevel(com.appodeal.ads.utils.Log.LogLevel.verbose)

                // Set Banner Callbacks for diagnostic logging and state tracking
                Appodeal.setBannerCallbacks(object : BannerCallbacks {
                    override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
                        Log.d("AdManager_Banner", "onBannerLoaded: height=$height, isPrecache=$isPrecache, isLoaded=${Appodeal.isLoaded(Appodeal.BANNER_VIEW)}")
                        _isBannerLoaded.value = true
                    }

                    override fun onBannerFailedToLoad() {
                        Log.d("AdManager_Banner", "onBannerFailedToLoad: isLoaded=${Appodeal.isLoaded(Appodeal.BANNER_VIEW)}")
                        _isBannerLoaded.value = false
                    }

                    override fun onBannerShown() {
                        Log.d("AdManager_Banner", "onBannerShown")
                    }

                    override fun onBannerShowFailed() {
                        Log.w("AdManager_Banner", "onBannerShowFailed")
                    }

                    override fun onBannerClicked() {
                        Log.d("AdManager_Banner", "onBannerClicked")
                    }

                    override fun onBannerExpired() {
                        Log.d("AdManager_Banner", "onBannerExpired")
                        _isBannerLoaded.value = false
                    }
                })

                // Set Interstitial Callbacks for diagnostic logging
                Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
                    override fun onInterstitialLoaded(isPrecache: Boolean) {
                        Log.d("AdManager_Interstitial", "onInterstitialLoaded: isPrecache=$isPrecache")
                    }

                    override fun onInterstitialFailedToLoad() {
                        Log.d("AdManager_Interstitial", "onInterstitialFailedToLoad")
                    }

                    override fun onInterstitialShown() {
                        Log.d("AdManager_Interstitial", "onInterstitialShown")
                    }

                    override fun onInterstitialShowFailed() {
                        Log.w("AdManager_Interstitial", "onInterstitialShowFailed")
                    }

                    override fun onInterstitialClicked() {
                        Log.d("AdManager_Interstitial", "onInterstitialClicked")
                    }

                    override fun onInterstitialClosed() {
                        Log.d("AdManager_Interstitial", "onInterstitialClosed")
                    }

                    override fun onInterstitialExpired() {
                        Log.d("AdManager_Interstitial", "onInterstitialExpired")
                    }
                })

                // Initialize ONLY BANNER_VIEW and INTERSTITIAL
                val adTypes = Appodeal.BANNER_VIEW or Appodeal.INTERSTITIAL
                Appodeal.initialize(
                    activity,
                    APP_KEY,
                    adTypes,
                    object : ApdInitializationCallback {
                        override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                            if (errors.isNullOrEmpty()) {
                                Log.d("AdManager", "Appodeal initialization completed successfully")
                            } else {
                                Log.e("AdManager", "Appodeal initialization completed with ${errors.size} error(s):")
                                errors.forEachIndexed { index, err ->
                                    Log.e("AdManager", "Error #$index: class/type=${err.javaClass.name}, message=${err.message}, details=$err")
                                }
                            }

                            Log.d("AdManager", "Post-Init Diagnostic Check:")
                            Log.d("AdManager", "isInitialized(BANNER_VIEW)=${Appodeal.isInitialized(Appodeal.BANNER_VIEW)}")
                            Log.d("AdManager", "isInitialized(INTERSTITIAL)=${Appodeal.isInitialized(Appodeal.INTERSTITIAL)}")
                            Log.d("AdManager", "isAutoCacheEnabled(BANNER_VIEW)=${Appodeal.isAutoCacheEnabled(Appodeal.BANNER_VIEW)}")
                            Log.d("AdManager", "isAutoCacheEnabled(INTERSTITIAL)=${Appodeal.isAutoCacheEnabled(Appodeal.INTERSTITIAL)}")
                            Log.d("AdManager", "isLoaded(BANNER_VIEW)=${Appodeal.isLoaded(Appodeal.BANNER_VIEW)}")
                            Log.d("AdManager", "isLoaded(INTERSTITIAL)=${Appodeal.isLoaded(Appodeal.INTERSTITIAL)}")
                        }
                    }
                )
                isInitialized = true
                Log.d("AdManager", "Appodeal SDK 4.3.0 initialize called with BANNER_VIEW and INTERSTITIAL for production.")
            } catch (e: Exception) {
                Log.e("AdManager", "Failed to initialize Appodeal SDK: ${e.message}", e)
            }
        }
    }

    /**
     * Increments successful operation counter by 1.
     * Sets isInterstitialPending = true after every successful operation.
     */
    fun recordSuccessfulOperation() {
        successfulOperationCount++
        Log.d("AdManager", "Operation succeeded. Total count: $successfulOperationCount")
        isInterstitialPending = true
        Log.d("AdManager", "Operation #$successfulOperationCount reached: Interstitial pending for next natural break.")
    }

    /**
     * Shows an Interstitial ad if one is pending and loaded.
     * Must be called at a natural transition break (e.g. leaving result screen).
     */
    fun showInterstitialIfPending(activity: Activity) {
        if (isInterstitialPending) {
            isInterstitialPending = false
            try {
                if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                    Log.d("AdManager", "Showing Interstitial Ad at natural break (op count=$successfulOperationCount)")
                    Appodeal.show(activity, Appodeal.INTERSTITIAL)
                } else {
                    Log.d("AdManager", "Interstitial pending but not loaded yet. Caching for future. Continuing normally.")
                    Appodeal.cache(activity, Appodeal.INTERSTITIAL)
                }
            } catch (e: Exception) {
                Log.e("AdManager", "Error showing Interstitial: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val APP_KEY = "c580a590f883f3f3686150d675ba237fd7b525dac160c1c0"
        val instance: AdManager by lazy { AdManager() }
    }
}


