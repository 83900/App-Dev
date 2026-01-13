package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.wonderlog.utils.SharedPreferencesUtils
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 检查登录状态
        if (!SharedPreferencesUtils.isLoggedIn(this)) {
            // 未登录，跳转到登录页面
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)

        // 设置WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化底部导航栏
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        
        // 设置默认Fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 设置底部导航栏监听器
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_diary -> {
                    replaceFragment(DiaryFragment())
                    true
                }
                R.id.nav_map -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.nav_expense -> {
                    replaceFragment(ExpenseFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // 替换Fragment的方法
    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        
        // 只给详情页面添加到回退栈，以便返回
        if (fragment is DiaryDetailFragment || fragment is TravelDetailFragment || fragment is ExpenseDetailFragment) {
            transaction.addToBackStack("detail")
        }
        
        transaction.commit()
    }
}