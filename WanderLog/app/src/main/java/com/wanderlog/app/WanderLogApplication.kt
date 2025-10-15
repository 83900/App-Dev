package com.wanderlog.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WanderLogApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("WanderLogApplication", "Application starting...")
        
        try {
            // 检查应用数据目录
            val filesDir = filesDir
            Log.d("WanderLogApplication", "App files directory: ${filesDir.absolutePath}")
            Log.d("WanderLogApplication", "Files directory exists: ${filesDir.exists()}")
            Log.d("WanderLogApplication", "Files directory writable: ${filesDir.canWrite()}")
            
            // 确保数据目录存在
            if (!filesDir.exists()) {
                val created = filesDir.mkdirs()
                Log.d("WanderLogApplication", "Created files directory: $created")
            }
            
            Log.d("WanderLogApplication", "Application initialized successfully")
        } catch (e: Exception) {
            Log.e("WanderLogApplication", "Error during application initialization", e)
        }
    }
}