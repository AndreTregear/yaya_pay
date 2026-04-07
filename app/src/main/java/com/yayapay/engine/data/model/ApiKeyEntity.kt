package com.yayapay.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "api_keys",
    indices = [Index(value = ["keyHash"], unique = true)]
)
data class ApiKeyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val keyHash: String,
    val keyPrefix: String,
    val active: Boolean = true,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
