package com.easynote.detail

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.easynote.R // 如果包名不同，这里可能会变，按 Alt+Enter 导入
import com.easynote.detail.adapter.NotePagerAdapter
import com.easynote.detail.data.model.NotePage

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

        pagerAdapter = NotePagerAdapter(pageList)
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