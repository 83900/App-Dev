package com.example.wonderlog

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wonderlog.data.UserItem
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.graphics.ImageDecoder

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var ivAvatar: ImageView
    private lateinit var btnSelectAvatar: Button
    private lateinit var etUsername: EditText
    private lateinit var etNickname: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    
    private var avatarUri: Uri? = null
    private var avatarFilePath: String = "" // 用于存储头像文件路径
    
    // 请求码
    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSION_REQUEST_CODE = 2
    
    // 头像存储目录
    private val AVATAR_DIR = "avatars"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // 初始化视图
        btnBack = findViewById(R.id.btn_back)
        ivAvatar = findViewById(R.id.iv_avatar)
        btnSelectAvatar = findViewById(R.id.btn_select_avatar)
        etUsername = findViewById(R.id.et_username)
        etNickname = findViewById(R.id.et_nickname)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
        
        // 设置默认头像
        ivAvatar.setImageResource(R.drawable.header)
        // 设置默认头像路径
        avatarFilePath = "default"
        
        // 设置返回按钮点击事件
        btnBack.setOnClickListener {
            finish()
        }
        
        // 设置头像选择按钮点击事件
        btnSelectAvatar.setOnClickListener {
            // 检查权限
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }
        
        // 设置注册按钮点击事件
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val nickname = etNickname.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            // 验证输入
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_username_password, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 执行注册逻辑
            register(username, password, nickname, avatarFilePath)
        }
        
        // 设置登录入口点击事件
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * 检查存储权限
     */
    private fun checkStoragePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上，使用READ_MEDIA_IMAGES权限
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 12及以下，使用READ_EXTERNAL_STORAGE权限
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 请求存储权限
     */
    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上，使用READ_MEDIA_IMAGES权限
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 12及以下，使用READ_EXTERNAL_STORAGE权限
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        // 明确请求权限
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * 权限请求结果处理
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，打开图片选择器
                openImagePicker()
            } else {
                // 权限拒绝，显示提示
                Toast.makeText(this, R.string.permission_storage_avatar, Toast.LENGTH_SHORT).show()
                
                // 只有当用户至少拒绝过一次权限后，shouldShowRequestPermissionRationale才会准确反映
                // 这里简化处理，不直接跳转到设置页面，避免错误引导
            }
        }
    }
    
    /**
     * 打开图片选择器
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    
    /**
     * 处理图片选择结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedUri = data.data!! // 非空断言，因为前面已经检查过非空
            
            try {
                // 处理并保存图片
                avatarFilePath = saveAvatarImage(selectedUri)
                if (avatarFilePath.isNotEmpty()) {
                    // 显示图片
                    ivAvatar.setImageURI(selectedUri)
                    Toast.makeText(this, R.string.avatar_selected, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_avatar_process, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 保存头像图片到文件
     */
    private fun saveAvatarImage(uri: Uri): String {
        return try {
            // 解码图片并进行缩放
            val bitmap = decodeSampledBitmapFromUri(uri, 200, 200)
            
            // 创建头像存储目录
            val avatarDir = File(getExternalFilesDir(null), AVATAR_DIR)
            if (!avatarDir.exists()) {
                avatarDir.mkdirs()
            }
            
            // 创建头像文件
            val avatarFile = File(avatarDir, "avatar_${UUID.randomUUID()}.png")
            
            // 保存图片
            FileOutputStream(avatarFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
            
            // 返回相对路径（用于存储在JSON中）
            return "${AVATAR_DIR}/${avatarFile.name}"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * 从Uri解码并缩放Bitmap
     */
    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 使用现代API ImageDecoder（Android P及以上）
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) {
                decoder, info, source ->
                // 设置解码选项，包括缩放
                val scale = Math.min(
                    info.size.width.toFloat() / reqWidth,
                    info.size.height.toFloat() / reqHeight
                )
                decoder.setTargetSize(
                    (info.size.width / scale).toInt(),
                    (info.size.height / scale).toInt()
                )
            }
            bitmap
        } else {
            // 兼容旧版本API
            BitmapFactory.Options().run {
                inJustDecodeBounds = true
                contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
                
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                
                contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
            } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }
    
    /**
     * 计算合适的采样率
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // 原始宽高
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            // 计算最大的inSampleSize，使得宽高都大于等于请求的宽高
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 执行注册逻辑
     */
    private fun register(username: String, password: String, nickname: String, avatarPath: String) {
        // 从文件中读取用户数据
        val allUsers = if (FileUtils.userDataFileExists(this)) {
            val jsonString = FileUtils.readUserData(this)
            jsonString?.let {
                JsonUtils.jsonStringToUserItemList(it)
            } ?: emptyList()
        } else {
            emptyList()
        }
        
        // 检查用户名是否已存在
        if (allUsers.any { it.username == username }) {
            Toast.makeText(this, R.string.error_username_exists, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建新用户
        val newUser = UserItem(
            id = "user_${UUID.randomUUID()}",
            username = username,
            password = password,
            nickname = nickname,
            avatar = avatarPath // 存储头像文件路径，而非Base64
        )
        
        // 添加新用户到列表
        val updatedUsers = allUsers + newUser
        
        // 保存更新后的用户数据到文件
        val jsonString = JsonUtils.userItemListToJsonString(updatedUsers)
        val saveSuccess = FileUtils.saveUserData(this, jsonString)
        
        if (saveSuccess) {
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
            
            // 注册成功后跳转到登录页面
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show()
        }
    }
}