package com.yonsei.koreanlearner.ui.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.data.local.database.DatabaseInitStatus
import com.futurekorean.app.R
import kotlinx.coroutines.delay

/**
 * ══════════════════════════════════════════════════════════════════
 * 纯正极简 Ins 高光纯白启动引导页 (SplashScreen)
 * 纯净白调质感，与主界面无缝融合，内置双重容错保底
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status by viewModel.initStatus.collectAsStateWithLifecycle()
    var hasNavigated by remember { mutableStateOf(false) }

    // 1. 正常数据就绪后平滑跳转
    LaunchedEffect(status) {
        if (!hasNavigated && status is DatabaseInitStatus.Ready) {
            hasNavigated = true
            delay(250)
            onLoaded()
        }
    }

    // 2. 超级容错保底：1.2 秒后无论如何强制进入主页
    LaunchedEffect(Unit) {
        delay(1200)
        if (!hasNavigated) {
            hasNavigated = true
            onLoaded()
        }
    }

    val currentProgress = when (val s = status) {
        is DatabaseInitStatus.Loading -> s.progress
        is DatabaseInitStatus.Ready -> 1.0f
        else -> 0.4f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val stageMessage = when (val s = status) {
        is DatabaseInitStatus.Loading -> s.stageText
        is DatabaseInitStatus.Ready -> "全量词库与语法就绪"
        is DatabaseInitStatus.Error -> "离线词库就绪"
        else -> "正在启动..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // ── 1. 纯白极简原手绘徽章 ────────────────────────
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_branding),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── 2. 纯黑极简大标题 ────────────────────────────
            Text(
                text          = "未来的韩语",
                fontSize      = 24.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color(0xFF111827),
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text       = "延世韩国语 1~6 册全景研习",
                fontSize   = 12.sp,
                color      = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // ── 3. 极简黑白微细进度条 ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0.1f, 1.0f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF111827))
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── 4. 阶段文本 ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = stageMessage,
                    color    = Color(0xFF6B7280),
                    fontSize = 12.sp
                )

                Text(
                    text       = "${(animatedProgress * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF111827),
                    fontSize   = 12.sp
                )
            }
        }

        // 底部版本标头
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text          = "YONSEI KOREAN",
                fontSize      = 10.sp,
                color         = Color(0xFFB0B5C0),
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}
