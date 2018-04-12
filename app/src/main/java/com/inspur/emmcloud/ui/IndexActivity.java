package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
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
import com.inspur.emmcloud.bean.system.AppTabAutoBean;
import com.inspur.emmcloud.bean.system.AppTabDataBean;
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
import com.inspur.emmcloud.ui.chat.MessageFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.TabBean;
import com.inspur.emmcloud.ui.work.WorkFragment;
import com.inspur.emmcloud.util.common.LogUtils;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        MyApplication.getInstance().setIndexActvityRunning(true);
        MyApplication.getInstance().closeAllDb();
        DbCacheUtils.initDb(MyApplication.getInstance());
        MyApplication.getInstance().closeWebSocket();
        MyApplication.getInstance().clearUserPhotoMap();
        MyApplication.getInstance().startPush();
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
     * 获取我的应用推荐小部件数据,如果到了更新时间才请求
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
        if (!AppUtils.isServiceWork(getApplicationContext(), PVCollectService.class.getName())) {
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

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new WeakHandler(IndexActivity.this) {

            @Override
            protected void handleMessage(Object o, Message msg) {
                switch (msg.what) {
                    case SYNC_ALL_BASE_DATA_SUCCESS:
                        LoadingDialog.dimissDlg(loadingDlg);
                        MyApplication.getInstance()
                                .setIsContactReady(true);
                        notifySyncAllBaseDataSuccess();
                        MyApplication.getInstance().startWebSocket();// 启动webSocket推送
                        deleteIllegalUser();
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
     * 通讯录完成时发送广播
     */
    private void notifySyncAllBaseDataSuccess() {
        // TODO Auto-generated method stub
        //当通讯录完成时需要刷新头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sync_all_base_data_success");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    /**
     * 清除数据库中非法用户
     */
    private void deleteIllegalUser() {
        try {
            boolean isHasDeletleIllegalUser = PreferencesByUserAndTanentUtils.getBoolean(getApplicationContext(), Constant.PREF_DELETE_ILLEGAL_USER, false);
            if (!isHasDeletleIllegalUser) {
                int illegalUserCount = ContactCacheUtils.deleteIllegalUser(getApplicationContext());
                if (illegalUserCount != -1) {
                    PreferencesByUserAndTanentUtils.putBoolean(getApplicationContext(), Constant.PREF_DELETE_ILLEGAL_USER, true);
                }
                if (illegalUserCount != 0) {
                    AppExceptionCacheUtils.saveAppException(getApplicationContext(), 5, "", "通讯录删除无效用户个数" + illegalUserCount, 0);
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
            MyApplication.getInstance().setIsContactReady(false);
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
            MyApplication.getInstance().setIsContactReady(false);
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
            String shoWNum = (num > 99)?"99+":num + "";
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
        setAndShowAppTabs();
    }

    /**
     * 处理tab数组
     *
     * @return
     */
    private void setAndShowAppTabs() {
        TabBean[] tabBeans = null;
        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, "app_tabbar_info_current", "");
        if (!StringUtils.isBlank(appTabs)) {
            Configuration config = getResources().getConfiguration();
            String environmentLanguage = config.locale.getLanguage();
            AppTabAutoBean appTabAutoBean = new AppTabAutoBean(appTabs);
            //发送到MessageFragment
            EventBus.getDefault().post(appTabAutoBean);
            ArrayList<AppTabDataBean> appTabList = (ArrayList<AppTabDataBean>) appTabAutoBean.getPayload().getTabs();
            if (appTabList.size() > 0) {
                tabBeans = new TabBean[appTabList.size()];
                for (int i = 0; i < appTabList.size(); i++) {
                    TabBean tabBean = null;
                    switch (appTabList.get(i).getTabId()) {
                        case "communicate":
                            tabBean = new TabBean(getString(R.string.communicate), R.drawable.selector_tab_message_btn + "", MessageFragment.class);
                            break;
                        case "work":
                            tabBean = new TabBean(getString(R.string.work), R.drawable.selector_tab_work_btn + "",
                                    WorkFragment.class);
                            break;
                        case "find":
                            tabBean = new TabBean(getString(R.string.find), R.drawable.selector_tab_find_btn + "",
                                    FindFragment.class);
                            break;
                        case "application":
                            tabBean = new TabBean(getString(R.string.application), R.drawable.selector_tab_app_btn + "",
                                    MyAppFragment.class);
                            break;
                        case "mine":
                            tabBean = new TabBean(getString(R.string.mine), R.drawable.selector_tab_more_btn + "",
                                    MoreFragment.class);
                            break;
                        default:
                            tabBean = new TabBean(getString(R.string.unknown), R.drawable.selector_tab_unknown_btn + "",
                                    NotSupportFragment.class);
                            break;
                    }
                    tabBean.setTabId(appTabList.get(i).getTabId());
                    tabBeans[i] = internationalMainLanguage(appTabList.get(i), environmentLanguage, tabBean);
                }
            }
        }
        if (tabBeans == null) {
            tabBeans = addDefaultTabs();
        }
        showTabs(tabBeans);
    }

    /**
     * 根据定制展示App
     *
     * @param tabs
     */
    private void showTabs(TabBean[] tabs) {
        final int size = tabs.length;
        int communicateIndex = -1;
        for (int i = 0; i < size; i++) {
            TabBean tabBean = tabs[i];
            String tabId = tabBean.getTabId();
            TabHost.TabSpec tab = mTabHost.newTabSpec(tabId);
            View tabView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.tab_item_view, null);
            ImageView tabImg = (ImageView) tabView.findViewById(R.id.imageview);
            TextView tabText = (TextView) tabView.findViewById(R.id.textview);
            if (tabId.equals("communicate")) {
                handleTipsView(tabView);
                communicateIndex = i;
            }
            tabText.setText(tabBean.getTabName());
            if (tabBean.getTabIcon().startsWith("http")) {
                ImageDisplayUtils.getInstance().displayImage(tabImg, tabBean.getTabIcon(), R.drawable.ic_app_default);
            } else {
                tabImg.setImageResource(Integer.parseInt(tabBean.getTabIcon()));
            }
            tab.setIndicator(tabView);
            tab.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(IndexActivity.this);
                }
            });
            mTabHost.addTab(tab, tabBean.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
            mTabHost.getTabWidget().getChildAt(i).setTag(tabBean.getTabId());
        }
        mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab((communicateIndex != -1 && isCommunicationRunning == false) ? communicateIndex : getTabIndex());
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
    private TabBean[] addDefaultTabs() {
        //无数据改为显示两个tab，数组变为2
        TabBean[] tabBeans = new TabBean[2];
        TabBean tabBeanApp = new TabBean(getString(R.string.application), R.drawable.selector_tab_app_btn + "",
                MyAppFragment.class);
        tabBeanApp.setTabId("application");
        TabBean tabBeanMine = new TabBean(getString(R.string.mine), R.drawable.selector_tab_more_btn + "",
                MoreFragment.class);
        tabBeanMine.setTabId("mine");
        //无数据改为显示两个tab
        tabBeans[0] = tabBeanApp;
        tabBeans[1] = tabBeanMine;
        return tabBeans;
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
    private TabBean internationalMainLanguage(AppTabDataBean tabsBean, String environmentLanguage, TabBean tabBean) {
        switch (environmentLanguage.toLowerCase()) {
            case "zh-hant":
                tabBean.setTabName(tabsBean.getTitle().getZhHant());
                break;
            case "en":
            case "en-us":
                tabBean.setTabName(tabsBean.getTitle().getEnUS());
                break;
            default:
                tabBean.setTabName(tabsBean.getTitle().getZhHans());
                break;
        }
        return tabBean;
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
                LocalBroadcastManager.getInstance(IndexActivity.this).sendBroadcast(intent);
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
        if (!StringUtils.isBlank(appTabs)) {
            ArrayList<AppTabDataBean> appTabList = (ArrayList<AppTabDataBean>) new AppTabAutoBean(appTabs).getPayload().getTabs();
            if (appTabList.size() > 0) {
                for (int i = 0; i < appTabList.size(); i++) {
                    if (appTabList.get(i).isSelected()) {
                        tabIndex = i;
                        break;
                    }
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
                MyApplication.getInstance().exit();
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
        tipsView.setCanTouch(tabId.equals("communicate"));
        if (!isSystemChangeTag) {
            //记录打开的tab页
            PVCollectModel pvCollectModel = new PVCollectModel(tabId, tabId);
            PVCollectModelCacheUtils.saveCollectModel(IndexActivity.this, pvCollectModel);
            isSystemChangeTag = true;
        }
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(
                mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.jasonDebug("setIndexActvityRunning---------before");
        MyApplication.getInstance().setIndexActvityRunning(false);
        LogUtils.jasonDebug("setIndexActvityRunning---------after");
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

    /**
     * 获取所有的Robot
     */
    private void getAllRobotInfo() {
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getAllRobotInfo();
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
            setAndShowAppTabs();
        }
    }


}
