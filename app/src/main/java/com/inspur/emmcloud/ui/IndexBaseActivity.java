package com.inspur.emmcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.SelectorUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ConfirmDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.contact.ContactClickMessage;
import com.inspur.emmcloud.bean.system.ChangeTabBean;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.TabBean;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.broadcastreceiver.NetworkChangeReceiver;
import com.inspur.emmcloud.broadcastreceiver.ScreenBroadcastReceiver;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;
import com.inspur.emmcloud.componentservice.web.WebService;
import com.inspur.emmcloud.ui.chat.CommunicationFragment;
import com.inspur.emmcloud.ui.chat.CommunicationV0Fragment;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.schedule.ScheduleHomeFragment;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.widget.MyFragmentTabHost;
import com.inspur.emmcloud.widget.tipsview.TipsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IndexBaseActivity extends BaseFragmentActivity implements OnTabChangeListener, OnTouchListener {
    private static final int REQUEST_CREATE_GUESTURE = 1;
    @BindView(android.R.id.tabhost)
    public MyFragmentTabHost mTabHost;
    @BindView(R.id.preload_webview)
    protected WebView webView;
    protected NetworkChangeReceiver networkChangeReceiver;
    @BindView(R.id.tip)
    TipsView tipsView;
    private long lastBackTime;
    private TextView newMessageTipsText;
    private RelativeLayout newMessageTipsLayout;
    private boolean batteryDialogIsShow = true;
    private boolean isCommunicationRunning = false;
    private boolean isSystemChangeTag = true;// 控制如果是系统切换的tab则不计入用户行为
    private String tabId = "";
    // protected ConnectivityManager.NetworkCallback networkCallback;
    // protected ConnectivityManager connectivityManager;
    private ConfirmDialog confirmDialog;
    private ArrayList<MainTabResult> mainTabResultList = new ArrayList<>();

    @Override
    public void onCreate() {
        setContentView(R.layout.activity_index);
        ButterKnife.bind(this);
        clearOldMainTabData();
        setStatus();
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        registerNetWorkListenerAccordingSysLevel();
        registerScreenReceiver();
        initTabs();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForceGuesture();
    }

    private void registerNetWorkListenerAccordingSysLevel() {
        // if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        // } else {
        // networkCallback = new NetworkCallbackImpl(this);
        // NetworkRequest.Builder builder = new NetworkRequest.Builder();
        // NetworkRequest request = builder.build();
        // connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // connectivityManager.registerNetworkCallback(request, networkCallback);
        // }
    }

    private void registerScreenReceiver() {
        ScreenBroadcastReceiver screenBroadcastReceiver = new ScreenBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        getApplicationContext().registerReceiver(screenBroadcastReceiver, filter);
    }

    /**
     * 检测配置是否强制开启手势验证码
     */
    private void checkForceGuesture() {
        int doubleValidation = PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(),
                Constant.PREF_MNM_DOUBLE_VALIADATION, -1);
        if (doubleValidation == 1 && !CreateGestureActivity.getGestureCodeIsOpenByUser(MyApplication.getInstance())) {
            Intent intent = new Intent(this, CreateGestureActivity.class);
            intent.putExtra(CreateGestureActivity.EXTRA_FORCE_SET, true);
            startActivityForResult(intent, REQUEST_CREATE_GUESTURE);
        }

    }

    /**
     * 清除旧版本的MainTab数据
     */
    private void clearOldMainTabData() {
        PreferencesByUserAndTanentUtils.clearDataByKey(this, "app_tabbar_version");
        PreferencesByUserAndTanentUtils.clearDataByKey(this, "app_tabbar_info_current");
    }

    /**
     * 处理tab数组
     *
     * @return
     */
    private void initTabs() {
        TabBean[] tabBeans = null;
//        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexBaseActivity.this,
//                Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        mainTabResultList = AppTabUtils.getMainTabResultList(IndexBaseActivity.this);
        if (mainTabResultList.size() > 0) {
            String environmentLanguage = LanguageManager.getInstance().getCurrentAppLanguage();
//            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(appTabs);
//            // 发送到MessageFragment
//            EventBus.getDefault().post(getAppMainTabResult);
//            ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
            if (mainTabResultList.size() > 0) {
                tabBeans = new TabBean[mainTabResultList.size()];
                for (int i = 0; i < mainTabResultList.size(); i++) {
                    TabBean tabBean = null;
                    MainTabResult mainTabResult = mainTabResultList.get(i);
                    switch (mainTabResult.getType()) {
                        case Constant.APP_TAB_TYPE_NATIVE:
                            switch (mainTabResult.getUri()) {
                                case Constant.APP_TAB_BAR_COMMUNACATE:
                                    if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                        tabBean = new TabBean(getString(R.string.communicate), CommunicationV0Fragment.class,
                                                mainTabResult);
                                    } else {
                                        tabBean = new TabBean(getString(R.string.communicate), CommunicationFragment.class,
                                                mainTabResult);
                                    }
                                    break;
                                case Constant.APP_TAB_BAR_WORK:
                                    tabBean = new TabBean(getString(R.string.work), ScheduleHomeFragment.class, mainTabResult);
                                    break;
                                case Constant.APP_TAB_BAR_APPLICATION:
                                    Router router = Router.getInstance();
                                    if (router.getService(ApplicationService.class) != null) {
                                        ApplicationService service = router.getService(ApplicationService.class);
                                        tabBean = new TabBean(getString(R.string.application), service.getMyAppFragment(), mainTabResult);
                                    }
                                    break;
                                case Constant.APP_TAB_BAR_PROFILE:
                                    tabBean = new TabBean(getString(R.string.mine), MoreFragment.class, mainTabResult);
                                    break;
                                case Constant.APP_TAB_BAR_CONTACT:
                                    tabBean = new TabBean(getString(R.string.contact), ContactSearchFragment.class,
                                            mainTabResult);
                                    break;
                            }
                            break;
                        case Constant.APP_TAB_TYPE_RN:
                            switch (mainTabResult.getUri()) {
                                case Constant.APP_TAB_BAR_RN_FIND:
                                    tabBean = new TabBean(getString(R.string.find), FindFragment.class, mainTabResult);
                                    break;
                            }
                            break;
                        case Constant.APP_TAB_TYPE_WEB:
                            Router router = Router.getInstance();
                            if (router.getService(WebService.class) != null) {
                                WebService service = router.getService(WebService.class);
                                tabBean = new TabBean(getString(R.string.web), service.getImpFragmentClass(), mainTabResult);
                            }

                            break;
                    }
                    if (tabBean == null) {
                        String noSupportTabName =
                                mainTabResult.getMainTabTitleResult().getTabTileByLanguage(environmentLanguage);
                        tabBean = new TabBean(noSupportTabName, NotSupportFragment.class, mainTabResult);
                    }
                    tabBean.setTabId(mainTabResultList.get(i).getUri());
                    tabBeans[i] = internationalMainLanguage(mainTabResultList.get(i), environmentLanguage, tabBean);
                }
            }
        }
        showTabs(tabBeans);
    }


    // 显示icon本地映射
    private int getIconFromLocalByIco(String icon) {
        int localIcon = R.drawable.selector_tab_unknown_btn;
        switch (icon) {
            case Constant.APP_TAB_BAR_COMMUNACATE_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_communicate);
                break;
            case Constant.APP_TAB_BAR_APPLICATION_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_app);
                break;
            case Constant.APP_TAB_BAR_WORK_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_work);
                break;
            case Constant.APP_TAB_BAR_MOMENT_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_cloud_tweet);
                break;
            case Constant.APP_TAB_BAR_ME_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_mine);
                break;
            case Constant.APP_TAB_BAR_CONTACT_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_contact);
                break;
            case Constant.APP_TAB_BAR_DISCOVER_NAME:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_find);
                break;
            default:
                localIcon = ResourceUtils.getResValueOfAttr(IndexBaseActivity.this, R.attr.bg_tab_unknown);
                break;
        }
        return localIcon;
    }

    protected void batteryWhiteListRemind(final Context context) {
        batteryDialogIsShow = PreferencesUtils.getBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && batteryDialogIsShow) {
            try {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
                if (!hasIgnored) {
                    confirmDialog = new ConfirmDialog(context, R.string.battery_tip_content,
                            R.string.battery_tip_ishide, R.string.battery_tip_toset, R.string.battery_tip_cancel);
                    confirmDialog.setClicklistener(new ConfirmDialog.ClickListenerInterface() {
                        @Override
                        public void doConfirm() {
                            if (confirmDialog.getIsHide()) {
                                PreferencesUtils.putBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, false);
                            }
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                PreferencesUtils.putBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, false);
                            } finally {
                                confirmDialog.dismiss();
                            }
                        }

                        @Override
                        public void doCancel() {
                            if (confirmDialog.getIsHide()) {
                                PreferencesUtils.putBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, false);
                            }
                            // TODO Auto-generated method stub
                            confirmDialog.dismiss();
                        }
                    });
                    confirmDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            View tabView = LayoutInflater.from(this).inflate(R.layout.tab_item_view, null);
            ImageView tabImg = tabView.findViewById(R.id.imageview);
            TextView tabText = tabView.findViewById(R.id.textview);
