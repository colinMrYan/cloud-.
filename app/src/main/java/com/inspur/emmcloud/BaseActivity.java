package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.ui.SchemeHandleActivity;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.login.ScanQrCodeLoginGSActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;
import com.inspur.imp.plugin.camera.imageedit.IMGEditActivity;
import com.inspur.imp.plugin.photo.ImageGalleryActivity;

import org.xutils.x;

import java.util.Arrays;

public class BaseActivity extends Activity {
    private static final String[] classNames = {
            MainActivity.class.getName(),
            LoginActivity.class.getName(),
            SchemeHandleActivity.class.getName(),
            CaptureActivity.class.getName(),
            FaceVerifyActivity.class.getName(),
            ReactNativeAppActivity.class.getName(),
            ScanQrCodeLoginGSActivity.class.getName(),
            IMGEditActivity.class.getName(),
            ImageGalleryActivity.class.getName(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        String className = this.getClass().getCanonicalName();
        boolean isContain = Arrays.asList(classNames).contains(className);
//        if (!isContain) {
//            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
//            if (currentThemeNo == 0){
//                setTheme(R.style.AppTheme_1);
//            }else {
//                setTheme(R.style.AppTheme_2);
//            }
//            StateBarUtils.translucent(this);
//        }
        super.onCreate(savedInstanceState);
        x.view().inject(this);
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
