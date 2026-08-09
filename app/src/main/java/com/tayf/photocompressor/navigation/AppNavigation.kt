package com.tayf.photocompressor.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tayf.photocompressor.ui.components.CustomBottomNavigation
import com.tayf.photocompressor.ui.screens.AboutUsScreen
import com.tayf.photocompressor.ui.screens.BatchScreen
import com.tayf.photocompressor.ui.screens.CompressScreen
import com.tayf.photocompressor.ui.screens.HistoryScreen
import com.tayf.photocompressor.ui.screens.HomeScreen
import com.tayf.photocompressor.ui.screens.OnboardingScreen
import com.tayf.photocompressor.ui.screens.PremiumScreen
import com.tayf.photocompressor.ui.screens.PrivacyPolicyScreen
import com.tayf.photocompressor.ui.screens.ResizeScreen
import com.tayf.photocompressor.ui.screens.ResultScreen
import com.tayf.photocompressor.ui.screens.SettingsScreen
import com.tayf.photocompressor.ui.viewmodel.BatchProcessState
import com.tayf.photocompressor.ui.viewmodel.MainViewModel
import com.tayf.photocompressor.ui.viewmodel.SingleProcessState
import com.tayf.photocompressor.util.ImageProcessor

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Collected View Model States
    val selectedSingleUri by viewModel.selectedSingleUri.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val singleProcessState by viewModel.singleProcessState.collectAsState()
    val compressOptions by viewModel.compressOptions.collectAsState()
    val resizeOptions by viewModel.resizeOptions.collectAsState()
    val batchItems by viewModel.batchItems.collectAsState()
    val batchState by viewModel.batchState.collectAsState()
    val isBatchCompressMode by viewModel.isBatchCompressMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val defaultQuality by viewModel.defaultQuality.collectAsState()
    val defaultFormat by viewModel.defaultFormat.collectAsState()

    val showBottomBar = currentRoute in listOf("home", "batch", "history", "settings")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CustomBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == "home") {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) "home" else "onboarding",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onFinish = {
                        viewModel.completeOnboarding()
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    recentItems = recentHistory,
                    onSelectSingleForCompress = { uri ->
                        viewModel.selectSingleImage(uri)
                        navController.navigate("compress")
                    },
                    onSelectSingleForResize = { uri ->
                        viewModel.selectSingleImage(uri)
                        navController.navigate("resize")
                    },
                    onSelectBatchForCompress = { uris ->
                        viewModel.setBatchCompressMode(true)
                        viewModel.selectBatchImages(uris)
                        navController.navigate("batch")
                    },
                    onSelectBatchForResize = { uris ->
                        viewModel.setBatchCompressMode(false)
                        viewModel.selectBatchImages(uris)
                        navController.navigate("batch")
                    },
                    onOpenHistory = { navController.navigate("history") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenPremium = { navController.navigate("premium") }
                )
            }

            composable("compress") {
                val currentUri = selectedSingleUri ?: Uri.EMPTY

                CompressScreen(
                    imageUri = currentUri,
                    state = singleProcessState,
                    compressOptions = compressOptions,
                    onUpdateLevel = { viewModel.updateCompressLevel(it) },
                    onUpdateQuality = { viewModel.updateCustomQuality(it) },
                    onUpdateTargetSize = { viewModel.updateTargetFileSizeKb(it) },
                    onUpdateFormat = { viewModel.updateOutputFormat(it) },
                    onExecuteCompress = {
                        if (currentUri != Uri.EMPTY) {
                            viewModel.executeSingleCompression(currentUri)
                            navController.navigate("result")
                        }
                    },
                    onBack = {
                        viewModel.resetSingleProcessState()
                        navController.popBackStack()
                    }
                )
            }

            composable("resize") {
                val currentUri = selectedSingleUri ?: Uri.EMPTY

                ResizeScreen(
                    imageUri = currentUri,
                    state = singleProcessState,
                    resizeOptions = resizeOptions,
                    onUpdatePreset = { preset, origW, origH -> viewModel.updateResizePreset(preset, origW, origH) },
                    onUpdateWidth = { w, origW, origH -> viewModel.updateCustomWidth(w, origW, origH) },
                    onUpdateHeight = { h, origW, origH -> viewModel.updateCustomHeight(h, origW, origH) },
                    onToggleLockAspect = { viewModel.toggleLockAspectRatio() },
                    onExecuteResize = {
                        if (currentUri != Uri.EMPTY) {
                            viewModel.executeSingleResize(currentUri)
                            navController.navigate("result")
                        }
                    },
                    onBack = {
                        viewModel.resetSingleProcessState()
                        navController.popBackStack()
                    }
                )
            }

            composable("batch") {
                BatchScreen(
                    batchItems = batchItems,
                    batchState = batchState,
                    isCompressMode = isBatchCompressMode,
                    compressOptions = compressOptions,
                    onToggleCompressMode = { viewModel.setBatchCompressMode(it) },
                    onSelectMoreImages = { viewModel.selectBatchImages(batchItems.map { item -> item.uri } + it) },
                    onUpdateQuality = { viewModel.updateCustomQuality(it) },
                    onExecuteBatch = { viewModel.executeBatchProcessing() },
                    onBatchFinishedNavToResult = { navController.navigate("history") }
                )
            }

            composable("result") {
                val result = (singleProcessState as? SingleProcessState.Success)?.result
                val beforeUri = selectedSingleUri

                ResultScreen(
                    result = result,
                    beforeUri = beforeUri,
                    onSaveToGallery = { res -> viewModel.saveResultToGallery(res) {} },
                    onShare = { uri -> ImageProcessor.shareImage(navController.context, uri) },
                    onProcessAnother = {
                        viewModel.resetSingleProcessState()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onDone = {
                        viewModel.resetSingleProcessState()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    historyList = allHistory,
                    onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
                    onClearAll = { viewModel.clearHistory() },
                    onShare = { uri -> ImageProcessor.shareImage(navController.context, uri) }
                )
            }

            composable("settings") {
                SettingsScreen(
                    themeMode = themeMode,
                    defaultQuality = defaultQuality,
                    defaultFormat = defaultFormat,
                    onSetThemeMode = { viewModel.setThemeMode(it) },
                    onSetDefaultQuality = { viewModel.setDefaultQuality(it) },
                    onSetDefaultFormat = { viewModel.setDefaultFormat(it) },
                    onOpenPremium = { navController.navigate("premium") },
                    onOpenPrivacyPolicy = { navController.navigate("privacy") },
                    onOpenAboutUs = { navController.navigate("about") }
                )
            }

            composable("privacy") {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("about") {
                AboutUsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("premium") {
                PremiumScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
