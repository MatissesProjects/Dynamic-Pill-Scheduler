package com.phos.phone.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.phos.core.intelligence.SpeechSegment

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Processing(val text: String) : VoiceState()
    data class Success(val text: String, val segments: List<SpeechSegment> = emptyList()) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

class VoiceManager(private val context: Context) {

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val segments = mutableListOf<SpeechSegment>()
    private var lastSegmentEndTime: Long = 0

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = VoiceState.Listening
            segments.clear()
            lastSegmentEndTime = System.currentTimeMillis()
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            _state.value = VoiceState.Error(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val fullText = matches[0]
                // Finalize the last segment if needed
                if (segments.isEmpty() && fullText.isNotEmpty()) {
                    segments.add(SpeechSegment(fullText, lastSegmentEndTime, System.currentTimeMillis()))
                }
                _state.value = VoiceState.Success(fullText, segments.toList())
            } else {
                _state.value = VoiceState.Error("No speech detected")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partialText = matches[0]
                val now = System.currentTimeMillis()

                // Very basic heuristic for segmenting based on partial updates
                val lastText = if (segments.isNotEmpty()) segments.last().text else ""
                if (partialText.length > lastText.length) {
                    val newContent = partialText.substring(lastText.length).trim()
                    if (newContent.isNotEmpty()) {
                        segments.add(SpeechSegment(newContent, lastSegmentEndTime, now))
                        lastSegmentEndTime = now
                    }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    init {
        speechRecognizer.setRecognitionListener(recognitionListener)
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun reset() {
        _state.value = VoiceState.Idle
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}
