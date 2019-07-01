package com.inspur.emmcloud.news.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.HtmlRegexpUtil;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.SwitchView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppWebConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.news.R;
import com.inspur.emmcloud.news.api.NewsAPIInsterfaceImpl;
import com.inspur.emmcloud.news.api.NewsAPIUri;
import com.inspur.emmcloud.news.api.NewsApiService;
import com.inspur.emmcloud.news.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.news.bean.GetSendMsgResult;
import com.inspur.emmcloud.news.bean.GroupNews;
import com.inspur.emmcloud.news.bean.NewsIntrcutionUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class NewsWebDetailActivity extends BaseActivity {

    private static final int SHARE_SEARCH_RUEST_CODE = 1;
    private static final String APP_NEWS_WEBVIEW_MODEL = "app_news_webview_model";
    private static final String APP_NEWS_TEXT_SIZE = "app_news_text_size";
    private static final String GROUP_NEWS = "groupNews";
    private static final String darkMode = "#dark_120";
    private static final String lightMode = "#light_120";
    private WebView webView;
    private String url;
    private LoadingDialog loadingDlg;
    private Dialog dialog;
    private int textSize;
    private Button smallerBtn, normalBtn, largerBtn, largestBtn;
    private String instruction = "";
    private String originalEditorComment = "";
    private GroupNews groupNews;
    private Dialog instructionDialog;
    private String fakeMessageId;
    private WebSettings settings;
    private Map<String, String> webViewHeaders;
    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        initData();
        initViews();
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.news_activity_newsweb_detail;
    }

    /**
     * 设置WebView的Header参数
     */
    private void loadUrlWithHeader(String url) {
        if (webViewHeaders == null) {
            webViewHeaders = new HashMap<>();
            // 根据规则添加token当URL主域名是Constant.INSPUR_HOST_URL或者Constant.INSPURONLINE_HOST_URL结尾时添加token
            try {
                URL urlHost = new URL(url);
                String token = BaseApplication.getInstance().getToken();
                if (token != null && (urlHost.getHost().endsWith(Constant.INSPUR_HOST_URL)) || urlHost.getHost().endsWith(Constant.INSPURONLINE_HOST_URL)) {
                    webViewHeaders.put("Authorization", token);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            webViewHeaders.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
            webViewHeaders.put("Accept-Language", LanguageManager.getInstance().getCurrentAppLanguage());
        }
        webView.loadUrl(url, webViewHeaders);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        loadingLayout = findViewById(R.id.rl_loading);
        loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);
        ((TextView) findViewById(R.id.header_text)).setText(((GroupNews) getIntent().getSerializableExtra(GROUP_NEWS)).getTitle());
        setWebView();
        setWebViewSettings();
        initWebViewGoBackOrClose();
    }

    /**
     * 初始化原生WebView的返回和关闭
     * 两处使用本方法的，专门封一个方法
     */
    public void initWebViewGoBackOrClose() {
        if (webView != null) {
            (findViewById(R.id.news_close_btn)).setVisibility(webView.canGoBack() ? View.VISIBLE : View.GONE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();// 返回上一页面
                return true;
            } else {
                finish();// 退出
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化WebView的Settings
     */
    private void setWebViewSettings() {
        settings = webView.getSettings();
        setWebSetting();
        WebSettings webSettings = webView.getSettings();
        //解决在安卓5.0以上跨域链接无法访问的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //初始化WebView字体的大小
        textSize = PreferencesByUserAndTanentUtils.getInt(NewsWebDetailActivity.this, APP_NEWS_TEXT_SIZE, MyAppWebConfig.NORMAL);
        webSettings.setTextZoom(textSize);
        // 加载需要显示的网页
        if (!url.startsWith("http")) {
            url = NewsAPIUri.getGroupNewsHtmlUrl(url);
        }
        //修改model在第一次加载时直接带着model而不是加载两次
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, "");
        loadUrlWithHeader(url + (StringUtils.isBlank(model) ? lightMode : model));
        setHeaderModel(model);
    }

    private void setWebView() {
        // 为0就是不给滚动条留空间，滚动条覆盖在网页上
        webView = (WebView) findViewById(R.id.wv_news);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //this.setInitialScale(100);
        webView.setInitialScale(0);
        webView.setLayoutAnimation(null);
        webView.setAnimation(null);
        webView.setNetworkAvailable(true);
        webView.setBackgroundColor(Color.WHITE);
        final String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, "");
        webView.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, (model.equals(darkMode)) ? R.color.app_news_night_color : R.color.white));
        //没有确定这里的影响，暂时不去掉
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
//        webView.clearCache(true);
        webView.setDownloadListener(new FileDownloadListener());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadingLayout != null) {
                    loadingLayout.setVisibility(View.GONE);
                }
                initWebViewGoBackOrClose();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Android8.0以下的需要返回true 并且需要loadUrl；8.0之后效果相反

                if (!url.startsWith("http") && !url.startsWith("ftp")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.setComponent(null);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                if (Build.VERSION.SDK_INT < 26) {
                    loadUrlWithHeader(url + (StringUtils.isBlank(model) ? lightMode : model));
                    return true;
                }
                return false;
            }

