package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import java.net.URLEncoder;

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
                R.layout.appcenter_dialog_approval_password_input, R.style.userhead_dialog_bg);
        passwordInputDlg.setCancelable(false);
        final EditText inputEdit = (EditText) passwordInputDlg.findViewById(R.id.edit);
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
                        ToastUtils.show(context, R.string.app_password_encrypt_error);
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
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebService());
            String myInfo = PreferencesUtils.getString(context, "myInfo");
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            apiService.veriryApprovalPassword(getMyInfoResult.getCode(), password);
        }
    }

    /**
     * 打开其他第三方Web原生应用
     */
    public void openWebApprovalApp() {
        String myInfo = PreferencesUtils.getString(context, "myInfo");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
        try {
            password = URLEncoder.encode(password, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
        }
        String url = app.getUri() + "?username=" + getMyInfoResult.getCode() + "&md5pw=" + password;
        UriUtils.openUrl(context, url);
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVeriryApprovalPasswordSuccess(String password) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (passwordInputDlg != null && passwordInputDlg.isShowing()) {
                passwordInputDlg.dismiss();
            }
            PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", password);
            openWebApprovalApp();
        }

        @Override
        public void returnVeriryApprovalPasswordFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(context, R.string.app_password_error);
            PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", "");
            if (passwordInputDlg == null) {
                showPasswordInputDlg();
            } else if (!passwordInputDlg.isShowing()) {
                passwordInputDlg.show();
            }

        }
    }
}
