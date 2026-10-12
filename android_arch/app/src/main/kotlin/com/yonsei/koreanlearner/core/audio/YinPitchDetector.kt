package com.yonsei.koreanlearner.core.audio

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * ══════════════════════════════════════════════════════════════════
 * 纯 Kotlin YIN 语音基频(F0)检测与 DTW 语调相似度评估器
 * ══════════════════════════════════════════════════════════════════
 */
object YinPitchDetector {

    private const val DEFAULT_THRESHOLD = 0.15f

    /**
     * 从 16bit 单声道 PCM 浮点采样数组中提取基频 (Fundamental Frequency / F0)
     *
     * @param buffer 采样缓冲区 (归一化到 [-1.0, 1.0])
     * @param sampleRate 采样率 (默认 44100Hz)
     * @param threshold YIN 算法绝对阈值 (默认 0.15)
     * @return 提取的基频 F0 (Hz)，若检测不到音高则返回 -1f (静音/纯噪声)
     */
    fun getPitch(
        buffer: FloatArray,
        sampleRate: Int = 44100,
        threshold: Float = DEFAULT_THRESHOLD
    ): Float {
        val bufferSize = buffer.size
        val halfBufferSize = bufferSize / 2
        if (halfBufferSize <= 0) return -1f

        // 能量检查：判断是否为静音
        var rms = 0f
        for (sample in buffer) {
            rms += sample * sample
        }
        rms = sqrt(rms / bufferSize)
        if (rms < 0.01f) return -1f // 音量过小视为静音

        // 步骤 1：计算差分函数 (Difference Function)
        val yinBuffer = FloatArray(halfBufferSize)
        for (tau in 0 until halfBufferSize) {
            var diffSum = 0f
            for (i in 0 until halfBufferSize) {
                val delta = buffer[i] - buffer[i + tau]
                diffSum += delta * delta
            }
            yinBuffer[tau] = diffSum
        }

        // 步骤 2：累积平均归一化差分函数 (Cumulative Mean Normalized Difference)
        yinBuffer[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfBufferSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / runningSum
        }

        // 步骤 3：绝对阈值查找周期 (Absolute Threshold)
        var tauEstimate = -1
        for (tau in 2 until halfBufferSize) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < halfBufferSize && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tauEstimate = tau + 1
                    break
                }
                if (tauEstimate == -1) tauEstimate = tau
                break
            }
        }

        // 若未低于阈值，寻找全局最小值
        if (tauEstimate == -1) {
            var minTau = 2
            var minVal = yinBuffer[2]
            for (tau in 3 until halfBufferSize) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    minTau = tau
                }
            }
            if (minVal < 0.35f) {
                tauEstimate = minTau
            } else {
                return -1f // 无周期信号
            }
        }

        // 步骤 4：抛物线插值细化精度 (Parabolic Interpolation)
        val betterTau: Float = if (tauEstimate > 0 && tauEstimate < halfBufferSize - 1) {
            val s0 = yinBuffer[tauEstimate - 1]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[tauEstimate + 1]
            tauEstimate + (s2 - s0) / (2f * (2f * s1 - s2 - s0))
        } else {
            tauEstimate.toFloat()
        }

        // 步骤 5：换算基频 F0
        val f0 = sampleRate / betterTau
        return if (f0 in 70f..550f) f0 else -1f // 人声常见有效基频区间
    }

    /**
     * 基于 DTW (Dynamic Time Warping) 动态时间规整算法评估两条音高轨迹的语调相似度
     *
     * @param userPitchPoints 用户实际发音音高轨迹 (归一化到 0.0~1.0)
     * @param targetTemplatePoints 标准母语者语调模板曲线 (归一化到 0.0~1.0)
     * @return 语调契合度得分 (0~100 分)
     */
    fun calculateIntonationScore(
        userPitchPoints: List<Float>,
        targetTemplatePoints: List<Float>
    ): Int {
        if (userPitchPoints.isEmpty() || targetTemplatePoints.isEmpty()) return 0

        val n = userPitchPoints.size
        val m = targetTemplatePoints.size

        // 构建 DTW 累计距离矩阵
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
        dtw[0][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = abs(userPitchPoints[i - 1] - targetTemplatePoints[j - 1])
                val minPrev = minOf(dtw[i - 1][j], dtw[i][j - 1], dtw[i - 1][j - 1])
                dtw[i][j] = cost + minPrev
            }
        }

        val totalCost = dtw[n][m]
        val avgDistance = totalCost / (n + m)

        // 将平均距离折算为百分制得分 (距离越小得分越高)
        val score = (100f - (avgDistance * 250f)).toInt().coerceIn(20, 98)
        return score
    }
}
