package com.yonsei.koreanlearner.ui.components

/**
 * ══════════════════════════════════════════════════════════════════
 * 120fps 性能约束实施规范（内部文档）
 * ══════════════════════════════════════════════════════════════════
 *
 * ## 1. @Immutable / @Stable 使用规则
 *
 * | 注解         | 适用场景                          | 效果                               |
 * |------------|-----------------------------------|------------------------------------|
 * | @Immutable | data class，所有属性为 val，创建后不变  | 编译器跳过所有参数的稳定性检查            |
 * | @Stable    | class/interface，equals()正确实现  | 编译器信任调用方保证稳定性               |
 *
 * 已标注的类：
 *   - [LessonStoryItem]    @Immutable ✅
 *   - [FlashcardData]      @Immutable ✅
 *   - [FlashcardCallbacks] @Stable   ✅
 *   - [NavDestination]     @Immutable ✅
 *   - [ExtendedColors]     @Immutable ✅（在 Theme.kt 中）
 *
 * ## 2. LazyColumn / LazyRow 性能约束
 *
 * 每个 items() / item() 调用必须配置：
 * ```kotlin
 * items(
 *     items       = list,
 *     key         = { it.id },          // ← 稳定、唯一、可 hash
 *     contentType = { "item_type" }     // ← 相同类型复用 Composition
 * ) { item -> ... }
 * ```
 *
 * ✅ 已实现：StoryProgressRow、HomeScreen Feed、FlashcardDeck
 *
 * ## 3. graphicsLayer 使用原则
 *
 * graphicsLayer {} 内的属性修改在**渲染线程（Render Thread）**执行，
 * 完全绕过主线程的 Measure 和 Layout 阶段。
 *
 * 可安全放入 graphicsLayer 的属性：
 *   translationX / translationY / translationZ
 *   scaleX / scaleY
 *   rotationX / rotationY / rotationZ
 *   alpha
 *   shadowElevation / ambientShadowColor
 *
 * 禁止放入 graphicsLayer 的操作：
 *   ❌ 读取 Composable 状态（会触发重组）
 *   ❌ 修改外部 MutableState（会触发重组）
 *   ❌ 调用任何 Composable 函数
 *
 * ✅ 已实现：
 *   - FlashcardDeck 的拖拽 translationX / rotationZ / alpha
 *   - StoryItemNode 的 scaleX / scaleY 脉冲动画
 *   - NavItem 的图标 translationY / scaleX / scaleY 弹跳
 *   - CardFaceContainer 的翻牌 rotationY
 *
 * ## 4. Animatable vs animate*AsState 选择策略
 *
 * | 场景                    | 推荐                        | 原因                         |
 * |------------------------|----------------------------|------------------------------|
 * | 手势驱动（需中断/snap）    | Animatable                 | 支持 snapTo/stop/中断重播      |
 * | 状态切换（二值/多值）      | animateFloatAsState 等      | 声明式，代码简洁                |
 * | 无限循环（脉冲/呼吸灯）    | rememberInfiniteTransition | 内置重复逻辑，自动在不可见时暂停   |
 *
 * ✅ 已实现：
 *   - FlashcardDeck 手势：Animatable（支持中途取消、snap 重置）
 *   - NavItem 弹跳：animateFloatAsState + spring
 *   - StoryItemNode 脉冲：rememberInfiniteTransition（仅 isActive=true 时运行）
 *
 * ## 5. derivedStateOf 使用规则
 *
 * 当一个值依赖其他状态计算得出，且计算频率高于消费频率时使用：
 * ```kotlin
 * val overlayColor by remember {
 *     derivedStateOf {
 *         // 仅当 offsetX.value 跨越阈值时才通知 Recomposition
 *         val p = offsetX.value / 400f
 *         if (p > 0.05f) greenColor else ...
 *     }
 * }
 * ```
 * ✅ 已实现：FlashcardDeck overlayColor / HomeScreen topBarAlpha
 *
 * ## 6. 禁止在 Compose 中的高频路径（滚动/动画帧）做的事
 *
 * ❌ 在 item {} 内部创建 mutableStateOf / remember（每次重用时重建）
 * ❌ 在 measure 或 layout 回调中读写 State
 * ❌ 在 Canvas drawContent 中分配新对象（Path、Paint、Bitmap）
 *    → 应 remember { Path() } 在 Composable 级别缓存
 * ❌ 使用 Modifier.pointerInput 但没有正确处理 key（每次重组重建手势识别器）
 *    → 使用 pointerInput(card.id) 明确 key
 *
 * ## 7. 已知限制与 TODO
 *
 * - BottomNavBar 的图标目前使用 Emoji，生产环境需替换为 ImageVector + Painter
 *   以获得更好的渲染性能（Emoji 渲染走 Software Canvas）
 * - FlashcardDeck 双击心形使用 Path 绘制，每帧重新 drawHeart 但 Path 对象
 *   未缓存——性能敏感场景应 remember { Path() }
 * - StoryProgressRow 的 Brush.sweepGradient 每次 draw 时重建
 *   → 优化方案：将 Brush 提升到 remember {} 块并按颜色 key 缓存
 */
object PerformanceGuide
