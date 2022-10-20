package com.inspur.emmcloud.web.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.MaxHeightListView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppWebConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.componentservice.application.maintab.MainTabMenu;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;
import com.inspur.emmcloud.web.plugin.IPlugin;
import com.inspur.emmcloud.web.plugin.PluginMgr;
import com.inspur.emmcloud.web.plugin.audio.IMPAudioService;
import com.inspur.emmcloud.web.plugin.barcode.scan.BarCodeService;
import com.inspur.emmcloud.web.plugin.bluetooth.BlueToothService;
import com.inspur.emmcloud.web.plugin.camera.CameraService;
import com.inspur.emmcloud.web.plugin.filetransfer.FileTransferService;
import com.inspur.emmcloud.web.plugin.invoice.InvoiceService;
import com.inspur.emmcloud.web.plugin.photo.PhotoService;
import com.inspur.emmcloud.web.plugin.screenshot.ScreenshotService;
import com.inspur.emmcloud.web.plugin.staff.SelectStaffService;
import com.inspur.emmcloud.web.plugin.video.VideoService;
import com.inspur.emmcloud.web.plugin.window.DropItemTitle;
import com.inspur.emmcloud.web.plugin.window.OnKeyDownListener;
import com.inspur.emmcloud.web.plugin.window.OnTitleBackKeyDownListener;
import com.inspur.emmcloud.web.plugin.window.WindowService;
import com.inspur.emmcloud.web.webview.ImpWebView;
import com.itheima.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yufuchang on 2018/7/9.
 */

