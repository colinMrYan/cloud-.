package com.inspur.emmcloud.setting.ui.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.UserProfileInfoBean;
import com.inspur.emmcloud.setting.util.FingerPrintUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 账号、设备安全
 */

public class SafeCenterActivity extends BaseActivity {

    @BindView(R2.id.ll_password)
    LinearLayout passwordLayout;
    @BindView(R2.id.rl_password_modify)
    RelativeLayout passwordModifyLayout;
    @BindView(R2.id.rl_password_reset)
    RelativeLayout passwordResetLayout;
    @BindView(R2.id.tv_setting_safe_gesture_face)
    TextView safeGestureFaceText;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        getUserInfoConfig();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_safe_center_activity;
    }

    private void initView() {
        safeGestureFaceText.setText(FingerPrintUtils.getFingerPrintInstance().isFingerPrintAvaiable(this) ?
                R.string.setting_safe_gesture_face : R.string.setting_safe_center_gesture);
        setUserInfoConfig(null);
    }

    /**
     * 配置用户信息的显示和隐藏
     *
     * @param userProfileInfoBean
     */
    private void setUserInfoConfig(UserProfileInfoBean userProfileInfoBean) {
        if (userProfileInfoBean == null) {
            String response = PreferencesByUserAndTanentUtils.getString(getApplicationContext(), "user_profiles");
            if (!StringUtils.isBlank(response)) {
                userProfileInfoBean = new UserProfileInfoBean(response);
            }
        }
        if (userProfileInfoBean != null) {
            if (userProfileInfoBean.getShowModifyPsd() == 0 && userProfileInfoBean.getShowResetPsd() == 0) {
                passwordLayout.setVisibility(View.GONE);
            } else {
                passwordLayout.setVisibility(View.VISIBLE);
                passwordModifyLayout.setVisibility((userProfileInfoBean.getShowModifyPsd() == 0) ? View.GONE : View.VISIBLE);
                passwordResetLayout.setVisibility((userProfileInfoBean.getShowResetPsd() == 0) ? View.GONE : View.VISIBLE);
            }

        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_setting_safe_gesture_face) {
            if (CreateGestureActivity.getGestureCodeIsOpenByUser(this)) {
                IntentUtils.startActivity(this, SafeGustureFaceSettingActivity.class);
            } else {
                IntentUtils.startActivity(SafeCenterActivity.this, CreateGestureActivity.class);
            }

        } else if (id == R.id.rl_setting_face_unlock) {
            intentFaceVerifyActivity(true);
        } else if (id == R.id.rl_setting_safe_account_device) {
            IntentUtils.startActivity(this, DeviceManagerActivity.class);
        } else if (id == R.id.rl_password_modify) {
            ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_PASSWORD_MODIFY).navigation();
        } else if (id == R.id.rl_password_reset) {
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            Bundle bundle = new Bundle();
            bundle.putInt("extra_mode", 2);
            bundle.putString("extra_phone", getMyInfoResult.getPhoneNumber());
            ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_BY_SMS).with(bundle).navigation();
        }
    }

    /**
     * 打开设置人脸识别页面
     *
     * @param isFaceSettingOpen
     */
    private void intentFaceVerifyActivity(boolean isFaceSettingOpen) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceSettingOpen", isFaceSettingOpen);
        IntentUtils.startActivity(SafeCenterActivity.this, FaceSettingActivity.class, bundle);
    }

    /**
     * 获取用户信息配置
     */
    private void getUserInfoConfig() {
        if (NetUtils.isNetworkConnected(SafeCenterActivity.this, false)) {
            SettingAPIService apiService = new SettingAPIService(SafeCenterActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getUserProfileConfigInfo();
        } else {
            setUserInfoConfig(null);
        }
    }


    public class WebService extends SettingAPIInterfaceImpl {
        @Override
        public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {
            setUserInfoConfig(userProfileInfoBean);
            PreferencesByUserAndTanentUtils.putString(getApplicationContext(), "user_profiles",
                    userProfileInfoBean.getResponse());
        }

        @Override
        public void returnUserProfileConfigFail(String error, int errorCode) {
        }

    }
}
