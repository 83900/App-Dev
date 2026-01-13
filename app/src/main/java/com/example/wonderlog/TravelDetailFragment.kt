package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils

class TravelDetailFragment : Fragment() {
    
    companion object {
        private const val ARG_TRAVEL_ITEM = "travel_item"
        
        fun newInstance(travelItem: TravelItem): TravelDetailFragment {
            val fragment = TravelDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_TRAVEL_ITEM, travelItem)
            fragment.arguments = args
            return fragment
        }
    }
    
    private lateinit var travelItem: TravelItem
    private lateinit var tvTitle: TextView
    private lateinit var tvLocations: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvBudget: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            travelItem = it.getParcelable(ARG_TRAVEL_ITEM)!!
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_travel_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        tvTitle = view.findViewById(R.id.tv_travel_detail_title)
        tvLocations = view.findViewById(R.id.tv_travel_detail_locations)
        tvDate = view.findViewById(R.id.tv_travel_detail_date)
        tvBudget = view.findViewById(R.id.tv_travel_detail_budget)
        
        // 初始化返回按钮
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 初始化修改按钮
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener {
            // 跳转到修改页面，传递当前旅行数据
            val intent = Intent(requireContext(), ModifyTravelActivity::class.java)
            intent.putExtra("travel_item", travelItem)
            startActivity(intent)
        }

        // 初始化删除按钮
        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        
        // 设置数据
        updateUI()
    }
    
    override fun onResume() {
        super.onResume()
        // 页面可见时重新加载最新的旅行数据
        loadLatestTravelData()
    }
    
    /**
     * 加载最新的旅行数据
     */
    private fun loadLatestTravelData() {
        val travelJson = FileUtils.readTravelData(requireContext())
        val allTravels = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it)
        } ?: emptyList()
        
        // 根据ID查找最新的旅行数据
        val latestTravel = allTravels.find { it.id == travelItem.id }
        if (latestTravel != null) {
            travelItem = latestTravel
            // 更新UI
            updateUI()
        }
    }
    
    /**
     * 更新UI显示
     */
    private fun updateUI() {
        tvTitle.text = travelItem.title
        tvLocations.text = travelItem.locations.joinToString(", ")
        tvDate.text = travelItem.date
        tvBudget.text = travelItem.budget
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("删除旅行记录")
            .setMessage("您确定要删除本次旅行记录吗（同时会删除与本次旅行相关的日记和费用记录哦）？")
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("确认") { dialog, _ ->
                deleteTravelRecord()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 删除旅行记录及其相关数据
     */
    private fun deleteTravelRecord() {
        // 1. 删除旅行记录
        val travelJson = FileUtils.readTravelData(requireContext())
        val allTravels = travelJson?.let {
            JsonUtils.jsonStringToTravelItemList(it)
        } ?: emptyList()
        val updatedTravels = allTravels.filter { it.id != travelItem.id }
        val updatedTravelJson = JsonUtils.travelItemListToJsonString(updatedTravels)
        FileUtils.saveTravelData(requireContext(), updatedTravelJson)

        // 2. 删除相关日记记录
        val diaryJson = FileUtils.readDiaryData(requireContext())
        val allDiaries = diaryJson?.let {
            JsonUtils.jsonStringToDiaryItemList(it)
        } ?: emptyList()
        val updatedDiaries = allDiaries.filter { it.travelId != travelItem.id }
        val updatedDiaryJson = JsonUtils.diaryItemListToJsonString(updatedDiaries)
        FileUtils.saveDiaryData(requireContext(), updatedDiaryJson)

        // 3. 删除相关费用记录
        val expenseJson = FileUtils.readExpenseData(requireContext())
        val allExpenses = expenseJson?.let {
            JsonUtils.jsonStringToExpenseItemList(it)
        } ?: emptyList()
        val updatedExpenses = allExpenses.filter { it.travelId != travelItem.id }
        val updatedExpenseJson = JsonUtils.expenseItemListToJsonString(updatedExpenses)
        FileUtils.saveExpenseData(requireContext(), updatedExpenseJson)

        // 4. 显示删除成功提示
        Toast.makeText(requireContext(), "旅行记录已删除", Toast.LENGTH_SHORT).show()
        
        // 5. 返回首页
        requireActivity().supportFragmentManager.popBackStack(null, 0)
    }
}