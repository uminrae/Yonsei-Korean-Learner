package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 语法对比领域模型定义
 * ══════════════════════════════════════════════════════════════════
 */

/**
 * 限制条件标签模型
 */
data class GrammarRestrictionTag(
    val label       : String,       // 如 "禁共动/命令句", "可接命令句", "仅消极结果"
    val isNegative  : Boolean = true // true 为禁止/受限（柔和红/橙），false 为允许/推荐（青绿）
)

/**
 * 正反例句对比对
 */
data class ComparisonExamplePair(
    val correctSentence   : String, // 正确例句（如 "비가 오니까 우산을 쓰세요. (O)"）
    val incorrectSentence : String? = null, // 常见误用病句（如 "비가 오기 때문에 우산을 쓰세요. (X)"）
    val explanation       : String  // 辨析解析说明
)

/**
 * 单个语法对比项数据模型
 */
data class GrammarComparisonItem(
    val pattern             : String,                   // 语法 Pattern（如 "-기 때문에"）
    val structuralFormula   : String,                   // 结构公式（如 "V/A + 기 때문에 | N + (이)기 때문에"）
    val contextDescription  : String,                   // 主客观语境与语义说明
    val nuanceDifference    : String,                   // 语感差异与语气强度解析
    val strengthLevel       : Int = 3,                  // 语气强度等级 1~5
    val restrictionTags     : List<GrammarRestrictionTag> = emptyList(),
    val examplePairs        : List<ComparisonExamplePair> = emptyList()
)

/**
 * 对比组模型（一组易混淆语法）
 */
data class GrammarComparisonGroup(
    val groupTitle  : String,                       // 对比主题（如 "原因理由语法大辨析"）
    val category    : String,                       // 分类（如 "原因/理由"）
    val items       : List<GrammarComparisonItem>   // 对比的 2~5 个语法点
)

/**
 * ══════════════════════════════════════════════════════════════════
 * 易混淆语法对比面板组件 (GrammarComparisonCard)
 * ══════════════════════════════════════════════════════════════════
 *
 * 视觉特性：
 * 1. 顶部语法 Pattern 切换 Tab（水平滑动/切换）。
 * 2. 核心结构公式卡片 + 语气强度星级。
 * 3. 限制条件 Chip 标签组（红/橙/绿语义提示）。
 * 4. 正误对比句（(O) 绿色正句 vs (X) 红色病句对比）。
 *
 * @param comparisonGroup 易混淆语法组数据
 * @param onSpeak 发音回调
 */
