package com.inspur.emmcloud.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.GetAppBadgeResult;
import com.inspur.emmcloud.bean.chat.EventMessageUnReadCount;
import com.inspur.emmcloud.bean.chat.TransparentBean;
import com.inspur.emmcloud.bean.contact.ContactClickMessage;
import com.inspur.emmcloud.bean.system.ChangeTabBean;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnTabReselectListener;
import com.inspur.emmcloud.ui.appcenter.MyAppFragment;
import com.inspur.emmcloud.ui.chat.CommunicationFragment;
import com.inspur.emmcloud.ui.chat.CommunicationV0Fragment;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.TabBean;
import com.inspur.emmcloud.ui.work.WorkFragment;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.MyFragmentTabHost;
import com.inspur.emmcloud.widget.tipsview.TipsView;
import com.inspur.imp.api.ImpFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

@ContentView(R.layout.activity_index)
public class IndexBaseActivity extends BaseFragmentActivity implements
        OnTabChangeListener, OnTouchListener {
    private long lastBackTime;

    @ViewInject(android.R.id.tabhost)
    public MyFragmentTabHost mTabHost;
    @ViewInject(R.id.preload_webview)
    protected WebView webView;
    private  TextView newMessageTipsText;

    private  RelativeLayout newMessageTipsLayout;

    @ViewInject(R.id.tip)
    private TipsView tipsView;
    private boolean isCommunicationRunning = false;
    private boolean isSystemChangeTag = true;//控制如果是系统切换的tab则不计入用户行为
    private String tabId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this);
        PreferencesUtils.putLong(this, Constant.PREF_ENTER_APP_TIME,System.currentTimeMillis());
        x.view().inject(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        initTabs();
    }

    /**
     * 处理tab数组
     *
     * @return
     */
    private void initTabs() {
        TabBean[] tabBeans = null;
        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        if (!StringUtils.isBlank(appTabs)) {
            Configuration config = getResources().getConfiguration();
            String environmentLanguage = config.locale.getLanguage();
            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(appTabs);
            //发送到MessageFragment
            EventBus.getDefault().post(getAppMainTabResult);
            ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabResultList();
            if (mainTabResultList.size() > 0) {
                tabBeans = new TabBean[mainTabResultList.size()];
                for (int i = 0; i < mainTabResultList.size(); i++) {
                    TabBean tabBean = null;
                    MainTabResult mainTabResult = mainTabResultList.get(i);
                    if(!mainTabResult.getType().equals("web")){
                        //包含native和发现两种
                        switch (mainTabResult.getUri()) {
                            case Constant.PREF_APP_TAB_BAR_COMMUNACATE:
                                if (MyApplication.getInstance().isV0VersionChat()){
                                    tabBean = new TabBean(getString(R.string.communicate), R.drawable.selector_tab_message_btn + "", CommunicationV0Fragment.class,mainTabResult);
                                }else {
                                    tabBean = new TabBean(getString(R.string.communicate), R.drawable.selector_tab_message_btn + "", CommunicationFragment.class,mainTabResult);
                                }
                                break;
                            case Constant.PREF_APP_TAB_BAR_WORK:
                                tabBean = new TabBean(getString(R.string.work), R.drawable.selector_tab_work_btn + "", WorkFragment.class,mainTabResult);
                                break;
                            case Constant.PREF_APP_TAB_BAR_RN_FIND:
                                tabBean = new TabBean(getString(R.string.find), R.drawable.selector_tab_find_btn + "", FindFragment.class,mainTabResult);
                                break;
                            case Constant.PREF_APP_TAB_BAR_APPLICATION:
                                tabBean = new TabBean(getString(R.string.application), R.drawable.selector_tab_app_btn + "", MyAppFragment.class,mainTabResult);
                                break;
                            case Constant.PREF_APP_TAB_BAR_PROFILE:
                                tabBean = new TabBean(getString(R.string.mine), R.drawable.selector_tab_more_btn + "", MoreFragment.class,mainTabResult);
                                break;
                            case Constant.PREF_APP_TAB_BAR_CONTACT:
                                tabBean = new TabBean(getString(R.string.contact),R.drawable.selector_tab_contact_btn + "", ContactSearchFragment.class,mainTabResult);
                                break;
                            default:
                                tabBean = new TabBean(getString(R.string.new_function), R.drawable.selector_tab_unknown_btn + "", NotSupportFragment.class,mainTabResult);
                                break;
                        }
                    }else{
                        tabBean = new TabBean(getString(R.string.web), R.drawable.selector_tab_cloud_tweet_btn + "", ImpFragment.class,mainTabResult);
                    }
                    tabBean.setTabId(mainTabResultList.get(i).getUri());
                    tabBeans[i] = internationalMainLanguage(mainTabResultList.get(i), environmentLanguage, tabBean);
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
                    return new View(IndexBaseActivity.this);
                }
            });
            if(tabBean.getMainTabResult().getType().equals("web")){
                Bundle bundle = new Bundle();
                bundle.putString("uri",tabBean.getMainTabResult().getUri());
                if(tabBean.getMainTabResult().getMainTabProperty().isHaveNavbar()){
                    bundle.putString("appName",tabBean.getMainTabResult().getName());
                }
                mTabHost.addTab(tab, tabBean.getClz(), bundle);
            }else{
                mTabHost.addTab(tab, tabBean.getClz(),null);
            }
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
            mTabHost.getTabWidget().getChildAt(i).setTag(tabBean.getTabId());
        }
        mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab((communicateIndex != -1 && isCommunicationRunning == false) ? communicateIndex : getTabIndex());
    }


    /**
     * 显示消息tab上的小红点（未读消息提醒）
     *
     * @param eventMessageUnReadCount
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMessageUnReadCount(EventMessageUnReadCount eventMessageUnReadCount) {
        if (newMessageTipsText != null) {
            if (eventMessageUnReadCount.getMessageUnReadCount() == 0) {
                newMessageTipsLayout.setVisibility(View.GONE);
            } else {
                String shoWNum = (eventMessageUnReadCount.getMessageUnReadCount() > 99)?"99+":eventMessageUnReadCount.getMessageUnReadCount() + "";
                newMessageTipsLayout.setVisibility(View.VISIBLE);
                newMessageTipsText.setText(shoWNum);
            }
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
            if (mTabHost.getTabWidget().getChildAt(i).getTag().toString().contains(Constant.PREF_APP_TAB_BAR_APPLICATION)) {
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

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void updateTabIndex(ChangeTabBean changeTabBean){
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            if (mTabHost.getTabWidget().getChildAt(i).getTag().toString().contains(changeTabBean.getTabId())) {
                mTabHost.setCurrentTab(i);
                break;
            }
        }
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
        ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(IndexBaseActivity.this, badgeNumber);
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
                MyAppFragment.class,new MainTabResult(new JSONObject()));
        tabBeanApp.setTabId(Constant.PREF_APP_TAB_BAR_APPLICATION);
        TabBean tabBeanMine = new TabBean(getString(R.string.mine), R.drawable.selector_tab_more_btn + "",
                MoreFragment.class,new MainTabResult(new JSONObject()));
        tabBeanMine.setTabId(Constant.PREF_APP_TAB_BAR_PROFILE);
        //无数据改为显示两个tab
        tabBeans[0] = tabBeanApp;
        tabBeans[1] = tabBeanMine;
        return tabBeans;
    }


    /**
     * 根据语言设置tab，扩展语言从这里扩展
     *
     * @param mainTabResult
     * @param environmentLanguage
     * @return
     */
    private TabBean internationalMainLanguage(MainTabResult mainTabResult, String environmentLanguage, TabBean tabBean) {
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
                LocalBroadcastManager.getInstance(IndexBaseActivity.this).sendBroadcast(intent);
                updateMessageUnReadCount(new EventMessageUnReadCount(0));
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
        String appTabs = PreferencesByUserAndTanentUtils.getString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        if (!StringUtils.isBlank(appTabs)) {
            ArrayList<MainTabResult> mainTabResultList = new GetAppMainTabResult(appTabs).getMainTabResultList();
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
            if(tabId.equals("find")){
                ContactClickMessage contactClickMessage = new ContactClickMessage();
                contactClickMessage.setTabId("find");
                contactClickMessage.setViewId(-1);
                EventBus.getDefault().post(contactClickMessage);
            }else if ((System.currentTimeMillis() - lastBackTime) > 2000) {
                ToastUtils.show(IndexBaseActivity.this,
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
    public void onTabChanged(final String tabId) {
        tipsView.setCanTouch(tabId.equals("communicate"));
        if (!isSystemChangeTag) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //记录打开的tab页
                    PVCollectModel pvCollectModel = new PVCollectModel(tabId, tabId);
                    PVCollectModelCacheUtils.saveCollectModel(IndexBaseActivity.this, pvCollectModel);
                }
            }).start();
            isSystemChangeTag = true;
        }
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(
                mTabHost.getCurrentTabTag());
    }

    /**
     * 根据命令升级Tabbar
     *
     * @param getAppMainTabResult
     */
    public void updateTabbarWithOrder(GetAppMainTabResult getAppMainTabResult) {
        String command = getAppMainTabResult.getCommand();
        if (command.equals("FORWARD")) {
            PreferencesByUserAndTanentUtils.putString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_VERSION, getAppMainTabResult.getVersion());
            PreferencesByUserAndTanentUtils.putString(IndexBaseActivity.this, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, getAppMainTabResult.getAppTabInfo());
            mTabHost.clearAllTabs(); //更新tabbar
            initTabs();
        }
    }
}
