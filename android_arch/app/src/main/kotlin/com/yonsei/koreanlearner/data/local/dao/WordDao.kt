package com.yonsei.koreanlearner.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.WordSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世韩国语词汇数据访问接口 (WordDao)
 * ══════════════════════════════════════════════════════════════════
 */
@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT * FROM words WHERE book = :book ORDER BY lesson ASC, cumulative_index ASC")
    fun getWordsByBook(book: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE book = :book AND lesson = :lesson ORDER BY cumulative_index ASC")
    fun getWordsByBookAndLesson(book: Int, lesson: Int): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        LEFT JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book   = :book
          AND w.lesson = :lesson
          AND rr.id IS NULL
        ORDER BY w.cumulative_index ASC
        LIMIT :limit
    """)
    fun getNewWordsByBookAndLesson(
        book: Int,
        lesson: Int,
        limit: Int = 10
    ): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        LEFT JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book
          AND rr.id IS NULL
        ORDER BY w.lesson ASC, w.cumulative_index ASC
        LIMIT :limit
    """)
    fun getAllNewWordsByBook(book: Int, limit: Int = 20): Flow<List<WordEntity>>

    @Transaction
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE rr.next_review_timestamp <= :nowMs
          AND rr.repetition_level < 7
        ORDER BY rr.next_review_timestamp ASC
    """)
    fun getDueWordsForReview(nowMs: Long): Flow<List<WordWithRecord>>

    @Transaction
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records rr ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE rr.next_review_timestamp <= :nowMs AND rr.repetition_level < 7
        ORDER BY rr.next_review_timestamp ASC
        LIMIT :limit
    """)
    fun getDueReviewWords(nowMs: Long, limit: Int = 20): Flow<List<WordWithRecord>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE rr.is_error_item = 1
        ORDER BY rr.error_count DESC, rr.last_review_timestamp DESC
    """)
    fun getErrorWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE book = :book")
    fun getTotalWordCount(book: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE book = :book")
    fun getTotalWordsCount(book: Int): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT w.id) FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book
          AND rr.repetition_level >= 7
    """)
    fun getGraduatedWordCount(book: Int): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT w.id) FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book
          AND rr.repetition_level >= 7
    """)
    fun getGraduatedWordsCount(book: Int): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT w.id) FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book AND w.lesson = :lesson
          AND rr.repetition_level > 0
    """)
    fun getLearnedWordCount(book: Int, lesson: Int): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT w.id) FROM words w
        INNER JOIN review_records rr
            ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book
          AND rr.repetition_level > 0
    """)
    fun getLearnedWordCount(book: Int): Flow<Int>

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordEntity?

    @Query("""
        SELECT * FROM words
        WHERE (:book = 0 OR book = :book)
        ORDER BY book ASC, lesson ASC, cumulative_index ASC
    """)
    fun getPagingWords(book: Int = 0): PagingSource<Int, WordEntity>

    @Query("""
        SELECT * FROM words
        WHERE book = :book
        ORDER BY lesson ASC, cumulative_index ASC
    """)
    fun getPagingWordsByBook(book: Int): PagingSource<Int, WordEntity>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records rr ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE rr.is_error_item = 1
        ORDER BY rr.error_count DESC
    """)
    fun getPagingErrorWords(): PagingSource<Int, WordEntity>

    @Transaction
    @Query("""
        SELECT * FROM words
        WHERE book = :book AND lesson = :lesson
        ORDER BY cumulative_index ASC
    """)
    fun getWordsWithRecordsByLesson(book: Int, lesson: Int): Flow<List<WordWithRecord>>

    @Transaction
    @Query("""
        SELECT * FROM words
        WHERE book = :book
        ORDER BY lesson ASC, cumulative_index ASC
    """)
    fun getWordsWithRecordsByBook(book: Int): Flow<List<WordWithRecord>>

    @Query("""
        SELECT w.id, w.book, w.lesson, w.cumulative_index,
               w.korean, w.chinese, w.part_of_speech, w.level, w.irregular_type,
               snippet(words_fts, '<b>', '</b>', '...', -1, 30) AS matchSnippet
        FROM words w
        JOIN words_fts ON w.id = words_fts.rowid
        WHERE words_fts MATCH :query
        LIMIT :limit
    """)
    fun searchWordsFts(query: String, limit: Int = 30): Flow<List<WordSearchResult>>

    @Query("""
        SELECT * FROM words
        WHERE korean IN (:candidates)
        ORDER BY LENGTH(korean) DESC
        LIMIT :limit
    """)
    suspend fun lookupWordsByCandidates(candidates: List<String>, limit: Int = 5): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getTotalCount(): Int
}

data class WordWithRecord(
    @Embedded val word: WordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    val record: ReviewRecordEntity? = null
) {
    val repetitionLevel: Int get() = record?.repetitionLevel ?: 0
    val isGraduated: Boolean get() = (record?.repetitionLevel ?: 0) >= 7
    val heatLevel: Int get() = (record?.repetitionLevel ?: 0).coerceIn(0, 7)
}
