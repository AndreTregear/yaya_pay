package com.yayapay.engine.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yayapay.engine.data.model.ApiKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {

    @Insert
    suspend fun insert(apiKey: ApiKeyEntity)

    @Query("SELECT * FROM api_keys WHERE keyHash = :hash AND active = 1")
    suspend fun getByHash(hash: String): ApiKeyEntity?

    @Query("SELECT * FROM api_keys ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ApiKeyEntity>>

    @Query("UPDATE api_keys SET active = 0 WHERE id = :id")
    suspend fun revoke(id: String)

    @Query("UPDATE api_keys SET lastUsedAt = :now WHERE id = :id")
    suspend fun updateLastUsed(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM api_keys WHERE active = 1")
    suspend fun countActive(): Int
}
