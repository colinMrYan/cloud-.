package com.inspur.emmcloud.ui.mine.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能介绍页面 com.inspur.emmcloud.ui.GuideActivity
 *
 */
@ContentView(R.layout.activity_guide)
public class GuideActivity extends Activity {

    @ViewInject(R.id.viewpager)
    private ViewPager viewPager;
    private List<View> guideViewList = new ArrayList<>();
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        x.view().inject(this);
        deleteReactNativeResource();
        initView();
    }

    /**
     * 删除老版本（低于2.0.0）的React文件目录
     */
    private void deleteReactNativeResource() {
        try {
            String reactNativeResourceFolderPath = getDir("ReactResource",MODE_PRIVATE).getPath();
            if(FileUtils.isFolderExist((reactNativeResourceFolderPath))){
                FileUtils.deleteFile(reactNativeResourceFolderPath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initView() {
        // TODO Auto-generated method stub
        List<Integer> splashResIdList = new ArrayList<>();
        //刚安装App初次进入
        if (PreferencesUtils.getBoolean(getApplicationContext(),"isFirst", true)){
            splashResIdList.add(R.drawable.guide_page_1);
            splashResIdList.add(R.drawable.guide_page_2);
            splashResIdList.add(R.drawable.guide_page_3);
            splashResIdList.add(R.drawable.guide_page_4);
            splashResIdList.add(R.drawable.guide_page_5);
            splashResIdList.add(R.drawable.guide_page_6);
        }else {//版本升级进入
            splashResIdList.add(R.drawable.guide_page_new_1);
            splashResIdList.add(R.drawable.guide_page_new_2);
            splashResIdList.add(R.drawable.guide_page_new_3);
            splashResIdList.add(R.drawable.guide_page_new_4);
            splashResIdList.add(R.drawable.guide_page_new_5);
        }

        for (int i = 0; i < splashResIdList.size(); i++) {
            View guideView = LayoutInflater.from(this).inflate(R.layout.view_pager_guide, null);
            ImageView img = (ImageView) guideView.findViewById(R.id.img);
                    ImageDisplayUtils.getInstance().displayImage(img,"drawable://"+splashResIdList.get(i));
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
                            if(!StringUtils.isBlank(accessToken)){
                                new ProfileUtils(GuideActivity.this, new CommonCallBack() {
                                    @Override
                                    public void execute() {
                                        IntentUtils.startActivity(GuideActivity.this,
                                                IndexActivity.class, true);
                                    }
                                }).initProfile();
                            }else{
                                startLoginActivity();
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
     * 转到LoginActivity
     */
    private void startLoginActivity(){
        // 存入当前版本号,方便判断新功能介绍显示的时机
        MyApplication.getInstance().signout();
        String appVersion = AppUtils.getVersion(GuideActivity.this);
        PreferencesUtils.putString(getApplicationContext(), "previousVersion",
                appVersion);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase));
    }
}
