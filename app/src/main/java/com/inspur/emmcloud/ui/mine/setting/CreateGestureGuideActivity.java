package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

/**
 * Created by yufuchang on 2017/9/7.
 */
public class CreateGestureGuideActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_create_gesture_guide;
    }

    @OnClick(R.id.bt_create_gesture)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.bt_create_gesture:
                Intent intent = new Intent();
                intent.setClass(CreateGestureGuideActivity.this, CreateGestureActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 创建成功后关闭当前页面
     *
     * @param createGestureCodeSuccess
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void finishActivity(String createGestureCodeSuccess) {
        if (createGestureCodeSuccess.equals(CreateGestureActivity.CREATE_GESTURE_CODE_SUCCESS)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
