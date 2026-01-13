package com.example.wonderlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderlog.data.DiaryItem
import com.example.wonderlog.utils.FileUtils
import com.example.wonderlog.utils.JsonUtils
import java.io.File

class DiaryDetailFragment : Fragment() {
    
    companion object {
        private const val ARG_DIARY_ITEM = "diary_item"
        
        fun newInstance(diaryItem: DiaryItem): DiaryDetailFragment {
            val fragment = DiaryDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_DIARY_ITEM, diaryItem)
            fragment.arguments = args
            return fragment
        }
    }
    
    private lateinit var diaryItem: DiaryItem
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTravelName: TextView
    private lateinit var rvDiaryImages: RecyclerView
    private lateinit var diaryImageAdapter: DiaryImageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            diaryItem = it.getParcelable(ARG_DIARY_ITEM)!!
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        tvTitle = view.findViewById(R.id.tv_diary_detail_title)
        tvContent = view.findViewById(R.id.tv_diary_detail_content)
        tvLocation = view.findViewById(R.id.tv_diary_detail_location)
        tvDate = view.findViewById(R.id.tv_diary_detail_date)
        tvTravelName = view.findViewById(R.id.tv_diary_detail_travel_name)
        
        // 初始化返回按钮
        val btnBack = view.findViewById<android.widget.ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // 初始化修改按钮
        val btnEdit = view.findViewById<android.widget.ImageButton>(R.id.btn_edit)
        btnEdit.setOnClickListener {
            // 跳转到修改日记页面
            val intent = android.content.Intent(activity, ModifyDiaryActivity::class.java)
            intent.putExtra("diary_item", diaryItem)
            startActivity(intent)
        }
        
        // 初始化删除按钮
        val btnDelete = view.findViewById<android.widget.ImageButton>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
        
        // 初始化图片RecyclerView
        rvDiaryImages = view.findViewById(R.id.rv_diary_images)
        
        // 加载数据
        loadDiaryData()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次页面可见时重新加载数据，确保显示最新的日记内容
        loadDiaryData()
    }
    
    /**
     * 加载日记数据
     */
    private fun loadDiaryData() {
        // 从文件中重新加载日记数据
        val diaryJson = FileUtils.readDiaryData(requireContext())
        val allDiaries = diaryJson?.let {
            JsonUtils.jsonStringToDiaryItemList(it)
        } ?: emptyList()
        
        // 查找最新的日记数据
        val latestDiary = allDiaries.find { it.id == diaryItem.id }
        if (latestDiary != null) {
            diaryItem = latestDiary
        }
        
        // 获取并设置旅行名称
        val travelName = getTravelNameById(diaryItem.travelId)
        tvTravelName.text = travelName
        
        // 设置数据
        tvTitle.text = diaryItem.title
        tvContent.text = diaryItem.content
        tvLocation.text = diaryItem.location
        tvDate.text = diaryItem.date
        
        // 初始化图片适配器
        diaryImageAdapter = DiaryImageAdapter(diaryItem.images)
        rvDiaryImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = diaryImageAdapter
        }
    }
    
    /**
     * 日记图片适配器
     */
    inner class DiaryImageAdapter(private val imagePaths: List<String>) : RecyclerView.Adapter<DiaryImageAdapter.ImageViewHolder>() {
        
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.iv_diary_image)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_image, parent, false)
            return ImageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imagePath = imagePaths[position]
            val imageFile = File(requireContext().getExternalFilesDir(null), imagePath)
            if (imageFile.exists()) {
                holder.imageView.setImageURI(android.net.Uri.fromFile(imageFile))
            }
            
            // 添加点击事件，打开大图浏览
            holder.itemView.setOnClickListener {
                val intent = Intent(requireContext(), DiaryImageDetailActivity::class.java)
                intent.putStringArrayListExtra(DiaryImageDetailActivity.EXTRA_IMAGE_PATHS, ArrayList(imagePaths))
                intent.putExtra(DiaryImageDetailActivity.EXTRA_CURRENT_POSITION, position)
                startActivity(intent)
            }
        }
        
        override fun getItemCount(): Int = imagePaths.size
    }
    
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("删除日记")
            .setMessage("您确定要删除这篇日记吗？")
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("确认") { dialog, _ ->
                deleteDiary()
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 删除日记
     */
    private fun deleteDiary() {
        // 1. 读取现有日记数据
        val diaryJson = FileUtils.readDiaryData(requireContext())
        val allDiaries = diaryJson?.let {
            JsonUtils.jsonStringToDiaryItemList(it)
        } ?: emptyList()
        
        // 2. 过滤掉要删除的日记
        val updatedDiaries = allDiaries.filter { it.id != diaryItem.id }
        
        // 3. 保存更新后的日记数据
        val updatedDiaryJson = JsonUtils.diaryItemListToJsonString(updatedDiaries)
        FileUtils.saveDiaryData(requireContext(), updatedDiaryJson)
        
        // 4. 显示删除成功提示
        android.widget.Toast.makeText(requireContext(), "日记已删除", android.widget.Toast.LENGTH_SHORT).show()
        
        // 5. 返回到日记首页（只弹出当前Fragment，返回到上一个Fragment）
        requireActivity().supportFragmentManager.popBackStack()
    }
    
    /**
     * 根据旅行ID获取旅行名称
     */
    private fun getTravelNameById(travelId: String): String {
        // 从文件中读取所有旅行数据
        val jsonString = FileUtils.readTravelData(requireContext())
        jsonString?.let {
            val travelList = JsonUtils.jsonStringToTravelItemList(it)
            // 根据ID查找旅行项
            val travelItem = travelList.find { it.id == travelId }
            return travelItem?.title ?: "未知旅行"
        }
        return "未知旅行"
    }
}