package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.util.ApiRequestRecordUploadUtils;
import com.inspur.emmcloud.basemodule.util.AppPVManager;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactProtoBuf;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.bean.contact.GetMultiContactResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.Update2NewVersionUtils;
import com.inspur.emmcloud.componentservice.app.CommonCallBack;
import com.inspur.emmcloud.componentservice.application.ApplicationService;
import com.inspur.emmcloud.componentservice.application.maintab.GetAppMainTabResult;
import com.inspur.emmcloud.componentservice.application.navibar.NaviBarModel;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.componentservice.schedule.ScheduleService;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.service.LocationService;
import com.inspur.emmcloud.util.privates.AppConfigUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.util.privates.ReactNativeUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 主页面
 *
 * @author Administrator
 */
@Route(path = Constant.AROUTER_CLASS_APP_INDEX)
public class IndexActivity extends IndexBaseActivity {

    private static final int SYNC_ALL_BASE_DATA_SUCCESS = 0;
    private static final int RELOAD_WEB = 3;
    private Handler handler;
    private boolean isHasCacheContact = false;
    private LoadingDialog loadingDlg;
    //更新到新版本（云+2.0）提醒间隔时间
    private static final int notUpdateInterval = 86400000;
    private SpannableString agreement;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        initAppEnvironment();
        initView();
        getInitData();
        startService();
        uploadApiRequestRecord();
        //目前隐私协议改为本地判断
//        getIsAgreed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Update2NewVersionUtils.getInstance(this).checkNeedUpdate2NewVersion();
    }

    private void getNaviTabData(String naviTabSaveConfigVersion) {
        if (NetUtils.isNetworkConnected(this, false)) {
            AppAPIService appAPIService = new AppAPIService(this);
            appAPIService.setAPIInterface(new WebService());
            appAPIService.getAppNaviTabs(naviTabSaveConfigVersion);
        }
    }

    /**
     * 获取是否同意了隐私协议和服务政策
     */
    private void getIsAgreed(){
        AppAPIService appAPIService = new AppAPIService(this);
        appAPIService.setAPIInterface(new WebService());
        appAPIService.getIsAgreeAgreement();
    }

    /**
     * 初始化app的运行环境
     */
    private void initAppEnvironment() {
        MyApplication.getInstance().setIsContactReady(false);
        MyApplication.getInstance().setIndexActvityRunning(true);
        MyApplication.getInstance().restartAllDb();
        MyApplication.getInstance().clearUserPhotoMap();
        AppUtils.judgeAndStartPush(IndexActivity.this);
        initScheduleCalendar();
        MessageSendManager.getInstance().initMessageStatus();
    }

    protected void initScheduleCalendar() {

        Router router = Router.getInstance();
        if (router.getService(ScheduleService.class) != null) {
            ScheduleService service = router.getService(ScheduleService.class);
            service.initScheduleCalendar();
        }
//        List<ScheduleCalendar> scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
//        if (scheduleCalendarList.size() == 0) {
//            scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.BLUE, "", "", "", AccountType.APP_SCHEDULE));
//            scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.ORANGE, "", "", "", AccountType.APP_MEETING));
//            String account = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
//            String password = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
//            if (!StringUtils.isBlank(account) && !StringUtils.isBlank(password)) {
//                scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.GREEN, account, account, password, AccountType.EXCHANGE));
//            }
//            ScheduleCalendarCacheUtils.saveScheduleCalendarList(BaseApplication.getInstance(), scheduleCalendarList);
//        }
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
        PushManagerUtils.getInstance().registerPushId2Emm();
        ClientConfigUpdateUtils.getInstance().getAllConfigUpdate();
        getAppRole();

        getAllRobotInfo();
        getAllChannelGroup();
        updateReactNative();  //从服务端获取显示tab
        getMyAppRecommendWidgets();
    }

    /**
     * 获取app权限
     */
    private void getAppRole() {
        if(NetUtils.isNetworkConnected(this)){
            AppAPIService appAPIService = new AppAPIService(this);
            appAPIService.setAPIInterface(new WebService());
            appAPIService.getAppRole();
        }
    }

    /**
     * 获取我的应用推荐小部件数据,如果到了更新时间才请求
     */
    private void getMyAppRecommendWidgets() {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            service.getMyAppRecommendWidgets();
        }

    }

    /**
     * 启动服务
     */
    private void startService() {
        //app应用行为分析上传
        new AppPVManager().uploadPV();
//        startCoreService();
        startLocationService();
    }

