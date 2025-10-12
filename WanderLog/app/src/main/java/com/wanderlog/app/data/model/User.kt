package com.wanderlog.app.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val displayName: String = username, // 显示名称，默认为用户名
    val password: String, // 实际应用中应该加密存储
    val email: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)