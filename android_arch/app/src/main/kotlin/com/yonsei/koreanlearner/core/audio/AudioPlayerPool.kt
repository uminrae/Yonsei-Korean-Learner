package com.yonsei.koreanlearner.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════
 * 零 GC 压力 MediaPlayer 实例池化管理器（AudioPlayerPool）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心优化：
 * 1. 维护固定上限（如 3 个）的活跃 MediaPlayer 实例池，复用 reset() 替代 release()/new()。
 * 2. 避免列表滚动连续点击播放不同例句音频时，产生短生命周期对象与大量 Native AudioTrack 泄漏。
 * 3. 异步平滑释放与生命周期回收。
 */
@Singleton
class AudioPlayerPool @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val maxPoolSize = 3
    private val availablePlayers = ConcurrentLinkedQueue<MediaPlayer>()
    private var activePlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        // 预创建 2 个实例置入空闲池
        repeat(2) {
            availablePlayers.offer(createPlayer())
        }
    }

    private fun createPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                _isPlaying.value = false
                recycle(this)
            }
            setOnErrorListener { mp, _, _ ->
                _isPlaying.value = false
                mp.reset()
                recycle(mp)
                true
            }
        }
    }

    /**
     * 播放本地 Assets 或 URL 音频
     */
    fun playAsset(assetFileName: String) {
        scope.launch {
            stopCurrent()
            val player = obtainPlayer()
            activePlayer = player

            try {
                val afd = context.assets.openFd(assetFileName)
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                player.prepareAsync()
                player.setOnPreparedListener {
                    it.start()
                    _isPlaying.value = true
                }
            } catch (e: Exception) {
                _isPlaying.value = false
                player.reset()
                recycle(player)
            }
        }
    }

    private fun obtainPlayer(): MediaPlayer {
        return availablePlayers.poll() ?: createPlayer()
    }

    private fun recycle(player: MediaPlayer) {
        player.reset()
        if (availablePlayers.size < maxPoolSize) {
            availablePlayers.offer(player)
        } else {
            player.release()
        }
    }

    fun stopCurrent() {
        activePlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            recycle(player)
            activePlayer = null
            _isPlaying.value = false
        }
    }

    fun releaseAll() {
        stopCurrent()
        while (availablePlayers.isNotEmpty()) {
            availablePlayers.poll()?.release()
        }
        scope.cancel()
    }
}
