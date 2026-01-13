package com.example.wonderlog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wonderlog.data.UserItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.SharedPreferencesUtils
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var tvTravelCount: TextView
    private lateinit var tvCityCount: TextView
    private lateinit var tvDiaryCount: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvAccount: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化TextView引用
        tvTravelCount = view.findViewById(R.id.tv_travel_count)
        tvCityCount = view.findViewById(R.id.tv_city_count)
        tvDiaryCount = view.findViewById(R.id.tv_diary_count)
        ivAvatar = view.findViewById(R.id.iv_avatar)
        tvUsername = view.findViewById(R.id.tv_user_nickname)
        tvAccount = view.findViewById(R.id.tv_user_account)
        
        // 初始化退出登录视图并设置点击事件
        val logoutLayout = view.findViewById<RelativeLayout>(R.id.rl_logout)
        logoutLayout.setOnClickListener {
            // 执行退出登录操作
            logout()
        }
        
        // 初始化修改账户信息视图并设置点击事件
        val modifyAccountLayout = view.findViewById<RelativeLayout>(R.id.rl_modify_account)
        modifyAccountLayout.setOnClickListener {
            // 跳转到修改账户信息页面
            val intent = Intent(activity, ModifyProfileActivity::class.java)
            startActivity(intent)
        }
        
        // 加载并显示用户信息
        loadUserInfo()
        
        // 更新统计数据
        updateStatistics()
    }
    
    private fun updateStatistics() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(requireContext()) ?: return
        
        // 读取旅行数据
        val travelJson = FileUtils.readTravelData(requireContext())
        val travelList = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it).filter { travelItem -> travelItem.userId == currentUserId }
        } ?: emptyList()
        val travelCount = travelList.size
        
        // 获取当前用户的所有旅行ID
        val userTravelIds = travelList.map { it.id }.toSet()
        
        // 读取日记数据，并统计属于当前用户旅行的日记数量
        val diaryJson = FileUtils.readDiaryData(requireContext())
        val diaryCount = diaryJson?.let {
            JsonUtils.jsonStringToDiaryItemList(it).filter { diaryItem -> 
                // 只统计属于当前用户旅行的日记
                userTravelIds.contains(diaryItem.travelId)
            }.size
        } ?: 0
        
        // 计算访问城市数量（从旅行数据中提取独特城市）
        val cityCount = travelList.flatMap { travelItem ->
            travelItem.locations.map { location ->
                location.split(",")[0].trim()
            }
        }.distinct().size
        
        // 更新TextView
        tvTravelCount.text = travelCount.toString()
        tvCityCount.text = cityCount.toString()
        tvDiaryCount.text = diaryCount.toString()
    }
    
    /**
     * 加载并显示当前登录用户的信息
     */
    private fun loadUserInfo() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(requireContext())
        if (currentUserId == null) {
            // 未登录，不显示用户信息
            return
        }
        
        // 读取所有用户数据
        val allUsersJson = FileUtils.readUserData(requireContext())
        val allUsers = allUsersJson?.let {
            JsonUtils.jsonStringToUserItemList(it)
        } ?: emptyList()
        
        // 查找当前登录用户
        val currentUser = allUsers.find { it.id == currentUserId }
        if (currentUser != null) {
            // 显示用户信息
            tvUsername.text = currentUser.nickname ?: "旅行者"
            tvAccount.text = currentUser.username
            
            // 显示头像
            if (!currentUser.avatar.isNullOrEmpty()) {
                try {
                    if (currentUser.avatar == "default") {
                        // 使用默认头像
                        ivAvatar.setImageResource(R.drawable.header)
                    } else {
                        // 从文件路径加载头像
                        val avatarFile = File(requireActivity().getExternalFilesDir(null), currentUser.avatar)
                        if (avatarFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
                            ivAvatar.setImageBitmap(bitmap)
                        } else {
                            // 文件不存在，使用默认头像
                            ivAvatar.setImageResource(R.drawable.header)
                        }
                    }
                    ivAvatar.setBackgroundResource(0) // 移除默认背景
                } catch (e: Exception) {
                    // 头像加载失败，使用自定义默认头像
                    e.printStackTrace()
                    ivAvatar.setImageResource(R.drawable.header)
                    ivAvatar.setBackgroundResource(0) // 移除默认背景
                }
            } else {
                // 没有头像，使用自定义默认头像
                ivAvatar.setImageResource(R.drawable.header)
                ivAvatar.setBackgroundResource(0) // 移除默认背景
            }
        }
    }
    
    /**
     * 执行退出登录操作
     */
    private fun logout() {
        // 清除登录状态
        SharedPreferencesUtils.clearLoginStatus(requireContext())
        
        // 显示退出登录提示
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
        
        // 跳转到登录页面
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        
        // 关闭当前的MainActivity
        activity?.finish()
    }
    
    /**
     * 当Fragment重新进入前台时调用
     */
    override fun onResume() {
        super.onResume()
        // 重新加载用户信息，确保修改后的数据能及时显示
        loadUserInfo()
        // 重新更新统计数据
        updateStatistics()
    }
}