//            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
//            if (currentThemeNo == 2) {
//                tabText.setTextColor(getResources().getColorStateList(R.color.seclector_footer_text_grey));
//            } else {
//                tabText.setTextColor(getResources().getColorStateList(R.color.seclector_footer_text_white));
//            }

            if (tabId.equals(Constant.APP_TAB_BAR_COMMUNACATE)) {
                handleTipsView(tabView);
                communicateIndex = i;
            }
            tabText.setText(tabBean.getTabName());
            if (tabBean.getMainTabResult().getIcon().startsWith("http")) {
                SelectorUtils.addSelectorFromNet(IndexBaseActivity.this, tabBean.getMainTabResult().getIcon(), tabImg, R.drawable.ic_tab_net_unknown, R.drawable.ic_tab_net_unknown_selected);
            } else {
                tabImg.setImageResource(getIconFromLocalByIco(tabBean.getMainTabResult().getIcon()));
            }
            tab.setIndicator(tabView);
            tab.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(IndexBaseActivity.this);
                }
            });
            Bundle bundle = new Bundle();
            if (tabBean.getMainTabResult().getType().equals(Constant.APP_TAB_TYPE_WEB)) {
                if (tabBean.getMainTabResult().getMainTabProperty().isHaveNavbar()) {
                    bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
                    bundle.putString(Constant.WEB_FRAGMENT_VERSION, PreferencesByUserAndTanentUtils
                            .getString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_VERSION, ""));
                    bundle.putSerializable(Constant.WEB_FRAGMENT_MENU,
                            (Serializable) tabBean.getMainTabResult().getMainTabProperty().getMainTabMenuList());
                } else {
                    bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, false);
                }
                bundle.putBoolean(Constant.Web_STATIC_TITLE, true);
            }
            bundle.putString(Constant.APP_WEB_URI, tabBean.getMainTabResult().getUri());
            bundle.putString(Constant.WEB_FRAGMENT_APP_NAME, tabBean.getTabName());
            mTabHost.addTab(tab, tabBean.getClz(), bundle);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
            mTabHost.getTabWidget().getChildAt(i).setTag(tabBean.getTabId());
        }
        mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab(
                (communicateIndex != -1 && isCommunicationRunning == false) ? communicateIndex : getTabIndex());
    }

    /**
     * 沟通未读数目变化
     *
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveCommunicationBadgeNum(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT)) {
            int communicationBadgeNum = (Integer) eventMessage.getMessageObj();
            setTabBarBadge(Constant.APP_TAB_BAR_COMMUNACATE_NAME, communicationBadgeNum);
        }
    }

    /**
     * 这个app未读数目变化
     *
     * @param badgeBodyModel
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAppBadgeNum(BadgeBodyModel badgeBodyModel) {
        if (badgeBodyModel.isSNSExist()) {
            int snsTabBarBadgeNum = badgeBodyModel.getSnsBadgeBodyModuleModel().getTotal();
            setTabBarBadge(Constant.APP_TAB_BAR_MOMENT_NAME, snsTabBarBadgeNum);
        } else {
            // 如果没有动态的key就把动态清0
            setTabBarBadge(Constant.APP_TAB_BAR_MOMENT_NAME, 0);
        }
        if (badgeBodyModel.isAppStoreExist()) {
            int appStoreTabBarBadgeNum = badgeBodyModel.getAppStoreBadgeBodyModuleModel().getTotal();
            if (appStoreTabBarBadgeNum > 0) {
                Map<String, Integer> appStoreBadgeMap =
                        badgeBodyModel.getAppStoreBadgeBodyModuleModel().getDetailBodyMap();
                appStoreTabBarBadgeNum = getFilterAppStoreBadgeNum(appStoreBadgeMap);
            }
            setTabBarBadge(Constant.APP_TAB_BAR_APPLICATION_NAME, appStoreTabBarBadgeNum);
        } else {
            // 如果没有应用的key就把应用清0
            setTabBarBadge(Constant.APP_TAB_BAR_APPLICATION_NAME, 0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveScheme(final SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.APP_TAB_BAR_WORK)) {
            SimpleEventMessage stickyEvent = EventBus.getDefault().getStickyEvent(SimpleEventMessage.class);
            if (stickyEvent != null) {
                EventBus.getDefault().removeStickyEvent(stickyEvent);
            }
            int index = findTargetTabIndex();
            if (mTabHost != null) {
                mTabHost.setCurrentTab(index);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        simpleEventMessage.setAction(Constant.SCHEDULE_DETAIL);
                        EventBus.getDefault().post(simpleEventMessage);
                    }
                }, 100);
            }
        }
    }

    /**
     * 通过scheme找到跳转的页面位置
     *
     * @return
     */
    private int findTargetTabIndex() {
        for (int i = 0; i < mainTabResultList.size(); i++) {
            if (mainTabResultList.get(i).getUri().equals(Constant.APP_TAB_BAR_WORK)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 过滤应用角标数目（只显示已安装的应用角标数目）
     *
     * @param appBadgeMap
     * @return
     */
    private int getFilterAppStoreBadgeNum(Map<String, Integer> appBadgeMap) {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            return service.getFilterAppStoreBadgeNum(appBadgeMap);
        }
        return 0;
    }

    private void setTabBarBadge(String tabName, int number) {
        // 在某些情况下
        try {
            // 查找tab之前，先清空一下对应tab的数据，防止tab找不到，数据还有的情况
            saveTabBarBadgeNumber(tabName, 0);
            // 根据tabName确定tabView的位置
            List<MainTabResult> mainTabResultList = AppTabUtils.getMainTabResultList(this);
            for (int i = 0; i < mainTabResultList.size(); i++) {
                if (mainTabResultList.get(i).getName().equals(tabName)) {
                    View tabView = mTabHost.getTabWidget().getChildAt(i);
                    RelativeLayout badgeLayout = (RelativeLayout) tabView.findViewById(R.id.rl_badge);
                    View badgeView = tabView.findViewById(R.id.v_badge);
                    if (number < 0) {
                        badgeLayout.setVisibility(View.GONE);
                        badgeView.setVisibility(View.VISIBLE);
                    } else {
                        badgeView.setVisibility(View.GONE);
                        badgeLayout.setVisibility((number == 0) ? View.GONE : View.VISIBLE);
                        TextView badgeText = (TextView) tabView.findViewById(R.id.tv_badge);
                        badgeText.setText("" + (number > 99 ? "99+" : number));
                    }
                    saveTabBarBadgeNumber(tabName, number);
                }
            }
            // 更新桌面角标数字
            ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(IndexBaseActivity.this, getDesktopNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据正负数规则获取桌面显示总数
     *
     * @return
     */
    private int getDesktopNumber() {
        int communicationTabBarNumber = PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(),
                Constant.PREF_BADGE_NUM_COMMUNICATION, 0);
        int appStoreTabBarNumber = PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(),
                Constant.PREF_BADGE_NUM_APPSTORE, 0);
        int momentTabBarNumber =
                PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(), Constant.PREF_BADGE_NUM_SNS, 0);
        return (WebServiceRouterManager.getInstance().isV0VersionChat() ? 0 : communicationTabBarNumber)
                + (appStoreTabBarNumber >= 0 ? appStoreTabBarNumber : 0)
                + (momentTabBarNumber >= 0 ? momentTabBarNumber : 0);
    }

    /**
     * 找到对应的tab后保存tabBarBadgeNumber
     *
     * @param tabName
     * @param tabBarBadgeNumber
     */
    private void saveTabBarBadgeNumber(String tabName, int tabBarBadgeNumber) {
        switch (tabName) {
            case Constant.APP_TAB_BAR_APPLICATION_NAME:
                PreferencesByUserAndTanentUtils.putInt(MyApplication.getInstance(), Constant.PREF_BADGE_NUM_APPSTORE,
                        tabBarBadgeNumber);
                break;
            case Constant.APP_TAB_BAR_COMMUNACATE_NAME:
                PreferencesByUserAndTanentUtils.putInt(MyApplication.getInstance(), Constant.PREF_BADGE_NUM_COMMUNICATION,
                        tabBarBadgeNumber);
                break;
            case Constant.APP_TAB_BAR_MOMENT_NAME:
                PreferencesByUserAndTanentUtils.putInt(MyApplication.getInstance(), Constant.PREF_BADGE_NUM_SNS,
                        tabBarBadgeNumber);
                break;
        }
    }

    /**
     * 打开相应位置的tab
     *
     * @param changeTabBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTabIndex(ChangeTabBean changeTabBean) {
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            if (mTabHost.getTabWidget().getChildAt(i).getTag().toString().contains(changeTabBean.getTabId())) {
                mTabHost.setCurrentTab(i);
                break;
            }
        }
    }

    /**
     * IndexActiveX首先打开MessageFragment,然后打开其他tab
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDefaultTab(SimpleEventMessage eventMessage) {
        try {
            if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_OPEN_DEFALT_TAB)) {
                isCommunicationRunning = true;
                int targetTabIndex = getTabIndex();
                boolean isOpenNotify = getIntent().hasExtra("command")
                        && getIntent().getStringExtra("command").equals("open_notification");
                if (mTabHost != null && mTabHost.getCurrentTab() != targetTabIndex && !isOpenNotify) {
                    mTabHost.setCurrentTab(targetTabIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据语言设置tab，扩展语言从这里扩展
     *
     * @param mainTabResult
     * @param environmentLanguage
     * @return
     */
    private TabBean internationalMainLanguage(MainTabResult mainTabResult, String environmentLanguage,
                                              TabBean tabBean) {
        if (!tabBean.getClz().getName().equals(NotSupportFragment.class.getName())) {
            switch (environmentLanguage.toLowerCase()) {
                case "zh-hant":
                    tabBean.setTabName(mainTabResult.getMainTabTitleResult().getZhHant());
                    break;
                case "en":
                case "en-us":
                    tabBean.setTabName(mainTabResult.getMainTabTitleResult().getEnUS());
                    break;
                default:
                    tabBean.setTabName(mainTabResult.getMainTabTitleResult().getZhHans());
                    break;
            }
        }
        return tabBean;
    }

    /**
     * 处理小红点的逻辑
     *
     * @param tabView
     */
    private void handleTipsView(View tabView) {
        newMessageTipsText = (TextView) tabView.findViewById(R.id.tv_badge);
        newMessageTipsLayout = (RelativeLayout) tabView.findViewById(R.id.rl_badge);
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
                LocalBroadcastManager.getInstance(IndexBaseActivity.this).sendBroadcast(intent);
                onReceiveCommunicationBadgeNum(
                        new SimpleEventMessage(Constant.EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT, 0));
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
//        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexBaseActivity.this,
//                Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        ArrayList<MainTabResult> mainTabResultList = AppTabUtils.getMainTabResultList(this);
        if (mainTabResultList.size() > 0) {
//            ArrayList<MainTabResult> mainTabResultList =
//                    new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
            if (mainTabResultList.size() > 0) {
                for (int i = 0; i < mainTabResultList.size(); i++) {
                    if (mainTabResultList.get(i).isSelected()) {
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
            // 如果是通讯录tab逐级返回功能，发送到ContactSearchFragment updateUI方法
            if (tabId.equals(Constant.APP_TAB_BAR_CONTACT)) {
                ContactClickMessage contactClickMessage = new ContactClickMessage();
                contactClickMessage.setTabId(Constant.APP_TAB_BAR_CONTACT);
                contactClickMessage.setViewId(-1);
                EventBus.getDefault().post(contactClickMessage);
            } else if ((System.currentTimeMillis() - lastBackTime) > 2000) {
                ToastUtils.show(IndexBaseActivity.this, getString(R.string.reclick_to_desktop));
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
        if (event.getAction() == MotionEvent.ACTION_DOWN && !v.equals(mTabHost.getCurrentTabView())) {
            isSystemChangeTag = false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onTabChanged(final String tabId) {
        this.tabId = tabId;
        tipsView.setCanTouch(tabId.equals(Constant.APP_TAB_BAR_COMMUNACATE));
        if (!isSystemChangeTag) {
            String mainTabName = getMainTabName(tabId);
            PVCollectModelCacheUtils.saveCollectModel(mainTabName, mainTabName);
            isSystemChangeTag = true;
        }
        //每次切换到工作tab时需要发出通知，刷新日程数据
        if (tabId.equals(Constant.APP_TAB_BAR_WORK)) {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_GUESTURE && resultCode != RESULT_OK) {
            MyApplication.getInstance().exit();
        }
    }

    /**
     * 根据tabId获取mainTab的name
     *
     * @param tabId
     * @return
     */
    private String getMainTabName(String tabId) {
        String functionId = "";
        String appTabs = PreferencesByUserAndTanentUtils.getString(this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        ArrayList<MainTabResult> tabList = new GetAppMainTabResult(appTabs).getMainTabPayLoad().getMainTabResultList();
        for (int i = 0; i < tabList.size(); i++) {
            MainTabResult mainTabResult = tabList.get(i);
            if (mainTabResult.getUri().equals(tabId)) {
                functionId = mainTabResult.getName();
                break;
            }
        }
        return functionId;
    }

    /**
     * 根据命令升级Tabbar
     *
     * @param getAppMainTabResult
     */
    public void updateMainTabbarWithOrder(GetAppMainTabResult getAppMainTabResult) {
        String command = getAppMainTabResult.getCommand();
        if (command.equals("FORWARD")) {
            PreferencesByUserAndTanentUtils.putString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_VERSION,
                    getAppMainTabResult.getMainTabPayLoad().getVersion());
            PreferencesByUserAndTanentUtils.putString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT,
                    getAppMainTabResult.getAppTabInfo());
            mTabHost.clearAllTabs(); // 更新tabbar
            initTabs();
        }
    }

    public void updateNaviTabbar() {
        mTabHost.clearAllTabs(); // 更新tabbar
        initTabs();
    }

    @Override
    protected void onDestroy() {
        // if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
            networkChangeReceiver = null;
        }
        // } else {
        // if (connectivityManager != null && networkCallback != null) {
        // connectivityManager.unregisterNetworkCallback(networkCallback);
        // networkCallback = null;
        // }
        // }
        super.onDestroy();
    }
}
