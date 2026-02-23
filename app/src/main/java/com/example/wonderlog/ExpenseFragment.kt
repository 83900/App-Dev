package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.SharedPreferencesUtils

class ExpenseFragment : Fragment() {

    private lateinit var recyclerViewExpense: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tvTotalExpense: android.widget.TextView
    private lateinit var tvBudget: android.widget.TextView
    private lateinit var tvRemaining: android.widget.TextView
    private lateinit var spinnerTravelFilter: android.widget.Spinner
    
    // 所有费用数据
    private lateinit var allExpenseList: List<com.example.wonderlog.data.ExpenseItem>
    // 筛选后的费用数据
    private lateinit var filteredExpenseList: List<com.example.wonderlog.data.ExpenseItem>
    // 所有旅行数据
    private lateinit var allTravelList: List<com.example.wonderlog.data.TravelItem>
    // 当前选中的旅行ID（null表示全部）
    private var selectedTravelId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        recyclerViewExpense = view.findViewById(R.id.recycler_view_expense)
        // 设置LayoutManager
        recyclerViewExpense.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        
        // 初始化TextView
        tvTotalExpense = view.findViewById(R.id.tv_total_expense)
        tvBudget = view.findViewById(R.id.tv_budget)
        tvRemaining = view.findViewById(R.id.tv_remaining)
        
        // 初始化Spinner
        spinnerTravelFilter = view.findViewById(R.id.spinner_travel_filter)
        
        // 添加费用按钮点击事件
        val btnAddExpense = view.findViewById<ImageButton>(R.id.btn_add_expense)
        btnAddExpense.setOnClickListener {
            // 跳转到添加费用页面
            val intent = Intent(activity, NewExpenseActivity::class.java)
            startActivity(intent)
        }
        
