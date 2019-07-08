package com.inspur.emmcloud.login.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;

/**
 * 扫码登录页面
 */
@Route(path = Constant.AROUTER_CLASS_LOGIN_GS)
public class ScanQrCodeLoginGSActivity extends BaseActivity {
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        loadingDlg = new LoadingDialog(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
    }

    @Override
    public int getLayoutResId() {
        return R.layout.login_activity_scan_qrcode_login_gs;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_text) {
            finish();

        } else if (i == R.id.login_btn) {
            faceLogin();

        } else if (i == R.id.login_cancel_text) {
            finish();

        } else {
        }
    }

    private void faceLogin() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            Bitmap bitmap = ImageUtils.getBitmap(getApplicationContext(), "face_unlock.png");
            String bitmapBase64 = ImageUtils.bitmapToBase64(bitmap);
            String token = getIntent().getStringExtra("token");
            LoginAPIService apiService = new LoginAPIService(ScanQrCodeLoginGSActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.faceLoginGS(bitmapBase64, token);
        }
    }

    class WebService extends LoginAPIInterfaceImpl {

        @Override
        public void returnFaceLoginGSSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(getApplicationContext(), "GS客户端登录成功");
            finish();
        }

        @Override
        public void returnFaceLoginGSFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ScanQrCodeLoginGSActivity.this, error, errorCode);
        }
    }
}
