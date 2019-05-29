package com.inspur.emmcloud.ui.appcenter;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragment;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.DragAdapter;
import com.inspur.emmcloud.adapter.RecommendAppWidgetListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;
import com.inspur.emmcloud.bean.appcenter.AppOrder;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.RecommendAppWidgetBean;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.bean.system.GetAllConfigVersionResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModuleModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnRecommendAppWidgetItemClickListener;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.common.CheckingNetStateUtils;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ShortCutUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientConfigUpdateUtils;
import com.inspur.emmcloud.util.privates.MyAppWidgetUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ScanQrCodeUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.widget.draggrid.DragGridView;
import com.inspur.emmcloud.widget.draggrid.DragGridView.OnChanageListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * classes : com.inspur.emmcloud.ui.app.MyAppFragment Create at 2016年12月13日
 * 上午11:10:20
 */
public class MyAppFragment extends BaseFragment {

    private static final int REQUEST_SCAN_LOGIN_QRCODE_RESULT = 5;
    private static final String ACTION_NAME = "add_app";
    private long lastOnItemClickTime = 0;//防止多次点击
    private View rootView;
    private ListView appListView;
    private AppListAdapter appListAdapter;
    private ImageButton configBtn;
    private ImageButton appcenterEnterBtn;
    private Button sortFinishBtn;
    private MyAppAPIService apiService;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private PopupWindow popupWindow;
    private MyAppSaveTask myAppSaveTask;
    private Map<String, Integer> appStoreBadgeMap = new HashMap<>();
    private RecyclerView recommendAppWidgetListView = null;
    private RecommendAppWidgetListAdapter recommendAppWidgetListAdapter = null;
    private int appListSizeExceptCommonlyUse = 0;
    private DataSetObserver dataSetObserver;
    private View netExceptionView;
    private boolean haveHeader = false;
    private boolean hasRequestBadgeNum = false;
    private MyOnClickListener myOnClickListener;
    private LinearLayout commonlyUseLayout;

