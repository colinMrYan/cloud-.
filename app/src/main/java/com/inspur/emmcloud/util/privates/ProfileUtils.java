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
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.config.Constant;
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
        if (ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_ROUTER)) {
            getUserProfile(isShowLoadingDlg);
        } else {
            if (commonCallBack != null) {
                commonCallBack.execute();
            }
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
        }
//        else{
//            showPromptDialog();
//        }
    }

    /**
     * 弹出提示
     */
    private void showPromptDialog() {
        final Dialog dialog = new MyDialog(activity, R.layout.dialog_profile_two_button);
        dialog.setCancelable(false);
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
                getUserProfile(true);
            }
        });
        dialog.show();
    }

    class WebService extends APIInterfaceInstance {
        @Override

        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null) {
                ToastUtils.show(MyApplication.getInstance(), R.string.user_not_bound_enterprise);
                MyApplication.getInstance().signout();
            } else {
                //存储上一个版本，不再有本地默认版本
                String myInfoOld = PreferencesUtils.getString(activity, "myInfo", "");
                PreferencesUtils.putString(activity, Constant.PREF_MY_INFO_OLD, myInfoOld);
                PreferencesUtils.putString(activity, "myInfo", getMyInfoResult.getResponse());
                ClusterBean chatClusterBeanOld = MutilClusterUtils.getClusterBean(MutilClusterUtils.ECM_CHAT);
                MyApplication.getInstance().initTanent();
                ClusterBean chatClusterBeanNew = MutilClusterUtils.getClusterBean(MutilClusterUtils.ECM_CHAT);
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_ROUTER, saveConfigVersion);
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
            LoadingDialog.dimissDlg(loadingDialog);
            //当统一更新接口无法返回正确消息，同时路由又无法获取成功时暂时不弹出提示框
            if (!StringUtils.isBlank(saveConfigVersion)) {
                showPromptDialog();
            }
        }
    }

    /**
     * 转到LoginActivity
     */
    private void startLoginActivity() {
        MyApplication.getInstance().signout();
        String appVersion = AppUtils.getVersion(activity);
    }
}
