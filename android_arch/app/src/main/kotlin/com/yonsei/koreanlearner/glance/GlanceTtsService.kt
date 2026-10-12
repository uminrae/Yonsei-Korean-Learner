package com.yonsei.koreanlearner.glance

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * ══════════════════════════════════════════════════════════════════
 * 桌面小组件专用轻量 TTS 播放后台 Service (GlanceTtsService)
 * ══════════════════════════════════════════════════════════════════
 */
class GlanceTtsService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var pendingText: String? = null
    private var isInitialized = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(applicationContext, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra("speak_text")
        if (text != null) {
            if (isInitialized) {
                speak(text)
            } else {
                pendingText = text
            }
        }
        return START_NOT_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        stopSelf() // 朗读完毕后自动释放 Service
                    }
                    override fun onError(utteranceId: String?) {
                        stopSelf()
                    }
                })
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            }
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GLANCE_TTS_UTTERANCE")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_KOREAN_TEXT = "speak_text"
    }
}
