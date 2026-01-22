package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukekadigitalservices.lectionnairecatholique.data.MesseLecture
import com.lukekadigitalservices.lectionnairecatholique.data.MessesResponse

@Composable
fun LecturesScreen(
    modifier: Modifier = Modifier,
    response: MessesResponse,
    selectedMesseIndex: Int,
    viewModel: LiturgieViewModel,
    accentColor: Color,
) {
    val currentMesse = response.messes.getOrNull(selectedMesseIndex) ?: response.messes.first()
    val onBackground = MaterialTheme.colorScheme.onBackground

    val groupedLectures = remember(currentMesse.lectures) {
        val groups = mutableListOf<MutableList<MesseLecture>>()

        currentMesse.lectures.forEach { lecture ->
            val lastGroup = groups.lastOrNull()
            val lastLecture = lastGroup?.lastOrNull()

            if (lastGroup != null && lastLecture != null) {
                val isSameType = lastLecture.type == lecture.type

                // On nettoie le HTML et on passe en majuscules pour la comparaison
                val currentContent = lecture.contenu.uppercase().trim()
                val prevContent = lastLecture.contenu.uppercase().trim()

                // 1. Détection des marqueurs de FIN (dans la lecture précédente)
                val prevHasChoiceAtEnd = prevContent.contains("OU BIEN") ||
                        prevContent.contains("OU ENCORE") ||
                        prevContent.contains("AU LIEU DE") ||
                        prevContent.contains("PEUT AUSSI LIRE") ||
                        prevContent.contains("L’ÉVANGILE CI-DESSOUS")

                // 2. Détection des marqueurs de DÉBUT (dans la lecture actuelle)
                val currentHasChoiceAtStart = currentContent.startsWith("<P>OU ") ||
                        currentContent.startsWith("<P><EM>OU ") ||
                        currentContent.contains("OU LECTURE BRÈVE")

                // 3. Cas spécial des lectures numérotées (toujours grouper si type identique)
                val isNumberedLecture = lecture.type.startsWith("lecture_")

                // LOGIQUE DE GROUPEMENT :
                // On groupe si c'est le même type ET (c'est une lecture numérotée OU il y a un marqueur)
                if (isSameType && (isNumberedLecture || prevHasChoiceAtEnd || currentHasChoiceAtStart)) {
                    lastGroup.add(lecture)
                } else {
                    groups.add(mutableListOf(lecture))
                }
            } else {
                groups.add(mutableListOf(lecture))
            }
        }
        groups
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopAudio() }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER DYNAMIQUE ---
        item {
            val info = response.informations

            // 1. Déterminer le titre principal
            val mainTitle = when {
                // Si c'est un dimanche ou une solennité, le nom liturgique est parfait
                info.jour?.lowercase() == "dimanche" || info.jourLiturgiqueNom?.lowercase() != "de la férie" -> {
                    info.jourLiturgiqueNom ?: "Liturgie du jour"
                }
                // Pour les féries, on utilise ligne1 qui est plus complet (ex: "mardi, 21ème Semaine...")
                !info.ligne1.isNullOrEmpty() -> info.ligne1
                // Fallback au cas où
                else -> "${info.jour ?: ""} ${info.semaine ?: ""}"
            }

            // 2. Nettoyage de la fête (éviter d'afficher "de la férie" en sous-titre)
            val subTitle = if (info.fete?.lowercase()?.contains("férie") == true) null else info.fete

            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                // Titre Principal (Nom du jour ou Saint)
                Text(
                    text = mainTitle.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 32.sp
                )

                // Affichage du Saint ou de la Mémoire (si présent)
                if (!subTitle.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = accentColor, // Vert, Rouge, Violet, etc.
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Affichage du Degré (Mémoire, Mémoire facultative, Solennité)
                if (!info.ligne3.isNullOrEmpty() && info.ligne3 != info.fete) {
                    Text(
                        text = info.ligne3,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // --- BADGE DE CONTEXTE (Temps & Année) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val anneeTxt = if (!info.annee.isNullOrEmpty()) " • Année ${info.annee}" else ""
                        val tempsTxt = info.tempsLiturgique?.replaceFirstChar { it.uppercase() } ?: "Ordinaire"

                        Text(
                            text = "Temps $tempsTxt$anneeTxt",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }

        // --- CARTES DE LECTURES ---
        itemsIndexed(groupedLectures) { indexInList, group ->
            var selectedVariantIndex by remember { mutableIntStateOf(0) }
            val currentVersion = group.getOrNull(selectedVariantIndex) ?: group[0]

            // Calcul de la numérotation (ex: Psaume I, Psaume II)
            val typeCount = groupedLectures.take(indexInList + 1).count { it.first().type == currentVersion.type }
            val totalOfType = groupedLectures.count { it.first().type == currentVersion.type }

            val displayTitle = formatType(
                type = currentVersion.type,
                countOfThisType = typeCount,
                totalOfType = totalOfType
            )

            LectureCard(
                lecture = currentVersion,
                displayTitle = displayTitle,
                accentColor = accentColor,
                viewModel = viewModel,
                variantsCount = group.size,
                selectedVariantIndex = selectedVariantIndex,
                onVariantChange = { selectedVariantIndex = it }
            )
        }

        item {
            FooterSection(accentColor, response.informations.tempsLiturgique)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureCard(
    lecture: MesseLecture,
    displayTitle: String,
    accentColor: Color,
    viewModel: LiturgieViewModel,
    variantsCount: Int,
    selectedVariantIndex: Int,
    onVariantChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val isPlaying = viewModel.currentlyPlayingRef == lecture.ref
    val onBadgeColor = if (accentColor.luminance() > 0.5f) Color.Black else Color.White

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER : TITRE ET SWITCH ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = accentColor, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = displayTitle,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBadgeColor
                    )
                }

                if (variantsCount > 1) {
                    SuggestionChip(
                        onClick = { onVariantChange((selectedVariantIndex + 1) % variantsCount) },
                        label = { Text(getVariantLabel(lecture.type, (selectedVariantIndex + 1) % variantsCount)) },
                        icon = { Icon(Icons.Default.SwapCalls, null, modifier = Modifier.size(16.dp)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = accentColor,
                            iconContentColor = accentColor,
                            containerColor = accentColor.copy(alpha = 0.05f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- SECTION AUDIO ---
            AudioControlSection(
                isPlaying = isPlaying,
                onPlayToggle = { viewModel.toggleAudio(context, lecture) },
                onShare = { viewModel.shareLecture(context, lecture) },
                accentColor = accentColor
            )

            AnimatedContent(
                targetState = lecture,
                transitionSpec = { fadeIn(tween(400)) + slideInVertically { it / 3 } togetherWith fadeOut(tween(300)) },
                label = "LectureTransition"
            ) { animLecture ->
                val isPsaume = animLecture.type.lowercase().contains("psaume") ||
                        animLecture.type.lowercase().contains("cantique")

                Column(modifier = Modifier.fillMaxWidth()) {

                    // --- ENTÊTE (TITRE OU REFRAIN) ---
                    if (!animLecture.titre.isNullOrEmpty()) {
                        // Affichage du Titre Standard pour les Lectures/Évangiles
                        Text(
                            text = animLecture.titre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else if (isPsaume && !animLecture.refrainPsalmique.isNullOrEmpty()) {
                        // Pour le psaume, le refrain est mis en forme comme un titre
                        HtmlText(
                            html = animLecture.refrainPsalmique,
                            textColor = accentColor,
                            style = MaterialTheme.typography.titleLarge.copy( // Changé de Medium à Large
                                fontWeight = FontWeight.Black,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        // Affichage de la référence du refrain (ex: Ps 71, 11)
                        // Utilisation d'une variable locale pour forcer la lecture de la propriété
                        val refDuRefrain = animLecture.refRefrain
                        if (!refDuRefrain.isNullOrBlank()) {
                            Text(
                                text = "Référence du refrain : $refDuRefrain",
                                style = MaterialTheme.typography.bodyMedium, // Plus lisible que labelSmall
                                color = accentColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        // Espace pour séparer le bloc refrain du reste
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- RÉFÉRENCE BIBLIQUE GÉNÉRALE (ex: Ps 71, 1-2...) ---
                    Text(
                        text = animLecture.ref ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPsaume) MaterialTheme.colorScheme.onSurfaceVariant else accentColor,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    HorizontalDivider(
                        Modifier.padding(vertical = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // --- CORPS DU TEXTE (Couplets ou Lecture) ---
                    val cleanedBody = cleanLectureContent(animLecture.contenu, animLecture.type)

                    HtmlText(
                        html = cleanedBody,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp)
                    )

                    // --- SECTION ACCLAMATION (Uniquement Évangile) ---
                    if (animLecture.type.lowercase().contains("evangile") && !animLecture.versetEvangile.isNullOrEmpty()) {
                        AcclamationSection(animLecture.versetEvangile, accentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun AudioControlSection(isPlaying: Boolean, onPlayToggle: () -> Unit, onShare: () -> Unit, accentColor: Color) {
    val bgColor by animateColorAsState(
        if (isPlaying) accentColor.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "AudioBg"
    )

    Surface(color = bgColor, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = onPlayToggle,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) accentColor else accentColor.copy(alpha = 0.1f),
                    contentColor = if (isPlaying) Color.White else accentColor
                )
            ) {
                Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, null)
            }
            Text(
                text = if (isPlaying) "Lecture en cours..." else "Écouter la lecture",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, null, tint = accentColor)
            }
        }
    }
}

@Composable
fun AcclamationSection(html: String, accentColor: Color) {
    Spacer(Modifier.height(24.dp))
    Surface(
        color = accentColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ACCLAMATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = accentColor)
            }
            Spacer(Modifier.height(8.dp))
            HtmlText(html = html, textColor = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FooterSection(accentColor: Color, temps: String?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("Source : AELF • Temps ${temps?.lowercase() ?: "ordinaire"}", style = MaterialTheme.typography.labelSmall)
        Text(
            text = "Développé par Lukeka Digital Services",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor.copy(alpha = 0.7f)
        )
    }
}

// --- LOGIQUE DE FORMATAGE ---

fun formatType(type: String, countOfThisType: Int = 1, totalOfType: Int = 1): String {
    val t = type.lowercase()

    // Gestion prioritaire des lectures numérotées par l'API (Lecture_1, etc.)
    if (t.startsWith("lecture_")) {
        val num = t.substringAfter("lecture_").toIntOrNull()
        val roman = when (num) {
            1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"; 6 -> "VI"; 7 -> "VII"
            else -> num?.toString() ?: ""
        }
        val suffix = if (num == 1) "-ÈRE" else "-ÈME"
        return "$roman$suffix LECTURE"
    }

    // Gestion des Psaumes et Cantiques (Numérotation romaine si plusieurs)
    val base = when (t) {
        "psaume" -> "PSAUME"
        "cantique" -> "CANTIQUE"
        "epitre" -> "ÉPÎTRE"
        "evangile" -> "ÉVANGILE"
        else -> type.uppercase().replace("_", " ")
    }

    return if (totalOfType > 1) {
        val romanSuffix = when (countOfThisType) {
            1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"; 6 -> "VI"; 7 -> "VII"
            else -> countOfThisType.toString()
        }
        "$base $romanSuffix"
    } else base
}

@Composable
fun getVariantLabel(type: String, index: Int): String {
    val t = type.lowercase()
    return when {
        t.contains("evangile") -> if (index == 0) "Texte principal" else "Autre Évangile"
        t.contains("psaume") || t.contains("cantique") -> if (index == 0) "Psaume" else "Variante"
        else -> if (index == 0) "Forme Longue" else "Forme Brève"
    }
}

fun cleanLectureContent(content: String, type: String): String {
    var cleaned = content.trim()
    val typeLower = type.lowercase()

    // 1. Couper après l'acclamation pour les Lectures et Évangiles
    if (typeLower.contains("lecture") || typeLower.contains("evangile")) {
        val markers = listOf(
            "– PAROLE DU SEIGNEUR.",
            "– ACCLAMONS LA PAROLE DE DIEU.",
            "- PAROLE DU SEIGNEUR.",
            "- ACCLAMONS LA PAROLE DE DIEU."
        )

        val upperContent = cleaned.uppercase()
        var lastFoundIndex = -1
        var usedMarkerLength = 0

        markers.forEach { marker ->
            val index = upperContent.lastIndexOf(marker)
            if (index > lastFoundIndex) {
                lastFoundIndex = index
                usedMarkerLength = marker.length
            }
        }

        if (lastFoundIndex != -1) {
            cleaned = cleaned.take(lastFoundIndex + usedMarkerLength)
        }
    }

    // 2. Supprimer les marqueurs de choix à la fin des Psaumes
    if (typeLower.contains("psaume") || typeLower.contains("cantique")) {
        val endMarkers = listOf("OU BIEN", "OU ENCORE", "OU")
        endMarkers.forEach { marker ->
            val pattern = "(?s)<P>\\s*(?:<EM>|<I>)?\\s*${marker}\\s*(?:</EM>|</I>)?\\s*</P>\\s*$".toRegex(RegexOption.IGNORE_CASE)
            cleaned = cleaned.replace(pattern, "").trim()
        }
    }

    return cleaned.trim()
}