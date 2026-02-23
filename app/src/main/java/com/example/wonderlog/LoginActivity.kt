package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wonderlog.data.UserItem
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.SharedPreferencesUtils
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // 初始化视图
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
        
        // 设置登录按钮点击事件
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            // 验证输入
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_username_password, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 执行登录逻辑
            login(username, password)
        }
        
        // 设置注册入口点击事件
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    
    /**
     * 执行登录逻辑
     */
    private fun login(username: String, password: String) {
        // 从文件中读取用户数据
        val allUsers = if (FileUtils.userDataFileExists(this)) {
            val jsonString = FileUtils.readUserData(this)
            jsonString?.let {
                JsonUtils.jsonStringToUserItemList(it)
            } ?: emptyList()
        } else {
            emptyList()
        }
        
        // 查找匹配的用户
        val matchedUser = allUsers.find { it.username == username && it.password == password }
        
        if (matchedUser != null) {
            // 登录成功
            SharedPreferencesUtils.saveLoginStatus(this, true, matchedUser.id, matchedUser.username)
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
            
            // 跳转到主页面
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 登录失败
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show()
        }
    }
}