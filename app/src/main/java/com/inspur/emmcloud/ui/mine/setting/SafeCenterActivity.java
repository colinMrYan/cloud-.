package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.login.GetMDMStateResult;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * 账号、设备安全
 */

public class SafeCenterActivity extends BaseActivity {

    public static final String FINGER_PRINT_STATE = "finger_print_state";
    private static final int REQUEST_FACE_SETTING = 2;

    private LoadingDialog loadingDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_center);
        loadingDlg = new LoadingDialog(this);
        getMDMState();
    }

    /**
     * 设置设备管理layout显示状态
     *
     * @param mdmState
     */
    private void setMDMLayoutState() {
         int mdmState = PreferencesByUserAndTanentUtils.getInt(getApplicationContext(), "mdm_state", 1);
        (findViewById(R.id.device_manager_layout)).setVisibility((mdmState == 1) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TextView)(findViewById(R.id.safe_center_gesture_open_text))).setText((getHasGesturePassword()&&getGestureCodeIsOpen())
                ?getString(R.string.safe_center_enable)
                :getString(R.string.safe_center_unenable));
    }


    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.safe_center_face_layout:
                intent = new Intent();
                intent.putExtra("isFaceSetting",true);
                intent.setClass(this, FaceVerifyActivity.class);
                startActivityForResult(intent,REQUEST_FACE_SETTING);
                break;
            case R.id.safe_center_experience_face_layout:
                intent = new Intent();
                intent.putExtra("isFaceSetting",true);
                intent.setClass(this, FaceVerifyActivity.class);
                startActivityForResult(intent,REQUEST_FACE_SETTING);
                break;
            case R.id.safe_center_gesture_layout:
                if(getHasGesturePassword()&&getGestureCodeIsOpen()){
                    IntentUtils.startActivity(this, SwitchGestureActivity.class);
                }else{
                    IntentUtils.startActivity(this, CreateGestureCodeGuidActivity.class);
                }
                break;
            case R.id.device_manager_layout:
                IntentUtils.startActivity(this, DeviceManagerActivity.class);
                break;
            default:
                break;
        }
    }

    /**
     * 获取是否有手势解锁码
     * @return
     */
    private boolean getHasGesturePassword() {
        String gestureCode = CreateGestureActivity.getGestureCodeByUser(SafeCenterActivity.this);
        return !StringUtils.isBlank(gestureCode);
    }

    /**
     * 获取是否打开了重置手势密码
     * @return
     */
    public boolean getGestureCodeIsOpen(){
        return CreateGestureActivity.getGestureCodeIsOpenByUser(SafeCenterActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 获取设备管理状态
     */
    private void getMDMState() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            MineAPIService apiService = new MineAPIService(this);
            apiService.setAPIInterface(new Webservice());
            apiService.getMDMState();
        } else {
            setMDMLayoutState();
        }
    }

    private class Webservice extends APIInterfaceInstance {

        @Override
        public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            int mdmState = getMDMStateResult.getMdmState();
            PreferencesByUserAndTanentUtils.putInt(getApplicationContext(), "mdm_state", mdmState);
            setMDMLayoutState();

        }

        @Override
        public void returnMDMStateFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            setMDMLayoutState();
            WebServiceMiddleUtils.hand(SafeCenterActivity.this, error, errorCode);
        }


    }
}
