package com.lukekadigitalservices.lectionnairecatholique.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.lukekadigitalservices.lectionnairecatholique.ui.ThemeMode
import com.lukekadigitalservices.lectionnairecatholique.ui.mapLiturgicalColor

@Composable
fun LectionnaireCatholiqueTheme(
    themeMode: ThemeMode,
    liturgicalColorName: String? = null,
    content: @Composable () -> Unit
) {
    val isDarkLayout = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Calcul de la couleur de base à partir de tes nouvelles variables
    val baseColor = mapLiturgicalColor(liturgicalColorName, isDarkLayout)

    // Calcul dynamique de la couleur du texte sur la couleur primaire
    val onPrimaryColor = if (baseColor.luminance() > 0.5f) Color.Black else Color.White

    val colorScheme = if (isDarkLayout) {
        darkColorScheme(
            primary = baseColor,
            onPrimary = onPrimaryColor,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onBackground = Color.White,
            onSurface = Color.White,
            // Optionnel : on peut utiliser une variante de la base pour le contour
            outlineVariant = baseColor.copy(alpha = 0.3f)
        )
    } else {
        lightColorScheme(
            primary = baseColor,
            onPrimary = onPrimaryColor,
            background = Color(0xFFFFFBFE),
            surface = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            outlineVariant = baseColor.copy(alpha = 0.2f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}