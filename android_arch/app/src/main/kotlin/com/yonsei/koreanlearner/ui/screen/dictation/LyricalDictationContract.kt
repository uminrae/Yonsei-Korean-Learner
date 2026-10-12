package com.yonsei.koreanlearner.ui.screen.dictation

import com.yonsei.koreanlearner.core.audio.LrcParser
import com.yonsei.koreanlearner.data.local.kpop.KPopLyricsLibrary
import com.yonsei.koreanlearner.data.local.kpop.KPopSong

/**
 * ══════════════════════════════════════════════════════════════════
 * 歌词逐句精听与听写沙盒 MVI 契约 (LyricalDictationContract)
 * ══════════════════════════════════════════════════════════════════
 */

data class LyricalDictationUiState(
    val selectedSongId       : String = "gidle_hann_winter",
    val availableSongs       : List<KPopSong> = KPopLyricsLibrary.SONGS,
    val title                : String = "한(寒) (HANN - Alone in winter)",
    val artist               : String = "(여자)아이들 ((G)I-DLE)",
    val albumCoverTag        : String = "4th Mini Album [I burn] 收录曲",
    val keyGrammarNotes      : List<String> = emptyList(),
    val lines                : List<LrcParser.LrcLine> = emptyList(),
    val currentLineIndex     : Int = 0,
    val currentPositionMs    : Long = 0L,
    val durationMs           : Long = 0L,
    val isPlaying            : Boolean = false,
    val isLoopingSingleLine  : Boolean = false,
    val speed                : Float = 1.0f,
    val isDictationMode      : Boolean = false,  // 默认开启歌词精读模式，可随时切换听写填空
    val userInputs           : Map<Int, String> = emptyMap(),      // lineIndex -> 用户输入
    val verificationResults  : Map<Int, Boolean> = emptyMap(),     // lineIndex -> 是否正确
    val phoneticHints        : Map<Int, String?> = emptyMap()      // lineIndex -> 音变提示
)

sealed interface LyricalDictationIntent {
    data class SelectSong(val songId: String) : LyricalDictationIntent
    data class SelectLine(val index: Int) : LyricalDictationIntent
    data object TogglePlayPause : LyricalDictationIntent
    data object ToggleSingleLineLoop : LyricalDictationIntent
    data class ChangeSpeed(val speed: Float) : LyricalDictationIntent
    data object ToggleDictationMode : LyricalDictationIntent
    data class InputAnswer(val lineIndex: Int, val text: String) : LyricalDictationIntent
    data class VerifyAnswer(val lineIndex: Int) : LyricalDictationIntent
    data class RevealAnswer(val lineIndex: Int) : LyricalDictationIntent
}

sealed interface LyricalDictationEffect {
    data object PlayHapticSuccess : LyricalDictationEffect
    data object PlayHapticError : LyricalDictationEffect
    data class AutoScrollToLine(val lineIndex: Int) : LyricalDictationEffect
    data class ShowToast(val message: String) : LyricalDictationEffect
}
