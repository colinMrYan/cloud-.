package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.util.StateBarColor;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String className = this.getClass().getCanonicalName();
        if (!className.equals("com.inspur.imp.plugin.barcode.scan.CaptureActivity") &&!className.equals("com.inspur.imp.plugin.camera.mycamera.MyCameraActivity") ){
            StateBarColor.changeStateBarColor(this);
        }
    }


    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplicationContext()).setIsActive(true);
    }
}
