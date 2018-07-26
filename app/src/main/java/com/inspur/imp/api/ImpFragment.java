package com.inspur.imp.api;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.bean.system.MainTabMenu;
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MDM.MDM;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.plugin.IPlugin;
import com.inspur.imp.plugin.PluginMgr;
import com.inspur.imp.plugin.barcode.scan.BarCodeService;
import com.inspur.imp.plugin.camera.CameraService;
import com.inspur.imp.plugin.file.FileService;
import com.inspur.imp.plugin.photo.PhotoService;
import com.inspur.imp.plugin.staff.SelectStaffService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2018/7/9.
 */

public class ImpFragment extends Fragment {
    private ImpWebView webView;
    // 浏览文件resultCode
    public static final int CAMERA_SERVICE_CAMERA_REQUEST = 1;
    public static final int CAMERA_SERVICE_GALLERY_REQUEST = 2;
    public static final int PHOTO_SERVICE_CAMERA_REQUEST = 3;
    public static final int PHOTO_SERVICE_GALLERY_REQUEST = 4;
    public static final int SELECT_STAFF_SERVICE_REQUEST = 5;
    public static final int FILE_SERVICE_REQUEST = 6;
    public static final int DO_NOTHING_REQUEST = 7;
    public static final int BARCODE_SERVER__SCAN_REQUEST = 8;
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
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(Res.getLayoutID("activity_imp"), null);
        initViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(Res.getLayoutID("activity_imp"), container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
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
        loadingLayout = (RelativeLayout) rootView.findViewById(Res.getWidgetID("loading_layout"));
        loadingText = (TextView) rootView.findViewById(Res.getWidgetID("loading_text"));
        frameLayout = (FrameLayout) rootView.findViewById(Res.getWidgetID("videoContainer"));
        loadFailLayout = (LinearLayout) rootView.findViewById(Res.getWidgetID("load_error_layout"));
        initMomentFunction();

        webView = (ImpWebView) rootView.findViewById(Res.getWidgetID("webview"));
        if (getActivity().getClass().getName().equals(IndexActivity.class.getName())) {
            rootView.findViewById(R.id.back_layout).setVisibility(View.GONE);
            ((TextView) rootView.findViewById(R.id.header_text)).setGravity(Gravity.CENTER_HORIZONTAL);
        }
        showLoadingDlg(getString(Res.getStringID("@string/loading_text")));
        if (!StringUtils.isBlank(getArguments().getString("help_url"))) {
            String helpUrl = getArguments().getString("help_url");
            if (!StringUtils.isBlank(helpUrl)) {
                this.helpUrl = helpUrl;
            }
        }
        if (!StringUtils.isBlank(getArguments().getString("appId"))) {
            appId = getArguments().getString("appId");
        }
        String url = getArguments().getString("uri");
        initWebViewHeaderLayout();
        setWebViewHeader();
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
        setWebViewFunctionVisiable();

    }

