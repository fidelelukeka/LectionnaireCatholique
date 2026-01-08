package com.lukekadigitalservices.lectionnairecatholique.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.text.Html
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import java.util.Locale

@Composable
fun rememberTts(context: Context): TextToSpeech {
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.FRENCH
            }
        }
        ttsInstance
    }

    // Nettoyage à la fermeture de l'écran
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    return tts
}

fun speakHtml(tts: TextToSpeech, html: String) {
    val plainText = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString()
    tts.speak(plainText, TextToSpeech.QUEUE_FLUSH, null, null)
}