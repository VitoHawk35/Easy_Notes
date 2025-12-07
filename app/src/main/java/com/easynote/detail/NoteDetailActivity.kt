package com.easynote.detail

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.easynote.R
import com.easynote.detail.adapter.NotePagerAdapter
import com.easynote.detail.data.model.NotePage
import com.easynote.detail.viewmodel.NoteDetailViewModel
import android.content.Intent
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.easynote.detail.adapter.NavAdapter
import com.easynote.detail.adapter.TagPagingAdapter
import com.easynote.ai.service.AIConfig
import androidx.lifecycle.lifecycleScope
import com.easynote.ai.core.TaskType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.easynote.data.entity.TagEntity
import androidx.core.widget.addTextChangedListener
class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var etTitle: EditText
    private lateinit var btnHome: ImageView
    private lateinit var btnMore: ImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnModeToggle: LinearLayout
    private lateinit var btmThumbnail: ImageView
    private val pageList = mutableListOf<NotePage>()
    private lateinit var pagerAdapter: NotePagerAdapter
    private var isReadOnly = true

    private val viewModel: NoteDetailViewModel by viewModels()
    private var currentNoteId: Long = 1L
    private var noteTitle: String = "未命名笔记"
    private lateinit var ivTag: ImageView
    private var currentTags = hashSetOf<TagEntity>()

    private var isDataChanged = false

    private var pendingImageCallback: ((Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)
        currentNoteId = intent.getLongExtra("NOTE_ID", -1L)
        noteTitle = intent.getStringExtra("NOTE_TITLE") ?: "未命名笔记"
        initView()
        initData()
        initListeners()
        initAi()
    }

    private fun initAi(){
        try {
            AIConfig.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initData() {
        viewModel.notePages.observe(this) { pages ->
            pageList.clear()
            pageList.addAll(pages)
            pagerAdapter.notifyDataSetChanged()
        }

        viewModel.noteTitle.observe(this) { title ->
            etTitle.setText(title)
        }

        viewModel.noteTags.observe(this) { tags ->
            currentTags.clear()
            currentTags.addAll(tags)
            val firstTagName = tags.firstOrNull()?.name ?: ""
            updateTagIconColor(firstTagName)
        }

        if (currentNoteId != -1L) {
            viewModel.loadNoteContent(currentNoteId)
        } else {
            pageList.add(NotePage(System.currentTimeMillis(), 1, ""))
            pagerAdapter.notifyDataSetChanged()
        }
    }

    private fun initView() {
        viewPager = findViewById(R.id.viewPager)
        etTitle = findViewById(R.id.edTitle)
        btnHome = findViewById(R.id.btnHome)
        btmThumbnail = findViewById(R.id.btmThumbnail)
        btnMore = findViewById(R.id.btmMore)

        btnModeToggle = findViewById(R.id.btmModeToggle)
        btnShare = findViewById(R.id.btmShare)
        ivTag = findViewById(R.id.ivTag)

        viewModel.currentTitle = noteTitle

        etTitle.addTextChangedListener { text ->
            if (!isDataChanged && text.toString() != noteTitle) {
                isDataChanged = true
            }
            viewModel.currentTitle = text.toString()
        }

        pagerAdapter = NotePagerAdapter(
            pages = pageList,

            addImage = { callback ->
                this.pendingImageCallback = callback
                pickImageLauncher.launch("image/*")
            },

            save = { position, html ->
                val currentPageIndex = pageList[position].pageNumber

                viewModel.saveNotePage(currentNoteId, currentPageIndex, html)

                Toast.makeText(this, "第 ${position + 1} 页正在保存...", Toast.LENGTH_SHORT).show()
            },

            onAiRequest = { text, taskType, context, viewCallback ->
                Toast.makeText(this, "AI 思考中...", Toast.LENGTH_SHORT).show()

                if (taskType == TaskType.TRANSLATE && context != null) {
                    viewModel.performTranslateTask(
                        context = context,
                        text = text,
                        onResult = viewCallback,
                        onError = { errorMsg ->
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    viewModel.performAiTask(
                        text,
                        taskType,
                        onResult = viewCallback,
                        onError = { errorMsg ->
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },

            onUpdateAbstract = { abstractText ->
                viewModel.updateAbstract(currentNoteId, abstractText)

                Toast.makeText(this, "摘要已更新", Toast.LENGTH_SHORT).show()
            }
        )



        viewPager.adapter = pagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        pagerAdapter.setReadOnlyMode(isReadOnly)

        etTitle.setText(noteTitle)

        updateModeState()
    }

    private fun initListeners() {
        btnHome.setOnClickListener {
            finish()
//            saveData()
//            Toast.makeText(this, "保存", Toast.LENGTH_SHORT).show()
        }

        btmThumbnail.setOnClickListener {
            showNavigationDialog()
        }

        btnMore.setOnClickListener {
//            addNewPage()
            showPageManageMenu()
        }

        btnModeToggle.setOnClickListener {
            isReadOnly = !isReadOnly
            updateModeState()
        }

//        etTitle.setOnClickListener {
//            if (isReadOnly) {
//                Toast.makeText(this, "当前为只读模式", Toast.LENGTH_SHORT).show()
//            }
//        }

        btnShare.setOnClickListener {
            exportNoteText()
        }

        ivTag.setOnClickListener {
            showTagSelectionDialog()
        }
    }

    private fun addNewPage() {
        val newIndex = pageList.size + 1
        val newPage = NotePage(System.currentTimeMillis(), newIndex, "")
        pageList.add(newPage)

        pagerAdapter.notifyItemInserted(pageList.size - 1)

        viewPager.currentItem = pageList.size - 1

        Toast.makeText(this, "已添加第 $newIndex 页", Toast.LENGTH_SHORT).show()
    }

    private fun updateModeState() {
        val tvStatus = btnModeToggle.getChildAt(1) as TextView

        if (isReadOnly) {
            tvStatus.text = "只读"
//            etTitle.isEnabled = false
            pagerAdapter.setReadOnlyMode(true)
        } else {
            tvStatus.text = "编辑"
//            etTitle.isEnabled = true
            pagerAdapter.setReadOnlyMode(false)
        }
        etTitle.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun exportNoteText() {
        currentFocus?.clearFocus()

        val sb = StringBuilder()
        val title = etTitle.text.toString().ifBlank { "无标题笔记" }
        sb.append("【$title】\n\n")

        var hasContent = false
        pageList.forEachIndexed { index, page ->
            if (page.content.isNotBlank()) {
                hasContent = true
                if (pageList.size > 1) {
                    sb.append("--- 第 ${index + 1} 页 ---\n")
                }
                sb.append(page.content)
                sb.append("\n\n")
            }
        }

        val finalContent = sb.toString()

        if (!hasContent && etTitle.text.isBlank()) {
            Toast.makeText(this, "笔记内容为空，无法分享", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, finalContent)
        }

        startActivity(Intent.createChooser(intent, "将笔记分享到..."))
    }

    private fun showNavigationDialog() {
        currentFocus?.clearFocus()

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.detail_dialog_nav_container, null)

        val recyclerView: RecyclerView = view.findViewById(R.id.rvNavigation)

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = NavAdapter(pageList) { position ->

            viewPager.setCurrentItem(position, false)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showTagSelectionDialog() {
        currentFocus?.clearFocus()
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.detail_tag_selector, null)

        val tempSelectedTags = HashSet(currentTags)

        val rvTags: RecyclerView = view.findViewById(R.id.rvTags)
        val btnConfirm: android.widget.Button = view.findViewById(R.id.btnConfirmTags)

        rvTags.layoutManager = LinearLayoutManager(this)

        val pagingAdapter = TagPagingAdapter(tempSelectedTags)
        rvTags.adapter = pagingAdapter

        lifecycleScope.launch {
            viewModel.allTagsFlow.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

        btnConfirm.setOnClickListener {
            currentTags.clear()
            currentTags.addAll(tempSelectedTags)

            val msg = if (currentTags.isEmpty()) "未选择标签" else "已选: ${currentTags.joinToString(",")}"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

            val firstTagName = currentTags.firstOrNull()?.name ?: ""
            updateTagIconColor(firstTagName)

            if (currentNoteId != -1L) {
                viewModel.updateNoteTags(currentNoteId, currentTags.toList())
            } else {
                Log.d("NoteDetail", "新建笔记，标签暂时保存在内存中，尚未写入数据库")
            }
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateTagIconColor(tag: String) {
        val targetEntity = currentTags.find { it.name == tag }

        val colorStr = targetEntity?.color

        val color = try {
            if (!colorStr.isNullOrBlank()) {
                android.graphics.Color.parseColor(colorStr)
            } else {
                android.graphics.Color.GRAY
            }
        } catch (e: Exception) {
            android.graphics.Color.GRAY
        }

        ivTag.setColorFilter(color)
    }


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { srcUri: Uri? ->
        if (srcUri != null) {

            //val currentNoteId = 1L
            // 获取当前页码
            val currentPageIndex = pageList[viewPager.currentItem].pageNumber

            // ViewModel保存图片
            viewModel.saveImage(currentNoteId, currentPageIndex, srcUri) { localUri ->

                // 1. ViewModel 保存成功，回调返回本地路径 (file://...)
                // 2. 将这个本地路径传给 RichTextView 进行显示
                pendingImageCallback?.invoke(localUri)

                // 3. 清理引用
                pendingImageCallback = null
            }
        } else {
            pendingImageCallback = null
        }
    }

    private fun saveData() {
        if (currentNoteId == -1L) return

        if (!isDataChanged) {
            Log.d("NoteDetailActivity", "数据未修改，跳过保存")
            return
        }

        currentFocus?.clearFocus()

        val tagsToList = currentTags.toList()

        viewModel.saveNote(currentNoteId, pageList, tagsToList)
    }

    private fun showPageManageMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(this, btnMore)

        popup.menu.add(0, 1, 0, "添加一页")
        popup.menu.add(0, 2, 0, "删除当前页")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    addNewPage()
                    true
                }
                2 -> {
                    deleteCurrentPage()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun deleteCurrentPage() {
        if (pageList.size <= 1) {
            Toast.makeText(this, "已经是最后一页了，无法删除", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPosition = viewPager.currentItem

        pageList.removeAt(currentPosition)

        pageList.forEachIndexed { index, page ->
            page.pageNumber = index + 1
        }

        pagerAdapter.notifyItemRemoved(currentPosition)
        pagerAdapter.notifyItemRangeChanged(currentPosition, pageList.size)

        isDataChanged = true

        Toast.makeText(this, "已删除第 ${currentPosition + 1} 页", Toast.LENGTH_SHORT).show()
    }
}