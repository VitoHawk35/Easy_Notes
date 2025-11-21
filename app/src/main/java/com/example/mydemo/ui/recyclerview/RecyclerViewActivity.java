package com.example.mydemo.ui.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mydemo.R;
import com.example.mydemo.ui.recyclerview.adapter.StaggeredAdapter;
import com.example.mydemo.ui.recyclerview.adapter.UserAdapter;
import com.example.mydemo.ui.recyclerview.bean.UserBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * create by WUzejian on 2025/11/17
 */
public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private List<UserBean> mUserList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        mUserList = new ArrayList<>();
        //获取列表组件
        mRecyclerView = findViewById(R.id.recyclerview_id);

        findViewById(R.id.linearLayout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建测试数据
                createUserList(RecyclerViewActivity.this);
                //设置布局管理器（这里用线性布局，垂直方向）
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RecyclerViewActivity.this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(linearLayoutManager);
                //创建Adapter并设置给RecyclerView
                UserAdapter adapter = new UserAdapter(mUserList);
                mRecyclerView.setAdapter(adapter);
            }
        });

        findViewById(R.id.girdLayout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建测试数据
                createUserList(RecyclerViewActivity.this);
                //设置网格布局管理器，2列
                GridLayoutManager gridLayoutManager = new GridLayoutManager(RecyclerViewActivity.this, 2);
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(gridLayoutManager);
                //创建Adapter并设置给RecyclerView
                UserAdapter adapter = new UserAdapter(mUserList);
                mRecyclerView.setAdapter(adapter);
            }
        });

        findViewById(R.id.staggeredGridLayout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] imgIds = createStaggeredGridLayoutManagerList();
                StaggeredAdapter staggeredAdapter = new StaggeredAdapter(RecyclerViewActivity.this.getApplicationContext(), imgIds);
                //2列垂直瀑布流
                StaggeredGridLayoutManager staggeredLayoutManager =
                        new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(staggeredLayoutManager);
                //创建Adapter并设置给RecyclerView
                mRecyclerView.setAdapter(staggeredAdapter);
            }
        });
    }

    public void createUserList(Context context) {
        Random random = new Random(); // 用于生成随机数
        // ignore_security_alert [ByDesign7.4]WeakPRNG

        // 1. 姓氏和名字数据源（用于随机组合名称）
        String[] familyNames = {"张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴"};
        String[] givenNames = {"明", "华", "强", "伟", "芳", "丽", "敏", "军", "杰", "娜"};

        // 2. 描述模板（随机选择）
        String[] descTemplates = {
                "喜欢旅行和摄影",
                "热爱生活的程序员",
                "美食爱好者，擅长烘焙",
                "健身达人，每周运动5次",
                "音乐迷，收藏了1000+首歌",
                "阅读是最大的爱好",
                "职场新人，努力提升中",
                "宠物博主，家有一只猫"
        };

        // 3. 循环创建22个User（i从1到22，对应avator_1到avator_22）
        for (int i = 1; i <= 22; i++) {
            // 生成随机名称（随机姓氏 + 随机名字）
            String randomFamily = familyNames[random.nextInt(familyNames.length)];
            String randomGiven = givenNames[random.nextInt(givenNames.length)];
            String randomName = randomFamily + randomGiven;

            // 获取头像资源ID（avator_i）
            String avatarName = "avator_" + i;
            // 通过资源名称获取ID：参数分别为（资源名，资源类型，包名）
            int avatarResId = context.getResources().getIdentifier(
                    avatarName,
                    "drawable",
                    context.getPackageName()
            );

            // 生成随机描述（从模板中随机选择）
            String randomDesc = descTemplates[random.nextInt(descTemplates.length)];

            // 创建User对象并添加到列表
            mUserList.add(new UserBean(randomName, randomDesc, avatarResId));
        }
    }


    public int[] createStaggeredGridLayoutManagerList() {
        int[] imgIds = new int[]{R.drawable.image1, R.drawable.avator_2, R.drawable.image2, R.drawable.image3, R.drawable.avator_3, R.drawable.image4,
                R.drawable.image5, R.drawable.image6, R.drawable.avator_1, R.drawable.avator_4, R.drawable.avator_5, R.drawable.avator_6, R.drawable.avator_7
                , R.drawable.avator_8, R.drawable.avator_9, R.drawable.avator_10, R.drawable.avator_11, R.drawable.avator_12, R.drawable.avator_13, R.drawable.avator_14};
        return imgIds;
    }
}
