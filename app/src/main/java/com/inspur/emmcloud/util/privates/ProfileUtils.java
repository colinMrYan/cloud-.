package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

/**
 * Created by yufuchang on 2018/6/27.
 */

public class ProfileUtils {

    private Activity activity;
    private CommonCallBack commonCallBack;
    private LoadingDialog loadingDialog;
    public ProfileUtils(Activity activity,CommonCallBack commonCallBack){
        this.activity = activity;
        this.commonCallBack = commonCallBack;
        loadingDialog = new LoadingDialog(activity);
    }

    /**
     * 初始化Profile，如果已经有了直接用，如果没有则从网络获取
     */
    public void initProfile(){
        if(checkNeedGetProfile()){
            getUserProfile();
        }else{
            String appVersion = AppUtils.getVersion(activity);
            PreferencesUtils.putString(activity, "previousVersion",
                    appVersion);
            commonCallBack.execute();
        }
    }

    /**
     * 获取用户的个人信息
     */
    private void getUserProfile() {
        if (NetUtils.isNetworkConnected(activity, true)) {
            loadingDialog.show();
            LoginAPIService apiServices = new LoginAPIService(activity);
            apiServices.setAPIInterface(new WebService());
            apiServices.getMyInfo();
        }else{
            showPromptDialog();
        }
    }

    /**
     * 弹出提示
     */
    private void showPromptDialog() {
        final Dialog dialog = new MyDialog(activity, R.layout.dialog_profile_two_button);
        dialog.setCanceledOnTouchOutside(false);
        ((TextView) dialog.findViewById(R.id.show_text)).setText(R.string.net_work_fail);
        dialog.findViewById(R.id.btn_re_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startLoginActivity();
            }
        });
        dialog.findViewById(R.id.btn_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                getUserProfile();
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0) {
                    dialog.dismiss();
                    activity.finish();
                }
                return false;
            }
        });
        dialog.show();
    }

    /**
     * 判断是否需要获取新的Profile
     * 如果是登录状态，并且进行了升级则需要获取profile
     * @return
     */
    private boolean checkNeedGetProfile() {
        String accessToken = PreferencesUtils.getString(
                activity, "accessToken", "");
        return !StringUtils.isBlank(accessToken) && AppUtils.isAppHasUpgraded(activity) && AppUtils.isLower202Version(activity);
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            //存储上一个版本，不再有本地默认版本
            PreferencesUtils.putString(activity, Constant.PREF_MY_INFO_OLD,PreferencesUtils.getString(activity,"myInfo",""));
            PreferencesUtils.putString(activity, "myInfo", getMyInfoResult.getResponse());
            MyApplication.getInstance().initTanent();
            String appVersion = AppUtils.getVersion(activity);
            PreferencesUtils.putString(activity, "previousVersion",
                    appVersion);
            commonCallBack.execute();
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            showPromptDialog();
        }
    }

    /**
     * 转到LoginActivity
     */
    private void startLoginActivity(){
        MyApplication.getInstance().signout();
        String appVersion = AppUtils.getVersion(activity);
        PreferencesUtils.putString(activity.getApplicationContext(), "previousVersion",
                appVersion);
    }
}
