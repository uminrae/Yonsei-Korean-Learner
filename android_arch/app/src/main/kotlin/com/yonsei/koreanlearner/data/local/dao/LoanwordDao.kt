package com.yonsei.koreanlearner.data.local.dao

import androidx.room.*
import com.yonsei.koreanlearner.data.local.entity.LoanwordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanwordDao {

    @Query("SELECT * FROM loanwords ORDER BY id ASC")
    fun getAllLoanwords(): Flow<List<LoanwordEntity>>

    @Query("SELECT * FROM loanwords WHERE category = :category ORDER BY id ASC")
    fun getLoanwordsByCategory(category: String): Flow<List<LoanwordEntity>>

    @Query("SELECT * FROM loanwords WHERE id = :id LIMIT 1")
    suspend fun getLoanwordById(id: Int): LoanwordEntity?

    @Query("""
        SELECT * FROM loanwords
        WHERE loanword LIKE '%' || :query || '%'
           OR foreign_origin LIKE '%' || :query || '%'
           OR native_equivalent LIKE '%' || :query || '%'
           OR chinese_meaning LIKE '%' || :query || '%'
        ORDER BY id ASC
    """)
    fun searchLoanwords(query: String): Flow<List<LoanwordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(loanwords: List<LoanwordEntity>)

    @Query("SELECT COUNT(*) FROM loanwords")
    suspend fun getCount(): Int
}
