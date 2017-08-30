package com.inspur.emmcloud.ui.mine.setting;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.ninelock.cache.ACache;
import com.inspur.emmcloud.util.ninelock.constant.Constant;
import com.inspur.emmcloud.widget.SwitchView;

/**
 * Created by yufuchang on 2017/8/29.
 */

public class SwitchGestureActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_gesture);
        ((MyApplication) getApplicationContext()).addActivity(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initShowResetGesturePassWord(getHasGesturePassword());
    }

    /**
     * 获取是否有手势解锁码
     * @return
     */
    private boolean getHasGesturePassword() {
        byte[] gesturePassword = ACache.get(SwitchGestureActivity.this).getAsBinary(Constant.GESTURE_PASSWORD);
        return  (gesturePassword == null);
    }

    /**
     * 初始化Views
     */
    private void init() {
        final boolean isHasGensturePassword = getHasGesturePassword();
        LogUtils.YfcDebug("是否存在手势密码："+isHasGensturePassword);
//        initShowResetGesturePassWord(isHasGensturePassword);
        SwitchView switchView = ((SwitchView)findViewById(R.id.switch_gesture_switchview));
        if(isHasGensturePassword){
            switchView.toggleSwitch(true);
        }else{
            switchView.toggleSwitch(false);
        }
        switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                ((SwitchView)view).setOpened(true);
                if(!isHasGensturePassword){
                    IntentUtils.startActivity(SwitchGestureActivity.this,CreateGestureActivity.class);
                }else{
                    IntentUtils.startActivity(SwitchGestureActivity.this,GestureLoginActivity.class);
                }
            }

            @Override
            public void toggleToOff(View view) {
                LogUtils.YfcDebug("关闭按钮");
                ((SwitchView)view).setOpened(false);
            }
        });
        findViewById(R.id.switch_gesture_change_code_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("gesture_code","reset");
                IntentUtils.startActivity(SwitchGestureActivity.this,GestureLoginActivity.class,bundle);
            }
        });
    }

    /**
     * 初始化是否展示重置
     * @param isHasGesturePassword
     */
    private void initShowResetGesturePassWord(boolean isHasGesturePassword) {
        if(isHasGesturePassword){
            findViewById(R.id.switch_gesture_change_code_layout).setVisibility(View.GONE);
        }else{
            findViewById(R.id.switch_gesture_change_code_layout).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.switch_gesture_switchview:

                break;
            default:
                break;
        }
    }
}
