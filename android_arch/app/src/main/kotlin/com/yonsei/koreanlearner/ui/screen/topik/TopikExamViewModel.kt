package com.yonsei.koreanlearner.ui.screen.topik

import androidx.lifecycle.ViewModel
import com.yonsei.koreanlearner.algorithm.korean.TopikAbilityScores
import com.yonsei.koreanlearner.algorithm.korean.TopikQuestion
import com.yonsei.koreanlearner.algorithm.korean.TopikQuestionGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TopikExamUiState(
    val questions           : List<TopikQuestion> = TopikQuestionGenerator.generateAdaptiveExam(),
    val currentQuestionIndex: Int = 0,
    val userAnswers         : Map<Int, Int> = emptyMap(), // QuestionId -> OptionIndex
    val isSubmitted         : Boolean = false,
    val abilityScores       : TopikAbilityScores = TopikAbilityScores()
)

@HiltViewModel
class TopikExamViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TopikExamUiState())
    val state: StateFlow<TopikExamUiState> = _state.asStateFlow()

    fun selectOption(questionId: Int, optionIndex: Int) {
        _state.update {
            it.copy(userAnswers = it.userAnswers + (questionId to optionIndex))
        }
    }

    fun nextQuestion() {
        _state.update {
            if (it.currentQuestionIndex + 1 < it.questions.size) {
                it.copy(currentQuestionIndex = it.currentQuestionIndex + 1)
            } else {
                it
            }
        }
    }

    fun previousQuestion() {
        _state.update {
            if (it.currentQuestionIndex > 0) {
                it.copy(currentQuestionIndex = it.currentQuestionIndex - 1)
            } else {
                it
            }
        }
    }

    fun submitExam() {
        val scores = TopikQuestionGenerator.calculateAbilityScores(
            _state.value.questions,
            _state.value.userAnswers
        )
        _state.update {
            it.copy(
                isSubmitted = true,
                abilityScores = scores
            )
        }
    }

    fun restartExam() {
        val newQuestions = TopikQuestionGenerator.generateAdaptiveExam()
        _state.update {
            TopikExamUiState(questions = newQuestions)
        }
    }
}
