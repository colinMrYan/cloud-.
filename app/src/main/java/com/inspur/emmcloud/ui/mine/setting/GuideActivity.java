package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能介绍页面 com.inspur.emmcloud.ui.GuideActivity
 *
 */
@ContentView(R.layout.activity_guide)
public class GuideActivity extends BaseActivity {

    @ViewInject(R.id.viewpager)
    private ViewPager viewPager;
    private List<View> guideViewList = new ArrayList<>();
    private LoadingDialog loadingDialog;
//    private boolean needGetMyProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this, R.color.white);
        initView();
    }

    private void initView() {
        // TODO Auto-generated method stub
        List<Integer> splashResIdList = new ArrayList<>();
        //刚安装App初次进入
        if (PreferencesUtils.getBoolean(getApplicationContext(),"isFirst", false)){
            splashResIdList.add(R.drawable.guide_page_1);
            splashResIdList.add(R.drawable.guide_page_2);
            splashResIdList.add(R.drawable.guide_page_3);
            splashResIdList.add(R.drawable.guide_page_4);
            splashResIdList.add(R.drawable.guide_page_5);
        }else {//版本升级进入
            splashResIdList.add(R.drawable.guide_page_1);
            splashResIdList.add(R.drawable.guide_page_2);
            splashResIdList.add(R.drawable.guide_page_3);
            splashResIdList.add(R.drawable.guide_page_4);
            splashResIdList.add(R.drawable.guide_page_5);
        }

        for (int i = 0; i < splashResIdList.size(); i++) {
            View guideView = LayoutInflater.from(this).inflate(R.layout.view_pager_guide, null);
            ImageView img = (ImageView) guideView.findViewById(R.id.img);
            img.setImageResource(splashResIdList.get(i));
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
                            if(!StringUtils.isBlank(accessToken) && AppUtils.isAppHasUpgraded(GuideActivity.this)){
                                getUserProfile();
                            }else{
                                startIntentActivity();
                            }
                        }
                    }
                });
            }
            guideViewList.add(guideView);
        }
        viewPager.setAdapter(new MyViewPagerAdapter(getApplicationContext(), guideViewList));
        loadingDialog = new LoadingDialog(this);
    }

    /**
     * 打开应该打开的Activity
     */
    private void startIntentActivity() {
        // 存入当前版本号,方便判断新功能介绍显示的时机
        String appVersion = AppUtils.getVersion(GuideActivity.this);
        PreferencesUtils.putString(getApplicationContext(), "previousVersion",
                appVersion);
        String accessToken = PreferencesUtils.getString(
                GuideActivity.this, "accessToken", "");
        IntentUtils.startActivity(GuideActivity.this, (!StringUtils.isBlank(accessToken)) ?
                IndexActivity.class : LoginActivity.class, true);
    }

    /**
     * 转到LoginActivity
     */
    private void startLoginActivity(){
        // 存入当前版本号,方便判断新功能介绍显示的时机
        String appVersion = AppUtils.getVersion(GuideActivity.this);
        PreferencesUtils.putString(getApplicationContext(), "previousVersion",
                appVersion);
        IntentUtils.startActivity(GuideActivity.this,LoginActivity.class,true);
    }

    /**
     * 获取用户的个人信息
     */
    private void getUserProfile() {
        if (NetUtils.isNetworkConnected(GuideActivity.this, true)) {
            loadingDialog.show();
            LoginAPIService apiServices = new LoginAPIService(GuideActivity.this);
            apiServices.setAPIInterface(new WebService());
            apiServices.getMyInfo();
        }else{
            startLoginActivity();
        }
    }


    class WebService extends APIInterfaceInstance{
        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            saveNewProfileAndOldProfile(getMyInfoResult.getResponse());
            MyApplication.getInstance().initTanent();
            startIntentActivity();
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            startLoginActivity();
        }
    }

    /**
     * 存储新旧profile
     * @param response
     */
    private void saveNewProfileAndOldProfile(String response) {
        //存储上一个版本，不再有本地默认版本
        PreferencesUtils.putString(GuideActivity.this, Constant.PREF_MY_INFO_OLD,PreferencesUtils.getString(GuideActivity.this,"",""));
        PreferencesUtils.putString(GuideActivity.this, "myInfo", response);
    }
}
