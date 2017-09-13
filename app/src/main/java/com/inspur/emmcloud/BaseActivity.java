package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;

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

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (!AppUtils.isAppOnForeground(getApplicationContext())) {
            // app 进入后台
            ((MyApplication) getApplicationContext()).setIsActive(false);
            // 全局变量isActive = false 记录当前已经进入后台
            ((MyApplication) getApplicationContext()).sendFrozenWSMsg();
            startUploadPVCollectService();
        }
    }

    /***
     * 打开app应用行为分析上传的Service;
     */
    private void startUploadPVCollectService() {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(getApplicationContext(), "com.inspur.emmcloud.service.CollectService") && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(this, PVCollectService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!((MyApplication) getApplicationContext()).getIsActive()) {
            if (((MyApplication) getApplicationContext())
                    .isIndexActivityRunning()) {
                ((MyApplication) getApplicationContext()).setIsActive(true);
                ((MyApplication) getApplicationContext()).clearNotification();
                uploadMDMInfo();
                ((MyApplication) getApplicationContext()).sendActivedWSMsg();
                if(getIsNeedGestureCode()){//这里两处登录均不走这个方法，如果以后集成单点登录，需要集成BaseActivity，或者BaseFragmentActivity
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10);
                                Bundle bundle = new Bundle();
                                bundle.putString("gesture_code_change","login");
                                IntentUtils.startActivity(BaseActivity.this, GestureLoginActivity.class,bundle);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }
            }
        }
    }

    /**
     * 判断收需要打开手势解锁
     * @return
     */
    private boolean getIsNeedGestureCode() {
        String gestureCode = CreateGestureActivity.getGestureCodeByUser(this);
        boolean gestureCodeOpen = CreateGestureActivity.getGestureCodeIsOpenByUser(this);
        return !StringUtils.isBlank(gestureCode) && gestureCodeOpen;
    }

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo() {
        if (NetUtils.isNetworkConnected(this, false)) {
            new AppAPIService(this).uploadMDMInfo();
        }

    }


    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplicationContext()).setIsActive(true);
    }
}
