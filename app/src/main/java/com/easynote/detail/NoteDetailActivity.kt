package com.easynote.detail

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        initData()
        initView()
        initListeners()
        initAi()
    }
    // 注册相册选择器
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // 拿到图片后，插入到【当前页】
            insertImageToCurrentPage(it)
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
            addImage = {
                // 打开相册
                pickImageLauncher.launch("image/*")
            },

            // 2. 处理“保存”请求
            save = { position, html ->
                saveNoteToDisk(position, html)
            }
        )



        viewPager.adapter = pagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        pagerAdapter.setReadOnlyMode(isReadOnly)

        etTitle.setText("计算机组成原理笔记")

        updateModeState()
    }

    /**
     * 保存具体的某一页到本地文件
     */
    private fun saveNoteToDisk(position: Int, html: String) {
        // 使用协程进行 IO 操作
        lifecycleScope.launch {
            try {
                // 生成文件名，例如：note_1_page_0.html
                // 实际项目中建议使用 noteId 和 pageId 组合
                val fileName = "note_${System.currentTimeMillis()}_page_$position.html"

                NoteSaver.saveNote(this@NoteDetailActivity, html, fileName)

                Toast.makeText(this@NoteDetailActivity, "第 ${position + 1} 页已保存", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@NoteDetailActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun insertImageToCurrentPage(uri: Uri) {
        val recyclerView = viewPager.getChildAt(0) as RecyclerView

        // 找到当前显示的那个 ViewHolder
        val currentPosition = viewPager.currentItem
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(currentPosition) as? NotePagerAdapter.PageViewHolder

        if (viewHolder != null) {
            // 找到了！调用那一页 RichTextView 的插入方法
            viewHolder.richEditor.insertImage(uri)
        } else {
            Toast.makeText(this, "未找到当前页面，请重试", Toast.LENGTH_SHORT).show()
        }
    }
}