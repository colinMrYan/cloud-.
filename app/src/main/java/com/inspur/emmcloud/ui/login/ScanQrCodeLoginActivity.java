package com.inspur.emmcloud.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by yufuchang on 2017/7/27.
 */

public class ScanQrCodeLoginActivity extends BaseActivity {
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this, R.color.scan_result_color);
//        setStatusBarIconDark(true);
        setMiuiStatusBarDarkMode(this,true);
        setContentView(R.layout.activity_scan_qrcode_login_result);
        initViews();
    }

    private void initViews() {
        loadingDialog = new LoadingDialog(ScanQrCodeLoginActivity.this);
    }

    private void closeLoginInActivity() {
        finish();
    }

    private void changeLoginUI() {

    }

    private void setStatusBarIconDark(boolean dark) {
        try {
            Object win = getWindow();
            Class<?> cls = win.getClass();
            Method method = cls.getDeclaredMethod("setStatusBarIconDark", boolean.class);
            method.invoke(win, dark);
        } catch (Exception e) {
            LogUtils.YfcDebug( "statusBarIconDark,e=" + e);
        }
    }

    /**
     * 登录云+桌面版
     *
     * @param msg
     */

    private void loginDesktopCloudPlus(String msg) {
        AppAPIService appAPIService = new AppAPIService(ScanQrCodeLoginActivity.this);
        appAPIService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(ScanQrCodeLoginActivity.this)) {
            loadingDialog.show();
            appAPIService.sendLoginDesktopCloudPlusInfo(msg);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
            case R.id.scan_cancle_login_desktop_button:
                finish();
                break;
            case R.id.scan_login_desktop_button:
//                setStatusBarIconDark(false);
                setMiuiStatusBarDarkMode(this,false);
                break;
        }

    }

    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
            WebServiceMiddleUtils.hand(ScanQrCodeLoginActivity.this, error, errorCode);
        }
    }
}
