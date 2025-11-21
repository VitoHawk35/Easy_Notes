package com.example.mydemo.data.sp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;
import com.example.mydemo.util.CreateDataFactory;

/**
 * create by WUzejian on 2025/11/18
 */
public class SharePreferenceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SharePreferenceActivity";
    private SharedPreferences mSharedPreferences;

    private TextView resultInfo;


    private Button insert;
    private Button modify;
    private Button delete;
    private Button query;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharepreference_activity);
        initView();
        //创建SharedPreferences对象
        //通常使用 Context.MODE_PRIVATE，表示该文件只能被当前应用访问
        mSharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
    }

    private void initView() {
        resultInfo = findViewById(R.id.sp_result_info);
        insert = findViewById(R.id.sp_insert);
        modify = findViewById(R.id.sp_modify);
        delete = findViewById(R.id.sp_delete);
        query = findViewById(R.id.sp_query);
        insert.setOnClickListener(this);
        modify.setOnClickListener(this);
        delete.setOnClickListener(this);
        query.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v.getId() == insert.getId()) {
            //写入数据需要通过 SharedPreferences.Editor 对象来完成，遵循三个步骤：
            //1. 调用 edit() 方法获取 Editor 实例。
            //2. 使用 putXxx() 方法（如 putString(), putInt(), putBoolean()）添加或修改数据。
            //3. 调用 apply() 或 commit() 方法提交更改。
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("id", CreateDataFactory.generate4DigitId());
            editor.putString("username", CreateDataFactory.getUserName());
            editor.putString("phone", CreateDataFactory.generateRandomPhones());
            editor.putBoolean("isVIP", true);
            editor.apply(); // 异步提交
//        editor.commit(); // 同步提交
            // 插入数据
            resultInfo.setText("插入数据成功\n id: " + CreateDataFactory.generate4DigitId() + ", username: " + CreateDataFactory.getUserName() + ", phone: " + CreateDataFactory.generateRandomPhones() + ", isVIP: " + true);
        } else if (v.getId() == modify.getId()) {
            // 读取修改前数据
            int id = mSharedPreferences.getInt("id", 0);
            String username = mSharedPreferences.getString("username", "default_value");
            String phone = mSharedPreferences.getString("phone", "default_value");
            boolean isVip = mSharedPreferences.getBoolean("isVIP", false);
            String modifyInfo = "修改前数据\n id: " + id + ", username: " + username + ", phone: " + phone + ", isVIP: " + isVip;
            // 修改数据
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("id", CreateDataFactory.generate4DigitId());
            editor.putString("username", CreateDataFactory.getUserName());
            editor.putString("phone", CreateDataFactory.generateRandomPhones());
            editor.putBoolean("isVIP", false);
            editor.apply(); // 异步提交
//        editor.commit(); // 同步提交
            resultInfo.setText("修改数据成功\n " + modifyInfo + "\n修改后数据：\nid: " + CreateDataFactory.generate4DigitId() + ", username: " + CreateDataFactory.getUserName() + ", phone: " + CreateDataFactory.generateRandomPhones() + ", isVIP: " + false);
        } else if (v.getId() == delete.getId()) {
            // 删除数据
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove("id");
            editor.remove("username");
            editor.remove("phone");
            editor.remove("isVIP");
            editor.apply(); // 异步提交
//        editor.commit(); // 同步提交
            resultInfo.setText("删除数据成功");
        } else if (v.getId() == query.getId()) {
            // 读取数据
            int id = mSharedPreferences.getInt("id", 0);
            String username = mSharedPreferences.getString("username", "default_value");
            String phone = mSharedPreferences.getString("phone", "default_value");
            boolean isVip = mSharedPreferences.getBoolean("isVIP", false);
            resultInfo.setText("读取数据成功\n id: " + id + ", username: " + username + ", phone: " + phone + ", isVIP: " + isVip);
        }
    }
}
