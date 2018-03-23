package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.bean.appcenter.GetAppBadgeResult;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.TransparentBean;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.GetAllContactResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.AppException;
import com.inspur.emmcloud.bean.system.AppTabAutoBean;
import com.inspur.emmcloud.bean.system.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.interf.OnTabReselectListener;
import com.inspur.emmcloud.service.BackgroundService;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.service.LocationService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.appcenter.MyAppFragment;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.chat.MessageFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.MainTabBean;
import com.inspur.emmcloud.ui.work.WorkFragment;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppConfigUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MyAppWidgetUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.PushInfoUtils;
import com.inspur.emmcloud.util.privates.ReactNativeUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyFragmentTabHost;
import com.inspur.emmcloud.widget.WeakHandler;
import com.inspur.emmcloud.widget.WeakThread;
import com.inspur.emmcloud.widget.tipsview.TipsView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 主页面
 *
 * @author Administrator
 */
public class IndexActivity extends BaseFragmentActivity implements
        OnTabChangeListener, OnTouchListener {
    private static final int SYNC_ALL_BASE_DATA_SUCCESS = 0;
    private static final int RELOAD_WEB = 3;
    private long lastBackTime;
    public MyFragmentTabHost mTabHost;
    private static TextView newMessageTipsText;
    private static RelativeLayout newMessageTipsLayout;
    private WeakHandler handler;
    private boolean isHasCacheContact = false;
    private TipsView tipsView;
    private LoadingDialog loadingDlg;
    private AppAPIService appApiService;
    private String notSupportTitle = "";
    private WebView webView;
    private boolean isCommunicationRunning = false;
    private boolean isSystemChangeTag = true;//控制如果是系统切换的tab则不计入用户行为
    private ContactSaveTask contactSaveTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this);
        setContentView(R.layout.activity_index);
        initAppEnvironment();
        initView();
        getData();
        startService();
        EventBus.getDefault().register(this);
        new PushInfoUtils(this).upload();//上传推送信息
    }

    /**
     * 初始化app的运行环境
     */
    private void initAppEnvironment() {
        ((MyApplication) getApplicationContext()).setIndexActvityRunning(true);
        ((MyApplication) getApplicationContext()).closeAllDb();
        DbCacheUtils.initDb(getApplicationContext());
        ((MyApplication) getApplicationContext()).closeWebSocket();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        ((MyApplication) getApplicationContext()).startPush();
    }

    private void initView() {
        appApiService = new AppAPIService(IndexActivity.this);
        appApiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(IndexActivity.this, getString(R.string.app_init));
        handMessage();
        initTabView();
        setPreloadWebApp();
    }

    /**
     * 初始化
     */
    private void getData() {
        new AppConfigUtils(IndexActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                startLocationService();
            }
        }).getAppConfig(); //获取整个应用的配置信息,获取完成后启动位置服务
        String contactLastUpdateTime = ContactCacheUtils
                .getLastUpdateTime(IndexActivity.this);
        isHasCacheContact = !StringUtils.isBlank(contactLastUpdateTime);
        if (!isHasCacheContact) {
            loadingDlg.show();
        }
        getContactInfo();
        getAllRobotInfo();
        getAppTabInfo();  //从服务端获取显示tab
        new SplashPageUtils(IndexActivity.this).update();//更新闪屏页面
        new ReactNativeUtils(IndexActivity.this).init(); //更新react
        getMyAppRecommendWidgets();
    }

    /**
     * 获取我的应用推荐小部件数据
     */
    private void getMyAppRecommendWidgets() {
        if (MyAppWidgetUtils.checkNeedUpdateMyAppWidget(IndexActivity.this)) {
            MyAppWidgetUtils.getInstance(getApplicationContext()).getMyAppWidgetsFromNet();
        }
    }

    /**
     * 启动服务
     */
    private void startService() {
        startUploadPVCollectService();
        startCoreService();
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        if (isAppSetRunBackground) {
            startBackgroudService();
        }
        startLocationService();
    }

    /***
     * 打开app应用行为分析上传的Service;
     */
    private void startUploadPVCollectService() {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(getApplicationContext(), "com.inspur.emmcloud.service.CollectService")) {
            Intent intent = new Intent();
            intent.setClass(this, PVCollectService.class);
            startService(intent);
        }
    }


    /**
     * 打开保活服务
     */
    private void startCoreService() {
        if (AppUtils.getSDKVersionNumber() < 26) {
            Intent intent = new Intent();
            intent.setClass(this, CoreService.class);
            startService(intent);
        }
    }

    /**
     * 打开后台保活服务
     */
    private void startBackgroudService() {
        Intent intent = new Intent();
        intent.setClass(this, BackgroundService.class);
        startService(intent);
    }

    /**
     * 打开位置收集服务
     */
    private void startLocationService() {
        Intent intent = new Intent();
        intent.setClass(this, LocationService.class);
        startService(intent);
    }

    /**
     * 为了使打开报销web应用更快，进行预加载
     */
    private void setPreloadWebApp() {
        if (MyApplication.getInstance().getTanent().equals("inspur_esg")) {
            webView = (WebView) findViewById(R.id.preload_webview);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSavePassword(false);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    view.loadUrl(url);
                    return true;
                }
            });
            webView.loadUrl("http://baoxiao.inspur.com/loadres.html");
            handler.sendEmptyMessageDelayed(RELOAD_WEB, 1000);
        }
    }


    /**
     * 获取应用显示tab
     */
    private void getAppTabInfo() {
        new ClientIDUtils(IndexActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
                    AppAPIService apiService = new AppAPIService(IndexActivity.this);
                    apiService.setAPIInterface(new WebService());
                    String version = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, "app_tabbar_version", "");
                    String clientId = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, Constant.PREF_REACT_NATIVE_CLIENTID, "");
                    apiService.getAppNewTabs(version, clientId);

                }
            }
        }).getClientID();


    }

    /**
     * 获取所有的Robot
     */
    private void getAllRobotInfo() {
        ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
        apiService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            apiService.getAllRobotInfo();
        }
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new WeakHandler(IndexActivity.this) {

            @Override
            protected void handleMessage(Object o, Message msg) {
                switch (msg.what) {
                    case SYNC_ALL_BASE_DATA_SUCCESS:
                        if (loadingDlg != null && loadingDlg.isShowing()) {
                            loadingDlg.dismiss();
                        }

                        ((MyApplication) getApplicationContext())
                                .setIsContactReady(true);
                        notifySyncAllBaseDataSuccess();
                        deleteIllegalUser();
                        handleShareIntent();
                        break;
                    case RELOAD_WEB:
                        if (webView != null) {
                            webView.reload();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 处理带分享功能的Action
     */
    private void handleShareIntent() {
        if (getIntent() != null) {
            String action = getIntent().getAction();
            List<Uri> uriList = new ArrayList<>();
            if (Intent.ACTION_SEND.equals(action)) {
                Uri uri = FileUtils.getShareFileUri(getIntent());
                if (uri != null) {
                    uriList.add(uri);
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                List<Uri> fileUriList = FileUtils.getShareFileUriList(getIntent());
                uriList.addAll(fileUriList);
            }
            if (uriList.size() > 0) {
                showShareFileDlg(uriList);
            }
        }
    }

    /**
     * 弹出上传文件到网盘的dialog
     */
    private void showShareFileDlg(final List<Uri> uriList) {
        // TODO Auto-generated method stub
        final String[] items = new String[]{getString(R.string.volume_upload_file_to_volume)};
        new QMUIDialog.MenuDialogBuilder(this)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                startVolumeShareActivity(uriList);
                                finish();
                                break;
//                            case 1:
//                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * @param uriList
     */
    private void startVolumeShareActivity(List<Uri> uriList) {
        Intent intent = new Intent();
        intent.setClass(IndexActivity.this, VolumeHomePageActivity.class);
        intent.putExtra("fileShareList", (Serializable) uriList);
        startActivity(intent);
    }

    /**
     * 通讯录完成时发送广播
     */
    private void notifySyncAllBaseDataSuccess() {
        // TODO Auto-generated method stub
        //当通讯录完成时需要刷新头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sync_all_base_data_success");
        sendBroadcast(intent);
    }

    private void deleteIllegalUser() {
        try {
            boolean isHasDeletleIllegalUser = PreferencesByUserAndTanentUtils.getBoolean(getApplicationContext(), Constant.PREF_DELETE_ILLEGAL_USER, false);
            if (!isHasDeletleIllegalUser) {
                int illegalUserCount = ContactCacheUtils.deleteIllegalUser(getApplicationContext());
                if (illegalUserCount != -1) {
                    PreferencesByUserAndTanentUtils.putBoolean(getApplicationContext(), Constant.PREF_DELETE_ILLEGAL_USER, true);
                }
                if (illegalUserCount != 0) {
                    AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(getApplicationContext()), 5, "", "通讯录删除无效用户个数" + illegalUserCount, -1);
                    AppExceptionCacheUtils.saveAppException(getApplicationContext(), appException);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有的群组信息
     */
    private void getAllChannelGroup() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ((MyApplication) getApplicationContext()).setIsContactReady(false);
            ChatAPIService apiService = new ChatAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getAllGroupChannelList();
        } else if (isHasCacheContact) {
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }
    }

    /**
     * 获取通讯录信息
     */
    private void getContactInfo() {
        // TODO Auto-generated method stub
        ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
        apiService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ((MyApplication) getApplicationContext()).setIsContactReady(false);

            String contackLastUpdateTime = ContactCacheUtils
                    .getLastUpdateTime(IndexActivity.this);
            apiService.getAllContact(contackLastUpdateTime);

        } else if (isHasCacheContact) {
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }
    }

    /**
     * 显示消息tab上的小红点（未读消息提醒）
     *
     * @param num
     */
    public static void showNotifyIcon(int num) {
        if (newMessageTipsText == null) {
            return;
        }
        if (num == 0) {
            newMessageTipsLayout.setVisibility(View.GONE);
        } else {
            String shoWNum = "";
            if (num > 99) {
                shoWNum = "99+";
            } else {
                shoWNum = num + "";
            }
            newMessageTipsLayout.setVisibility(View.VISIBLE);
            newMessageTipsText.setText(shoWNum);
        }
    }

    /**
     * 初始化底部的4个Tab
     */
    private void initTabView() {
        tipsView = (TipsView) findViewById(R.id.tip);
        mTabHost = (MyFragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        findViewById(R.id.index_root_layout).setPadding(0, StateBarUtils.getStateBarHeight(IndexActivity.this), 0, 0);
        handleAppTabs();
    }

    /**
     * 处理tab数组
     *
     * @return
     */
    private MainTabBean[] handleAppTabs() {
        MainTabBean[] mainTabs = null;
        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, "app_tabbar_info_current", "");
        if (!StringUtils.isBlank(appTabs)) {
            Configuration config = getResources().getConfiguration();
            String environmentLanguage = config.locale.getLanguage();
            AppTabAutoBean appTabAutoBean = new AppTabAutoBean(appTabs);
            if (appTabAutoBean != null) {
                EventBus.getDefault().post(appTabAutoBean);
            }
            ArrayList<AppTabAutoBean.PayloadBean.TabsBean> appTabList = (ArrayList<AppTabAutoBean.PayloadBean.TabsBean>) appTabAutoBean.getPayload().getTabs();
            if (appTabList != null && appTabList.size() > 0) {
                mainTabs = new MainTabBean[appTabList.size()];
                for (int i = 0; i < appTabList.size(); i++) {
                    MainTabBean mainTabBean = null;
                    switch (appTabList.get(i).getComponent()) {
                        case "communicate":
                            mainTabBean = new MainTabBean(i, R.string.communicate, R.drawable.selector_tab_message_btn, MessageFragment.class);
                            mainTabBean.setCommpant(appTabList.get(i).getComponent());
                            break;
                        case "work":
                            mainTabBean = new MainTabBean(i, R.string.work, R.drawable.selector_tab_work_btn,
                                    WorkFragment.class);
                            mainTabBean.setCommpant(appTabList.get(i).getComponent());
                            break;
                        case "find":
                            mainTabBean = new MainTabBean(i, R.string.find, R.drawable.selector_tab_find_btn,
                                    FindFragment.class);
                            mainTabBean.setCommpant(appTabList.get(i).getComponent());
                            break;
                        case "application":
                            mainTabBean = new MainTabBean(i, R.string.application, R.drawable.selector_tab_app_btn,
                                    MyAppFragment.class);
                            mainTabBean.setCommpant(appTabList.get(i).getComponent());
                            break;
                        case "mine":
                            mainTabBean = new MainTabBean(i, R.string.mine, R.drawable.selector_tab_more_btn,
                                    MoreFragment.class);
                            mainTabBean.setCommpant(appTabList.get(i).getComponent());
                            break;
                        default:
                            mainTabBean = new MainTabBean(i, R.string.unknown, R.drawable.selector_tab_unknown_btn,
                                    NotSupportFragment.class);
                            break;
                    }
                    mainTabs[i] = internationalMainLanguage(appTabList.get(i), environmentLanguage, mainTabBean);
                }
            }
        }
        if (mainTabs == null) {
            mainTabs = addDefaultTabs();
        }
        displayMainTabs(mainTabs);
        return mainTabs;
    }

    /**
     * 根据定制展示App
     *
     * @param tabs
     */
    private void displayMainTabs(MainTabBean[] tabs) {
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTabBean mainTab = tabs[i];
            TabHost.TabSpec tab = mTabHost.newTabSpec(getString(mainTab.getResName()));
            View tabView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.tab_item_view, null);
            ImageView tabImg = (ImageView) tabView.findViewById(R.id.imageview);
            TextView tabText = (TextView) tabView.findViewById(R.id.textview);
            if (mainTab.getCommpant().equals("communicate")) {
                handleTipsView(tabView);
            }
            if (!StringUtils.isBlank(mainTab.getConfigureName())) {
                tabText.setText(mainTab.getConfigureName());
            } else {
                tabText.setText(getString(mainTab.getResName()));
            }
            if (!StringUtils.isBlank(mainTab.getConfigureIcon())) {
                ImageDisplayUtils.getInstance().displayImage(tabImg, mainTab.getConfigureIcon(), R.drawable.ic_app_default);
            } else {
                tabImg.setImageResource(mainTab.getResIcon());
            }
            tab.setIndicator(tabView);
            tab.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {
                    return new View(IndexActivity.this);
                }
            });

            mTabHost.addTab(tab, mainTab.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
            mTabHost.getTabWidget().getChildAt(i).setTag(mainTab.getCommpant());
            mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
            mTabHost.setOnTabChangedListener(this);
        }
        int tabSize = tabs.length;
        int communicateLocation = -1;
        for (int i = 0; i < tabSize; i++) {
            if (tabs[i].getCommpant().equals("communicate")) {
                communicateLocation = tabs[i].getIdx();
                break;
            }
        }
        if (communicateLocation != -1 && isCommunicationRunning == false) {
            mTabHost.setCurrentTab(communicateLocation);
        } else {
            mTabHost.setCurrentTab(getTabIndex());
        }
    }

    /**
     * 更新底部tab数字，从MyAppFragment badge请求返回
     *
     * @param getAppBadgeResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateBadgeNumber(GetAppBadgeResult getAppBadgeResult) {
        int badgeNumber = getAppBadgeResult.getTabBadgeNumber();
        findAndSetUnhandleBadgesDisplay(badgeNumber);
    }

    /**
     * 查找应用tab并改变tab上的角标
     *
     * @param badgeNumber
     */
    private void findAndSetUnhandleBadgesDisplay(int badgeNumber) {
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            View tabView = mTabHost.getTabWidget().getChildAt(i);
            if (mTabHost.getTabWidget().getChildAt(i).getTag().toString().contains("application")) {
                setUnHandledBadgesDisplay(tabView, badgeNumber);
                break;
            }
        }
    }

    /**
     * 修改tab角标，来自ECMTransparentUtils
     *
     * @param transparentBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateBadgeNumber(TransparentBean transparentBean) {
        findAndSetUnhandleBadgesDisplay(transparentBean.getBadgeNumber());
    }

    /**
     * 处理未处理消息个数的显示
     *
     * @param tabView
     */
    private void setUnHandledBadgesDisplay(View tabView, int badgeNumber) {
        RelativeLayout unhandledBadgesLayout = (RelativeLayout) tabView.findViewById(R.id.new_message_tips_layout);
        unhandledBadgesLayout.setVisibility((badgeNumber == 0) ? View.GONE : View.VISIBLE);
        TextView unhandledBadgesText = (TextView) tabView.findViewById(R.id.new_message_tips_text);
        unhandledBadgesText.setText("" + (badgeNumber > 99 ? "99+" : badgeNumber));
        //更新桌面角标数字
        ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(IndexActivity.this, badgeNumber);
    }

    /**
     * IndexActiveX首先打开MessageFragment,然后打开其他tab
     */
    public void openTargetFragment() {
        try {
            isCommunicationRunning = true;
            int targetTabIndex = getTabIndex();
            boolean isOpenNotify = getIntent().hasExtra("command") && getIntent().getStringExtra("command").equals("open_notification");
            if (mTabHost != null && mTabHost.getCurrentTab() != targetTabIndex && !isOpenNotify) {
                mTabHost.setCurrentTab(targetTabIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当没有数据的时候返回内容
     *
     * @return
     */
    private MainTabBean[] addDefaultTabs() {
        //无数据改为显示两个tab，数组变为2
        MainTabBean[] mainTabs = new MainTabBean[2];
        MainTabBean mainTabBeanCommunicate = new MainTabBean(0, R.string.communicate, R.drawable.selector_tab_message_btn,
                MessageFragment.class);
        mainTabBeanCommunicate.setCommpant("communicate");
        MainTabBean mainTabBeanApp = new MainTabBean(3, R.string.application, R.drawable.selector_tab_app_btn,
                MyAppFragment.class);
        MainTabBean mainTabBeanMine = new MainTabBean(4, R.string.mine, R.drawable.selector_tab_more_btn,
                MoreFragment.class);
        //无数据改为显示两个tab
        mainTabs[0] = mainTabBeanApp;
        mainTabs[1] = mainTabBeanMine;
        return mainTabs;
    }

    /**
     * 暴露当前页面标题接口
     *
     * @return
     */
    public String getNotSupportString() {
        return notSupportTitle;
    }


    /**
     * 根据语言设置tab，扩展语言从这里扩展
     *
     * @param tabsBean
     * @param environmentLanguage
     * @return
     */
    private MainTabBean internationalMainLanguage(AppTabAutoBean.PayloadBean.TabsBean tabsBean, String environmentLanguage, MainTabBean mainTab) {
        if (environmentLanguage.toLowerCase().equals("zh") || environmentLanguage.toLowerCase().equals("zh-Hans".toLowerCase())) {
            mainTab.setConfigureName(tabsBean.getTitle().getZhHans());
        } else if (environmentLanguage.toLowerCase().equals("zh-Hant".toLowerCase())) {
            mainTab.setConfigureName(tabsBean.getTitle().getZhHant());
        } else if (environmentLanguage.toLowerCase().equals("en-US".toLowerCase()) ||
                environmentLanguage.toLowerCase().equals("en".toLowerCase())) {
            mainTab.setConfigureName(tabsBean.getTitle().getEnUS());
        } else {
            mainTab.setConfigureName(tabsBean.getTitle().getZhHans());
        }
        return mainTab;
    }

    /**
     * 处理小红点的逻辑
     *
     * @param tabView
     */
    private void handleTipsView(View tabView) {
        newMessageTipsText = (TextView) tabView
                .findViewById(R.id.new_message_tips_text);
        newMessageTipsLayout = (RelativeLayout) tabView.findViewById(R.id.new_message_tips_layout);
        tipsView.attach(newMessageTipsLayout, new TipsView.Listener() {

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onComplete() {
                // TODO Auto-generated method stub
                Intent intent = new Intent("message_notify");
                intent.putExtra("command", "set_all_message_read");
                sendBroadcast(intent);
                showNotifyIcon(0);
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * 获取显示位置
     *
     * @return
     */
    private int getTabIndex() {
        int tabIndex = 0;
        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, "app_tabbar_info_current", "");
        ArrayList<AppTabAutoBean.PayloadBean.TabsBean> appTabList;
        if (!StringUtils.isBlank(appTabs)) {
            appTabList = (ArrayList<AppTabAutoBean.PayloadBean.TabsBean>) new AppTabAutoBean(appTabs).getPayload().getTabs();
        } else {
            appTabList = new ArrayList<AppTabAutoBean.PayloadBean.TabsBean>();
        }

        if (appTabList != null && appTabList.size() > 0) {
            for (int i = 0; i < appTabList.size(); i++) {
                if (appTabList.get(i).isSelected()) {
                    tabIndex = i;
                }
            }
        }
        return tabIndex;
    }


    /**
     * 连点退出应用
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - lastBackTime) > 2000) {
                ToastUtils.show(IndexActivity.this,
                        getString(R.string.reclick_to_desktop));
                lastBackTime = System.currentTimeMillis();
            } else {
                ((MyApplication) getApplicationContext()).exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        boolean consumed = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && v.equals(mTabHost.getCurrentTabView())) {
            Fragment currentFragment = getCurrentFragment();
//            addFragment(currentFragment);
            if (currentFragment != null
                    && currentFragment instanceof OnTabReselectListener) {
                OnTabReselectListener listener = (OnTabReselectListener) currentFragment;
                listener.onTabReselect();
                consumed = true;
            }
        } else {
            isSystemChangeTag = false;
        }
        return consumed;
    }


    @Override
    public void onTabChanged(String tabId) {
        notSupportTitle = tabId;
        if (tabId.equals(getString(R.string.communicate))) {
            tipsView.setCanTouch(true);
        } else {
            tipsView.setCanTouch(false);
        }
        if (!isSystemChangeTag) {
            recordOpenTab(tabId);
            isSystemChangeTag = true;
        }
    }

    /**
     * 记录打开的tab页
     *
     * @param tabId
     */
    private void recordOpenTab(String tabId) {
        if (tabId.equals(getString(R.string.communicate))) {
            tabId = "communicate";
        } else if (tabId.equals(getString(R.string.work))) {
            tabId = "work";
        } else if (tabId.equals(getString(R.string.find))) {
            tabId = "find";
        } else if (tabId.equals(getString(R.string.application))) {
            tabId = "application";
        } else if (tabId.equals(getString(R.string.mine))) {
            tabId = "mine";
        } else {
            tabId = "";
        }
        PVCollectModel pvCollectModel = new PVCollectModel(tabId, tabId);
        PVCollectModelCacheUtils.saveCollectModel(IndexActivity.this, pvCollectModel);
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(
                mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MyApplication) getApplicationContext()).setIndexActvityRunning(false);
        if (contactSaveTask != null && !contactSaveTask.isCancelled() && contactSaveTask.getStatus() == AsyncTask.Status.RUNNING) {
            contactSaveTask.cancel(true);
            contactSaveTask = null;
        }
        if (handler != null) {
            handler = null;
        }
        if (newMessageTipsText != null) {
            newMessageTipsText = null;
        }
        if (newMessageTipsLayout != null) {
            newMessageTipsLayout = null;
        }
        EventBus.getDefault().unregister(this);
    }

    class ContactSaveTask extends AsyncTask<GetAllContactResult, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            getAllChannelGroup();
        }

        @Override
        protected Void doInBackground(GetAllContactResult... params) {
            GetAllContactResult getAllContactResult = params[0];
            List<Contact> allContactList = getAllContactResult
                    .getAllContactList();
            List<Contact> modifyContactLsit = getAllContactResult
                    .getModifyContactList();
            List<String> deleteContactIdList = getAllContactResult.getDeleteContactIdList();
            ContactCacheUtils.saveContactList(getApplicationContext(),
                    allContactList);
            ContactCacheUtils.saveContactList(getApplicationContext(),
                    modifyContactLsit);
            ContactCacheUtils.deleteContact(IndexActivity.this, deleteContactIdList);
            ContactCacheUtils.saveLastUpdateTime(getApplicationContext(),
                    getAllContactResult.getLastUpdateTime());
            ContactCacheUtils.saveLastUpdateunitID(IndexActivity.this, getAllContactResult.getUnitID());
            return null;
        }
    }

    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnAllContactSuccess(
                final GetAllContactResult getAllContactResult) {
            contactSaveTask = new ContactSaveTask();
            contactSaveTask.execute(getAllContactResult);
        }

        @Override
        public void returnAllContactFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            getAllChannelGroup();
            WebServiceMiddleUtils.hand(IndexActivity.this, error, errorCode);
        }

        @Override
        public void returnSearchChannelGroupSuccess(
                final GetSearchChannelGroupResult getSearchChannelGroupResult) {
            // TODO Auto-generated method stub
            WeakThread weakThread = new WeakThread(IndexActivity.this) {
                @Override
                public void run() {
                    super.run();
                    try {
                        List<ChannelGroup> channelGroupList = getSearchChannelGroupResult
                                .getSearchChannelGroupList();
                        ChannelGroupCacheUtils.clearChannelGroupList(getApplicationContext());
                        ChannelGroupCacheUtils.saveChannelGroupList(
                                getApplicationContext(), channelGroupList);
                        if (handler != null) {
                            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            weakThread.start();
        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errorCode) {
            super.returnSearchChannelGroupFail(error, errorCode);
            // 无论成功或者失败都返回成功都能进入应用
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }


        @Override
        public void returnAllRobotsSuccess(
                final GetAllRobotsResult getAllBotInfoResult) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RobotCacheUtils.clearRobotList(IndexActivity.this);
                    RobotCacheUtils.saveOrUpdateRobotList(IndexActivity.this, getAllBotInfoResult.getRobotList());
                }
            }).start();

        }

        @Override
        public void returnAllRobotsFail(String error, int errorCode) {
        }


        @Override
        public void returnAppTabAutoSuccess(GetAppTabAutoResult getAppTabAutoResult) {
            updateTabbarWithOrder(getAppTabAutoResult);
        }

        @Override
        public void returnAppTabAutoFail(String error, int errorCode) {
        }


    }

    /**
     * 根据命令升级Tabbar
     *
     * @param getAppTabAutoResult
     */
    private void updateTabbarWithOrder(GetAppTabAutoResult getAppTabAutoResult) {
        String command = getAppTabAutoResult.getCommand();
        if (command.equals("FORWARD")) {
            PreferencesByUserAndTanentUtils.putString(IndexActivity.this, "app_tabbar_version", getAppTabAutoResult.getVersion());
            PreferencesByUserAndTanentUtils.putString(IndexActivity.this, "app_tabbar_info_current", getAppTabAutoResult.getAppTabInfo());
            mTabHost.clearAllTabs(); //更新tabbar
            handleAppTabs();
        }
    }


}
