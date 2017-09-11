package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;

/**
 * Created by yufuchang on 2017/9/7.
 */
@ContentView(R.layout.activity_create_gesture_code_guid)
public class CreateGestureCodeGuidActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        EventBus.getDefault().register(this);
    }

    public void onClick(View view){
       switch (view.getId()){
           case R.id.back_layout:
               finish();
               break;
           default:
               break;
       }
    }

    /**
     * 点击创建手势密码的动作
     * @param view
     */
    @Event(R.id.create_gesture_code_btn)
    private void startCreateGestureActivity(View view){
        switch (view.getId()){
            case R.id.create_gesture_code_btn:
                Intent intent = new Intent();
                intent.setClass(CreateGestureCodeGuidActivity.this,CreateGestureActivity.class);
                startActivity(intent);
                break;
        }

    }

    /**
     * 创建成功后关闭当前页面
     * @param createGestureCodeSuccess
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void finishActivity(String createGestureCodeSuccess) {
        if(createGestureCodeSuccess.equals(CreateGestureActivity.CREATE_GESTURE_CODE_SUCCESS)){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
