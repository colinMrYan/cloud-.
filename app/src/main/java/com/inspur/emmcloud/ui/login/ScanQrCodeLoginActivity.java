package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.login.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.lang.reflect.Method;


/**
 * Created by yufuchang on 2017/7/27.
 */

public class ScanQrCodeLoginActivity extends BaseActivity {
    private static final int SCAN_LOGIN_QRCODE_RESULT = 5;
    private LoadingDialog loadingDialog;
    private TextView scanLoginSysType;
    private boolean isLogin = true;

    @Override
    public void onCreate() {
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_scan_qrcode_login_result;
    }

    protected int getStatusType() {
        return STATUS_WHITE;
    }

    private void initViews() {
        loadingDialog = new LoadingDialog(ScanQrCodeLoginActivity.this);
        scanLoginSysType = (TextView) findViewById(R.id.scan_login_desktop_sys_type_text);
        changeLoginUI();
    }

    private void closeLoginInActivity() {
        finish();
    }

    /**
     * 识别到不同系统的pc客户端显示不同的样式
     */
    private void changeLoginUI() {

    }

    private void setStatusBarIconDark(boolean dark) {
        try {
            Object win = getWindow();
            Class<?> cls = win.getClass();
            Method method = cls.getDeclaredMethod("setStatusBarIconDark", boolean.class);
            method.invoke(win, dark);
        } catch (Exception e) {
            LogUtils.YfcDebug("statusBarIconDark,e=" + e);
        }
    }

    /**
     * 登录云+桌面版
     */
    private void loginDesktopCloudPlus() {
        String msg = "";
        if (getIntent().hasExtra("scanMsg")) {
            msg = getIntent().getStringExtra("scanMsg");
        }
        LogUtils.YfcDebug("msg:" + msg);
        AppAPIService appAPIService = new AppAPIService(ScanQrCodeLoginActivity.this);
        appAPIService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(ScanQrCodeLoginActivity.this)) {
            loadingDialog.show();
            appAPIService.sendLoginDesktopCloudPlusInfo(msg);
        }
    }

    /**
     * 登录失败修改ui
     */
    private void loginFailUI() {
        scanLoginSysType.setText("登录请求已过期，请尝试重新扫码");
        ((Button) findViewById(R.id.scan_login_desktop_button)).setText("重新登录");
    }

    /**
     * 扫描成功修改
     */
    private void scanSuccess() {
        scanLoginSysType.setText("Windows云＋登录确认");
        ((Button) findViewById(R.id.scan_login_desktop_button)).setText("登录");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
            case R.id.scan_cancle_login_desktop_button:
                finish();
                break;
            case R.id.scan_login_desktop_button:
                if (!isLogin) {
                    AppUtils.openScanCode(ScanQrCodeLoginActivity.this, SCAN_LOGIN_QRCODE_RESULT);
                    return;
                }
                loginDesktopCloudPlus();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == SCAN_LOGIN_QRCODE_RESULT)) {
            if (data.hasExtra("isDecodeSuccess")) {
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess", false);
                if (isDecodeSuccess) {
                    String msg = data.getStringExtra("msg");
                    LogUtils.YfcDebug("解析到的信息：" + msg);
//                    ScanQrCodeUtils.getScanQrCodeUtilsInstance(ScanQrCodeLoginActivity.this).handleActionWithMsg(msg);
                    isLogin = true;
                    scanSuccess();
                } else {
                    LogUtils.YfcDebug("解析失败");
                }
            }
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            ToastUtils.show(ScanQrCodeLoginActivity.this, "登录成功");
        }

        @Override
        public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loginFailUI();
            isLogin = false;
            WebServiceMiddleUtils.hand(ScanQrCodeLoginActivity.this, error, errorCode);
        }
    }
}