//            @Override
//            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
//                super.doUpdateVisitedHistory(view, url, isReload);
//                view.clearHistory();
//            }
        });
    }

    // 设置websettings属性
    public void setWebSetting() {
        //基础设置，地理位置，缓存，userAgent等
        setBaseConfig();
        // 支持js相关方法
        setJSConfig();
        // 页面效果设置
        setPageStyle();
        settings.setDefaultTextEncodingName("utf-8");
        // 本地安全设置
        setSecury();
        // API为16的方法访问
        Level16Apis.invoke(settings);
        // 允许ajax执行web请求
        Level4Apis.invoke(settings);
        // 支持html5数据库和使用缓存的功能
        Html5Apis htmlApi = new Html5Apis();
        htmlApi.invoke(settings);
    }

    /**
     * 基础设置
     */
    private void setBaseConfig() {
        // 代理字符串，如果字符串为空或者null系统默认字符串将被利用
        String userAgent = "Mozilla/5.0 (Linux; U; Android "
                + Build.VERSION.RELEASE + "; en-us; " + Build.MODEL
                + " Build/FRF91) AppleWebKit/533.1 "
                + "(KHTML, like Gecko) Version/4.0 Chrome/51.0.2704.81 Mobile Safari/533.1" + "/emmcloud/" + AppUtils.getVersion(NewsWebDetailActivity.this);
        settings.setUserAgentString(userAgent);
        settings.enableSmoothTransition();
        settings.setGeolocationEnabled(true);
        String dir = NewsWebDetailActivity.this.getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        settings.setBlockNetworkLoads(false);
    }

    /* 支持js相关方法 */
    private void setJSConfig() {
        // 设置WebView的属性，此时可以去执行JavaScript脚本
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        //解决在安卓5.0以上跨域链接无法访问的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /* 页面效果设置 */
    private void setPageStyle() {
        //设置自适应屏幕
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        // 支持多窗口
        settings.supportMultipleWindows();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 页面适应手机屏幕的分辨率
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("utf-8");//设置自适应屏幕
    }

    // 本地安全设置
    private void setSecury() {
        // 是否保存表单数据
        settings.setSaveFormData(false);
        // 是否保存密码
        settings.setSavePassword(false);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        groupNews = (GroupNews) getIntent().getSerializableExtra(GROUP_NEWS);
        if (groupNews != null) {
            String postTime = groupNews.getCreationDate();
            url = StringUtils.isBlank(groupNews.getUrl()) ? (getNewsTimePathIn(postTime)
                    + groupNews.getResource()) : groupNews.getUrl();
            originalEditorComment = groupNews.getOriginalEditorComment();
        } else {
            url = getIntent().getDataString();
        }
    }


    /**
     * 带有时区的时间路径,目前是零时区GMT
     * 如果需要改成东八区则GMT+8
     *
     * @param postTime
     * @return
     */
    private String getNewsTimePathIn(String postTime) {
        SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        postTime = sdfGMT.format(Long.parseLong(postTime));
        String timeYear = postTime.substring(0, 4);
        String timeMon = postTime.substring(5, 7);
        String timeDay = postTime.substring(8, 10);
        int year = Integer.parseInt(timeYear);
        int mon = Integer.parseInt(timeMon);
        int day = Integer.parseInt(timeDay);
        String timePath = NewsAPIUri.getGroupNewsArticleUrl() + year + "/" + mon
                + "/" + day + "/";
        return timePath;
    }


    /**
     * 打开字体设置，夜间模式设置Dialog
     */
    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.news_choose_dialog, null);
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        initDialogViews();
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
        // 控制透明度相关
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        setFontSizeBtn();
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, "");
        setDialogModel(model);
        dialog.show();
    }

    /**
     * 初始化字体大小设置，夜间模式设置Dialog的Views和监听
     */
    private void initDialogViews() {
        if (!groupNews.isHasExtraPermission()) {
            dialog.findViewById(R.id.app_news_instructions_btn).setVisibility(View.GONE);
            (dialog.findViewById(R.id.app_news_share_btn)).setPadding(getIconLeftSize(), 0, 0, 0);
        }
        smallerBtn = dialog.findViewById(R.id.app_news_font_normal_btn);
        normalBtn = dialog.findViewById(R.id.app_news_font_middle_btn);
        largerBtn = dialog.findViewById(R.id.app_news_font_big_btn);
        largestBtn = dialog.findViewById(R.id.app_news_font_biggest_btn);
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, lightMode);
        final SwitchView nightModeSwitchBtn = dialog.findViewById(R.id.app_news_mode_switch);
        nightModeSwitchBtn.setPaintColorOn(0x7E000000);
        nightModeSwitchBtn.setPaintCircleBtnColor(0x1A666666);
        nightModeSwitchBtn.setOpened(model.endsWith(darkMode));
        nightModeSwitchBtn.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, darkMode);
                setDialogModel(darkMode);
                setWebViewModel(darkMode);
                nightModeSwitchBtn.toggleSwitch(true);
                reRender();
            }

            @Override
            public void toggleToOff(View view) {
                PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, lightMode);
                setDialogModel(lightMode);
                setWebViewModel(lightMode);
                nightModeSwitchBtn.toggleSwitch(false);
                reRender();
            }
        });
    }

    /**
     * 设置Dialog的夜间，日间模式
     *
     * @param model
     */
    private void setDialogModel(String model) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawable.setColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_background_layout)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_background_layout));
        (dialog.findViewById(R.id.app_news_dialog)).setBackground(drawable);
        GradientDrawable drawableBtn = new GradientDrawable();
        drawableBtn.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableBtn.setColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_background_btn) : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_background_btn));
        (dialog.findViewById(R.id.app_news_mode_layout)).setBackground(drawableBtn);
        setDayBtn(model.equals(darkMode) ? 1 : 2);
        Button shareBtn = (dialog.findViewById(R.id.app_news_share_btn));
        shareBtn.setBackground(drawableBtn);
        shareBtn.setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black));
        Button instructionsBtn = dialog.findViewById(R.id.app_news_instructions_btn);
        instructionsBtn.setBackground(drawableBtn);
        instructionsBtn.setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black));
        dialog.findViewById(R.id.app_news_read_mode_line).setBackgroundColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_background_btn)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_read_line_color));
        ((TextView) dialog.findViewById(R.id.app_news_read_mode_text)).setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color));
        TextView dayOrNightModeText = dialog.findViewById(R.id.app_news_mode_night_text);
        dayOrNightModeText.setText(model.equals(darkMode) ? getString(R.string.news_night_mode_text) : getString(R.string.news_day_mode_text));
        dayOrNightModeText.setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white) : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black));
        ((ImageView) dialog.findViewById(R.id.app_news_mode_sun_img)).setImageResource(model.equals(darkMode) ? R.drawable.app_news_mode_day_light : R.drawable.app_news_mode_day_dark);
        ((ImageView) dialog.findViewById(R.id.app_news_mode_moon_img)).setImageResource(model.equals(darkMode) ? R.drawable.app_news_mode_light : R.drawable.app_news_mode_dark);
        smallerBtn.setBackground(drawableBtn);
        normalBtn.setBackground(drawableBtn);
        largerBtn.setBackground(drawableBtn);
        largestBtn.setBackground(drawableBtn);
        setFontSizeBtn();
        dialog.findViewById(R.id.app_news_mode_line).setBackgroundColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_line_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_line_color));
        dialog.findViewById(R.id.app_news_font_line).setBackgroundColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_line_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_line_color));
        ((TextView) dialog.findViewById(R.id.app_news_font_text)).setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color));
    }

    /**
     * 获取分享图标左侧大小
     *
     * @return
     */
    private int getIconLeftSize() {
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        float leftSize = (float) (width * 0.34);
        return Math.round(leftSize);
    }

    /**
     * 改变WebView字体大小，设置dialog上四种字体的UI
     *
     * @param textZoom
     */
    private void setNewsFontSize(int textZoom) {
        webView.getSettings().setTextZoom(textZoom);
        PreferencesByUserAndTanentUtils.putInt(NewsWebDetailActivity.this, APP_NEWS_TEXT_SIZE, textZoom);
        textSize = textZoom;
        reRender();
        setFontSizeBtn();
    }

    /**
     * 重新设置一次布局
     */
    private void reRender() {
        //这里为了解决一个改变字体时的bug，很奇怪
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, "");
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawable.setColor(model.endsWith(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_background_btn)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_background_btn));
        if (groupNews.isHasExtraPermission()) {
            dialog.findViewById(R.id.app_news_instructions_btn).setBackground(drawable);
        }
        (dialog.findViewById(R.id.app_news_share_btn)).setBackground(drawable);
        (dialog.findViewById(R.id.app_news_mode_layout)).setBackground(drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialog != null) {
            reRender();
        }
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            if (webView.canGoBack()) {
                webView.goBack();// 返回上一页面
            } else {
                finishActivity();
            }

        } else if (i == R.id.news_close_btn) {
            finish();

        } else if (i == R.id.news_share_img) {
            showDialog();

        } else if (i == R.id.app_news_mode_day_btn) {
            setWebViewModel(lightMode);
            setDialogModel(lightMode);

        } else if (i == R.id.app_news_mode_night_btn) {
            setWebViewModel(darkMode);
            setDialogModel(darkMode);

        } else if (i == R.id.app_news_share_btn) {
            //                shareNewsToFrinds();
        } else if (i == R.id.app_news_instructions_btn) {//批示逻辑
            dialog.dismiss();
            if (!StringUtils.isBlank(groupNews.getApprovedDate())) {
                String content = groupNews.getEditorComment();
                if (!StringUtils.isBlank(content)) {
                    instruction = content;
                }
                showHasInstructionDialog();
            } else if (groupNews.isEditorCommentCreated() == true) {
                instruction = groupNews.getOriginalEditorComment();
                showHasInstructionDialog();
            } else {
                showInstruceionDialog();
            }
        } else if (i == R.id.app_news_font_normal_btn) {
            setNewsFontSize(MyAppWebConfig.SMALLER);
        } else if (i == R.id.app_news_font_middle_btn) {
            setNewsFontSize(MyAppWebConfig.NORMAL);
        } else if (i == R.id.app_news_font_big_btn) {
            setNewsFontSize(MyAppWebConfig.LARGER);
        } else if (i == R.id.app_news_font_biggest_btn) {
            setNewsFontSize(MyAppWebConfig.LARGEST);
        }
    }

    /**
     * 展示已经批示过的新闻
     */
    private void showHasInstructionDialog() {
        final Dialog hasInstructionDialog = new Dialog(NewsWebDetailActivity.this,
                R.style.transparentFrameWindowStyle);
        Window window = hasInstructionDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        window.getDecorView().setPadding(DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0, DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0);
        hasInstructionDialog.setCanceledOnTouchOutside(true);
        View view = getLayoutInflater().inflate(R.layout.news_has_instruction_dialog, null);
        hasInstructionDialog.setContentView(view);
        final TextView instructionText = view.findViewById(R.id.news_has_instrcution_text);
        instructionText.setFocusable(false);
        instructionText.setEnabled(false);
        instruction = handleInstruction(instruction);
        instructionText.setText(instruction);
        Button okBtn = view.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasInstructionDialog.dismiss();
            }
        });
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.dimAmount = 0.31f;
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        wl.height = WindowManager.LayoutParams.WRAP_CONTENT;
        hasInstructionDialog.getWindow().setAttributes(wl);
        hasInstructionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        hasInstructionDialog.show();
    }

    /**
     * 如果批示里含有html标签，需要处理，如果不含则没有影响
     *
     * @param instruction
     * @return
     */
    private String handleInstruction(String instruction) {
        instruction = instruction.replace("<(br|BR)\\\\s*/?>(\\\\s*</(br|BR)>)?", "\n").replace(" ", "");
        instruction = HtmlRegexpUtil.filterHtml(instruction);
        return instruction;
    }

    /**
     * 展示批示
     */
    private void showInstruceionDialog() {
        instructionDialog = new Dialog(NewsWebDetailActivity.this,
                R.style.transparentFrameWindowStyleForIntrcution);
        Window window = instructionDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        window.getDecorView().setPadding(DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0, DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0);
        instructionDialog.setCanceledOnTouchOutside(true);
        View view = getLayoutInflater().inflate(R.layout.news_instruction_dialog, null);
        instructionDialog.setContentView(view);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionDialog.dismiss();
            }
        });
        final EditText editText = view.findViewById(R.id.news_instrcution_text);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        (view.findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originalEditorComment = editText.getText().toString();
                if (StringUtils.isBlank(originalEditorComment)) {
                    ToastUtils.show(NewsWebDetailActivity.this, getString(R.string.news_content_cant_empty));
                    return;
                }
                sendInstructions();
            }
        });
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.dimAmount = 0.31f;
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        wl.height = WindowManager.LayoutParams.WRAP_CONTENT;
        instructionDialog.getWindow().setAttributes(wl);
        instructionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        instructionDialog.show();
        openSoftKeyboard(editText);
    }

    /**
     * 发布批示
     */
    private void sendInstructions() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            NewsApiService apiService = new NewsApiService(NewsWebDetailActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.sendNewsInstruction(groupNews.getId(), originalEditorComment);
        }
    }

    /**
     * 打开软键盘
     *
     * @param view
     */
    private void openSoftKeyboard(final EditText view) {
        final InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                input.showSoftInput(view, 0);
            }
        });
    }

    /**
     * 改变原生导航栏
     */
    private void setHeaderModel(String model) {
        boolean isDarkMode = model.equals(darkMode);
        int color = ResourceUtils.getResValueOfAttr(this, R.attr.header_bg_color);
        int statusBarColor = isDarkMode ? R.color.app_news_night_color : color;
        ImmersionBar.with(this).statusBarColor(statusBarColor).init();
        (findViewById(R.id.rl_header)).setBackgroundColor(ContextCompat.getColor(BaseApplication.getInstance(), statusBarColor));
    }

    /**
     * 修改WebView的模式
     *
     * @param model
     */
    private void setWebViewModel(String model) {
        setHeaderModel(model);
        loadUrlWithHeader(url + model);
        PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, model);
    }

    /**
     * 当改变日夜间模式的时候字体按钮对应改变
     */
    private void setFontSizeBtn() {
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, APP_NEWS_WEBVIEW_MODEL, "");
        int lightModeFontColor = ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_font_color);
        int darkModeFontColor = ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color);
        int blackFontColor = ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black);
        int whiteFontColor = ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white);
        switch (textSize) {
            case MyAppWebConfig.SMALLER:
                normalBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largestBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                smallerBtn.setTextColor(model.equals(darkMode) ? darkModeFontColor : lightModeFontColor);
                break;
            case MyAppWebConfig.NORMAL:
                smallerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largestBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                normalBtn.setTextColor(model.equals(darkMode) ? darkModeFontColor : lightModeFontColor);
                break;
            case MyAppWebConfig.LARGER:
                smallerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                normalBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largestBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largerBtn.setTextColor(model.equals(darkMode) ? darkModeFontColor : lightModeFontColor);
                break;
            case MyAppWebConfig.LARGEST:
                smallerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                normalBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largerBtn.setTextColor(model.equals(darkMode) ? whiteFontColor : blackFontColor);
                largestBtn.setTextColor(model.equals(darkMode) ? darkModeFontColor : lightModeFontColor);
                break;
            default:
                break;
        }
    }

    /**
     * 选择日间夜间模式
     *
     * @param dayOrNight
     */
    public void setDayBtn(int dayOrNight) {
        Resources res = getResources();
        Drawable instructionIcon = (dayOrNight == 1) ? res.getDrawable(R.drawable.icon_news_instruction_wihte) : res.getDrawable(R.drawable.icon_news_instructions);
        Drawable shareIcon = (dayOrNight == 1) ? res.getDrawable(R.drawable.app_news_share_night) : res.getDrawable(R.drawable.app_news_share_day);
        instructionIcon.setBounds(0, 0, instructionIcon.getMinimumWidth(), instructionIcon.getMinimumHeight());
        shareIcon.setBounds(0, 0, shareIcon.getMinimumWidth(), shareIcon.getMinimumHeight());
        ((Button) dialog.findViewById(R.id.app_news_share_btn)).setCompoundDrawables(shareIcon, null, null, null);
        ((Button) dialog.findViewById(R.id.app_news_instructions_btn)).setCompoundDrawables(instructionIcon, null, null, null);
    }

