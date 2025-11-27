package com.easynote.detail

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.easynote.R
import com.easynote.detail.adapter.NotePagerAdapter
import com.easynote.detail.data.model.NotePage
import com.easynote.detail.viewmodel.NoteDetailViewModel

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var etTitle: EditText
    private lateinit var btnHome: ImageView
    private lateinit var btnMore: ImageView
    private lateinit var btnModeToggle: LinearLayout

    private val pageList = mutableListOf<NotePage>()
    private lateinit var pagerAdapter: NotePagerAdapter
    private var isReadOnly = true

    private val viewModel: NoteDetailViewModel by viewModels()
    private var currentNoteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        currentNoteId = intent.getIntExtra("NOTE_ID", -1)
//        currentNoteId = 106

        initView()
        initData()
        initListeners()
    }

    private fun initData() {
        if (currentNoteId != -1) {
            // === 情况 A：编辑旧笔记 ===
            // 观察 ViewModel 里的数据库查询结果
            viewModel.getNoteById(currentNoteId).observe(this) { entity ->
                // 注意：数据库查询是异步的，一开始 entity 可能是 null
                if (entity != null) {
                    // 1. 回填标题
                    etTitle.setText(entity.title)

                    // 2. 让 ViewModel 帮忙解析 JSON 变回 List<NotePage>
                    val savedPages = viewModel.parsePagesFromJson(entity.content)

                    // 3. 刷新列表
                    pageList.clear()
                    pageList.addAll(savedPages)
                    pagerAdapter.notifyDataSetChanged()
                }
            }
        } else {
            // === 情况 B：新建笔记 ===
            // 默认添加一张空白页
            pageList.add(NotePage(System.currentTimeMillis(), 1, ""))
            pagerAdapter.notifyDataSetChanged()
        }
    }

    private fun initView() {
        viewPager = findViewById(R.id.viewPager)
        etTitle = findViewById(R.id.edTitle)
        btnHome = findViewById(R.id.btnHome)

        btnMore = findViewById(R.id.btmMore)

        btnModeToggle = findViewById(R.id.btmModeToggle)

        pagerAdapter = NotePagerAdapter(pageList)
        viewPager.adapter = pagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        pagerAdapter.setReadOnlyMode(isReadOnly)

        etTitle.setText("未命名的笔记")

        updateModeState()
    }

    private fun initListeners() {
        btnHome.setOnClickListener {
//            finish()
            saveData()
            Toast.makeText(this, "尝试写入数据库...", Toast.LENGTH_SHORT).show()
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

    private fun saveData() {
        // 如果当前焦点在输入框，先清除焦点以确保内容写入 Model
        currentFocus?.clearFocus()

        val title = etTitle.text.toString()

        // 简单防呆：如果没有标题且所有页面都是空的，就不保存空文件了
        // (你可以根据需求去掉这个判断)
        val isContentEmpty = pageList.all { it.content.isBlank() }
        if (title.isBlank() && isContentEmpty) {
            return
        }

        // 调用 ViewModel 进行保存
        viewModel.saveNote(currentNoteId, title, pageList)
    }

    // 6. 【新增】生命周期回调：当页面暂停/退出时自动保存
    override fun onPause() {
        super.onPause()
        saveData()
    }

}