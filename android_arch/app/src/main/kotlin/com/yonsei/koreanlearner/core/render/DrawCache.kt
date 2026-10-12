package com.yonsei.koreanlearner.core.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer

/**
 * ══════════════════════════════════════════════════════════════════
 * Compose 高性能渲染与绘制缓存引擎（Zero-Allocation Draw Cache）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心优化：
 * 1. 规避在 DrawScope 每帧（120fps = 8.3ms）创建 Path/Brush/Paint 等对象造成的内存抖动与 GC 卡顿。
 * 2. 3D 翻转背面剔除修饰符（Backface Culling），根治卡片翻转撕裂与透视穿透（Z-fighting）。
 * 3. 静态与动态 Path 形状对象复用池。
 */

/**
 * 3D 翻转背面剔除修饰符（硬件级加速与消除穿透撕裂）
 *
 * @param rotationY 当前绕 Y 轴旋转角度（0°~180°）
 * @param isBackFace 此层是否为卡片背面内容
 * @param cameraDistance 透视相机距离（建议 12f~20f * density）
 */
fun Modifier.backfaceCulling(
    rotationY: Float,
    isBackFace: Boolean,
    cameraDistance: Float = 16f
): Modifier = this.graphicsLayer {
    this.rotationY = rotationY
    this.cameraDistance = cameraDistance * density

    // 标准化角度至 0~360
    val normalizedAngle = ((rotationY % 360f) + 360f) % 360f
    val isFacingFront = normalizedAngle <= 90f || normalizedAngle >= 270f

    // 核心背面剔除：如果是正面且当前朝前，则可见；如果是背面且当前朝后，则可见；否则完全不提交给渲染管线
    val isVisible = if (isBackFace) !isFacingFront else isFacingFront
    this.alpha = if (isVisible) 1f else 0f

    // 背面内容镜像翻转回正向（避免文字镜像）
    if (isBackFace) {
        this.rotationY = rotationY - 180f
    }
}

/**
 * 预分配复用的 Heart Path（避免双击心形动画每帧 allocate Path 对象）
 */
class CachedHeartPath {
    val path = Path()
    private var lastCx = -1f
    private var lastCy = -1f
    private var lastR = -1f

    fun update(cx: Float, cy: Float, r: Float): Path {
        if (cx == lastCx && cy == lastCy && r == lastR) {
            return path
        }
        lastCx = cx
        lastCy = cy
        lastR = r

        path.reset()
        path.moveTo(cx, cy + r * 0.3f)
        path.cubicTo(cx - r * 1.2f, cy - r * 0.5f, cx - r * 2f, cy + r * 0.5f, cx, cy + r * 2f)
        path.cubicTo(cx + r * 2f, cy + r * 0.5f, cx + r * 1.2f, cy - r * 0.5f, cx, cy + r * 0.3f)
        path.close()
        return path
    }
}

@Composable
fun rememberCachedHeartPath(): CachedHeartPath {
    return remember { CachedHeartPath() }
}

/**
 * 缓存的渐变 Brush 生成器（按 size 与 colors 缓存，避免反复创建 SweepGradient Shader）
 */
class CachedSweepGradientBrush {
    private var lastCenter = Offset.Zero
    private var lastColors: List<Color> = emptyList()
    private var cachedBrush: Brush? = null

    fun getOrCreate(center: Offset, colors: List<Color>): Brush {
        if (cachedBrush != null && lastCenter == center && lastColors == colors) {
            return cachedBrush!!
        }
        lastCenter = center
        lastColors = colors
        val brush = Brush.sweepGradient(colors = colors, center = center)
        cachedBrush = brush
        return brush
    }
}

@Composable
fun rememberCachedSweepGradient(): CachedSweepGradientBrush {
    return remember { CachedSweepGradientBrush() }
}
