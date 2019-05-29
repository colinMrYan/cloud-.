package com.inspur.emmcloud.ui.mine.setting;

import java.util.ArrayList;
import java.util.List;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.LanguageManager;
import com.inspur.emmcloud.util.privates.ProfileUtils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 功能介绍页面 com.inspur.emmcloud.ui.GuideActivity
 */
public class GuideActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    private List<View> guideViewList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).init();
        deleteReactNativeResource();
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_guide;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    /**
     * 删除老版本（低于2.0.0）的React文件目录
     */
    private void deleteReactNativeResource() {
        try {
            String reactNativeResourceFolderPath = getDir("ReactResource", MODE_PRIVATE).getPath();
            if (FileUtils.isFolderExist((reactNativeResourceFolderPath))) {
                FileUtils.deleteFile(reactNativeResourceFolderPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        // TODO Auto-generated method stub
        List<Integer> splashResIdList = new ArrayList<>();
        //刚安装App初次进入
        if (PreferencesUtils.getBoolean(getApplicationContext(), "isFirst", true) && AppUtils.isAppVersionStandard()) {
            splashResIdList.add(R.drawable.guide_page_1);
            splashResIdList.add(R.drawable.guide_page_2);
            splashResIdList.add(R.drawable.guide_page_3);
            splashResIdList.add(R.drawable.guide_page_4);
            splashResIdList.add(R.drawable.guide_page_5);
            splashResIdList.add(R.drawable.guide_page_6);
        } else {//版本升级进入
            splashResIdList.add(R.drawable.guide_page_new_1);
            splashResIdList.add(R.drawable.guide_page_new_2);
            splashResIdList.add(R.drawable.guide_page_new_3);
        }

        for (int i = 0; i < splashResIdList.size(); i++) {
            View guideView = LayoutInflater.from(this).inflate(R.layout.view_pager_guide, null);
            ImageView img = (ImageView) guideView.findViewById(R.id.img);
            ImageDisplayUtils.getInstance().displayImageNoCache(img, "drawable://" + splashResIdList.get(i));
            if (i == splashResIdList.size() - 1) {
                Button enterButton = ((Button) guideView
                        .findViewById(R.id.enter_app_btn));
                enterButton.setVisibility(View.VISIBLE);
                // 当从关于页面跳转到此页面时，按钮显示不同的内容
                if (getIntent().getExtras() != null && getIntent().getExtras().getString("from", "").equals("about")) {
                    enterButton.setText(getString(R.string.return_app));
                }
                enterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getIntent().getExtras() != null && getIntent().getExtras().getString("from", "").equals("about")) {
                            finish();
                        } else {
                            // 将首次进入应用的标志位置为false
                            PreferencesUtils.putBoolean(getApplicationContext(),
                                    "isFirst", false);
                            String accessToken = PreferencesUtils.getString(
                                    GuideActivity.this, "accessToken", "");
                            if (!StringUtils.isBlank(accessToken)) {
                                if (AppUtils.isAppHasUpgraded(GuideActivity.this) && NetUtils.isNetworkConnected(GuideActivity.this)) {
                                    new ProfileUtils(GuideActivity.this, new CommonCallBack() {
                                        @Override
                                        public void execute() {
                                            IntentUtils.startActivity(GuideActivity.this,
                                                    IndexActivity.class, true);
                                        }
                                    }).initProfile();
                                }
                            } else {
                                MyApplication.getInstance().signout();
                            }
                        }
                    }
                });
            }
            guideViewList.add(guideView);
        }
        viewPager.setAdapter(new MyViewPagerAdapter(getApplicationContext(), guideViewList));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }
}
