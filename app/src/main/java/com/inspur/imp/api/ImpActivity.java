package com.inspur.imp.api;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.MDM.MDM;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
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
//        initViews();
        fragment = new ImpFragment();
        //获取到FragmentManager，在V4包中通过getSupportFragmentManager，
        //在系统中原生的Fragment是通过getFragmentManager获得的。
        FragmentManager fragmentManager = getFragmentManager();
        //2.开启一个事务，通过调用beginTransaction方法开启。
        FragmentTransaction MfragmentTransaction = fragmentManager.beginTransaction();
        //向容器内加入Fragment，一般使用add或者replace方法实现，需要传入容器的id和Fragment的实例。
        MfragmentTransaction.add(R.id.fl_container,fragment);
        //提交事务，调用commit方法提交。
        MfragmentTransaction.commit();
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        loadingLayout = (RelativeLayout) findViewById(Res.getWidgetID("loading_layout"));
        loadingText = (TextView) findViewById(Res.getWidgetID("loading_text"));
        frameLayout = (FrameLayout) findViewById(Res.getWidgetID("videoContainer"));
        loadFailLayout = (LinearLayout) findViewById(Res.getWidgetID("load_error_layout"));
        webView = (ImpWebView) findViewById(Res.getWidgetID("webview"));
        showLoadingDlg(getString(Res.getStringID("@string/loading_text")));
        if (getIntent().hasExtra("help_url")) {
            String helpUrl = getIntent().getStringExtra("help_url");
            if (!StringUtils.isBlank(helpUrl)) {
                this.helpUrl = helpUrl;
            }
        }
        if (getIntent().hasExtra("appId")) {
            appId = getIntent().getExtras().getString("appId");
        }
        String url = getIntent().getExtras().getString("uri");
        initWebViewHeaderLayout();
        setWebViewHeader();
        setWebViewUserAgent();
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
        webView.loadUrl(url, webViewHeaders);
        LogUtils.jasonDebug("url="+url);
        setWebViewFunctionVisiable();
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
     * 设置Webview自定义功能是否显示
     */
    private void setWebViewFunctionVisiable() {
        if (getIntent().hasExtra("is_zoomable")) {
            int isZoomable = getIntent().getIntExtra("is_zoomable", 0);
            if (isZoomable == 1 || !StringUtils.isBlank(helpUrl)) {
                findViewById(R.id.imp_change_font_size_btn).setVisibility(View.VISIBLE);
            }
            if (isZoomable == 1) {
                int textSize = PreferencesByUsersUtils.getInt(ImpActivity.this, "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
                webView.getSettings().setTextZoom(textSize);
            }
        }
    }

    /**
     * 初始化webview haader layout
     */
    private void initWebViewHeaderLayout() {
        if (getIntent().hasExtra("appName")) {
            String title = getIntent().getExtras().getString("appName");
            headerText = (TextView) findViewById(Res.getWidgetID("header_text"));
            webView.setProperty(headerText, loadFailLayout, frameLayout);
            initWebViewGoBackOrClose();
            (findViewById(Res.getWidgetID("header_layout")))
                    .setVisibility(View.VISIBLE);
            headerText.setText(title);
        } else {
            webView.setProperty(null, loadFailLayout, frameLayout);
        }
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

    public void setTitle(String title) {
        if (headerText != null && !StringUtils.isBlank(title)) {
            urlTilteMap.put(webView.getUrl(), title);
            headerText.setText(title);
        }
    }

    /**
     * 解决有的机型Webview goback时候不会获取title的问题
     */
    private void setGoBackTitle() {
        if (headerText != null) {
            String title = urlTilteMap.get(webView.getUrl());
            if (!StringUtils.isBlank(title)) {
                headerText.setText(title);
            }
        }
    }

    /**
     * 返回
     */
    private void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();// 返回上一页面
            setGoBackTitle();
        } else {
            finishActivity();
        }
    }

    public void finishActivity() {
        if (getIntent().hasExtra("function") && getIntent().getStringExtra("function").equals("mdm")) {
            new MDM().getMDMListener().MDMStatusNoPass();
        }
        finish();// 退出程序
    }


    private void setWebViewUserAgent() {
        // TODO Auto-generated method stub
        WebSettings settings = webView.getSettings();
        String userAgent = settings.getUserAgentString();
        userAgent = userAgent + "/emmcloud/" + AppUtils.getVersion(this);
        settings.setUserAgentString(userAgent);
        settings.enableSmoothTransition();
        settings.setJavaScriptEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        settings.setDomStorageEnabled(true);


    }

    private void setWebViewHeader() {
        webViewHeaders = new HashMap<>();
        String token = ((MyApplication) getApplicationContext())
                .getToken();
        if (token != null) {
            webViewHeaders.put("Authorization", token);
        }
        webViewHeaders.put("X-ECC-Current-Enterprise", ((MyApplication) getApplicationContext()).getCurrentEnterprise().getId());
        String languageJson = PreferencesUtils.getString(
                getApplicationContext(), MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null) {
            Language language = new Language(languageJson);
            webViewHeaders.put("Accept-Language", language.getIana());
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imp_change_font_size_btn:
                showChangeFontSizeDialog();
                break;
            case R.id.app_imp_crm_font_normal_btn:
                setNewsFontSize(MyAppWebConfig.NORMAL);
                break;
            case R.id.app_imp_crm_font_middle_btn:
                setNewsFontSize(MyAppWebConfig.CRM_BIG);
                break;
            case R.id.app_imp_crm_font_big_btn:
                setNewsFontSize(MyAppWebConfig.CRM_BIGGER);
                break;
            case R.id.app_imp_crm_font_biggest_btn:
                setNewsFontSize(MyAppWebConfig.CRM_BIGGEST);
                break;
            case R.id.back_layout:
                goBack();
                break;
            case R.id.imp_close_btn:
                finishActivity();
                break;
            case R.id.refresh_text:
                showLoadingDlg(getString(Res.getStringID("@string/loading_text")));
                webView.reload();
                webView.setVisibility(View.INVISIBLE);
                loadFailLayout.setVisibility(View.GONE);
                break;
            default:
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
        if (!StringUtils.isBlank(helpUrl)) {
            initHelpUrlViews(dialog, view);
        }
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
        if (getIntent().hasExtra("is_zoomable") && (getIntent().getIntExtra("is_zoomable", 0) == 1)) {
            setWebViewButtonTextColor(0);
        }
        dialog.show();
    }

    /**
     * 初始化帮助view
     */
    private void initHelpUrlViews(final Dialog dialog, View view) {
        view.findViewById(R.id.app_imp_crm_help_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.app_news_share_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ImpActivity.this, ImpActivity.class);
                intent.putExtra("uri", helpUrl);
                intent.putExtra("appName", "");
                startActivity(intent);
                dialog.dismiss();
            }
        });
    }

    /**
     * 初始化Dialog的Views
     *
     * @param view
     */
    private void initFontSizeDialogViews(View view) {
        if (getIntent().hasExtra("is_zoomable") && (getIntent().getIntExtra("is_zoomable", 0) == 1)) {
            view.findViewById(R.id.app_imp_crm_font_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.app_imp_crm_font_layout).setVisibility(View.VISIBLE);
            normalBtn = (Button) view.findViewById(R.id.app_imp_crm_font_normal_btn);
            normalBtn.setText(getString(R.string.news_font_normal));
            middleBtn = (Button) view.findViewById(R.id.app_imp_crm_font_middle_btn);
            middleBtn.setText(getString(R.string.news_font_middle));
            bigBtn = (Button) view.findViewById(R.id.app_imp_crm_font_big_btn);
            bigBtn.setText(getString(R.string.news_font_big_text));
            biggestBtn = (Button) view.findViewById(R.id.app_imp_crm_font_biggest_btn);
            biggestBtn.setText(getString(R.string.news_font_biggest_text));
        }

    }


    /**
     * 改变WebView字体大小
     *
     * @param textZoom
     */
    private void setNewsFontSize(int textZoom) {
        WebSettings webSettings = webView.getSettings();
        PreferencesByUsersUtils.putInt(ImpActivity.this, "app_crm_font_size_" + appId, textZoom);
        webSettings.setTextZoom(textZoom);
        setWebViewButtonTextColor(textZoom);
    }

    /**
     * 初始化WebView的字体大小
     */
    private void setWebViewButtonTextColor(int textZoom) {
        int textSize = PreferencesByUsersUtils.getInt(ImpActivity.this, "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
        if (textZoom != 0) {
            textSize = textZoom;
        }
        int lightModeFontColor = ContextCompat.getColor(ImpActivity.this, R.color.app_dialog_day_font_color);
        int blackFontColor = ContextCompat.getColor(ImpActivity.this, R.color.black);
        normalBtn.setTextColor((textSize == MyAppWebConfig.NORMAL) ? lightModeFontColor : blackFontColor);
        middleBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIG) ? lightModeFontColor : blackFontColor);
        bigBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGER) ? lightModeFontColor : blackFontColor);
        biggestBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGEST) ? lightModeFontColor : blackFontColor);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();// 返回上一页面
                return true;
            } else {
                finishActivity();// 退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 弹出提示框
     */
    public void showImpDialog(){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        EasyDialog.showDialog(ImpActivity.this, getString(R.string.prompt),
                getString(R.string.imp_function_error),
                getString(R.string.ok),listener, false);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
        //清除掉图片缓存
        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
    }


    public void showLoadingDlg(String content) {
        if (StringUtils.isBlank(content)) {
            loadingText.setVisibility(View.GONE);
        } else {
            loadingText.setText(content);
            loadingText.setVisibility(View.VISIBLE);
        }
        loadingLayout.setVisibility(View.VISIBLE);
    }

    public void dimissLoadingDlg() {
//        loadingLayout.setVisibility(View.GONE);
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
