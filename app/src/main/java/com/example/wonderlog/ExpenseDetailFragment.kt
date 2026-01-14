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
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_expense)
            .setMessage(R.string.confirm_delete_expense)
            .setPositiveButton(R.string.confirm) { _, _ ->
                deleteExpense()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 删除费用记录
     */
    private fun deleteExpense() {
        // 读取费用数据
        val expenseJson = FileUtils.readExpenseData(requireContext())
        val expenseList = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it).toMutableList()
        } ?: mutableListOf()

        // 移除当前费用
        val removed = expenseList.removeAll { it.id == expenseItem.id }

        if (removed) {
            // 保存更新后的数据
            val updatedJson = JsonUtils.expenseItemListToJsonString(expenseList)
            FileUtils.saveExpenseData(requireContext(), updatedJson)

            // 提示并返回
            android.widget.Toast.makeText(requireContext(), R.string.expense_deleted, android.widget.Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            android.widget.Toast.makeText(requireContext(), R.string.error_delete_expense, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 根据旅行ID获取旅行名称
     */
    private fun getTravelNameById(travelId: String): String {
        // 读取旅行数据
        val travelJson = FileUtils.readTravelData(requireContext())
        val travelList = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it)
        } ?: emptyList()

        return travelList.find { it.id == travelId }?.title ?: getString(R.string.unknown_trip)
    }
}