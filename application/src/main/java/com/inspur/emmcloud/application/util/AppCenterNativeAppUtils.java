package com.inspur.emmcloud.application.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;

import java.io.File;


/**
 * Created by Administrator on 2017/4/21.
 */

public class AppCenterNativeAppUtils {

    private Activity context;
    private App app;
    private LoadingDialog loadingDlg;
    private MyDialog passwordInputDlg;

    public void InstallOrOpen(Activity context, App app) {
        this.context = context;
        this.app = app;
        if (app.getPackageName().equals("cn.knowhowsoft.khmap5")) {
            app.setPackageName("com.knowhowsoft.khmap5");
            app.setInstallUri("http://office8.inspur.com:8082/inspur/file/apknew/KHMAP5-PUB-Client-Inspur_newc.apk");
            app.setMainActivityName("com.knowhowsoft.khmap5.WhatsnewActivity");
        }
        LogUtils.jasonDebug("app=" + app.getPackageName());
        if (AppUtils.isAppInstalled(context, app.getPackageName())) {
            if (app.getPackageName().equals("com.knowhowsoft.khmap5")) {
                getApprovalPassword();
            } else {
                openNativeApp();
            }
        } else {
            installApp();
        }
    }

    /**
     * 打开其他第三方原生应用
     */
    public void openNativeApp() {
        try {
            Intent intent;
            String packageName = app.getPackageName();
            String mainActivityName = app.getMainActivityName();
            LogUtils.jasonDebug("packageName=" + packageName);
            LogUtils.jasonDebug("mainActivityName=" + mainActivityName);
            if (!StringUtils.isBlank(packageName) && !StringUtils.isBlank(mainActivityName)) {
                intent = new Intent(Intent.ACTION_MAIN);
                ComponentName cn = new ComponentName(packageName, mainActivityName);
                intent.setComponent(cn);
                String myInfo = PreferencesUtils.getString(context, "myInfo");
                GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
                intent.putExtra("USERNAME", getMyInfoResult.getCode());
                String password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
                intent.putExtra("PASSWORD", password);
            } else {
                PackageManager packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(packageName);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(context, "应用未安装");
        }

    }

    /**
     * 获取审批的密码
     */
    private void getApprovalPassword() {
        String password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
        if (StringUtils.isBlank(password)) {
            showPasswordInputDlg();
        } else {
            veriryPassword(password);
        }
    }

    /**
     * 弹出密码输入框
     */
    private void showPasswordInputDlg() {
        passwordInputDlg = new MyDialog(context,
                R.layout.volume_dialog_update_name_input, R.style.userhead_dialog_bg);
        passwordInputDlg.setCancelable(false);
        final EditText inputEdit = (EditText) passwordInputDlg.findViewById(R.id.edit);
        inputEdit.setTypeface(Typeface.DEFAULT);
        inputEdit.setTransformationMethod(new PasswordTransformationMethod());
        (passwordInputDlg.findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = inputEdit.getText().toString();
                if (StringUtils.isBlank(password)) {
                    ToastUtils.show(context, R.string.login_please_input_password);
                } else if (password.length() < 6) {
                    ToastUtils.show(context, R.string.approval_input_password_valiad);
                } else {
                    try {
                        String encodePassword = EncryptUtils.encodeApprovalPassword(password);
                        veriryPassword(encodePassword);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.show(context, R.string.application_app_password_encrypt_error);
                    }
                }
            }
        });

        (passwordInputDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordInputDlg.dismiss();
            }
        });
        passwordInputDlg.show();
    }

    private void veriryPassword(String password) {
        if (NetUtils.isNetworkConnected(context)) {
            loadingDlg = new LoadingDialog(context);
            loadingDlg.show();
            ApplicationAPIService apiService = new ApplicationAPIService(context);
            apiService.setAPIInterface(new WebService());
            String myInfo = PreferencesUtils.getString(context, "myInfo");
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            apiService.veriryApprovalPassword(getMyInfoResult.getCode(), password);
        }
    }

    public void installApp() {
        if (NetUtils.isNetworkConnected(context)) {
            loadingDlg = new LoadingDialog(context);
            loadingDlg.show();
            loadingDlg.setText("");
            final String downloadStr = context.getString(R.string.application_app_download);
            APIDownloadCallBack progressCallback = new APIDownloadCallBack(context, app.getInstallUri()) {

                @Override
                public void callbackStart() {
                    loadingDlg.setText(downloadStr + "%0");
                }

                @Override
                public void callbackLoading(long total, long current, boolean isUploading) {
                    if (loadingDlg != null && loadingDlg.isShowing()) {
                        if (total == 0) {
                            total = 1;
                        }
                        loadingDlg.setHint(downloadStr + "100%");
                        int progress = (int) ((current * 100) / total);
                        loadingDlg.setText(downloadStr + progress + "%");
                    }
                }

                @Override
                public void callbackSuccess(File file) {
                    if (loadingDlg != null && loadingDlg.isShowing()) {
                        loadingDlg.dismiss();
                    }
                    FileUtils.openFile(context, file, true);
                }

                @Override
                public void callbackError(Throwable arg0, boolean arg1) {
                    if (loadingDlg != null && loadingDlg.isShowing()) {
                        loadingDlg.dismiss();
                    }
                    ToastUtils.show(context, R.string.download_fail);
                }

                @Override
                public void callbackCanceled(CancelledException e) {

                }
            };
            new DownLoaderUtils().startDownLoad(app.getInstallUri(), MyAppConfig.getFileDownloadDirPath() + app.getAppID() + ".apk", progressCallback);
        }
    }

    private class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnVeriryApprovalPasswordSuccess(String password) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (passwordInputDlg != null && passwordInputDlg.isShowing()) {
                passwordInputDlg.dismiss();
            }
            PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", password);
            openNativeApp();
        }

        @Override
        public void returnVeriryApprovalPasswordFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(context, R.string.application_app_password_error);
            PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", "");
            if (passwordInputDlg == null) {
                showPasswordInputDlg();
            } else if (!passwordInputDlg.isShowing()) {
                passwordInputDlg.show();
            }

        }
    }
}
