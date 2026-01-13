package com.example.wonderlog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wonderlog.data.ExpenseItem
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class NewExpenseActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var btnSelectDate: Button
    private lateinit var etExpenseTitle: EditText
    private lateinit var etExpenseAmount: EditText
    private lateinit var spinnerTravel: Spinner
    private lateinit var spinnerCategory: Spinner

    private var selectedDate: String = ""
    private var selectedTravelId: String = ""
    private var selectedCategory: String = ""
    
    // 选中旅行的开始和结束日期
    private var selectedTravelStartDate: String = ""
    private var selectedTravelEndDate: String = ""

    // 消费类别列表
    private val categories = listOf("交通", "饮食", "购物", "住宿", "景点", "娱乐")
    
    // 类别图标映射
    private val categoryIcons = mapOf(
        "交通" to android.R.drawable.ic_menu_directions,
        "饮食" to android.R.drawable.ic_menu_compass,
        "购物" to android.R.drawable.ic_menu_share,
        "住宿" to android.R.drawable.ic_menu_myplaces,
        "景点" to android.R.drawable.ic_menu_gallery,
        "娱乐" to android.R.drawable.ic_media_play
    )

    // 用于日期格式化的SimpleDateFormat
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)

        // 初始化视图
        initViews()

        // 设置按钮点击事件
        setButtonListeners()

        // 初始化旅行下拉菜单
        initTravelSpinner()

        // 初始化类别下拉菜单
        initCategorySpinner()
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSave = findViewById(R.id.btn_save)
        btnSelectDate = findViewById(R.id.btn_select_date)
        etExpenseTitle = findViewById(R.id.et_expense_title)
        etExpenseAmount = findViewById(R.id.et_expense_amount)
        spinnerTravel = findViewById(R.id.spinner_travel)
        spinnerCategory = findViewById(R.id.spinner_category)
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
            saveExpense()
        }

        // 选择日期按钮
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    /**
     * 初始化旅行下拉菜单
     */
    private fun initTravelSpinner() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(this) ?: ""
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 读取旅行数据
        val travelJson = FileUtils.readTravelData(this)
        val travelList = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it).filter { travelItem -> travelItem.userId == currentUserId }
        } ?: emptyList()

        if (travelList.isEmpty()) {
            Toast.makeText(this, "请先创建旅行", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 创建旅行名称列表
        val travelNames = travelList.map { it.title }

        // 设置Spinner适配器
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            travelNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTravel.adapter = adapter

        // 设置默认选中第一个旅行
        val defaultTravel = travelList[0]
        selectedTravelId = defaultTravel.id
        
        // 解析默认旅行的日期范围
        val defaultDateRange = defaultTravel.date
        if (defaultDateRange.contains(" 至 ")) {
            val dates = defaultDateRange.split(" 至 ")
            if (dates.size == 2) {
                selectedTravelStartDate = dates[0]
                selectedTravelEndDate = dates[1]
            }
        } else {
            // 如果没有日期范围，使用相同日期作为开始和结束日期
            selectedTravelStartDate = defaultDateRange
            selectedTravelEndDate = defaultDateRange
        }

        // 设置Spinner选中监听器
        spinnerTravel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTravel = travelList[position]
                selectedTravelId = selectedTravel.id
                
                // 解析旅行日期范围
                val dateRange = selectedTravel.date
                if (dateRange.contains(" 至 ")) {
                    val dates = dateRange.split(" 至 ")
                    if (dates.size == 2) {
                        selectedTravelStartDate = dates[0]
                        selectedTravelEndDate = dates[1]
                    }
                } else {
                    // 如果没有日期范围，使用相同日期作为开始和结束日期
                    selectedTravelStartDate = dateRange
                    selectedTravelEndDate = dateRange
                }
                
                // 清空已选择的日期，因为旅行变更后，原日期可能不再有效
                selectedDate = ""
                btnSelectDate.text = "请选择消费日期"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 没有选中任何项
            }
        }
    }

    /**
     * 初始化类别下拉菜单
     */
    private fun initCategorySpinner() {
        // 设置Spinner适配器
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // 设置默认选中第一个类别
        selectedCategory = categories[0]

        // 设置Spinner选中监听器
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 没有选中任何项
            }
        }
    }

    /**
     * 显示日期选择器
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // 解析旅行的开始和结束日期
        val startDate = dateFormat.parse(selectedTravelStartDate)
        val endDate = dateFormat.parse(selectedTravelEndDate)
        
        // 设置日历默认显示为旅行开始日期
        if (startDate != null) {
            calendar.time = startDate
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 格式化日期
                selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                btnSelectDate.text = selectedDate
            },
            year,
            month,
            day
        )
        
        // 设置日期选择器的最小和最大日期
        if (startDate != null) {
            datePickerDialog.datePicker.minDate = startDate.time
        }
        if (endDate != null) {
            datePickerDialog.datePicker.maxDate = endDate.time
        }
        
        datePickerDialog.show()
    }

    /**
     * 保存费用记录
     */
    private fun saveExpense() {
        // 获取表单数据
        val title = etExpenseTitle.text.toString().trim()
        val amount = etExpenseAmount.text.toString().trim()

        // 表单验证
        if (!validateForm(title, amount)) {
            return
        }

        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(this) ?: ""
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取类别对应的图标
        val iconResId = categoryIcons[selectedCategory] ?: android.R.drawable.ic_menu_gallery

        // 创建费用项
        val expenseItem = ExpenseItem(
            id = "expense_${UUID.randomUUID()}",
            travelId = selectedTravelId,
            title = title,
            amount = amount,
            category = selectedCategory,
            date = selectedDate,
            iconResId = iconResId
        )

        // 保存到文件
        saveExpenseToFile(expenseItem)

        // 返回费用页面
        finish()
    }

    /**
     * 表单验证
     */
    private fun validateForm(title: String, amount: String): Boolean {
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入消费名称", Toast.LENGTH_SHORT).show()
            return false
        }

        if (amount.isEmpty()) {
            Toast.makeText(this, "请输入消费金额", Toast.LENGTH_SHORT).show()
            return false
        }

        // 验证金额格式
        try {
            amount.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "请输入有效的消费金额", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "请选择消费日期", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedTravelId.isEmpty()) {
            Toast.makeText(this, "请选择所属旅行", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "请选择消费类别", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * 保存费用数据到文件
     */
    private fun saveExpenseToFile(expenseItem: ExpenseItem) {
        // 读取现有费用数据
        val expenseJson = FileUtils.readExpenseData(this)
        val expenseList = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it).toMutableList()
        } ?: mutableListOf()

        // 添加新费用项
        expenseList.add(expenseItem)

        // 保存到文件
        val updatedJson = JsonUtils.expenseItemListToJsonString(expenseList)
        val saveSuccess = FileUtils.saveExpenseData(this, updatedJson)

        if (saveSuccess) {
            Toast.makeText(this, "费用添加成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "费用添加失败", Toast.LENGTH_SHORT).show()
        }
    }
}