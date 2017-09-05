package com.inspur.emmcloud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;

public class BaseFragmentActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        StateBarColor.changeStateBarColor(this);
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
        if (!AppUtils.isServiceWork(getApplicationContext(), "com.inspur.emmcloud.service.CollectService")&& (!DbCacheUtils.isDbNull())) {
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
                if(getIsNeedGestureCode()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putString("gesture_code_change","login");
                            IntentUtils.startActivity(BaseFragmentActivity.this, GestureLoginActivity.class,bundle);
                        }
                    },200);
                }
            }
        }

    }

    /**
     * 判断收需要打开手势解锁
     * @return
     */
    private boolean getIsNeedGestureCode() {
        String gestureCode = PreferencesByUserAndTanentUtils.getString(this,"gesture_code");
        boolean gestureCodeOpen = PreferencesByUserAndTanentUtils.getBoolean(this,"gesture_code_isopen",false);
        return !StringUtils.isBlank(gestureCode) && gestureCodeOpen;
    }


    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo() {
        if (NetUtils.isNetworkConnected(this,false)) {
            new AppAPIService(this).uploadMDMInfo();
        }

    }

}
