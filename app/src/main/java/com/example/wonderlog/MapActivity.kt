package com.example.wonderlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.MyLocationStyle

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var aMap: AMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化地图SDK
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        
        setContentView(R.layout.activity_map)
        
        // 初始化地图视图
        mapView = findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        
        // 获取地图实例
        aMap = mapView.map
        
        // 配置地图
        configureMap()
    }
    
    /**
     * 配置地图属性
     */
    private fun configureMap() {
        // 启用定位蓝点
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        myLocationStyle.showMyLocation(true)
        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = true
        
        // 设置地图默认缩放级别
        aMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomTo(15f))
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
    }
}