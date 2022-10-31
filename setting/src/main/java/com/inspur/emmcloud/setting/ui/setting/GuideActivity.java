package com.inspur.emmcloud.setting.ui.setting;

import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.app.CommonCallBack;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.adapter.SettingMyViewPagerAdapter;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 功能介绍页面 com.inspur.emmcloud.ui.GuideActivity
 */
@Route(path = Constant.AROUTER_CLASS_SETTING_GUIDE)
public class GuideActivity extends BaseActivity implements NotSupportLand {

    @BindView(R2.id.viewpager)
    ViewPager viewPager;
    private List<View> guideViewList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        deleteReactNativeResource();
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_guide_activity;
    }

    protected int getStatusType() {
        return STATUS_FULL_SCREEN;
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
        boolean darkTheme = DarkUtil.isDarkTheme();
        //刚安装App初次进入
        if (PreferencesUtils.getBoolean(getApplicationContext(), "isFirst", true) && AppUtils.isAppVersionStandard()) {
            splashResIdList.add(darkTheme ? R.drawable.guide_page_first_dark_1 : R.drawable.guide_page_first_light_1);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_first_dark_2 : R.drawable.guide_page_first_light_2);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_first_dark_3 : R.drawable.guide_page_first_light_3);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_first_dark_4 : R.drawable.guide_page_first_light_4);
        } else {//版本升级进入
            splashResIdList.add(darkTheme ? R.drawable.guide_page_dark_1 : R.drawable.guide_page_light_1);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_dark_2 : R.drawable.guide_page_light_2);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_dark_3 : R.drawable.guide_page_light_3);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_dark_4 : R.drawable.guide_page_light_4);
            splashResIdList.add(darkTheme ? R.drawable.guide_page_dark_5 : R.drawable.guide_page_light_5);
        }

        for (int i = 0; i < splashResIdList.size(); i++) {
            View guideView = LayoutInflater.from(this).inflate(R.layout.setting_view_pager_guide, null);
            ImageView img = (ImageView) guideView.findViewById(R.id.img);
            img.setImageResource(splashResIdList.get(i));
//            ImageDisplayUtils.getInstance().displayImageNoCache(img, "drawable://" + splashResIdList.get(i));
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
//                            String accessToken = PreferencesUtils.getString(
//                                    GuideActivity.this, "accessToken", "");
                            MMKV kv = MMKV.mmkvWithID("InterProcessKV", MMKV.MULTI_PROCESS_MODE);
                            String accessToken = kv.decodeString("accessToken", "");
                            if (!StringUtils.isBlank(accessToken)) {
                                if (AppUtils.isAppHasUpgraded(GuideActivity.this) && NetUtils.isNetworkConnected(GuideActivity.this)) {
                                    AppService appService = Router.getInstance().getService(AppService.class);
                                    if (appService != null) {
                                        appService.initProfile(GuideActivity.this, false, new CommonCallBack() {
                                            @Override
                                            public void execute() {
                                                ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).navigation(GuideActivity.this);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            } else {
                                BaseApplication.getInstance().signout();
                            }
                        }
                    }
                });
            }
            guideViewList.add(guideView);
        }
        viewPager.setAdapter(new SettingMyViewPagerAdapter(getApplicationContext(), guideViewList));
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
//    }
}
