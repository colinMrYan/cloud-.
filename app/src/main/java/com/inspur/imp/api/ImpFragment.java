package com.inspur.imp.api;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.bean.system.MainTabMenu;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MDM.MDM;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.widget.MaxHeightListView;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.plugin.IPlugin;
import com.inspur.imp.plugin.PluginMgr;
import com.inspur.imp.plugin.barcode.scan.BarCodeService;
import com.inspur.imp.plugin.camera.CameraService;
import com.inspur.imp.plugin.file.FileService;
import com.inspur.imp.plugin.filetransfer.FileTransferService;
import com.inspur.imp.plugin.photo.PhotoService;
import com.inspur.imp.plugin.staff.SelectStaffService;
import com.inspur.imp.plugin.window.DropItemTitle;
import com.inspur.imp.plugin.window.OnKeyDownListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2018/7/9.
 */

public class ImpFragment extends ImpBaseFragment {
    // 浏览文件resultCode
    public static final int CAMERA_SERVICE_CAMERA_REQUEST = 1;
    public static final int CAMERA_SERVICE_GALLERY_REQUEST = 2;
    public static final int PHOTO_SERVICE_CAMERA_REQUEST = 3;
    public static final int PHOTO_SERVICE_GALLERY_REQUEST = 4;
    public static final int SELECT_STAFF_SERVICE_REQUEST = 5;
    public static final int FILE_SERVICE_REQUEST = 6;
    public static final int DO_NOTHING_REQUEST = 7;
    public static final int BARCODE_SERVER__SCAN_REQUEST = 8;
    public static final int SELECT_FILE_SERVICE_REQUEST = 9;
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private static final String JAVASCRIPT_PREFIX = "javascript:";
    private ImpWebView webView;
    private Map<String, String> webViewHeaders;
    private LinearLayout loadFailLayout;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private String appId = "";
    private FrameLayout frameLayout;
    private RelativeLayout loadingLayout;
    private TextView loadingText;
    private String helpUrl = "";
    private HashMap<String, String> urlTilteMap = new HashMap<>();
    private View rootView;

