package com.example.wonderlog

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

class DiaryFragment : Fragment() {

    private lateinit var recyclerViewDiary: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var spinnerTravelFilter: android.widget.Spinner
    
    // 所有日记数据
    private lateinit var allDiaryList: List<com.example.wonderlog.data.DiaryItem>
    // 筛选后的日记数据
    private lateinit var filteredDiaryList: List<com.example.wonderlog.data.DiaryItem>
    // 所有旅行数据
    private lateinit var allTravelList: List<com.example.wonderlog.data.TravelItem>
    // 当前选中的旅行ID（null表示全部）
    private var selectedTravelId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化RecyclerView
        recyclerViewDiary = view.findViewById(R.id.recycler_view_diary)
        // 设置LayoutManager
        recyclerViewDiary.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        
        // 初始化Spinner
        spinnerTravelFilter = view.findViewById(R.id.spinner_travel_filter)
        
        // 初始化添加日记按钮
        val btnAddDiary = view.findViewById<ImageButton>(R.id.btn_add_diary)
        btnAddDiary.setOnClickListener {
            // 跳转到新建日记页面
            val intent = android.content.Intent(activity, NewDiaryActivity::class.java)
            startActivity(intent)
        }
        
        // 加载数据
        loadDiaryData()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次页面可见时重新加载数据，确保显示当前用户的数据
        loadDiaryData()
    }
    
    /**
     * 加载日记数据
     */
    private fun loadDiaryData() {
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
        
        // 从JSON文件中读取所有日记数据
        val allDiaryItems = if (FileUtils.diaryDataFileExists(requireContext())) {
            // 文件存在，读取数据
            val jsonString = FileUtils.readDiaryData(requireContext())
            jsonString?.let {
                JsonUtils.jsonStringToDiaryItemList(it)
            } ?: emptyList()
        } else {
            // 文件不存在，不创建默认数据，只返回空列表
            // 日记应该与用户自己创建的旅行项关联
            emptyList()
        }
        
        // 筛选当前用户的日记
        allDiaryList = allDiaryItems.filter { userTravelIds.contains(it.travelId) }
        
        // 设置旅行列表为当前用户的旅行项
        allTravelList = userTravelItems
        
        // 设置适配器
        diaryAdapter = DiaryAdapter(allDiaryList) {
            // 日记项点击事件，跳转到日记详情页面
            val diaryDetailFragment = DiaryDetailFragment.newInstance(it)
            (activity as MainActivity).replaceFragment(diaryDetailFragment)
        }
        recyclerViewDiary.adapter = diaryAdapter
        
        // 初始化筛选Spinner
        initTravelFilterSpinner()
        
        // 初始显示全部日记
        filteredDiaryList = allDiaryList
        updateDiaryAdapter(filteredDiaryList)
    }
    
    /**
     * 初始化旅行筛选Spinner
     */
    private fun initTravelFilterSpinner() {
        // 创建旅行名称列表，第一个选项为"全部旅行"
        val travelNames = mutableListOf("全部旅行")
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
                // 没有选中任何项，显示全部日记
                selectedTravelId = null
                filteredDiaryList = allDiaryList
                updateDiaryAdapter(filteredDiaryList)
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
            filteredDiaryList = allDiaryList
        } else {
            // 获取选中的旅行
            val selectedTravel = allTravelList[position - 1]
            selectedTravelId = selectedTravel.id
            
            // 筛选日记列表
            filteredDiaryList = allDiaryList.filter { it.travelId == selectedTravelId }
        }
        
        // 更新日记适配器
        updateDiaryAdapter(filteredDiaryList)
    }
    
    /**
     * 更新日记列表适配器
     */
    private fun updateDiaryAdapter(diaryList: List<com.example.wonderlog.data.DiaryItem>) {
        diaryAdapter = DiaryAdapter(diaryList) { diaryItem ->
            // 日记项点击事件，跳转到日记详情页面
            val diaryDetailFragment = DiaryDetailFragment.newInstance(diaryItem)
            (activity as MainActivity).replaceFragment(diaryDetailFragment)
        }
        recyclerViewDiary.adapter = diaryAdapter
    }
}
