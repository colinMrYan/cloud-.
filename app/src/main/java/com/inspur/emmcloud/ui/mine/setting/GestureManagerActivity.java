package com.inspur.emmcloud.ui.mine.setting;

import com.inspur.emmcloud.BaseActivity;

/**
 * Created by yufuchang on 2017/8/29.
 */

public class GestureManagerActivity extends BaseActivity {

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gesture_manager);
//        init();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        initShowResetGesturePassWord(getHasGesturePassword() && getGestureCodeIsOpen());
//    }
//
//    /**
//     * 获取是否有手势解锁码
//     *
//     * @return
//     */
//    private boolean getHasGesturePassword() {
//        String gestureCode = CreateGestureActivity.getGestureCodeByUser(GestureManagerActivity.this);
//        return !StringUtils.isBlank(gestureCode);
//    }
//
//    /**
//     * 初始化Views
//     */
//    private void init() {
//        SwitchView switchView = ((SwitchView) findViewById(R.id.switch_gesture_switchview));
//        if (getHasGesturePassword() && getGestureCodeIsOpen()) {
//            switchView.setOpened(true);
//        } else {
//            switchView.setOpened(false);
//        }
//        switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
//            @Override
//            public void toggleToOn(View view) {
//                ((SwitchView) view).setOpened(true);
//                IntentUtils.startActivity(GestureManagerActivity.this, CreateGestureActivity.class);
//            }
//
//            @Override
//            public void toggleToOff(View view) {
////                ((SwitchView)view).setOpened(false);
//                Bundle bundle = new Bundle();
//                bundle.putString("gesture_code_change", "close");
//                IntentUtils.startActivity(GestureManagerActivity.this, GestureLoginActivity.class, bundle);
//            }
//        });
//        findViewById(R.id.switch_gesture_change_code_layout).setVisibility(getGestureCodeIsOpen() ? View.VISIBLE : View.GONE);
//    }
//
//    /**
//     * 获取是否打开了重置手势密码
//     *
//     * @return
//     */
//    public boolean getGestureCodeIsOpen() {
//        return CreateGestureActivity.getGestureCodeIsOpenByUser(GestureManagerActivity.this);
//    }
//
//    /**
//     * 初始化是否展示重置
//     *
//     * @param isHasGesturePassword
//     */
//    private void initShowResetGesturePassWord(boolean isHasGesturePassword) {
//        findViewById(R.id.switch_gesture_change_code_layout).setVisibility(isHasGesturePassword ? View.VISIBLE : View.GONE);
//        SwitchView switchView = ((SwitchView) findViewById(R.id.switch_gesture_switchview));
//        switchView.setOpened(getHasGesturePassword() && getGestureCodeIsOpen());
//    }
//
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.back_layout:
//                finish();
//                break;
//            case R.id.switch_gesture_change_code_layout:
//                Bundle bundle = new Bundle();
//                bundle.putString("gesture_code_change", "reset");
//                IntentUtils.startActivity(GestureManagerActivity.this, GestureLoginActivity.class, bundle);
//                break;
//            default:
//                break;
//        }
//    }
}
