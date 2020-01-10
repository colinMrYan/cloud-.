package com.inspur.emmcloud.application.util;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;

/**
 * Created by yufuchang on 2018/1/17.
 */

public class AppCenterApprovalUtils {

    private Activity context;
    private LoadingDialog loadingDlg;
    private MyDialog passwordInputDlg;
    private App app;

    /**
     * 打开Web行政审批
     */
    public void openApprovalApp(Activity context, App app) {
        this.context = context;
        this.app = app;
        getApprovalPassword();
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
                R.layout.appcenter_dialog_input_password, R.style.userhead_dialog_bg);
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

    /**
     * 验证密码
     *
     * @param password
     */
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


    private class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnVeriryApprovalPasswordSuccess(String password, String locationUrl) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (passwordInputDlg != null && passwordInputDlg.isShowing()) {
                passwordInputDlg.dismiss();
            }
            PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", password);
            locationUrl = "http://ishenpi.inspur.com:8090" + locationUrl;
            ApplicationUriUtils.openUrl(context, locationUrl);
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
