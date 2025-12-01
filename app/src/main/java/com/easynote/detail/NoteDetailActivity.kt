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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.easynote.R // 如果包名不同，这里可能会变，按 Alt+Enter 导入
import com.easynote.detail.adapter.NotePagerAdapter
import com.easynote.detail.data.model.NotePage
import com.easynote.richtext.utils.NoteSaver
import com.easynote.richtext.view.RichTextView
import com.example.mydemo.ai.service.AIConfig
import kotlinx.coroutines.launch

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var etTitle: EditText
    private lateinit var btnHome: ImageView
    private lateinit var btnMore: ImageView
    private lateinit var btnModeToggle: LinearLayout

    private val pageList = mutableListOf<NotePage>()
    private lateinit var pagerAdapter: NotePagerAdapter
    private var isReadOnly = true // 默认只读

    // 1. 获取 ViewModel
    private val viewModel: NoteDetailViewModel by viewModels()

    // 1. 定义一个暂存回调的变量
    private var pendingImageCallback: ((Uri) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        initData()
        initView()
        initListeners()
        initAi()
    }
    // 注册相册选择器
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { srcUri: Uri? ->
        if (srcUri != null) {
            // 假设当前 noteId=1 (实际开发中请从 Intent 获取)
            val currentNoteId = 1L
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

    private fun initAi(){
        try {
            AIConfig.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initData() {
        pageList.add(NotePage(1, 1, "第一页：左右滑动试试。"))
        pageList.add(NotePage(2, 2, "第二页：点击右上角更多按钮可以加页。"))
    }

    private fun initView() {
        viewPager = findViewById(R.id.viewPager)
        etTitle = findViewById(R.id.edTitle)
        btnHome = findViewById(R.id.btnHome)

        btnMore = findViewById(R.id.btmMore)

        btnModeToggle = findViewById(R.id.btmModeToggle)

        //
        pagerAdapter = NotePagerAdapter(
            pages = pageList,

            // 1. 处理“插入图片”请求
            addImage = { callback ->
                // 保存 View 层传来的回调
                this.pendingImageCallback = callback
                // 打开相册
                pickImageLauncher.launch("image/*")
            },

            // 2. 处理“保存”请求
            save = { position, html ->
                // 假设当前 Note ID 为 1 (实际应从 Intent 获取)
                val currentNoteId = 1L
                val currentPageIndex = pageList[position].pageNumber // 或者直接用 position + 1

                viewModel.saveNotePage(currentNoteId, currentPageIndex, html)

                // 只是简单的 UI 反馈
                Toast.makeText(this, "第 ${position + 1} 页正在保存...", Toast.LENGTH_SHORT).show()
            },

            onAiRequest = { text, taskType, viewCallback ->
                // 1. 显示 Loading (Activity 负责 UI 反馈)
                Toast.makeText(this, "AI 思考中...", Toast.LENGTH_SHORT).show()

                // 2. 调用 ViewModel
                viewModel.performAiTask(
                    text,
                    taskType,
                    onResult = { resultText ->
                        // 3. 拿到结果，传回给 View (闭包的威力)
                        viewCallback(resultText)
                    },
                    onError = { errorMsg ->
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )



        viewPager.adapter = pagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        pagerAdapter.setReadOnlyMode(isReadOnly)

        etTitle.setText("计算机组成原理笔记")

        updateModeState()
    }



    private fun initListeners() {
        btnHome.setOnClickListener {
            finish()
        }

        btnMore.setOnClickListener {
            addNewPage()
        }

        btnModeToggle.setOnClickListener {
            isReadOnly = !isReadOnly
            updateModeState()
        }

        etTitle.setOnClickListener {
            if (isReadOnly) {
                Toast.makeText(this, "当前为只读模式", Toast.LENGTH_SHORT).show()
            }
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
            etTitle.isEnabled = false
            pagerAdapter.setReadOnlyMode(true)
        } else {
            tvStatus.text = "编辑"
            etTitle.isEnabled = true
            pagerAdapter.setReadOnlyMode(false)
        }

        // 通知 Adapter 刷新数据的逻辑...
    }

}