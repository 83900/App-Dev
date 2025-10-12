package com.wanderlog.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wanderlog.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usersFile = File(context.filesDir, "users.json")
    private val currentUserFile = File(context.filesDir, "current_user.json")
    private val gson = Gson()
    
    // 获取所有用户
    fun getAllUsers(): List<User> {
        return try {
            if (usersFile.exists()) {
                val json = usersFile.readText()
                val type = object : TypeToken<List<User>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // 保存用户
    fun saveUser(user: User): Boolean {
        return try {
            val users = getAllUsers().toMutableList()
            users.add(user)
            val json = gson.toJson(users)
            usersFile.writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 验证登录
    fun validateLogin(username: String, password: String): User? {
        return getAllUsers().find { 
            it.username == username && it.password == password 
        }
    }
    
    // 检查用户名是否存在
    fun isUsernameExists(username: String): Boolean {
        return getAllUsers().any { it.username == username }
    }
    
    // 保存当前登录用户
    fun saveCurrentUser(user: User) {
        try {
            val json = gson.toJson(user)
            currentUserFile.writeText(json)
        } catch (e: Exception) {
            // 处理错误
        }
    }
    
    // 获取当前登录用户
    fun getCurrentUser(): User? {
        return try {
            if (currentUserFile.exists()) {
                val json = currentUserFile.readText()
                gson.fromJson(json, User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // 登出
    fun logout() {
        if (currentUserFile.exists()) {
            currentUserFile.delete()
        }
    }
}