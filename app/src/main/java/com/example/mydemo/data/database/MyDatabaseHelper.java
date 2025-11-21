package com.example.mydemo.data.database;

/**
 * create by WUzejian on 2025/11/18
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // 数据库文件名
    public static final String DATABASE_NAME = "notes.db";
    // 数据库版本号
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_NOTES = "notes";
    // 列名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_CREATED_AT = "created_at";

    // 创建表的 SQL 语句
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP ," +
                    COLUMN_PHONE + " TEXT" +
                    ");";
    private Context context;
    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 首次创建数据库时调用，执行建表语句
        db.execSQL(TABLE_CREATE);
    }

    /**
     * - 数据库升级：当应用更新，数据库版本号增加时，自动调用 onUpgrade() 方法，让你可以在此执行数据迁移、修改表结构等操作。
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库版本升级时调用
        Log.w(MyDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // 简单的升级策略：删除旧表，创建新表（会丢失数据）
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);

        // 在实际项目中，应实现更复杂的数据迁移逻辑
        // 例如，当从版本1升级到版本2时，可能需要添加新列：
        // if (oldVersion < 2) {
        //     db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN priority INTEGER DEFAULT 0");
        // }
    }

    /**
     * 数据库降级处理
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // 方式1：删除旧表，重新创建低版本表结构（数据会丢失，适合开发）
//        // 注意：需先删除所有表（按实际表名修改）
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
//        // 调用onCreate创建低版本表结构
//        onCreate(db);
//        // 最后必须调用setVersion，将数据库版本更新为新版本（避免重复触发降级）
//        db.setVersion(newVersion);


        // 假设：从版本2降级到版本1，版本2比版本1多了一个"COLUMN_PHONE"字段
        // 目标：保留原有数据，删除新增的字段（版本1没有的字段）

        // 步骤1：创建临时表，存储版本2的数据
        db.execSQL("CREATE TABLE " + TABLE_NOTES + "_temp ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_CREATED_AT + " TEXT, "
                + "phone TEXT)"); // 版本2新增的字段

        // 步骤2：将版本2的数据复制到临时表
        db.execSQL("INSERT INTO " + TABLE_NOTES + "_temp SELECT * FROM " + TABLE_NOTES);

        // 步骤3：删除原表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);

        // 步骤4：创建版本1的表结构（无phone字段）
        onCreate(db);

        // 步骤5：将临时表中版本1需要的数据复制回原表（忽略phone字段）
        db.execSQL("INSERT INTO " + TABLE_NOTES + " (" + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_CONTENT + ", " + COLUMN_CREATED_AT + ")"
                + " SELECT " + COLUMN_ID + ", " + COLUMN_NAME + ", " + COLUMN_CONTENT + ", " + COLUMN_CREATED_AT + " FROM " + TABLE_NOTES + "_temp");

        // 步骤6：删除临时表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES + "_temp");

        // 步骤7：更新数据库版本为目标版本
        db.setVersion(newVersion);

        Toast.makeText(context, "数据库已降级到Version 1 " + newVersion, Toast.LENGTH_SHORT).show();
    }


}

