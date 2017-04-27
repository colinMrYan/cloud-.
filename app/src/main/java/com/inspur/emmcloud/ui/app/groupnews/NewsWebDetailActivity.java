package com.inspur.emmcloud.ui.app.groupnews;

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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ProgressWebView;
import com.inspur.emmcloud.widget.SwitchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NewsWebDetailActivity extends BaseActivity {

    private ProgressWebView webView;
    private static final int SHARE_SEARCH_RUEST_CODE = 1;
    private static final String darkMode = "#dark_120";
    private static final String lightMode = "#light_120";
    private String url;
    private String poster;
    private String title;
    private String digest;
    private LoadingDialog loadingDlg;
    private String shareCid;
    private Dialog dialog;
    private int textSize;
    private RelativeLayout relativeLayout;
    private WebSettings webSettings;
    private LinearLayout dialogLayout;
    private LinearLayout dayOrNightLayout;
    private Button shareBtn;
    private Button instructionsBtn;
    private TextView readModeText;
    private TextView dayOrNightModeText;
    private ImageView sunImg,moonImg;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private TextView fontTxt;
    private View dayOrNightLine;
    private View fontLine;
    private int darkModeBtnColor;
    private int lightModeBtnColor;
    private int lightModeFontColor;
    private int blackFontColor;
    private int whiteFontColor;
    private TextView headText;
    private View appReadModeLine;
    private String pagerTitle = "";
    private SwitchView nightModeSwitchBtn;
    private GradientDrawable lightChooseFontBtnBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplicationContext()).addActivity(this);
        setContentView(R.layout.activity_newsweb_detail);
        initData();
        initViews();
    }



    /**
     * 初始化Views
     */
    private void initViews() {
        lightChooseFontBtnBackgroundDrawable = new GradientDrawable();
        lightChooseFontBtnBackgroundDrawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this,5));
        lightChooseFontBtnBackgroundDrawable.setColor(lightModeBtnColor);
        loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.header_layout);
        headText = (TextView)findViewById(R.id.header_text);
        if(StringUtils.isBlank(title)){
            title = getString(R.string.group_news_detail);
        }
        headText.setText(pagerTitle);
        initWebView();
    }

    /**
     * 初始化WebView，设置WebView属性
     */
    private void initWebView() {
        webView = (ProgressWebView) findViewById(R.id.news_webdetail_webview);
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        if (model.equals(darkMode)) {
            webView.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_news_night_color));
        } else {
            webView.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, R.color.white));
        }
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.clearCache(true);
        initWebViewTextSize();
        initWebViewSettings();
        initWebViewModel();
    }

    /**
     * 初始化是否夜间日间模式
     */
    private void initWebViewModel() {
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        if (StringUtils.isBlank(model)) {
            changeWebViewModel(lightMode);
        } else {
            changeWebViewModel(model);
        }
    }

    /**
     * 初始化WebView的Settings
     */
    private void initWebViewSettings() {
        webSettings = webView.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(false);
        // 设置字体大小
        webSettings.setSupportZoom(false);
        webSettings.setTextZoom(textSize);
        // 加载需要显示的网页
        if (!url.startsWith("http")) {
            url = UriUtils.getGroupNewsUrl(url);
        }
        webView.loadUrl(url);
        // 设置Web视图
        webView.setWebViewClient(new webViewClient());
    }

    /**
     * 初始化WebView的字体大小
     */
    private void initWebViewTextSize() {
        textSize = PreferencesByUserUtils.getInt(NewsWebDetailActivity.this, "app_news_text_size", MyAppWebConfig.NORMAL);
        switch (textSize) {
            case MyAppWebConfig.SMALLESET:
                textSize = MyAppWebConfig.SMALLESET;
                break;
            case MyAppWebConfig.SMALLER:
                textSize = MyAppWebConfig.SMALLER;
                break;
            case MyAppWebConfig.NORMAL:
                textSize = MyAppWebConfig.NORMAL;
                break;
            case MyAppWebConfig.LARGER:
                textSize = MyAppWebConfig.LARGER;
                break;
            case MyAppWebConfig.LARGEST:
                textSize = MyAppWebConfig.LARGEST;
                break;
            default:
                textSize = MyAppWebConfig.NORMAL;
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //夜间模式黑底
        darkModeBtnColor = ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_background_btn);
        lightModeBtnColor = ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_background_btn);
        lightModeFontColor = ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_font_color);
        blackFontColor = ContextCompat.getColor(NewsWebDetailActivity.this,R.color.black);
        whiteFontColor = ContextCompat.getColor(NewsWebDetailActivity.this,R.color.white);
        Intent intent = getIntent();
        if (intent.hasExtra("url")) {
            url = intent.getStringExtra("url");
        } else {
            url = getIntent().getDataString();
        }
        if (intent.hasExtra("poster")) {
            poster = intent.getStringExtra("poster");
        }
        if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title");
        }
        if (intent.hasExtra("digest")) {
            digest = intent.getStringExtra("digest");
        }
        if(intent.hasExtra("pager_title")){
            this.pagerTitle = intent.getStringExtra("pager_title");
        }
    }

    /**
     * 打开Dialog
     */
    private void showDialog() {
        View  view = getLayoutInflater().inflate(R.layout.app_news_choose_dialog, null);
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        initDialogViews(view);
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
        initDialogFontSize();
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        if(model.equals(darkMode)){
            changeDialogModelToNight();
        }else {
            changeDialogModelToDay();
        }
        dialog.show();
    }

    /**
     * Dialog字体按钮控制
     */
    private void initDialogFontSize() {
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        switch (textSize) {
            case MyAppWebConfig.SMALLESET:
                chooseNormalFont(model);
                break;
            case MyAppWebConfig.SMALLER:
                chooseNormalFont(model);
                break;
            case MyAppWebConfig.NORMAL:
                chooseMiddleFont(model);
                break;
            case MyAppWebConfig.LARGER:
                chooseBigFont(model);
                break;
            case MyAppWebConfig.LARGEST:
                chooseBiggestFont(model);
                break;
            default:
                chooseNormalFont(model);
                break;
        }
    }


    /**
     * 初始化Dialog的Views
     *
     * @param view
     */
    private void initDialogViews(View view) {
        dialogLayout = (LinearLayout) view.findViewById(R.id.app_news_dialog);
        dayOrNightLayout = (LinearLayout) view.findViewById(R.id.app_news_mode_layout);
        shareBtn = (Button) view.findViewById(R.id.app_news_share_btn);
        instructionsBtn = (Button)view.findViewById(R.id.app_news_instructions_btn);
        shareBtn.setText(getString(R.string.news_share_text));
//        shareBtn.setPadding(DensityUtil.dip2px(NewsWebDetailActivity.this,139),0,0,0);
        dayOrNightModeText = (TextView) view.findViewById(R.id.app_news_mode_night_text);
        dayOrNightModeText.setText(getString(R.string.news_read_mode));
        sunImg = (ImageView) view.findViewById(R.id.app_news_mode_sun_img);
        moonImg = (ImageView) view.findViewById(R.id.app_news_mode_moon_img);
        appReadModeLine = view.findViewById(R.id.app_news_read_mode_line);
        readModeText = (TextView) view.findViewById(R.id.app_news_read_mode_text);
        readModeText.setText(getString(R.string.news_read_mode));
        normalBtn = (Button) view.findViewById(R.id.app_news_font_normal_btn);
        normalBtn.setText(getString(R.string.news_font_smaller));
        middleBtn = (Button) view.findViewById(R.id.app_news_font_middle_btn);
        middleBtn.setText(getString(R.string.news_font_normal));
        bigBtn = (Button) view.findViewById(R.id.app_news_font_big_btn);
        bigBtn.setText(getString(R.string.news_font_big_text));
        biggestBtn = (Button) view.findViewById(R.id.app_news_font_biggest_btn);
        biggestBtn.setText(getString(R.string.news_font_biggest_text));
        dayOrNightLine = view.findViewById(R.id.app_news_mode_line);
        fontLine = view.findViewById(R.id.app_news_font_line);
        fontTxt = (TextView) view.findViewById(R.id.app_news_font_text);
        nightModeSwitchBtn = (SwitchView) view.findViewById(R.id.app_news_mode_switch);
        nightModeSwitchBtn.setPaintColorOn(0x7E000000);
        nightModeSwitchBtn.setPaintCircleBtnColor(0x1A666666);
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", lightMode);
        if(model.equals(darkMode)){
            nightModeSwitchBtn.setOpened(true);
        }else{
            nightModeSwitchBtn.setOpened(false);
        }
        nightModeSwitchBtn.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                PreferencesByUserUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", darkMode);
                changeDialogModelToNight();
                changeWebViewModel(darkMode);
                nightModeSwitchBtn.toggleSwitch(true);
                reRender();
            }

            @Override
            public void toggleToOff(View view) {
                PreferencesByUserUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", lightMode);
                changeDialogModelToDay();
                changeWebViewModel(lightMode);
                nightModeSwitchBtn.toggleSwitch(false);
                reRender();
            }
        });
    }

    /**
     * 改变WebView字体大小
     *
     * @param settings
     * @param textZoom
     */
    private void changeNewsFontSize(WebSettings settings, int textZoom) {
        settings.setTextZoom(textZoom);
        PreferencesByUserUtils.putInt(NewsWebDetailActivity.this, "app_news_text_size", textZoom);
        textSize = textZoom;
        reRender();
    }

    /**
     * 重新设置一次布局
     */
    private void reRender() {
        //这里为了解决一个改变字体时的bug，很奇怪
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));

        if(model.equals(darkMode)){
            drawable.setColor(darkModeBtnColor);
        }else{
            drawable.setColor(lightModeBtnColor);
        }
        shareBtn.setBackground(drawable);
        instructionsBtn.setBackground(drawable);
        dayOrNightLayout.setBackground(drawable);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(dialog != null){
            reRender();
        }
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onClick(View v) {
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.news_share_img:
                showDialog();
                break;
            case R.id.app_news_mode_day_btn:
                changeWebViewModel(lightMode);
                changeDialogModelToDay();
                break;
            case R.id.app_news_mode_night_btn:
                changeWebViewModel(darkMode);
                changeDialogModelToNight();
                break;
            case R.id.app_news_share_btn:
                shareNewsToFrinds();
                break;
            case R.id.app_news_instructions_btn:
                //批示逻辑
                dialog.dismiss();
                showInstruceionDialog();
                break;
            case R.id.app_news_font_normal_btn:
                changeNewsFontSize(webSettings, MyAppWebConfig.SMALLER);
                chooseNormalFont(model);
                break;
            case R.id.app_news_font_middle_btn:
                changeNewsFontSize(webSettings, MyAppWebConfig.NORMAL);
                chooseMiddleFont(model);
                break;
            case R.id.app_news_font_big_btn:
                changeNewsFontSize(webSettings, MyAppWebConfig.LARGER);
                chooseBigFont(model);
                break;
            case R.id.app_news_font_biggest_btn:
                changeNewsFontSize(webSettings, MyAppWebConfig.LARGEST);
                chooseBiggestFont(model);
                break;
            default:
                break;
        }

    }

    /**
     * 展示审批
     */
    private void showInstruceionDialog(){
        final Dialog intrcutionDialog = new Dialog(NewsWebDetailActivity.this,
                R.style.transparentFrameWindowStyle);
        intrcutionDialog.setCanceledOnTouchOutside(true);
        View  view = getLayoutInflater().inflate(R.layout.app_news_instruction_dialog, null);
        intrcutionDialog.setContentView(view);
        Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intrcutionDialog.dismiss();
            }
        });
        final EditText editText = (EditText) view.findViewById(R.id.news_instrcution_text);
        Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInstructions(editText.getText().toString());
                intrcutionDialog.dismiss();
            }
        });

        Window window = intrcutionDialog.getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