        loadData()
    }
    
    override fun onResume() {
        super.onResume()
        // 刷新数据，确保显示当前用户的数据
        loadData()
    }
    
    /**
     * 加载数据并刷新UI
     */
    private fun loadData() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(requireContext()) ?: "user_1"
        
        // 从JSON文件中读取所有旅行数据
        val allTravelItems = if (FileUtils.travelDataFileExists(requireContext())) {
            val jsonString = FileUtils.readTravelData(requireContext())
            jsonString?.let {
                JsonUtils.jsonStringToTravelItemList(it)
            } ?: emptyList()
        } else {
            emptyList()
        }
        
        // 筛选当前用户的旅行项
        val userTravelItems = allTravelItems.filter { it.userId == currentUserId }
        
        // 获取当前用户旅行项的travelId列表
        val userTravelIds = userTravelItems.map { it.id }.toSet()
        
        // 从JSON文件中读取所有费用数据
        val allExpenseItems = if (FileUtils.expenseDataFileExists(requireContext())) {
            // 文件存在，读取数据
            val jsonString = FileUtils.readExpenseData(requireContext())
            jsonString?.let {
                JsonUtils.jsonStringToExpenseItemList(it)
            } ?: emptyList()
        } else {
            // 文件不存在，不创建默认数据，只返回空列表
            // 费用应该与用户自己创建的旅行项关联
            emptyList()
        }
        
        // 筛选当前用户的费用记录
        allExpenseList = allExpenseItems.filter { userTravelIds.contains(it.travelId) }
        
        // 设置旅行列表为当前用户的旅行项
        allTravelList = userTravelItems
        
        // 设置适配器
        expenseAdapter = ExpenseAdapter(allExpenseList) {
            // 费用项点击事件，跳转到费用详情页面
            val expenseDetailFragment = ExpenseDetailFragment.newInstance(it)
            (activity as MainActivity).replaceFragment(expenseDetailFragment)
        }
        recyclerViewExpense.adapter = expenseAdapter
        
        // 初始化筛选Spinner
        initTravelFilterSpinner()
        
        // 初始显示全部费用
        filteredExpenseList = allExpenseList
        calculateAndUpdateStatistics(filteredExpenseList, null)
    }
    
    /**
     * 初始化旅行筛选Spinner
     */
    private fun initTravelFilterSpinner() {
        // 创建旅行名称列表，第一个选项为"全部旅行"
        val travelNames = mutableListOf(getString(R.string.all_trips))
        // 添加所有旅行名称
        allTravelList.forEach { travelNames.add(it.title) }
        
        // 设置Spinner适配器
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            travelNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTravelFilter.adapter = adapter
        
        // 设置Spinner选中监听器
        spinnerTravelFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                // 处理筛选事件
                onTravelFilterSelected(position)
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // 没有选中任何项，显示全部费用
                selectedTravelId = null
                filteredExpenseList = allExpenseList
                expenseAdapter = ExpenseAdapter(filteredExpenseList) { expenseItem ->
                    val expenseDetailFragment = ExpenseDetailFragment.newInstance(expenseItem)
                    (activity as MainActivity).replaceFragment(expenseDetailFragment)
                }
                recyclerViewExpense.adapter = expenseAdapter
                calculateAndUpdateStatistics(filteredExpenseList, null)
            }
        }
    }
    
    /**
     * 处理旅行筛选选中事件
     */
    private fun onTravelFilterSelected(position: Int) {
        // 第一个选项是"全部旅行"
        if (position == 0) {
            selectedTravelId = null
            filteredExpenseList = allExpenseList
            calculateAndUpdateStatistics(filteredExpenseList, null)
        } else {
            // 获取选中的旅行
            val selectedTravel = allTravelList[position - 1]
            selectedTravelId = selectedTravel.id
            
            // 筛选费用列表
            filteredExpenseList = allExpenseList.filter { it.travelId == selectedTravelId }
            
            // 计算并更新统计数据
            calculateAndUpdateStatistics(filteredExpenseList, selectedTravelId)
        }
        
        // 更新RecyclerView适配器
        expenseAdapter = ExpenseAdapter(filteredExpenseList) { expenseItem ->
            val expenseDetailFragment = ExpenseDetailFragment.newInstance(expenseItem)
            (activity as MainActivity).replaceFragment(expenseDetailFragment)
        }
        recyclerViewExpense.adapter = expenseAdapter
    }
    
    /**
     * 计算并更新费用统计数据
     */
    private fun calculateAndUpdateStatistics(expenseList: List<com.example.wonderlog.data.ExpenseItem>, travelId: String?) {
        // 计算总费用
        val totalExpense = calculateTotalExpense(expenseList)
        
        // 计算预算
        val budget = calculateBudget(travelId)
        
        // 计算剩余金额
        val remaining = budget - totalExpense
        
        // 更新UI
        tvTotalExpense.text = formatAmount(totalExpense)
        tvBudget.text = formatAmount(budget)
        tvRemaining.text = formatAmount(remaining)
        
        // 设置剩余金额的颜色：如果超支，显示红色；否则显示绿色
        tvRemaining.setTextColor(
            if (remaining < 0) {
                resources.getColor(android.R.color.holo_red_light, null)
            } else {
                resources.getColor(android.R.color.holo_green_light, null)
            }
        )
    }
    
    /**
     * 计算总费用
     */
    private fun calculateTotalExpense(expenseList: List<com.example.wonderlog.data.ExpenseItem>): Double {
        return expenseList.sumOf { amountStringToDouble(it.amount) }
    }
    
    /**
     * 计算预算
     * @param travelId 旅行ID，如果为null则计算所有旅行的总预算
     */
    private fun calculateBudget(travelId: String?): Double {
        return if (travelId == null) {
            // 计算所有旅行的总预算
            allTravelList.sumOf { amountStringToDouble(it.budget) }
        } else {
            // 计算指定旅行的预算
            allTravelList.find { it.id == travelId }?.let {
                amountStringToDouble(it.budget)
            } ?: 0.0
        }
    }
    
    /**
     * 将金额字符串转换为Double
     */
    private fun amountStringToDouble(amountString: String): Double {
        // 移除金额字符串中的非数字字符（除了数字、小数点和负号）
        val cleanAmount = amountString.filter { it.isDigit() || it == '.' || it == '-' }
        return try {
            cleanAmount.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }
    
    /**
     * 格式化金额为带千位分隔符的字符串
     */
    private fun formatAmount(amount: Double): String {
        // 格式化金额，保留两位小数，添加千位分隔符
        return String.format("¥ %,d", amount.toLong())
    }
}