@Composable
fun GrammarComparisonCard(
    comparisonGroup: GrammarComparisonGroup,
    onSpeak: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (comparisonGroup.items.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentItem = comparisonGroup.items.getOrElse(selectedIndex) { comparisonGroup.items[0] }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── 顶部组标题与分类 ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = comparisonGroup.groupTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "分类: ${comparisonGroup.category} · 横向切换对比",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${comparisonGroup.items.size} 语法对比",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── 语法 Pattern 水平切换 Tab 行 ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comparisonGroup.items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedIndex = index },
                        label    = {
                            Text(
                                text = item.pattern,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        shape    = RoundedCornerShape(10.dp),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 核心内容展示区 ───────────────────────────────────
            GrammarDetailComparisonBody(
                item = currentItem,
                onSpeak = onSpeak
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 语法对比内容体
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarDetailComparisonBody(
    item: GrammarComparisonItem,
    onSpeak: ((String) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // ── 1. 接续公式框 ────────────────────────────────────
        Surface(
            color  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            shape  = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " 结构公式",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "语气强弱: ${"★".repeat(item.strengthLevel)}${"☆".repeat(5 - item.strengthLevel)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.structuralFormula,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // ── 2. 限制条件标签组 (Chips) ─────────────────────────
        if (item.restrictionTags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "限制条件 & 使用禁忌",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.restrictionTags.forEach { tag ->
                        RestrictionChip(tag)
                    }
                }
            }
        }

        // ── 3. 主客观语境与语义说明 ──────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "语境与语感解析",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.contextDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
            if (item.nuanceDifference.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = " ${item.nuanceDifference}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── 4. 正反例句组对比 ─────────────────────────────────
        if (item.examplePairs.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "对照例句与辨析",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.examplePairs.forEach { pair ->
                    ExamplePairCard(pair = pair, onSpeak = onSpeak)
                }
            }
        }
    }
}

// ── 限制条件 Chip ────────────────────────────────────────────────

@Composable
private fun RestrictionChip(tag: GrammarRestrictionTag) {
    val bgColor = if (tag.isNegative) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f)
    val textColor = if (tag.isNegative) Color(0xFFDC2626) else Color(0xFF059669)
    val icon = if (tag.isNegative) "⚠️ " else "✅ "

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "$icon${tag.label}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

// ── 正反例句卡片 ─────────────────────────────────────────────────

@Composable
private fun ExamplePairCard(
    pair: ComparisonExamplePair,
    onSpeak: ((String) -> Unit)?
) {
    Surface(
        color  = MaterialTheme.colorScheme.background,
        shape  = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 正确例句 (O)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = pair.correctSentence,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (onSpeak != null) {
                    Text(
                        text = "",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onSpeak(pair.correctSentence.substringBefore("(").trim()) }
                            .padding(4.dp)
                    )
                }
            }

            // 错误病句 (X)
            if (pair.incorrectSentence != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✗", color = Color(0xFFEF4444), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = pair.incorrectSentence,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444).copy(alpha = 0.85f)
                    )
                }
            }

            // 辨析解析
            Text(
                text = "解析: ${pair.explanation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 内置延世高频易混淆语法库（开箱即用数据源）
// ══════════════════════════════════════════════════════════════════

object YonseiGrammarComparisonPresets {

    /** 原因/理由 5 大语法大辨析 */
    val REASON_CAUSE_GROUP = GrammarComparisonGroup(
        groupTitle = "原因/理由 5 大易混淆语法",
        category   = "原因与依据",
        items      = listOf(
            GrammarComparisonItem(
                pattern = "-기 때문에",
                structuralFormula = "动词/形容词 + 기 때문에 | 名词 + (이)기 때문에",
                contextDescription = "表示强烈的、客观的因果关系，强调理由是确凿的事实或明确的原委。",
                nuanceDifference = "客观因果最强；禁止用于命令句和共动句；前面可以带过去时 -았/었-。",
                strengthLevel = 5,
                restrictionTags = listOf(
                    GrammarRestrictionTag("禁命令句 (-(으)십시오)", isNegative = true),
                    GrammarRestrictionTag("禁共动句 (-(으)ㅂ시다)", isNegative = true),
                    GrammarRestrictionTag("可接过去时 -았/었-", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "비가 오기 때문에 길이 미끄럽습니다.",
                        incorrectSentence = "비가 오기 때문에 우산을 쓰십시오. (X)",
                        explanation = "-기 때문에 后句不能接命令句，命令句应改用 -(으)니까。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-아/어/여서",
                structuralFormula = "动词/形容词词干 + -아/어/여서 | 名词 + (이)어서",
                contextDescription = "表示自然产生的直接原因或先后顺序，语气温和自然，常用于日常寒暄道歉。",
                nuanceDifference = "最日常；前面绝对禁止接时态标记 -았/었-；禁命令句与共动句。",
                strengthLevel = 2,
                restrictionTags = listOf(
                    GrammarRestrictionTag("严禁接时态 -았/었-", isNegative = true),
                    GrammarRestrictionTag("禁命令/共动句", isNegative = true),
                    GrammarRestrictionTag("常用于道歉/感谢寒暄", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "늦어서 죄송합니다.",
                        incorrectSentence = "늦었어서 죄송합니다. (X)",
                        explanation = "-아/어서 前面不能接过去时 -았/었-，时态由句末谓词决定。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-(으)니까",
                structuralFormula = "动词/形容词词干 + -(으)니까 | 名词 + (이)니까",
                contextDescription = "表示说话人主观的理由、判断或新发现的事实依据，核心用于支撑后句的提议或指令。",
                nuanceDifference = "唯一可自由接命令句与共动句的原因语法；语气主观性强。",
                strengthLevel = 4,
                restrictionTags = listOf(
                    GrammarRestrictionTag("完美支持命令句 (-(으)세요)", isNegative = false),
                    GrammarRestrictionTag("完美支持共动句 (-(으)ㅂ시다)", isNegative = false),
                    GrammarRestrictionTag("常用于主观建议", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "날씨가 추우니까 따뜻하게 입으세요. (O)",
                        incorrectSentence = "날씨가 춥기 때문에 따뜻하게 입으세요. (X)",
                        explanation = "后句为命令/劝诱句 (-으세요) 时，前句必须用 -(으)니까。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-느라고",
                structuralFormula = "动词词干 + -느라고",
                contextDescription = "表示为了进行前项动作而付出精力/时间，直接导致了后项不良或消极的结果。",
                nuanceDifference = "前后主语必须一致；后句必须是消极、否定或遗憾的结果；前句只能接动词。",
                strengthLevel = 3,
                restrictionTags = listOf(
                    GrammarRestrictionTag("前后主语必须一致", isNegative = true),
                    GrammarRestrictionTag("后句必须是消极/负面结果", isNegative = true),
                    GrammarRestrictionTag("仅限动词", isNegative = true)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "어제 시험공부를 하느라고 잠을 못 잤어요. (O)",
                        incorrectSentence = "어제 시험공부를 하느라고 시험에 합격했어요. (X)",
                        explanation = "-느라고 后句必须是负面或消极结果（如没睡好、没去成），不能接合格等正面结果。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-는 바람에",
                structuralFormula = "动词词干 + -는 바람에",
                contextDescription = "表示突然发生不可预料的外部突发事件，作为消极原因导致了后项不良结果。",
                nuanceDifference = "前项通常为非故意的突发外部状况；后项必须为消极后果；禁接命令句。",
                strengthLevel = 4,
                restrictionTags = listOf(
                    GrammarRestrictionTag("必须为突发不可抗力", isNegative = true),
                    GrammarRestrictionTag("后句必须为消极/不良结果", isNegative = true),
                    GrammarRestrictionTag("禁命令/共动句", isNegative = true)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "갑자기 비가 오는 바람에 옷이 다 젖었어요. (O)",
                        incorrectSentence = "열심히 공부하는 바람에 1등을 했어요. (X)",
                        explanation = "-는 바람에 只能用于突发负面影响，不能接主动意图或积极成就。"
                    )
                )
            )
        )
    )

    /** 转折让步 3 大易混淆语法 */
    val CONTRAST_GROUP = GrammarComparisonGroup(
        groupTitle = "转折让步 3 大核心语法辨析",
        category   = "转折与让步",
        items      = listOf(
            GrammarComparisonItem(
                pattern = "-지만",
                structuralFormula = "动词/形容词 + -지만",
                contextDescription = "标准的强转折连词，前后两项事实直接对立，前后分句地位平等。",
                nuanceDifference = "通用转折；前后事实对立清晰；前面可接时态。",
                strengthLevel = 4,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "한국어는 어렵지만 재미있습니다.",
                        explanation = "直接对立两个属性（难但有趣）。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-는데/(으)ㄴ데",
                structuralFormula = "动词 + 는데 | 形容词 + -(으)ㄴ데 | 名词 + 인데",
                contextDescription = "提供背景说明、轻微转折或引出下文的铺垫连词，语气委婉柔和。",
                nuanceDifference = "重点在引出后句；转折语气弱于 -지만；常用于提出请求的前置铺垫。",
                strengthLevel = 2,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "지금 밖에 비가 오는데 우산이 있어요?",
                        explanation = "提供背景（外面下雨）并委婉询问下文。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-기는 하지만",
                structuralFormula = "动词/形容词 + -기는 하지만",
                contextDescription = "先充分承认认可前项事实，随后话锋一转，着重强调后项与前项相反的真实情况。",
                nuanceDifference = "强调让步；相当于汉语的“好是好，但是...”，转折对比意图极其鲜明。",
                strengthLevel = 5,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "그 옷이 예쁘기는 하지만 너무 비싸요.",
                        explanation = "承认好看，但核心在强调太贵无法购买。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-(으)ㄴ/는 반면에",
                structuralFormula = "动词 + 는 반면에 | 形容词 + -(으)ㄴ 반면에",
                contextDescription = "鲜明对比同一事物的两面性（优缺点或正反面）。",
                nuanceDifference = "专门用于两面对比；“一方面...另一方面...”。",
                strengthLevel = 4,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "이 집은 지하철역에서 가까운 반면에 집세가 비싸요.",
                        explanation = "对比优缺点（交通便利 vs 房租昂贵）。"
                    )
                )
            )
        )
    )

    /** 时序顺接 4 大核心语法辨析 */
    val SEQUENCE_TIME_GROUP = GrammarComparisonGroup(
        groupTitle = "时序顺接 4 大核心语法辨析",
        category   = "时间与顺接",
        items      = listOf(
            GrammarComparisonItem(
                pattern = "-자마자",
                structuralFormula = "动词词干 + -자마자",
                contextDescription = "前项动作一发生，后项动作立即紧接着发生（几乎无时间间隔）。",
                nuanceDifference = "紧迫感强；前项禁止接过去时 -았/었-；可接各种句式。",
                strengthLevel = 4,
                restrictionTags = listOf(
                    GrammarRestrictionTag("前项严禁过去时 -았/었-", isNegative = true),
                    GrammarRestrictionTag("无前后主语限制", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "집에 도착하자마자 손을 씻었어요.",
                        explanation = "强调回到家瞬间立即洗手。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-는 대로",
                structuralFormula = "动词词干 + -는 대로",
                contextDescription = "前项动作一旦完成或发生，立即着手进行后项（常用于未来计划或嘱咐）。",
                nuanceDifference = "常用于未来尚未发生的事情（一...就...）；后句常接命令或承诺。",
                strengthLevel = 3,
                restrictionTags = listOf(
                    GrammarRestrictionTag("常用于未来计划 / 承诺", isNegative = false),
                    GrammarRestrictionTag("禁接过去时 -았/었-", isNegative = true)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "회의가 끝나는 대로 바로 연락드리겠습니다.",
                        explanation = "一开完会马上联系您（未来承诺）。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-기가 무섭게",
                structuralFormula = "动词词干 + -기가 무섭게",
                contextDescription = "极度强调前后两个动作衔接之快，带有夸张口吻（“刚一...就立刻...”）。",
                nuanceDifference = "语气强度最高；具有生动夸张色彩；多用于既成事实叙述。",
                strengthLevel = 5,
                restrictionTags = listOf(
                    GrammarRestrictionTag("极高语气强度 (生动夸张)", isNegative = false),
                    GrammarRestrictionTag("多用于过去事实叙述", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "수업이 끝나기가 무섭게 학생들이 교실 밖으로 뛰어나갔어요.",
                        explanation = "下课铃刚响，学生们就如脱缰野马冲出教室。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-고 나서",
                structuralFormula = "动词词干 + -고 나서",
                contextDescription = "明确强调前项动作完全终结之后，再展开下一个动作（侧重前后时序先后阶段）。",
                nuanceDifference = "侧重阶段先后；客观阐述动作次序，无瞬间紧迫感。",
                strengthLevel = 2,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "식사를 하고 나서 약을 드세요.",
                        explanation = "吃完饭之后再吃药（时序清晰）。"
                    )
                )
            )
        )
    )

    /** 递进添加 3 大核心语法辨析 */
    val ADDITION_INCLUSION_GROUP = GrammarComparisonGroup(
        groupTitle = "递进添加 3 大核心语法辨析",
        category   = "递进与添加",
        items      = listOf(
            GrammarComparisonItem(
                pattern = "-(으)ㄹ 뿐만 아니라",
                structuralFormula = "动词/形容词 + -(으)ㄹ 뿐만 아니라 | 名词 + 뿐만 아니라",
                contextDescription = "不仅包含前项事实，后项更有进一步的叠加或延展（不仅...而且...）。",
                nuanceDifference = "通用性最高；前后两项在感情色彩上必须一致（同为正面或同为负面）。",
                strengthLevel = 4,
                restrictionTags = listOf(
                    GrammarRestrictionTag("前后褒贬色彩必须一致", isNegative = true),
                    GrammarRestrictionTag("支持名词/形容词/动词", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "그 사람은 친절할 뿐만 아니라 일도 잘해요.",
                        explanation = "不仅亲切，而且工作也出色（双正面递进）。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-는 데다가",
                structuralFormula = "动词 + 는 데다가 | 形容词 + -(으)ㄴ 데다가",
                contextDescription = "在前项事实的基础上，又加上了后项具有相同性质的情况（雪上加霜或锦上添花）。",
                nuanceDifference = "侧重“添砖加瓦 / 额外累加”；前后主语通常一致；前后分句性质必须相同。",
                strengthLevel = 4,
                restrictionTags = listOf(
                    GrammarRestrictionTag("锦上添花 or 雪上加霜", isNegative = false),
                    GrammarRestrictionTag("前后性质必须同向", isNegative = true)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "비가 오는 데다가 바람까지 세게 불어요.",
                        explanation = "不仅下雨，再加上刮大风（双负面叠加）。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-거니와",
                structuralFormula = "动词/形容词 + -거니와",
                contextDescription = "书面语与高级正式语体，认可前项事实的同时，重点引出后项更重要的情况。",
                nuanceDifference = "极强书面色彩；常用于演讲、论文与新闻评论。",
                strengthLevel = 5,
                restrictionTags = listOf(
                    GrammarRestrictionTag("TOPIK 高级书面专用", isNegative = false)
                ),
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "그는 학식이 풍부하거니와 인품 또한 훌륭하다.",
                        explanation = "他不仅学识渊博，人品也极其高尚（典雅高级书面语）。"
                    )
                )
            )
        )
    )

    /** 间接引语（간접화법）口语缩略形式全集 */
    val INDIRECT_SPEECH_GROUP = GrammarComparisonGroup(
        groupTitle = "间接引语（间接话法）缩略公式大矩阵",
        category   = "引述与传达",
        items      = listOf(
            GrammarComparisonItem(
                pattern = "-대(요) (陈述句引述)",
                structuralFormula = "动词: -ㄴ/는대요 | 形容词: -대요 | 名词: -(이)래요",
                contextDescription = "转述听到或了解到的客观陈述事实（原句为 -ㄴ/는다고 해요 的口语极速缩略）。",
                nuanceDifference = "口语及影视剧最高频；过去时用 -았/었대요，将来时用 -(으)ㄹ 거래요。",
                strengthLevel = 3,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "민우가 오늘 아파서 못 온대요. (原句: 온다고 해요)",
                        explanation = "敏宇说他今天生病不能来。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-냬(요) (疑问句引述)",
                structuralFormula = "动词/形容词: -(으)냬요 | 名词: -(이)냬요",
                contextDescription = "转述第三方的询问或疑问句（原句为 -냐고 해요 的口语缩略）。",
                nuanceDifference = "专门用于转述问句；“某某问...”。",
                strengthLevel = 3,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "선생님이 숙제를 다 했냬요. (原句: 했냐고 해요)",
                        explanation = "老师问作业做完了没有。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-재(요) (共动句引述)",
                structuralFormula = "动词词干 + -재요",
                contextDescription = "转述第三方的共同提议或邀请（原句为 -자고 해요 的口语缩略）。",
                nuanceDifference = "专门用于转述提议（“一起去/一起做”）。",
                strengthLevel = 3,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "친구가 이번 주말에 영화 보러 가재요. (原句: 가자고 해요)",
                        explanation = "朋友提议这周末一起去看电影。"
                    )
                )
            ),
            GrammarComparisonItem(
                pattern = "-래(요) (命令句引述)",
                structuralFormula = "动词词干 + -(으)래요",
                contextDescription = "转述第三方的要求、指令或嘱咐（原句为 -(으)라고 해요 的口语缩略）。",
                nuanceDifference = "专门用于转述命令与嘱托；“某某让你/叫你...”。",
                strengthLevel = 4,
                examplePairs = listOf(
                    ComparisonExamplePair(
                        correctSentence = "엄마가 밥 먹고 바로 양치하래요. (原句: 하라고 해요)",
                        explanation = "妈妈叫你吃完饭立刻刷牙。"
                    )
                )
            )
        )
    )

    val ALL_PRESET_GROUPS = listOf(
        REASON_CAUSE_GROUP,
        CONTRAST_GROUP,
        SEQUENCE_TIME_GROUP,
        ADDITION_INCLUSION_GROUP,
        INDIRECT_SPEECH_GROUP
    )
}
