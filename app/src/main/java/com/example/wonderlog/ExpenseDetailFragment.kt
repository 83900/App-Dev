package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wonderlog.data.ExpenseItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils

class ExpenseDetailFragment : Fragment() {
    
    companion object {
        private const val ARG_EXPENSE_ITEM = "expense_item"
        
        fun newInstance(expenseItem: ExpenseItem): ExpenseDetailFragment {
            val fragment = ExpenseDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_EXPENSE_ITEM, expenseItem)
            fragment.arguments = args
            return fragment
        }
    }
    
    private lateinit var expenseItem: ExpenseItem
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTravelName: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            expenseItem = it.getParcelable(ARG_EXPENSE_ITEM)!!
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        ivIcon = view.findViewById(R.id.iv_expense_detail_icon)
        tvTitle = view.findViewById(R.id.tv_expense_detail_title)
        tvAmount = view.findViewById(R.id.tv_expense_detail_amount)
        tvCategory = view.findViewById(R.id.tv_expense_detail_category)
        tvDate = view.findViewById(R.id.tv_expense_detail_date)
        tvTravelName = view.findViewById(R.id.tv_expense_detail_travel_name)
        
        // 初始化返回按钮
        val btnBack = view.findViewById<android.widget.ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // 初始化修改按钮
        val btnEdit = view.findViewById<android.widget.ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener {
            // 跳转到修改费用页面
            val intent = Intent(requireContext(), ModifyExpenseActivity::class.java)
            intent.putExtra("expense_item", expenseItem)
            startActivity(intent)
        }
        
        // 初始化删除按钮
        val btnDelete = view.findViewById<android.widget.ImageButton>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        
        // 加载数据
        loadExpenseData()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次页面可见时重新加载数据，确保显示最新的费用内容
        loadExpenseData()
    }
    
    /**
     * 加载费用数据
     */
    private fun loadExpenseData() {
        // 从文件中重新加载费用数据
        val expenseJson = FileUtils.readExpenseData(requireContext())
        val allExpenses = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it)
        } ?: emptyList()
        
        // 查找最新的费用数据
        val latestExpense = allExpenses.find { it.id == expenseItem.id }
        if (latestExpense != null) {
            expenseItem = latestExpense
        }
        
        // 获取并设置旅行名称
        val travelName = getTravelNameById(expenseItem.travelId)
        tvTravelName.text = travelName
        
        // 设置数据
        ivIcon.setImageResource(expenseItem.iconResId)
        tvTitle.text = expenseItem.title
        tvAmount.text = expenseItem.amount
        tvCategory.text = expenseItem.category
        tvDate.text = expenseItem.date
    }
    
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmationDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("确认删除")
            .setMessage("您确定要删除本次消费记录吗？")
            .setPositiveButton("确认") {
                dialog, which -> deleteExpense()
            }
            .setNegativeButton("取消") {
                dialog, which -> dialog.dismiss()
            }
            .create()
            .show()
    }
    
    /**
     * 删除费用记录
     */
    private fun deleteExpense() {
        // 读取现有费用数据
        val expenseJson = FileUtils.readExpenseData(requireContext())
        val expenseList = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it).toMutableList()
        } ?: mutableListOf()
        
        // 删除指定的费用项
        expenseList.removeAll { it.id == expenseItem.id }
        
        // 保存更新后的费用数据
        val updatedJson = JsonUtils.expenseItemListToJsonString(expenseList)
        val saveSuccess = FileUtils.saveExpenseData(requireContext(), updatedJson)
        
        if (saveSuccess) {
            // 显示删除成功提示
            android.widget.Toast.makeText(requireContext(), "费用记录已删除", android.widget.Toast.LENGTH_SHORT).show()
            
            // 清除返回栈并返回到费用首页
            val mainActivity = requireActivity() as MainActivity
            
            // 清除所有Fragment回栈
            mainActivity.supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            
            // 切换到费用Fragment
            mainActivity.replaceFragment(ExpenseFragment())
            
            // 更新底部导航栏选中状态
            mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_expense
        } else {
            // 显示删除失败提示
            android.widget.Toast.makeText(requireContext(), "费用记录删除失败", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 根据旅行ID获取旅行名称
     */
    private fun getTravelNameById(travelId: String): String {
        // 从文件中读取所有旅行数据
        val jsonString = FileUtils.readTravelData(requireContext())
        jsonString?.let {
            val travelList = JsonUtils.jsonStringToTravelItemList(it)
            // 根据ID查找旅行项
            val travelItem = travelList.find { it.id == travelId }
            return travelItem?.title ?: "未知旅行"
        }
        return "未知旅行"
    }
}