//        wl.alpha = 0.31f;
        wl.dimAmount = 0.31f;
        intrcutionDialog.getWindow().setAttributes(wl);
        intrcutionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        intrcutionDialog.show();
        openSoftKeyboard(editText);
    }

    /**
     * 发布批示
     * @param s
     */
    private void sendInstructions(String s) {
        ChatAPIService apiService = new ChatAPIService(NewsWebDetailActivity.this);
        if(NetUtils.isNetworkConnected(NewsWebDetailActivity.this)){
            if(!loadingDlg.isShowing()){
                loadingDlg.show();
            }
            apiService.sendNewsInstruction(s);
        }
    }


    /**
     * 打开软键盘
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
     * 选择正常字体
     */
    private void chooseNormalFont(String model) {
        if(model.equals(darkMode)){
            middleBtn.setTextColor(whiteFontColor);
            bigBtn.setTextColor(whiteFontColor);
            biggestBtn.setTextColor(whiteFontColor);
            normalBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        }else{
            middleBtn.setTextColor(blackFontColor);
            bigBtn.setTextColor(blackFontColor);
            biggestBtn.setTextColor(blackFontColor);
            normalBtn.setTextColor(lightModeFontColor);
        }

    }

    /**
     * 选择中字体
     */
    private void chooseMiddleFont(String model) {
        if(model.equals(darkMode)){
            normalBtn.setTextColor(whiteFontColor);
            bigBtn.setTextColor(whiteFontColor);
            biggestBtn.setTextColor(whiteFontColor);
            middleBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        }else{
            normalBtn.setTextColor(blackFontColor);
            bigBtn.setTextColor(blackFontColor);
            biggestBtn.setTextColor(blackFontColor);
            middleBtn.setTextColor(lightModeFontColor);
        }
    }

    /**
     * 选择大字体
     */
    private void chooseBigFont(String model) {
        if(model.equals(darkMode)){
            normalBtn.setTextColor(whiteFontColor);
            middleBtn.setTextColor(whiteFontColor);
            biggestBtn.setTextColor(whiteFontColor);
            bigBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        }else{
            normalBtn.setTextColor(blackFontColor);
            middleBtn.setTextColor(blackFontColor);
            biggestBtn.setTextColor(blackFontColor);
            bigBtn.setTextColor(lightModeFontColor);
        }

    }

    /**
     * 选择超大字体
     */
    private void chooseBiggestFont(String model) {
        if(model.equals(darkMode)){
            normalBtn.setTextColor(whiteFontColor);
            middleBtn.setTextColor(whiteFontColor);
            bigBtn.setTextColor(whiteFontColor);
            biggestBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        }else{
            normalBtn.setTextColor(blackFontColor);
            middleBtn.setTextColor(blackFontColor);
            bigBtn.setTextColor(blackFontColor);
            biggestBtn.setTextColor(lightModeFontColor);
        }

    }

    /**
     * 修改WebView的模式
     *
     * @param model
     */
    private void changeWebViewModel(String model) {
        if (model.equals(darkMode)) {
            StateBarColor.changeStateBarColor(NewsWebDetailActivity.this, R.color.app_news_night_color);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, R.color.app_news_night_color));
            webView.loadUrl(url + model);
        }else {
            StateBarColor.changeStateBarColor(NewsWebDetailActivity.this, R.color.header_bg);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this, R.color.header_bg));
            webView.loadUrl(url + model);
        }
        PreferencesByUserUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", model);
    }

    /**
     * 修改Dilaog的夜间模式
     */
    private void changeDialogModelToNight() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawable.setColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_background_layout));
        dialogLayout.setBackground(drawable);
        GradientDrawable drawableBtn = new GradientDrawable();
        drawableBtn.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableBtn.setColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_background_btn));
        dayOrNightLayout.setBackground(drawableBtn);
        setDayBtn(1);
        changeFontSizeBtn();
        shareBtn.setBackground(drawableBtn);
        shareBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.white));
        instructionsBtn.setBackground(drawableBtn);
        instructionsBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.white));
        appReadModeLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_background_btn));
        readModeText.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        dayOrNightModeText.setText(getString(R.string.news_night_mode_text));
        dayOrNightModeText.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.white));
        sunImg.setImageResource(R.drawable.app_news_mode_day_light);
        moonImg.setImageResource(R.drawable.app_news_mode_light);
        normalBtn.setBackground(drawableBtn);
        middleBtn.setBackground(drawableBtn);
        bigBtn.setBackground(drawableBtn);
        biggestBtn.setBackground(drawableBtn);
        initDialogFontSize();
        dayOrNightLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_line_color));
        fontLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_line_color));
        fontTxt.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
    }

    /**
     * 修改Dialog的日间模式
     */
    private void changeDialogModelToDay() {
        GradientDrawable drawableDay = new GradientDrawable();
        drawableDay.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableDay.setColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_background_layout));
        dialogLayout.setBackground(drawableDay);
        GradientDrawable drawableDayBtn = new GradientDrawable();
        drawableDayBtn.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableDayBtn.setColor(lightModeBtnColor);
        dayOrNightLayout.setBackground(drawableDayBtn);
        setDayBtn(2);
        changeFontSizeBtn();
        shareBtn.setBackground(drawableDayBtn);
        shareBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.black));
        instructionsBtn.setBackground(drawableDayBtn);
        instructionsBtn.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.black));
        appReadModeLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_read_line_color));
        readModeText.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
        readModeText.setText(getString(R.string.news_read_mode));
        dayOrNightModeText.setText(getString(R.string.news_day_mode_text));
        dayOrNightModeText.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.black));
        sunImg.setImageResource(R.drawable.app_news_mode_day_dark);
        moonImg.setImageResource(R.drawable.app_news_mode_dark);
        normalBtn.setBackground(drawableDayBtn);
        middleBtn.setBackground(drawableDayBtn);
        bigBtn.setBackground(drawableDayBtn);
        biggestBtn.setBackground(drawableDayBtn);
        dayOrNightLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_line_color));
        fontLine.setBackgroundColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_day_line_color));
        fontTxt.setTextColor(ContextCompat.getColor(NewsWebDetailActivity.this,R.color.app_dialog_night_font_size_color));
    }

    /**
     * 当改变日夜间模式的时候字体按钮对应改变
     */
    private void changeFontSizeBtn() {
        String model = PreferencesByUserUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        switch (textSize){
            case MyAppWebConfig.SMALLER:
                chooseNormalFont(model);
                break;
            case MyAppWebConfig.NORMAL:
                chooseMiddleFont(model);
                break;
            case MyAppWebConfig.LARGER:
                chooseBigFont(model);
                break;
            case MyAppWebConfig.LARGEST:
                chooseBiggestFont(model);
                break;
        }
    }

    /**
     * 选择日间夜间模式
     * @param dayOrNight
     */
    public void setDayBtn(int dayOrNight){
        Drawable shareIcon = null,instructionIcon = null;
        Resources res = getResources();
        if(dayOrNight == 1){
            instructionIcon = res.getDrawable(R.drawable.icon_news_instruction_wihte);
        }else {
            instructionIcon = res.getDrawable(R.drawable.icon_news_instructions);
        }

        if(dayOrNight == 1){
            shareIcon = res.getDrawable(R.drawable.app_news_share_night);
        }else {
            shareIcon = res.getDrawable(R.drawable.app_news_share_day);
        }
        instructionIcon.setBounds(0, 0, instructionIcon.getMinimumWidth(), instructionIcon.getMinimumHeight());
        shareIcon.setBounds(0, 0, shareIcon.getMinimumWidth(), shareIcon.getMinimumHeight());
        shareBtn.setCompoundDrawables(shareIcon,null,null,null);
        instructionsBtn.setCompoundDrawables(instructionIcon,null,null,null);
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
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_SEARCH_RUEST_CODE && resultCode == RESULT_OK
                && NetUtils.isNetworkConnected(getApplicationContext())) {
            String result = data.getStringExtra("searchResult");
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("people")) {
                    JSONArray peopleArray = jsonObject.getJSONArray("people");
                    if (peopleArray.length() > 0) {
                        JSONObject peopleObj = peopleArray.getJSONObject(0);
                        String uid = peopleObj.getString("pid");
                        createDirectChannel(uid);
                    }

                }

                if (jsonObject.has("channelGroup")) {
                    JSONArray channelGroupArray = jsonObject
                            .getJSONArray("channelGroup");
                    if (channelGroupArray.length() > 0) {
                        JSONObject cidObj = channelGroupArray.getJSONObject(0);
                        shareCid = cidObj.getString("cid");
                        shareNewsLink(shareCid);
                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                showShareFailToast();
            }

        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        // TODO Auto-generated method stub
        new ChatCreateUtils().createDirectChannel(NewsWebDetailActivity.this, uid,
                new OnCreateDirectChannelListener() {

                    @Override
                    public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        // TODO Auto-generated method stub
                        shareNewsLink(getCreateSingleChannelResult.getCid());

                    }

                    @Override
                    public void createDirectChannelFail() {
                        // TODO Auto-generated method stub
                        showShareFailToast();
                    }
                });
    }

    /**
     * 分享新闻
     *
     * @param cid
     */
    private void shareNewsLink(String cid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
            jsonObject.put("poster", poster);
            jsonObject.put("digest", digest);
            jsonObject.put("title", title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMsg(cid, jsonObject.toString(), System.currentTimeMillis() + "");
    }

    /**
     * 发送新闻分享
     *
     * @param cid
     * @param jsonNews
     * @param time
     */
    private void sendMsg(String cid, String jsonNews, String time) {
        ChatAPIService apiService = new ChatAPIService(
                NewsWebDetailActivity.this);
        apiService.setAPIInterface(new WebService());
        apiService.sendMsg(cid, jsonNews, "res_link", time);
    }

    /**
     * 自定义WebViewClient在应用中打开页面
     */
    private class webViewClient extends WebViewClient {
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

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Toast.makeText(NewsWebDetailActivity.this,
                    getString(R.string.news_share_success), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            showShareFailToast();
        }

        @Override
        public void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult) {
            if(loadingDlg != null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
        }

        @Override
        public void returnNewsInstructionFail(String error) {
            if(loadingDlg != null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.onPause(); // 暂停网页中正在播放的视频
        }
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

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

}