//    /**
//     * 给朋友分享新闻
//     */
//    private void shareNewsToFrinds() {
//        Intent intent = new Intent();
//        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
//        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
//        ArrayList<String> uidList = new ArrayList<>();
//        uidList.add(BaseApplication.getInstance().getUid());
//        intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
//        intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.news_share));
//        intent.setClass(getApplicationContext(),
//                ContactSearchActivity.class);
//        startActivityForResult(intent, SHARE_SEARCH_RUEST_CODE);
//        dialog.dismiss();
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SHARE_SEARCH_RUEST_CODE && resultCode == RESULT_OK
//                && NetUtils.isNetworkConnected(getApplicationContext())) {
//            String result = data.getStringExtra("searchResult");
//            JSONObject jsonObject = JSONUtils.getJSONObject(result);
//            if (jsonObject.has("people")) {
//                JSONArray peopleArray = JSONUtils.getJSONArray(jsonObject, "people", new JSONArray());
//                if (peopleArray.length() > 0) {
//                    JSONObject peopleObj = JSONUtils.getJSONObject(peopleArray, 0, new JSONObject());
//                    String uid = JSONUtils.getString(peopleObj, "pid", "");
//                    createDirectChannel(uid);
//                }
//            }
//            if (jsonObject.has("channelGroup")) {
//                JSONArray channelGroupArray = JSONUtils.getJSONArray(jsonObject, "channelGroup", new JSONArray());
//                if (channelGroupArray.length() > 0) {
//                    JSONObject cidObj = JSONUtils.getJSONObject(channelGroupArray, 0, new JSONObject());
//                    String cid = JSONUtils.getString(cidObj, "cid", "");
//                    sendMsg(cid);
//                }
//            }
//        }
//    }

