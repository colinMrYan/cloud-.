package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.widget.SwitchView;

/**
 * Created by yufuchang on 2017/8/29.
 */

public class SwitchGestureActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_gesture);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initShowResetGesturePassWord(getHasGesturePassword()&&getGestureCodeIsOpen());
    }

    /**
     * 获取是否有手势解锁码
     * @return
     */
    private boolean getHasGesturePassword() {
        String gestureCode = CreateGestureActivity.getGestureCodeByUser(SwitchGestureActivity.this);
        return !StringUtils.isBlank(gestureCode);
    }

    /**
     * 初始化Views
     */
    private void init() {
        SwitchView switchView = ((SwitchView)findViewById(R.id.switch_gesture_switchview));
        if(getHasGesturePassword() && getGestureCodeIsOpen()){
            switchView.setOpened(true);
        }else{
            switchView.setOpened(false);
        }
        switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                ((SwitchView)view).setOpened(true);
                IntentUtils.startActivity(SwitchGestureActivity.this,CreateGestureActivity.class);
//                if(!getHasGesturePassword()){
//                    IntentUtils.startActivity(SwitchGestureActivity.this,CreateGestureActivity.class);
//                }else{
//                    findViewById(R.id.switch_gesture_change_code_layout).setVisibility(View.VISIBLE);
//                }

            }

            @Override
            public void toggleToOff(View view) {
//                ((SwitchView)view).setOpened(false);
                Bundle bundle = new Bundle();
                bundle.putString("gesture_code_change","close");
                IntentUtils.startActivity(SwitchGestureActivity.this,GestureLoginActivity.class,bundle);
//                findViewById(R.id.switch_gesture_change_code_layout).setVisibility(View.GONE);
//                PreferencesByUserAndTanentUtils.putBoolean(SwitchGestureActivity.this,"gesture_code_isopen",false);
            }
        });
        findViewById(R.id.switch_gesture_change_code_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("gesture_code_change","reset");
                IntentUtils.startActivity(SwitchGestureActivity.this,GestureLoginActivity.class,bundle);
            }
        });
        findViewById(R.id.switch_gesture_change_code_layout).setVisibility(getGestureCodeIsOpen()?View.VISIBLE:View.GONE);
    }

    /**
     * 获取是否打开了重置手势密码
     * @return
     */
    public boolean getGestureCodeIsOpen(){
       return CreateGestureActivity.getGestureCodeIsOpenByUser(SwitchGestureActivity.this);
    }

    /**
     * 初始化是否展示重置
     * @param isHasGesturePassword
     */
    private void initShowResetGesturePassWord(boolean isHasGesturePassword) {
        findViewById(R.id.switch_gesture_change_code_layout).setVisibility(isHasGesturePassword?View.VISIBLE:View.GONE);
        SwitchView switchView = ((SwitchView)findViewById(R.id.switch_gesture_switchview));
        switchView.setOpened((getHasGesturePassword()&&getGestureCodeIsOpen())?true:false);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            default:
                break;
        }
    }
}
