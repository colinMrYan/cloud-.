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
import com.inspur.emmcloud.basemodule.bean.badge.AppBadgeModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModuleModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/8/8.
 */
@Route(path = Constant.AROUTER_CLASS_APPCENTER_GROUP)
public class AppGroupActivity extends BaseActivity {

    RecyclerView appGroupRecyclerView;
    TextView textView;
    private List<App> appList = new ArrayList<>();
    private Map<String, Integer> mAppStoreBadgeMap = new HashMap<>();
    private AppGroupAdapter mAppGroupAdapter;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        appList.addAll((List<App>) getIntent().getSerializableExtra("appGroupList"));
        mAppStoreBadgeMap = (Map<String, Integer>) getIntent().getSerializableExtra("appStoreBadgeMap");
        initViews();
        EventBus.getDefault().register(this);
    }


    //接收从AppBadgeUtils里发回的聊天服务角标数字
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAppBadgeNum(BadgeBodyModel badgeBodyModel) {
        if (!badgeBodyModel.isFromWebSocket()) {
            return;
        }
        BadgeBodyModuleModel badgeBodyModuleModel = badgeBodyModel.getAppStoreBadgeBodyModuleModel();
        mAppStoreBadgeMap = badgeBodyModuleModel.getDetailBodyMap();
        mAppGroupAdapter.updateBadgeNum(mAppStoreBadgeMap);
    }

    //接收从AppBadgeUtils里发回的角标服务角标数字
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAppBadgeNumFromBadgeServer(AppBadgeModel appBadgeModel) {
        mAppGroupAdapter.updateBadgeNum(appBadgeModel.getAppBadgeMap());
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
        mAppGroupAdapter = new AppGroupAdapter(this, appList, mAppStoreBadgeMap);
        appGroupRecyclerView.setAdapter(mAppGroupAdapter);
        mAppGroupAdapter.setGroupListener(new GroupAppListClickListener() {
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
