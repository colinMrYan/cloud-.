package com.inspur.imp.api;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.engine.webview.ImpWebChromeClient;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.file.FileService;
import com.inspur.mdm.MDM;

import java.util.HashMap;
import java.util.Map;


public class ImpActivity extends ImpBaseActivity {

    public static final String USERAGENT = "Mozilla/5.0 (Linux; U; isInImp; Android "
            + Build.VERSION.RELEASE
            + "; en-us; "
            + Build.MODEL
            + " Build/FRF91) AppleWebKit/533.1 "
            + "(KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private ImpWebView webView;
    // 浏览文件resultCode
    private int FILEEXPLOER_RESULTCODE = 4;
    private RelativeLayout progressLayout;
    private Map<String, String> extraHeaders;
    private TextView headerText;
    private LinearLayout loadFailLayout;
    private boolean isMDM = false;//mdm页面
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private int blackFontColor;
    private int lightModeFontColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplicationContext()).addActivity(this);
        setContentView(Res.getLayoutID("activity_imp"));
        initViews();
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        progressLayout = (RelativeLayout) findViewById(Res
                .getWidgetID("progress_layout"));
        loadFailLayout = (LinearLayout) findViewById(Res.getWidgetID("load_error_layout"));
        webView = (ImpWebView) findViewById(Res.getWidgetID("webview"));
        if(getIntent().hasExtra("is_zoomable")){
            int isZoomable = getIntent().getIntExtra("is_zoomable",0);
            if(isZoomable == 0){
                ((Button)findViewById(R.id.imp_change_font_size_btn)).setVisibility(View.GONE);
            }else if(isZoomable == 1){
                ((Button)findViewById(R.id.imp_change_font_size_btn)).setVisibility(View.VISIBLE);
            }
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String url = "";
        Uri uri = getIntent().getData();
        boolean isUriHasTitle = false;
        String title = "";
        if (uri != null) {
            String host = uri.getHost();
            url = "https://emm.inspur.com/ssohandler/gs_msg/" + host;
            String openMode = uri.getQueryParameter("openMode");
            isUriHasTitle = (openMode != null && openMode.equals("1")) ? true : false;
        } else {
            url = getIntent().getExtras().getString("uri");
        }
        if (getIntent().hasExtra("appName")) {
            isUriHasTitle = true;
            title = getIntent().getExtras().getString("appName");
        }
        if (isUriHasTitle) {
            headerText = (TextView) findViewById(Res.getWidgetID("header_text"));
            webView.setProperty(progressLayout, headerText, loadFailLayout);
            initWebViewGoBackOrClose();
            (findViewById(Res.getWidgetID("header_layout")))
                    .setVisibility(View.VISIBLE);
            headerText.setText(title);
        } else {
            webView.setProperty(progressLayout, null, loadFailLayout);
        }

        String token = ((MyApplication) getApplicationContext())
                .getToken();
        isMDM = getIntent().hasExtra("function") && getIntent().getStringExtra("function").equals("mdm");
        if (isMDM) {
            token = PreferencesUtils.getString(this, "mdm_accessToken");
        }
        setOauthHeader(token);
        setLangHeader(UriUtils.getLanguageCookie(this));
        setUserAgent("/emmcloud/" + AppUtils.getVersion(this));
        webView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

        (findViewById(Res.getWidgetID("refresh_text"))).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFailLayout.setVisibility(View.GONE);
                webView.reload();
            }
        });
        webView.loadUrl(url, extraHeaders);
        progressLayout.setVisibility(View.VISIBLE);
        lightModeFontColor = ContextCompat.getColor(ImpActivity.this, R.color.app_dialog_day_font_color);
        blackFontColor = ContextCompat.getColor(ImpActivity.this, R.color.black);
        int textSize = PreferencesByUserUtils.getInt(ImpActivity.this, "app_crm_font_size", MyAppWebConfig.NORMAL);
        webView.getSettings().setTextZoom(textSize);
    }

    /**
     * 初始化原生WebView的返回和关闭
     * （不是GS应用，GS应用有重定向，不容易实现返回）
     */
    private void initWebViewGoBackOrClose() {
        final TextView buttonBack = (TextView) findViewById(Res.getWidgetID("imp_back_btn"));
        final TextView buttonClose = (TextView) findViewById(Res.getWidgetID("imp_close_btn"));
        buttonClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webView.getWebChromeClient().setOnFinishLoadUrlListener(
                new ImpWebChromeClient.OnFinishLoadUrlListener() {
                    @Override
                    public void OnFinishLoadUrlListener(boolean isFinish) {
                        (findViewById(Res
                                .getWidgetID("header_layout")))
                                .setVisibility(View.VISIBLE);
                        if (webView.canGoBack()) {
                            buttonClose.setVisibility(View.VISIBLE);
                        } else {
                            buttonClose.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * 返回
     */
    private void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();// 返回上一页面
        } else {
            finishActivity();
        }
    }

    private void finishActivity() {
        finish();// 退出程序
        if (getIntent().hasExtra("function") && getIntent().getStringExtra("function").equals("mdm")) {
            new MDM().getMDMListener().MDMStatusNoPass();
        }
    }

//	/**
//	 * 设置cookie
//	 */
//	private void setCookies(String url, String cookie) {
//		// TODO Auto-generated method stub
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.setAcceptCookie(true);
//		cookieManager.acceptCookie();
//		cookieManager.setCookie(url, cookie);
//	}

    private void setUserAgent(String userAgentExtra) {
        // TODO Auto-generated method stub
        WebSettings settings = webView.getSettings();
        String userAgent = settings.getUserAgentString();
        userAgent = userAgent + userAgentExtra;
        settings.setUserAgentString(userAgent);
        settings.enableSmoothTransition();
        settings.setJavaScriptEnabled(true);
        //禁用缩放
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setDisplayZoomControls(false);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        settings.setDomStorageEnabled(true);


    }

    private void setOauthHeader(String OauthHeader) {
        extraHeaders = new HashMap<>();
        extraHeaders.put("Authorization", OauthHeader);
        extraHeaders.put("X-ECC-Current-Enterprise", ((MyApplication) getApplicationContext()).getInterpriseId());
    }

    private void setLangHeader(String langHeader) {
        extraHeaders.put("lang", langHeader);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imp_change_font_size_btn:
                showChangeFontSizeDialog();
                break;
            case R.id.app_imp_crm_font_normal_btn:
                changeNewsFontSize(MyAppWebConfig.SMALLER);
                break;
            case R.id.app_imp_crm_font_middle_btn:
                changeNewsFontSize(MyAppWebConfig.NORMAL);
                break;
            case R.id.app_imp_crm_font_big_btn:
                changeNewsFontSize(MyAppWebConfig.LARGER);
                break;
            case R.id.app_imp_crm_font_biggest_btn:
                changeNewsFontSize(MyAppWebConfig.LARGEST);
                break;
            default:
                goBack();
                break;
        }

    }

    /**
     * 打开修改字体的dialog
     */
    private void showChangeFontSizeDialog() {
        View view = getLayoutInflater().inflate(R.layout.app_imp_crm_font_dialog, null);
        Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        initFontSizeDialogViews(view);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置Dialog的透明度
        wl.dimAmount = 0.31f;
        dialog.getWindow().setAttributes(wl);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        initWebViewTextSize(0);
        dialog.show();
    }

    /**
     * 初始化Dialog的Views
     *
     * @param view
     */
    private void initFontSizeDialogViews(View view) {
        normalBtn = (Button) view.findViewById(R.id.app_imp_crm_font_normal_btn);
        normalBtn.setText(getString(R.string.news_font_smaller));
        middleBtn = (Button) view.findViewById(R.id.app_imp_crm_font_middle_btn);
        middleBtn.setText(getString(R.string.news_font_normal));
        bigBtn = (Button) view.findViewById(R.id.app_imp_crm_font_big_btn);
        bigBtn.setText(getString(R.string.news_font_big_text));
        biggestBtn = (Button) view.findViewById(R.id.app_imp_crm_font_biggest_btn);
        biggestBtn.setText(getString(R.string.news_font_biggest_text));
    }

    /**
     * 改变WebView字体大小
     *
     * @param textZoom
     */
    private void changeNewsFontSize(int textZoom) {
        WebSettings webSettings = webView.getSettings();
        PreferencesByUserUtils.putInt(ImpActivity.this, "app_crm_font_size", textZoom);
        webSettings.setTextZoom(textZoom);
        initWebViewTextSize(textZoom);
    }

    /**
     * 初始化WebView的字体大小
     */
    private void initWebViewTextSize(int textZoom) {
        int textSize = PreferencesByUserUtils.getInt(ImpActivity.this, "app_crm_font_size", MyAppWebConfig.NORMAL);
        if(textZoom != 0){
            textSize = textZoom;
        }
        switch (textSize) {
            case MyAppWebConfig.SMALLESET:
                chooseNormalFont();
                break;
            case MyAppWebConfig.SMALLER:
                chooseNormalFont();
                break;
            case MyAppWebConfig.NORMAL:
                chooseMiddleFont();
                break;
            case MyAppWebConfig.LARGER:
                chooseBigFont();
                break;
            case MyAppWebConfig.LARGEST:
                chooseBiggestFont();
                break;
            default:
                chooseNormalFont();
                break;
        }
    }


    /**
     * 选择正常字体
     */
    private void chooseNormalFont() {

        middleBtn.setTextColor(blackFontColor);
        bigBtn.setTextColor(blackFontColor);
        biggestBtn.setTextColor(blackFontColor);
        normalBtn.setTextColor(lightModeFontColor);

    }

    /**
     * 选择中字体
     */
    private void chooseMiddleFont() {

        normalBtn.setTextColor(blackFontColor);
        bigBtn.setTextColor(blackFontColor);
        biggestBtn.setTextColor(blackFontColor);
        middleBtn.setTextColor(lightModeFontColor);
    }

    /**
     * 选择大字体
     */
    private void chooseBigFont() {

        normalBtn.setTextColor(blackFontColor);
        middleBtn.setTextColor(blackFontColor);
        biggestBtn.setTextColor(blackFontColor);
        bigBtn.setTextColor(lightModeFontColor);
    }

    /**
     * 选择超大字体
     */
    private void chooseBiggestFont() {
        normalBtn.setTextColor(blackFontColor);
        middleBtn.setTextColor(blackFontColor);
        bigBtn.setTextColor(blackFontColor);
        biggestBtn.setTextColor(lightModeFontColor);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                LogUtils.jasonDebug("canGoBack");
                webView.goBack();// 返回上一页面
                return true;
            } else {
                LogUtils.jasonDebug("not------canGoBack");
                finishActivity();// 退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (PublicWay.photoService != null) {
            PublicWay.photoService.onActivityResult(requestCode, -2, data);
            return;
        }
        if (PublicWay.uploadPhotoService != null) {
            PublicWay.uploadPhotoService.onActivityResult(requestCode, resultCode, data);
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
