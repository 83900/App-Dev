package com.example.wonderlog

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener

/**
 * 高德地图定位服务工具类
 */
object LocationService {
    private var locationClient: AMapLocationClient? = null
    
    /**
     * 初始化定位服务
     */
    fun init(context: Context) {
        if (locationClient == null) {
            // 初始化定位客户端
            locationClient = AMapLocationClient(context)
            
            // 配置定位参数
            val option = AMapLocationClientOption()
            // 设置定位模式为高精度模式
            option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            // 设置是否返回地址信息
            option.isNeedAddress = true
            // 设置是否只定位一次
            option.isOnceLocation = false
            // 设置定位间隔时间（毫秒）
            option.interval = 2000
            // 设置坐标系类型
            option.isLocationCacheEnable = false
            // 应用配置
            locationClient?.setLocationOption(option)
        }
    }
    
    /**
     * 开始定位
     */
    fun startLocation(listener: AMapLocationListener) {
        locationClient?.setLocationListener(listener)
        locationClient?.startLocation()
    }
    
    /**
     * 停止定位
     */
    fun stopLocation() {
        locationClient?.stopLocation()
    }
    
    /**
     * 销毁定位客户端
     */
    fun destroy() {
        locationClient?.onDestroy()
        locationClient = null
    }
    
    /**
     * 获取格式化的位置信息
     */
    fun formatLocation(location: AMapLocation): String {
        return if (location.poiName.isNullOrEmpty()) {
            location.address ?: "未知位置"
        } else {
            "${location.poiName} (${location.address})"
        }
    }
}