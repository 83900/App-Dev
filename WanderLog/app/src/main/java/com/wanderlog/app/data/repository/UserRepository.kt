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
    
    init {
        // 初始化内置测试账户
        initializeBuiltInAccount()
    }
    
    private fun initializeBuiltInAccount() {
        val users = getAllUsers()
        val testAccountExists = users.any { it.username == "test" }
        
        if (!testAccountExists) {
            val testUser = User(
                id = "test_user_id", // 使用固定的ID
                username = "test",
                displayName = "测试用户",
                password = "root",
                createdAt = System.currentTimeMillis()
            )
            saveUser(testUser)
        }
    }
    
    // 获取所有用户
    fun getAllUsers(): List<User> {
        return try {
            if (usersFile.exists()) {
                val json = usersFile.readText()
                android.util.Log.d("UserRepository", "Reading users file: $json")
                val type = object : TypeToken<List<User>>() {}.type
                val users = gson.fromJson<List<User>>(json, type) ?: emptyList()
                android.util.Log.d("UserRepository", "Parsed ${users.size} users")
                users
            } else {
                android.util.Log.d("UserRepository", "Users file does not exist")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error reading users file", e)
            emptyList()
        }
    }
    
    // 保存用户
    fun saveUser(user: User): Boolean {
        return try {
            val users = getAllUsers().toMutableList()
            // 检查是否已存在相同用户名的用户，如果存在则更新，否则添加
            val existingIndex = users.indexOfFirst { it.username == user.username }
            if (existingIndex >= 0) {
                users[existingIndex] = user
            } else {
                users.add(user)
            }
            val json = gson.toJson(users)
            usersFile.writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 验证登录
    fun validateLogin(username: String, password: String): User? {
        val users = getAllUsers()
        android.util.Log.d("UserRepository", "Validating login for username: '$username', password: '$password'")
        android.util.Log.d("UserRepository", "Total users: ${users.size}")
        
        users.forEach { user ->
            android.util.Log.d("UserRepository", "User: ${user.username}, password: ${user.password}")
        }
        
        val user = users.find { 
            it.username == username && it.password == password 
        }
        
        android.util.Log.d("UserRepository", "Login result: ${if (user != null) "SUCCESS" else "FAILED"}")
        return user
    }
    
    // 检查用户名是否存在
    fun isUsernameExists(username: String): Boolean {
        val users = getAllUsers()
        val exists = users.any { it.username == username }
        android.util.Log.d("UserRepository", "Checking username '$username', exists: $exists, total users: ${users.size}")
        users.forEach { user ->
            android.util.Log.d("UserRepository", "User: ${user.username}")
        }
        return exists
    }
    
    // 保存当前登录用户
    fun saveCurrentUser(user: User): Boolean {
        return try {
            val json = gson.toJson(user)
            currentUserFile.writeText(json)
            android.util.Log.d("UserRepository", "Current user saved: ${user.username}")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error saving current user", e)
            false
        }
    }
    
    // 获取当前用户
    fun getCurrentUser(): User? {
        return try {
            if (currentUserFile.exists()) {
                val json = currentUserFile.readText()
                val user = gson.fromJson(json, User::class.java)
                android.util.Log.d("UserRepository", "Current user loaded: ${user?.username}")
                user
            } else {
                android.util.Log.d("UserRepository", "No current user file found")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error loading current user", e)
            null
        }
    }
    
    // 清除当前用户（登出）
    fun clearCurrentUser(): Boolean {
        return try {
            if (currentUserFile.exists()) {
                val deleted = currentUserFile.delete()
                android.util.Log.d("UserRepository", "Current user file deleted: $deleted")
                deleted
            } else {
                android.util.Log.d("UserRepository", "No current user file to delete")
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error clearing current user", e)
            false
        }
    }
    
    // 保持向后兼容性
    fun logout() {
        clearCurrentUser()
    }
}