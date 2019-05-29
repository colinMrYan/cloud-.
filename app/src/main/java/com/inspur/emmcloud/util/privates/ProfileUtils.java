package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import java.util.List;

/**
 * Created by yufuchang on 2018/6/27.
 */

public class ProfileUtils {

    private Activity activity;
    private CommonCallBack commonCallBack;
    private LoadingDialog loadingDialog;
    private String saveConfigVersion = "";
    private int retry = 0;

    public ProfileUtils(Activity activity, CommonCallBack commonCallBack) {
        this.activity = activity;
        this.commonCallBack = commonCallBack;
        loadingDialog = new LoadingDialog(activity);
    }

    /**
     * 初始化Profile，如果已经有了直接用，如果没有则从网络获取
     */
    public void initProfile() {
        initProfile(true);
    }

    public void initProfile(boolean isShowLoadingDlg) {
        if (isForceUpdateProfile() || ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_ROUTER)) {
            getUserProfile(isShowLoadingDlg);
        } else {
            callback();
        }
    }

    /**
     * 获取用户的个人信息
     */
    private void getUserProfile(boolean isShowLoadingDlg) {
        if (NetUtils.isNetworkConnected(activity, false)) {
            loadingDialog.show(isShowLoadingDlg);
            saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_ROUTER);
            LoginAPIService apiServices = new LoginAPIService(activity);
            apiServices.setAPIInterface(new WebService());
            apiServices.getMyInfo();
        } else {
            showPromptDialog();
        }
    }

    /**
     * 弹出提示
     */
    private void showPromptDialog() {
        //当强制更新或者统一更新接口无法返回正确消息，同时路由又无法获取成功时暂时不弹出提示框
        if (!MyApplication.getInstance().isIndexActivityRunning() && isForceUpdateProfile()) {
            final Dialog dialog = new MyDialog(activity, R.layout.basewidget_dialog_one_button);
            dialog.setCancelable(false);
            ((TextView) dialog.findViewById(R.id.show_text)).setText(R.string.net_work_fail);
            dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    MyApplication.getInstance().signout();
                }
            });
            dialog.show();
        } else {
            callback();
        }
    }

    private void callback() {
        PreferencesUtils.putString(MyApplication.getInstance(), Constant.PREF_APP_PREVIOUS_VERSION, AppUtils.getVersion(MyApplication.getInstance()));
        if (commonCallBack != null) {
            commonCallBack.execute();
        }
    }

    /**
     * 判断是否需要获取新的Profile
     * 如果是登录状态，并且进行了升级则需要获取profile
     *
     * @return
     */
    private boolean isForceUpdateProfile() {
        String accessToken = PreferencesUtils.getString(
                activity, "accessToken", "");
        //2.6.0以下版本使用消息V0的路由，2.6.0或以上使用消息V1的路由
        return !StringUtils.isBlank(accessToken) && AppUtils.isAppHasUpgraded(activity);
    }

    class WebService extends APIInterfaceInstance {
        @Override

        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null) {
                ToastUtils.show(MyApplication.getInstance(), R.string.login_user_not_bound_enterprise);
                MyApplication.getInstance().signout();
            } else {
                //存储上一个版本，不再有本地默认版本
                String myInfoOld = PreferencesUtils.getString(activity, "myInfo", "");
                PreferencesUtils.putString(activity, Constant.PREF_MY_INFO_OLD, myInfoOld);
                PreferencesUtils.putString(activity, "myInfo", getMyInfoResult.getResponse());
                ClusterBean chatClusterBeanOld = WebServiceRouterManager.getInstance().getClusterBean(WebServiceRouterManager.ECM_CHAT);
                MyApplication.getInstance().initTanent();
                ClusterBean chatClusterBeanNew = WebServiceRouterManager.getInstance().getClusterBean(WebServiceRouterManager.ECM_CHAT);
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_ROUTER, saveConfigVersion);
                String appVersion = AppUtils.getVersion(activity);
                PreferencesUtils.putString(activity, Constant.PREF_APP_PREVIOUS_VERSION,
                        appVersion);
                if (commonCallBack != null) {
                    commonCallBack.execute();
                }
                boolean isChatClusterBeanUnchanged = (chatClusterBeanOld == null && chatClusterBeanNew == null) || ((chatClusterBeanOld != null) && (chatClusterBeanNew != null) && (chatClusterBeanOld.getServiceVersion().equals(chatClusterBeanNew.getServiceVersion())));
                if (!isChatClusterBeanUnchanged) {
                    WebSocketPush.getInstance().closeWebsocket();
                    Intent intentLog = new Intent(activity,
                            IndexActivity.class);
                    intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intentLog);
                }
            }
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            if (retry == 0) {
                retry = retry + 1;
                getUserProfile(false);
            } else {
                LoadingDialog.dimissDlg(loadingDialog);
                //当统一更新接口无法返回正确消息，同时路由又无法获取成功时暂时不弹出提示框
                showPromptDialog();
            }

        }
    }

}
