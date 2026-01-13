package com.example.wonderlog.utils

import com.example.wonderlog.data.DiaryItem
import com.example.wonderlog.data.ExpenseItem
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.data.UserItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * JSON工具类，用于序列化和反序列化数据
 */
object JsonUtils {

    // ------------------ TravelItem JSON转换 ------------------

    /**
     * 将TravelItem对象转换为JSONObject
     */
    fun travelItemToJson(travelItem: TravelItem): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", travelItem.id)
        jsonObject.put("userId", travelItem.userId)
        jsonObject.put("title", travelItem.title)
        // 将List<String>转换为JSONArray
        val locationsArray = JSONArray()
        for (location in travelItem.locations) {
            locationsArray.put(location)
        }
        jsonObject.put("locations", locationsArray)
        jsonObject.put("date", travelItem.date)
        jsonObject.put("budget", travelItem.budget)
        return jsonObject
    }

    /**
     * 将JSONObject转换为TravelItem对象
     */
    fun jsonToTravelItem(jsonObject: JSONObject): TravelItem {
        // 将JSONArray转换为List<String>
        val locationsArray = jsonObject.getJSONArray("locations")
        val locationsList = mutableListOf<String>()
        for (i in 0 until locationsArray.length()) {
            locationsList.add(locationsArray.getString(i))
        }
        return TravelItem(
            id = jsonObject.getString("id"),
            userId = if (jsonObject.has("userId")) jsonObject.getString("userId") else "user_1",
            title = jsonObject.getString("title"),
            locations = locationsList,
            date = jsonObject.getString("date"),
            budget = if (jsonObject.has("budget")) jsonObject.getString("budget") else ""
        )
    }

    /**
     * 将TravelItem列表转换为JSONArray
     */
    fun travelItemListToJsonArray(travelItems: List<TravelItem>): JSONArray {
        val jsonArray = JSONArray()
        for (item in travelItems) {
            jsonArray.put(travelItemToJson(item))
        }
        return jsonArray
    }

    /**
     * 将JSONArray转换为TravelItem列表
     */
    fun jsonArrayToTravelItemList(jsonArray: JSONArray): List<TravelItem> {
        val travelItems = mutableListOf<TravelItem>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            travelItems.add(jsonToTravelItem(jsonObject))
        }
        return travelItems
    }

    /**
     * 将JSON字符串转换为TravelItem列表
     */
    fun jsonStringToTravelItemList(jsonString: String): List<TravelItem> {
        return try {
            val jsonArray = JSONArray(jsonString)
            jsonArrayToTravelItemList(jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 将TravelItem列表转换为JSON字符串
     */
    fun travelItemListToJsonString(travelItems: List<TravelItem>): String {
        return travelItemListToJsonArray(travelItems).toString()
    }

    // ------------------ DiaryItem JSON转换 ------------------

    /**
     * 将DiaryItem对象转换为JSONObject
     */
    fun diaryItemToJson(diaryItem: DiaryItem): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", diaryItem.id)
        jsonObject.put("travelId", diaryItem.travelId)
        jsonObject.put("title", diaryItem.title)
        jsonObject.put("content", diaryItem.content)
        jsonObject.put("location", diaryItem.location)
        jsonObject.put("date", diaryItem.date)
        // 将List<String>转换为JSONArray
        val imagesArray = JSONArray()
        for (image in diaryItem.images) {
            imagesArray.put(image)
        }
        jsonObject.put("images", imagesArray)
        return jsonObject
    }

    /**
     * 将JSONObject转换为DiaryItem对象
     */
    fun jsonToDiaryItem(jsonObject: JSONObject): DiaryItem {
        // 将JSONArray转换为List<String>
        val imagesList = mutableListOf<String>()
        if (jsonObject.has("images")) {
            val imagesArray = jsonObject.getJSONArray("images")
            for (i in 0 until imagesArray.length()) {
                imagesList.add(imagesArray.getString(i))
            }
        }
        return DiaryItem(
            id = jsonObject.getString("id"),
            travelId = jsonObject.getString("travelId"),
            title = jsonObject.getString("title"),
            content = jsonObject.getString("content"),
            location = jsonObject.getString("location"),
            date = jsonObject.getString("date"),
            images = imagesList
        )
    }

    /**
     * 将DiaryItem列表转换为JSONArray
     */
    fun diaryItemListToJsonArray(diaryItems: List<DiaryItem>): JSONArray {
        val jsonArray = JSONArray()
        for (item in diaryItems) {
            jsonArray.put(diaryItemToJson(item))
        }
        return jsonArray
    }

    /**
     * 将JSONArray转换为DiaryItem列表
     */
    fun jsonArrayToDiaryItemList(jsonArray: JSONArray): List<DiaryItem> {
        val diaryItems = mutableListOf<DiaryItem>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            diaryItems.add(jsonToDiaryItem(jsonObject))
        }
        return diaryItems
    }

    /**
     * 将JSON字符串转换为DiaryItem列表
     */
    fun jsonStringToDiaryItemList(jsonString: String): List<DiaryItem> {
        return try {
            val jsonArray = JSONArray(jsonString)
            jsonArrayToDiaryItemList(jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 将DiaryItem列表转换为JSON字符串
     */
    fun diaryItemListToJsonString(diaryItems: List<DiaryItem>): String {
        return diaryItemListToJsonArray(diaryItems).toString()
    }

    // ------------------ ExpenseItem JSON转换 ------------------

    /**
     * 将ExpenseItem对象转换为JSONObject
     */
    fun expenseItemToJson(expenseItem: ExpenseItem): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", expenseItem.id)
        jsonObject.put("travelId", expenseItem.travelId)
        jsonObject.put("title", expenseItem.title)
        jsonObject.put("amount", expenseItem.amount)
        jsonObject.put("category", expenseItem.category)
        jsonObject.put("date", expenseItem.date)
        jsonObject.put("iconResId", expenseItem.iconResId)
        return jsonObject
    }

    /**
     * 将JSONObject转换为ExpenseItem对象
     */
    fun jsonToExpenseItem(jsonObject: JSONObject): ExpenseItem {
        return ExpenseItem(
            id = jsonObject.getString("id"),
            travelId = jsonObject.getString("travelId"),
            title = jsonObject.getString("title"),
            amount = jsonObject.getString("amount"),
            category = jsonObject.getString("category"),
            date = jsonObject.getString("date"),
            iconResId = jsonObject.getInt("iconResId")
        )
    }

    /**
     * 将ExpenseItem列表转换为JSONArray
     */
    fun expenseItemListToJsonArray(expenseItems: List<ExpenseItem>): JSONArray {
        val jsonArray = JSONArray()
        for (item in expenseItems) {
            jsonArray.put(expenseItemToJson(item))
        }
        return jsonArray
    }

    /**
     * 将JSONArray转换为ExpenseItem列表
     */
    fun jsonArrayToExpenseItemList(jsonArray: JSONArray): List<ExpenseItem> {
        val expenseItems = mutableListOf<ExpenseItem>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            expenseItems.add(jsonToExpenseItem(jsonObject))
        }
        return expenseItems
    }

    /**
     * 将JSON字符串转换为ExpenseItem列表
     */
    fun jsonStringToExpenseItemList(jsonString: String): List<ExpenseItem> {
        return try {
            val jsonArray = JSONArray(jsonString)
            jsonArrayToExpenseItemList(jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 将ExpenseItem列表转换为JSON字符串
     */
    fun expenseItemListToJsonString(expenseItems: List<ExpenseItem>): String {
        return expenseItemListToJsonArray(expenseItems).toString()
    }

    // ------------------ UserItem JSON转换 ------------------

    /**
     * 将UserItem对象转换为JSONObject
     */
    fun userItemToJson(userItem: UserItem): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", userItem.id)
        jsonObject.put("username", userItem.username)
        jsonObject.put("password", userItem.password)
        jsonObject.put("nickname", userItem.nickname)
        jsonObject.put("avatar", userItem.avatar)
        return jsonObject
    }

    /**
     * 将JSONObject转换为UserItem对象
     */
    fun jsonToUserItem(jsonObject: JSONObject): UserItem {
        return UserItem(
            id = jsonObject.getString("id"),
            username = jsonObject.getString("username"),
            password = jsonObject.getString("password"),
            nickname = if (jsonObject.has("nickname")) jsonObject.getString("nickname") else "",
            avatar = if (jsonObject.has("avatar")) jsonObject.getString("avatar") else ""
        )
    }

    /**
     * 将UserItem列表转换为JSONArray
     */
    fun userItemListToJsonArray(userItems: List<UserItem>): JSONArray {
        val jsonArray = JSONArray()
        for (item in userItems) {
            jsonArray.put(userItemToJson(item))
        }
        return jsonArray
    }

    /**
     * 将JSONArray转换为UserItem列表
     */
    fun jsonArrayToUserItemList(jsonArray: JSONArray): List<UserItem> {
        val userItems = mutableListOf<UserItem>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            userItems.add(jsonToUserItem(jsonObject))
        }
        return userItems
    }

    /**
     * 将JSON字符串转换为UserItem列表
     */
    fun jsonStringToUserItemList(jsonString: String): List<UserItem> {
        return try {
            val jsonArray = JSONArray(jsonString)
            jsonArrayToUserItemList(jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 将UserItem列表转换为JSON字符串
     */
    fun userItemListToJsonString(userItems: List<UserItem>): String {
        return userItemListToJsonArray(userItems).toString()
    }
}
