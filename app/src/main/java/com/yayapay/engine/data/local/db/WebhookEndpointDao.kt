package com.yayapay.engine.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yayapay.engine.data.model.WebhookEndpointEntity

@Dao
interface WebhookEndpointDao {

    @Insert
    suspend fun insert(endpoint: WebhookEndpointEntity)

    @Query("SELECT * FROM webhook_endpoints WHERE id = :id")
    suspend fun getById(id: String): WebhookEndpointEntity?

    @Query("SELECT * FROM webhook_endpoints WHERE active = 1")
    suspend fun getActiveEndpoints(): List<WebhookEndpointEntity>

    @Query("SELECT * FROM webhook_endpoints ORDER BY createdAt DESC")
    suspend fun getAll(): List<WebhookEndpointEntity>

    @Query("UPDATE webhook_endpoints SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("DELETE FROM webhook_endpoints WHERE id = :id")
    suspend fun delete(id: String)
}
