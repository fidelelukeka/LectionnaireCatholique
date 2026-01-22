package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lukekadigitalservices.lectionnairecatholique.data.ApiResult
import com.lukekadigitalservices.lectionnairecatholique.data.Messe
import com.lukekadigitalservices.lectionnairecatholique.data.MessesResponse
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    selectedZone: String,
    apiResponse: MessesResponse?,
    errorState: ApiResult.Error?,
    isLoading: Boolean,
    adaptiveColor: Color,
    // Note: isWhiteTheme n'est plus strictement nécessaire si on utilise la luminance,
    // mais on le garde si votre logique globale en dépend.
    isWhiteTheme: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onZoneSelected: (String) -> Unit,
    onNavigateToLectures: (Messe) -> Unit,
    onValidateClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val animatedAccent by animateColorAsState(
        targetValue = adaptiveColor,
        animationSpec = tween(600),
        label = "AccentAnimation"
    )

    // Logique de contraste pour le texte sur les boutons colorés
    val onAccentColor = when {
        animatedAccent.luminance() > 0.8f -> Color.Black
        else -> Color.White
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // --- SECTION HEADER ---
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = animatedAccent
        )
        Text(
            text = "Lectionnaire",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        // --- SÉLECTEUR DE DATE ---
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = animatedAccent)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- SÉLECTEUR DE ZONE ---
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when(selectedZone) {
                    "france" -> "France / Monde"
                    "afrique" -> "Afrique"
                    "belgique" -> "Belgique"
                    "canada" -> "Canada"
                    else -> "Suisse"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Zone géographique") },
                leadingIcon = { Icon(Icons.Default.Language, null, tint = animatedAccent) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = animatedAccent,
                    focusedLabelColor = animatedAccent
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("france" to "France / Monde", "afrique" to "Afrique", "belgique" to "Belgique", "canada" to "Canada", "suisse" to "Suisse").forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = if(key == selectedZone) animatedAccent else Color.Unspecified) },
                        onClick = { onZoneSelected(key); expanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- SECTION DYNAMIQUE ---
        AnimatedContent(
            targetState = Triple(isLoading, errorState, apiResponse),
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(400)) },
            modifier = Modifier.fillMaxWidth(),
            label = "ContentTransition"
        ) { (loading, error, response) ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                when {
                    loading -> LoadingIndicator(animatedAccent)
                    error != null -> ErrorCard(onRetryClick)
                    response != null -> {
                        // CORRECTION : Appel avec seulement 3 arguments comme défini précédemment
                        LiturgicalSummary(
                            response = response,
                            tint = animatedAccent,
                            onMesseClick = onNavigateToLectures
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- BOUTON DE VALIDATION ---
        AnimatedVisibility(
            visible = apiResponse != null && !isLoading,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = fadeOut()
        ) {
            val isWhiteLiturgical = animatedAccent.luminance() > 0.9f

            Button(
                onClick = onValidateClick,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                // On ajoute une ombre si c'est blanc pour que le bouton soit visible
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isWhiteLiturgical) 6.dp else 2.dp),
                border = if (isWhiteLiturgical) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedAccent,
                    contentColor = onAccentColor
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null)
                Spacer(Modifier.width(12.dp))
                Text("LIRE LA PAROLE DU JOUR", fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        DatePickerModal(selectedDate, animatedAccent, onAccentColor, onDateSelected) { showDatePicker = false }
    }
}


@Composable
fun LiturgicalSummary(response: MessesResponse, tint: Color, onMesseClick: (Messe) -> Unit) {
    val isWhiteTime = tint.luminance() > 0.9f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // --- BADGE DU TEMPS ---
        Surface(
            color = if (isWhiteTime) Color.White else tint.copy(alpha = 0.12f),
            shape = RoundedCornerShape(30.dp),
            shadowElevation = if (isWhiteTime) 6.dp else 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = if (isWhiteTime) Color.LightGray.copy(alpha = 0.5f) else tint.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // INDICATEUR DE COULEUR (Cercle)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(tint, CircleShape)
                        .then(
                            if (isWhiteTime) Modifier.border(0.5.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                            else Modifier
                        )
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Temps ${response.informations.tempsLiturgique?.lowercase() ?: "ordinaire"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- APPEL DE LA CARTE D'INFO ---
        LiturgicalInfoCard(response, tint, onMesseClick)
    }
}

@Composable
fun LiturgicalInfoCard(
    response: MessesResponse,
    adaptiveTint: Color,
    onMesseClick: (Messe) -> Unit
) {
    val info = response.informations
    val isWhiteTime = adaptiveTint.luminance() > 0.9f

    // --- ALGORITHME DE TITRE IDENTIQUE AU HEADER ---
    val mainTitle = when {
        // Priorité aux dimanches et jours propres (non féries)
        info.jour?.lowercase() == "dimanche" || info.jourLiturgiqueNom?.lowercase() != "de la férie" -> {
            info.jourLiturgiqueNom ?: "Liturgie du jour"
        }
        // Utilisation de ligne1 pour éviter "de la férie" en titre principal
        !info.ligne1.isNullOrEmpty() -> info.ligne1
        else -> "${info.jour ?: ""} ${info.semaine ?: ""}"
    }

    // Nettoyage du sous-titre (Saints, Fêtes) pour masquer "de la férie"
    val subTitle = if (info.fete?.lowercase()?.contains("férie") == true) null else info.fete

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // BARRE LATÉRALE (100% Blanche mais détachée du fond)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(10.dp),
                // On garde la vraie couleur liturgique (même le blanc pur)
                color = adaptiveTint,
                // L'élévation crée une ombre portée naturelle sur le fond blanc
                shadowElevation = if (isWhiteTime) 8.dp else 0.dp,
                // On ajoute une bordure subtile sur les bords pour définir la forme
                border = if (isWhiteTime) BorderStroke(
                    width = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                ) else null
            ) {}

            Column(modifier = Modifier.padding(20.dp)) {
                // --- SECTION TEXTES ---
                Column {
                    // TITRE DU JOUR (Algorithme appliqué)
                    Text(
                        text = mainTitle.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWhiteTime) MaterialTheme.colorScheme.onSurface else adaptiveTint
                    )

                    // SOUS-TITRE (Saints ou Fêtes spécifiques)
                    if (!subTitle.isNullOrEmpty()) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isWhiteTime) MaterialTheme.colorScheme.secondary else adaptiveTint.copy(alpha = 0.8f)
                        )
                    }

                    // LIGNE 3 (Mémoires, détails liturgiques)
                    if (!info.ligne3.isNullOrEmpty() && info.ligne3 != info.fete) {
                        Text(
                            text = info.ligne3!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BADGE DE CONTEXTE (Temps & Année)
                Surface(
                    color = adaptiveTint.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, adaptiveTint.copy(alpha = 0.2f))
                ) {
                    val anneeTxt = if (!info.annee.isNullOrEmpty()) " • Année ${info.annee}" else ""
                    val tempsTxt = info.tempsLiturgique?.replaceFirstChar { it.uppercase() } ?: "Ordinaire"
                    Text(
                        text = "Temps $tempsTxt$anneeTxt",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWhiteTime) MaterialTheme.colorScheme.primary else adaptiveTint
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // LISTE DES MESSES (Contenu principal de la carte)
                response.messes.forEach { messe ->
                    MesseRowItem(
                        messe = messe,
                        tint = adaptiveTint,
                        onClick = { onMesseClick(messe) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
@Composable
fun MesseRowItem(messe: Messe, tint: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dans MesseRowItem
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(16.dp)
                        .then(
                            if (tint.luminance() > 0.85f)
                                Modifier.border(0.5.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                            else Modifier
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = messe.nom, style = MaterialTheme.typography.labelLarge, color = tint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // REGROUPEMENT DES LECTURES PAR TYPE POUR LE SOMMAIRE
            val groupedLectures = messe.lectures.groupBy { it.type }

            groupedLectures.forEach { (type, variants) ->
                // On récupère les références uniques pour ce type (ex: si forme longue et brève ont la même ref)
                val refs = variants.mapNotNull { it.ref }.distinct().joinToString(" ; ")

                // On calcule le numéro si plusieurs psaumes/lectures de même type existent dans la messe
                val totalOfType = groupedLectures.keys.count { it == type }

                Text(
                    text = "• ${formatTypeShort(type)} : $refs",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator(tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = tint)
        Spacer(Modifier.height(16.dp))
        Text("Recherche de la liturgie...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(selectedDate: LocalDate, tint: Color, onAccent: Color, onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()) }
                onDismiss()
            }) { Text("Confirmer", color = tint, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    ) {
        DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(selectedDayContainerColor = tint, selectedDayContentColor = onAccent, todayContentColor = tint, todayDateBorderColor = tint))
    }
}

@Composable
fun ErrorCard(onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Padding externe
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        // On utilise une Column globale pour empiler le contenu et le bouton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Aligne le contenu au centre
        ) {
            // --- SECTION ICONE ET TEXTE (Centrée) ---
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Données non disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Vérifiez votre connexion internet.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(Modifier.height(24.dp)) // Espace de sécurité pour séparer du bouton

            // --- SECTION BOUTON (Alignée à droite) ---
            TextButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End), // Aligne uniquement le bouton à droite
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "RÉESSAYER",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

fun formatTypeShort(type: String): String {
    val t = type.lowercase()
    if (t.startsWith("lecture_")) {
        val num = t.substringAfter("lecture_").toIntOrNull()
        val suffix = if (num == 1) "ère" else "ème"
        return "${num ?: ""}$suffix Lecture"
    }
    return when (t) {
        "psaume" -> "Psaume"
        "cantique" -> "Cantique"
        "epitre" -> "Épître"
        "evangile" -> "Évangile"
        else -> type.replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}