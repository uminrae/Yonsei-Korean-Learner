package com.yonsei.koreanlearner.data.local.dao

import androidx.room.*
import com.yonsei.koreanlearner.data.local.entity.IdiomEntity
import com.yonsei.koreanlearner.data.local.entity.IdiomType
import kotlinx.coroutines.flow.Flow

@Dao
interface IdiomDao {

    @Query("SELECT * FROM idioms ORDER BY id ASC")
    fun getAllIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE type = :type ORDER BY id ASC")
    fun getIdiomsByType(type: IdiomType): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE id = :id LIMIT 1")
    suspend fun getIdiomById(id: Int): IdiomEntity?

    @Query("""
        SELECT * FROM idioms
        WHERE korean LIKE '%' || :query || '%'
           OR metaphor_meaning LIKE '%' || :query || '%'
           OR hanja LIKE '%' || :query || '%'
        ORDER BY id ASC
    """)
    fun searchIdioms(query: String): Flow<List<IdiomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(idioms: List<IdiomEntity>)

    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun getCount(): Int
}
