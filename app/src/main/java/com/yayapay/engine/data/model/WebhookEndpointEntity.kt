package com.yayapay.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "webhook_endpoints",
    indices = [Index(value = ["active"])]
)
data class WebhookEndpointEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val secret: String,
    val enabledEvents: String,
    val description: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
