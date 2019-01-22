package com.inspur.emmcloud;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerV0Activity;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.imp.plugin.camera.imageedit.IMGEditActivity;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;

import java.util.Arrays;

public class BaseFragmentActivity extends FragmentActivity {
    private static final String[] classNames = {
            MyCameraActivity.class.getName(),
            ImagePagerV0Activity.class.getName(),
            ImagePagerActivity.class.getName(),
            IMGEditActivity.class.getName()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        if (!isContain){
//            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
//            if (currentThemeNo == 0){
//                setTheme(R.style.AppTheme_1);
//                StateBarUtils.setStateBarTextColor(this,true);
//            }else {
//                setTheme(R.style.AppTheme_2);
//            }
            StateBarUtils.translucent(this);
            StateBarUtils.setStateBarTextColor(this,true);
        }
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



}
