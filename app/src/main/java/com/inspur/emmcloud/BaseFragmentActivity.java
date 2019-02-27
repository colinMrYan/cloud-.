package com.inspur.emmcloud;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerV0Activity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
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

    protected void setTheme(){
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        if (!isContain){
            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
            switch (currentThemeNo){
                case 1:
                    setTheme(R.style.AppTheme_1);
//                    StateBarUtils.translucent(this);
//                    StateBarUtils.setStateBarTextColor(this,false);
                    break;
                case 2:
                    setTheme(R.style.AppTheme_2);
//                    StateBarUtils.translucent(this);
//                    StateBarUtils.setStateBarTextColor(this,true);
                    break;
                default:
                    setTheme(R.style.AppTheme_0);

//                    ImmersionBar.with(this)
//                            .statusBarDarkFont(true, 0.2f)
//                            .navigationBarDarkIcon(true, 0.2f)
//                            .init();
                    break;
            }

        }
    }

    protected void setStatus(){
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        if (!isContain) {
            int color = ResourceUtils.getValueOfAttr(BaseFragmentActivity.this,R.attr.header_bg_color);
            boolean isStatusFontDark = true;
            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
            switch (currentThemeNo){
                case 1:
                    isStatusFontDark =false;
                    break;
                case 2:
                    isStatusFontDark =true;
                    break;
                default:
                    isStatusFontDark =true;
                    break;
            }
            ImmersionBar.with(this).statusBarColor(color).statusBarDarkFont(isStatusFontDark).init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
