package com.inspur.emmcloud.ui.appcenter.groupnews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.appcenter.news.GroupNews;
import com.inspur.emmcloud.bean.appcenter.news.NewsIntrcutionUpdateEvent;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.HtmlRegexpUtil;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ProgressWebView;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.imp.plugin.PluginMgr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;


public class NewsWebDetailActivity extends BaseActivity {

    private static final int SHARE_SEARCH_RUEST_CODE = 1;
    private static final String darkMode = "#dark_120";
    private static final String lightMode = "#light_120";
    private ProgressWebView webView;
    private String url;
    private LoadingDialog loadingDlg;
    private Dialog dialog;
    private int textSize;
    private Button smallerBtn, normalBtn, largerBtn, largestBtn;
    private String instruction = "";
    private String originalEditorComment = "";
    private GroupNews groupNews;
    private Dialog intrcutionDialog;
    private String fakeMessageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsweb_detail);
        initData();
        initViews();
        EventBus.getDefault().register(this);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);
        ((TextView) findViewById(R.id.header_text)).setText(getString(R.string.group_news));
        initWebView();
    }

    /**
     * 初始化WebView，设置WebView属性
     */
    private void initWebView() {
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        webView = (ProgressWebView) findViewById(R.id.news_webdetail_webview);
        webView.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, (model.equals(darkMode)) ? R.color.app_news_night_color : R.color.white));
        //没有确定这里的影响，暂时不去掉
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.clearCache(true);
        initWebViewSettings();
        setWebViewModel(StringUtils.isBlank(model) ? lightMode : model);
        webView.setDownloadListener(new FileDownloadListener());
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
                PluginMgr pluginMgr = new PluginMgr(NewsWebDetailActivity.this, null);
                pluginMgr.execute("FileTransferService", "download", object.toString());
                pluginMgr.onDestroy();
            }
        }
    }

    /**
     * 初始化WebView的Settings
     */
    private void initWebViewSettings() {
        WebSettings webSettings = webView.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(false);
        // 设置字体大小
        webSettings.setSupportZoom(false);
        webSettings.setSavePassword(false);
        //解决在安卓5.0以上跨域链接无法访问的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //初始化WebView字体的大小
        textSize = PreferencesByUserAndTanentUtils.getInt(NewsWebDetailActivity.this, "app_news_text_size", MyAppWebConfig.NORMAL);
        webSettings.setTextZoom(textSize);
        // 加载需要显示的网页
        if (!url.startsWith("http")) {
            url = APIUri.getGroupNewsHtmlUrl(url);
        }
        webView.loadUrl(url);
        // 设置Web视图
        webView.setWebViewClient(new GroupNewsWebViewClient());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        groupNews = (GroupNews) getIntent().getSerializableExtra("groupNews");
        if (groupNews != null) {
            String postTime = groupNews.getCreationDate();
            url = StringUtils.isBlank(groupNews.getUrl()) ? (TimeUtils.getNewsTimePathIn(postTime)
                    + groupNews.getResource()) : groupNews.getUrl();
            originalEditorComment = groupNews.getOriginalEditorComment();
        } else {
            url = getIntent().getDataString();
        }
    }

    /**
     * 打开字体设置，夜间模式设置Dialog
     */
    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.app_news_choose_dialog, null);
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
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        smallerBtn = (Button) dialog.findViewById(R.id.app_news_font_normal_btn);
        normalBtn = (Button) dialog.findViewById(R.id.app_news_font_middle_btn);
        largerBtn = (Button) dialog.findViewById(R.id.app_news_font_big_btn);
        largestBtn = (Button) dialog.findViewById(R.id.app_news_font_biggest_btn);
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", lightMode);
        final SwitchView nightModeSwitchBtn = (SwitchView) dialog.findViewById(R.id.app_news_mode_switch);
        nightModeSwitchBtn.setPaintColorOn(0x7E000000);
        nightModeSwitchBtn.setPaintCircleBtnColor(0x1A666666);
        nightModeSwitchBtn.setOpened(model.endsWith(darkMode) ? true : false);
        nightModeSwitchBtn.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", darkMode);
                setDialogModel(darkMode);
                setWebViewModel(darkMode);
                nightModeSwitchBtn.toggleSwitch(true);
                reRender();
            }

            @Override
            public void toggleToOff(View view) {
                PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", lightMode);
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
        Button shareBtn = (Button) (dialog.findViewById(R.id.app_news_share_btn));
        shareBtn.setBackground(drawableBtn);
        shareBtn.setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black));
        Button instructionsBtn = (Button) dialog.findViewById(R.id.app_news_instructions_btn);
        instructionsBtn.setBackground(drawableBtn);
        instructionsBtn.setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.black));
        dialog.findViewById(R.id.app_news_read_mode_line).setBackgroundColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_background_btn)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_day_read_line_color));
        ((TextView) dialog.findViewById(R.id.app_news_read_mode_text)).setTextColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_dialog_night_font_size_color));
        TextView dayOrNightModeText = (TextView) dialog.findViewById(R.id.app_news_mode_night_text);
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
        PreferencesByUserAndTanentUtils.putInt(NewsWebDetailActivity.this, "app_news_text_size", textZoom);
        textSize = textZoom;
        reRender();
        setFontSizeBtn();
    }

    /**
     * 重新设置一次布局
     */
    private void reRender() {
        //这里为了解决一个改变字体时的bug，很奇怪
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.news_share_img:
                showDialog();
                break;
            case R.id.app_news_mode_day_btn:
                setWebViewModel(lightMode);
                setDialogModel(lightMode);
                break;
            case R.id.app_news_mode_night_btn:
                setWebViewModel(darkMode);
                setDialogModel(darkMode);
                break;
            case R.id.app_news_share_btn:
                shareNewsToFrinds();
                break;
            case R.id.app_news_instructions_btn:
                //批示逻辑
                dialog.dismiss();
                if (!StringUtils.isBlank(groupNews.getApprovedDate())) {
                    String content = groupNews.getEditorComment();
                    if (!StringUtils.isBlank(content)) {
                        instruction = content;
                    }
                    showHasInstruceionDialog();
                } else if (groupNews.isEditorCommentCreated() == true) {
                    instruction = groupNews.getOriginalEditorComment();
                    showHasInstruceionDialog();
                } else {
                    showInstruceionDialog();
                }
                break;
            case R.id.app_news_font_normal_btn:
                setNewsFontSize(MyAppWebConfig.SMALLER);
                break;
            case R.id.app_news_font_middle_btn:
                setNewsFontSize(MyAppWebConfig.NORMAL);
                break;
            case R.id.app_news_font_big_btn:
                setNewsFontSize(MyAppWebConfig.LARGER);
                break;
            case R.id.app_news_font_biggest_btn:
                setNewsFontSize(MyAppWebConfig.LARGEST);
                break;
            default:
                break;
        }
    }

    /**
     * 展示已经批示过的新闻
     */
    private void showHasInstruceionDialog() {
        final Dialog hasIntrcutionDialog = new Dialog(NewsWebDetailActivity.this,
                R.style.transparentFrameWindowStyle);
        Window window = hasIntrcutionDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        window.getDecorView().setPadding(DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0, DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0);
        hasIntrcutionDialog.setCanceledOnTouchOutside(true);
        View view = getLayoutInflater().inflate(R.layout.app_news_has_instruction_dialog, null);
        hasIntrcutionDialog.setContentView(view);
        final TextView instrcutionText = (TextView) view.findViewById(R.id.news_has_instrcution_text);
        instrcutionText.setFocusable(false);
        instrcutionText.setEnabled(false);
        instruction = handleInstruction(instruction);
        instrcutionText.setText(instruction);
        Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasIntrcutionDialog.dismiss();
            }
        });
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.dimAmount = 0.31f;
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        wl.height = WindowManager.LayoutParams.WRAP_CONTENT;
        hasIntrcutionDialog.getWindow().setAttributes(wl);
        hasIntrcutionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        hasIntrcutionDialog.show();
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
        intrcutionDialog = new Dialog(NewsWebDetailActivity.this,
                R.style.transparentFrameWindowStyleForIntrcution);
        Window window = intrcutionDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        window.getDecorView().setPadding(DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0, DensityUtil.dip2px(NewsWebDetailActivity.this, 20), 0);
        intrcutionDialog.setCanceledOnTouchOutside(true);
        View view = getLayoutInflater().inflate(R.layout.app_news_instruction_dialog, null);
        intrcutionDialog.setContentView(view);
        Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intrcutionDialog.dismiss();
            }
        });
        final EditText editText = (EditText) view.findViewById(R.id.news_instrcution_text);
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
        intrcutionDialog.getWindow().setAttributes(wl);
        intrcutionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        intrcutionDialog.show();
        openSoftKeyboard(editText);
    }

    /**
     * 发布批示
     */
    private void sendInstructions() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(NewsWebDetailActivity.this);
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
     * 修改WebView的模式
     *
     * @param model
     */
    private void setWebViewModel(String model) {
        StateBarUtils.changeStateBarColor(NewsWebDetailActivity.this, model.equals(darkMode) ? R.color.app_news_night_color : R.color.header_bg);
        (findViewById(R.id.header_layout)).setBackgroundColor(model.equals(darkMode) ? ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_news_night_color)
                : ContextCompat.getColor(NewsWebDetailActivity.this, R.color.header_bg));
        webView.loadUrl(url + model);
        PreferencesByUserAndTanentUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", model);
    }

    /**
     * 当改变日夜间模式的时候字体按钮对应改变
     */
    private void setFontSizeBtn() {
        String model = PreferencesByUserAndTanentUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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

    /**
     * 给朋友分享新闻
     */
    private void shareNewsToFrinds() {
        Intent intent = new Intent();
        intent.putExtra("select_content", 0);
        intent.putExtra("isMulti_select", false);
        intent.putExtra("isContainMe", true);
        intent.putExtra("title", getString(R.string.news_share));
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_SEARCH_RUEST_CODE);
        dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_SEARCH_RUEST_CODE && resultCode == RESULT_OK
                && NetUtils.isNetworkConnected(getApplicationContext())) {
            String result = data.getStringExtra("searchResult");
            JSONObject jsonObject = JSONUtils.getJSONObject(result);
            if (jsonObject.has("people")) {
                JSONArray peopleArray = JSONUtils.getJSONArray(jsonObject,"people",new JSONArray());
                if (peopleArray.length() > 0) {
                    JSONObject peopleObj = JSONUtils.getJSONObject(peopleArray,0,new JSONObject());
                    String uid = JSONUtils.getString(peopleObj,"pid","");
                    createDirectChannel(uid);
                }
            }
            if (jsonObject.has("channelGroup")){
                JSONArray channelGroupArray = JSONUtils.getJSONArray(jsonObject,"channelGroup",new JSONArray());
                if (channelGroupArray.length() > 0) {
                    JSONObject cidObj = JSONUtils.getJSONObject(channelGroupArray,0,new JSONObject());
                    String cid = JSONUtils.getString(cidObj,"cid","");
                    sendMsg(cid);
                }
            }
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        new ChatCreateUtils().createDirectChannel(NewsWebDetailActivity.this, uid,
                new OnCreateDirectChannelListener() {
                    @Override
                    public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        sendMsg(getCreateSingleChannelResult.getCid());
                    }

                    @Override
                    public void createDirectChannelFail() {
                        showShareFailToast();
                    }
                });
    }

    /**
     * 发送新闻分享
     *
     * @param cid
     */
    private void sendMsg(String cid) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            if (MyApplication.getInstance().isV0VersionChat()) {
                ChatAPIService apiService = new ChatAPIService(
                        NewsWebDetailActivity.this);
                apiService.setAPIInterface(new WebService());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("url", url);
                    jsonObject.put("poster", groupNews.getPoster());
                    jsonObject.put("digest", groupNews.getSummary());
                    jsonObject.put("title", groupNews.getTitle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.sendMsg(cid, jsonObject.toString(), "res_link", System.currentTimeMillis() + "");
            } else {
                Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, groupNews.getPoster(), groupNews.getTitle(), groupNews.getSummary(), url);
                fakeMessageId = message.getId();
                WSAPIService.getInstance().sendChatExtendedLinksMsg(cid, message);
            }

        }

    }

    /**
     * 自定义WebViewClient在应用中打开页面
     */
    private class GroupNewsWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * 弹出分享失败toast
     */
    private void showShareFailToast() {
        Toast.makeText(NewsWebDetailActivity.this,
                getString(R.string.news_share_fail), Toast.LENGTH_SHORT).show();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.onPause(); // 暂停网页中正在播放的视频
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                if (fakeMessageId != null && String.valueOf(eventMessage.getExtra()).equals(fakeMessageId)) {
                    Toast.makeText(NewsWebDetailActivity.this,
                            getString(R.string.news_share_success), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                showShareFailToast();
            }

        }

    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Toast.makeText(NewsWebDetailActivity.this,
                    getString(R.string.news_share_success), Toast.LENGTH_SHORT)
                    .show();
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
            intrcutionDialog.dismiss();
            groupNews.setEditorCommentCreated(true);
            sendInstructionEvent();
            Toast.makeText(NewsWebDetailActivity.this,
                    getString(R.string.news_instructions_success_text), Toast.LENGTH_SHORT)
                    .show();
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
