package com.yonsei.koreanlearner.ui.screen.stroke

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.algorithm.korean.HangulLetterStroke
import com.yonsei.koreanlearner.algorithm.korean.HangulStrokeData
import com.yonsei.koreanlearner.ui.components.UserDrawnStroke
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HangulStrokeUiState(
    val currentLetterIndex : Int = 0,
    val letters            : List<HangulLetterStroke> = HangulStrokeData.LETTERS,
    val userStrokes        : List<UserDrawnStroke> = emptyList(),
    val isDemonstrating    : Boolean = false,
    val evaluationScore    : Int? = null
)

@HiltViewModel
class HangulStrokeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HangulStrokeUiState())
    val state: StateFlow<HangulStrokeUiState> = _state.asStateFlow()

    fun selectLetter(index: Int) {
        _state.update {
            it.copy(
                currentLetterIndex = index,
                userStrokes = emptyList(),
                isDemonstrating = false,
                evaluationScore = null
            )
        }
    }

    fun addStroke(stroke: UserDrawnStroke) {
        _state.update {
            it.copy(
                userStrokes = it.userStrokes + stroke,
                evaluationScore = null
            )
        }
    }

    fun undoStroke() {
        _state.update {
            if (it.userStrokes.isNotEmpty()) {
                it.copy(userStrokes = it.userStrokes.dropLast(1), evaluationScore = null)
            } else {
                it
            }
        }
    }

    fun clearCanvas() {
        _state.update {
            it.copy(userStrokes = emptyList(), evaluationScore = null)
        }
    }

    fun playDemonstration() {
        viewModelScope.launch {
            _state.update { it.copy(isDemonstrating = true, userStrokes = emptyList()) }
            delay(1700)
            _state.update { it.copy(isDemonstrating = false) }
        }
    }

    fun evaluateHandwriting() {
        val currentLetter = _state.value.letters[_state.value.currentLetterIndex]
        val strokeCount = _state.value.userStrokes.size

        // 简易契合度：若笔画数吻合给出高分 92~98，否则给出提示
        val baseScore = if (strokeCount == currentLetter.totalStrokes) {
            (90 + (Math.random() * 8).toInt())
        } else {
            (70 + (Math.random() * 15).toInt())
        }

        _state.update { it.copy(evaluationScore = baseScore) }
    }
}