    /**
     * 配置圈子导航栏上的功能
     */
    private void initMomentFunction() {
        final ArrayList<MainTabMenu> mainTabMenuArrayList = (ArrayList<MainTabMenu>)getArguments().getSerializable("menuList");
        if(mainTabMenuArrayList != null){
            if(mainTabMenuArrayList.size() == 1){
                ImageView imageViewFun1 = (ImageView) rootView.findViewById(R.id.imp_cloud_function1_img);
                imageViewFun1.setVisibility(View.VISIBLE);
                ImageDisplayUtils.getInstance().displayImage(imageViewFun1,mainTabMenuArrayList.get(0).getIco());
            }else if(mainTabMenuArrayList.size() == 2){
                ImageView imageViewFun1 = (ImageView) rootView.findViewById(R.id.imp_cloud_function1_img);
                ImageView imageViewFun2 = (ImageView) rootView.findViewById(R.id.imp_cloud_function2_img);
                imageViewFun1.setVisibility(View.VISIBLE);
                imageViewFun2.setVisibility(View.VISIBLE);
                ImageDisplayUtils.getInstance().displayImage(imageViewFun1,mainTabMenuArrayList.get(0).getIco());
                ImageDisplayUtils.getInstance().displayImage(imageViewFun2,mainTabMenuArrayList.get(1).getIco());
            }
        }
        rootView.findViewById(R.id.imp_cloud_function2_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:"+mainTabMenuArrayList.get(1).getAction());
            }
        });
        rootView.findViewById(R.id.imp_cloud_function1_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:"+mainTabMenuArrayList.get(0).getAction());
            }
        });
    }


    /**
     * 返回逻辑
     *
     * @return
     */
    public boolean onKeyDown() {
        if (webView.canGoBack()) {
            webView.goBack();// 返回上一页面
            return true;
        } else {
            finishActivity();// 退出程序
        }
        return false;
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
        if (!StringUtils.isBlank(getArguments().getString("is_zoomable"))) {
            int isZoomable = getArguments().getInt("is_zoomable", 0);
            if (isZoomable == 1 || !StringUtils.isBlank(helpUrl)) {
                rootView.findViewById(R.id.imp_change_font_size_btn).setVisibility(View.VISIBLE);
            }
            if (isZoomable == 1) {
                int textSize = PreferencesByUsersUtils.getInt(getActivity(), "app_crm_font_size_" + appId, MyAppWebConfig.NORMAL);
                webView.getSettings().setTextZoom(textSize);
            }
        }
    }

    /**
     * 初始化webview haader layout
     */
    private void initWebViewHeaderLayout() {
        ImpCallBackInterface impCallBackInterface = getImpCallBackInterface();
        if (!StringUtils.isEmpty(getArguments().getString("appName"))) {
            String title = getArguments().getString("appName");
            headerText = (TextView) rootView.findViewById(Res.getWidgetID("header_text"));
            webView.setProperty(headerText, loadFailLayout, frameLayout, impCallBackInterface);
            initWebViewGoBackOrClose();
            (rootView.findViewById(Res.getWidgetID("header_layout")))
                    .setVisibility(View.VISIBLE);
            headerText.setText(title);
        } else {
            webView.setProperty(null, loadFailLayout, frameLayout, impCallBackInterface);
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
            public Map<String, String> onGetWebViewHeaders() {
                return getWebViewHeaders();
            }

            @Override
            public void onInitWebViewGoBackOrClose() {
                initWebViewGoBackOrClose();
            }

            @Override
            public void onSetTitle(String title) {
                setTitle(title);
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
        };

    }

    /**
     * 初始化原生WebView的返回和关闭
     * （不是GS应用，GS应用有重定向，不容易实现返回）
     */
    public void initWebViewGoBackOrClose() {
        if (headerText != null) {
            (rootView.findViewById(Res.getWidgetID("imp_close_btn"))).
                    setVisibility((webView.canGoBack()&&getActivity().getClass().getName().
                            equals(ImpActivity.class.getName())) ? View.VISIBLE : View.GONE);
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
        if (!StringUtils.isBlank(getArguments().getString("function")) && getArguments().getString("function").equals("mdm")) {
            new MDM().getMDMListener().MDMStatusNoPass();
        }
        getActivity().finish();// 退出程序
    }


    private void setWebViewHeader() {
        webViewHeaders = new HashMap<>();
        String token = MyApplication.getInstance().getToken();
        if (token != null) {
            webViewHeaders.put("Authorization", token);
        }
        webViewHeaders.put("X-ECC-Current-Enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
        String languageJson = PreferencesUtils.getString(
                getActivity(), MyApplication.getInstance().getTanent() + "appLanguageObj");
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
            case R.id.imp_cloud_function2_img:
                LogUtils.YfcDebug("点击了function2");
                break;
            case R.id.imp_cloud_function1_img:
                LogUtils.YfcDebug("点击了function1");
                break;
            default:
                break;
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

        if (!StringUtils.isBlank(getArguments().getString("is_zoomable")) && (getArguments().getInt("is_zoomable", 0) == 1)) {
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

        if (!StringUtils.isBlank(getArguments().getString("is_zoomable")) && (getArguments().getInt("is_zoomable", 0) == 1)) {
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
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        EasyDialog.showDialog(getActivity(), getString(R.string.prompt),
                getString(R.string.imp_function_error),
                getString(R.string.ok), listener, false);
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
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
