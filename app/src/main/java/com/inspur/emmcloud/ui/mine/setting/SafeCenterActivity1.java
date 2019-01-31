package com.inspur.emmcloud.ui.mine.setting;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;

/**
 * 账号、设备安全
 */

@ContentView(R.layout.activity_safe_center)
public class SafeCenterActivity1 extends BaseActivity {
//
//    @ViewInject(R.id.safe_center_gesture_open_text)
//    private TextView getstureOpenText;
//
//    @ViewInject(R.id.safe_center_face_open_text)
//    private TextView faceOpenText;
//
//    @ViewInject(R.id.device_manager_layout)
//    private RelativeLayout deviceManagerLayout;
//
//
//    public static final String FINGER_PRINT_STATE = "finger_print_state";
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setMDMLayoutState();
//        getMDMState();
//    }
//
//    /**
//     * 设置设备管理layout显示状态
//     *
//     * @param mdmState
//     */
//    private void setMDMLayoutState() {
//        int mdmState = PreferencesByUserAndTanentUtils.getInt(getApplicationContext(), "mdm_state", 0);
//        deviceManagerLayout.setVisibility((mdmState == 1) ? View.VISIBLE : View.GONE);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        getstureOpenText.setText((getHasGesturePassword() && getGestureCodeIsOpen())? getString(R.string.safe_center_enable): getString(R.string.safe_center_unenable));
//        faceOpenText.setText(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this) ? getString(R.string.safe_center_enable) : getString(R.string.safe_center_unenable));
//    }
//
//
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.ibt_back:
//                finish();
//                break;
//            case R.id.safe_center_face_layout:
//                IntentUtils.startActivity(this, FaceVerifyManagerActivity.class);
//                break;
//            case R.id.safe_center_gesture_layout:
//                if (getHasGesturePassword() && getGestureCodeIsOpen()) {
//                    IntentUtils.startActivity(this, GestureManagerActivity.class);
//                } else {
//                    IntentUtils.startActivity(this, CreateGestureCodeGuidActivity.class);
//                }
//                break;
//            case R.id.device_manager_layout:
//                IntentUtils.startActivity(this, DeviceManagerActivity.class);
//                break;
//            default:
//                break;
//        }
//    }
//
//    /**
//     * 获取是否有手势解锁码
//     *
//     * @return
//     */
//    private boolean getHasGesturePassword() {
//        String gestureCode = CreateGestureActivity.getGestureCodeByUser(SafeCenterActivity1.this);
//        return !StringUtils.isBlank(gestureCode);
//    }
//
//    /**
//     * 获取是否打开了重置手势密码
//     *
//     * @return
//     */
//    public boolean getGestureCodeIsOpen() {
//        return CreateGestureActivity.getGestureCodeIsOpenByUser(SafeCenterActivity1.this);
//    }
//
//
//    /**
//     * 获取设备管理状态
//     */
//    private void getMDMState() {
//        if (NetUtils.isNetworkConnected(this)) {
//            MineAPIService apiService = new MineAPIService(this);
//            apiService.setAPIInterface(new Webservice());
//            apiService.getMDMState();
//        }
//    }
//
//    private class Webservice extends APIInterfaceInstance {
//
//        @Override
//        public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {
//            int mdmState = getMDMStateResult.getMdmState();
//            PreferencesByUserAndTanentUtils.putInt(getApplicationContext(), "mdm_state", mdmState);
//            setMDMLayoutState();
//
//        }
//
//        @Override
//        public void returnMDMStateFail(String error, int errorCode) {
//            WebServiceMiddleUtils.hand(SafeCenterActivity1.this, error, errorCode);
//        }
//
//
//    }
}
