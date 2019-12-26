package com.inspur.emmcloud.application.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.adapter.AppGroupAdapter;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.util.ApplicationUriUtils;
import com.inspur.emmcloud.application.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/8/8.
 */
@Route(path = Constant.AROUTER_CLASS_APPCENTER_GROUP)
public class AppGroupActivity extends BaseActivity {

    RecyclerView appGroupRecyclerView;
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
        return R.layout.application_activity_group_app;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        appGroupRecyclerView = findViewById(R.id.recyclerview_group_app);
        textView = findViewById(R.id.header_text);
        appGroupRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(BaseApplication.getInstance(), 11)));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(BaseApplication.getInstance(), 4);
        appGroupRecyclerView.setLayoutManager(gridLayoutManager);
        AppGroupAdapter appGroupAdapter = new AppGroupAdapter(this, appList);
        appGroupRecyclerView.setAdapter(appGroupAdapter);
        appGroupAdapter.setGroupListener(new GroupAppListClickListener() {
            @Override
            public void onGroupAppClick(App app) {
                ApplicationUriUtils.openApp(AppGroupActivity.this, app, "application");
            }
        });
        String categoryName = getIntent().getStringExtra("categoryName");
        textView.setText(categoryName == null ? "" : categoryName);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.ibt_back) {
            finish();
        }
    }

    public interface GroupAppListClickListener {
        void onGroupAppClick(App app);
    }
}
