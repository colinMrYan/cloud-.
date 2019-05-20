package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.chat.ConversationGroupInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.login.ScanQrCodeLoginGSActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.imp.plugin.barcode.decoder.PreviewDecodeActivity;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;
import com.inspur.imp.plugin.camera.imageedit.IMGEditActivity;
import com.inspur.imp.plugin.photo.ImageGalleryActivity;

import org.xutils.x;

import java.util.Arrays;

public class BaseActivity extends Activity {
    private static final String[] classNames = {
            MainActivity.class.getName(),
//            SchemeHandleActivity.class.getName(),
            LoginActivity.class.getName(),
            CaptureActivity.class.getName(),
            PreviewDecodeActivity.class.getName(),
            FaceVerifyActivity.class.getName(),
            ReactNativeAppActivity.class.getName(),
            ScanQrCodeLoginGSActivity.class.getName(),
            IMGEditActivity.class.getName(),
            ImageGalleryActivity.class.getName(),
            MyInfoActivity.class.getName(),
            GuideActivity.class.getName(),
            UserInfoActivity.class.getName(),
            GestureLoginActivity.class.getName(),
            ConversationGroupInfoActivity.class.getName(),

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setTheme();
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setStatus();
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

    private void setStatus() {
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
        int navigationBarColor = R.color.white;
        if (!isContain) {
            int statusBarColor = ResourceUtils.getResValueOfAttr(BaseActivity.this, R.attr.header_bg_color);
            boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this, R.attr.status_bar_dark_font);
            ImmersionBar.with(this).statusBarColor(statusBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
        } else {
            ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
        }
    }

//    protected void setTransparentStatus() {
//        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this,R.attr.status_bar_dark_font);
//        ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(isStatusBarDarkFont).init();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
