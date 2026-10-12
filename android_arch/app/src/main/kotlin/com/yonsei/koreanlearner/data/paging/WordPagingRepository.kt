package com.yonsei.koreanlearner.data.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════
 * 词汇 Paging 3 分页与预加载仓储（WordPagingRepository）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心优化策略：
 * 1. 【预加载缓冲区（Prefetch Distance）】：配置 prefetchDistance = 10，当用户滑动接近列表底部时，提前在后台协程异步加载下一页数据，彻底消除快速滑动到底部时的白屏/加载菊花停顿。
 * 2. 【占位符预留高度（Placeholders）】：enablePlaceholders = true，让 LazyColumn 预先感知总数据量，保持滚动条与列表总长度精确稳定，杜绝列表动态增高引起的布局跳动（Layout Shift）。
 * 3. 【内存最大保留上限（MaxSize）】：防止超长滑动加载数千个对象撑爆堆内存（Out Of Memory）。
 */
@Singleton
class WordPagingRepository @Inject constructor(
    private val wordDao: WordDao
) {
    companion object {
        private val PAGING_CONFIG = PagingConfig(
            pageSize         = 30,      // 单页数据量（延世背诵本约每课 20~30 词）
            prefetchDistance = 10,      // 提前 10 项触发预加载
            enablePlaceholders = true,  // 启用占位符，保持滚动条精确
            initialLoadSize  = 60,      // 首屏预加载 2 页数据，确保首屏填满无等待
            maxSize          = 300      // 内存中最多保留 300 项，超出自动释放远程页面
        )
    }

    /**
     * 获取指定册数的词汇分页流
     */
    fun getWordsByBookPaging(book: Int): Flow<PagingData<WordEntity>> {
        return Pager(
            config = PAGING_CONFIG,
            pagingSourceFactory = { wordDao.getPagingWordsByBook(book) }
        ).flow
    }

    /**
     * 获取错题集词汇分页流
     */
    fun getErrorWordsPaging(): Flow<PagingData<WordEntity>> {
        return Pager(
            config = PAGING_CONFIG,
            pagingSourceFactory = { wordDao.getPagingErrorWords() }
        ).flow
    }
}
