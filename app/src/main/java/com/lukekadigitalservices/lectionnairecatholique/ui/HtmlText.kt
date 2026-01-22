package com.lukekadigitalservices.lectionnairecatholique.ui

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

@Composable
fun HtmlText(
    html: String?,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    lineSpacingMultiplier: Float = 1.4f
) {
    val nativeTextColor = textColor.toArgb()
    val fontSize = style.fontSize.value

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
                setLineSpacing(0f, lineSpacingMultiplier)
                typeface = Typeface.SANS_SERIF
                includeFontPadding = false
            }
        },
        update = { textView ->
            textView.setTextColor(nativeTextColor)
            // On transforme les sauts de ligne en paragraphes HTML
            val formattedHtml = html?.replace("\n\n", "<br/>") ?: ""
            textView.text = HtmlCompat.fromHtml(formattedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    )
}