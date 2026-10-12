package com.yonsei.koreanlearner.ui.screen.spacing

import androidx.lifecycle.ViewModel
import com.yonsei.koreanlearner.algorithm.korean.KoreanSpacingCheckerEngine
import com.yonsei.koreanlearner.algorithm.korean.SpacingCheckResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SpacingCorrectionUiState(
    val currentCaseIndex   : Int = 0,
    val drillCases         : List<KoreanSpacingCheckerEngine.DrillCase> = KoreanSpacingCheckerEngine.PRESET_DRILL_CASES,
    val insertedSpaces     : Set<Int> = emptySet(), // 字符索引位置集合 (在第 i 个字符后插入空格)
    val checkResult        : SpacingCheckResult? = null,
    val isSubmitted        : Boolean = false
)

@HiltViewModel
class SpacingCorrectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SpacingCorrectionUiState())
    val state: StateFlow<SpacingCorrectionUiState> = _state.asStateFlow()

    fun toggleSpaceAt(charIndex: Int) {
        if (_state.value.isSubmitted) return
        _state.update {
            val current = it.insertedSpaces
            val updated = if (current.contains(charIndex)) {
                current - charIndex
            } else {
                current + charIndex
            }
            it.copy(insertedSpaces = updated)
        }
    }

    fun submitCheck() {
        val currentCase = _state.value.drillCases[_state.value.currentCaseIndex]
        val rawChars = currentCase.rawUnspaced.toCharArray()

        val sb = StringBuilder()
        rawChars.forEachIndexed { index, c ->
            sb.append(c)
            if (_state.value.insertedSpaces.contains(index)) {
                sb.append(" ")
            }
        }
        val userSpacedText = sb.toString().trim()

        val result = KoreanSpacingCheckerEngine.correctSpacing(userSpacedText)
        _state.update {
            it.copy(
                isSubmitted = true,
                checkResult = result
            )
        }
    }

    fun resetDrill() {
        _state.update {
            it.copy(
                insertedSpaces = emptySet(),
                checkResult = null,
                isSubmitted = false
            )
        }
    }

    fun nextCase() {
        _state.update {
            val nextIdx = (it.currentCaseIndex + 1) % it.drillCases.size
            SpacingCorrectionUiState(
                currentCaseIndex = nextIdx,
                drillCases = it.drillCases
            )
        }
    }
}
