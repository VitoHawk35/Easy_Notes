package com.easynote.data.test

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.easynote.R

class Activity : ComponentActivity() {
    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        findViewById<Button>(R.id.btn).setOnClickListener { v ->
            viewModel.click()
        }
    }


}