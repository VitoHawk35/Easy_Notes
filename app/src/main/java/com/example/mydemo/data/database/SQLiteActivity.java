package com.example.mydemo.data.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;
import com.example.mydemo.util.CreateDataFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * create by WUzejian on 2025/11/18
 */
public class SQLiteActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView databaseInfo;
    private TextView resultInfo;


    private Button insert;
    private Button upgrade;
    private Button modify;
    private Button delete;
    private Button query;
    private Button delete_database;

    private SQLiteDatabase writableDatabase;
    private MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqllite);
        initView();
        //创建DatabaseHelper对象
        // 只执行这句话是不会创建或打开连接的
        databaseHelper = new MyDatabaseHelper(this);
        writableDatabase = databaseHelper.getWritableDatabase();//打开可读可写数据库连接
        String dataInfo = String.format(Locale.getDefault(),
                "数据库版本：%d\n数据库路径：%s",
                writableDatabase.getVersion(),
                databaseHelper.getDatabaseName());
        databaseInfo.setText(dataInfo);
    }

    private void initView() {
        databaseInfo = findViewById(R.id.database_info);
        resultInfo = findViewById(R.id.result_info);
        insert = findViewById(R.id.insert);
        upgrade = findViewById(R.id.upgrade);
        modify = findViewById(R.id.modify);
        delete = findViewById(R.id.delete);
        query = findViewById(R.id.query);
        delete_database = findViewById(R.id.delete_database);
        insert.setOnClickListener(this);
        upgrade.setOnClickListener(this);
        modify.setOnClickListener(this);
        delete.setOnClickListener(this);
        query.setOnClickListener(this);
        delete_database.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        int id = v.getId(); // 获取点击控件的ID
        if (id == R.id.insert) {
            // 插入数据
            ContentValues values = new ContentValues();
            values.put(MyDatabaseHelper.COLUMN_ID, CreateDataFactory.generate4DigitId());
            values.put(MyDatabaseHelper.COLUMN_NAME, CreateDataFactory.getUserName());
            values.put(MyDatabaseHelper.COLUMN_CONTENT, CreateDataFactory.getDesc());
            values.put(MyDatabaseHelper.COLUMN_PHONE, CreateDataFactory.generateRandomPhones());
            values.put(MyDatabaseHelper.COLUMN_CREATED_AT, getCurrentTime());
            long result = writableDatabase.insert(MyDatabaseHelper.TABLE_NOTES, null, values);
            resultInfo.setText("插入结果：" + (result > 0 ? "成功" : "失败") + ",用户信息：\n" + values.toString());
        } else if (id == R.id.upgrade) {
            // 处理升级逻辑
            writableDatabase.setVersion(2);
            String dataInfo = String.format(Locale.getDefault(),
                    "数据库版本：%d\n数据库路径：%s",
                    writableDatabase.getVersion(),
                    databaseHelper.getDatabaseName());
            resultInfo.setText("升级数据库结果：成功\n" + dataInfo);
            databaseInfo.setText(dataInfo);
        } else if (id == R.id.modify) {
            // 修改数据
            ContentValues values = new ContentValues();
            values.put(MyDatabaseHelper.COLUMN_NAME, CreateDataFactory.getUserName());
            values.put(MyDatabaseHelper.COLUMN_CONTENT, CreateDataFactory.getDesc());
            values.put(MyDatabaseHelper.COLUMN_CREATED_AT, getCurrentTime());
            int result = writableDatabase.update(MyDatabaseHelper.TABLE_NOTES, values, MyDatabaseHelper.COLUMN_ID + " = ?", new String[]{"1514"});
            resultInfo.setText("修改结果：" + (result > 0 ? "成功" : "失败") + ",用户信息：\n" + values.toString());
        } else if (id == R.id.delete) {
            // 处理删除逻辑
            int result = writableDatabase.delete(MyDatabaseHelper.TABLE_NOTES, MyDatabaseHelper.COLUMN_ID + " = ?", new String[]{"1514"});
            resultInfo.setText("删除结果：" + (result > 0 ? "成功" : "失败") + ",用户ID：1514");
        } else if (id == R.id.query) {
            // 处理查询逻辑
            queryAllNotes();

        } else if (id == R.id.delete_database) {
            // 处理删除数据库逻辑
            writableDatabase.execSQL("DROP DATABASE IF EXISTS " + MyDatabaseHelper.DATABASE_NAME);
            resultInfo.setText("删除数据库结果：成功");
        }
    }


    // 按ID查询单条数据的方法
    @SuppressLint("SetTextI18n")
    private void queryNoteById(int targetId) {
        // 1. 定义带占位符的SQL语句（? 为参数占位符，避免SQL注入）
        String sql = "SELECT * FROM " + MyDatabaseHelper.TABLE_NOTES
                + " WHERE " + MyDatabaseHelper.COLUMN_ID + " = ?";

        // 2. 定义查询参数（与占位符一一对应，字符串数组）
        String[] selectionArgs = {String.valueOf(targetId)}; // 这里将int转为String

        // 3. 执行查询
        Cursor cursor = writableDatabase.rawQuery(sql, selectionArgs);

        // 4. 处理结果
        if (cursor != null && cursor.moveToFirst()) {
            // 解析数据（与场景1相同）
            // 通过字段名获取列索引（推荐，避免字段顺序变化导致错误）
            int idIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME);
            int contentIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_CONTENT);
            int createdAtIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_CREATED_AT);

            // 根据索引获取对应值（注意字段类型匹配：id是整数，其他是字符串）
            int id = cursor.getInt(idIndex); // COLUMN_ID是整数类型
            String name = cursor.getString(nameIndex);
            String content = cursor.getString(contentIndex);
            String createdAt = cursor.getString(createdAtIndex);
            // 打印或处理数据（实际场景可封装为对象、更新UI等）
            Log.d("QueryResult", "ID: " + id + ", Name: " + name + ", Content: " + content + ", Created At: " + createdAt);
            resultInfo.setText("查询结果：ID=" + id + ", Name=" + name);
        } else {
            Log.d("QueryById", "未找到ID为" + targetId + "的记录");
        }

        // 5. 关闭Cursor
        if (cursor != null) {
            cursor.close();
        }
    }


    // 查询所有数据的方法
    private void queryAllNotes() {
        // 1. 定义SQL查询语句（查询所有字段）
        String sql = "SELECT * FROM " + MyDatabaseHelper.TABLE_NOTES;

        // 2. 使用writableDatabase执行查询（rawQuery返回Cursor结果集）
        // 注：虽然查询通常用readableDatabase，但writableDatabase也支持查询
        Cursor cursor = writableDatabase.rawQuery(sql, null); // 第二个参数为查询参数（无参数时传null）
        StringBuffer stringBuffer = new StringBuffer();

        // 3. 处理查询结果
        if (cursor != null && cursor.moveToFirst()) { // 判断Cursor非空且有数据
            do {
                // 通过字段名获取列索引（推荐，避免字段顺序变化导致错误）
                int idIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME);
                int contentIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_CONTENT);
                int createdAtIndex = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_CREATED_AT);

                // 根据索引获取对应值（注意字段类型匹配：id是整数，其他是字符串）
                int id = cursor.getInt(idIndex); // COLUMN_ID是整数类型
                String name = cursor.getString(nameIndex);
                String content = cursor.getString(contentIndex);
                String createdAt = cursor.getString(createdAtIndex);

                // 打印或处理数据（实际场景可封装为对象、更新UI等）
                Log.d("QueryResult", "ID: " + id
                        + ", Name: " + name
                        + ", Content: " + content
                        + ", CreatedAt: " + createdAt);

                stringBuffer.append("ID: ").append(id).append(", Name: ").append(name).append(", Content: ").append(content).append(", CreatedAt: ").append(createdAt).append("\n");
            } while (cursor.moveToNext()); // 循环遍历所有行

            resultInfo.setText(stringBuffer.toString());
        }

        // 4. 关闭Cursor（必须关闭，避免内存泄漏）
        if (cursor != null) {
            cursor.close();
        }
    }


    // 时间格式（注意：SimpleDateFormat是非线程安全的，不可定义为static共享）
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前时间（兼容旧版本）
     */
    public static String getCurrentTime() {
        // 每个方法调用新建实例，避免线程安全问题
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.CHINA);
        return sdf.format(new Date());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭数据库
        if (writableDatabase != null) {
            writableDatabase.close();
        }
    }
}