    private String appName = "";
    private String version;
    private ImpFragmentClickListener listener;
    private PopupWindow dropTitlePopupWindow;
    private RelativeLayout headerLayout;
    private List<DropItemTitle> dropItemTitleList = new ArrayList<>();
    private Adapter dropTitleAdapter;
    private ImpCallBackInterface impCallBackInterface;
    private OnKeyDownListener onKeyDownListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(Res.getLayoutID("activity_imp"), null);
        initViews();
        version = getArguments().getString(Constant.WEB_FRAGMENT_VERSION, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (headerLayout.getVisibility() == View.VISIBLE) {
            setFragmentStatusBarCommon();
        } else {
            setFragmentStatusBarWhite();
        }
        if (rootView == null) {
            rootView = inflater.inflate(Res.getLayoutID("activity_imp"), container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        if (!version.equals(getArguments().getString(Constant.WEB_FRAGMENT_VERSION, ""))) {
            initFragmentViews();
        }
        return rootView;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        //防止以后扩展其他Activity时，忘记设置相关参数造成崩溃
        if (getArguments() == null) {
            setArguments(new Bundle());
        }
        appName = getArguments().getString(Constant.WEB_FRAGMENT_APP_NAME);
        headerLayout = (RelativeLayout) rootView.findViewById(Res.getWidgetID("rl_header"));
        loadingLayout = (RelativeLayout) rootView.findViewById(Res.getWidgetID("rl_loading"));
        loadingText = (TextView) rootView.findViewById(Res.getWidgetID("tv_loading"));
        frameLayout = (FrameLayout) rootView.findViewById(Res.getWidgetID("videoContainer"));
        loadFailLayout = (LinearLayout) rootView.findViewById(Res.getWidgetID("load_error_layout"));
        webView = (ImpWebView) rootView.findViewById(Res.getWidgetID("webview"));
        headerText = (TextView) rootView.findViewById(Res.getWidgetID("header_text"));
        functionLayout = (RelativeLayout) rootView.findViewById(Res.getWidgetID("function_layout"));
        webFunctionLayout = (LinearLayout) rootView.findViewById(Res.getWidgetID("ll_web_function"));
        if (getActivity().getClass().getName().equals(IndexActivity.class.getName())) {
            rootView.findViewById(R.id.ibt_back).setVisibility(View.GONE);
            rootView.findViewById(R.id.imp_close_btn).setVisibility(View.GONE);
        }
        showLoadingDlg("");
        if (!StringUtils.isBlank(getArguments().getString("help_url"))) {
            String helpUrl = getArguments().getString("help_url");
            if (!StringUtils.isBlank(helpUrl)) {
                this.helpUrl = helpUrl;
            }
        }
        if (!StringUtils.isBlank(getArguments().getString("appId"))) {
            appId = getArguments().getString("appId");
        }
        initFragmentViews();
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName()) ?
                (RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.CENTER_VERTICAL) : RelativeLayout.CENTER_IN_PARENT);
        headerText.setLayoutParams(layoutParams);
        if (getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName())) {
            headerText.setPadding(DensityUtil.dip2px(getActivity(), 15), 0, 0, 0);
        } else {
            headerText.setTextSize(17);
        }
        headerText.setText(StringUtils.isBlank(appName) ? "" : appName);
    }


    /**
     * 初始化Fragment的WebView
     */
    private void initFragmentViews() {
        String url = getArguments().getString(Constant.APP_WEB_URI);
        optionMenuList = (ArrayList<MainTabMenu>) getArguments().getSerializable(Constant.WEB_FRAGMENT_MENU);
        setWebViewFunctionVisiable();
        initHeaderOptionMenu();
        initListeners();
        initWebViewHeaderLayout();
        setWebViewHeader(url);
        webView.setOnTouchListener(new View.OnTouchListener() {
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
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        listener = new ImpFragmentClickListener();
        rootView.findViewById(R.id.imp_change_font_size_btn).setOnClickListener(listener);
        rootView.findViewById(R.id.ibt_back).setOnClickListener(listener);
        rootView.findViewById(R.id.imp_close_btn).setOnClickListener(listener);
        rootView.findViewById(R.id.refresh_text).setOnClickListener(listener);
        rootView.findViewById(R.id.load_error_layout).setOnClickListener(listener);
    }

    /**
     * 执行JS脚本
     *
     * @param script
     */
    protected void runJavaScript(String script) {
        webView.loadUrl(script);
    }

    /**
     * 在WebClient获取header
     * 为了防止第一层不符合规则，第二层符合添加token规则时不再检查url的问题，需要回传url重新检查增加每次检查是否需要加token
     *
     * @return
     */
    public Map<String, String> getWebViewHeaders(String url) {
        addAuthorizationToken(url);
        return webViewHeaders;
    }

    /**
     * 设置Webview自定义功能是否显示
     */
    private void setWebViewFunctionVisiable() {
        int isZoomable = getArguments().getInt("is_zoomable", 0);
        if (isZoomable == 1 || !StringUtils.isBlank(helpUrl)) {
            rootView.findViewById(R.id.imp_change_font_size_btn).setVisibility(View.VISIBLE);
        }
        if (isZoomable == 1) {
            int textSize = PreferencesByUsersUtils.getInt(getActivity(), "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
            webView.getSettings().setTextZoom(textSize);
        }
    }

    /**
     * 初始化webview haader layout
     */
    private void initWebViewHeaderLayout() {
        impCallBackInterface = getImpCallBackInterface();
        if (getArguments().getBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true)) {
            String title = getArguments().getString(Constant.WEB_FRAGMENT_APP_NAME);
            headerText.setOnClickListener(new ImpFragmentClickListener());
            webView.setProperty(headerText, loadFailLayout, frameLayout, impCallBackInterface);
            initWebViewGoBackOrClose();
            headerLayout.setVisibility(View.VISIBLE);
            headerText.setText(title);
        } else {
            webView.setProperty(null, loadFailLayout, frameLayout, impCallBackInterface);
        }
    }

    private void showDropTitlePop() {
        // 一个自定义的布局，作为显示的内容
        if (dropTitlePopupWindow == null) {
            View contentView = LayoutInflater.from(ImpFragment.this.getContext())
                    .inflate(R.layout.plugin_pop_drop_title, null);
            // 设置按钮的点击事件
            dropTitlePopupWindow = new PopupWindow(contentView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true);
            dropTitlePopupWindow.setTouchable(true);
            dropTitlePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setHeaderTitleTextDropImg();

                }
            });
            MaxHeightListView listView = (MaxHeightListView) contentView.findViewById(R.id.list);
            listView.setMaxHeight(DensityUtil.dip2px(MyApplication.getInstance(), 240));
            dropTitleAdapter = new Adapter();
            listView.setAdapter(dropTitleAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setDropItemTitleSelect(position);
                    dropTitlePopupWindow.dismiss();
                }
            });
        } else {
            dropTitleAdapter.notifyDataSetChanged();
        }
        dropTitlePopupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        dropTitlePopupWindow.showAsDropDown(headerLayout);
    }

    private void setDropItemTitleSelect(int position) {
        if (dropItemTitleList != null && position < dropItemTitleList.size()) {
            if (position == -1) {
                for (int i = 0; i < dropItemTitleList.size(); i++) {
                    if (dropItemTitleList.get(i).isSelected()) {
                        position = i;
                        break;
                    }
                }
            }
            DropItemTitle dropItemTitle = dropItemTitleList.get(position);
            dropItemTitle.setSelected(true);
            runJavaScript(JAVASCRIPT_PREFIX + dropItemTitle.getAction());
            setTitle(dropItemTitle.getText());
            for (int i = 0; i < dropItemTitleList.size(); i++) {
                if (i != position) {
                    dropItemTitleList.get(i).setSelected(false);
                }
            }
        }

    }

    /**
     * 与主Fragment通信的接口
     *
     * @return
     */
    private ImpCallBackInterface getImpCallBackInterface() {
        return new ImpCallBackInterface() {
            @Override
            public void onLoadingDlgDimiss() {
                dimissLoadingDlg();
            }

            @Override
            public void onShowImpDialog() {
                showImpDialog();
            }

            @Override
            public Map<String, String> onGetWebViewHeaders(String url) {
                return getWebViewHeaders(url);
            }

            @Override
            public void onInitWebViewGoBackOrClose() {
                initWebViewGoBackOrClose();
            }

            @Override
            public void onSetTitle(String title) {
                if (StringUtils.isBlank(appName)) {
                    setTitle(title);
                }
            }

            @Override
            public void onFinishActivity() {
                finishActivity();
            }

            @Override
            public void onLoadingDlgShow(String content) {
                showLoadingDlg(content);
            }

            @Override
            public void onStartActivityForResult(Intent intent, int requestCode) {
                startActivityForResult(intent, requestCode);
            }

            @Override
            public void onSetDropTitles(List<DropItemTitle> dropItemTitleList) {
                ImpFragment.this.dropItemTitleList = dropItemTitleList;
                setHeaderTitleTextDropImg();
            }

            @Override
            public void onProgressChanged(int newProgress) {
            }

            @Override
            public void onSetOptionMenu(List<MainTabMenu> optionMenuList) {
                ImpFragment.this.optionMenuList = optionMenuList;
                initHeaderOptionMenu();
            }

            @Override
            public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
                ImpFragment.this.onKeyDownListener = onKeyDownListener;
            }
        };
    }

    private void setHeaderTitleTextDropImg() {
        boolean isDropTitlePopShow = (dropTitlePopupWindow != null && dropTitlePopupWindow.isShowing());
        int dropUpRes = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.plugin_ic_header_title_drop_up);
        int dropDownRes = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.plugin_ic_header_title_drop_down);
        Drawable drawable = ContextCompat.getDrawable(MyApplication.getInstance(), isDropTitlePopShow ? dropUpRes : dropDownRes);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        headerText.setCompoundDrawables(null, null, drawable, null);
        setDropItemTitleSelect(-1);
    }

    /**
     * 初始化原生WebView的返回和关闭
     * （不是GS应用，GS应用有重定向，不容易实现返回）
     */
    public void initWebViewGoBackOrClose() {
        if (webView != null) {
            if (getActivity().getClass().getName().equals(ImpActivity.class.getName())) {
                (rootView.findViewById(Res.getWidgetID("imp_close_btn"))).setVisibility(webView.canGoBack() ? View.VISIBLE : View.GONE);
            }
            setHeaderTextWidth();
        }
    }

    public void setTitle(String title) {
        if (!StringUtils.isBlank(title)) {
            urlTilteMap.put(webView.getUrl(), title);
            headerText.setText(title);
        }
    }

    /**
     * 解决有的机型Webview goback时候不会获取title的问题
     */
    private void setGoBackTitle() {
        String title = urlTilteMap.get(webView.getUrl());
        if (!StringUtils.isBlank(title)) {
            headerText.setText(title);
        }
    }

    /**
     * 返回
     */
    public boolean onBackKeyDown() {
        if (ImpFragment.this.onKeyDownListener != null){
            ImpFragment.this.onKeyDownListener.onBackKeyDown();
        }else {
            if (webView.canGoBack()) {
                webView.goBack();// 返回上一页面
                setGoBackTitle();
            } else {
                finishActivity();
            }
        }
        return true;
    }

    public void finishActivity() {
        if (!StringUtils.isBlank(getArguments().getString("function")) && getArguments().getString("function").equals("mdm")) {
            new MDM().getMDMListener().MDMStatusNoPass();
        }
        getActivity().finish();// 退出程序
    }

    /**
     * 设置WebView的Header参数
     */
    private void setWebViewHeader(String url) {
        webViewHeaders = new HashMap<>();
        addAuthorizationToken(url);
        webViewHeaders.put("X-ECC-Current-Enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
        String languageJson = PreferencesUtils.getString(
                getActivity(), MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null) {
            Language language = new Language(languageJson);
            webViewHeaders.put("Accept-Language", language.getIana());
        }
    }

    /**
     * 根据规则添加token
     * 当URL主域名是Constant.INSPUR_HOST_URL
     * 或者Constant.INSPURONLINE_HOST_URL结尾时添加token
     */
    private void addAuthorizationToken(String url) {
        try {
            URL urlHost = new URL(url);
            String token = MyApplication.getInstance().getToken();
            if (token != null && (urlHost.getHost().endsWith(Constant.INSPUR_HOST_URL)) || urlHost.getHost().endsWith(Constant.INSPURONLINE_HOST_URL) || urlHost.getPath().endsWith("/app/mdm/v3.0/loadForRegister")) {
                webViewHeaders.put("Authorization", token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开修改字体的dialog
     */
    private void showChangeFontSizeDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.app_imp_crm_font_dialog, null);
        Dialog dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
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
        wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
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

        view.findViewById(R.id.app_imp_crm_font_normal_btn).setOnClickListener(listener);
        view.findViewById(R.id.app_imp_crm_font_middle_btn).setOnClickListener(listener);
        view.findViewById(R.id.app_imp_crm_font_big_btn).setOnClickListener(listener);
        view.findViewById(R.id.app_imp_crm_font_biggest_btn).setOnClickListener(listener);

        if (getArguments().getInt("is_zoomable", 0) == 1) {
            setWebViewButtonTextColor(0);
        }
        dialog.show();
    }

    /**
     * 初始化帮助view
     */
    private void initHelpUrlViews(final Dialog dialog, View view) {
        view.findViewById(R.id.app_imp_crm_help_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.app_news_share_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ImpActivity.class);
                intent.putExtra(Constant.APP_WEB_URI, helpUrl);
                intent.putExtra(Constant.WEB_FRAGMENT_APP_NAME, "");
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
        if (getArguments().getInt("is_zoomable", 0) == 1) {
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
        PreferencesByUsersUtils.putInt(getActivity(), "app_crm_font_size_" + appId, textZoom);
        webSettings.setTextZoom(textZoom);
        setWebViewButtonTextColor(textZoom);
    }

    /**
     * 初始化WebView的字体大小
     */
    private void setWebViewButtonTextColor(int textZoom) {
        int textSize = PreferencesByUsersUtils.getInt(getActivity(), "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
        if (textZoom != 0) {
            textSize = textZoom;
        }
        int lightModeFontColor = ContextCompat.getColor(getActivity(), R.color.app_dialog_day_font_color);
        int blackFontColor = ContextCompat.getColor(getActivity(), R.color.black);
        normalBtn.setTextColor((textSize == MyAppWebConfig.NORMAL) ? lightModeFontColor : blackFontColor);
        middleBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIG) ? lightModeFontColor : blackFontColor);
        bigBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGER) ? lightModeFontColor : blackFontColor);
        biggestBtn.setTextColor((textSize == MyAppWebConfig.CRM_BIGGEST) ? lightModeFontColor : blackFontColor);
    }

    /**
     * 弹出提示框
     */
    public void showImpDialog() {
        ToastUtils.show(getActivity(), R.string.imp_function_error);
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        impCallBackInterface = null;
        //清除掉图片缓存
//        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        super.onDestroy();
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
        loadingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
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
        } else {
            PluginMgr pluginMgr = webView.getPluginMgr();
            if (pluginMgr != null) {
                String serviceName = "";
                switch (requestCode) {
                    case CAMERA_SERVICE_CAMERA_REQUEST:
                    case CAMERA_SERVICE_GALLERY_REQUEST:
                        serviceName = CameraService.class.getCanonicalName().trim();
                        break;
                    case PHOTO_SERVICE_CAMERA_REQUEST:
                    case PHOTO_SERVICE_GALLERY_REQUEST:
                        serviceName = PhotoService.class.getCanonicalName().trim();
                        break;
                    case SELECT_STAFF_SERVICE_REQUEST:
                        serviceName = SelectStaffService.class.getCanonicalName().trim();
                        break;
                    case FILE_SERVICE_REQUEST:
                        serviceName = FileService.class.getCanonicalName().trim();
                        break;
                    case BARCODE_SERVER__SCAN_REQUEST:
                        serviceName = BarCodeService.class.getCanonicalName().trim();
                        break;
                    case SELECT_FILE_SERVICE_REQUEST:
                        serviceName = FileTransferService.class.getCanonicalName().trim();
                        break;
                    default:
                        break;
                }
                if (!StringUtils.isBlank(serviceName)) {
                    IPlugin plugin = pluginMgr.getPlugin(serviceName);
                    if (plugin != null) {
                        plugin.onActivityResult(requestCode, resultCode, data);
                    }

                }

            }
        }


    }

    class ImpFragmentClickListener implements View.OnClickListener {

        @Override
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
                case R.id.ibt_back:
                    if (webView.canGoBack()) {
                        webView.goBack();// 返回上一页面
                        setGoBackTitle();
                    } else {
                        finishActivity();
                    }
                    break;
                case R.id.imp_close_btn:
                    finishActivity();
                    break;
                case R.id.refresh_text:
                    IntentUtils.startActivity(getActivity(), NetWorkStateDetailActivity.class);
                    break;
                case R.id.load_error_layout:
                    showLoadingDlg(getString(Res.getStringID("@string/loading_text")));
                    webView.reload();
                    webView.setVisibility(View.INVISIBLE);
                    loadFailLayout.setVisibility(View.GONE);
                    break;
                case R.id.header_text:
                    if (dropItemTitleList != null && dropItemTitleList.size() > 0) {
                        if (dropTitlePopupWindow != null && dropTitlePopupWindow.isShowing()) {
                            dropTitlePopupWindow.dismiss();
                        } else {
                            showDropTitlePop();
                            setHeaderTitleTextDropImg();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dropItemTitleList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.plugin_pop_drop_list_item_view, null);
            DropItemTitle dropItemTitle = dropItemTitleList.get(position);
            ImageView iconImg = (ImageView) convertView.findViewById(R.id.iv_icon);
            TextView titleText = (TextView) convertView.findViewById(R.id.tv_name_tips);
            ImageView selectImg = (ImageView) convertView.findViewById(R.id.iv_select);
            ImageDisplayUtils.getInstance().displayImage(iconImg, dropItemTitle.getIco(), R.drawable.icon_photo_default);
            titleText.setText(dropItemTitle.getText());
            selectImg.setVisibility(dropItemTitle.isSelected() ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }
    }
}
