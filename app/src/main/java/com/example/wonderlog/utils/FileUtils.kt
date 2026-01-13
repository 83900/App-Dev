package com.example.wonderlog.utils

import android.content.Context
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

/**
 * 文件读写工具类，用于保存和读取JSON数据
 */
object FileUtils {

    // 文件名称常量
    private const val FILE_USER_DATA = "user_data.json"
    private const val FILE_TRAVEL_DATA = "travel_data.json"
    private const val FILE_DIARY_DATA = "diary_data.json"
    private const val FILE_EXPENSE_DATA = "expense_data.json"

    /**
     * 获取应用的私有文件目录下的文件
     */
    private fun getFile(context: Context, fileName: String): File {
        return File(context.filesDir, fileName)
    }

    /**
     * 将JSON字符串写入文件
     */
    fun writeJsonToFile(context: Context, fileName: String, jsonString: String): Boolean {
        return try {
            val file = getFile(context, fileName)
            val fileWriter = FileWriter(file)
            fileWriter.write(jsonString)
            fileWriter.flush()
            fileWriter.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从文件中读取JSON字符串
     */
    fun readJsonFromFile(context: Context, fileName: String): String? {
        return try {
            val file = getFile(context, fileName)
            if (!file.exists()) {
                return null
            }
            val fileReader = FileReader(file)
            val stringBuilder = StringBuilder()
            val buffer = CharArray(1024)
            var bytesRead: Int
            while (fileReader.read(buffer).also { bytesRead = it } != -1) {
                stringBuilder.append(buffer, 0, bytesRead)
            }
            fileReader.close()
            stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 检查文件是否存在
     */
    fun fileExists(context: Context, fileName: String): Boolean {
        return getFile(context, fileName).exists()
    }

    // ------------------ 旅行数据文件操作 ------------------

    /**
     * 保存旅行数据到文件
     */
    fun saveTravelData(context: Context, jsonString: String): Boolean {
        return writeJsonToFile(context, FILE_TRAVEL_DATA, jsonString)
    }

    /**
     * 读取旅行数据从文件
     */
    fun readTravelData(context: Context): String? {
        return readJsonFromFile(context, FILE_TRAVEL_DATA)
    }

    /**
     * 检查旅行数据文件是否存在
     */
    fun travelDataFileExists(context: Context): Boolean {
        return fileExists(context, FILE_TRAVEL_DATA)
    }

    // ------------------ 日记数据文件操作 ------------------

    /**
     * 保存日记数据到文件
     */
    fun saveDiaryData(context: Context, jsonString: String): Boolean {
        return writeJsonToFile(context, FILE_DIARY_DATA, jsonString)
    }

    /**
     * 读取日记数据从文件
     */
    fun readDiaryData(context: Context): String? {
        return readJsonFromFile(context, FILE_DIARY_DATA)
    }

    /**
     * 检查日记数据文件是否存在
     */
    fun diaryDataFileExists(context: Context): Boolean {
        return fileExists(context, FILE_DIARY_DATA)
    }

    // ------------------ 费用数据文件操作 ------------------

    /**
     * 保存费用数据到文件
     */
    fun saveExpenseData(context: Context, jsonString: String): Boolean {
        return writeJsonToFile(context, FILE_EXPENSE_DATA, jsonString)
    }

    /**
     * 读取费用数据从文件
     */
    fun readExpenseData(context: Context): String? {
        return readJsonFromFile(context, FILE_EXPENSE_DATA)
    }

    /**
     * 检查费用数据文件是否存在
     */
    fun expenseDataFileExists(context: Context): Boolean {
        return fileExists(context, FILE_EXPENSE_DATA)
    }

    // ------------------ 用户数据文件操作 ------------------

    /**
     * 保存用户数据到文件
     */
    fun saveUserData(context: Context, jsonString: String): Boolean {
        return writeJsonToFile(context, FILE_USER_DATA, jsonString)
    }

    /**
     * 读取用户数据从文件
     */
    fun readUserData(context: Context): String? {
        return readJsonFromFile(context, FILE_USER_DATA)
    }

    /**
     * 检查用户数据文件是否存在
     */
    fun userDataFileExists(context: Context): Boolean {
        return fileExists(context, FILE_USER_DATA)
    }
}
