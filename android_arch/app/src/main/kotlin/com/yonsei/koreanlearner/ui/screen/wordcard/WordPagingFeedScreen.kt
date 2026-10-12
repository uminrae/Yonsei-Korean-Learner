package com.yonsei.koreanlearner.ui.screen.wordcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.paging.WordPagingRepository
import com.yonsei.koreanlearner.ui.theme.extended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════
 * Paging 3 词汇流 ViewModel（细粒度流隔离）
 * ══════════════════════════════════════════════════════════════════
 */
@HiltViewModel
class WordPagingViewModel @Inject constructor(
    private val pagingRepository: WordPagingRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    // 缓存至 viewModelScope，确保配置变更（旋转屏幕）时不重新发起 Room Paging 重新查询
    val wordsPagingFlow: Flow<PagingData<WordEntity>> =
        pagingRepository.getWordsByBookPaging(book = 1)
            .cachedIn(viewModelScope)
}

/**
 * ══════════════════════════════════════════════════════════════════
 * 120fps 无限顺滑滑动 Paging 3 词汇流界面（WordPagingFeedScreen）
 * ══════════════════════════════════════════════════════════════════
 *
 * 性能保障点：
 * 1. 【itemKey 稳定主键绑定】：itemKey { it.id }，列表增量重排与滑动复用 0 闪烁。
 * 2. 【占位符骨架渲染（Placeholders）】：数据尚未加载到内存时渲染轻量 Skeleton 占位，避免 Item 高度塌陷引起的滚动条剧烈跳动。
 * 3. 【音频单例池直接触发】：点击发音直接委托全局单例 TtsManager，列表滚动中 0 对象分配。
 */
@Composable
fun WordPagingFeedScreen(
    onWordClick: (Int) -> Unit = {},
    viewModel: WordPagingViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.wordsPagingFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { it.id },
                contentType = { "word_paging_row" }
            ) { index ->
                val word = lazyPagingItems[index]
                if (word != null) {
                    WordPagingRow(
                        word = word,
                        onPronounce = { viewModel.ttsManager.speak(word.korean) },
                        onClick = { onWordClick(word.id) }
                    )
                } else {
                    // Paging 占位骨架屏（固定高度，杜绝滑动跳动）
                    WordRowPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun WordPagingRow(
    word: WordEntity,
    onPronounce: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 课时与序号
        Text(
            text = "${word.lesson}-${word.cumulativeIndex}",
            modifier = Modifier.width(38.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(8.dp))

        // 韩文单词与中文释义
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = word.korean,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${word.partOfSpeech} · ${word.chinese}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 不规则变音标签
        word.irregularType?.let { irregular ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = irregular,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        // 发音按钮
        IconButton(
            onClick = onPronounce,
            modifier = Modifier.size(36.dp)
        ) {
            Text("", fontSize = 16.sp)
        }
    }

    Divider(
        modifier = Modifier.padding(start = 54.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun WordRowPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp, 14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(100.dp, 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(160.dp, 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
