package com.example.wonderlog

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wonderlog.data.DiaryItem
import com.example.wonderlog.data.TravelItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import com.example.wonderlog.utils.SharedPreferencesUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.ImageDecoder

class NewDiaryActivity : AppCompatActivity() {
    
    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var etDiaryTitle: EditText
    private lateinit var spinnerTravel: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var etLocation: EditText
    private lateinit var etDiaryContent: EditText
    private lateinit var btnAddImage: Button
    private lateinit var llImagesContainer: LinearLayout
    
    // 旅行数据
    private lateinit var travelList: List<TravelItem>
    private lateinit var travelAdapter: ArrayAdapter<String>
    private var selectedTravelId: String = ""
    private var selectedTravelStartDate: Date? = null
    private var selectedTravelEndDate: Date? = null
    
    // 图片相关
    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSION_REQUEST_CODE = 2
    private val MAX_IMAGES = 9
    private var imageUris: MutableList<Uri> = mutableListOf()
    private var imageFilePaths: MutableList<String> = mutableListOf()
    
    // 图片存储目录
    private val DIARY_IMAGES_DIR = "diary_images"
    
    // 日期相关
    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_diary)
        
        // 初始化视图
        initViews()
        
        // 加载旅行数据
        loadTravelData()
        
        // 设置默认日期为当天
        selectedDate = dateFormat.format(Date())
        btnSelectDate.text = selectedDate
        
        // 设置点击事件
        setClickListeners()
    }
    
    /**
     * 初始化视图
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSave = findViewById(R.id.btn_save)
        etDiaryTitle = findViewById(R.id.et_diary_title)
        spinnerTravel = findViewById(R.id.spinner_travel)
        btnSelectDate = findViewById(R.id.btn_select_date)
        etLocation = findViewById(R.id.et_location)
        etDiaryContent = findViewById(R.id.et_diary_content)
        btnAddImage = findViewById(R.id.btn_add_image)
        llImagesContainer = findViewById(R.id.ll_images_container)
    }
    
    /**
     * 加载旅行数据
     */
    private fun loadTravelData() {
        // 获取当前登录用户ID
        val currentUserId = SharedPreferencesUtils.getCurrentUserId(this) ?: "user_1"
        
        // 从JSON文件中读取所有旅行数据
        travelList = if (FileUtils.travelDataFileExists(this)) {
            val jsonString = FileUtils.readTravelData(this)
            jsonString?.let {
                JsonUtils.jsonStringToTravelItemList(it).filter { travelItem -> 
                    travelItem.userId == currentUserId
                }
            } ?: emptyList()
        } else {
            emptyList()
        }
        
        // 如果没有旅行记录，提示用户并关闭Activity
        if (travelList.isEmpty()) {
            Toast.makeText(this, R.string.error_create_trip_first, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 准备旅行名称列表用于Spinner
        val travelNames = travelList.map { it.title }
        
        // 创建并设置Spinner适配器
        travelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, travelNames)
        travelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTravel.adapter = travelAdapter
        
        // 设置Spinner选中监听器
        spinnerTravel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTravelId = travelList[position].id
                
                // 解析所选旅行的开始日期和结束日期
                val travelItem = travelList[position]
                // 兼容中英文日期分隔符
                val dateRange = if (travelItem.date.contains(" to ")) {
                    travelItem.date.split(" to ")
                } else {
                    travelItem.date.split(" 至 ")
                }
                
                if (dateRange.size == 2) {
                    try {
                        selectedTravelStartDate = dateFormat.parse(dateRange[0])
                        selectedTravelEndDate = dateFormat.parse(dateRange[1])
                        
                        // 如果当前选中的日记日期不在旅行日期范围内，重置为旅行开始日期
                        if (selectedDate.isNotEmpty()) {
                            val currentDiaryDate = dateFormat.parse(selectedDate)
                            if (currentDiaryDate != null) {
                                if ((selectedTravelStartDate != null && currentDiaryDate.before(selectedTravelStartDate)) ||
                                    (selectedTravelEndDate != null && currentDiaryDate.after(selectedTravelEndDate))) {
                                    selectedDate = dateRange[0]
                                    btnSelectDate.text = selectedDate
                                    Toast.makeText(this@NewDiaryActivity, R.string.diary_date_reset, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // 如果还没有选择日记日期，设置为旅行开始日期
                            selectedDate = dateRange[0]
                            btnSelectDate.text = selectedDate
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        selectedTravelStartDate = null
                        selectedTravelEndDate = null
                    }
                } else {
                    selectedTravelStartDate = null
                    selectedTravelEndDate = null
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 没有选中项时的处理
                selectedTravelStartDate = null
                selectedTravelEndDate = null
            }
        }
        
        // 默认选中第一个旅行
        if (travelList.isNotEmpty()) {
            selectedTravelId = travelList[0].id
        }
    }
    
    /**
     * 设置点击事件
     */
    private fun setClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }
        
        // 保存按钮
        btnSave.setOnClickListener {
            saveDiary()
        }
        
        // 日期选择按钮
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        // 添加图片按钮
        btnAddImage.setOnClickListener {
            // 检查是否已达到最大图片数量
            if (imageUris.size >= MAX_IMAGES) {
                Toast.makeText(this, R.string.error_max_images, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 检查权限
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }
    }
    
    /**
     * 显示日期选择器
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // 如果已选择日期，则设置为已选择日期
        if (selectedDate.isNotEmpty()) {
            try {
                calendar.time = dateFormat.parse(selectedDate)!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // 使用系统日期选择器
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                selectedDate = dateFormat.format(selectedCalendar.time)
                btnSelectDate.text = selectedDate
            },
            year,
            month,
            day
        )
        
        // 设置日期选择器的最小和最大可选日期为所选旅行的开始和结束日期
        selectedTravelStartDate?.let {
            datePickerDialog.datePicker.minDate = it.time
        }
        selectedTravelEndDate?.let {
            datePickerDialog.datePicker.maxDate = it.time
        }
        
        datePickerDialog.show()
    }
    
    /**
     * 检查存储权限
     */
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上，使用READ_MEDIA_IMAGES权限
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12及以下，使用READ_EXTERNAL_STORAGE权限
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 请求存储权限
     */
    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * 权限请求结果处理
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，打开图片选择器
                openImagePicker()
            } else {
                // 权限拒绝，显示提示
                Toast.makeText(this, R.string.permission_storage_images, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 打开图片选择器
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    
    /**
     * 处理图片选择结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedUri = data.data!!
            
            try {
                // 保存图片并获取文件路径
                val imagePath = saveDiaryImage(selectedUri)
                if (imagePath.isNotEmpty()) {
                    // 添加到列表
                    imageUris.add(selectedUri)
                    imageFilePaths.add(imagePath)
                    
                    // 更新图片预览
                    updateImagesPreview()
                    Toast.makeText(this, R.string.image_added, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_image_process, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 保存日记图片到文件
     */
    private fun saveDiaryImage(uri: Uri): String {
        return try {
            // 解码图片并进行缩放
            val bitmap = decodeSampledBitmapFromUri(uri, 800, 800)
            
            // 创建图片存储目录
            val imagesDir = File(getExternalFilesDir(null), DIARY_IMAGES_DIR)
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // 创建图片文件
            val imageFile = File(imagesDir, "diary_image_${UUID.randomUUID()}.png")
            
            // 保存图片
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
            
            // 返回相对路径（用于存储在JSON中）
            return "${DIARY_IMAGES_DIR}/${imageFile.name}"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * 从Uri解码并缩放Bitmap
     */
    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 使用现代API ImageDecoder（Android P及以上）
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) {
                decoder, info, source ->
                // 设置解码选项，包括缩放
                val scale = Math.min(
                    info.size.width.toFloat() / reqWidth,
                    info.size.height.toFloat() / reqHeight
                )
                decoder.setTargetSize(
                    (info.size.width / scale).toInt(),
                    (info.size.height / scale).toInt()
                )
            }
            bitmap
        } else {
            // 兼容旧版本API
            BitmapFactory.Options().run {
                inJustDecodeBounds = true
                contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
                
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                
                contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
            } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }
    
    /**
     * 计算合适的采样率
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // 原始宽高
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            // 计算最大的inSampleSize，使得宽高都大于等于请求的宽高
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 更新图片预览
     */
    private fun updateImagesPreview() {
        // 清除现有预览
        llImagesContainer.removeAllViews()
        
        // 创建新的图片预览
        for (i in imageUris.indices) {
            val imageUri = imageUris[i]
            
            // 创建图片预览布局
            val previewLayout = LinearLayout(this)
            previewLayout.orientation = LinearLayout.HORIZONTAL
            previewLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            previewLayout.setPadding(0, 0, 0, 8)
            
            // 创建ImageView
            val imageView = ImageView(this)
            val imageLayoutParams = LinearLayout.LayoutParams(
                120,
                120
            )
            imageLayoutParams.rightMargin = 8
            imageView.layoutParams = imageLayoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(imageUri)
            
            // 创建删除按钮
            val deleteButton = Button(this)
            val deleteLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            deleteLayoutParams.gravity = android.view.Gravity.CENTER_VERTICAL
            deleteButton.layoutParams = deleteLayoutParams
            deleteButton.text = getString(R.string.delete)
            deleteButton.setBackgroundTintList(resources.getColorStateList(R.color.primary, theme))
            deleteButton.setTextColor(resources.getColor(android.R.color.white, theme))
            deleteButton.textSize = 12f
            deleteButton.tag = i // 保存索引用于删除
            
            // 设置删除按钮点击事件
            deleteButton.setOnClickListener {
                val index = it.tag as Int
                removeImage(index)
            }
            
            // 添加到布局
            previewLayout.addView(imageView)
            previewLayout.addView(deleteButton)
            llImagesContainer.addView(previewLayout)
        }
    }
    
    /**
     * 删除图片
     */
    private fun removeImage(index: Int) {
        if (index >= 0 && index < imageUris.size) {
            imageUris.removeAt(index)
            imageFilePaths.removeAt(index)
            updateImagesPreview()
            Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 保存日记
     */
    private fun saveDiary() {
        // 获取表单数据
        val title = etDiaryTitle.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val content = etDiaryContent.text.toString().trim()
        
        // 验证表单
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.hint_diary_title, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedTravelId.isEmpty()) {
            Toast.makeText(this, R.string.error_select_trip, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, R.string.error_select_diary_date, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (location.isEmpty()) {
            Toast.makeText(this, R.string.hint_location, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (content.isEmpty()) {
            Toast.makeText(this, R.string.hint_diary_content, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建日记项
        val diaryItem = DiaryItem(
            id = "diary_${UUID.randomUUID()}",
            travelId = selectedTravelId,
            title = title,
            content = content,
            location = location,
            date = selectedDate,
            images = imageFilePaths
        )
        
        // 保存到文件
        saveDiaryToFile(diaryItem)
    }
    
    /**
     * 保存日记到文件
     */
    private fun saveDiaryToFile(diaryItem: DiaryItem) {
        // 读取现有日记数据
        val allDiaries = if (FileUtils.diaryDataFileExists(this)) {
            val jsonString = FileUtils.readDiaryData(this)
            jsonString?.let {
                JsonUtils.jsonStringToDiaryItemList(it)
            } ?: emptyList()
        } else {
            emptyList()
        }
        
        // 添加新日记
        val updatedDiaries = allDiaries + diaryItem
        
        // 保存到文件
        val jsonString = JsonUtils.diaryItemListToJsonString(updatedDiaries)
        val saveSuccess = FileUtils.saveDiaryData(this, jsonString)
        
        if (saveSuccess) {
            Toast.makeText(this, R.string.diary_save_success, Toast.LENGTH_SHORT).show()
            finish() // 保存成功后返回
        } else {
            Toast.makeText(this, R.string.error_diary_save_failed, Toast.LENGTH_SHORT).show()
        }
    }
}