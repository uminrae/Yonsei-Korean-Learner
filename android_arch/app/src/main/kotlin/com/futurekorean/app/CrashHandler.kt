package com.futurekorean.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import java.io.PrintWriter
import java.io.StringWriter

/**
 * ══════════════════════════════════════════════════════════════════
 * 全局未捕获异常防崩与实时诊断处理器 (CrashHandler)
 * 彻底杜绝无提示黑屏/闪退退回桌面，捕获所有底层崩溃并展示诊断看板
 * ══════════════════════════════════════════════════════════════════
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private var appContext: Context? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        if (defaultHandler == null) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e("FUTURE_KOREAN_CRASH", "Uncaught exception in thread ${thread.name}", throwable)

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        appContext?.let { ctx ->
            try {
                val intent = Intent(ctx, CrashDisplayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(CrashDisplayActivity.EXTRA_ERROR_MESSAGE, throwable.localizedMessage ?: throwable.javaClass.simpleName)
                    putExtra(CrashDisplayActivity.EXTRA_STACK_TRACE, stackTrace)
                }
                ctx.startActivity(intent)
            } catch (e: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } ?: run {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

/**
 * 纯原生、零依赖、绝不崩溃的错误诊断界面
 */
class CrashDisplayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMsg = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "未知异常"
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "无堆栈信息"

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(0xFF0E0E11.toInt())
            setPadding(32, 48, 32, 48)
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
        }

        val titleView = TextView(this).apply {
            text = "⚠️ 未来的韩语 · 启动异常诊断看板"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }

        val descView = TextView(this).apply {
            text = "异常类型: $errorMsg\n\n详细堆栈:"
            textSize = 13f
            setTextColor(0xFFFF6B6B.toInt())
            setPadding(0, 0, 0, 16)
        }

        val stackView = TextView(this).apply {
            text = stackTrace
            textSize = 11f
            setTextColor(0xFFCCCCCC.toInt())
            setTextIsSelectable(true)
            setBackgroundColor(0xFF1E1E26.toInt())
            setPadding(24, 24, 24, 24)
        }

        val restartBtn = Button(this).apply {
            text = "尝试安全重启"
            setOnClickListener {
                val restartIntent = packageManager.getLaunchIntentForPackage(packageName)
                restartIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(restartIntent)
                finish()
            }
        }

        layout.addView(titleView)
        layout.addView(descView)
        layout.addView(stackView)
        layout.addView(restartBtn)
        scrollView.addView(layout)

        setContentView(scrollView)
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
    }
}
