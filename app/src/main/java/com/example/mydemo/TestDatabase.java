package com.example.mydemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydemo.ui.viewmodel.MainActivityViewModel;

/**
 * create by WUzejian on 2025/11/16
 */
public class TestDatabase extends AppCompatActivity {
    private MainActivityViewModel viewModel;
    private EditText etTitle;
    private EditText etContent;
    private View btn_addNote;
    private View btn_readNote;
    private Button btn_addTag;
    private Button btn_readTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);


        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btn_addNote = findViewById(R.id.btn_addNote);
        btn_addTag = findViewById(R.id.btn_addTag);
        btn_readNote = findViewById(R.id.btn_readNote);
        btn_readTag = findViewById(R.id.btn_readTag);

        btn_addNote.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();
            viewModel.setTitleAndContent(title, content);
        });

        btn_addTag.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();
            viewModel.setTagName(title, content);
        });

        btn_readNote.setOnClickListener(v -> {
            viewModel.getNote().observe(this, noteEntities -> {
                etTitle.setText(noteEntities.get(0).getTitle());
                etContent.setText(noteEntities.get(0).getContent());
            });
        });

        btn_readTag.setOnClickListener(v->{
            viewModel.getTag().observe(this, tagEntities -> {
                etTitle.setText(tagEntities.get(0).getName());
                etContent.setText(tagEntities.get(0).getColor());
            });
        });
    }
}
