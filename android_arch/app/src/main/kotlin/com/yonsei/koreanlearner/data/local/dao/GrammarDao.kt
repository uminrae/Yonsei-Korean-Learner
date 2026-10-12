package com.yonsei.koreanlearner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import kotlinx.coroutines.flow.Flow

/** 语法 GrammarEntity 的 DAO 接口 */
@Dao
interface GrammarDao {

    // ──────────────────────────────────────────────────────────
    // 写操作
    // ──────────────────────────────────────────────────────────

    /** 批量插入种子语法（首次启动时使用） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grammars: List<GrammarEntity>)

    // ──────────────────────────────────────────────────────────
    // 新语法查询（TODAY'S NEW GRAMMARS）
    // ──────────────────────────────────────────────────────────

    /**
     * 获取指定册数+课次的**未学新语法**。
     *
     * 每课语法点通常 4~10 条，limit 建议 4，
     * 避免单次学习负荷过重。
     */
    @Query("""
        SELECT g.* FROM grammars g
        LEFT JOIN review_records rr
            ON rr.item_id = g.id AND rr.item_type = 'GRAMMAR'
        WHERE g.book   = :book
          AND g.lesson = :lesson
          AND rr.id IS NULL
        ORDER BY g.sub_lesson ASC
        LIMIT :limit
    """)
    fun getNewGrammarsByBookAndLesson(
        book: Int,
        lesson: Int,
        limit: Int = 4
    ): Flow<List<GrammarEntity>>

    // ──────────────────────────────────────────────────────────
    // 复习队列查询（DUE REVIEW）
    // ──────────────────────────────────────────────────────────

    /**
     * 今日到期复习语法列表。
     *
     * 利用 review_records.next_review_timestamp 索引高效范围扫描，
     * 过滤 repetition_level < 7（已毕业不再复习）。
     *
     * @param nowMs 当前时间戳（毫秒）
     */
    @Query("""
        SELECT g.* FROM grammars g
        INNER JOIN review_records rr
            ON rr.item_id = g.id AND rr.item_type = 'GRAMMAR'
        WHERE rr.next_review_timestamp <= :nowMs
          AND rr.repetition_level < 7
        ORDER BY rr.next_review_timestamp ASC
    """)
    fun getDueGrammarsForReview(nowMs: Long): Flow<List<GrammarEntity>>

    // ──────────────────────────────────────────────────────────
    // 错题集
    // ──────────────────────────────────────────────────────────

    /** 获取所有错题集语法（按出错次数降序） */
    @Query("""
        SELECT g.* FROM grammars g
        INNER JOIN review_records rr
            ON rr.item_id = g.id AND rr.item_type = 'GRAMMAR'
        WHERE rr.is_error_item = 1
        ORDER BY rr.error_count DESC
    """)
    fun getErrorGrammars(): Flow<List<GrammarEntity>>

    // ──────────────────────────────────────────────────────────
    // 通用查询
    // ──────────────────────────────────────────────────────────

    /** 获取全量语法点（按册/课/子课顺序） */
    @Query("SELECT * FROM grammars ORDER BY book ASC, lesson ASC, sub_lesson ASC")
    fun getAllGrammars(): Flow<List<GrammarEntity>>

    /** 获取某册全部语法（供"语法手册"浏览界面使用） */
    @Query("""
        SELECT * FROM grammars
        WHERE book = :book
        ORDER BY lesson ASC, sub_lesson ASC
    """)
    fun getGrammarsByBook(book: Int): Flow<List<GrammarEntity>>

    /** 按功能分类获取语法列表（供"分类浏览"界面使用） */
    @Query("""
        SELECT * FROM grammars
        WHERE category = :category
        ORDER BY book ASC, lesson ASC
    """)
    fun getGrammarsByCategory(category: String): Flow<List<GrammarEntity>>

    /** 全局搜索语法（按 grammar_pattern 模糊匹配） */
    @Query("""
        SELECT * FROM grammars
        WHERE grammar_pattern LIKE '%' || :query || '%'
           OR explanation     LIKE '%' || :query || '%'
        ORDER BY book ASC, lesson ASC
    """)
    fun searchGrammars(query: String): Flow<List<GrammarEntity>>

    // ──────────────────────────────────────────────────────────
    // 统计查询
    // ──────────────────────────────────────────────────────────

    /** 指定册数总语法点数量 */
    @Query("SELECT COUNT(*) FROM grammars WHERE book = :book")
    fun getTotalGrammarCount(book: Int): Flow<Int>

    /** 指定册数已毕业语法点数量 */
    @Query("""
        SELECT COUNT(*) FROM grammars g
        INNER JOIN review_records rr ON rr.item_id = g.id AND rr.item_type = 'GRAMMAR'
        WHERE g.book = :book AND rr.repetition_level >= 7
    """)
    fun getGraduatedGrammarCount(book: Int): Flow<Int>

    /** 按主键查语法点（用于复习队列构建时根据 itemId 拉取详情） */
    @Query("SELECT * FROM grammars WHERE id = :id LIMIT 1")
    suspend fun getGrammarById(id: Int): GrammarEntity?

    /** 分页查询语法点 */
    @Query("SELECT * FROM grammars WHERE (:book = 0 OR book = :book) ORDER BY book ASC, lesson ASC, sub_lesson ASC")
    fun getPagingGrammars(book: Int = 0): androidx.paging.PagingSource<Int, GrammarEntity>

    @Query("SELECT COUNT(*) FROM grammars")
    suspend fun getTotalCount(): Int
}
