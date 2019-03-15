package com.inspur.emmcloud;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerV0Activity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;

import java.util.Arrays;

public class BaseFragmentActivity extends FragmentActivity {
    private static final String[] classNames = {
            ImagePagerV0Activity.class.getName(),
            ImagePagerActivity.class.getName(),
            MyCameraActivity.class.getName(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }


    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApplication.getInstance().setEnterSystemUI(false);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase));
    }

    protected void setTheme() {
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        if (!isContain) {
            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
            switch (currentThemeNo) {
                case 1:
                    setTheme(R.style.AppTheme_1);
                    break;
                case 2:
                    setTheme(R.style.AppTheme_2);
                    break;
                default:
                    setTheme(R.style.AppTheme_0);
                    break;
            }
        }
    }

    protected void setStatus() {
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        if (!isContain) {
            int statusBarColor = ResourceUtils.getResValueOfAttr(this, R.attr.header_bg_color);
            int navigationBarColor =R.color.white;
            boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this,R.attr.status_bar_dark_font);
            ImmersionBar.with(this).statusBarColor(statusBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true,1.0f).statusBarDarkFont(isStatusBarDarkFont,0.2f).init();
        }
    }

    protected void setNavigationBarColor(int color){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(ContextCompat.getColor(MyApplication.getInstance(),color));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
