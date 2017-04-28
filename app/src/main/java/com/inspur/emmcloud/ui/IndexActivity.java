package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.AppTabAutoBean;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.GetExceptionResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.bean.ReactNativeClientIdErrorBean;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.interf.OnTabReselectListener;
import com.inspur.emmcloud.interf.OnWorkFragmentDataChanged;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.app.MyAppFragment;
import com.inspur.emmcloud.ui.chat.MessageFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.MainTabBean;
import com.inspur.emmcloud.ui.work.WorkFragment;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.RNCacheViewManager;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyFragmentTabHost;
import com.inspur.emmcloud.widget.WeakHandler;
import com.inspur.emmcloud.widget.tipsview.TipsView;
import com.inspur.reactnative.ReactNativeFlow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
    private static final int SYNC_CONTACT_SUCCESS = 1;
    private long lastBackTime;
    public MyFragmentTabHost mTabHost;
    private static TextView newMessageTipsText;
    private static RelativeLayout newMessageTipsLayout;
    private OnWorkFragmentDataChanged workFragmentListener;
    private WeakHandler handler;
    private boolean isHasCacheContact = false;
    private TipsView tipsView;
    private String reactNativeCurrentPath = "";
    private LoadingDialog loadingDlg;
    private IndexReactNativeReceiver reactNativeReceiver;
    private ReactNativeUpdateBean reactNativeUpdateBean;
    private AppAPIService appApiService;
    private String userId;
    private boolean isReactNativeClientUpdateFail = false;
    private boolean isGetTab = false;
    private String notSupportTitle = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        ((MyApplication) getApplicationContext()).addActivity(this);
        ((MyApplication) getApplicationContext()).setIndexActvityRunning(true);
        ((MyApplication) getApplicationContext()).closeAllDb();
        DbCacheUtils.initDb(getApplicationContext());
        userId = ((MyApplication) getApplication()).getUid();
        initReactNative();
        loadingDlg = new LoadingDialog(IndexActivity.this, getString(R.string.app_init));
        handMessage();
        getIsHasCacheContact();
        if (!isHasCacheContact) {
            loadingDlg.show();
        }
        getAllContact();
        getAllRobots();
        initTabView();
        if (!AppUtils.isApkDebugable(IndexActivity.this)) {
            uploadLastTimeException();
        }
        /**从服务端获取显示tab**/
        getAppTabs();
		startUploadPVCollectService();
        registerReactNativeReceiver();
        startCoreService();
    }

    /**
     * 注册刷新广播
     */
    private void registerReactNativeReceiver() {
        if (reactNativeReceiver == null) {
            reactNativeReceiver = new IndexReactNativeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.react.success");
            registerReceiver(reactNativeReceiver, filter);
        }
    }

    /**
     * 初始化ReactNative
     */
    private void initReactNative() {
        RNCacheViewManager.init(IndexActivity.this);
        reactNativeCurrentPath = MyAppConfig.getReactAppFilePath(IndexActivity.this,userId,"discover");
        if (checkClientIdNotExit()) {
            getReactNativeClientId();
        }
        if (!ReactNativeFlow.checkBundleFileIsExist(reactNativeCurrentPath + "/index.android.bundle")) {
            ReactNativeFlow.initReactNative(IndexActivity.this,userId);
        } else {
            updateReactNative();
        }
    }

    /**
     * 获取clientId
     */
    private void getReactNativeClientId() {
        AppAPIService appAPIService = new AppAPIService(IndexActivity.this);
        appAPIService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(IndexActivity.this)) {
            appAPIService.getClientId(AppUtils.getMyUUID(IndexActivity.this), AppUtils.GetChangShang());
        }
    }

    /**
     * 打开保活服务
     */
    private void startCoreService(){
        Intent intent = new Intent();
        intent.setClass(this, CoreService.class);
        startService(intent);
    }


    /**
     * 检查clientId是否存在
     *
     * @return
     */
    private boolean checkClientIdNotExit() {
        String clientId = PreferencesUtils.getString(IndexActivity.this, UriUtils.tanent + userId + "react_native_clientid", "");
        return StringUtils.isBlank(clientId);
    }

    /**
     * 更新ReactNative
     */
    private void updateReactNative() {
        appApiService = new AppAPIService(IndexActivity.this);
        appApiService.setAPIInterface(new WebService());
        String clientId = PreferencesUtils.getString(IndexActivity.this, UriUtils.tanent + userId + "react_native_clientid", "");
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactNativeCurrentPath +"/bundle.json", "UTF-8");
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        if (NetUtils.isNetworkConnected(IndexActivity.this)) {
            appApiService.getReactNativeUpdate(androidBundleBean.getVersion(), androidBundleBean.getCreationDate(), clientId);
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
        AppAPIService apiService = new AppAPIService(IndexActivity.this);
        apiService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
//            apiService.getAppTabs();
            String version = PreferencesByUserUtils.getString(IndexActivity.this,"app_tabbar_version","");;
            String clientid = PreferencesUtils.getString(IndexActivity.this, UriUtils.tanent + userId + "react_native_clientid","");
            if(!StringUtils.isBlank(clientid)){
                apiService.getAppNewTabs(version,clientid);
            }else{
                isGetTab = true;
                getReactNativeClientId();
            }

        }
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

    /**
     * 上传异常
     */
    private void uploadLastTimeException() {

        boolean isErrFileExist = FileUtils
                .isFileExist(MyAppConfig.ERROR_FILE_PATH + "errorLog.txt");
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)
                && isErrFileExist) {
            // 异常信息上传
            JSONObject jsonException = organizeException();
            AppAPIService apiService = new AppAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.uploadException(jsonException);
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
                        sendCreatChannelGroupIconBroadCaset();
                        break;
                    case SYNC_CONTACT_SUCCESS:
                        getAllChannelGroup();
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
    private void sendCreatChannelGroupIconBroadCaset() {
        // TODO Auto-generated method stub
        //当通讯录完成时需要刷新头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
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
//        MainTab[] tabs = handleAppTabs();
        handleAppTabs();
    }


    /**
     * 处理tab数组
     *
     * @return
     */
    private MainTabBean[] handleAppTabs() {
        MainTabBean[] mainTabs = null;
        String appTabs = PreferencesByUserUtils.getString(IndexActivity.this,"app_tabbar_info_current","");
        if (!StringUtils.isBlank(appTabs)) {
            String languageJson = PreferencesUtils.getString(
                    getApplicationContext(), UriUtils.tanent + "appLanguageObj");
            String environmentLanguage = "";
            if (languageJson != null) {
                Language language = new Language(languageJson);
                environmentLanguage = language.getIana();
            }
            ArrayList<AppTabAutoBean.PayloadBean.TabsBean> appTabList = (ArrayList<AppTabAutoBean.PayloadBean.TabsBean>) new AppTabAutoBean(appTabs).getPayload().getTabs();
            if (appTabList != null && appTabList.size() > 0) {
                mainTabs = new MainTabBean[appTabList.size()];
                for (int i = 0; i < appTabList.size(); i++) {
                    if (appTabList.get(i).getComponent().equals("communicate")) {
                        MainTabBean mainTabBean = new MainTabBean(0,R.string.communicate,R.drawable.selector_tab_message_btn,MessageFragment.class);
                        mainTabBean.setCommpant(appTabList.get(i).getComponent());
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    } else if (appTabList.get(i).getComponent().equals("work")) {
                        MainTabBean mainTabBean = new MainTabBean(1, R.string.work, R.drawable.selector_tab_work_btn,
                                WorkFragment.class);
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    } else if (appTabList.get(i).getComponent().equals("find")) {
                        MainTabBean mainTabBean = new MainTabBean(2, R.string.find, R.drawable.selector_tab_find_btn,
                                FindFragment.class);
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    } else if (appTabList.get(i).getComponent().equals("application")) {
                        MainTabBean mainTabBean = new MainTabBean(3, R.string.application, R.drawable.selector_tab_app_btn,
                                MyAppFragment.class);
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    } else if (appTabList.get(i).getComponent().equals("mine")) {
                        MainTabBean mainTabBean = new MainTabBean(4, R.string.mine, R.drawable.selector_tab_more_btn,
                                MoreFragment.class);
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    }else{
                        MainTabBean mainTabBean = new MainTabBean(5, R.string.unknown, R.drawable.selector_tab_unknown_btn,
                                NotSupportFragment.class);
                        mainTabs[i] = internationalMainLanguage(appTabList.get(i),environmentLanguage,mainTabBean);
                    }
                }
            } else {
                mainTabs = addNoDataTabs();
            }
        } else {
            mainTabs = addNoDataTabs();
        }
        displayMainTabs(mainTabs);
        return mainTabs;
    }

    /**
     * 根据定制展示App
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
            if(!StringUtils.isBlank(mainTab.getConfigureName())){
                tabText.setText(mainTab.getConfigureName());
            }else{
                tabText.setText(getString(mainTab.getResName()));
            }
            if(!StringUtils.isBlank(mainTab.getConfigureIcon())){
                ImageDisplayUtils imageDisplayUtils  = new ImageDisplayUtils(getApplicationContext(), R.drawable.icon_empty_icon);
                imageDisplayUtils.displayPic(tabImg,mainTab.getConfigureIcon());
            }else{
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
            mTabHost.setOnTabChangedListener(this);
        }
        mTabHost.setCurrentTab(getTabIndex());
    }


    /**
     * 当没有数据的时候返回内容
     * @return
     */
    private MainTabBean[] addNoDataTabs() {
        MainTabBean[] mainTabs = new MainTabBean[5];
        MainTabBean mainTabBeanCommunicate = new MainTabBean(0,R.string.communicate,R.drawable.selector_tab_message_btn,
                MessageFragment.class);
        mainTabBeanCommunicate.setCommpant("communicate");
        MainTabBean mainTabBeanWork = new MainTabBean(1, R.string.work, R.drawable.selector_tab_work_btn,
                WorkFragment.class);
        MainTabBean mainTabBeanFind = new MainTabBean(2, R.string.find, R.drawable.selector_tab_find_btn,
                FindFragment.class);
        MainTabBean mainTabBeanApp = new MainTabBean(3, R.string.application, R.drawable.selector_tab_app_btn,
                MyAppFragment.class);
        MainTabBean mainTabBeanMine = new MainTabBean(4, R.string.mine, R.drawable.selector_tab_more_btn,
                MoreFragment.class);
        mainTabs[0] = mainTabBeanCommunicate;
        mainTabs[1] = mainTabBeanWork;
        mainTabs[2] = mainTabBeanFind;
        mainTabs[3] = mainTabBeanApp;
        mainTabs[4] = mainTabBeanMine;
        return mainTabs;
    }

    /**
     * 暴露当前页面标题接口
     * @return
     */
    public String getNotSupportString(){
        return notSupportTitle;
    }


    /**
     * 根据语言设置tab，扩展语言从这里扩展
     * @param tabsBean
     * @param environmentLanguage
     * @return
     */
    private MainTabBean internationalMainLanguage(AppTabAutoBean.PayloadBean.TabsBean tabsBean, String environmentLanguage,MainTabBean mainTab) {
        if(environmentLanguage.toLowerCase().equals("zh-Hans".toLowerCase())){
            mainTab.setConfigureName(tabsBean.getTitle().getZhHans());
        }else if(environmentLanguage.toLowerCase().equals("zh-Hant".toLowerCase())){
            mainTab.setConfigureName(tabsBean.getTitle().getZhHant());
        }else if(environmentLanguage.toLowerCase().equals("en-US".toLowerCase())||
                environmentLanguage.toLowerCase().equals("en".toLowerCase())){
            mainTab.setConfigureName(tabsBean.getTitle().getEnUS());
        }else{
            mainTab.setConfigureName(tabsBean.getTitle().getZhHans());
        }
        return mainTab;
    }

    /**
     * 处理小红点的逻辑
     * @param tabView
     */
    private void handleTipsView(View tabView){
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
        String appTabs = PreferencesByUserUtils.getString(IndexActivity.this,"app_tabbar_info_current","");
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
    protected void onDestroy() {
        super.onDestroy();
        ((MyApplication) getApplicationContext()).setIndexActvityRunning(false);
        if (handler != null){
            handler = null;
        }
        if (newMessageTipsText != null) {
            newMessageTipsText = null;
        }
        if (newMessageTipsLayout != null) {
            newMessageTipsLayout = null;
        }
        if(reactNativeReceiver != null){
            unregisterReceiver(reactNativeReceiver);
            reactNativeReceiver = null;
        }
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
        }
        return consumed;
    }

    /**
     * 添加Fragment
     *
     * @param fragment
     */
    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.attach(fragment);
        transaction.add(fragment, "");
        transaction.commit();
    }

    @Override
    public void onTabChanged(String tabId) {
        notSupportTitle = tabId;
        String lastUpdateTime = PreferencesUtils.getString(IndexActivity.this,"react_native_lastupdatetime","");
        if (tabId.equals(getString(R.string.communicate))) {
            tipsView.setCanTouch(true);
        } else {
            tipsView.setCanTouch(false);
        }
//        if(ReactNativeFlow.moreThanHalfHour(lastUpdateTime)){
            updateReactNative();
//        }
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(
                mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);
        if (arg1 == RESULT_OK) {

            switch (arg0) {
                case 3:
                    Fragment currentFragment = getCurrentFragment();
                    if (currentFragment != null && workFragmentListener != null) {
                        workFragmentListener.onWorkFragmentDataChanged();
                    }
                    break;
                case 4:
//				MyAppFragment myAppFragment = (MyAppFragment) getCurrentFragment();
//				myAppFragment.onActivityResult(arg0, arg1, arg2);
                    break;
                default:
                    break;
            }
        }

    }

    public void setOnWorkFragmentDataChanged(OnWorkFragmentDataChanged l) {
        this.workFragmentListener = l;
    }

    /**
     * 上传异常信息前的信息组织
     */
    private JSONObject organizeException() {

        JSONObject jsonException = new JSONObject();
        JSONObject uploadJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        String mobileInfo = "OSVERSION:" + android.os.Build.VERSION.RELEASE
                + ";APPVERSION:" + AppUtils.getVersion(getApplicationContext())
                + ";MOBILEMODEL:" + android.os.Build.MODEL;
        try {
            jsonException.put("InstanceCode", "");
            jsonException.put("UserId",
                    PreferencesUtils.getString(IndexActivity.this, "userID"));
            jsonException.put("UserCode",
                    PreferencesUtils.getString(this, "userRealID"));
            jsonException.put("ErrorCode", "");
            jsonException.put("ModuleCode", "");

            if (PreferencesUtils.getString(IndexActivity.this, "crashtime") != null) {
                jsonException.put("HappenTime", Long.parseLong(PreferencesUtils
                        .getString(this, "crashtime")));
            } else {
                jsonException.put("HappenTime", 0);
            }

            jsonException.put("ClientInfo", mobileInfo);
            jsonException.put("ServerInfo", "");
            jsonException.put("ExceptionMessage", "App崩溃");
            jsonException.put(
                    "ExceptionInfo",
                    FileUtils.readFile(MyAppConfig.ERROR_FILE_PATH
                            + "errorLog.txt", "UTF-8"));
            jsonException.put("LicenseInfo", "");
            jsonException.put("LastModifyTime", "");
            jsonException.put("ClientName", "ECM_Android");

            jsonArray.put(jsonException);

            uploadJson.put("errors", jsonArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return uploadJson;
    }

    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnAllContactSuccess(
                final GetAllContactResult getAllContactResult) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    List<Contact> allContactList = getAllContactResult
                            .getAllContactList();
                    List<Contact> modifyContactLsit = getAllContactResult
                            .getModifyContactList();
//					JSONArray deleteIdArray = getAllContactResult.getDeleteIdArray();
                    List<String> deleteContactIdList = getAllContactResult.getDeleteContactIdList();
                    ContactCacheUtils.saveContactList(getApplicationContext(),
                            allContactList);
                    ContactCacheUtils.saveContactList(getApplicationContext(),
                            modifyContactLsit);
//					ContactCacheUtils.deleteContact(IndexActivity.this, deleteIdArray);
                    ContactCacheUtils.deleteContact(IndexActivity.this, deleteContactIdList);
                    ContactCacheUtils.saveLastUpdateTime(getApplicationContext(),
                            getAllContactResult.getLastUpdateTime());
                    ContactCacheUtils.saveLastUpdateunitID(IndexActivity.this,getAllContactResult.getUnitID());
                    if(handler != null){
                        handler.sendEmptyMessage(SYNC_CONTACT_SUCCESS);
                    }

                }
            }).start();

        }

        @Override
        public void returnAllContactFail(String error) {
            // TODO Auto-generated method stub
            getAllChannelGroup();
            WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }

        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            // TODO Auto-generated method stub
            super.returnSearchChannelGroupSuccess(getSearchChannelGroupResult);
            List<ChannelGroup> channelGroupList = getSearchChannelGroupResult
                    .getSearchChannelGroupList();
            ChannelGroupCacheUtils.saveChannelGroupList(
                    getApplicationContext(), channelGroupList);
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }

        @Override
        public void returnSearchChannelGroupFail(String error) {
            super.returnSearchChannelGroupFail(error);
            // 无论成功或者失败都返回成功都能进入应用
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }

        @Override
        public void returnUploadExceptionSuccess(
                GetExceptionResult getExceptionResult) {
            FileUtils.deleteFile(MyAppConfig.ERROR_FILE_PATH + "errorLog.txt");
        }

        @Override
        public void returnUploadExceptionFail(String error) {
            WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }

        @Override
        public void returnAllRobotsSuccess(
                GetAllRobotsResult getAllBotInfoResult) {
            RobotCacheUtils.saveOrUpdateRobotList(IndexActivity.this, getAllBotInfoResult.getRobotList());
        }

        @Override
        public void returnAllRobotsFail(String error) {
            //暂时去掉机器人错误
//			WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }

        @Override
        public void returnGetAppTabsSuccess(GetAppTabsResult getAppTabsResult) {
            PreferencesUtils.putString(IndexActivity.this,
                    UriUtils.tanent + userId + "appTabs", JSON.toJSONString(getAppTabsResult.getAppTabBeanList()));
            if(!StringUtils.isBlank(JSON.toJSONString(getAppTabsResult.getAppTabBeanList()))){
                mTabHost.clearAllTabs();
                handleAppTabs();
            }
        }

        @Override
        public void returnGetAppTabsFail(String error) {
            WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }


        @Override
        public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean) {
            IndexActivity.this.reactNativeUpdateBean = reactNativeUpdateBean;
            updateReactNativeWithOrder();
            PreferencesUtils.putString(IndexActivity.this,"react_native_lastupdatetime",System.currentTimeMillis()+"");
        }

        @Override
        public void returnReactNativeUpdateFail(ReactNativeClientIdErrorBean reactNativeClientIdErrorBean) {
            isReactNativeClientUpdateFail = true;
            if(!checkClientIdNotExit()){
                getReactNativeClientId();
            }
        }

        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            super.returnGetClientIdResultSuccess(getClientIdRsult);
            isReactNativeClientUpdateFail = false;
            PreferencesUtils.putString(IndexActivity.this, UriUtils.tanent + userId + "react_native_clientid", getClientIdRsult.getClientId());
            if(isReactNativeClientUpdateFail){
                updateReactNative();
            }
            if(isGetTab){
                getAppTabs();
                isGetTab = false;
            }
        }

        @Override
        public void returnGetClientIdResultFail(String error) {
            super.returnGetClientIdResultFail(error);
        }

        @Override
        public void returnAppTabAutoSuccess(GetAppTabAutoResult getAppTabAutoResult) {
            updateTabbarWithOrder(getAppTabAutoResult);
        }

        @Override
        public void returnAppTabAutoFail(String error) {
            LogUtils.YfcDebug("检查是否有Tab新接口");
//            WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }
    }

    /**
     * 根据命令升级Tabbar
     * @param getAppTabAutoResult
     */
    private void updateTabbarWithOrder(GetAppTabAutoResult getAppTabAutoResult) {
        String command = getAppTabAutoResult.getCommand();
        PreferencesByUserUtils.putString(IndexActivity.this,"app_tabbar_version",getAppTabAutoResult.getVersion());
        if(command.equals("FORWARD")){
            PreferencesByUserUtils.putString(IndexActivity.this,"app_tabbar_info_current",getAppTabAutoResult.getAppTabInfo());
            mTabHost.clearAllTabs();
            handleAppTabs();
        }else if(command.equals("STANDBY")){
            LogUtils.YfcDebug("收到保持现状指令");
        }else{
            LogUtils.YfcDebug("收到不支持的指令");
        }
//        mTabHost.clearAllTabs();
//        handleAppTabs();
    }

    /**
     * 按照更新指令更新ReactNative
     */
    private void updateReactNativeWithOrder() {
        int state = ReactNativeFlow.checkReactNativeOperation(reactNativeUpdateBean.getCommand());
            String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(IndexActivity.this,userId);
            if (state == ReactNativeFlow.REACT_NATIVE_RESET) {
                //删除current和temp目录，重新解压assets下的zip
                resetReactNative();
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_ROLLBACK) {
            //拷贝temp下的current到app内部current目录下
            File file = new File(reactNatviveTempPath);
            if(file.exists()){
                ReactNativeFlow.moveFolder(reactNatviveTempPath, reactNativeCurrentPath);
                LogUtils.YfcDebug("回滚时temp："+reactNatviveTempPath);
                LogUtils.YfcDebug("回滚时current："+reactNativeCurrentPath);
                FileUtils.deleteFile(reactNatviveTempPath);
            }else {
                ReactNativeFlow.initReactNative(IndexActivity.this,userId);
            }
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_FORWORD) {
                LogUtils.YfcDebug("Forword");
            //下载zip包并检查是否完整，完整则解压，不完整则重新下载,完整则把current移动到temp下，把新包解压到current
            ReactNativeFlow.downLoadZipFile(IndexActivity.this, reactNativeUpdateBean, userId);
        } else if (state == ReactNativeFlow.REACT_NATIVE_UNKNOWN) {
            //发生了未知错误，下载state为0
            //同Reset的情况，删除current和temp目录，重新解压assets下的zip
            resetReactNative();
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_NO_UPDATE) {
            //没有更新什么也不做
                LogUtils.YfcDebug("Standy");
        }
        if(FindFragment.hasUpdated){
            RNCacheViewManager.init(IndexActivity.this);
        }
    }

    /**
     * 重新整理目录恢复状态
     */
    private void resetReactNative() {
        String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(IndexActivity.this,userId);
        FileUtils.deleteFile(reactNatviveTempPath);
        FileUtils.deleteFile(reactNativeCurrentPath);
        ReactNativeFlow.initReactNative(IndexActivity.this,userId);
    }

    /**
     * 更新ReactNative广播接收类
     */
    class IndexReactNativeReceiver extends BroadcastReceiver {
        private static final String ACTION_REFRESH = "com.inspur.react.success";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_REFRESH)) {
                String clientId = PreferencesUtils.getString(IndexActivity.this, UriUtils.tanent + userId +
                        "react_native_clientid", "");
                appApiService.sendBackReactNativeUpdateLog(reactNativeUpdateBean.getCommand(),
                        reactNativeUpdateBean.getBundle().getId().getVersion(), clientId);
            }
        }
    }

}
