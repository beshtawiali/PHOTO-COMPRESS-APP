package com.tayf.photocompressor.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.appodeal.ads.Appodeal
import com.tayf.photocompressor.util.AdManager

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AppodealBannerView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: run {
        Log.e("AppodealBannerView", "findActivity returned null for context $context")
        return
    }

    val isBannerLoaded by AdManager.instance.isBannerLoaded.collectAsState()

    LaunchedEffect(activity) {
        if (!Appodeal.isLoaded(Appodeal.BANNER_VIEW) && !Appodeal.isLoaded(Appodeal.BANNER)) {
            Log.d("AppodealBannerView", "Requesting banner load via Appodeal.cache")
            Appodeal.cache(activity, Appodeal.BANNER_VIEW)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    if (id == View.NO_ID) {
                        id = View.generateViewId()
                    }
                    attachBannerView(activity, this, isBannerLoaded)
                }
            },
            update = { frameLayout ->
                attachBannerView(activity, frameLayout, isBannerLoaded)
            }
        )
    }
}

private fun attachBannerView(activity: Activity, frameLayout: FrameLayout, isBannerLoaded: Boolean) {
    if (frameLayout.id == View.NO_ID) {
        frameLayout.id = View.generateViewId()
    }
    val isLoadedBanner = Appodeal.isLoaded(Appodeal.BANNER)
    val isLoadedBannerView = Appodeal.isLoaded(Appodeal.BANNER_VIEW)
    Log.d("AppodealBannerView", "attachBannerView - isBannerLoadedState=$isBannerLoaded, isLoaded(BANNER)=$isLoadedBanner, isLoaded(BANNER_VIEW)=$isLoadedBannerView, childCount=${frameLayout.childCount}")

    try {
        val showResult = Appodeal.show(activity, Appodeal.BANNER_VIEW)
        Log.d("AppodealBannerView", "Appodeal.show(activity, BANNER_VIEW) returned $showResult")

        val bannerView = Appodeal.getBannerView(activity)
        Log.d("AppodealBannerView", "getBannerView(activity) returned: $bannerView")

        if (bannerView != null) {
            if (bannerView.id == View.NO_ID) {
                bannerView.id = View.generateViewId()
            }
            if (bannerView.parent != frameLayout) {
                (bannerView.parent as? ViewGroup)?.removeView(bannerView)
                frameLayout.removeAllViews()
                frameLayout.addView(
                    bannerView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            bannerView.post {
                Log.d("AppodealBannerView", "BannerView dimensions: width=${bannerView.width}, height=${bannerView.height}, visibility=${bannerView.visibility}, parent=${bannerView.parent}")
            }
        } else {
            Log.e("AppodealBannerView", "getBannerView returned null after show call")
        }
    } catch (e: Exception) {
        Log.e("AppodealBannerView", "Error in attachBannerView: ${e.message}", e)
    }
}



