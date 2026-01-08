package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalScaffold(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    showInfoButton: Boolean = true,
    onNavigateToInfo: () -> Unit = {},
    liturgicalColorName: String? = null,
    liturgicalColor: Color = MaterialTheme.colorScheme.primary,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues, Color, Color) -> Unit
) {
    // 1. Animation fluide de la couleur liturgique
    val animatedColor by animateColorAsState(
        targetValue = liturgicalColor,
        animationSpec = tween(durationMillis = 500),
        label = "LiturgicalColorAnimation"
    )

    // 2. Détection du mode (Dark/Light)
    val isDarkLayout = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // 3. INVERSION DE LOGIQUE : On utilise les couleurs du ColorScheme
    // Au lieu de forcer Color.Black ou White, on demande au thème quelle couleur
    // doit aller sur la couleur primaire (onPrimary).
    val scaffoldContentColor = if (animatedColor == MaterialTheme.colorScheme.primary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        // Calcul de sécurité pour les couleurs animées
        if (animatedColor.luminance() > 0.5f) Color.Black else Color.White
    }

    // Couleur d'accentuation pour le contenu
    val accentColor = animatedColor

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = themeMode,
            onDismiss = { showThemeDialog = false },
            onModeSelected = {
                onThemeModeSelected(it)
                showThemeDialog = false
            },
            liturgicalColorName = liturgicalColorName,
            liturgicalColor = animatedColor
        )
    }

    Scaffold(
        topBar = {
            // Utilisation directe du TopAppBar avec les couleurs thématiques
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                // On utilise une opacité sur la couleur de contenu standard
                                color = scaffoldContentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        val themeIcon = when (themeMode) {
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                        }
                        Icon(themeIcon, null)
                    }
                    if (showInfoButton) {
                        IconButton(onClick = onNavigateToInfo) {
                            Icon(Icons.Default.Info, null)
                        }
                    }
                },
                // APPLICATION DES COULEURS INVERSÉES (Basé sur le Primary du thème)
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = animatedColor,
                    titleContentColor = scaffoldContentColor,
                    navigationIconContentColor = scaffoldContentColor,
                    actionIconContentColor = scaffoldContentColor
                )
            )
        },
        bottomBar = bottomBar ?: {},
        content = { padding ->
            // On fournit au contenu :
            // 1. Le padding du scaffold
            // 2. La couleur liturgique (pour les titres/icônes)
            // 3. La couleur de contraste (pour le texte sur fond liturgique)
            content(padding, accentColor, scaffoldContentColor)
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onModeSelected: (ThemeMode) -> Unit,
    liturgicalColorName: String?,
    liturgicalColor: Color
) {
    val isWhite = liturgicalColorName?.lowercase() == "blanc"
    val accentColor = if (isWhite) MaterialTheme.colorScheme.primary else liturgicalColor

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Apparence",
                style = MaterialTheme.typography.titleLarge,
                color = if (isWhite) MaterialTheme.colorScheme.onSurface else accentColor
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ThemeOptionItem(
                    label = "Système",
                    icon = Icons.Default.SettingsSuggest,
                    mode = ThemeMode.SYSTEM,
                    isSelected = currentMode == ThemeMode.SYSTEM,
                    accentColor = accentColor,
                    onSelect = onModeSelected
                )
                ThemeOptionItem(
                    label = "Clair",
                    icon = Icons.Default.LightMode,
                    mode = ThemeMode.LIGHT,
                    isSelected = currentMode == ThemeMode.LIGHT,
                    accentColor = accentColor,
                    onSelect = onModeSelected
                )
                ThemeOptionItem(
                    label = "Sombre",
                    icon = Icons.Default.DarkMode,
                    mode = ThemeMode.DARK,
                    isSelected = currentMode == ThemeMode.DARK,
                    accentColor = accentColor,
                    onSelect = onModeSelected
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isWhite) MaterialTheme.colorScheme.primary else accentColor
                )
            ) {
                Text("Annuler", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ThemeOptionItem(
    label: String,
    icon: ImageVector,
    mode: ThemeMode,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: (ThemeMode) -> Unit
) {
    val isAccentLight = accentColor.luminance() > 0.8f

    val containerColor = if (isSelected) {
        accentColor.copy(alpha = if (isAccentLight) 0.3f else 0.15f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        if (isAccentLight) MaterialTheme.colorScheme.primary else accentColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = { onSelect(mode) },
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
            )
            Spacer(Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}