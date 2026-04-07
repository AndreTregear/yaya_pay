package com.yayapay.engine.server.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimiter @Inject constructor() {

    private val windows = mutableMapOf<String, MutableList<Long>>()
    private val maxRequestsPerMinute = 120

    @Synchronized
    fun allowRequest(apiKey: String): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = now - 60_000L

        val requests = windows.getOrPut(apiKey) { mutableListOf() }
        requests.removeAll { it < windowStart }

        if (requests.size >= maxRequestsPerMinute) return false

        requests.add(now)
        return true
    }
}
