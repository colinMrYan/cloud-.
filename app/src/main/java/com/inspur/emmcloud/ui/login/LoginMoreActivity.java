package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.login.LoginMoreBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.find.ScanResultActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

/**
 * Created by yufuchang on 2018/1/30.
 */

public class LoginMoreActivity extends BaseActivity {
    private static final int SCAN_LOGIN_ENTERPRISE_INFO = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_more);
        findViewById(R.id.login_more_reset_btn).setVisibility(StringUtils.isBlank(PreferencesUtils
                .getString(LoginMoreActivity.this, "login_enterprise_name", ""))
                ? View.GONE : View.VISIBLE);
    }

    /**
     * onclick
     *
     * @param view
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.login_more_scan_btn:
                Intent intent = new Intent();
                intent.setClass(LoginMoreActivity.this, CaptureActivity.class);
                intent.putExtra("from", "loginMore");
                startActivityForResult(intent, SCAN_LOGIN_ENTERPRISE_INFO);
                break;
            case R.id.login_more_reset_btn:
                PreferencesUtils.putString(LoginMoreActivity.this, "login_enterprise_name", "");
                MyApplication.getInstance().setCloudId(Constant.DEFAULT_CLUSTER_ID);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SCAN_LOGIN_ENTERPRISE_INFO) {
            if (data.hasExtra("isDecodeSuccess")) {
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess", false);
                if (isDecodeSuccess) {
                    String msg = data.getStringExtra("msg");
                    if (JSONUtils.isJsonObjStringHasKey(msg, "u") && JSONUtils.isJsonObjStringHasKey(msg, "n")) {
                        showConfirmDialog(msg);
                    } else {
                        showUnKnownMsg(msg);
                    }
                } else {
                    ToastUtils.show(LoginMoreActivity.this, getString(R.string.qr_code_analysis_fail));
                    finish();
                }
            }
        }
    }

    /**
     * 确认，取消dialog
     *
     * @param msg
     */
    private void showConfirmDialog(String msg) {
        final LoginMoreBean loginMoreBean = new LoginMoreBean(msg);
        new MyQMUIDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage("您将要登录的企业:"+loginMoreBean.getName())
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("loginEnterprise", loginMoreBean.getName());
                        setResult(RESULT_OK, intent);
                        MyApplication.getInstance().setCloudId(StringUtils.isBlank(loginMoreBean.getUrl()) ? Constant.DEFAULT_CLUSTER_ID : loginMoreBean.getUrl());
                        findViewById(R.id.login_more_reset_btn).setVisibility(View.VISIBLE);
                        PreferencesUtils.putString(LoginMoreActivity.this, "login_enterprise_name", loginMoreBean.getName());
                        finish();
                    }
                })
                .show();
    }

    /**
     * 展示扫描到的信息
     *
     * @param msg
     */
    private void showUnKnownMsg(String msg) {
        Intent intent = new Intent();
        intent.putExtra("result", msg);
        intent.setClass(LoginMoreActivity.this, ScanResultActivity.class);
        startActivity(intent);
    }
}
