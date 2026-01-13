package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.SharedPreferencesUtils

class HomeFragment : Fragment() {

    private lateinit var recyclerViewTravel: RecyclerView
    private lateinit var travelAdapter: TravelAdapter
    private lateinit var etSearchTravel: android.widget.EditText
    
    // 所有旅行数据
    private lateinit var allTravelList: List<com.example.wonderlog.data.TravelItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化RecyclerView
        recyclerViewTravel = view.findViewById(R.id.recycler_view_travel)
        // 设置LayoutManager
        recyclerViewTravel.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        
        // 初始化搜索框
        etSearchTravel = view.findViewById(R.id.et_search_travel)
        
        // 初始化新建旅行按钮
        val btnNewTrip = view.findViewById<Button>(R.id.btn_new_trip)
        btnNewTrip.setOnClickListener {
            // 跳转到新建旅行Activity
            val intent = Intent(activity, NewTravelActivity::class.java)
            startActivity(intent)
        }
        
        // 加载数据
        loadTravelData()
        
        // 设置搜索框的文本变化监听器
        etSearchTravel.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 文本变化前的处理
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 文本变化时的处理
                val searchText = s.toString().trim()
                searchTravel(searchText)
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {
                // 文本变化后的处理
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        // 每次页面可见时重新加载数据，确保显示当前用户的数据
        loadTravelData()
    }
    
    /**
     * 加载旅行数据
     */
    private fun loadTravelData() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(requireContext()) ?: "user_1"
        
        // 从JSON文件中读取旅行数据
        val allTravelItems = if (FileUtils.travelDataFileExists(requireContext())) {
            // 文件存在，读取数据
            val jsonString = FileUtils.readTravelData(requireContext())
            jsonString?.let {
                JsonUtils.jsonStringToTravelItemList(it)
            } ?: emptyList()
        } else {
            // 文件不存在，不创建默认数据，只返回空列表
            // 旅行项应该由用户自己创建，避免不同用户间的数据混淆
            emptyList()
        }
        
        // 根据当前用户ID筛选旅行项
        allTravelList = allTravelItems.filter { it.userId == currentUserId }
        
        // 更新适配器
        travelAdapter = TravelAdapter(allTravelList) {
            // 旅行项点击事件，跳转到旅行详情页面
            val travelDetailFragment = TravelDetailFragment.newInstance(it)
            (activity as MainActivity).replaceFragment(travelDetailFragment)
        }
        recyclerViewTravel.adapter = travelAdapter
    }
    
    /**
     * 根据关键词搜索旅行
     */
    private fun searchTravel(searchText: String) {
        val filteredList = if (searchText.isEmpty()) {
            // 如果搜索文本为空，显示所有旅行
            allTravelList
        } else {
            // 根据关键词模糊匹配旅行名称
            allTravelList.filter { 
                it.title.contains(searchText, ignoreCase = true)
            }
        }
        
        // 更新适配器
        travelAdapter = TravelAdapter(filteredList) {
            // 旅行项点击事件，跳转到旅行详情页面
            val travelDetailFragment = TravelDetailFragment.newInstance(it)
            (activity as MainActivity).replaceFragment(travelDetailFragment)
        }
        recyclerViewTravel.adapter = travelAdapter
    }
}
