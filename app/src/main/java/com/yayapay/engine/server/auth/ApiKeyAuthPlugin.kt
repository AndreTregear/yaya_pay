package com.yayapay.engine.server.auth

import com.yayapay.engine.data.local.db.ApiKeyDao
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyAuth @Inject constructor(
    private val apiKeyDao: ApiKeyDao
) {
    suspend fun validate(bearerToken: String): Boolean {
        val hash = hashKey(bearerToken)
        val key = apiKeyDao.getByHash(hash) ?: return false
        if (!key.active) return false
        apiKeyDao.updateLastUsed(key.id)
        return true
    }

    companion object {
        fun hashKey(key: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(key.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
