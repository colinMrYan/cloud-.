package com.inspur.emmcloud.ui.login;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

public class ScanQrCodeLoginGSActivity extends BaseActivity {
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode_login_gs);
        loadingDlg = new LoadingDialog(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_text:
                finish();
                break;
            case R.id.login_btn:

                faceLogin();
                break;
            case R.id.login_cancel_text:
                finish();
                break;
            default:
                break;
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

    class WebService extends APIInterfaceInstance {

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
