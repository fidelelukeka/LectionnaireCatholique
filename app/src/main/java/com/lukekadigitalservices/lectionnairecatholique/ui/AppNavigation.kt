package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lukekadigitalservices.lectionnairecatholique.data.ApiResult
import com.lukekadigitalservices.lectionnairecatholique.data.local.ThemeSettings
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AppNavigation(viewModel: LiturgieViewModel, themeSettings: ThemeSettings) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- GESTION DU THÈME ---
    val currentThemeMode by themeSettings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isDarkLayout = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val uiState = viewModel.uiState

    // --- ÉTAT DE SÉLECTION ---
    var selectedDateString by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selectedDate = remember(selectedDateString) { LocalDate.parse(selectedDateString) }
    var selectedZone by rememberSaveable { mutableStateOf("france") }

    // Extraction des données de succès
    val successData = (uiState as? ApiResult.Success)?.data
    val liturgicalColorName = successData?.informations?.couleur
    val currentLiturgicalColor = mapLiturgicalColor(liturgicalColorName, isDarkLayout)

    // Chargement automatique lors du changement de critères
    LaunchedEffect(selectedDate, selectedZone) {
        viewModel.loadMesse(selectedDate.toString(), selectedZone)
    }

    NavHost(navController = navController, startDestination = "home") {

        // --- ÉCRAN D'ACCUEIL ---
        composable("home") {
            UniversalScaffold(
                title = "Lectionnaire",
                themeMode = currentThemeMode,
                onThemeModeSelected = { mode -> scope.launch { themeSettings.setThemeMode(mode) } },
                liturgicalColorName = liturgicalColorName,
                liturgicalColor = currentLiturgicalColor,
                onNavigateToInfo = { navController.navigate("info") },
                showInfoButton = true
            ) { padding, accentColor, _ ->
                HomeScreen(
                    modifier = Modifier.padding(padding),
                    selectedDate = selectedDate,
                    selectedZone = selectedZone,
                    apiResponse = successData,
                    errorState = (uiState as? ApiResult.Error),
                    isLoading = uiState is ApiResult.Loading,
                    adaptiveColor = accentColor,
                    isWhiteTheme = liturgicalColorName?.lowercase() == "blanc",
                    onDateSelected = { date -> selectedDateString = date.toString() },
                    onZoneSelected = { zone -> selectedZone = zone },
                    onNavigateToLectures = { messe ->
                        viewModel.setCurrentMesse(messe)
                        navController.navigate("lectures")
                    },
                    onValidateClick = {
                        successData?.messes?.firstOrNull()?.let {
                            viewModel.setCurrentMesse(it)
                            navController.navigate("lectures")
                        }
                    },
                    onRetryClick = { viewModel.loadMesse(selectedDate.toString(), selectedZone) }
                )
            }
        }

        // --- ÉCRAN DES LECTURES ---
        composable("lectures") {
            if (successData == null) {
                // Sécurité : retour à l'accueil si les données sont perdues
                LaunchedEffect(Unit) { navController.popBackStack("home", false) }
            } else {
                // Gestion de l'index de la messe sélectionnée (ex: Messe du soir, Matines)
                var selectedMesseIndex by remember {
                    mutableIntStateOf(
                        successData.messes.indexOf(viewModel.selectedMesse).coerceAtLeast(0)
                    )
                }

                UniversalScaffold(
                    title = "Lectures",
                    subtitle = successData.informations.date,
                    showBackButton = true,
                    onBack = { navController.popBackStack() },
                    themeMode = currentThemeMode,
                    onThemeModeSelected = { mode -> scope.launch { themeSettings.setThemeMode(mode) } },
                    liturgicalColorName = liturgicalColorName,
                    liturgicalColor = currentLiturgicalColor,
                    onNavigateToInfo = { navController.navigate("info") },
                    bottomBar = {
                        if (successData.messes.size > 1) {
                            val isWhite = liturgicalColorName?.lowercase() == "blanc"
                            val containerColor = if (isWhite && !isDarkLayout) MaterialTheme.colorScheme.surface else currentLiturgicalColor

                            val contentColor = if (isWhite || currentLiturgicalColor.luminance() > 0.5f) {
                                if (isDarkLayout) Color.White else MaterialTheme.colorScheme.primary
                            } else Color.White

                            Surface(tonalElevation = 8.dp, color = containerColor) {
                                ScrollableTabRow(
                                    selectedTabIndex = selectedMesseIndex,
                                    containerColor = Color.Transparent,
                                    contentColor = contentColor,
                                    edgePadding = 16.dp,
                                    indicator = { tabPositions ->
                                        if (selectedMesseIndex < tabPositions.size) {
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMesseIndex]),
                                                color = contentColor
                                            )
                                        }
                                    },
                                    divider = {}
                                ) {
                                    successData.messes.forEachIndexed { index, messe ->
                                        Tab(
                                            selected = selectedMesseIndex == index,
                                            onClick = { selectedMesseIndex = index },
                                            text = {
                                                Text(
                                                    text = messe.nom,
                                                    color = if (selectedMesseIndex == index) contentColor else contentColor.copy(alpha = 0.6f),
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { paddingValues, accentColor, _ ->
                    LecturesScreen(
                        modifier = Modifier.padding(paddingValues),
                        response = successData,
                        selectedMesseIndex = selectedMesseIndex,
                        viewModel = viewModel, // viewModel injecté ici, remplace le TODO()
                        accentColor = accentColor
                    )
                }
            }
        }

        // --- ÉCRAN À PROPOS ---
        composable("info") {
            UniversalScaffold(
                title = "À Propos",
                showBackButton = true,
                onBack = { navController.popBackStack() },
                themeMode = currentThemeMode,
                onThemeModeSelected = { mode -> scope.launch { themeSettings.setThemeMode(mode) } },
                liturgicalColorName = liturgicalColorName,
                liturgicalColor = currentLiturgicalColor,
                showInfoButton = false
            ) { padding, accentColor, _ ->
                DeveloperInfoScreen(
                    modifier = Modifier.padding(padding),
                    accentColor = accentColor,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}