    private CheckingNetStateUtils checkingNetStateUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkingNetStateUtils = new CheckingNetStateUtils(getContext(), NetUtils.pingUrls);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_app, null);
        initViews();
        registerReceiver();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarCommon();
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_app, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        getMyAppRecommendWidgetsUpdate();
        return rootView;
    }

    /**
     * 检查获取我的应用推荐应用小部件更新
     * 过期则更新不过期不更新
     */
    private void getMyAppRecommendWidgetsUpdate() {
        if (!MyAppWidgetUtils.isEffective(PreferencesByUserAndTanentUtils.getLong(getContext(), Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE, 0L))) {
            MyAppWidgetUtils.getInstance(getActivity().getApplicationContext()).getMyAppWidgetsFromNet();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_MY_APP)) {
            getMyApp();
        }
        hasRequestBadgeNum = false;
        if (!StringUtils.isBlank(MyAppCacheUtils.getMyAppListJson(getActivity()))) {
            new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
            hasRequestBadgeNum = true;
        }
        refreshRecommendAppWidgetView();
        checkingNetStateUtils.getNetStateResult(5);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        netExceptionView = LayoutInflater.from(getContext()).inflate(R.layout.recycleview_header_item, null);
        netExceptionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.startActivity(getActivity(), NetWorkStateDetailActivity.class);
            }
        });
        apiService = new MyAppAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        //当Adapter的大小发生改变时调用此方法
        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                boolean isDataBlank = appListAdapter == null || appListAdapter.getCount() == 0;
                (rootView
                        .findViewById(R.id.rl_no_app)).setVisibility(isDataBlank ? View.VISIBLE : View.GONE);
            }
        };
        initPullRefreshLayout();
        appListView = rootView.findViewById(R.id.my_app_list);
        refreshAppListView();
        configBtn = rootView.findViewById(R.id.ibt_appcenter_config);
        myOnClickListener = new MyOnClickListener();
        configBtn.setOnClickListener(myOnClickListener);
        sortFinishBtn = rootView.findViewById(R.id.bt_sort_finish);
        sortFinishBtn.setOnClickListener(myOnClickListener);
        appcenterEnterBtn = rootView.findViewById(R.id.ibt_appcenter_enter);
        appcenterEnterBtn.setOnClickListener(myOnClickListener);
        setTabTitle();
        //当Fragment创建时重置时间
        PreferencesByUserAndTanentUtils.putInt(getActivity(), Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR, 0);
    }

    /**
     * 添加LIstView 的HeaderView
     */
    private void AddHeaderView() {
        if (!haveHeader) {
            appListView.addHeaderView(netExceptionView);
            haveHeader = true;
        }
    }

    /**
     * 删除ListView 的HeaderView
     */
    private void DeleteHeaderView() {
        if (haveHeader) {
            appListView.removeHeaderView(netExceptionView);
            haveHeader = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == REQUEST_SCAN_LOGIN_QRCODE_RESULT)) {
            if (data.hasExtra("isDecodeSuccess")) {
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess", false);
                if (isDecodeSuccess) {
                    String msg = data.getStringExtra("msg");
                    ScanQrCodeUtils.getScanQrCodeUtilsInstance(getActivity()).handleActionWithMsg(msg);
                } else {
                    ToastUtils.show(getActivity(), getString(R.string.qr_code_analysis_fail));
                }
            }
        }
    }

    /**
     * 刷新推荐应用小部件
     * 每个小时都有可能有变化
     */
    private void refreshRecommendAppWidgetView() {
        //判断时间是否点击了叉号，不在显示时间内，或者推荐应用已经过了有效期
        if (!(MyAppWidgetUtils.isNeedShowMyAppRecommendWidgets(getActivity())) ||
                !MyAppWidgetUtils.isEffective(PreferencesByUserAndTanentUtils.getLong(getContext()
                        , Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE, 0L))) {
            (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
            return;
        }
        //是否是需要刷新的时间，即过了当前小时内appId的显示时间，这是只控制刷新，不控制显示隐藏，MyAPPFragment Destroy时会重置这个时间，使下次进入时不会影响刷新UI
        boolean isRefreshTime = PreferencesByUserAndTanentUtils.getInt(getActivity(), Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR, -1) != MyAppWidgetUtils.getNowHour();
        if (!isRefreshTime) {
            return;
        }
        GetRecommendAppWidgetListResult getRecommendAppWidgetListResult = new GetRecommendAppWidgetListResult(PreferencesByUserAndTanentUtils.
                getString(getActivity(), Constant.PREF_MY_APP_RECOMMEND_DATA, ""));
        List<RecommendAppWidgetBean> recommendAppWidgetBeanList = getRecommendAppWidgetListResult.getRecommendAppWidgetBeanList();
        List<App> appList = MyAppWidgetUtils.getShouldShowAppList(recommendAppWidgetBeanList, appListAdapter.getAppAdapterList());
        if (appList.size() > 0) {
            if (recommendAppWidgetListView == null) {
                recommendAppWidgetListView = (RecyclerView) rootView.findViewById(R.id.my_app_recommend_app_wiget_recyclerview);
                (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.VISIBLE);
                recommendAppWidgetListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recommendAppWidgetListView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(getActivity(), 4)));
                recommendAppWidgetListAdapter = new RecommendAppWidgetListAdapter(getActivity());
                recommendAppWidgetListView.setAdapter(recommendAppWidgetListAdapter);
                recommendAppWidgetListAdapter.setOnRecommendAppWidgetItemClickListener(new OnRecommendAppWidgetItemClickListener() {
                    @Override
                    public void onRecommendAppWidgetItemClick(App app) {
                        UriUtils.openApp(getActivity(), app, "smartrecommend");
                    }
                });
                rootView.findViewById(R.id.my_app_recommend_app_widget_img).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
                        MyAppWidgetUtils.saveNotShowDate(getActivity(), TimeUtils.getEndTime());
                    }
                });
            }
            recommendAppWidgetListAdapter.setAndReFreshRecommendList(appList);
            PreferencesByUserAndTanentUtils.putInt(getActivity(), Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR, MyAppWidgetUtils.getNowHour());
        } else {
            //当前小时没有需要显示的appId或者列表中没有当前小时内的应用
            (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
        }
    }


    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyApp();
                new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
            }
        });
    }


    /**
     * 初始化AppListView，加载缓存数据
     */
    private void refreshAppListView() {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(getContext());
        if (appListAdapter != null) {
            appListAdapter.setAppAdapterList(appGroupList);
        } else {
//            List<App> commonlyUseNeedShowList = AppCacheUtils.getCommonlyUseNeedShowList(getActivity());
//            boolean isNeedCommonlyUseAppList = true;
//            for (int i = 0; i < (commonlyUseNeedShowList.size()>8?8:commonlyUseNeedShowList.size()); i++) {
//                if(!appGroupList.get(0).getAppItemList().get(i).getAppID().equals(i)){
//                    isNeedCommonlyUseAppList = false;
//                }
//            }
//            if(!isNeedCommonlyUseAppList){
//                appGroupList.remove(0);
//            }
            appListAdapter = new AppListAdapter(appGroupList);
            appListAdapter.registerDataSetObserver(dataSetObserver);
            appListView.setAdapter(appListAdapter);
            appListAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 设置标题，根据当前Fragment类名获取显示名称
     */
    private void setTabTitle() {
        String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        if (!StringUtils.isBlank(appTabs)) {
            ((TextView) rootView.findViewById(R.id.tv_header)).setText(AppTabUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
        }
    }


    /**
     * 更推荐应用小部件信息
     * 从MyAppWidgetUtils发送通知而来
     *
     * @param getRecommendAppWidgetListResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateRecommendAppWidegtList(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult) {
        refreshRecommendAppWidgetView();
    }

    /**
     * 更新常用应用数据
     * 来自AppDetailActivity打开应用
     *
     * @param app
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCommonlyUseAppList(App app) {
        saveOrChangeCommonlyUseAppList(app, appListAdapter.getAppAdapterList());
    }

    /**
     * 客户端统一配置版本更新
     *
     * @param getAllConfigVersionResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClientConfigVersionUpdate(final GetAllConfigVersionResult getAllConfigVersionResult) {
        boolean isMyAppUpdate = ClientConfigUpdateUtils.getInstance().isItemNeedUpdate(ClientConfigItem.CLIENT_CONFIG_MY_APP, getAllConfigVersionResult);
        if (isMyAppUpdate) {
            getMyApp();
        }
    }

    /**
     * app页网络异常提示框
     *
     * @param netState 通过Action获取操作类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netWorkStateHint(SimpleEventMessage netState) {
        if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT)) {   //网络异常提示
            if ((boolean) netState.getMessageObj()) {
                DeleteHeaderView();
            } else {
                AddHeaderView();
            }
        }
    }

    /**
     * 获取我的apps
     */
    private void getMyApp() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            String saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_MY_APP);
            apiService.getUserApps(saveConfigVersion);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 注册添加应用广播
     */
    private void registerReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION_NAME)) {
                    getMyApp();
                    (rootView.findViewById(R.id.bt_sort_finish)).setVisibility(View.GONE);
                    configBtn.setVisibility(View.VISIBLE);
                    appListAdapter.setCanEdit(false);
                    appListAdapter.notifyDataSetChanged();
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        // 注册广播
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    /**
     * 点击应用后处理常用应用
     *
     * @param app
     * @param appAdapterList
     */
    private void saveOrChangeCommonlyUseAppList(App app, List<AppGroupBean> appAdapterList) {
        List<AppCommonlyUse> appCommonlyUseAddCountList = addClickCount(app);
        calculateAppWeight(appCommonlyUseAddCountList);
        if (getNeedCommonlyUseApp()) {
            showCommonlyUseApps(app, appAdapterList);
        }
    }

    /**
     * 展示常用应用
     *
     * @param app
     * @param appAdapterList
     */
    private void showCommonlyUseApps(App app,
                                     List<AppGroupBean> appAdapterList) {
        if (AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0 && appAdapterList.size() > appListSizeExceptCommonlyUse) {
            //如果已经有了常用app则需要先移除掉第一组
            appAdapterList.remove(0);
            handCommonlyUseAppData(appAdapterList, true);
        } else {
            AppGroupBean appGroupBean = new AppGroupBean();
            appGroupBean.setCategoryID("commonly");
            appGroupBean.setCategoryName(getString(R.string.commoly_use_app));
            List<App> commonlyUseAppList = new ArrayList<App>();
            commonlyUseAppList.add(app);
            appGroupBean.setAppItemList(commonlyUseAppList);
            appAdapterList.add(0, appGroupBean);
            appListAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 根据前面计算出的点击次数和当前位置计算权重
     *
     * @param appCommonlyUseAddCountList
     * @return
     */
    private List<AppCommonlyUse> calculateAppWeight(List<AppCommonlyUse> appCommonlyUseAddCountList) {
        int appCommonlyUseListSize = appCommonlyUseAddCountList.size();
        List<AppCommonlyUse> appCommonlyUseList = null;
        for (int i = 0; i < appCommonlyUseListSize; i++) {
            AppCommonlyUse appCommonlyUseWeight = appCommonlyUseAddCountList.get(i);
            int count = appCommonlyUseWeight.getClickCount();
            int index = appCommonlyUseAddCountList.indexOf(appCommonlyUseWeight);
            double weight = 0.6 * count + (0.4 * 10 * (1 - ((double) index) / ((double) appCommonlyUseListSize)));
            appCommonlyUseAddCountList.get(i).setWeight(weight);
        }
        Collections.sort(appCommonlyUseAddCountList, new SortCommonlyUseApp());
        AppCacheUtils.saveAppCommonlyUseList(getActivity(), appCommonlyUseAddCountList);
        if (appCommonlyUseAddCountList.size() > 8) {
            appCommonlyUseList = appCommonlyUseAddCountList.subList(0, 8);
            return appCommonlyUseList;
        }
        return appCommonlyUseAddCountList;
    }

    /**
     * 点击应用后clickCount加1
     *
     * @param app
     * @return
     */
    private List<AppCommonlyUse> addClickCount(App app) {
        List<AppCommonlyUse> appCommonlyUseList = AppCacheUtils.getCommonlyUseList(getActivity());
        AppCommonlyUse appCommonlyUse = new AppCommonlyUse();
        appCommonlyUse.setAppID(app.getAppID());
        int index = appCommonlyUseList.indexOf(appCommonlyUse);
        if (index != -1) {
            AppCommonlyUse appCommonlyUseInTable = appCommonlyUseList.get(index);
            int count = appCommonlyUseInTable.getClickCount();
            appCommonlyUseList.get(index).setClickCount(count + 1);
        } else {
            AppCommonlyUse appCommonlyUseNew = new AppCommonlyUse();
            appCommonlyUseNew.setClickCount(1);
            appCommonlyUseNew.setAppID(app.getAppID());
            appCommonlyUseNew.setWeight(0);
            appCommonlyUseNew.setLastUpdateTime(System.currentTimeMillis());
            appCommonlyUseList.add(appCommonlyUseNew);
        }
        return appCommonlyUseList;
    }

    /**
     * 处理常用应用的改变
     *
     * @param appAdapterList
     */
    private void deleteCommonlyUseApp(List<AppGroupBean> appAdapterList,
                                      App app) {
        List<App> commonlyAppItemList = appAdapterList.get(0)
                .getAppItemList();
        if (getNeedCommonlyUseApp() && (commonlyAppItemList.indexOf(app) != -1)) {
            commonlyAppItemList.remove(app);
            List<App> appList = AppCacheUtils.getCommonlyUseNeedShowList(getActivity());
            appAdapterList.get(0)
                    .setAppItemList(appList.size() > 8 ? appList.subList(0, 8) : appList);
        }
        Iterator<AppGroupBean> appGroupBeanList = appAdapterList.iterator();
        while (appGroupBeanList.hasNext()) {
            AppGroupBean appGroupBean = appGroupBeanList.next();
            if (appGroupBean.getAppItemList().size() == 0) {
                appGroupBeanList.remove();
            }
        }
        appListAdapter.setAppAdapterList(appAdapterList);
    }

    /**
     * 排序之后进行的操作
     *
     * @param appGroupItemList
     * @param from
     * @param to
     */
    protected void handAppOrderChange(List<App> appGroupItemList, int from, int to) {
        App temp = appGroupItemList.get(from);
        // 这里的处理需要注意下
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(appGroupItemList, i, i + 1);
            }
        } else if (from > to) {
            for (int i = from; i > to; i--) {
                Collections.swap(appGroupItemList, i, i - 1);
            }
        }
        appGroupItemList.set(to, temp);
    }

    //接收从AppBadgeUtils里发回的角标数字
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAppBadgeNum(BadgeBodyModel badgeBodyModel) {
        BadgeBodyModuleModel badgeBodyModuleModel = badgeBodyModel.getAppStoreBadgeBodyModuleModel();
        appStoreBadgeMap = badgeBodyModuleModel.getDetailBodyMap();
        appListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        //为解决切换tab卡死的bug
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (myAppSaveTask != null && !myAppSaveTask.isCancelled()
                && myAppSaveTask.getStatus() == AsyncTask.Status.RUNNING) {
            myAppSaveTask.cancel(true);
            myAppSaveTask = null;
        }
        if (appListAdapter != null) {
            appListAdapter.unregisterDataSetObserver(dataSetObserver);
            dataSetObserver = null;
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 应用操作窗口
     * 包括编辑应用（点击后在非必装应用上显示叉号，点叉号删除，长按挪动位置）
     * 和是否显示常用应用
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        // 一个自定义的布局，作为popwindowivew显示的内容
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.app_center_popup_window_view, null);
//        final SwitchView switchView = contentView.findViewById(R.id.switch_view_common_app);
        commonlyUseLayout = contentView.findViewById(R.id.ll_common_app_switch);
        //为了在打开PopWindow时立刻显示当前状态
//        switchView.setOpened(getNeedCommonlyUseApp());
        // 设置按钮的点击事件
        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
//        switchView.setOnStateChangedListener(new OnStateChangedListener() {
//            @Override
//            public void toggleToOn(View view) {
//                if (view == null || switchView == null) {
//                    return;
//                }
//                switchView.toggleSwitch(true);
//                saveNeedCommonlyUseApp(true);
//                handCommonlyUseAppData(appListAdapter.getAppAdapterList(), true);
//            }
//
//            @Override
//            public void toggleToOff(View view) {
//                if (view == null || switchView == null) {
//                    return;
//                }
//                switchView.toggleSwitch(false);
//                if (getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0) {
//                    appListAdapter.getAppAdapterList().remove(0);
//                    appListAdapter.notifyDataSetChanged();
//                }
//                saveNeedCommonlyUseApp(false);
//            }
//        });
        setCommonlyUseIconAndText();
        (contentView.findViewById(R.id.ll_app_order)).setOnClickListener(myOnClickListener);
        (contentView.findViewById(R.id.ll_common_app_switch)).setOnClickListener(myOnClickListener);
        (contentView.findViewById(R.id.ll_app_scan)).setOnClickListener(myOnClickListener);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        backgroundAlpha(0.8f);
        popupWindow.showAsDropDown(view);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 存储是否需要显示常用app
     *
     * @param isNeedCommonlyUseApp
     */
    private void saveNeedCommonlyUseApp(boolean isNeedCommonlyUseApp) {
        String userId = ((MyApplication) getActivity().getApplication()).getUid();
        PreferencesUtils.putBoolean(getActivity(), MyApplication.getInstance().getTanent()
                        + userId + "needCommonlyUseApp",
                isNeedCommonlyUseApp);
    }

    /**
     * 获取是否需要显示常用app
     *
     * @return
     */
    private boolean getNeedCommonlyUseApp() {
        String userId = MyApplication.getInstance().getUid();
        return PreferencesUtils.getBoolean(getActivity(), MyApplication.getInstance().getTanent()
                + userId + "needCommonlyUseApp", true);
    }

    /**
     * 存储App顺序
     *
     * @param changeId
     */
    private void saveAppChangeOrder(int changeId) {
        List<AppGroupBean> appGroupList = appListAdapter.getAppAdapterList();
        int appOrderSize = appGroupList.get(changeId).getAppItemList().size();
        List<App> appItemList = appGroupList.get(changeId).getAppItemList();
        List<AppOrder> appOrderList = new ArrayList<AppOrder>();
        for (int j = 0; j < appOrderSize; j++) {
            AppOrder appOrder = new AppOrder();
            appOrder.setAppID(appItemList.get(j).getAppID());
            appOrder.setCategoryID(appItemList.get(j).getCategoryID());
            appOrder.setOrderId(j + "");
            appOrderList.add(appOrder);
        }
        AppCacheUtils.saveAppOrderList(getActivity(), appOrderList,
                appGroupList.get(changeId).getCategoryID());
    }

    /**
     * 获取到网络数据后对排序和显示进行处理
     *
     * @param appGroupList
     * @return
     */
    public List<AppGroupBean> handleAppList(List<AppGroupBean> appGroupList) {
        List<AppOrder> appOrderList = AppCacheUtils
                .getAllAppOrderList(getActivity());
        int appListSize = appGroupList.size();
        for (int i = 0; i < appListSize; i++) {
            List<App> appItemList = appGroupList.get(i).getAppItemList();
            int appGroupSize = appItemList.size();
            for (int j = 0; j < appGroupSize; j++) {
                App app = appItemList.get(j);
                AppOrder appOrderCache = new AppOrder();
                appOrderCache.setAppID(app.getAppID());
                int index = appOrderList.indexOf(appOrderCache);
                if (index != -1) {
                    app.setOrderId(Integer.parseInt(appOrderList.get(index).getOrderId()));
                }
            }
            Collections.sort(appGroupList.get(i).getAppItemList(),
                    new SortAppClass());
        }
        handCommonlyUseAppData(appGroupList, false);
        return appGroupList;
    }

    /**
     * 处理应用加载数据时常用应用部分
     *
     * @param appGroupList
     */
    private void handCommonlyUseAppData(List<AppGroupBean> appGroupList, boolean isNeedRefresh) {
        List<AppCommonlyUse> appCommonlyUseList =
                AppCacheUtils.getCommonlyUseList(getActivity());//这里换成获取所有
        if (appCommonlyUseList.size() > 0 && getNeedCommonlyUseApp()) {
            AppGroupBean appGroupBean = new AppGroupBean();
            appGroupBean.setCategoryID("commonly");
            appGroupBean.setCategoryName(getString(R.string.commoly_use_app));
            List<App> myCommonlyUseAppList = new ArrayList<App>();
            for (int i = 0; i < appGroupList.size(); i++) {
                List<App> appItemList = appGroupList.get(i).getAppItemList();
                int appGroupSize = appItemList.size();
                for (int j = 0; j < appGroupSize; j++) {
                    App app = appItemList.get(j);
                    AppCommonlyUse appCommonlyUse = new AppCommonlyUse();
                    appCommonlyUse.setAppID(app.getAppID());
                    int index = appCommonlyUseList.indexOf(appCommonlyUse);
                    int allreadHas = myCommonlyUseAppList.indexOf(app);
                    if (index != -1 && allreadHas == -1) {
                        AppCommonlyUse appCommonlyUseTemp = appCommonlyUseList.get(index);
                        app.setWeight(appCommonlyUseTemp.getWeight());
                        myCommonlyUseAppList.add(app);
                    }
                }
            }
            //先排序再取前四个
            Collections.sort(myCommonlyUseAppList, new SortCommonlyUseAppClass());
            if (myCommonlyUseAppList.size() > 8) {
                myCommonlyUseAppList = myCommonlyUseAppList.subList(0, 8);
            }
            //取完前四个再排序一次
            Collections.sort(myCommonlyUseAppList, new SortCommonlyUseAppClass());
            //需要调试常用应用权重时解开
//            for (int i = 0; i < myCommonlyUseAppList.size(); i++) {
//                LogUtils.YfcDebug("app名称：" + myCommonlyUseAppList.get(i).getAppName() + "常用应用的权重" + myCommonlyUseAppList.get(i).getWeight());
//            }
            if (myCommonlyUseAppList.size() > 0) {
                appGroupBean.setAppItemList(myCommonlyUseAppList);
                appGroupList.add(0, appGroupBean);
                saveNeedCommonlyUseApp(true);
            }
        }
        if (isNeedRefresh) {
            appListAdapter.notifyDataSetChanged();
            MyAppCacheUtils.saveMyAppList(getActivity(), appListAdapter.getAppAdapterList());
        }
    }

    /**
     * 判断是否连点
     *
     * @return
     */
    private boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastOnItemClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastOnItemClickTime = time;
        return false;

    }

    private void setCommonlyUseIconAndText() {
        if (popupWindow != null && commonlyUseLayout != null) {
            ImageView imageView = commonlyUseLayout.findViewById(R.id.iv_app_commonly_use);
            TextView textView = commonlyUseLayout.findViewById(R.id.tv_app_commonly_use);
            imageView.setImageResource(getNeedCommonlyUseApp() ? R.drawable.ic_commonly_use_open : R.drawable.ic_commonly_use_close);
            textView.setText(getNeedCommonlyUseApp() ? R.string.app_commonly_use_close : R.string.app_commonly_use);
        }
    }

    private void setAppEditStatus(boolean isEditStatus) {
        sortFinishBtn.setVisibility(isEditStatus ? View.VISIBLE : View.GONE);
        configBtn.setVisibility(isEditStatus ? View.GONE : View.VISIBLE);
        appcenterEnterBtn.setVisibility(isEditStatus ? View.GONE : View.VISIBLE);
    }

    /**
     * 创建快捷方式的Dialog
     *
     * @param app
     * @param appType
     * @param clz
     * @param icon
     */
    private void showCreateShortCutDialog(final App app, final String appType, final Class clz, final int icon, final Bitmap bitmap) {
        final Dialog hasIntrcutionDialog = new Dialog(getActivity(),
                R.style.transparentFrameWindowStyle);
        hasIntrcutionDialog.setCanceledOnTouchOutside(true);
        View view = getActivity().getLayoutInflater().inflate(R.layout.app_create_shortcut_dialog, null);
        hasIntrcutionDialog.setContentView(view);
        final TextView textView = (TextView) view.findViewById(R.id.news_has_instrcution_text);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.shortcut_dialog_checkbox);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setText(getString(R.string.app_commonly_use_app));
        Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (icon == 0) {
                    ShortCutUtils.createShortCut(getActivity(), clz,
                            app.getAppName(), app.getUri(), appType, bitmap);
                }
                if (bitmap == null) {
                    ShortCutUtils.createShortCut(getActivity(), clz,
                            app.getAppName(), app.getUri(), appType, icon);
                }
                UriUtils.openApp(getActivity(), app, "application");
                hasIntrcutionDialog.dismiss();
            }
        });
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    PreferencesByUserAndTanentUtils.putBoolean(getActivity(), "need_create_shortcut" + app.getAppID(), false);
                }
                UriUtils.openApp(getActivity(), app, "application");
                hasIntrcutionDialog.dismiss();
            }
        });
        Window window = hasIntrcutionDialog.getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.dimAmount = 0.31f;
        hasIntrcutionDialog.getWindow().setAttributes(wl);
        hasIntrcutionDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        hasIntrcutionDialog.show();
    }

    /**
     * 在应用ListView中获取当前分组是否是常用应用分组，传入参数为当前分组的位置，返回是否常用应用分组
     *
     * @param listPosition
     * @return
     */
    private boolean getIsCommonlyUseGroupInList(int listPosition) {
        return (listPosition == 0) && getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0;
    }

    /**
     * App组列表Adapter
     */
    public class AppListAdapter extends BaseAdapter {
        private List<AppGroupBean> appAdapterList = new ArrayList<AppGroupBean>();
        private boolean canEdit = false;

        public AppListAdapter(List<AppGroupBean> appAdapterList) {
            this.appAdapterList = appAdapterList;
        }

        @Override
        public int getCount() {
            return appAdapterList == null ? 0 : appAdapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int listPosition, View convertView,
                            ViewGroup parent) {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.app_drag_item, null);
            if (listPosition == (getCount() - 1)) {
                View dividerView = (View) convertView.findViewById(R.id.v_applist_devid);
                dividerView.setVisibility(View.GONE);
            }
            ((TextView) convertView.findViewById(R.id.app_title_text))
                    .setText(appAdapterList.get(listPosition).getCategoryName());
            DragGridView dragGridView = (DragGridView) convertView
                    .findViewById(R.id.app_list_draggrid);
            final List<App> appGroupItemList = appAdapterList.get(
                    listPosition).getAppItemList();
            final DragAdapter dragGridViewAdapter = new DragAdapter(
                    getActivity(), appGroupItemList, listPosition, appStoreBadgeMap);
            dragGridViewAdapter.setCommonlyUseGroup(getIsCommonlyUseGroupInList(listPosition));
            dragGridView.setCanScroll(false);
            dragGridView.setPosition(listPosition);
            dragGridView.setPullRefreshLayout(swipeRefreshLayout);
            dragGridViewAdapter.setGroupPosition(listPosition);
            dragGridView.setOnChangeListener(new OnChanageListener() {
                @Override
                public void onChange(int listPosition, int from, int to) {
                    handAppOrderChange(appGroupItemList, from, to);
                    dragGridViewAdapter.notifyDataSetChanged();
                    //去掉在排序完成之后的刷新，这里不影响删除应用相关的逻辑
                    saveAppChangeOrder(listPosition);
                }
            });
            dragGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (!canEdit) {
                        App app = appGroupItemList.get(position);
                        if (NetUtils.isNetworkConnected(getActivity()) && !isFastDoubleClick()) {
                            if (app.getSubAppList().size() > 0) {
                                Intent intent = new Intent();
                                intent.setClass(getActivity(), AppGroupActivity.class);
                                intent.putExtra("categoryName", app.getAppName());
                                intent.putExtra("appGroupList", (Serializable) app.getSubAppList());
                                startActivity(intent);
                            } else {
                                if (getIsCommonlyUseGroupInList(listPosition)) {
                                    UriUtils.openApp(getActivity(), app, "commonapplications");
                                } else {
                                    UriUtils.openApp(getActivity(), app, "application");
                                }
                            }

                        }
                        if (getNeedCommonlyUseApp()) {
                            saveOrChangeCommonlyUseAppList(app, appAdapterList);
                            MyAppCacheUtils.saveMyAppList(getContext(), appAdapterList);
                        }
                    }
                }
            });
            dragGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {
                    if (!canEdit) {
                        appListAdapter.setCanEdit(true);
                        appListAdapter.notifyDataSetChanged();
                        setAppEditStatus(true);
                        canEdit = true;
                        Vibrator mVibrator = (Vibrator) getActivity()
                                .getSystemService(Context.VIBRATOR_SERVICE);
                        mVibrator.vibrate(50);
                    }
                    return false;
                }
            });
            dragGridViewAdapter
                    .setNotifyCommonlyUseListener(new DragAdapter.NotifyCommonlyUseListener() {
                        @Override
                        public void onNotifyCommonlyUseApp(App app) {
                            deleteCommonlyUseApp(appAdapterList, app);
                            new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
                            appListAdapter.notifyDataSetChanged();
                            dragGridViewAdapter.notifyDataSetChanged();
                            MyAppCacheUtils.saveMyAppList(getActivity(), appListAdapter.getAppAdapterList());
                        }
                    });
            if (canEdit) {
                //控制常用应用是否可以晃动删除
//                if ((listPosition == 0) && getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0) {
//                    //如果应用列表可以编辑，并且有常用应用分组，则把常用应用的可编辑属性设置false（也就是第0行设为false）
//                    dragGridViewAdapter.setCanEdit(false);
//                } else {
                //如果应用列表可以编辑，不是常用应用分组
                dragGridViewAdapter.setCanEdit(true);
                dragGridView.setCanEdit(true);
//                }
            } else {
                //如果不能编辑则把adapter和View的属性都设置为false
                dragGridViewAdapter.setCanEdit(false);
                dragGridView.setCanEdit(false);
            }
            dragGridView.setAdapter(dragGridViewAdapter);
            return convertView;
        }

        /**
         * 获取当前listview中的的list
         *
         * @return
         */
        public List<AppGroupBean> getAppAdapterList() {
            return appAdapterList;
        }

        /**
         * 设置AppAdapter
         */
        public void setAppAdapterList(List<AppGroupBean> list) {
            appAdapterList = list;
            notifyDataSetChanged();
        }

        /**
         * 获取是否可以删除
         */
        public boolean getCanEdit() {
            return this.canEdit;
        }

        /**
         * 设置是否可以删除
         */
        public void setCanEdit(boolean canDelete) {
            this.canEdit = canDelete;
        }
    }

    /**
     * 应用顺序排序接口，比较orderId
     */
    public class SortAppClass implements Comparator {
        public int compare(Object arg0, Object arg1) {
            App appItemA = (App) arg0;
            App appItemB = (App) arg1;
            int appSortA = appItemA.getOrderId();
            int appSortB = appItemB.getOrderId();
            if (appSortA > appSortB) {
                return 1;
            } else if (appSortA < appSortB) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * 应用排序接口，比较权重，用于展示APP
     */
    public class SortCommonlyUseAppClass implements Comparator {
        public int compare(Object arg0, Object arg1) {
            App appItemA = (App) arg0;
            App appItemB = (App) arg1;
            double appSortA = appItemA.getWeight();
            double appSortB = appItemB.getWeight();
            if (appSortA == 0 || appSortB == 0) {
                return -1;
            }
            if (appSortA > appSortB) {
                return -1;
            } else if (appSortA < appSortB) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 常用应用排序接口，比较权重，用于存储常用app
     */
    public class SortCommonlyUseApp implements Comparator {
        public int compare(Object arg0, Object arg1) {
            AppCommonlyUse appItemA = (AppCommonlyUse) arg0;
            AppCommonlyUse appItemB = (AppCommonlyUse) arg1;
            double appSortA = appItemA.getWeight();
            double appSortB = appItemB.getWeight();
            if (appSortA == 0 || appSortB == 0) {
                return -1;
            }
            if (appSortA > appSortB) {
                return -1;
            } else if (appSortA < appSortB) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    class MyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibt_appcenter_enter:
                    IntentUtils.startActivity(getActivity(), AppCenterActivity.class);
                    PVCollectModelCacheUtils.saveCollectModel("appcenter", "application");
                    break;
                case R.id.ibt_appcenter_config:
                    showPopupWindow(v);
                    break;
                case R.id.bt_sort_finish:
                    appListAdapter.setCanEdit(false);
                    appListAdapter.notifyDataSetChanged();
                    setAppEditStatus(false);
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    break;
                case R.id.ll_app_order:
                    if (popupWindow != null && appListAdapter != null) {
                        appListAdapter.setCanEdit(true);
                        appListAdapter.notifyDataSetChanged();
                        setAppEditStatus(true);
                        popupWindow.dismiss();
                    }
                    break;
                case R.id.ll_common_app_switch:
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    if (!getNeedCommonlyUseApp()) {
                        saveNeedCommonlyUseApp(true);
                        handCommonlyUseAppData(appListAdapter.getAppAdapterList(), true);
                        setCommonlyUseIconAndText();
                    } else {
                        if (getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0) {
                            appListAdapter.getAppAdapterList().remove(0);
                            appListAdapter.notifyDataSetChanged();
                        }
                        saveNeedCommonlyUseApp(false);
                        setCommonlyUseIconAndText();
                    }
                    break;
                case R.id.ll_app_scan:
                    AppUtils.openScanCode(MyAppFragment.this, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                    break;
            }

        }
    }

    class MyAppSaveTask extends AsyncTask<GetAppGroupResult, Void, List<AppGroupBean>> {
        private String clientConfigMyAppVersion;

        public MyAppSaveTask(String clientConfigMyAppVersion) {
            this.clientConfigMyAppVersion = clientConfigMyAppVersion;
        }

        @Override
        protected List<AppGroupBean> doInBackground(GetAppGroupResult... params) {
            try {
                List<AppGroupBean> appGroupList = handleAppList((params[0])
                        .getAppGroupBeanList());
                MyAppCacheUtils.saveMyAppList(getActivity(), appGroupList);
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_MY_APP, clientConfigMyAppVersion);
                return appGroupList;
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

        }

        @Override
        protected void onPostExecute(List<AppGroupBean> appGroupList) {
            super.onPostExecute(appGroupList);
            if (!hasRequestBadgeNum) {
                new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
            }
            appListAdapter.setAppAdapterList(appGroupList);
            swipeRefreshLayout.setRefreshing(false);
            refreshRecommendAppWidgetView();
        }
    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnUserAppsSuccess(final GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion) {
            swipeRefreshLayout.setRefreshing(false);
            myAppSaveTask = new MyAppSaveTask(clientConfigMyAppVersion);
            myAppSaveTask.execute(getAppGroupResult);
            appListSizeExceptCommonlyUse = getAppGroupResult.getAppGroupBeanList().size();
        }

        @Override
        public void returnUserAppsFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            //          WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }


    }
}
