package com.example.wonderlog.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences工具类，用于管理用户登录状态和相关数据
 */
object SharedPreferencesUtils {
    
    private const val PREF_NAME = "wonderlog_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_CURRENT_USER_ID = "current_user_id"
    private const val KEY_CURRENT_USERNAME = "current_username"
    
    /**
     * 获取SharedPreferences实例
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 保存登录状态
     */
    fun saveLoginStatus(context: Context, isLoggedIn: Boolean, userId: String? = null, username: String? = null) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        userId?.let { editor.putString(KEY_CURRENT_USER_ID, it) }
        username?.let { editor.putString(KEY_CURRENT_USERNAME, it) }
        editor.apply()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * 获取当前登录用户ID
     */
    fun getCurrentUserId(context: Context): String? {
        return getPreferences(context).getString(KEY_CURRENT_USER_ID, null)
    }
    
    /**
     * 获取当前登录用户名
     */
    fun getCurrentUsername(context: Context): String? {
        return getPreferences(context).getString(KEY_CURRENT_USERNAME, null)
    }
    
    /**
     * 清除登录状态
     */
    fun clearLoginStatus(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_CURRENT_USER_ID)
        editor.remove(KEY_CURRENT_USERNAME)
        editor.apply()
    }
}