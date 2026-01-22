package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueRose
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueRoseDark
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueRouge
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueRougeDark
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueVert
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueVertDark
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueViolet
import com.lukekadigitalservices.lectionnairecatholique.ui.theme.LiturgiqueVioletDark

@Composable
fun mapLiturgicalColor(name: String?, isDark: Boolean): Color {
    val colorName = name?.lowercase()?.trim() ?: ""

    return when (colorName) {
        "vert" -> if (isDark) LiturgiqueVertDark else LiturgiqueVert
        "violet" -> if (isDark) LiturgiqueVioletDark else LiturgiqueViolet
        "rouge" -> if (isDark) LiturgiqueRougeDark else LiturgiqueRouge
        "rose" -> if (isDark) LiturgiqueRoseDark else LiturgiqueRose
        "blanc" -> if (isDark) Color.White else Color.Black
        else -> if (isDark) Color.White else Color.Black
    }
}