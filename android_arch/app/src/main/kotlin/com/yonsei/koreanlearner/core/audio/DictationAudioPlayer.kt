package com.yonsei.koreanlearner.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/**
 * 播放器状态枚举
 */
sealed class DictationPlaybackState {
    data object Idle : DictationPlaybackState()
    data object Preparing : DictationPlaybackState()
    data class Playing(val isLoopingSingleLine: Boolean, val speed: Float) : DictationPlaybackState()
    data class Paused(val positionMs: Long) : DictationPlaybackState()
    data class Error(val message: String) : DictationPlaybackState()
}

/**
 * ══════════════════════════════════════════════════════════════════
 * 逐句精听与听写专用音频控制器 (DictationAudioPlayer)
 * ══════════════════════════════════════════════════════════════════
 */
class DictationAudioPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    // 播放状态与当前毫秒进度
    private val _playbackState = MutableStateFlow<DictationPlaybackState>(DictationPlaybackState.Idle)
    val playbackState: StateFlow<DictationPlaybackState> = _playbackState.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    // A-B 单句单段循环参数
    private var isLoopingSegment = false
    private var loopStartMs = 0L
    private var loopEndMs = 0L

    private var currentSpeed = 1.0f

    init {
        initMediaPlayer()
    }

    private var isPrepared = false

    private fun initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    isPrepared = true
                    _durationMs.value = mp.duration.toLong()
                    _playbackState.value = DictationPlaybackState.Paused(0L)
                }
                setOnCompletionListener {
                    if (!isLoopingSegment) {
                        _playbackState.value = DictationPlaybackState.Paused(0L)
                        stopProgressTracker()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    // 静默吞掉底层空音频源错误，保证 TTS 接管
                    true
                }
            }
        } catch (_: Exception) {
            mediaPlayer = null
        }
    }

    /**
     * 加载并准备音频资源（支持 Assets 资源或模拟内置 Demo 音频）
     */
    fun loadAsset(context: Context, assetFileName: String) {
        try {
            _playbackState.value = DictationPlaybackState.Preparing
            mediaPlayer?.reset()
            isPrepared = false
            val afd = context.assets.openFd(assetFileName)
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer?.prepareAsync()
        } catch (_: IOException) {
            _playbackState.value = DictationPlaybackState.Idle
        }
    }

    /**
     * 播放 / 暂停切换
     */
    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null && isPrepared) {
            try {
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.value = DictationPlaybackState.Paused(player.currentPosition.toLong())
                    stopProgressTracker()
                } else {
                    player.start()
                    applySpeed(currentSpeed)
                    _playbackState.value = DictationPlaybackState.Playing(isLoopingSegment, currentSpeed)
                    startProgressTracker()
                }
            } catch (_: Exception) {}
        } else {
            // 模拟播放态切换，交由 TTS 驱动
            val currentlyPlaying = _playbackState.value is DictationPlaybackState.Playing
            if (currentlyPlaying) {
                _playbackState.value = DictationPlaybackState.Paused(0L)
            } else {
                _playbackState.value = DictationPlaybackState.Playing(isLoopingSegment, currentSpeed)
            }
        }
    }

    fun play() {
        val player = mediaPlayer
        if (player != null && isPrepared) {
            try {
                if (!player.isPlaying) {
                    player.start()
                    applySpeed(currentSpeed)
                    _playbackState.value = DictationPlaybackState.Playing(isLoopingSegment, currentSpeed)
                    startProgressTracker()
                }
            } catch (_: Exception) {}
        } else {
            _playbackState.value = DictationPlaybackState.Playing(isLoopingSegment, currentSpeed)
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = DictationPlaybackState.Paused(player.currentPosition.toLong())
            stopProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _currentPositionMs.value = positionMs
    }

    /**
     * 设定单句 A-B 循环区间并立即定位播放
     */
    fun setSingleLineLoop(startMs: Long, endMs: Long, enableLoop: Boolean = true) {
        this.loopStartMs = startMs
        this.loopEndMs = endMs
        this.isLoopingSegment = enableLoop
        seekTo(startMs)
        play()
    }

    /**
     * 切换播放倍速 (0.8x / 1.0x / 1.2x)
     */
    fun setSpeed(speed: Float) {
        this.currentSpeed = speed
        applySpeed(speed)
        if (mediaPlayer?.isPlaying == true) {
            _playbackState.value = DictationPlaybackState.Playing(isLoopingSegment, speed)
        }
    }

    private fun applySpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.playbackParams = PlaybackParams().apply {
                            this.speed = speed
                            this.pitch = 1.0f // 保持自然音调不失真
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // ── 高频进度监控与 A-B 循环拦截 ─────────────────────────
    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _currentPositionMs.value = pos

                // A-B 单句循环检查
                if (isLoopingSegment && loopEndMs > loopStartMs && pos >= loopEndMs) {
                    seekTo(loopStartMs)
                }

                delay(80L) // 80ms 刷新率，保证波形与字幕平滑同步且低功耗
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = DictationPlaybackState.Idle
    }
}
