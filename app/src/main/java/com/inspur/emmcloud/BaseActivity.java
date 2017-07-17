package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StateBarColor;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
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
            }
        }
    }

//	@Override
//	public Resources getResources() {
//	    Resources res = super.getResources();
//	    Configuration config=new Configuration();
//	    config.setToDefaults();
//	    res.updateConfiguration(config,res.getDisplayMetrics() );
//	    return res;
//	}

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo() {
        if (NetUtils.isNetworkConnected(this,false)) {
            new AppAPIService(this).uploadMDMInfo();
        }

    }


    //修改本地字体方案预留
//	@Override
//	protected void attachBaseContext(Context newBase) {
//		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//
//	}


}
