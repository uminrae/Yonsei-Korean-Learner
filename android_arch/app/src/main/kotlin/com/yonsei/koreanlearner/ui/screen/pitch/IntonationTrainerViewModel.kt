package com.yonsei.koreanlearner.ui.screen.pitch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.algorithm.korean.IntonationTemplate
import com.yonsei.koreanlearner.algorithm.korean.IntonationTemplateDatabase
import com.yonsei.koreanlearner.core.audio.MicrophonePitchRecorder
import com.yonsei.koreanlearner.core.audio.YinPitchDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntonationTrainerUiState(
    val currentTemplateIndex : Int = 0,
    val templates            : List<IntonationTemplate> = IntonationTemplateDatabase.TEMPLATES,
    val isRecording          : Boolean = false,
    val userPitchTrack       : List<Float> = emptyList(),
    val intonationScore      : Int? = null
)

@HiltViewModel
class IntonationTrainerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(IntonationTrainerUiState())
    val state: StateFlow<IntonationTrainerUiState> = _state.asStateFlow()

    private val recorder = MicrophonePitchRecorder()
    private var recordJob: Job? = null

    fun selectTemplate(index: Int) {
        stopRecording()
        _state.update {
            it.copy(
                currentTemplateIndex = index,
                userPitchTrack = emptyList(),
                intonationScore = null
            )
        }
    }

    fun toggleRecording() {
        if (_state.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        _state.update {
            it.copy(
                isRecording = true,
                userPitchTrack = emptyList(),
                intonationScore = null
            )
        }

        recordJob = viewModelScope.launch {
            recorder.startPitchStream().collect { pitch ->
                _state.update {
                    val updatedList = (it.userPitchTrack + pitch).takeLast(40)
                    it.copy(userPitchTrack = updatedList)
                }
            }
        }
    }

    private fun stopRecording() {
        recordJob?.cancel()
        recordJob = null

        val currentTrack = _state.value.userPitchTrack
        val targetTemplate = _state.value.templates[_state.value.currentTemplateIndex]

        val calculatedScore = if (currentTrack.size >= 5) {
            YinPitchDetector.calculateIntonationScore(currentTrack, targetTemplate.pitchCurve)
        } else {
            null
        }

        _state.update {
            it.copy(
                isRecording = false,
                intonationScore = calculatedScore
            )
        }
    }

    /**
     * 演示用：注入一组拟合母语者的跟读测试音高序列
     */
    fun injectSimulatedUserPitch() {
        val target = _state.value.templates[_state.value.currentTemplateIndex]
        val simulated = target.pitchCurve.map { (it + (Math.random().toFloat() * 0.08f - 0.04f)).coerceIn(0.1f, 1.0f) }
        val score = YinPitchDetector.calculateIntonationScore(simulated, target.pitchCurve)

        _state.update {
            it.copy(
                userPitchTrack = simulated,
                intonationScore = score,
                isRecording = false
            )
        }
    }
}
