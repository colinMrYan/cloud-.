package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.GetMDMStateResult;
import com.inspur.emmcloud.setting.bean.LogOffResult;
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
    @BindView(R2.id.setting_safe_center_more)
    ImageView safeMoreImg;

    LoadingDialog loadingDialog;
    SettingAPIService apiService;
    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        apiService = new SettingAPIService(SafeCenterActivity.this);
        apiService.setAPIInterface(new WebService());
        initView();
        getUserInfoConfig();
        getMDMState();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_safe_center_activity;
    }

    private void initView() {
        loadingDialog = new LoadingDialog(this);
        safeGestureFaceText.setText(FingerPrintUtils.getFingerPrintInstance().isFingerPrintAvaiable(this) ?
                R.string.setting_safe_gesture_face : R.string.setting_safe_center_gesture);
        setUserInfoConfig(null);
        setMDMLayoutState(1);
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

    private void getMDMState() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDialog.show();
            apiService.getMDMState();
        } else {
            setMDMLayoutState(null);
        }
    }

    /**
     * 设置设备管理layout显示状态
     *
     * @param mdmState
     */
    private void setMDMLayoutState(Integer mdmState) {
        if (mdmState == null) {
            mdmState = PreferencesByUserAndTanentUtils.getInt(getApplicationContext(), "mdm_state", 1);
        }
        (findViewById(R.id.rl_device_manager)).setVisibility((mdmState == 1) ? View.VISIBLE : View.GONE);
    }


    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_setting_safe_gesture_face) {
//            if (CreateGestureActivity.getGestureCodeIsOpenByUser(this)) {
                IntentUtils.startActivity(this, SafeGustureFaceSettingActivity.class);
//            } else {
//                IntentUtils.startActivity(SafeCenterActivity.this, CreateGestureActivity.class);
//            }

        } else if (id == R.id.rl_setting_face_unlock) {
            intentFaceVerifyActivity(true);
        } else if (id == R.id.rl_setting_safe_account_device) {
            IntentUtils.startActivity(this, DeviceManagerActivity.class);
        } else if (id == R.id.rl_password_modify) {
            String modifyUrl = PreferencesByUsersUtils.getString(this, Constant.PREF_LOGIN_MODIFY_URL);
            if (!StringUtils.isEmpty(modifyUrl) && modifyUrl.startsWith("http")) {
                Bundle bundle = new Bundle();
                bundle.putString("uri", modifyUrl);
                ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
                return;
            }
            ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_PASSWORD_MODIFY).navigation();
        } else if (id == R.id.rl_password_reset) {
            String forgetUrl = PreferencesByUsersUtils.getString(this, Constant.PREF_LOGIN_FORGET_URL);
            if (!StringUtils.isEmpty(forgetUrl) && forgetUrl.startsWith("http")) {
                Bundle bundle = new Bundle();
                bundle.putString("uri", forgetUrl);
                ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
                return;
            }
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            Bundle bundle = new Bundle();
            bundle.putInt("extra_mode", 2);
            bundle.putString("extra_phone", getMyInfoResult.getPhoneNumber());
            ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_BY_SMS).with(bundle).navigation();
        } else if (id == R.id.setting_safe_center_more){
            showOperationDialog();
        }
    }

    private void showOperationDialog() {
        ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener onSheetItemClickListener = new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                String tag = (String) itemView.getTag();
                if (tag.equals(getString(R.string.setting_log_off))) {
                    new CustomDialog.MessageDialogBuilder(SafeCenterActivity.this)
                            .setMessage(SafeCenterActivity.this.getString(R.string.setting_log_off_dialog_info))
                            .setNegativeButton(SafeCenterActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(SafeCenterActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    apiService.logOffAccount();
                                }
                            })
                            .setCancelable(false).show();
                }
                dialog.dismiss();
            }
        };

        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(this);
            builder.addItem(getString(R.string.setting_log_off));
        builder.setOnSheetItemClickListener(onSheetItemClickListener)
                .setItemColor(DarkUtil.getTextColor())
                .build()
                .show();
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

        @Override
        public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            int mdmState = getMDMStateResult.getMdmState();
            PreferencesByUserAndTanentUtils.putInt(getApplicationContext(), "mdm_state", mdmState);
            setMDMLayoutState(mdmState);

        }

        @Override
        public void returnMDMStateFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            setMDMLayoutState(null);
        }

        @Override
        public void returnLogOffSuccess(LogOffResult logOffResult) {
            ToastUtils.show(getString(R.string.setting_log_offed));
            BaseApplication.getInstance().signout();
        }
    }
}
