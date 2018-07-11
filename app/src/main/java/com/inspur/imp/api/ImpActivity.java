package com.inspur.imp.api;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.file.FileService;

import java.util.HashMap;
import java.util.Map;


public class ImpActivity extends ImpBaseActivity {

    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private ImpWebView webView;
    // 浏览文件resultCode
    private int FILEEXPLOER_RESULTCODE = 4;
    public static final int DO_NOTHING_RESULTCODE = 5;
    private Map<String, String> webViewHeaders;
    private TextView headerText;
    private LinearLayout loadFailLayout;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private String appId = "";
    private FrameLayout frameLayout;
    private RelativeLayout loadingLayout;
    private TextView loadingText;
    private String helpUrl = "";
    private HashMap<String, String> urlTilteMap = new HashMap<>();
    private ImpFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        boolean isWebAutoRotate = Boolean.parseBoolean(AppConfigCacheUtils.getAppConfigValue(this, Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
        //设置是否开启webview自动旋转
        setRequestedOrientation(isWebAutoRotate ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(Res.getLayoutID("activity_imp_hold"));
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        fragment = new ImpFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }

    /**
     * 在WebClient获取header
     *
     * @return
     */
    public Map<String, String> getWebViewHeaders() {
        return webViewHeaders;
    }

    /**
     * 初始化原生WebView的返回和关闭
     * （不是GS应用，GS应用有重定向，不容易实现返回）
     */
    public void initWebViewGoBackOrClose() {
        if (headerText != null) {
            (findViewById(Res.getWidgetID("imp_close_btn"))).setVisibility(webView.canGoBack() ? View.VISIBLE : View.GONE);
        }
    }

    public void onClick(View v) {
        fragment.onClick(v);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return fragment.onKeyDown();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PublicWay.photoService != null) {
            PublicWay.photoService.onActivityResult(requestCode, -2, data);
            return;
        }
        if (PublicWay.uploadPhotoService != null) {
            PublicWay.uploadPhotoService.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (PublicWay.selectStaffService != null) {
            PublicWay.selectStaffService.onActivityResult(requestCode, resultCode, data);
            return;
        }
        // 获取选择的文件
        else if (resultCode == FILEEXPLOER_RESULTCODE) {
            FileService.fileService.getAudioFilePath(data
                    .getStringExtra("filePath"));
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri uri = data == null || resultCode != Activity.RESULT_OK ? null
                    : data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ValueCallback<Uri[]> mUploadCallbackAboveL = webView
                        .getWebChromeClient().getValueCallbackAboveL();
                if (null == mUploadCallbackAboveL)
                    return;
                if (uri == null) {
                    mUploadCallbackAboveL.onReceiveValue(null);
                } else {
                    Uri[] uris = new Uri[]{uri};
                    mUploadCallbackAboveL.onReceiveValue(uris);
                }
                mUploadCallbackAboveL = null;
            } else {
                ValueCallback<Uri> mUploadMessage = webView
                        .getWebChromeClient().getValueCallback();
                if (null == mUploadMessage)
                    return;
                mUploadMessage.onReceiveValue(uri);
                mUploadMessage = null;
            }
        }
    }

}
