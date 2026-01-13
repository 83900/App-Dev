package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.wonderlog.data.DiaryItem
import java.io.File

class DiaryImageDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_PATHS = "image_paths"
        const val EXTRA_CURRENT_POSITION = "current_position"
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var btnBack: ImageButton
    private lateinit var imagePaths: List<String>
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_image_detail)

        // 获取传递的图片路径和当前位置
        imagePaths = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS) ?: emptyList()
        currentPosition = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0)

        // 初始化视图
        viewPager = findViewById(R.id.view_pager_images)
        btnBack = findViewById(R.id.btn_back)

        // 设置ViewPager适配器
        viewPager.adapter = DiaryImagePagerAdapter(this, imagePaths)
        viewPager.currentItem = currentPosition

        // 设置返回按钮点击事件
        btnBack.setOnClickListener {
            finish()
        }
    }
}
