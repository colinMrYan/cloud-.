package com.inspur.emmcloud.ui.appcenter;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AppGroupAdapter;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/8.
 */
@ContentView(R.layout.activity_group_app)
public class AppGroupActivity extends BaseActivity{

    @ViewInject(R.id.recyclerview_group_app)
    private RecyclerView appGroupRecyclerView;
    @ViewInject(R.id.header_text)
    private TextView textView;
    private List<App> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appList.addAll((List<App>) getIntent().getSerializableExtra("appGroupList"));
        initViews();
    }

    private void initViews() {
        appGroupRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(MyApplication.getInstance(), 11)));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MyApplication.getInstance(), 4);
        appGroupRecyclerView.setLayoutManager(gridLayoutManager);
        AppGroupAdapter appGroupAdapter = new AppGroupAdapter(this,appList);
        appGroupRecyclerView.setAdapter(appGroupAdapter);
        appGroupAdapter.setGroupListener(new GroupAppListClickListener() {
            @Override
            public void onGroupAppClick(App app) {
                UriUtils.openApp(AppGroupActivity.this, app, "application");
            }
        });
        textView.setText(getIntent().getStringExtra("categoryName"));
    }

    public interface GroupAppListClickListener{
        void onGroupAppClick(App app);
    }
}
