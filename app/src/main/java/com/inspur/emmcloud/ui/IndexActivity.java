package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.bean.AppTabBean;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetExceptionResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.interf.OnTabReselectListener;
import com.inspur.emmcloud.interf.OnWorkFragmentDataChanged;
import com.inspur.emmcloud.service.CollectService;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.FileSafeCode;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyFragmentTabHost;
import com.inspur.emmcloud.widget.tipsview.TipsView;
import com.inspur.reactnative.ReactNativeFlow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.facebook.react.common.ApplicationHolder.getApplication;
import static com.inspur.emmcloud.util.FileUtils.readFile;
import static com.inspur.reactnative.ReactNativeFlow.moveFolder;

/**
 * 主页面
 *
 * @author Administrator
 *
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
    private Handler handler;
    private boolean isHasCacheContact = false;
    private TipsView tipsView;
    private String reactNativeDicPath = "";
	private LoadingDialog loadingDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);
		((MyApplication) getApplicationContext()).addActivity(this);
		((MyApplication) getApplicationContext()).setIndexActvityRunning(true);
		((MyApplication) getApplicationContext()).closeAllDb();
		DbCacheUtils.initDb(getApplicationContext());
		loadingDlg = new LoadingDialog(IndexActivity.this,getString(R.string.app_init));
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
//		startUploadCollectService();
        initReactNative();
//        boolean moveSuccess = FileUtils.copyFolder(reactNativeDicPath+"/current",reactNativeDicPath+"/temp");
//        boolean moveSuccess = ReactNativeFlow.moveFolder(reactNativeDicPath+"/current",reactNativeDicPath+"/temp");
//        LogUtils.YfcDebug("移动是否成功："+moveSuccess);
        File file = new File("/sdcard/IMP-Cloud/cache/cloud/default.zip");
        try {
            LogUtils.YfcDebug("获取zip文件的sha1"+ FileSafeCode.getSha1(file));
            LogUtils.YfcDebug("获取zip文件的md5"+ FileSafeCode.getMD5(file));
            LogUtils.YfcDebug("获取zip文件的crc"+ FileSafeCode.getCRC32(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化ReactNative
     */
    private void initReactNative() {
        reactNativeDicPath = getFilesDir().getPath();
        if(!ReactNativeFlow.checkBundleFileIsExist(reactNativeDicPath+"/current/default/index.android.bundle")){
            LogUtils.YfcDebug("IndexActivity没有检测到Bundle");
            ReactNativeFlow.initReactNative(IndexActivity.this);
        }else{
            if(ReactNativeFlow.moreThanHalfHour("")){
                updateReactNative();
            }
        }
    }

    /**
     * 更新ReactNative
     */
    private void updateReactNative() {
        LogUtils.YfcDebug("IndexActivity发起检查更新的请求");
        AppAPIService appApiService = new AppAPIService(IndexActivity.this);
        appApiService.setAPIInterface(new WebService());
        //此处未写完读取json路径
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactNativeDicPath + "/current/xxx.json","UTF-8");
        try {
            JSONObject json = new JSONObject(String.valueOf(describeVersionAndTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(NetUtils.isNetworkConnected(IndexActivity.this)){
            appApiService.getReactNativeUpdate(0,0L);
        }
    }


    /***
     * 打开app应用行为分析上传的Service;
     */
    private void startUploadCollectService() {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(getApplicationContext(), "com.inspur.emmcloud.service.CollectService")) {
            Intent intent = new Intent();
            intent.setClass(this, CollectService.class);
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
            apiService.getAppTabs();
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
        handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
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
		intent.putExtra("command", "creat_group_icon");
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

//		MainTab[] tabs = MainTab.values();
        MainTab[] tabs = handleAppTabs();
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTab mainTab = tabs[i];
            TabSpec tab = mTabHost.newTabSpec(getString(mainTab.getResName()));
            View tabView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.tab_item_view, null);
            ImageView tabImg = (ImageView) tabView.findViewById(R.id.imageview);
            TextView tabText = (TextView) tabView.findViewById(R.id.textview);
            if (i == 0) {
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
            tabText.setText(getString(mainTab.getResName()));
            tabImg.setImageResource(mainTab.getResIcon());
            tab.setIndicator(tabView);
            tab.setContent(new TabContentFactory() {

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
     * 获取显示位置
     *
     * @return
     */
    private int getTabIndex() {
        int tabIndex = 0;
        String userId = ((MyApplication) getApplication()).getUid();
        String appTabs = PreferencesUtils.getString(IndexActivity.this,
                UriUtils.tanent + userId + "appTabs", "");
        ArrayList<AppTabBean> appTabList;
        if (!StringUtils.isBlank(appTabs)) {
            appTabList = (ArrayList<AppTabBean>) JSON.parseArray(appTabs, AppTabBean.class);
        } else {
            appTabList = new ArrayList<AppTabBean>();
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
     * 处理tab数组
     *
     * @return
     */
    private MainTab[] handleAppTabs() {
        MainTab[] tabs = null;
        String userId = ((MyApplication) getApplication()).getUid();
        String appTabs = PreferencesUtils.getString(IndexActivity.this,
                UriUtils.tanent + userId + "appTabs", "");
        if (!StringUtils.isBlank(appTabs)) {
            ArrayList<AppTabBean> appTabList = (ArrayList<AppTabBean>) JSON.parseArray(appTabs, AppTabBean.class);
            if (appTabList != null && appTabList.size() > 0) {
                tabs = new MainTab[appTabList.size()];
                for (int i = 0; i < appTabList.size(); i++) {
                    if (appTabList.get(i).getTitle().equals("communicate")) {
                        tabs[i] = MainTab.NEWS;
                    } else if (appTabList.get(i).getTitle().equals("work")) {
                        tabs[i] = MainTab.WORK;
                    } else if (appTabList.get(i).getTitle().equals("find")) {
                        tabs[i] = MainTab.FIND;
                    } else if (appTabList.get(i).getTitle().equals("application")) {
                        tabs[i] = MainTab.APPLICATION;
                    } else if (appTabList.get(i).getTitle().equals("mine")) {
                        tabs[i] = MainTab.MINE;
                    }
                }
            } else {
                tabs = MainTab.values();
            }
        } else {
            tabs = MainTab.values();
        }
        return tabs;
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
        if (newMessageTipsText != null) {
            newMessageTipsText = null;
        }
        if (newMessageTipsLayout != null) {
            newMessageTipsLayout = null;
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
     * @param fragment
     */
    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.attach(fragment);
        transaction.add(fragment,"");
        transaction.commit();
        LogUtils.YfcDebug("当前Fragment是否已经attached："+fragment.isAdded());
    }

    @Override
    public void onTabChanged(String tabId) {
        if (tabId.equals(getString(R.string.communicate))) {
            tipsView.setCanTouch(true);
        } else {
            tipsView.setCanTouch(false);
        }

    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(
                mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);
        LogUtils.debug("yfcLog", "返回到IndexActivity" + arg0);
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
                    handler.sendEmptyMessage(SYNC_CONTACT_SUCCESS);
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
            String userId = ((MyApplication) getApplication()).getUid();
            PreferencesUtils.putString(IndexActivity.this,
                    UriUtils.tanent + userId + "appTabs", JSON.toJSONString(getAppTabsResult.getAppTabBeanList()));
        }

        @Override
        public void returnGetAppTabsFail(String error) {
            WebServiceMiddleUtils.hand(IndexActivity.this, error);
        }


        @Override
        public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean) {
            updateReactNativeWithOrder(reactNativeUpdateBean);
        }

        @Override
        public void returnReactNativeUpdateFail(String error) {
            WebServiceMiddleUtils.hand(IndexActivity.this,error);
        }

    }

    /**
     * 按照更新指令更新ReactNative
     * @param reactNativeUpdateBean
     */
    private void updateReactNativeWithOrder(ReactNativeUpdateBean reactNativeUpdateBean) {
        int state = ReactNativeFlow.checkReactNativeOperation(reactNativeUpdateBean.getState());
        LogUtils.YfcDebug("返回请求结果state是："+state);
        if(state == ReactNativeFlow.REACT_NATIVE_RESET){
            //删除current和temp目录，重新解压assets下的zip
            resetReactNative();
            LogUtils.YfcDebug("重置操作");
        }else if(state == ReactNativeFlow.REACT_NATIVE_REVERT){
            //拷贝temp下的current到app内部current目录下
            moveFolder(reactNativeDicPath+"/temp",reactNativeDicPath+"/current");
            LogUtils.YfcDebug("回滚操作");
        }else if(state == ReactNativeFlow.REACT_NATIVE_FORWORD){
            //下载zip包并检查是否完整，完整则解压，不完整则重新下载,完整则把current移动到temp下，把新包解压到current
            ReactNativeFlow.downLoadZipFile(IndexActivity.this,reactNativeUpdateBean.getUrl());
            LogUtils.YfcDebug("更新版本操作");
        }else if(state == ReactNativeFlow.REACT_NATIVE_UNKNOWN){
            //发生了未知错误，下载state为0
            //同Reset的情况，删除current和temp目录，重新解压assets下的zip
            resetReactNative();
            LogUtils.YfcDebug("未知错误重置操作");
        }
    }

    /**
     * 重新整理目录恢复状态
     */
    private void resetReactNative() {
        FileUtils.deleteFile(reactNativeDicPath+"/temp");
        FileUtils.deleteFile(reactNativeDicPath+"/current");
        ReactNativeFlow.initReactNative(IndexActivity.this);
//        FindFragment.reactNativeViewNeedToRefresh = true;
    }

}
