package com.lukekadigitalservices.lectionnairecatholique.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukekadigitalservices.lectionnairecatholique.data.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Enumération des modes de thèmes disponibles
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class LiturgieViewModel(
    private val repository: LiturgieRepository
) : ViewModel() {

    // --- ÉTAT DES MESSES ---
    var uiState by mutableStateOf<ApiResult<MessesResponse>>(ApiResult.Loading)
        private set

    private var lastLoadedDate: String? = null
    private var lastLoadedZone: String? = null

    // --- ÉTAT DE L'AUDIO (TTS) ---
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // Contient la référence (lecture.ref) de ce qui est lu actuellement
    var currentlyPlayingRef by mutableStateOf<String?>(null)
        private set

    // --- ÉTAT DE LA NAVIGATION / SÉLECTION ---
    var selectedMesse by mutableStateOf<Messe?>(null)
        private set

    fun setCurrentMesse(messe: Messe) {
        selectedMesse = messe
    }

    /**
     * Charge les messes avec vérification pour éviter les doubles appels
     */
    fun loadMesse(date: String, zone: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && date == lastLoadedDate && zone == lastLoadedZone && uiState is ApiResult.Success) {
            return
        }

        lastLoadedDate = date
        lastLoadedZone = zone
        uiState = ApiResult.Loading

        viewModelScope.launch {
            repository.getMesses(date, zone).collectLatest { result ->
                uiState = result
            }
        }
    }

    // --- LOGIQUE AUDIO (TTS) ---

    /**
     * Lance ou arrête la lecture d'une lecture spécifique
     */
    fun toggleAudio(context: Context, lecture: MesseLecture) {
        if (currentlyPlayingRef == lecture.ref) {
            stopAudio()
        } else {
            // On arrête toute lecture en cours avant d'en lancer une nouvelle
            stopAudio()
            readAloud(context, lecture)
        }
    }

    fun stopAudio() {
        tts?.stop()
        currentlyPlayingRef = null
    }

    private fun readAloud(context: Context, lecture: MesseLecture) {
        val cleanText = Html.fromHtml(lecture.contenu, Html.FROM_HTML_MODE_COMPACT).toString()
        val ref = lecture.ref ?: "lecture_unique"

        // Initialisation à la demande pour économiser les ressources
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    executeSpeech(cleanText, ref)
                }
            }
        } else {
            executeSpeech(cleanText, ref)
        }
    }

    private fun executeSpeech(text: String, ref: String) {
        tts?.apply {
            language = Locale.FRENCH

            // Écouteur pour mettre à jour l'UI quand la voix s'arrête
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    currentlyPlayingRef = utteranceId
                }
                override fun onDone(utteranceId: String?) {
                    currentlyPlayingRef = null
                }
                override fun onError(utteranceId: String?) {
                    currentlyPlayingRef = null
                }
            })

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, ref)
            }

            speak(text, TextToSpeech.QUEUE_FLUSH, params, ref)
        }
    }

    // --- PARTAGE ---

    fun shareLecture(context: Context, lecture: MesseLecture) {
        val cleanContent = Html.fromHtml(lecture.contenu, Html.FROM_HTML_MODE_COMPACT).toString().trim()
        val acclamation = if (lecture.type.lowercase().contains("evangile") && !lecture.versetEvangile.isNullOrEmpty()) {
            "\n\n🔥 ACCLAMATION :\n" + Html.fromHtml(lecture.versetEvangile, Html.FROM_HTML_MODE_COMPACT).toString().trim()
        } else ""

        val shareText = buildString {
            append("📖 ${lecture.type}\n")
            if (!lecture.titre.isNullOrEmpty()) append("${lecture.titre}\n")
            append("📍 Réf : ${lecture.ref}\n\n")
            append(cleanContent)
            append(acclamation)
            append("\n\n---\n🕊️ Application Lectionnaire Catholique")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Partager via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // --- NETTOYAGE ---

    override fun onCleared() {
        tts?.apply {
            stop()
            shutdown()
        }
        super.onCleared()
    }
}