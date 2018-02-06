package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.login.LoginMoreBean;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;

/**
 * Created by yufuchang on 2018/1/30.
 */

public class LoginMoreActivity extends BaseActivity{
    private static final int SCAN_LOGIN_ENTERPRISE_INFO = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_more);
    }

    /**
     * onclick
     * @param view
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.login_more_scan_btn:
                Intent intent = new Intent();
                intent.setClass(LoginMoreActivity.this, CaptureActivity.class);
                intent.putExtra("from","loginMore");
                startActivityForResult(intent, SCAN_LOGIN_ENTERPRISE_INFO);
                break;
            case R.id.login_more_reset_btn:
                APIUri.URL_BASE_ID = "https://id.inspuronline.com/";
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SCAN_LOGIN_ENTERPRISE_INFO){
            if (data.hasExtra("isDecodeSuccess")) {
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess", false);
                if (isDecodeSuccess) {
                    String msg = data.getStringExtra("msg");
                    if(JSONUtils.isJsonObjStringHasKey(msg,"u") && JSONUtils.isJsonObjStringHasKey(msg,"n")){
                        LoginMoreBean loginMoreBean = new LoginMoreBean(msg);
                        Intent intent = new Intent();
                        intent.putExtra("loginEnterprise", loginMoreBean.getName() );
                        setResult(RESULT_OK,intent);
                        APIUri.URL_BASE_ID = StringUtils.isBlank(loginMoreBean.getUrl())? "https://id.inspuronline.com/" : loginMoreBean.getUrl();
                        findViewById(R.id.login_more_reset_btn).setVisibility(View.VISIBLE);
                        PreferencesUtils.putBoolean(LoginMoreActivity.this,"login_more",false);
                        finish();
                    }else{
                        ToastUtils.show(LoginMoreActivity.this,"未扫描到正确的系统");
                    }
                } else {
                    ToastUtils.show(LoginMoreActivity.this, getString(R.string.qr_code_analysis_fail));
                    finish();
                }
            }
        }
    }
}
