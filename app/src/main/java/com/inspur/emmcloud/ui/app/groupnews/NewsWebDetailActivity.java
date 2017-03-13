package com.inspur.emmcloud.ui.app.groupnews;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ProgressWebView;

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
    private Button dayBtn;
    private Button nightBtn;
    private Button shareBtn;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private TextView fontTxt;
    private View dayOrNightLine;
    private View fontLine;
    private String userId = "";

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
        loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.header_layout);
        initWebView();
    }

    /**
     * 初始化WebView，设置WebView属性
     */
    private void initWebView() {
        webView = (ProgressWebView) findViewById(R.id.news_webdetail_webview);
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        webSettings.setBuiltInZoomControls(true);
        // 设置字体大小
        webSettings.setSupportZoom(true);
        webSettings.setTextZoom(textSize);
        // 加载需要显示的网页
        if (!url.startsWith("http")) {
            url = UriUtils.getGroupNewsUrl(url);
        }
        webView.loadUrl(url);
//		webView.loadUrl("file:///android_asset/news1.html");
        // 设置Web视图
        webView.setWebViewClient(new webViewClient());
        // 设置背景颜色
//		webView.setBackgroundColor(Color.BLUE);
    }

    /**
     * 初始化WebView的字体大小
     */
    private void initWebViewTextSize() {
        textSize = PreferencesUtils.getInt(NewsWebDetailActivity.this, "app_news_text_size", MyAppWebConfig.NORMAL);
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
        userId = ((MyApplication)getApplication()).getUid();
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
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        if(model.equals(darkMode)){
            changeDialogModelToNight();
        }else {
            chagneDialogModelToDay();
        }
        dialog.show();
    }

    /**
     * 初始化Dialog的字体大小
     */
    private void initDialogFontSize() {
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        dayBtn = (Button) view.findViewById(R.id.app_news_mode_day_btn);
        nightBtn = (Button) view.findViewById(R.id.app_news_mode_night_btn);
        shareBtn = (Button) view.findViewById(R.id.app_news_share_btn);
        normalBtn = (Button) view.findViewById(R.id.app_news_font_normal_btn);
        middleBtn = (Button) view.findViewById(R.id.app_news_font_middle_btn);
        bigBtn = (Button) view.findViewById(R.id.app_news_font_big_btn);
        biggestBtn = (Button) view.findViewById(R.id.app_news_font_biggest_btn);
        dayOrNightLine = view.findViewById(R.id.app_news_mode_line);
        fontLine = view.findViewById(R.id.app_news_font_line);
        fontTxt = (TextView) view.findViewById(R.id.app_news_font_text);
    }

    /**
     * 改变WebView字体大小
     *
     * @param settings
     * @param textZoom
     */
    private void changeNewsFontSize(WebSettings settings, int textZoom) {
        settings.setTextZoom(textZoom);
        PreferencesUtils.putInt(NewsWebDetailActivity.this, "app_news_text_size", textZoom);
        textSize = textZoom;
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        //这里为了解决一个改变字体时的bug，很奇怪
        if(model.equals(darkMode)){
            drawable.setColor(Color.parseColor("#FF3B4451"));
        }else{
            drawable.setColor(Color.parseColor("#268E8E8E"));
        }
        shareBtn.setBackground(drawable);
        dayOrNightLayout.setBackground(drawable);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onClick(View v) {
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.news_share_img:
                showDialog();
                break;
            case R.id.app_news_mode_day_btn:
                changeWebViewModel(lightMode);
                chagneDialogModelToDay();
                break;
            case R.id.app_news_mode_night_btn:
                changeWebViewModel(darkMode);
                changeDialogModelToNight();
                break;
            case R.id.app_news_share_btn:
                shareNewsToFrinds();
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
     * 选择正常字体
     */
    private void chooseNormalFont(String model) {
        if(model.equals(darkMode)){
            normalBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
        }else{
            normalBtn.setTextColor(Color.parseColor("#FF999999"));
        }
        middleBtn.setTextColor(Color.parseColor("#FF000000"));
        bigBtn.setTextColor(Color.parseColor("#FF000000"));
        biggestBtn.setTextColor(Color.parseColor("#FF000000"));
    }

    /**
     * 选择中字体
     */
    private void chooseMiddleFont(String model) {
        if(model.equals(darkMode)){
            middleBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
        }else{
            middleBtn.setTextColor(Color.parseColor("#FF999999"));
        }
        normalBtn.setTextColor(Color.parseColor("#FF000000"));
        bigBtn.setTextColor(Color.parseColor("#FF000000"));
        biggestBtn.setTextColor(Color.parseColor("#FF000000"));
    }

    /**
     * 选择大字体
     */
    private void chooseBigFont(String model) {
        if(model.equals(darkMode)){
            bigBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
        }else{
            bigBtn.setTextColor(Color.parseColor("#FF999999"));
        }
        normalBtn.setTextColor(Color.parseColor("#FF000000"));
        middleBtn.setTextColor(Color.parseColor("#FF000000"));
        biggestBtn.setTextColor(Color.parseColor("#FF000000"));
    }

    /**
     * 选择超大字体
     */
    private void chooseBiggestFont(String model) {
        if(model.equals(darkMode)){
            biggestBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
        }else{
            biggestBtn.setTextColor(Color.parseColor("#FF999999"));
        }
        normalBtn.setTextColor(Color.parseColor("#FF000000"));
        middleBtn.setTextColor(Color.parseColor("#FF000000"));
        bigBtn.setTextColor(Color.parseColor("#FF000000"));
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
        PreferencesUtils.putString(NewsWebDetailActivity.this, "app_news_webview_model", model);
    }

    /**
     * 修改Dilaog的夜间模式
     */
    private void changeDialogModelToNight() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawable.setColor(Color.parseColor("#FF5E6875"));
        dialogLayout.setBackground(drawable);
        GradientDrawable drawableBtn = new GradientDrawable();
        drawableBtn.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableBtn.setColor(Color.parseColor("#FF3B4451"));
        dayOrNightLayout.setBackground(drawableBtn);
        dayBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
        setDayBtn(1);
        changeFontSizeBtn();
        shareBtn.setBackground(drawableBtn);
        normalBtn.setBackground(drawableBtn);
        middleBtn.setBackground(drawableBtn);
        bigBtn.setBackground(drawableBtn);
        biggestBtn.setBackground(drawableBtn);
        dayOrNightLine.setBackgroundColor(Color.parseColor("#FF161E29"));
        fontLine.setBackgroundColor(Color.parseColor("#FF161E29"));
        fontTxt.setTextColor(Color.parseColor("#FFFFFFFF"));
    }

    /**
     * 修改Dialog的日间模式
     */
    private void chagneDialogModelToDay() {
        GradientDrawable drawableDay = new GradientDrawable();
        drawableDay.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableDay.setColor(Color.parseColor("#FFFFFCFA"));
        dialogLayout.setBackground(drawableDay);
        GradientDrawable drawableDayBtn = new GradientDrawable();
        drawableDayBtn.setCornerRadius(DensityUtil.dip2px(NewsWebDetailActivity.this, 5));
        drawableDayBtn.setColor(Color.parseColor("#268E8E8E"));
        dayOrNightLayout.setBackground(drawableDayBtn);
        dayBtn.setTextColor(Color.parseColor("#FF999999"));
        setDayBtn(2);
        changeFontSizeBtn();
        shareBtn.setBackground(drawableDayBtn);
        normalBtn.setBackground(drawableDayBtn);
        middleBtn.setBackground(drawableDayBtn);
        bigBtn.setBackground(drawableDayBtn);
        biggestBtn.setBackground(drawableDayBtn);
        dayOrNightLine.setBackgroundColor(Color.parseColor("#FFD6D6D6"));
        fontLine.setBackgroundColor(Color.parseColor("#FFD6D6D6"));
        fontTxt.setTextColor(Color.parseColor("#FF999999"));
    }

    /**
     * 当改变日夜间模式的时候字体按钮对应改变
     */
    private void changeFontSizeBtn() {
        String model = PreferencesUtils.getString(NewsWebDetailActivity.this, "app_news_webview_model", "");
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
        Drawable dayIcon = null,nightIcon = null,shareIcon = null;
        Resources res = getResources();
        if(dayOrNight == 1){
            dayIcon = res.getDrawable(R.drawable.app_news_night_day);
            nightIcon = res.getDrawable(R.drawable.app_news_night_night);
            shareIcon = res.getDrawable(R.drawable.app_news_night_share);
        }else {
            dayIcon = res.getDrawable(R.drawable.app_news_day_day);
            nightIcon = res.getDrawable(R.drawable.app_news_day_night);
            shareIcon = res.getDrawable(R.drawable.app_news_share);
        }
        dayIcon.setBounds(0, 0, dayIcon.getMinimumWidth(), dayIcon.getMinimumHeight());
        dayBtn.setCompoundDrawables(dayIcon, null, null, null); //设置左图标
        nightIcon.setBounds(0, 0, dayIcon.getMinimumWidth(), dayIcon.getMinimumHeight());
        nightBtn.setCompoundDrawables(nightIcon,null,null,null);
        shareIcon.setBounds(0, 0, dayIcon.getMinimumWidth(), dayIcon.getMinimumHeight());
        shareBtn.setCompoundDrawables(shareIcon,null,null,null);
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
