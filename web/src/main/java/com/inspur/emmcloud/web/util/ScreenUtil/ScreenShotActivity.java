package com.inspur.emmcloud.web.util.ScreenUtil;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.inspur.emmcloud.basemodule.ui.BaseActivity;


/**
 * Created by wei on 16-9-18.
 * <p>
 * 完全透明 只是用于弹出权限申请的窗而已
 * <p>
 * 这个类的用于service需要在manifest中配置 intent-filter action
 */
public class ScreenShotActivity extends BaseActivity {

    public static final int REQUEST_MEDIA_PROJECTION = 0x2893;

    public String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        setTheme(android.R.style.Theme_Dialog);//这个在这里设置 之后导致 的问题是 背景很黑
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
            if (path == null) {
                path = "";
            }
        }
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setDimAmount(0f);
        requestScreenShot();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    public void requestScreenShot() {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivityForResult(createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        } else {
            toast("版本过低,无法截屏");
        }
    }

    private Intent createScreenCaptureIntent() {
        //这里用media_projection代替Context.MEDIA_PROJECTION_SERVICE 是防止低于21 api编译不过
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent();
        } else {
            return null;
        }
    }

    private void toast(String str) {
        Toast.makeText(ScreenShotActivity.this, str, Toast.LENGTH_LONG).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION: {
                if (resultCode == RESULT_OK && data != null) {
                    Shotter shotter = new Shotter(ScreenShotActivity.this, resultCode, data);
                    shotter.startScreenShot(new Shotter.OnShotListener() {
                        @Override
                        public void onFinish() {
                            toast("shot finish!");
                            finish(); // don't forget finish activity
                        }
                    }, path);
                    finish();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                    toast("shot cancel , please give permission.");
                } else {
                    finish();
                    toast("unknow exceptions!");
                }
            }
        }
    }

}