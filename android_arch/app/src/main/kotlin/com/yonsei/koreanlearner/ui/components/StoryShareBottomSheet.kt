package com.yonsei.koreanlearner.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import com.yonsei.koreanlearner.ui.theme.extended
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════
 * Instagram Story 快照分享底部弹窗（StoryShareBottomSheet）
 * ══════════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryShareBottomSheet(
    snapshotData: StorySnapshotGenerator.StorySnapshotData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTheme by remember { mutableStateOf(StorySnapshotGenerator.StoryTheme.DARK) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(true) }
    var isActionInProgress by remember { mutableStateOf(false) }

    // 当主题切换时异步生成对应 1080x1920 高清 Bitmap
    LaunchedEffect(selectedTheme, snapshotData) {
        isRendering = true
        generatedBitmap = StorySnapshotGenerator.renderStoryBitmap(snapshotData, selectedTheme)
        isRendering = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 顶部标头与主题切换 ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "生成打卡快照",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "9:16 Instagram Story 高清海报",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dark / Light 主题切换器
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTheme == StorySnapshotGenerator.StoryTheme.DARK,
                        onClick = { selectedTheme = StorySnapshotGenerator.StoryTheme.DARK },
                        label = { Text("暗黑", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = selectedTheme == StorySnapshotGenerator.StoryTheme.LIGHT,
                        onClick = { selectedTheme = StorySnapshotGenerator.StoryTheme.LIGHT },
                        label = { Text("纯白", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // ── 9:16 高清预览区 ─────────────────────────────────
            Box(
                modifier = Modifier
                    .width(225.dp)
                    .height(400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(
                        if (generatedBitmap != null) Modifier else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRendering || generatedBitmap == null) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "Story Snapshot Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ── 底部操作按钮（一键分享 & 保存相册） ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 保存相册按钮
                OutlinedButton(
                    onClick = {
                        val bmp = generatedBitmap ?: return@OutlinedButton
                        scope.launch {
                            isActionInProgress = true
                            val result = StorySnapshotGenerator.saveToGallery(context, bmp)
                            isActionInProgress = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "已保存高清海报至系统相册 ", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "保存失败：${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isRendering && !isActionInProgress,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder)
                ) {
                    Text("保存至相册", fontWeight = FontWeight.SemiBold)
                }

                // Instagram 渐变分享按钮
                Button(
                    onClick = {
                        val bmp = generatedBitmap ?: return@Button
                        scope.launch {
                            isActionInProgress = true
                            val cacheResult = StorySnapshotGenerator.saveBitmapToCache(context, bmp)
                            isActionInProgress = false
                            if (cacheResult.isSuccess) {
                                val uri = cacheResult.getOrThrow()
                                StorySnapshotGenerator.shareStory(context, uri, preferInstagram = true)
                            } else {
                                Toast.makeText(context, "创建分享文件失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isRendering && !isActionInProgress,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .background(
                            Brush.linearGradient(InsGradientColors),
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("分享至 Story ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
