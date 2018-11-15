package com.inspur.emmcloud.ui.mine.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.qmuiteam.qmui.widget.QMUILoadingView;


/**
 * Created by libaochao on 2018/11/8.
 */

public class PortalLogInActivity extends BaseActivity {

    private WebView webview;

    private QMUILoadingView qmuiLoadingWebView;

    private LoadingDialog portalLoadingDialog  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_login);
        portalLoadingDialog = new LoadingDialog(this);
        portalLoadingDialog.show();
        String portUrl  =  getIntent().getExtras().getString("PortalUrl");
        webview = (WebView)findViewById(R.id.wv_show_login_detail);
        qmuiLoadingWebView = (QMUILoadingView)findViewById(R.id.qlv_wifi_portal_checkloading);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(portUrl);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                LoadingDialog.dimissDlg(portalLoadingDialog);

            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
            }
        });
    }



    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back_portal_login:
                finish();
                break;
            default:
                break;
        }
    }
}