//    /**
//     * 创建单聊
//     *
//     * @param uid
//     */
//    private void createDirectChannel(String uid) {
//        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
//            new ConversationCreateUtils().createDirectConversation(NewsWebDetailActivity.this, uid,
//                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
//                        @Override
//                        public void createDirectConversationSuccess(Conversation conversation) {
//                            sendMsg(conversation.getId());
//                        }
//
//                        @Override
//                        public void createDirectConversationFail() {
//
//                        }
//                    });
//        } else {
//            new ChatCreateUtils().createDirectChannel(NewsWebDetailActivity.this, uid,
//                    new OnCreateDirectChannelListener() {
//                        @Override
//                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
//                            sendMsg(getCreateSingleChannelResult.getCid());
//                        }
//
//                        @Override
//                        public void createDirectChannelFail() {
//                            //showShareFailToast();
//                        }
//                    });
//        }
//
//    }

//    /**
//     * 发送新闻分享
//     *
//     * @param cid
//     */
//    private void sendMsg(String cid) {
//        if (NetUtils.isNetworkConnected(getApplicationContext())) {
//            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
//                ChatAPIService apiService = new ChatAPIService(
//                        NewsWebDetailActivity.this);
//                apiService.setAPIInterface(new WebService());
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("url", url);
//                    jsonObject.put("poster", groupNews.getPoster());
//                    jsonObject.put("digest", groupNews.getSummary());
//                    jsonObject.put("title", groupNews.getTitle());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                apiService.sendMsg(cid, jsonObject.toString(), "res_link", System.currentTimeMillis() + "");
//            } else {
//                String poster = StringUtils.isBlank(groupNews.getPoster()) ? "" : NewsAPIUri.getPreviewUrl(groupNews.getPoster());
//                Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, poster, groupNews.getTitle(), groupNews.getSummary(), url);
//                fakeMessageId = message.getId();
//                WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
//            }
//
//        }
//
//    }

    /**
     * 弹出分享失败toast
     */
    private void showShareFailToast() {
        ToastUtils.show(NewsWebDetailActivity.this, getString(R.string.news_share_fail));
    }

    /**
     * 发送批示成功事件
     */
    private void sendInstructionEvent() {
        NewsIntrcutionUpdateEvent groupEvent = new NewsIntrcutionUpdateEvent();
        groupEvent.setId(groupNews.getId());
        groupEvent.setEditorCommentCreated(true);
        groupEvent.setOriginalEditorComment(originalEditorComment);
        EventBus.getDefault().post(groupEvent);
    }

    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && webView != null) {
            webView.onPause(); // 暂停网页中正在播放的视频
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (instructionDialog != null && instructionDialog.isShowing()) {
            instructionDialog.dismiss();
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            return;
        }
        finishActivity();
    }

    /**
     * 关闭页面
     */
    private void finishActivity() {
        finish();
        if (webView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.onPause(); // 暂停网页中正在播放的视频
            }
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (fakeMessageId != null && String.valueOf(eventMessage.getId()).equals(fakeMessageId)) {
                if (eventMessage.getStatus() == 200) {
                    ToastUtils.show(NewsWebDetailActivity.this, getString(R.string.news_share_success));
                } else {
                    showShareFailToast();
                }
            }
        }

    }

    /**
     * API为16的方法访问
     */
    @TargetApi(16)
    private static class Level16Apis {
        static void invoke(WebSettings settings) {
            try {
                Method method = WebSettings.class.getMethod(
                        "setAllowFileAccessFromFileURLs",
                        boolean.class);
                method.invoke(settings, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * API为16或以上，允许ajax执行web请求
     */
    @TargetApi(16)
    private static class Level4Apis {
        static void invoke(WebSettings settings) {
            try {
                Method method = WebSettings.class.getMethod(
                        "setAllowUniversalAccessFromFileURLs",
                        boolean.class);
                method.invoke(settings, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FileDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, final String mimetype, long contentLength) {
            if (NetUtils.isNetworkConnected(NewsWebDetailActivity.this)) {
                JSONObject object = new JSONObject();
                try {
                    object.put("url", url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Router router = Router.getInstance();
                if (router.getService(com.inspur.emmcloud.componentservice.web.WebService.class) != null) {
                    com.inspur.emmcloud.componentservice.web.WebService service = router.getService(com.inspur.emmcloud.componentservice.web.WebService.class);
                    service.fileTransferServiceDownload(NewsWebDetailActivity.this, object.toString());
                }
            }
        }
    }

    /**
     * 支持html5数据库和使用缓存的功能
     */
    private class Html5Apis {
        void invoke(WebSettings settings) {
            try {
                // 数据库路径
                String databasePath = NewsWebDetailActivity.this.getDir("database", 0).getPath();
                // 使用localStorage则必须打开
                settings.setDomStorageEnabled(true);
                // 是否允许数据库存储的api
                settings.setDatabaseEnabled(true);
                // 设置数据库路径
                settings.setDatabasePath(databasePath);

                // webview加载 服务端的网页，为了减少访问压力，用html5缓存技术
                settings.setAppCacheEnabled(true);
                // 设置加载cache的方式，
                settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                // 设置缓存路径
                settings.setAppCachePath(databasePath);
                // 设置缓冲大小,此处设置为缓存最大为8m
                settings.setAppCacheMaxSize(1024 * 1024 * 8);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class WebService extends NewsAPIInsterfaceImpl {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(NewsWebDetailActivity.this, getString(R.string.news_share_success));
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            showShareFailToast();
        }

        @Override
        public void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            instructionDialog.dismiss();
            groupNews.setEditorCommentCreated(true);
            sendInstructionEvent();
            ToastUtils.show(NewsWebDetailActivity.this, getString(R.string.news_instructions_success_text));
        }

        @Override
        public void returnNewsInstructionFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            groupNews.setEditorCommentCreated(false);
            originalEditorComment = "";
            WebServiceMiddleUtils.hand(NewsWebDetailActivity.this, error, errorCode);
        }
    }
}
