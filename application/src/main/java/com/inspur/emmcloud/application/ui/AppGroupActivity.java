package com.inspur.emmcloud.application.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.adapter.AppGroupAdapter;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/8/8.
 */
public class AppGroupActivity extends BaseActivity {

    @BindView(R.id.recyclerview_group_app)
    RecyclerView appGroupRecyclerView;
    @BindView(R.id.header_text)
    TextView textView;
    private List<App> appList = new ArrayList<>();


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        appList.addAll((List<App>) getIntent().getSerializableExtra("appGroupList"));
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_app;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        appGroupRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(BaseApplication.getInstance(), 11)));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(BaseApplication.getInstance(), 4);
        appGroupRecyclerView.setLayoutManager(gridLayoutManager);
        AppGroupAdapter appGroupAdapter = new AppGroupAdapter(this, appList);
        appGroupRecyclerView.setAdapter(appGroupAdapter);
        appGroupAdapter.setGroupListener(new GroupAppListClickListener() {
            @Override
            public void onGroupAppClick(App app) {
                UriUtils.openApp(AppGroupActivity.this, app, "application");
            }
        });
        String categoryName = getIntent().getStringExtra("categoryName");
        textView.setText(categoryName == null ? "" : categoryName);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    public interface GroupAppListClickListener {
        void onGroupAppClick(App app);
    }
}
