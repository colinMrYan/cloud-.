package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;

import org.xutils.x;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        String className = this.getClass().getCanonicalName();
        if ( !className.endsWith(".CaptureActivity") &&!className.endsWith(".MyCameraActivity") && !className.endsWith(".LoginActivity")
                && !className.endsWith(".MainActivity") && !className.endsWith(".FaceVerifyActivity") && !className.endsWith(".ReactNativeAppActivity")  && !className.endsWith(".ScanQrCodeLoginGSActivity")
                && !className.endsWith(".IMGEditActivity") && !className.endsWith(".ImageGalleryActivity")){
            StateBarUtils.changeStateBarColor(this);
        }
        setImmersiveStateBar();
    }

    /**
     * lbc
     * 设置沉浸式状态栏（华为手机不显示沉浸状态）
     */
    private  void  setImmersiveStateBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplicationContext()).setIsActive(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase));
    }
}
