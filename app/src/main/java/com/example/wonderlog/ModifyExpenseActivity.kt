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

class ModifyExpenseActivity : AppCompatActivity() {

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
    private val categories by lazy {
        listOf(
            getString(R.string.cat_transport),
            getString(R.string.cat_food),
            getString(R.string.cat_shopping),
            getString(R.string.cat_accommodation),
            getString(R.string.cat_sightseeing),
            getString(R.string.cat_entertainment)
        )
    }
    
    // 类别图标映射
    private val categoryIcons by lazy {
        mapOf(
            getString(R.string.cat_transport) to android.R.drawable.ic_menu_directions,
            getString(R.string.cat_food) to android.R.drawable.ic_menu_compass,
            getString(R.string.cat_shopping) to android.R.drawable.ic_menu_share,
            getString(R.string.cat_accommodation) to android.R.drawable.ic_menu_myplaces,
            getString(R.string.cat_sightseeing) to android.R.drawable.ic_menu_gallery,
            getString(R.string.cat_entertainment) to android.R.drawable.ic_media_play
        )
    }

    // 用于日期格式化的SimpleDateFormat
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // 当前要修改的费用项
    private lateinit var currentExpenseItem: ExpenseItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)
        
        // 获取传递过来的费用项
        currentExpenseItem = intent.getParcelableExtra("expense_item")!!
        
        // 初始化视图
        initViews()
        
        // 填充现有数据
        fillExistingData()
        
        // 设置按钮点击事件
        setButtonListeners()
        
        // 初始化旅行下拉菜单
        initTravelSpinner(true)
        
        // 初始化类别下拉菜单
        initCategorySpinner(true)
        
        // 重新设置日期，确保监听器触发后日期仍然正确
        selectedDate = currentExpenseItem.date
        btnSelectDate.text = selectedDate
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
            saveModifiedExpense()
        }
        
        // 选择日期按钮
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    /**
     * 填充现有数据到表单
     */
    private fun fillExistingData() {
        // 填充费用名称
        etExpenseTitle.setText(currentExpenseItem.title)
        
        // 填充金额
        etExpenseAmount.setText(currentExpenseItem.amount)
        
        // 处理日期
        selectedDate = currentExpenseItem.date
        btnSelectDate.text = selectedDate
    }

    /**
     * 初始化旅行下拉菜单
     */
    private fun initTravelSpinner(preselect: Boolean) {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(this) ?: ""
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 读取旅行数据
        val travelJson = FileUtils.readTravelData(this)
        val travelList = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it).filter { travelItem -> travelItem.userId == currentUserId }
        } ?: emptyList()

        if (travelList.isEmpty()) {
            Toast.makeText(this, R.string.error_create_trip_first, Toast.LENGTH_SHORT).show()
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
        
        // 如果需要预选中
        if (preselect) {
            // 找到当前旅行的位置
            val travelIndex = travelList.indexOfFirst { it.id == currentExpenseItem.travelId }
            if (travelIndex != -1) {
                spinnerTravel.setSelection(travelIndex)
                selectedTravelId = currentExpenseItem.travelId
                
                // 解析当前旅行的日期范围
                val selectedTravel = travelList[travelIndex]
                val dateRange = selectedTravel.date
                if (dateRange.contains(" to ")) {
                    val dates = dateRange.split(" to ")
                    if (dates.size == 2) {
                        selectedTravelStartDate = dates[0]
                        selectedTravelEndDate = dates[1]
                    }
                } else if (dateRange.contains(" 至 ")) {
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
            }
        }
        
        // 延迟设置监听器，确保初始化完成后再触发
        spinnerTravel.post {
            // 设置Spinner选中监听器
            spinnerTravel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedTravel = travelList[position]
                    selectedTravelId = selectedTravel.id
                    
                    // 解析旅行日期范围
                    val dateRange = selectedTravel.date
                    if (dateRange.contains(" to ")) {
                        val dates = dateRange.split(" to ")
                        if (dates.size == 2) {
                            selectedTravelStartDate = dates[0]
                            selectedTravelEndDate = dates[1]
                        }
                    } else if (dateRange.contains(" 至 ")) {
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
                    
                    // 只有当旅行发生变更时才清空日期
                    // 如果是初始设置，保持原日期不变
                    if (selectedTravelId != currentExpenseItem.travelId) {
                        // 清空已选择的日期，因为旅行变更后，原日期可能不再有效
                        selectedDate = ""
                        btnSelectDate.text = getString(R.string.select_expense_date)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // 没有选中任何项
                }
            }
        }
    }

    /**
     * 初始化类别下拉菜单
     */
    private fun initCategorySpinner(preselect: Boolean) {
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
        
        // 如果需要预选中
        if (preselect) {
            // 找到当前类别的位置
            val categoryIndex = categories.indexOf(currentExpenseItem.category)
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
                selectedCategory = currentExpenseItem.category
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
        
        // 设置日历默认显示为当前选中日期，如果没有选中则显示旅行开始日期
        if (selectedDate.isNotEmpty()) {
            val date = dateFormat.parse(selectedDate)
            date?.let {
                calendar.time = it
            }
        } else if (startDate != null) {
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
     * 保存修改后的费用数据
     */
    private fun saveModifiedExpense() {
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
            Toast.makeText(this, R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show()
            return
        }

        // 获取类别对应的图标
        val iconResId = categoryIcons[selectedCategory] ?: android.R.drawable.ic_menu_gallery

        // 创建更新后的费用项（保留原有ID和travelId）
        val updatedExpenseItem = ExpenseItem(
            id = currentExpenseItem.id, // 保持原有ID
            travelId = currentExpenseItem.travelId, // 保持原有旅行ID
            title = title,
            amount = amount,
            category = selectedCategory,
            date = selectedDate,
            iconResId = iconResId
        )

        // 保存到文件
        updateExpenseInFile(updatedExpenseItem)

        // 返回
        finish()
    }

    /**
     * 表单验证
     */
    private fun validateForm(title: String, amount: String): Boolean {
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.hint_expense_name, Toast.LENGTH_SHORT).show()
            return false
        }

        if (amount.isEmpty()) {
            Toast.makeText(this, R.string.hint_amount, Toast.LENGTH_SHORT).show()
            return false
        }

        // 验证金额格式
        try {
            amount.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.error_invalid_amount, Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, R.string.select_expense_date, Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedTravelId.isEmpty()) {
            Toast.makeText(this, R.string.error_select_trip, Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, R.string.error_select_category, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * 更新文件中的费用数据
     */
    private fun updateExpenseInFile(updatedExpenseItem: ExpenseItem) {
        // 读取现有费用数据
        val expenseJson = FileUtils.readExpenseData(this)
        val expenseList = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it).toMutableList()
        } ?: mutableListOf()
        
        // 找到并替换原有费用项
        val index = expenseList.indexOfFirst { it.id == updatedExpenseItem.id }
        if (index != -1) {
            expenseList[index] = updatedExpenseItem
        }
        
        // 保存到文件
        val updatedJson = JsonUtils.expenseItemListToJsonString(expenseList)
        val saveSuccess = FileUtils.saveExpenseData(this, updatedJson)

        if (saveSuccess) {
            Toast.makeText(this, R.string.expense_modify_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.error_expense_modify_failed, Toast.LENGTH_SHORT).show()
        }
    }
}