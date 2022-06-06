package com.inspur.emmcloud.login.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.componentservice.web.WebService;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.R2;
import com.inspur.emmcloud.login.bean.LoginMoreBean;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/1/30.
 */
public class LoginMoreActivity extends BaseActivity {

    private static final int SCAN_LOGIN_ENTERPRISE_INFO = 5;
    @BindView(R2.id.tv_current_enterprise_name)
    TextView currentEnterpriseNameText;
    @BindView(R2.id.ll_reset_enterprise)
    LinearLayout resetEnterpriseLayout;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.login_activity_login_more;
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
        int i = view.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.ll_scan) {
            AppUtils.openScanCode(this, SCAN_LOGIN_ENTERPRISE_INFO);

        } else if (i == R.id.ll_reset_enterprise) {
            showConfirmClearDialog();

        }
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog() {
        new CustomDialog.MessageDialogBuilder(LoginMoreActivity.this)
                .setMessage(getString(R.string.login_confirm_clear))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, "");
                        PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_CLOUD_IDM, "");
                        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_LOGIN_FORGET_URL, "");
                        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_LOGIN_MODIFY_URL, "");
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
                    PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_LOGIN_FORGET_URL, JSONUtils.getString(msg, "forgetPassword", ""));
                    PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_LOGIN_MODIFY_URL, JSONUtils.getString(msg, "modifyPassword", ""));
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("loginEnterprise", loginMoreBean.getName());
                        setResult(RESULT_OK, intent);
                        //todo  此处应做非空判断，如果为空需要给出提示，后续版本进行更改
                        if (!StringUtils.isBlank(loginMoreBean.getUrl())) {
                            PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_CLOUD_IDM, loginMoreBean.getUrl() + "/");
                        }
                        resetEnterpriseLayout.setVisibility(View.VISIBLE);
                        PreferencesUtils.putString(LoginMoreActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, loginMoreBean.getName());
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
        Router router = Router.getInstance();
        if (router.getService(WebService.class) != null) {
            WebService service = router.getService(WebService.class);
            service.showScanResult(LoginMoreActivity.this, msg);
        }
    }
}
