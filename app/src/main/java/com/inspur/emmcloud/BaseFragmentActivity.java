package com.inspur.emmcloud;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class BaseFragmentActivity extends FragmentActivity {

    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplicationContext()).setIsActive(true);
    }

}