//    /**
//     * 打开保活服务
//     */
//    private void startCoreService() {
//        if (AppUtils.getSDKVersionNumber() < 26) {
//            Intent intent = new Intent();
//            intent.setClass(this, CoreService.class);
//            startService(intent);
//        }
//    }


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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webView.getSettings().setSafeBrowsingEnabled(false);
            }
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
     * 获取RN应用显示tab
     */
    private void updateReactNative() {
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
                @Override
                public void getClientIdSuccess(String clientId) {
                    if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
                        new ReactNativeUtils(IndexActivity.this).init(); //更新react
                    }
                }

                @Override
                public void getClientIdFail() {
                }
            }).getClientId();
        }
    }

    private void getTabInfo() {
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {

            new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
                @Override
                public void getClientIdSuccess(String clientId) {
                    AppAPIService apiService = new AppAPIService(IndexActivity.this);
                    apiService.setAPIInterface(new WebService());
                    String mainTabSaveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_MAINTAB);
                    String version = PreferencesByUserAndTanentUtils.getString(IndexActivity.this, Constant.PREF_APP_TAB_BAR_VERSION, "");
                    apiService.getAppNewTabs(version, clientId, mainTabSaveConfigVersion);
                }

                @Override
                public void getClientIdFail() {
                }
            }).getClientId();
        }
    }

    /**
     * 保存是否同意隐私政策和服务协议
     */
    private void saveIsAgreed() {
        AppAPIService appAPIService = new AppAPIService(this);
        appAPIService.setAPIInterface(new WebService());
        //1代表已经同意隐私政策和服务协议0未同意
        appAPIService.saveAgreeState("1");
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case SYNC_ALL_BASE_DATA_SUCCESS:
                        LoadingDialog.dimissDlg(loadingDlg);
                        if (!MyApplication.getInstance().getIsContactReady()) {
                            MyApplication.getInstance()
                                    .setIsContactReady(true);
                            notifySyncAllBaseDataSuccess();
                            getContactOrg();
                            getMultipleContactOrg();
                        }
                        WebSocketPush.getInstance().startWebSocket();// 启动webSocket推送
                        batteryWhiteListRemind(IndexActivity.this);
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
     * 上传http响应事件日志
     */
    private void uploadApiRequestRecord() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                new ApiRequestRecordUploadUtils().start();
            }
        });
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleMessage(SimpleEventMessage simpleEventMessage) {
        //为Message添加showContent字段以便于搜索
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_MESSAGE_ADD_SHOW_CONTENT)) {
            Observable.create(new ObservableOnSubscribe<Void>() {
                @Override
                public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                    List<String> messageTypeList = new ArrayList<>();
                    messageTypeList.add(Message.MESSAGE_TYPE_TEXT_PLAIN);
                    messageTypeList.add(Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN);
                    messageTypeList.add(Message.MESSAGE_TYPE_TEXT_MARKDOWN);
                    List<Message> messageList = MessageCacheUtil.getMessageListByType(BaseApplication.getInstance(), messageTypeList);
                    for (Message message : messageList) {
                        message.setMessageShowContent();
                    }
                    MessageCacheUtil.saveMessageList(BaseApplication.getInstance(), messageList);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();

        } else if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_CONVERSATION_ADD_SHOW_CONTENT)) {
            //为Conversation添加showContent字段以便于搜索
            Observable.create(new ObservableOnSubscribe<Void>() {
                @Override
                public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                    List<Conversation> conversationList = ConversationCacheUtils.getConversationList(BaseApplication.getInstance());
                    for (Conversation conversation : conversationList) {
                        conversation.setShowName(CommunicationUtils.getConversationTitle(conversation));
                    }
                    ConversationCacheUtils.saveConversationList(BaseApplication.getInstance(), conversationList);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
        }
    }


    /**
     * 客户端统一配置版本更新
     *
     * @param getAllConfigVersionResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClientConfigVersionUpdate(final GetAllConfigVersionResult getAllConfigVersionResult) {
        boolean isRouterUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_ROUTER, getAllConfigVersionResult);
        boolean isContactUserUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER, getAllConfigVersionResult);
        boolean isContactOrgUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG, getAllConfigVersionResult);
        boolean isNaviTabUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_NAVI_TAB, getAllConfigVersionResult);
        if (isRouterUpdate) {
            new ProfileUtils(IndexActivity.this, null).initProfile(false);
        }
        if (isContactUserUpdate) {
            getContactUser();
        } else if (handler != null) {
            handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
        }
        if (isContactOrgUpdate) {
            getContactOrg();
            getMultipleContactOrg();
        }
        if (isNaviTabUpdate) {
            getNaviTabData(ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_NAVI_TAB));
        }
        new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
            @Override
            public void getClientIdSuccess(String clientId) {
                boolean isMainTabUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_MAINTAB, getAllConfigVersionResult);
                if (isMainTabUpdate) {
                    getTabInfo();
                }
                boolean isSplashUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_SPLASH, getAllConfigVersionResult);
                if (isSplashUpdate) {
                    new SplashPageUtils(IndexActivity.this).update();//更新闪屏页面
                }
            }

            @Override
            public void getClientIdFail() {
            }
        }).getClientId();

    }

    /**
     * 获取所有的群组信息
     */
    private void getAllChannelGroup() {
        // TODO Auto-generated method stub
        if (!StringUtils.isBlank(WebServiceRouterManager.getInstance().getClusterChatVersion()) && NetUtils.isNetworkConnected(getApplicationContext(), false)) {
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
            String saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER);
            long contactUserLastQuetyTime = ContactUserCacheUtils.getLastQueryTime();
            if (contactUserLastQuetyTime == 0) {
                apiService.getContactUserList(saveConfigVersion);
            } else {
                apiService.getContactUserListUpdate(contactUserLastQuetyTime, saveConfigVersion);
            }
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
            String saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG);
            long contactOrgLastQuetyTime = ContactOrgCacheUtils.getLastQueryTime();
            if (contactOrgLastQuetyTime == 0) {
                apiService.getContactOrgList(saveConfigVersion);
            } else {
                apiService.getContactOrgListUpdate(contactOrgLastQuetyTime, saveConfigVersion);
            }

        }
    }

    /**
     * 获取多组织
     * 对应的人有
     */
    private void getMultipleContactOrg(){
        if(NetUtils.isNetworkConnected(getApplicationContext(),false)){
            ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getMultipleContactOrgList();
        }
    }

    /**
     * 获取所有的Robot
     */
    private void getAllRobotInfo() {
        if (!StringUtils.isBlank(WebServiceRouterManager.getInstance().getClusterBot()) && NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            ContactAPIService apiService = new ContactAPIService(IndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getAllRobotInfo();
        }
    }

    class CacheContactUserThread extends Thread {
        private byte[] result;
        private String saveConfigVersion;

        public CacheContactUserThread(byte[] result, String saveConfigVersion) {
            this.result = result;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.users users = ContactProtoBuf.users.parseFrom(result);
                List<ContactProtoBuf.user> userList = users.getUsersList();
                List<ContactUser> contactUserList = ContactProtoBuf.protoBufUserList2ContactUserList(userList, users.getLastQueryTime());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                ContactUserCacheUtils.setLastQueryTime(users.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER, saveConfigVersion);
                if (handler != null) {
                    handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CacheContactUserUpdateThread extends Thread {
        private GetContactUserListUpateResult getContactUserListUpateResult;
        private String saveConfigVersion;

        public CacheContactUserUpdateThread(GetContactUserListUpateResult getContactUserListUpateResult, String saveConfigVersion) {
            this.getContactUserListUpateResult = getContactUserListUpateResult;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                List<ContactUser> contactUserChangedList = getContactUserListUpateResult.getContactUserChangedList();
                List<String> contactUserIdDeleteList = getContactUserListUpateResult.getContactUserIdDeleteList();
                ContactUserCacheUtils.saveContactUserList(contactUserChangedList);
                ContactUserCacheUtils.deleteContactUserList(contactUserIdDeleteList);
                ContactUserCacheUtils.setLastQueryTime(getContactUserListUpateResult.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER, saveConfigVersion);
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
        private String saveConfigVersion;

        public CacheContactOrgThread(byte[] result, String saveConfigVersion) {
            this.result = result;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.orgs orgs = ContactProtoBuf.orgs.parseFrom(result);
                List<ContactProtoBuf.org> orgList = orgs.getOrgsList();
                List<ContactOrg> contactOrgList = ContactOrg.protoBufOrgList2ContactOrgList(orgList);
                ContactOrgCacheUtils.saveContactOrgList(contactOrgList);
                ContactOrgCacheUtils.setContactOrgRootId(orgs.getRootID());
                ContactOrgCacheUtils.setLastQueryTime(orgs.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG, saveConfigVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CacheContactOrgUpdateThread extends Thread {
        private GetContactOrgListUpateResult getContactOrgListUpateResult;
        private String saveConfigVersion;

        public CacheContactOrgUpdateThread(GetContactOrgListUpateResult getContactOrgListUpateResult, String saveConfigVersion) {
            this.getContactOrgListUpateResult = getContactOrgListUpateResult;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                List<ContactOrg> contactOrgChangedList = getContactOrgListUpateResult.getContactOrgChangedList();

                List<String> contactOrgIdDeleteList = getContactOrgListUpateResult.getContactOrgIdDeleteList();
                ContactOrgCacheUtils.saveContactOrgList(contactOrgChangedList);
                ContactOrgCacheUtils.deleteContactOrgList(contactOrgIdDeleteList);
                if (getContactOrgListUpateResult.getRootID() != null) {
                    ContactOrgCacheUtils.setContactOrgRootId(getContactOrgListUpateResult.getRootID());
                }
                ContactOrgCacheUtils.setLastQueryTime(getContactOrgListUpateResult.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG, saveConfigVersion);
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
                ChannelGroupCacheUtils.saveChannelGroupList(
                        getApplicationContext(), channelGroupList);
            } catch (Exception e) {
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
            RobotCacheUtils.saveRobotList(MyApplication.getInstance(), getAllBotInfoResultl.getRobotList());
        }
    }

    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnContactUserListSuccess(byte[] bytes, String saveConfigVersion) {
            new CacheContactUserThread(bytes, saveConfigVersion).start();
        }

        @Override
        public void returnContactUserListFail(String error, int errorCode) {
            if (handler != null) {
                handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
            }
//            WebServiceMiddleUtils.hand(IndexActivity.this, error, errorCode);
        }

        @Override
        public void returnContactUserListUpdateSuccess(GetContactUserListUpateResult getContactUserListUpateResult, String saveConfigVersion) {
            new CacheContactUserUpdateThread(getContactUserListUpateResult, saveConfigVersion).start();
        }

        @Override
        public void returnContactUserListUpdateFail(String error, int errorCode) {
            if (handler != null) {
                handler.sendEmptyMessage(SYNC_ALL_BASE_DATA_SUCCESS);
            }
//            WebServiceMiddleUtils.hand(IndexActivity.this, error, errorCode);
        }


        @Override
        public void returnContactOrgListSuccess(byte[] bytes, String saveConfigVersion) {
            new CacheContactOrgThread(bytes, saveConfigVersion).start();
        }

        @Override
        public void returnContactOrgListFail(String error, int errorCode) {

        }

        @Override
        public void returnContactOrgListUpdateSuccess(GetContactOrgListUpateResult getContactOrgListUpateResult, String saveConfigVersion) {
            new CacheContactOrgUpdateThread(getContactOrgListUpateResult, saveConfigVersion).start();
        }

        @Override
        public void returnContactOrgListUpdateFail(String error, int errorCode) {
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
        public void returnAppTabAutoSuccess(GetAppMainTabResult getAppMainTabResult, String mainTabSaveConfigVersion) {
            NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(IndexActivity.this, Constant.APP_TAB_LAYOUT_DATA, ""));
            if (naviBarModel.getNaviBarPayload().getNaviBarSchemeList().size() == 0) {
                updateMainTabbarWithOrder(getAppMainTabResult);
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_MAINTAB, mainTabSaveConfigVersion);
            } else {
                PreferencesByUserAndTanentUtils.putString(IndexActivity.this, Constant.PREF_APP_TAB_BAR_VERSION,
                        getAppMainTabResult.getMainTabPayLoad().getVersion());
                PreferencesByUserAndTanentUtils.putString(IndexActivity.this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT,
                        getAppMainTabResult.getAppTabInfo());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_MAINTAB, mainTabSaveConfigVersion);
            }
        }

        @Override
        public void returnAppTabAutoFail(String error, int errorCode) {
        }

        @Override
        public void returnNaviBarModelSuccess(NaviBarModel naviBarModel) {
            super.returnNaviBarModelSuccess(naviBarModel);
            PreferencesByUserAndTanentUtils.putString(IndexActivity.this, Constant.APP_TAB_LAYOUT_DATA, naviBarModel.getResponse());
            ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_NAVI_TAB, naviBarModel.getLastNaviLocalVersion());
            if (naviBarModel.getNaviBarPayload().getNaviBarSchemeList().size() != 0) {
                updateNaviTabbar();
            }
        }

        @Override
        public void returnNaviBarModelFail(String error, int errorCode) {
            super.returnNaviBarModelFail(error, errorCode);
        }

        @Override
        public void returnMultiContactOrgSuccess(GetMultiContactResult getMultiContactResult) {
            super.returnMultiContactOrgSuccess(getMultiContactResult);
            ContactOrgCacheUtils.saveMultiOrg(getMultiContactResult.getMultiOrgList());
        }

        @Override
        public void returnMultiContactOrgFail(String error, int errorCode) {
            super.returnMultiContactOrgFail(error, errorCode);
        }

        @Override
        public void returnAppRoleSuccess(String appRole) {
            super.returnAppRoleSuccess(appRole);
            PreferencesByUserAndTanentUtils.putString(IndexActivity.this,Constant.APP_ROLE,appRole);
        }

        @Override
        public void returnAppRoleFail(String error, int errorCode) {
            super.returnAppRoleFail(error, errorCode);
        }

        @Override
        public void returnIsAgreedSuccess(String isSuccess) {
            //0代表未同意，1代表已经统一隐私政策和服务协议
            if(JSONUtils.getString(isSuccess,"agreed","0").equals("0")){
//                ProtocolUtil.showProtocolDialog(IndexActivity.this);
            }
        }

        @Override
        public void returnIsAgreedFail(String error, int errorCode) {
            super.returnIsAgreedFail(error, errorCode);
        }
    }

}
