package com.example.wonderlog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment(), AMapLocationListener, PoiSearch.OnPoiSearchListener {

    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    
    // 旧的位置信息显示组件
    private lateinit var locationText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    
    // 新的UI组件
    private lateinit var searchBar: androidx.cardview.widget.CardView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var fabLocation: FloatingActionButton
    private lateinit var cardLocationName: TextView
    private lateinit var cardLatitudeText: TextView
    private lateinit var cardLongitudeText: TextView
    
    // 当前位置信息
    private var currentLocation: AMapLocation? = null
    // 定位模式切换标志
    private var isFollowMode = false
    
    // POI搜索相关
    private var poiSearch: PoiSearch? = null

    companion object {
        // 定位权限请求码
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        // 需要请求的定位权限
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        
        // 初始化视图
        locationText = view.findViewById(R.id.locationText)
        latitudeText = view.findViewById(R.id.latitudeText)
        longitudeText = view.findViewById(R.id.longitudeText)
        
        // 初始化新的UI组件
        searchBar = view.findViewById(R.id.searchCard) as androidx.cardview.widget.CardView
        searchEditText = view.findViewById(R.id.searchEditText) as EditText
        searchButton = view.findViewById(R.id.searchButton) as ImageView
        fabLocation = view.findViewById(R.id.fab_location) as FloatingActionButton
        cardLocationName = view.findViewById(R.id.cardLocationName) as TextView
        cardLatitudeText = view.findViewById(R.id.cardLatitudeText) as TextView
        cardLongitudeText = view.findViewById(R.id.cardLongitudeText) as TextView
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        initLocation()
        initEventListeners()
    }
    
    /**
     * 初始化事件监听器
     */
    private fun initEventListeners() {
        // 定位按钮点击事件 - 切换定位模式
        fabLocation.setOnClickListener {
            isFollowMode = !isFollowMode
            val myLocationStyle = MyLocationStyle()
            if (isFollowMode) {
                // 切换到跟随模式
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
                Toast.makeText(requireContext(), "已切换到跟随模式", Toast.LENGTH_SHORT).show()
                // 如果有当前位置，移动地图到当前位置
                currentLocation?.let {
                    aMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    )
                }
            } else {
                // 切换到只显示模式
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
                Toast.makeText(requireContext(), "已切换到显示模式", Toast.LENGTH_SHORT).show()
            }
            myLocationStyle.showMyLocation(true)
            aMap.myLocationStyle = myLocationStyle
        }
        
        // 搜索按钮点击事件
        searchButton.setOnClickListener {
            performSearch()
        }
        
        // 搜索输入框回车事件
        searchEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                performSearch()
                return@setOnKeyListener true
            }
            false
        }
    }
    
    /**
     * 执行POI搜索
     */
    private fun performSearch() {
        val keyword = searchEditText.text.toString().trim()
        if (keyword.isEmpty()) {
            Toast.makeText(requireContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 构建POI搜索参数
        val query = PoiSearch.Query(keyword, "", "")
        query.pageSize = 1
        query.pageNum = 0
        
        // 执行搜索
        poiSearch = PoiSearch(requireContext(), query)
        poiSearch?.setOnPoiSearchListener(this)
        poiSearch?.searchPOIAsyn()
    }
    
    /**
     * 处理POI搜索结果
     */
    private fun handlePoiSearchResult(result: PoiResult) {
        val poiItems = result.pois
        if (poiItems.isNotEmpty()) {
            val poiItem = poiItems[0]
            val latLng = LatLng(poiItem.latLonPoint.latitude, poiItem.latLonPoint.longitude)
            
            // 移动地图到搜索结果位置
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            
            // 更新位置信息卡片
            updateSearchResultCard(poiItem)
        } else {
            Toast.makeText(requireContext(), "未找到相关地点", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新搜索结果到位置信息卡片
     */
    private fun updateSearchResultCard(poiItem: PoiItem) {
        cardLocationName.text = poiItem.title
        cardLatitudeText.text = String.format("纬度: %.4f°N", poiItem.latLonPoint.latitude)
        cardLongitudeText.text = String.format("经度: %.4f°E", poiItem.latLonPoint.longitude)
    }
    
    /**
     * POI搜索结果回调
     */
    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null) {
            handlePoiSearchResult(result)
        } else {
            Toast.makeText(requireContext(), "搜索失败: $rCode", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * POI详情搜索结果回调
     */
    override fun onPoiItemSearched(item: PoiItem?, rCode: Int) {
        // 单个POI详情搜索结果处理
    }

    private fun initMap() {
        // 初始化地图SDK隐私设置（必须在使用地图前调用）
        try {
            com.amap.api.maps.MapsInitializer.updatePrivacyShow(requireContext(), true, true)
            com.amap.api.maps.MapsInitializer.updatePrivacyAgree(requireContext(), true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        aMap = mapView.map
        
        // 配置地图定位蓝点样式，但暂时不启用
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        myLocationStyle.showMyLocation(true)
        aMap.myLocationStyle = myLocationStyle
        
        // 设置地图默认缩放级别
        aMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomTo(15f))
        
        // 隐藏旧的位置信息显示组件
        locationText.visibility = View.GONE
        latitudeText.visibility = View.GONE
        longitudeText.visibility = View.GONE
    }
    
    /**
     * 启用定位蓝点并开始定位
     */
    private fun enableLocation() {
        // 启用地图定位蓝点
        aMap.isMyLocationEnabled = true
        // 初始化定位服务
        LocationService.init(requireContext())
        // 开始定位
        LocationService.startLocation(this)
    }
    
    private fun initLocation() {
        // 检查定位权限
        if (checkLocationPermission()) {
            // 有权限，启用定位
            enableLocation()
        } else {
            // 没有权限，请求定位权限
            requestLocationPermission()
        }
    }

    /**
     * 定位回调
     */
    override fun onLocationChanged(location: AMapLocation?) {
        location?.let {
            // 保存当前位置
            currentLocation = it
            
            // 更新旧的UI显示
            val formattedLocation = LocationService.formatLocation(it)
            locationText.text = formattedLocation
            latitudeText.text = String.format("纬度: %.4f°N", it.latitude)
            longitudeText.text = String.format("经度: %.4f°E", it.longitude)
            
            // 更新新的位置信息卡片
            updateLocationCard(it)
        }
    }
    
    /**
     * 更新位置信息卡片
     */
    private fun updateLocationCard(location: AMapLocation) {
        val locationName = if (location.poiName.isNullOrEmpty()) {
            location.address ?: "未知位置"
        } else {
            location.poiName
        }
        cardLocationName.text = locationName
        cardLatitudeText.text = String.format("纬度: %.4f°N", location.latitude)
        cardLongitudeText.text = String.format("经度: %.4f°E", location.longitude)
    }
    
    /**
     * 检查是否有定位权限
     */
    private fun checkLocationPermission(): Boolean {
        for (permission in LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    
    /**
     * 请求定位权限
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            LOCATION_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // 检查所有权限是否都被授予
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                // 权限被授予，启用定位
                enableLocation()
            } else {
                // 权限被拒绝，显示提示信息
                Toast.makeText(
                    requireContext(),
                    "定位权限被拒绝，无法获取位置信息",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        LocationService.destroy()
        // 释放POI搜索实例
        poiSearch = null
    }
}