public class ImpFragment extends ImpBaseFragment implements View.OnClickListener {
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
    public static final int REQUEST_CODE_RECORD_VIDEO = 10;
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;
    private static final int REQUEST_EDIT_SCREENSHOT_IMG = 11;
    public static final int SHARE_WEB_URL_REQUEST = 12;
    public static final int REQUEST_CODE_RECORD_AUDIO = 13;
    public  static final int REQUEST_CONNECT_DEVICE_SECURE = 14;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 15;
    public static final int SELECTOR_SERVICE_GALLERY_REQUEST = 16;
    public static String EXTRA_CALLBACK_SUCCESS = "success";
    public static String EXTRA_CALLBACK_FAIL = "fail";
//    public  static final int GELLARY_RESULT = 2;
    private static final String JAVASCRIPT_PREFIX = "javascript:";
    private static String EXTRA_OUTSIDE_URL = "extra_outside_url";
    private static String EXTRA_OUTSIDE_URL_REQUEST_RESULT = "extra_outside_url_request_result";
    @BindView(R2.id.webview)
    ImpWebView webView;
    @BindView(R2.id.load_error_layout)
    LinearLayout loadFailLayout;
    @BindView(R2.id.rl_header)
    RelativeLayout headerLayout;
    @BindView(R2.id.videoContainer)
    FrameLayout frameLayout;
    @BindView(R2.id.rl_loading)
    RelativeLayout loadingLayout;
    @BindView(R2.id.tv_loading)
    TextView loadingText;
    @BindView(R2.id.iv_screenshot)
    RoundedImageView screenshotImg;
    @BindView(R2.id.rl_root)
    RelativeLayout rootLayout;
    @BindView(R2.id.ibt_back)
    ImageButton backImgBtn;
    @BindView(R2.id.imp_close_btn)
    TextView closeBtn;
    @BindView(R2.id.imp_change_font_size_btn)
    ImageView changeFontSizeBtn;
    private Button normalBtn, middleBtn, bigBtn, biggestBtn;
    private String appName = "";
    private String helpUrl = "";
    private String appId = "";
    private Map<String, String> webViewHeaders;
    private HashMap<String, String> urlTitleMap = new HashMap<>();
    private PopupWindow dropTitlePopupWindow;
    private List<DropItemTitle> dropItemTitleList = new ArrayList<>();
    private Adapter dropTitleAdapter;
    private ImpCallBackInterface impCallBackInterface;
    private OnKeyDownListener onKeyDownListener;
    private OnTitleBackKeyDownListener onTitleBackKeyDownListener;
    private boolean isStaticWebTitle = false;
    private boolean isTitlePriorityFirst = false; // 打开WebView传参title字段优先级，true表示title一直作为标题
    //错误url和错误信息
    private String errorUrl = "";
    private String errorDescription = "";
    private String screenshotImgPath = "";
    private Handler handler;
    private Runnable screenshotRunnable;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web_fragment_imp, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViews();
        dealStatusBar();
        //String version = getArguments().getString(Constant.WEB_FRAGMENT_VERSION, "");
        // if (!version.equals(getArguments().getString(Constant.WEB_FRAGMENT_VERSION, ""))) {
        //    initFragmentViews();
        //}
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            dealStatusBar();
        }
    }

    private void dealStatusBar() {
        if (headerLayout.getVisibility() == View.VISIBLE) {
            if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(Constant.WEB_FRAGMENT_BAR_TINT_COLOR))) {
                boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(getActivity(), com.inspur.emmcloud.basemodule.R.attr.status_bar_dark_font);
                ImmersionBar.with(getActivity()).statusBarColor(getArguments().getString(Constant.WEB_FRAGMENT_BAR_TINT_COLOR)).statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(com.inspur.emmcloud.basemodule.R.color.white).navigationBarDarkIcon(true, 1.0f).init();
            } else {
                setFragmentStatusBarCommon();
            }
        } else {
            setFragmentStatusBarWhite();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        webView.onActivityStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onActivityResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onActivityPause();
    }

    protected void onNewIntent(Intent intent) {
        webView.onActivityNewIntent(intent);
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
        isStaticWebTitle = getArguments().getBoolean(Constant.Web_STATIC_TITLE, false);
        isTitlePriorityFirst = getArguments().getBoolean(Constant.WEB_FRAGMENT_TITLE_PRIORITY_FIRST, false);
        if (isStaticWebTitle) {
            backImgBtn.setVisibility(View.GONE);
            closeBtn.setVisibility(View.GONE);
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
        initImpCallBackInterface();
        initFragmentViews();
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(isStaticWebTitle ?
                (RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.CENTER_VERTICAL) : RelativeLayout.CENTER_IN_PARENT);
        headerText.setLayoutParams(layoutParams);
        if (isStaticWebTitle) {
            headerText.setPadding(DensityUtil.dip2px(getActivity(), 15), 0, 0, 0);
        } else {
            headerText.setTextSize(17);
        }
        headerText.setText(StringUtils.isBlank(appName) ? "" : appName);
        handler = new Handler();
        screenshotRunnable = new Runnable() {
            @Override
            public void run() {
                if (screenshotImg != null) {
                    screenshotImg.setVisibility(View.GONE);
                }
            }
        };
    }


    /**
     * 初始化Fragment的WebView
     */
    private void initFragmentViews() {
        String url = getArguments().getString(Constant.APP_WEB_URI);
        optionMenuList = (ArrayList<MainTabMenu>) getArguments().getSerializable(Constant.WEB_FRAGMENT_MENU);
        setWebViewFunctionVisiable();
        initHeaderOptionMenu();
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
     * 执行JS脚本
     *
     * @param script
     */
    @Override
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
        if (!webViewHeaders.containsKey("X-ECC-Current-Enterprise") && BaseApplication.getInstance().getCurrentEnterprise() != null) {
            webViewHeaders.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
        }
        return webViewHeaders;
    }

    /**
     * 设置Webview自定义功能是否显示
     */
    private void setWebViewFunctionVisiable() {
        int isZoomable = getArguments().getInt("is_zoomable", 0);
        if (isZoomable == 1 || !StringUtils.isBlank(helpUrl)) {
            changeFontSizeBtn.setVisibility(View.VISIBLE);
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
        if (getArguments() != null && getArguments().getBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true)) {
            webView.setProperty(headerText, frameLayout, impCallBackInterface);
            initWebViewGoBackOrClose();
            headerLayout.setVisibility(View.VISIBLE);
            String image = getArguments().getString(Constant.WEB_FRAGMENT_TITLE_IMAGE);
            if (TextUtils.isEmpty(image)) {
                headerText.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.GONE);
                String title = getArguments().getString(Constant.WEB_FRAGMENT_TITLE);
                title = TextUtils.isEmpty(title) ? getArguments().getString(Constant.WEB_FRAGMENT_APP_NAME) : title;
                headerText.setText(title);
                String textColor = getArguments().getString(Constant.WEB_FRAGMENT_TITLE_COLOR);
                if (!TextUtils.isEmpty(textColor)) {
                    headerText.setTextColor(Color.parseColor(textColor));
                }
            } else {
                headerText.setVisibility(View.GONE);
                headerImage.setVisibility(View.VISIBLE);
                ImageDisplayUtils.getInstance().displayImage(headerImage, image + "@2x.png");
            }
            String barTintColorString = getArguments().getString(Constant.WEB_FRAGMENT_BAR_TINT_COLOR);
            if (!TextUtils.isEmpty(barTintColorString)) {
                headerLayout.setBackgroundColor(Color.parseColor(barTintColorString));
            }
            int titleBarHeight = getArguments().getInt(Constant.WEB_FRAGMENT_TITLE_BAR_HEIGHT, -1);
            if (titleBarHeight >= 0) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headerLayout.getLayoutParams();
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = DensityUtil.dip2px(titleBarHeight);
                headerLayout.setLayoutParams(params);
            }
        } else {
            webView.setProperty(null, frameLayout, impCallBackInterface);
        }
    }

    private void showDropTitlePop() {
        // 一个自定义的布局，作为显示的内容
        if (dropTitlePopupWindow == null) {
            View contentView = LayoutInflater.from(ImpFragment.this.getContext())
                    .inflate(R.layout.web_pop_drop_title, null);
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
            listView.setMaxHeight(DensityUtil.dip2px(BaseApplication.getInstance(), 240));
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

    public void displayOrHideTitle(boolean show) {
        headerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void initImpCallBackInterface() {
        impCallBackInterface = new ImpCallBackInterface() {
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
                if (!isStaticWebTitle && !isTitlePriorityFirst) {
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
            public void onStartActivityForResult(String routerPath, Bundle bundle, int requestCode) {
                //ARouter不支持fragment.startActivityForResult().
                Postcard postcard = ARouter.getInstance().build(routerPath).with(bundle);
                LogisticsCenter.completion(postcard);
                Intent intent = new Intent(getActivity(), postcard.getDestination());
                intent.putExtras(postcard.getExtras());
                startActivityForResult(intent, requestCode);


//                ARouter.getInstance().build(routerPath).with(bundle).navigation(ImpFragment.this.getActivity(),requestCode);
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

            @Override
            public void setOnTitleBackKeyDownListener(OnTitleBackKeyDownListener onTitleBackKeyDownListener) {
                ImpFragment.this.onTitleBackKeyDownListener = onTitleBackKeyDownListener;
            }

            @Override
            public boolean isWebFromIndex() {
                return isStaticWebTitle;
            }

            @Override
            public void showLoadFailLayout(String url, String description) {
                errorUrl = url;
                errorDescription = description;
                if (loadFailLayout != null) {
                    loadFailLayout.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void hideScreenshotImg() {
                screenshotImg.clearAnimation();
                screenshotImg.setVisibility(View.GONE);
            }

            @Override
            public void showScreenshotImg(String screenshotImgPath) {
                ImpFragment.this.screenshotImgPath = screenshotImgPath;
                screenshotImg.setVisibility(View.VISIBLE);
                ImageDisplayUtils.getInstance().displayImage(screenshotImg, screenshotImgPath);
                float scale = rootLayout.getWidth() * 1.0f / DensityUtil.dip2px(90);
                int fromX = DensityUtil.dip2px((45 * scale - 75));
                float fromY = -(rootLayout.getHeight() - rootLayout.getHeight() * 1.0f / rootLayout.getWidth() * DensityUtil.dip2px(90) - DensityUtil.dip2px(40));
                TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, fromX, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, fromY, Animation.ABSOLUTE, 0);
                Animation scaleAnimation = new ScaleAnimation(scale, 1, scale, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                AnimationSet animationSet = new AnimationSet(true);

                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(500);
                screenshotImg.startAnimation(animationSet);
                if (handler != null) {
                    handler.removeCallbacks(screenshotRunnable);
                    handler.postDelayed(screenshotRunnable, 3000);
                }
            }
        };
    }


    /**
     * 与主Fragment通信的接口
     *
     * @return
     */
    private ImpCallBackInterface getImpCallBackInterface() {
        return impCallBackInterface;
    }

    private void setHeaderTitleTextDropImg() {
        boolean isDropTitlePopShow = (dropTitlePopupWindow != null && dropTitlePopupWindow.isShowing());
        int dropUpRes = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.plugin_ic_header_title_drop_up);
        int dropDownRes = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.plugin_ic_header_title_drop_down);
        Drawable drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), isDropTitlePopShow ? dropUpRes : dropDownRes);
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
                closeBtn.setVisibility(webView.canGoBack() && !webView.getImpWebViewClient().isLogin() ? View.VISIBLE : View.GONE);
            }
            setHeaderTextWidth();
        }
    }

    public void setTitle(String title) {
        if (!StringUtils.isBlank(title) && headerText != null) {
            urlTitleMap.put(webView.getUrl(), title);
            headerText.setText(title);
        }
    }

    /**
     * 解决有的机型Webview goback时候不会获取title的问题
     */
    private void setGoBackTitle() {
        if (headerText != null) {
            String title = urlTitleMap.get(webView.getUrl());
            if (!StringUtils.isBlank(title)) {
                headerText.setText(title);
            }
        }

    }

    /**
     * 返回
     */
    public boolean onBackKeyDown() {
        if (ImpFragment.this.onKeyDownListener != null) {
            ImpFragment.this.onKeyDownListener.onBackKeyDown();
        } else {
            if (!webView.getWebChromeClient().hideCustomView()) {
                if (webView.canGoBack()) {
                    webView.goBack();// 返回上一页面
                    setGoBackTitle();
                } else {
                    finishActivity();
                }
            }

        }
        return true;
    }

    public void finishActivity() {
        if (!StringUtils.isBlank(getArguments().getString("function")) && getArguments().getString("function").equals("mdm")) {
            Router router = Router.getInstance();
            if (router.getService(LoginService.class) != null) {
                LoginService service = router.getService(LoginService.class);
                service.setMDMStatusNoPass();
            }
        }
        webView.onActivityDestroy();
        getActivity().finish();// 退出程序
    }

    /**
     * 设置WebView的Header参数
     */
    private void setWebViewHeader(String url) {
        webViewHeaders = new HashMap<>();
        addAuthorizationToken(url);
        if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
            webViewHeaders.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
        }
        webViewHeaders.put("Accept-Language", LanguageManager.getInstance().getCurrentAppLanguage());
    }

    private void addAuthorizationToken(String url) {
        try {
            String token = BaseApplication.getInstance().getToken();
            if (token != null && AppUtils.needAuthorizationToken(url)) {
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

        view.findViewById(R.id.app_imp_crm_font_normal_btn).setOnClickListener(this);
        view.findViewById(R.id.app_imp_crm_font_middle_btn).setOnClickListener(this);
        view.findViewById(R.id.app_imp_crm_font_big_btn).setOnClickListener(this);
        view.findViewById(R.id.app_imp_crm_font_biggest_btn).setOnClickListener(this);

        if (getArguments().getInt("is_zoomable", 0) == 1) {
            setWebViewButtonTextColor(0);
        }
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.app_imp_crm_font_normal_btn) {
            setNewsFontSize(MyAppWebConfig.NORMAL);

        } else if (i == R.id.app_imp_crm_font_middle_btn) {
            setNewsFontSize(MyAppWebConfig.CRM_BIG);

        } else if (i == R.id.app_imp_crm_font_big_btn) {
            setNewsFontSize(MyAppWebConfig.CRM_BIGGER);

        } else if (i == R.id.app_imp_crm_font_biggest_btn) {
            setNewsFontSize(MyAppWebConfig.CRM_BIGGEST);

        }
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
            webView.onActivityDestroy();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        if (handler != null) {
            handler.removeCallbacks(screenshotRunnable);
            handler = null;
        }
        impCallBackInterface = null;
        //清除掉图片缓存
//        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        EventBus.getDefault().unregister(this);
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
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }

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
                if (null == mUploadCallbackAboveL) {
                    return;
                }
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
                if (null == mUploadMessage) {
                    return;
                }
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
                    case SELECTOR_SERVICE_GALLERY_REQUEST:
                        serviceName = PhotoService.class.getCanonicalName().trim();
                        break;
                    case SELECT_STAFF_SERVICE_REQUEST:
                        serviceName = SelectStaffService.class.getCanonicalName().trim();
                        break;
                    case BARCODE_SERVER__SCAN_REQUEST:
                        serviceName = BarCodeService.class.getCanonicalName().trim();
                        break;
                    case SELECT_FILE_SERVICE_REQUEST:
                        serviceName = FileTransferService.class.getCanonicalName().trim();
                        break;
                    case REQUEST_CODE_RECORD_VIDEO:
                        serviceName = VideoService.class.getCanonicalName();
                        break;
                    case REQUEST_EDIT_SCREENSHOT_IMG:
                        serviceName = ScreenshotService.class.getCanonicalName();
                        break;
                    case SHARE_WEB_URL_REQUEST:
                        serviceName = WindowService.class.getCanonicalName();
                        break;
                    case REQUEST_CODE_RECORD_AUDIO:
                        serviceName = IMPAudioService.class.getCanonicalName();
                        break;
                    case REQUEST_CONNECT_DEVICE_SECURE:
                    case REQUEST_CONNECT_DEVICE_INSECURE:
                        serviceName = BlueToothService.class.getCanonicalName();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWechatEvent(SimpleEventMessage message) {
        if (message.getAction().equals(Constant.EVENTBUS_TAG_WECHAT_RESULT)) {
            String result = (String) message.getMessageObj();

            String serviceName = InvoiceService.class.getCanonicalName();
            PluginMgr pluginMgr = webView.getPluginMgr();
            InvoiceService invoiceService = (InvoiceService) pluginMgr.getPlugin(serviceName);
            invoiceService.handleWechatResult(result);
        }
    }


    @OnClick({R2.id.imp_change_font_size_btn, R2.id.ibt_back, R2.id.imp_close_btn, R2.id.tv_look_web_error_detail,
            R2.id.tv_reload_web, R2.id.header_text, R2.id.iv_screenshot})
    public void onViewClick(View v) {
        int i = v.getId();
        if (i == R.id.imp_change_font_size_btn) {
            showChangeFontSizeDialog();

        } else if (i == R.id.ibt_back) {
            if (ImpFragment.this.onTitleBackKeyDownListener != null) {
                ImpFragment.this.onTitleBackKeyDownListener.onTitleBackKeyDown();
            } else {
                if (webView.canGoBack()) {
                    webView.goBack();// 返回上一页面
                    setGoBackTitle();
                } else {
                    finishActivity();
                }
            }

        } else if (i == R.id.imp_close_btn) {
            finishActivity();

        } else if (i == R.id.tv_look_web_error_detail) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_OUTSIDE_URL, errorUrl);
            bundle.putString(EXTRA_OUTSIDE_URL_REQUEST_RESULT, errorDescription);
            ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_WEB_ERROR_DETAIL).with(bundle).navigation();

        } else if (i == R.id.tv_reload_web) {
            showLoadingDlg(getString(Res.getStringID("@string/loading_text")));
            webView.reload();
            webView.setVisibility(View.INVISIBLE);
            loadFailLayout.setVisibility(View.GONE);

        } else if (i == R.id.header_text) {
            if (dropItemTitleList != null && dropItemTitleList.size() > 0) {
                if (dropTitlePopupWindow != null && dropTitlePopupWindow.isShowing()) {
                    dropTitlePopupWindow.dismiss();
                } else {
                    showDropTitlePop();
                    setHeaderTitleTextDropImg();
                }
            }

        } else if (i == R.id.iv_screenshot) {
            screenshotImg.setVisibility(View.GONE);
            startActivityForResult(new Intent(getActivity(), IMGEditActivity.class)
                    .putExtra(IMGEditActivity.EXTRA_IS_COVER_ORIGIN, true)
                    .putExtra(IMGEditActivity.OUT_FILE_PATH_IN_PICTURE, true)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, screenshotImgPath), REQUEST_EDIT_SCREENSHOT_IMG);
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
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.web_pop_drop_list_item_view, null);
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
