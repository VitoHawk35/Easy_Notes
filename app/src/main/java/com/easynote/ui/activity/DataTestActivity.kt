package com.easynote.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydemo.R
import com.easynote.ui.adapter.DataTestAdapter
import com.easynote.ui.viewmodel.DataTestActivityViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataTestActivity : AppCompatActivity() {
    private val viewModel: DataTestActivityViewModel by viewModels()

    private val adapter: DataTestAdapter

    private val recycleView: RecyclerView

    init {
        adapter = DataTestAdapter(DataTestAdapter.DiffCallback)
        recycleView = findViewById(R.id.recycle_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_test)


        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModel.getFlow().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

    }
}