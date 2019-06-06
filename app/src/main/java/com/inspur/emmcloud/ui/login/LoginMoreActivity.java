package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.bean.login.LoginMoreBean;
import com.inspur.emmcloud.ui.find.ScanResultActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/1/30.
 */
public class LoginMoreActivity extends BaseActivity {

    private static final int SCAN_LOGIN_ENTERPRISE_INFO = 5;
    @BindView(R.id.tv_current_enterprise_name)
    TextView currentEnterpriseNameText;
    @BindView(R.id.ll_reset_enterprise)
    LinearLayout resetEnterpriseLayout;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_login_more;
    }



    /**
     * 初始化
     */
    private void initView() {
        String enterpriseName = PreferencesUtils
                .getString(LoginMoreActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, "");
        resetEnterpriseLayout.setVisibility(StringUtils.isBlank(enterpriseName)
                ? View.GONE : View.VISIBLE);
        currentEnterpriseNameText.setText(StringUtils.isBlank(enterpriseName) ? "" : getString(R.string.login_more_current_enterprise) + enterpriseName);
    }

    /**
     * onclick
     *
     * @param view
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ll_scan:
                AppUtils.openScanCode(this, SCAN_LOGIN_ENTERPRISE_INFO);
                break;
            case R.id.ll_reset_enterprise:
                showConfirmClearDialog();
                break;
        }
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog() {
        new CustomDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage(getString(R.string.confirm_clear))
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, "");
                    PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_CLOUD_IDM, Constant.DEFAULT_CLUSTER_ID);
                    PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                    finish();
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
     *
     * @param msg
     */
    private void showConfirmDialog(String msg) {
        final LoginMoreBean loginMoreBean = new LoginMoreBean(msg);
        new CustomDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage(getString(R.string.login_more_scan_find_left) + loginMoreBean.getName() + getString(R.string.login_more_scan_find_right))
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("loginEnterprise", loginMoreBean.getName());
                    setResult(RESULT_OK, intent);
                    PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_CLOUD_IDM, StringUtils.isBlank(loginMoreBean.getUrl()) ? Constant.DEFAULT_CLUSTER_ID : (loginMoreBean.getUrl() + "/"));
                    resetEnterpriseLayout.setVisibility(View.VISIBLE);
                    PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, loginMoreBean.getName());
                    PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                    finish();
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
