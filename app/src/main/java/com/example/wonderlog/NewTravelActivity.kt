package com.example.wonderlog

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class NewTravelActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var btnSelectStartDate: Button
    private lateinit var btnSelectEndDate: Button
    private lateinit var btnAddCity: Button
    private lateinit var etTravelTitle: EditText
    private lateinit var etBudget: EditText
    private lateinit var llCitiesContainer: LinearLayout

    private var startDate: String = ""
    private var endDate: String = ""
    private var cityCount = 1
    
    // 用于日期比较的SimpleDateFormat
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_travel)

        // 初始化视图
        initViews()

        // 设置按钮点击事件
        setButtonListeners()
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSave = findViewById(R.id.btn_save)
        btnSelectStartDate = findViewById(R.id.btn_select_start_date)
        btnSelectEndDate = findViewById(R.id.btn_select_end_date)
        btnAddCity = findViewById(R.id.btn_add_city)
        etTravelTitle = findViewById(R.id.et_travel_title)
        etBudget = findViewById(R.id.et_budget)
        llCitiesContainer = findViewById(R.id.ll_cities_container)

        // 初始化第一个城市的删除按钮
        val btnRemoveCity0 = findViewById<Button>(R.id.btn_remove_city_0)
        btnRemoveCity0.setOnClickListener {
            removeCity(0)
        }
    }

    /**
     * 设置按钮点击事件
     */
    private fun setButtonListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }

        // 保存按钮
        btnSave.setOnClickListener {
            saveTravel()
        }

        // 选择开始日期按钮
        btnSelectStartDate.setOnClickListener {
            showStartDatePicker()
        }

        // 选择结束日期按钮
        btnSelectEndDate.setOnClickListener {
            showEndDatePicker()
        }

        // 添加城市按钮
        btnAddCity.setOnClickListener {
            addCity()
        }
    }

    /**
     * 显示开始日期选择器
     */
    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 格式化日期
                startDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                btnSelectStartDate.text = startDate
                
                // 如果结束日期早于开始日期，清空结束日期
                if (endDate.isNotEmpty()) {
                    val start = dateFormat.parse(startDate)
                    val end = dateFormat.parse(endDate)
                    if (start != null && end != null && end.before(start)) {
                        endDate = ""
                        btnSelectEndDate.text = "请选择结束日期"
                        Toast.makeText(this, "结束日期不能早于开始日期，已清空结束日期", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    /**
     * 显示结束日期选择器
     */
    private fun showEndDatePicker() {
        // 如果没有选择开始日期，提示用户先选择开始日期
        if (startDate.isEmpty()) {
            Toast.makeText(this, "请先选择开始日期", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        
        // 将开始日期设置为日历的最小日期
        val start = dateFormat.parse(startDate)
        if (start != null) {
            calendar.time = start
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 格式化日期
                endDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                btnSelectEndDate.text = endDate
            },
            year,
            month,
            day
        )
        
        // 设置日期选择器的最小日期为开始日期
        datePickerDialog.datePicker.minDate = start?.time ?: 0
        datePickerDialog.show()
    }

    /**
     * 添加城市输入框
     */
    private fun addCity() {
        // 创建城市输入布局
        val cityLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8 * resources.displayMetrics.density.toInt() // 8dp转换为像素
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // 创建城市输入框
        val etCity = EditText(this).apply {
            id = View.generateViewId()
            val padding = 16 * resources.displayMetrics.density.toInt() // 16dp转换为像素
            layoutParams = LinearLayout.LayoutParams(
                0,
                56 * resources.displayMetrics.density.toInt(), // 固定高度56dp
                1f
            )
            setBackgroundResource(R.drawable.rounded_background)
            hint = "请输入城市名称"
            setPadding(padding, 0, padding, 0) // 只设置左右padding，上下由高度控制
            setTextColor(resources.getColor(R.color.on_surface))
            textSize = 16f
            gravity = android.view.Gravity.CENTER_VERTICAL // 文本居中显示
        }

        // 创建删除按钮
        val btnRemoveCity = Button(this).apply {
            id = View.generateViewId()
            val buttonSize = 24 * resources.displayMetrics.density.toInt() // 24dp转换为像素
            val margin = 8 * resources.displayMetrics.density.toInt() // 8dp转换为像素
            layoutParams = LinearLayout.LayoutParams(
                buttonSize,
                buttonSize
            ).apply {
                marginStart = margin
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            setBackgroundResource(R.drawable.rounded_background)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                backgroundTintList = ColorStateList.valueOf(resources.getColor(android.R.color.holo_red_dark))
            } else {
                setBackgroundTintList(ColorStateList.valueOf(resources.getColor(android.R.color.holo_red_dark)))
            }
            text = "-"
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f // 调整为适合24dp按钮的字体大小
            setTypeface(null, Typeface.BOLD) // 设置加粗
            gravity = android.view.Gravity.CENTER // 文本居中
            setPadding(0, 0, 0, 0) // 移除内边距
            includeFontPadding = false // 移除字体默认内边距
            setOnClickListener {
                // 查找当前按钮所在的城市布局在容器中的索引
                val index = llCitiesContainer.indexOfChild(cityLayout)
                if (index != -1) {
                    removeCity(index)
                }
            }
        }

        // 添加到容器
        cityLayout.addView(etCity)
        cityLayout.addView(btnRemoveCity)
        llCitiesContainer.addView(cityLayout)

        cityCount++
    }

    /**
     * 删除城市输入框
     */
    private fun removeCity(index: Int) {
        // 至少保留一个城市
        if (cityCount <= 1) {
            Toast.makeText(this, "至少需要一个城市", Toast.LENGTH_SHORT).show()
            return
        }

        // 移除对应的城市布局
        llCitiesContainer.removeViewAt(index)
        cityCount--
    }

    /**
     * 保存旅行数据
     */
    private fun saveTravel() {
        // 获取表单数据
        val title = etTravelTitle.text.toString().trim()
        val budget = etBudget.text.toString().trim()

        // 表单验证
        if (!validateForm(title)) {
            return
        }

        // 收集城市列表
        val cities = mutableListOf<String>()
        for (i in 0 until llCitiesContainer.childCount) {
            val cityLayout = llCitiesContainer.getChildAt(i) as LinearLayout
            val etCity = cityLayout.getChildAt(0) as EditText
            val cityName = etCity.text.toString().trim()
            if (cityName.isNotEmpty()) {
                cities.add(cityName)
            }
        }

        // 验证城市列表
        if (cities.isEmpty()) {
            Toast.makeText(this, "请至少输入一个城市", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(this) ?: ""
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建旅行项
        val travelItem = TravelItem(
            id = "travel_${UUID.randomUUID()}",
            userId = currentUserId,
            title = title,
            locations = cities,
            date = "$startDate 至 $endDate",
            budget = budget
        )

        // 保存到文件
        saveTravelToFile(travelItem)

        // 返回首页
        finish()
    }

    /**
     * 表单验证
     */
    private fun validateForm(title: String): Boolean {
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入旅行名称", Toast.LENGTH_SHORT).show()
            return false
        }

        if (startDate.isEmpty()) {
            Toast.makeText(this, "请选择开始日期", Toast.LENGTH_SHORT).show()
            return false
        }

        if (endDate.isEmpty()) {
            Toast.makeText(this, "请选择结束日期", Toast.LENGTH_SHORT).show()
            return false
        }

        // 验证结束日期不早于开始日期
        try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            if (start != null && end != null && end.before(start)) {
                Toast.makeText(this, "结束日期不能早于开始日期", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "日期格式错误", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * 保存旅行数据到文件
     */
    private fun saveTravelToFile(travelItem: TravelItem) {
        // 读取现有旅行数据
        val travelJson = FileUtils.readTravelData(this)
        val travelList = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it).toMutableList()
        } ?: mutableListOf()

        // 添加新旅行项
        travelList.add(travelItem)

        // 保存到文件
        val updatedJson = JsonUtils.travelItemListToJsonString(travelList)
        val saveSuccess = FileUtils.saveTravelData(this, updatedJson)

        if (saveSuccess) {
            Toast.makeText(this, "旅行创建成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "旅行创建失败", Toast.LENGTH_SHORT).show()
        }
    }
}
