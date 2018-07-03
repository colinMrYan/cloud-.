package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactProtoBuf;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.GetAppTabAutoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.service.BackgroundService;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.service.LocationService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppConfigUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.MyAppWidgetUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ReactNativeUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.WeakHandler;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * 主页面
 *
 * @author Administrator
 */
public class IndexActivity extends IndexBaseActivity {
    private static final int SYNC_ALL_BASE_DATA_SUCCESS = 0;
    private static final int RELOAD_WEB = 3;
    @ViewInject(R.id.preload_webview)
    private WebView webView;
    private WeakHandler handler;
    private boolean isHasCacheContact = false;
    private LoadingDialog loadingDlg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppEnvironment();
        initView();
        getInitData();
        startService();
        EventBus.getDefault().register(this);
    }

    /**
     * 初始化app的运行环境
     */
    private void initAppEnvironment() {
        MyApplication.getInstance().setIndexActvityRunning(true);
        MyApplication.getInstance().restartAllDb();
        MyApplication.getInstance().clearUserPhotoMap();
        MyApplication.getInstance().startPush();
    }

    private void initView() {
        loadingDlg = new LoadingDialog(IndexActivity.this, getString(R.string.app_init));
        handMessage();
        setPreloadWebApp();
    }

    /**
     * 初始化
     */
    private void getInitData() {
        isHasCacheContact = (ContactUserCacheUtils.getLastQueryTime() != 0);
        if (!isHasCacheContact) {
            loadingDlg.show();
        }
        getAllRobotInfo();
        getContactUser();
        getAppTabInfo();  //从服务端获取显示tab
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
        startBackgroudService();
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
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        if (isAppSetRunBackground) {
            Intent intent = new Intent();
            intent.setClass(this, BackgroundService.class);
            startService(intent);
        }
    }

    /**
     * 打开位置收集服务
     */
    private void startLocationService() {
        new AppConfigUtils(IndexActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                Intent intent = new Intent();
                intent.setClass(IndexActivity.this, LocationService.class);
                startService(intent);
            }
        }).getAppConfig(); //获取整个应用的配置信息,获取完成后启动位置服务
    }

    /**
     * 为了使打开报销web应用更快，进行预加载
     */
    private void setPreloadWebApp() {
        if (MyApplication.getInstance().getTanent().equals("inspur_esg")) {
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
                    new SplashPageUtils(IndexActivity.this).update();//更新闪屏页面
                    new ReactNativeUtils(IndexActivity.this).init(); //更新react
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
                        MyApplication.getInstance().startWebSocket(true);// 启动webSocket推送
                        getContactOrg();
                        getAllChannelGroup();
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

    @Override
    protected void onDestroy() {
        MyApplication.getInstance().setIndexActvityRunning(false);
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    class CacheContactUserThread extends Thread {
        private byte[] result;

        public CacheContactUserThread(byte[] result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.users users = ContactProtoBuf.users.parseFrom(result);
                List<ContactProtoBuf.user> userList = users.getUsersList();
                List<ContactUser> contactUserList = ContactUser.protoBufUserList2ContactUserList(userList,users.getLastQueryTime());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                ContactUserCacheUtils.setLastQueryTime(users.getLastQueryTime());
                if (handler != null) {
                    handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CacheContactOrgThread extends Thread {
        private byte[] result;

        public CacheContactOrgThread(byte[] result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.orgs orgs = ContactProtoBuf.orgs.parseFrom(result);
                List<ContactProtoBuf.org> orgList = orgs.getOrgsList();
                List<ContactOrg> contactOrgList = ContactOrg.protoBufOrgList2ContactOrgList(orgList);
                ContactOrgCacheUtils.saveContactOrgList(contactOrgList);
                ContactOrgCacheUtils.setLastQueryTime(orgs.getLastQueryTime());
                ContactOrgCacheUtils.setContactOrgRootId(orgs.getRootID());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class CacheChannelGroupThread extends Thread {
        private GetSearchChannelGroupResult getSearchChannelGroupResult;

        public CacheChannelGroupThread(GetSearchChannelGroupResult getSearchChannelGroupResult) {
            this.getSearchChannelGroupResult = getSearchChannelGroupResult;
        }

        @Override
        public void run() {
            try {
                List<ChannelGroup> channelGroupList = getSearchChannelGroupResult
                        .getSearchChannelGroupList();
                ChannelGroupCacheUtils.clearChannelGroupList(getApplicationContext());
                ChannelGroupCacheUtils.saveChannelGroupList(
                        getApplicationContext(), channelGroupList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class CacheRobotInfoThread extends Thread {
        private GetAllRobotsResult getAllBotInfoResultl;

        public CacheRobotInfoThread(GetAllRobotsResult getAllBotInfoResult) {
            this.getAllBotInfoResultl = getAllBotInfoResult;
        }

        @Override
        public void run() {
            RobotCacheUtils.clearRobotList(MyApplication.getInstance());
            RobotCacheUtils.saveOrUpdateRobotList(MyApplication.getInstance(), getAllBotInfoResultl.getRobotList());
        }
    }

    /**
     * 获取所有的群组信息
     */
    private void getAllChannelGroup() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ChatAPIService apiService = new ChatAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getAllGroupChannelList();
        }
    }

    /**
     * 获取通讯录人员信息
     */
    private void getContactUser() {
        // TODO Auto-generated method stub
        ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
        apiService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            MyApplication.getInstance().setIsContactReady(false);
            long contactUserLastQuetyTime = ContactUserCacheUtils.getLastQueryTime();
            apiService.getContactUserList(contactUserLastQuetyTime);
        } else if (handler != null) {
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }
    }

    /**
     * 获取通讯录人员信息
     */
    private void getContactOrg() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            long contactOrgLastQuetyTime = ContactOrgCacheUtils.getLastQueryTime();
            apiService.getContactOrgList(contactOrgLastQuetyTime);
        }
    }


    /**
     * 获取所有的Robot
     */
    private void getAllRobotInfo() {
        if (!StringUtils.isBlank(MyApplication.getInstance().getClusterChatVersion())&&NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getAllRobotInfo();
        }
    }

    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnContactUserListSuccess(byte[] bytes) {
            new CacheContactUserThread(bytes).start();
        }

        @Override
        public void returnContactUserListFail(String error, int errorCode) {
            if (handler != null) {
                handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
            }
            WebServiceMiddleUtils.hand(IndexActivity.this, error, errorCode);
        }

        @Override
        public void returnContactOrgListSuccess(byte[] bytes) {
            new CacheContactOrgThread(bytes).start();
        }

        @Override
        public void returnContactOrgListFail(String error, int errorCode) {

        }

        @Override
        public void returnSearchChannelGroupSuccess(
                final GetSearchChannelGroupResult getSearchChannelGroupResult) {
            new CacheChannelGroupThread(getSearchChannelGroupResult).start();
        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errorCode) {
        }


        @Override
        public void returnAllRobotsSuccess(
                final GetAllRobotsResult getAllBotInfoResult) {
            new CacheRobotInfoThread(getAllBotInfoResult).start();
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

}
