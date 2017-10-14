package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.inspur.emmcloud.bean.AppTabAutoBean;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.callback.CommonCallBack;
import com.inspur.emmcloud.interf.OnTabReselectListener;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.app.MyAppFragment;
import com.inspur.emmcloud.ui.chat.MessageFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.mine.setting.LanguageChangeActivity;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.MainTabBean;
import com.inspur.emmcloud.ui.work.WorkFragment;
import com.inspur.emmcloud.util.AppConfigUtils;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ClientIDUtils;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ReactNativeUtils;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.SplashPageUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
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
        OnTabChangeListener, OnTouchListener, MyAppFragment.AppLanguageState {
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
        StateBarColor.changeStateBarColor(this);
        setContentView(R.layout.activity_index);
        ((MyApplication) getApplicationContext()).setIndexActvityRunning(true);
        ((MyApplication) getApplicationContext()).closeAllDb();
        DbCacheUtils.initDb(getApplicationContext());
        ((MyApplication) getApplicationContext()).closeWebSocket();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        ((MyApplication) getApplicationContext()).startPush();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        appApiService = new AppAPIService(IndexActivity.this);
        appApiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(IndexActivity.this, getString(R.string.app_init));
        handMessage();
        getIsHasCacheContact();
        if (!isHasCacheContact) {
            loadingDlg.show();
        }
        getAllContact();
        getAllRobots();
        initTabView();

        /**从服务端获取显示tab**/
        getAppTabs();
        new SplashPageUtils(IndexActivity.this).update();//更新闪屏页面
        new ReactNativeUtils(IndexActivity.this).init(); //初始化和更新react
        new AppConfigUtils(IndexActivity.this).getAppConfig();
        startUploadPVCollectService();
        startCoreService();
        setPreloadWebApp();
        EventBus.getDefault().register(this);
    }

    /**
     * 打开保活服务
     */
    private void startCoreService() {
        Intent intent = new Intent();
        intent.setClass(this, CoreService.class);
        startService(intent);
    }

    /**
     * 为了使打开报销web应用更快，进行预加载
     */
    private void setPreloadWebApp() {
        if (UriUtils.tanent.equals("inspur_esg")) {
            webView = (WebView) findViewById(R.id.preload_webview);
            webView.getSettings().setJavaScriptEnabled(true);
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
     * 获取应用显示tab
     */
    private void getAppTabs() {
        new ClientIDUtils(IndexActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
                    AppAPIService apiService = new AppAPIService(IndexActivity.this);
                    apiService.setAPIInterface(new WebService());
                    String uid = ((MyApplication) getApplication()).getUid();
                    String version = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, "app_tabbar_version", "");
                    String clientid = PreferencesUtils.getString(IndexActivity.this, UriUtils.tanent + uid + "react_native_clientid", "");
                    apiService.getAppNewTabs(version, clientid);

                }
            }
        }).getClientID();


    }

    /**
     * 获取所有的Robot
     */
    private void getAllRobots() {
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
        sendBroadcast(intent);

    }

    /**
     * 判断通讯录是否已经缓存过
     */
    private void getIsHasCacheContact() {
        // TODO Auto-generated method stub
        String contactLastUpdateTime = ContactCacheUtils
                .getLastUpdateTime(IndexActivity.this);
        isHasCacheContact = StringUtils.isBlank(contactLastUpdateTime) ? false
                : true;
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
    private void getAllContact() {
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
        handleAppTabs();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTabBarLanguage(Language language) {
        if (language != null) {
            handleAppTabs();
        }
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
                            break;
                        case "find":
                            mainTabBean = new MainTabBean(i, R.string.find, R.drawable.selector_tab_find_btn,
                                    FindFragment.class);
                            break;
                        case "application":
                            mainTabBean = new MainTabBean(i, R.string.application, R.drawable.selector_tab_app_btn,
                                    MyAppFragment.class);
                            break;
                        case "mine":
                            mainTabBean = new MainTabBean(i, R.string.mine, R.drawable.selector_tab_more_btn,
                                    MoreFragment.class);
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
        if (mainTabs == null){
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
                ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_empty_icon);
                imageDisplayUtils.displayImage(tabImg, mainTab.getConfigureIcon());
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

    //修改语言时状态接口
    @Override
    public boolean getAppLanguageState() {
        if (getIntent().hasExtra(LanguageChangeActivity.LANGUAGE_CHANGE)) {
            return getIntent().getBooleanExtra(LanguageChangeActivity.LANGUAGE_CHANGE, false);
        }
        return false;
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
