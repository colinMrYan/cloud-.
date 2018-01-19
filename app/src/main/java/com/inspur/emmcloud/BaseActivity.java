package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
        if (!className.endsWith(".CaptureActivity") &&!className.endsWith(".MyCameraActivity") && !className.endsWith(".LoginActivity")
                && !className.endsWith(".MainActivity") && !className.endsWith(".FaceVerifyActivity")){
            StateBarUtils.changeStateBarColor(this);
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
