package com.yonsei.koreanlearner.ui.screen.dictation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.core.audio.DictationAudioPlayer
import com.yonsei.koreanlearner.core.audio.DictationPlaybackState
import com.yonsei.koreanlearner.core.audio.LrcParser
import com.yonsei.koreanlearner.data.local.kpop.KPopLyricsLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════
 * 歌词逐句精析与填空听写 ViewModel (LyricalDictationViewModel)
 * ══════════════════════════════════════════════════════════════════
 */
@HiltViewModel
class LyricalDictationViewModel @Inject constructor() : ViewModel() {

    val audioPlayer = DictationAudioPlayer(viewModelScope)

    private val _state = MutableStateFlow(LyricalDictationUiState())
    val state: StateFlow<LyricalDictationUiState> = _state.asStateFlow()

    private val _effect = Channel<LyricalDictationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        selectSong("gidle_hann_winter")
        observeAudioPlayer()
    }

    fun selectSong(songId: String) {
        val song = KPopLyricsLibrary.SONGS.find { it.id == songId } ?: KPopLyricsLibrary.SONGS.first()
        val parsed = LrcParser.parseLrc(song.lrcContent)

        _state.update {
            it.copy(
                selectedSongId = song.id,
                title = song.title,
                artist = song.artist,
                albumCoverTag = song.albumCoverTag,
                keyGrammarNotes = song.keyGrammarNotes,
                lines = parsed,
                currentLineIndex = 0,
                currentPositionMs = 0L,
                durationMs = parsed.lastOrNull()?.endTimeMs ?: 30000L,
                userInputs = emptyMap(),
                verificationResults = emptyMap(),
                phoneticHints = emptyMap()
            )
        }
        audioPlayer.seekTo(0L)
    }

    private fun observeAudioPlayer() {
        // 监听毫秒进度并计算当前高亮行
        viewModelScope.launch {
            audioPlayer.currentPositionMs.collect { posMs ->
                val lines = _state.value.lines
                if (lines.isEmpty()) return@collect

                val activeIndex = lines.indexOfLast { posMs >= it.startTimeMs }.coerceAtLeast(0)
                if (activeIndex != _state.value.currentLineIndex) {
                    _state.update { it.copy(currentLineIndex = activeIndex, currentPositionMs = posMs) }
                    _effect.send(LyricalDictationEffect.AutoScrollToLine(activeIndex))
                } else {
                    _state.update { it.copy(currentPositionMs = posMs) }
                }
            }
        }

        // 监听播放状态
        viewModelScope.launch {
            audioPlayer.playbackState.collect { playState ->
                when (playState) {
                    is DictationPlaybackState.Playing -> {
                        _state.update {
                            it.copy(
                                isPlaying = true,
                                isLoopingSingleLine = playState.isLoopingSingleLine,
                                speed = playState.speed
                            )
                        }
                    }
                    is DictationPlaybackState.Paused -> {
                        _state.update { it.copy(isPlaying = false) }
                    }
                    is DictationPlaybackState.Idle -> {
                        _state.update { it.copy(isPlaying = false) }
                    }
                    is DictationPlaybackState.Error -> {
                        _effect.send(LyricalDictationEffect.ShowToast(playState.message))
                    }
                    else -> {}
                }
            }
        }
    }

    fun onIntent(intent: LyricalDictationIntent) {
        when (intent) {
            is LyricalDictationIntent.SelectSong -> selectSong(intent.songId)
            is LyricalDictationIntent.SelectLine -> selectLine(intent.index)
            LyricalDictationIntent.TogglePlayPause -> togglePlayPause()
            LyricalDictationIntent.ToggleSingleLineLoop -> toggleSingleLineLoop()
            is LyricalDictationIntent.ChangeSpeed -> changeSpeed(intent.speed)
            LyricalDictationIntent.ToggleDictationMode -> toggleDictationMode()
            is LyricalDictationIntent.InputAnswer -> inputAnswer(intent.lineIndex, intent.text)
            is LyricalDictationIntent.VerifyAnswer -> verifyAnswer(intent.lineIndex)
            is LyricalDictationIntent.RevealAnswer -> revealAnswer(intent.lineIndex)
        }
    }

    private fun selectLine(index: Int) {
        val lines = _state.value.lines
        val target = lines.getOrNull(index) ?: return
        _state.update { it.copy(currentLineIndex = index) }
        if (_state.value.isLoopingSingleLine) {
            audioPlayer.setSingleLineLoop(target.startTimeMs, target.endTimeMs, enableLoop = true)
        } else {
            audioPlayer.seekTo(target.startTimeMs)
            audioPlayer.play()
        }
    }

    private fun togglePlayPause() {
        val state = _state.value
        val currentLine = state.lines.getOrNull(state.currentLineIndex)
        if (!state.isPlaying && state.isLoopingSingleLine && currentLine != null) {
            audioPlayer.setSingleLineLoop(currentLine.startTimeMs, currentLine.endTimeMs, enableLoop = true)
        } else {
            audioPlayer.togglePlayPause()
        }
    }

    private fun toggleSingleLineLoop() {
        val currentLoop = _state.value.isLoopingSingleLine
        val newLoop = !currentLoop
        val currentLine = _state.value.lines.getOrNull(_state.value.currentLineIndex)

        _state.update { it.copy(isLoopingSingleLine = newLoop) }

        if (newLoop && currentLine != null) {
            audioPlayer.setSingleLineLoop(currentLine.startTimeMs, currentLine.endTimeMs, enableLoop = true)
            viewModelScope.launch {
                _effect.send(LyricalDictationEffect.ShowToast("已开启单句 A-B 循环 🔁"))
            }
        } else {
            audioPlayer.setSingleLineLoop(0, 0, enableLoop = false)
            viewModelScope.launch {
                _effect.send(LyricalDictationEffect.ShowToast("已关闭单句循环 ➡️"))
            }
        }
    }

    private fun changeSpeed(speed: Float) {
        audioPlayer.setSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    private fun toggleDictationMode() {
        _state.update { it.copy(isDictationMode = !it.isDictationMode) }
    }

    private fun inputAnswer(lineIndex: Int, text: String) {
        _state.update {
            it.copy(userInputs = it.userInputs + (lineIndex to text))
        }
    }

    private fun verifyAnswer(lineIndex: Int) {
        val line = _state.value.lines.getOrNull(lineIndex) ?: return
        val targetAnswer = line.blankQuestion?.targetAnswer?.trim() ?: return
        val userInput = _state.value.userInputs[lineIndex]?.trim() ?: ""

        val isCorrect = userInput.equals(targetAnswer, ignoreCase = true)
        _state.update {
            it.copy(
                verificationResults = it.verificationResults + (lineIndex to isCorrect),
                phoneticHints = it.phoneticHints + (lineIndex to if (!isCorrect) line.blankQuestion.phoneticTip else null)
            )
        }

        viewModelScope.launch {
            if (isCorrect) {
                _effect.send(LyricalDictationEffect.PlayHapticSuccess)
                _effect.send(LyricalDictationEffect.ShowToast("✨ 拼写完全正确！"))
            } else {
                _effect.send(LyricalDictationEffect.PlayHapticError)
                _effect.send(LyricalDictationEffect.ShowToast("拼写有误，请注意音变与收音！"))
            }
        }
    }

    private fun revealAnswer(lineIndex: Int) {
        val line = _state.value.lines.getOrNull(lineIndex) ?: return
        val answer = line.blankQuestion?.targetAnswer ?: return
        _state.update {
            it.copy(
                userInputs = it.userInputs + (lineIndex to answer),
                verificationResults = it.verificationResults + (lineIndex to true)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
