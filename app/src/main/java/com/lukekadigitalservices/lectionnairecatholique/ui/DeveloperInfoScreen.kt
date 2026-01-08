package com.lukekadigitalservices.lectionnairecatholique.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun DeveloperInfoScreen(
    modifier: Modifier = Modifier,
    accentColor: Color, // Injecté depuis AppNavigation (Scaffold)
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- LOGIQUE DE NAVIGATION ---
    fun openLink(url: String) = try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    // --- LOGIQUE DE COPIE ---
    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copié", Toast.LENGTH_SHORT).show()
    }

    // --- LOGIQUE DE PARTAGE ---
    fun shareApp() {
        val playStoreLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val message = "🙏 Découvre l'app 'Lectionnaire Catholique' pour suivre la Parole de Dieu quotidiennement : $playStoreLink"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Partager via"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.15f) // Adaptatif
        ) {
            Icon(
                Icons.Default.Person,
                null,
                modifier = Modifier.padding(20.dp),
                tint = accentColor // Adaptatif
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Fidèle Lukeka", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Lukeka Digital Services", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION DONS ---
        SectionHeader("Soutenir le projet (Dons)", accentColor)

        SocialItem(Icons.Default.Smartphone, "M-Pesa (RDC)", "+243 827 808 428", accentColor) {
            copyToClipboard("+243827808428", "Numéro M-Pesa")
        }
        SocialItem(Icons.Default.Payments, "Airtel Money (RDC)", "+243 979 413 421", accentColor) {
            copyToClipboard("+243979413421", "Numéro Airtel Money")
        }

        // Bouton Tipeee mis en évidence
        Button(
            onClick = { openLink("https://fr.tipeee.com/lukekadigitalservices") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF96854)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tipeee (Soutien International)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION RÉSEAUX (GRILLE) ---
        SectionHeader("Réseaux Sociaux", accentColor)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val socialLinks = listOf(
                Triple(Icons.Default.Share, "LinkedIn", "https://linkedin.com/in/fidele-lukeka"),
                Triple(Icons.Default.AlternateEmail, "X (Twitter)", "https://x.com/lukeka_fidele"),
                Triple(Icons.Default.PlayCircle, "YouTube", "https://www.youtube.com/@fidelelukeka"),
                Triple(Icons.Default.Code, "GitHub", "https://github.com/fidelelukeka"),
                Triple(Icons.Default.Terminal, "Google Dev", "https://g.dev/fidelelukeka"),
                Triple(Icons.Default.Language, "Facebook", "https://www.facebook.com/fidelelukeka"),
                Triple(Icons.Default.Email, "Email", "mailto:fidelelukeka@gmail.com"),
                Triple(Icons.Default.ChatBubbleOutline, "Threads", "https://www.threads.net/@fidelelukeka")
            )

            for (i in socialLinks.indices step 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SocialGridItem(Modifier.weight(1f), socialLinks[i].first, socialLinks[i].second, accentColor) { openLink(socialLinks[i].third) }
                    SocialGridItem(Modifier.weight(1f), socialLinks[i+1].first, socialLinks[i+1].second, accentColor) { openLink(socialLinks[i+1].third) }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- ACTION FINALE ---
        Button(
            onClick = { shareApp() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor) // Adaptatif
        ) {
            Icon(Icons.Default.IosShare, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Recommander l'application", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("© 2026 Lukeka Digital Services", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SocialGridItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor // Adaptatif
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, accentColor: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = accentColor, // Adaptatif
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SocialItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f) // Adaptatif
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = accentColor // Adaptatif
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}