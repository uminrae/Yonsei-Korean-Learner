package com.yonsei.koreanlearner.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════
 * 低延迟韩语音频预热与 SoundPool 资源池管理器 (KoreanSoundPoolManager)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心特性：
 * 1. 采用 Android 原生 SoundPool（最大并发 6 路流），音频加载进内存后实现 <50ms 超低延迟即时播放。
 * 2. 结合 LruCache（容量 64 条）缓存 SoundId，避免频繁重复解码与 GC 内存抖动。
 * 3. 滑动预加载机制：在列表滑动时，自动异步预加载当前项的前 3 个与后 3 个词条音频。
 */
@Singleton
class KoreanSoundPoolManager @Inject constructor(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(audioAttributes)
        .build()

    // LRU Sound ID 内存缓存 (WordId -> SoundId)
    private val soundIdLruCache = object : LruCache<Int, Int>(64) {
        override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Int, newValue: Int?) {
            if (evicted && oldValue != 0) {
                soundPool.unload(oldValue)
            }
        }
    }

    // 记录正在异步加载中的集合，防止重复发出 IO 请求
    private val pendingLoads = ConcurrentHashMap.newKeySet<Int>()

    // 记录已经成功加载完毕可立即播放的 Sound ID 集合
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            }
        }
    }

    /**
     * 滑动视口音频预加载窗口：
     * 预加载 [currentIndex - windowSize, currentIndex + windowSize] 范围内的音频
     */
    fun preloadWindow(wordIds: List<Int>, currentIndex: Int, windowSize: Int = 3) {
        if (wordIds.isEmpty()) return

        val start = (currentIndex - windowSize).coerceAtLeast(0)
        val end = (currentIndex + windowSize).coerceAtMost(wordIds.size - 1)

        val targetBatch = wordIds.subList(start, end + 1)
        preloadBatch(targetBatch)
    }

    /**
     * 异步批量预加载音频
     */
    fun preloadBatch(wordIds: List<Int>) {
        externalScope.launch(Dispatchers.IO) {
            wordIds.forEach { id ->
                loadSoundForWord(id)
            }
        }
    }

    /**
     * 超低延迟播放单词发音（若已在 SoundPool 内存池中，<50ms 即刻出声）
     *
     * @param wordId 单词 ID
     * @param fallbackTts 当本地音频缺失时的降级 TTS 回调
     */
    fun playWordAudio(
        wordId: Int,
        volume: Float = 1.0f,
        fallbackTts: (() -> Unit)? = null
    ) {
        val soundId = soundIdLruCache.get(wordId)
        if (soundId != null && loadedSoundIds.contains(soundId)) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // 未预热或缺失本地音频，异步尝试加载并执行降级播放
            externalScope.launch(Dispatchers.IO) {
                val loadedId = loadSoundForWord(wordId)
                if (loadedId != null) {
                    soundPool.play(loadedId, volume, volume, 1, 0, 1.0f)
                } else {
                    fallbackTts?.invoke()
                }
            }
        }
    }

    /**
     * 加载单词本地 OGG/MP3 缓存文件到 SoundPool
     */
    private fun loadSoundForWord(wordId: Int): Int? {
        val cachedId = soundIdLruCache.get(wordId)
        if (cachedId != null) return cachedId

        if (!pendingLoads.add(wordId)) return null

        return try {
            val audioFile = getLocalAudioFile(wordId)
            if (audioFile.exists()) {
                val soundId = soundPool.load(audioFile.absolutePath, 1)
                soundIdLruCache.put(wordId, soundId)
                soundId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            pendingLoads.remove(wordId)
        }
    }

    private fun getLocalAudioFile(wordId: Int): File {
        val audioDir = File(context.cacheDir, "audio_cache")
        if (!audioDir.exists()) audioDir.mkdirs()
        return File(audioDir, "word_${wordId}.ogg")
    }

    /**
     * 释放 SoundPool 与清空缓存
     */
    fun release() {
        soundIdLruCache.evictAll()
        loadedSoundIds.clear()
        pendingLoads.clear()
        soundPool.release()
    }
}
