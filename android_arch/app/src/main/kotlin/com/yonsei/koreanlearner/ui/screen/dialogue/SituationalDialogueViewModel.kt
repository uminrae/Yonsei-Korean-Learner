package com.yonsei.koreanlearner.ui.screen.dialogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.algorithm.korean.DialogueMessage
import com.yonsei.koreanlearner.algorithm.korean.DialogueOption
import com.yonsei.koreanlearner.algorithm.korean.DialogueScenario
import com.yonsei.koreanlearner.algorithm.korean.DialogueStateMachine
import com.yonsei.koreanlearner.algorithm.korean.DialogueStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SituationalDialogueUiState(
    val currentScenario     : DialogueScenario = DialogueStateMachine.SCENARIOS[0],
    val currentStep         : DialogueStep? = DialogueStateMachine.SCENARIOS[0].steps[DialogueStateMachine.SCENARIOS[0].initialStepId],
    val messages            : List<DialogueMessage> = emptyList(),
    val isNpcTyping         : Boolean = false,
    val totalScore          : Int = 0,
    val isCompleted         : Boolean = false
)

@HiltViewModel
class SituationalDialogueViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SituationalDialogueUiState())
    val state: StateFlow<SituationalDialogueUiState> = _state.asStateFlow()

    init {
        startScenario(_state.value.currentScenario)
    }

    fun selectScenario(scenario: DialogueScenario) {
        _state.update {
            it.copy(
                currentScenario = scenario,
                currentStep = scenario.steps[scenario.initialStepId],
                messages = emptyList(),
                totalScore = 0,
                isCompleted = false
            )
        }
        startScenario(scenario)
    }

    private fun startScenario(scenario: DialogueScenario) {
        val firstStep = scenario.steps[scenario.initialStepId] ?: return
        viewModelScope.launch {
            _state.update { it.copy(isNpcTyping = true) }
            delay(600) // 模拟打字延时
            val initialNpcMsg = DialogueMessage(
                id = "npc_0",
                senderName = firstStep.npcSpeakerName,
                isUser = false,
                koreanText = firstStep.npckoreanText,
                chineseText = firstStep.npcChineseText
            )
            _state.update {
                it.copy(
                    isNpcTyping = false,
                    messages = listOf(initialNpcMsg),
                    currentStep = firstStep
                )
            }
        }
    }

    fun chooseOption(option: DialogueOption) {
        val currentStep = _state.value.currentStep ?: return

        // 1. 追加用户发送的消息气泡
        val userMsg = DialogueMessage(
            id = "user_${System.currentTimeMillis()}",
            senderName = "나",
            isUser = true,
            koreanText = option.koreanText,
            chineseText = option.chineseText,
            rating = option.rating,
            feedback = option.feedbackExplanation
        )

        val newScore = _state.value.totalScore + option.rating.score
        _state.update {
            it.copy(
                messages = it.messages + userMsg,
                totalScore = newScore,
                currentStep = null // 暂时隐藏选项等待 NPC 回复
            )
        }

        // 2. 判断是否有下一跳
        val nextStepId = option.nextStepId
        if (nextStepId != null) {
            val nextStep = _state.value.currentScenario.steps[nextStepId]
            if (nextStep != null) {
                viewModelScope.launch {
                    _state.update { it.copy(isNpcTyping = true) }
                    delay(800)
                    val nextNpcMsg = DialogueMessage(
                        id = "npc_${System.currentTimeMillis()}",
                        senderName = nextStep.npcSpeakerName,
                        isUser = false,
                        koreanText = nextStep.npckoreanText,
                        chineseText = nextStep.npcChineseText
                    )
                    _state.update {
                        it.copy(
                            isNpcTyping = false,
                            messages = it.messages + nextNpcMsg,
                            currentStep = nextStep
                        )
                    }
                }
            } else {
                _state.update { it.copy(isCompleted = true) }
            }
        } else {
            // 剧情结束
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
