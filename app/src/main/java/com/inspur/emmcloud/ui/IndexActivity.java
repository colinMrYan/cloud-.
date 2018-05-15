package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.inspur.emmcloud.util.common.LogUtils;
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
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.WeakHandler;
import com.inspur.emmcloud.widget.WeakThread;

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
    private ContactUserCacheTask contactUserCacheTask;
    private long a = System.currentTimeMillis();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().setIndexActvityRunning(false);
        if (contactUserCacheTask != null && !contactUserCacheTask.isCancelled() && contactUserCacheTask.getStatus() == AsyncTask.Status.RUNNING) {
            contactUserCacheTask.cancel(true);
            contactUserCacheTask = null;
        }
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    class ContactUserCacheTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            getAllChannelGroup();
        }

        @Override
        protected Void doInBackground(byte[]... params) {
            long b = System.currentTimeMillis();
            LogUtils.jasonDebug("get----time000000000="+(b-a)/1000.0);
            try {
                List<ContactProtoBuf.user> userList = ContactProtoBuf.users.parseFrom(params[0]).getUsersList();
                long c = System.currentTimeMillis();
                List<ContactUser> contactUserList = ContactUser.protoBufUserList2ContactUserList(userList);
                LogUtils.jasonDebug("xuliehua----time000000000="+(c-b)/1000.0);
                long d = System.currentTimeMillis();
                LogUtils.jasonDebug("contactUserList="+contactUserList.size());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                LogUtils.jasonDebug("存储时间----time000000000="+(System.currentTimeMillis()-d)/1000.0);
                LogUtils.jasonDebug("总时间----time000000000="+(System.currentTimeMillis()-a)/1000.0);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    class ContactOrgCacheTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                List<ContactProtoBuf.org> orgList = ContactProtoBuf.orgs.parseFrom(params[0]).getOrgsList();



                List<ContactUser> contactUserList = ContactUser.protoBufUserList2ContactUserList(userList);
                LogUtils.jasonDebug("contactUserList="+contactUserList.size());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
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
            apiService.getContactOrgList();
            apiService.getContactUserList();
        } else if (isHasCacheContact && handler != null) {
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
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
        public void returnContactUserListSuccess(byte[] bytes) {
            contactUserCacheTask = new ContactUserCacheTask();
            contactUserCacheTask.execute(bytes);
            long b = System.currentTimeMillis();
            LogUtils.jasonDebug("get----time000000000="+(b-a)/1000.0);
            try {
                List<ContactProtoBuf.user> userList = ContactProtoBuf.users.parseFrom(bytes).getUsersList();
                long c = System.currentTimeMillis();
                List<ContactUser> contactUserList = ContactUser.protoBufUserList2ContactUserList(userList);
                LogUtils.jasonDebug("xuliehua----time000000000="+(c-b)/1000.0);
                long d = System.currentTimeMillis();
                LogUtils.jasonDebug("contactUserList="+contactUserList.size());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                LogUtils.jasonDebug("存储时间----time000000000="+(System.currentTimeMillis()-d)/1000.0);
                LogUtils.jasonDebug("总时间----time000000000="+(System.currentTimeMillis()-a)/1000.0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void returnContactUserListFail(String error, int errorCode) {
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
            if (handler != null) {
                handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
            }
        }


        @Override
        public void returnAllRobotsSuccess(
                final GetAllRobotsResult getAllBotInfoResult) {
            RobotCacheUtils.clearRobotList(IndexActivity.this);
            RobotCacheUtils.saveOrUpdateRobotList(IndexActivity.this, getAllBotInfoResult.getRobotList());
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
