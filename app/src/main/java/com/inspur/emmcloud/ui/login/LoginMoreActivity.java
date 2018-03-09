package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.login.LoginMoreBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.find.ScanResultActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
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
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        String enterpriseName = PreferencesUtils
                .getString(LoginMoreActivity.this, "login_enterprise_name", "");
        findViewById(R.id.login_more_reset_layout).setVisibility(StringUtils.isBlank(enterpriseName)
                ? View.GONE : View.VISIBLE);
        ((TextView)findViewById(R.id.login_more_enterprise_text)).setText(getString(R.string.login_more_current_enterprise)+enterpriseName);
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
            case R.id.login_more_scan_layout:
                Intent intent = new Intent();
                intent.setClass(LoginMoreActivity.this, CaptureActivity.class);
                intent.putExtra("from", "loginMore");
                startActivityForResult(intent, SCAN_LOGIN_ENTERPRISE_INFO);
                break;
            case R.id.login_more_reset_layout:
                showConfirmClearDialog();
                break;
        }
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog() {
        new MyQMUIDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage(getString(R.string.confirm_clear))
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
                        PreferencesUtils.putString(LoginMoreActivity.this, "login_enterprise_name", "");
                        PreferencesUtils.putString(LoginMoreActivity.this,"cloud_idm",Constant.DEFAULT_CLUSTER_ID);
                        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                        finish();
                    }
                })
                .show();
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
     * @param msg
     */
    private void showConfirmDialog(String msg) {
        final LoginMoreBean loginMoreBean = new LoginMoreBean(msg);
        new MyQMUIDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage(getString(R.string.login_more_scan_find_left)+loginMoreBean.getName()+getString(R.string.login_more_scan_find_right))
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
                        PreferencesUtils.putString(LoginMoreActivity.this,"cloud_idm",StringUtils.isBlank(loginMoreBean.getUrl()) ? Constant.DEFAULT_CLUSTER_ID : (loginMoreBean.getUrl()+"/"));
                        findViewById(R.id.login_more_reset_btn).setVisibility(View.VISIBLE);
                        PreferencesUtils.putString(LoginMoreActivity.this, "login_enterprise_name", loginMoreBean.getName());
                        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
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
