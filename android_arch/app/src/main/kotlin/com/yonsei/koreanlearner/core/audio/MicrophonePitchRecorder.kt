package com.yonsei.koreanlearner.core.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * ══════════════════════════════════════════════════════════════════
 * 原生 AudioRecord 实时麦克风音高采集器 (MicrophonePitchRecorder)
 * ══════════════════════════════════════════════════════════════════
 */
class MicrophonePitchRecorder(
    private val sampleRate: Int = 44100
) {

    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

    /**
     * 开启麦克风录音并以 Flow 实时发射检测到的基频与归一化音高 (0.0~1.0)
     */
    @SuppressLint("MissingPermission")
    fun startPitchStream(): Flow<Float> = flow {
        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return@flow
            }

            audioRecord.startRecording()
            val shortBuffer = ShortArray(1024)
            val floatBuffer = FloatArray(1024)

            while (coroutineContext.isActive) {
                val readCount = audioRecord.read(shortBuffer, 0, shortBuffer.size)
                if (readCount > 0) {
                    for (i in 0 until readCount) {
                        floatBuffer[i] = shortBuffer[i] / 32768.0f
                    }

                    val f0 = YinPitchDetector.getPitch(floatBuffer, sampleRate)
                    if (f0 > 0f) {
                        // 将 80Hz ~ 400Hz 归一化到 0.0f ~ 1.0f
                        val normalized = ((f0 - 80f) / (400f - 80f)).coerceIn(0.1f, 1.0f)
                        emit(normalized)
                    }
                }
            }
        } catch (e: Exception) {
            // 权限或设备占用
        } finally {
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) {}
        }
    }.flowOn(Dispatchers.IO)
}
