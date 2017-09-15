package com.inspur.emmcloud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;

public class BaseFragmentActivity extends FragmentActivity {

    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplicationContext()).setIsActive(true);
    }

}
