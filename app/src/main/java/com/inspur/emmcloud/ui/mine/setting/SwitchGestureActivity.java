package com.inspur.emmcloud.ui.mine.setting;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

/**
 * Created by yufuchang on 2017/8/29.
 */

public class SwitchGestureActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_gesture);
        ((MyApplication) getApplicationContext()).addActivity(this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }
}
