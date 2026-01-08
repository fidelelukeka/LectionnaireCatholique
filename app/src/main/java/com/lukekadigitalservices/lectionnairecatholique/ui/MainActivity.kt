package com.lukekadigitalservices.lectionnairecatholique.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lukekadigitalservices.lectionnairecatholique.data.ApiResult
import com.lukekadigitalservices.lectionnairecatholique.data.LiturgieRepository
import com.lukekadigitalservices.lectionnairecatholique.data.RetrofitClient
import com.lukekadigitalservices.lectionnairecatholique.data.local.AppDatabase
import com.lukekadigitalservices.lectionnairecatholique.data.local.ThemeSettings
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LectionnaireCatholiqueTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LiturgieViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LiturgieRepository(
            api = RetrofitClient.instance,
            dao = database.messeDao()
        )
        LiturgieViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeSettings = ThemeSettings(this)

        setContent {
            val currentThemeMode by themeSettings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val uiState = viewModel.uiState

            val isDarkLayout = when (currentThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // On extrait proprement la couleur
            val liturgicalColorName = (uiState as? ApiResult.Success)?.data?.informations?.couleur
            val liturgicalColor = mapLiturgicalColor(liturgicalColorName, isDarkLayout)

            val systemUiController = rememberSystemUiController()

            // Mise à jour des barres système uniquement quand la couleur change
            SideEffect {
                val useDarkIcons = liturgicalColor.luminance() > 0.5f
                systemUiController.setSystemBarsColor(
                    color = liturgicalColor,
                    darkIcons = useDarkIcons
                )
            }

            LectionnaireCatholiqueTheme(
                themeMode = currentThemeMode,
                liturgicalColorName = liturgicalColorName
            ) {
                // AppNavigation doit être ici
                AppNavigation(
                    viewModel = viewModel,
                    themeSettings = themeSettings
                )
            }
        }
    }
}