package com.inspur.emmcloud.util.privates;

import android.app.Activity;

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
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

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
        new MyQMUIDialog.MessageDialogBuilder(activity)
                .setMessage(activity.getString(R.string.net_work_fail))
                .addAction(activity.getString(R.string.retry), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        getUserProfile();
                    }
                })
                .addAction(activity.getString(R.string.re_login), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        startLoginActivity();
                    }
                })
                .show();
    }

    /**
     * 判断是否需要获取新的Profile
     * 如果是登录状态，并且进行了升级则需要获取profile
     * @return
     */
    private boolean checkNeedGetProfile() {
        String accessToken = PreferencesUtils.getString(
                activity, "accessToken", "");
        return !StringUtils.isBlank(accessToken) && AppUtils.isAppHasUpgraded(activity);
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            //存储上一个版本，不再有本地默认版本
            PreferencesUtils.putString(activity, Constant.PREF_MY_INFO_OLD,PreferencesUtils.getString(activity,"myInfo",""));
            PreferencesUtils.putString(activity, "myInfo", getMyInfoResult.getResponse());
            MyApplication.getInstance().initTanent();
            commonCallBack.execute();
            String appVersion = AppUtils.getVersion(activity);
            PreferencesUtils.putString(activity, "previousVersion",
                    appVersion);
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            new MyQMUIDialog.MessageDialogBuilder(activity)
                    .setMessage(activity.getString(R.string.net_work_fail))
                    .addAction(activity.getString(R.string.retry), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            getUserProfile();
                        }
                    })
                    .addAction(activity.getString(R.string.re_login), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            startLoginActivity();
                        }
                    })
                    .show();
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
        activity.finish();
    }
}
