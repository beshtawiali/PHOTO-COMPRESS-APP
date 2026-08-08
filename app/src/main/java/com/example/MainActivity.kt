package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.navigation.AppNavigation
import com.example.ui.theme.PhotoCompressorTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AdManager

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AdManager.instance.initialize(applicationContext)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            PhotoCompressorTheme(themeMode = themeMode) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
