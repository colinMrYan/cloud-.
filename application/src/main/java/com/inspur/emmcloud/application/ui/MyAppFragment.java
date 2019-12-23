package com.inspur.emmcloud.application.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.adapter.RecommendAppWidgetListAdapter;
import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.AppCommonlyUse;
import com.inspur.emmcloud.application.bean.AppGroupBean;
import com.inspur.emmcloud.application.bean.AppOrder;
import com.inspur.emmcloud.application.bean.GetAppGroupResult;
import com.inspur.emmcloud.application.bean.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.application.bean.RecommendAppWidgetBean;
import com.inspur.emmcloud.application.interf.OnRecommendAppWidgetItemClickListener;
import com.inspur.emmcloud.application.util.AppCacheUtils;
import com.inspur.emmcloud.application.util.AppConfigCacheUtils;
import com.inspur.emmcloud.application.util.ApplicationUriUtils;
import com.inspur.emmcloud.application.util.MyAppCacheUtils;
import com.inspur.emmcloud.application.util.MyAppWidgetUtils;
import com.inspur.emmcloud.application.widget.DragAdapter;
import com.inspur.emmcloud.application.widget.DragGridView;
import com.inspur.emmcloud.application.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.baselib.widget.popmenu.DropPopMenu;
import com.inspur.emmcloud.baselib.widget.popmenu.MenuItem;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModuleModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppBadgeUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.CheckingNetStateUtils;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.ScanQrCodeUtils;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;

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
    private ApplicationAPIService apiService;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private PopupWindow popupWindow;
    private MyAppSaveTask myAppSaveTask;
    private Map<String, Integer> appStoreBadgeMap = new HashMap<>();
    private RecyclerView recommendAppWidgetListView = null;
    private RecommendAppWidgetListAdapter recommendAppWidgetListAdapter = null;
    private DataSetObserver dataSetObserver;
    private View netExceptionView;
    private boolean haveHeader = false;
    private MyOnClickListener myOnClickListener;
    private LinearLayout commonlyUseLayout;

    private CheckingNetStateUtils checkingNetStateUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkingNetStateUtils = new CheckingNetStateUtils(getContext(), NetUtils.pingUrls, NetUtils.httpUrls);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_app, null);
        copyData();
        initViews();
        registerReceiver();
        EventBus.getDefault().register(this);
    }

    /**
     * 如果存在旧数据读取旧数据，转存并清除旧数据（3.1.3升级4.0.1发现的问题）
     */
    private void copyData() {
        if (PreferencesByUserAndTanentUtils.isKeyExist(getActivity(), "my_app_list")) {
            String myAppList = PreferencesByUserAndTanentUtils.getString(getActivity(), "my_app_list");
            if (!StringUtils.isBlank(myAppList)) {
                List<AppGroupBean> appGroupList = JSONUtils.parseArray(myAppList, AppGroupBean.class);
                if (appGroupList.size() > 0) {
                    if (appGroupList.get(0).getCategoryID().equals("commonly")) {
                        appGroupList.remove(0);
                    }
                    MyAppCacheUtils.saveMyAppListFromNet(getActivity(), appGroupList);
                }
            }
            PreferencesByUserAndTanentUtils.clearDataByKey(getActivity(), "my_app_list");
        }
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
        if (MyAppCacheUtils.getMyAppListFromNet(getActivity()).size() > 0) {
            new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
        }
        refreshRecommendAppWidgetView();
        checkingNetStateUtils.getNetStateResult(5);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        netExceptionView = LayoutInflater.from(getContext()).inflate(R.layout.header_error, null);
        netExceptionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Router router = Router.getInstance();
                if (router.getService(CommunicationService.class) != null) {
                    CommunicationService service = router.getService(CommunicationService.class);
                    service.startNetWorkStateActivity(getActivity());
                }
            }
        });
        apiService = new ApplicationAPIService(getActivity());
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
        if (getActivity() == null) {
            return;
        }
        //判断时间是否点击了叉号，不在显示时间内，或者推荐应用已经过了有效期
        if (!(MyAppWidgetUtils.isNeedShowMyAppRecommendWidgets(BaseApplication.getInstance())) ||
                !MyAppWidgetUtils.isEffective(PreferencesByUserAndTanentUtils.getLong(getContext()
                        , Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE, 0L))) {
            (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
            return;
        }
        //是否是需要刷新的时间，即过了当前小时内appId的显示时间，这是只控制刷新，不控制显示隐藏，MyAPPFragment Destroy时会重置这个时间，使下次进入时不会影响刷新UI
        boolean isRefreshTime = PreferencesByUserAndTanentUtils.getInt(BaseApplication.getInstance(), Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR, -1) != MyAppWidgetUtils.getNowHour();
        if (!isRefreshTime) {
            return;
        }
        GetRecommendAppWidgetListResult getRecommendAppWidgetListResult = new GetRecommendAppWidgetListResult(PreferencesByUserAndTanentUtils.
                getString(BaseApplication.getInstance(), Constant.PREF_MY_APP_RECOMMEND_DATA, ""));
        List<RecommendAppWidgetBean> recommendAppWidgetBeanList = getRecommendAppWidgetListResult.getRecommendAppWidgetBeanList();
        List<App> appList = MyAppWidgetUtils.getShouldShowAppList(recommendAppWidgetBeanList, appListAdapter.getAppAdapterList());
        if (appList.size() > 0) {
            if (recommendAppWidgetListView == null) {
                recommendAppWidgetListView = (RecyclerView) rootView.findViewById(R.id.my_app_recommend_app_wiget_recyclerview);
                (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.VISIBLE);
                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                manager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recommendAppWidgetListView.setLayoutManager(manager);
                recommendAppWidgetListView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(getActivity(), 4)));
                recommendAppWidgetListAdapter = new RecommendAppWidgetListAdapter(getActivity());
                recommendAppWidgetListView.setAdapter(recommendAppWidgetListAdapter);
                recommendAppWidgetListAdapter.setOnRecommendAppWidgetItemClickListener(new OnRecommendAppWidgetItemClickListener() {
                    @Override
                    public void onRecommendAppWidgetItemClick(App app) {
                        ApplicationUriUtils.openApp(getActivity(), app, "smartrecommend");
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
                new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
            }
        });
    }


    /**
     * 初始化AppListView，加载缓存数据
     */
    private void refreshAppListView() {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(getContext());
        List<AppGroupBean> appGroupFromNetList = MyAppCacheUtils.getMyAppListFromNet(getContext());
        if (appGroupList.size() > 0 && (appGroupList.size() != appGroupFromNetList.size()) && !MyAppCacheUtils.getNeedCommonlyUseApp()) {
            appGroupList.remove(0);
        }
        handleAppOrder(appGroupList);
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
            ((TextView) rootView.findViewById(R.id.tv_header)).setText(getHeaderText(getClass().getSimpleName()));
        }
    }

    private String getHeaderText(String simpleName) {
        String headerText = "";
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            headerText = service.getMyAppFragmentHeaderText(simpleName);
        }
        return headerText;
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
            if ((boolean) netState.getMessageObj()
                    || NetworkInfo.State.CONNECTED == NetUtils.getNetworkMobileState(getActivity())
                    || NetUtils.isVpnConnected()) {
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
        if (MyAppCacheUtils.getNeedCommonlyUseApp()) {
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
        if (getNeedRemoveCommonlyUseGroup()) {
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
        if (MyAppCacheUtils.getNeedCommonlyUseApp() && (commonlyAppItemList.indexOf(app) != -1)) {
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
        final DropPopMenu dropPopMenu = new DropPopMenu(getActivity());
        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 1:
                        AppUtils.openScanCode(MyAppFragment.this, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                        break;
                    case 2:
                        if (appListAdapter != null) {
                            appListAdapter.setCanEdit(true);
                            appListAdapter.notifyDataSetChanged();
                            setAppEditStatus(true);
                        }
                        break;
                    case 3:
                        if (!MyAppCacheUtils.getNeedCommonlyUseApp()) {
                            MyAppCacheUtils.saveNeedCommonlyUseApp(true);
                            handCommonlyUseAppData(appListAdapter.getAppAdapterList(), true);
                        } else {
                            if (getNeedRemoveCommonlyUseGroup()) {
                                appListAdapter.getAppAdapterList().remove(0);
                                appListAdapter.notifyDataSetChanged();
                            }
                            MyAppCacheUtils.saveNeedCommonlyUseApp(false);
                        }
                        break;
                    case 4:
                        break;
                    default:
                        break;
                }
            }
        });
        List<MenuItem> list = getAddMenuList();
        dropPopMenu.setMenuList(list);
        dropPopMenu.show(view);
    }

    private List<MenuItem> getAddMenuList() {
        List<MenuItem> menuItemList = new ArrayList<>();//
        menuItemList.add(new MenuItem(R.drawable.ic_message_menu_scan_black, 1, getActivity().getString(R.string.sweep)));
        menuItemList.add(new MenuItem(R.drawable.ic_change_app_order, 2, getString(R.string.application_app_sort_order)));
        boolean isOpenCommAppFromSer = AppConfigCacheUtils.getAppConfigValue(getContext(), "EnableCommonFunction", "true").equals("true");
        if (isOpenCommAppFromSer) {
            menuItemList.add(new MenuItem(MyAppCacheUtils.getNeedCommonlyUseApp() ? R.drawable.ic_commonly_use_open : R.drawable.ic_commonly_use_close
                    , 3, getActivity().getString(MyAppCacheUtils.getNeedCommonlyUseApp() ? R.string.application_app_commonly_use_close : R.string.application_app_commonly_use)));
        }
        return menuItemList;
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

    public void handleAppOrder(List<AppGroupBean> appGroupList) {
        List<AppOrder> appOrderList = AppCacheUtils.getAllAppOrderList(getActivity());
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
            Collections.sort(appGroupList.get(i).getAppItemList(), new SortAppClass());
        }
    }

    /**
     * 获取到网络数据后对排序和显示进行处理
     *
     * @param appGroupList
     * @return
     */
    public List<AppGroupBean> handleAppList(List<AppGroupBean> appGroupList) {
        handleAppOrder(appGroupList);
        handCommonlyUseAppData(appGroupList, false);
        return appGroupList;
    }

    /**
     * 处理应用加载数据时常用应用部分
     *
     * @param appGroupList
     */
    private void handCommonlyUseAppData(List<AppGroupBean> appGroupList, boolean isNeedRefresh) {
        if (MyAppCacheUtils.getNeedCommonlyUseApp()) {
            AppGroupBean appGroupBean = MyAppCacheUtils.getCommonlyUserAppGroup();
            if (appGroupBean != null) {
                appGroupList.add(0, appGroupBean);
                MyAppCacheUtils.saveNeedCommonlyUseApp(true);
            }
        }
        if (isNeedRefresh) {
            appListAdapter.notifyDataSetChanged();
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

    private void setAppEditStatus(boolean isEditStatus) {
        sortFinishBtn.setVisibility(isEditStatus ? View.VISIBLE : View.GONE);
        configBtn.setVisibility(isEditStatus ? View.GONE : View.VISIBLE);
        appcenterEnterBtn.setVisibility(isEditStatus ? View.GONE : View.VISIBLE);
    }

    /**
     * 在应用ListView中获取当前分组是否是常用应用分组，传入参数为当前分组的位置，返回是否常用应用分组
     *
     * @param listPosition
     * @return
     */
    private boolean getIsCommonlyUseGroupInList(int listPosition) {
        return (listPosition == 0) && MyAppCacheUtils.getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0;
    }

    /**
     * 判断是否需要移除常用应用分组
     *
     * @return
     */
    private boolean getNeedRemoveCommonlyUseGroup() {
        return MyAppCacheUtils.getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0
                && (MyAppCacheUtils.getMyAppListFromNet(getActivity()).size() != appListAdapter.getCount());
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
            dragGridView.setOnChangeListener(new DragGridView.OnChanageListener() {
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
                        if (!isFastDoubleClick()) {
                            if (app.getSubAppList().size() > 0) {
                                Intent intent = new Intent();
                                intent.setClass(getActivity(), AppGroupActivity.class);
                                intent.putExtra("categoryName", app.getAppName());
                                intent.putExtra("appGroupList", (Serializable) app.getSubAppList());
                                startActivity(intent);
                            } else {
                                if (getIsCommonlyUseGroupInList(listPosition)) {
                                    ApplicationUriUtils.openApp(getActivity(), app, "commonapplications");
                                } else {
                                    ApplicationUriUtils.openApp(getActivity(), app, "application");
                                }
                            }

                        }
                        if (MyAppCacheUtils.getNeedCommonlyUseApp()) {
                            saveOrChangeCommonlyUseAppList(app, appAdapterList);
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
                            new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
                            appListAdapter.notifyDataSetChanged();
                            dragGridViewAdapter.notifyDataSetChanged();
                            MyAppCacheUtils.deleteAppInCache(getActivity(), app);
//                            MyAppCacheUtils.saveMyAppList(getActivity(), appListAdapter.getAppAdapterList());
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
            int i = v.getId();
            if (i == R.id.ibt_appcenter_enter) {
                IntentUtils.startActivity(getActivity(), AppCenterActivity.class);
                PVCollectModelCacheUtils.saveCollectModel("appcenter", "application");
            } else if (i == R.id.ibt_appcenter_config) {
                showPopupWindow(v);

            } else if (i == R.id.bt_sort_finish) {
                appListAdapter.setCanEdit(false);
                appListAdapter.notifyDataSetChanged();
                setAppEditStatus(false);
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

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
                MyAppCacheUtils.saveMyAppListFromNet(getActivity(), (params[0])
                        .getAppGroupBeanList());
                List<AppGroupBean> appGroupList = handleAppList((params[0])
                        .getAppGroupBeanList());
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
            if (MyAppCacheUtils.getMyAppListFromNet(BaseApplication.getInstance()).size() > 0) {
                new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
            }
            appListAdapter.setAppAdapterList(appGroupList);
            swipeRefreshLayout.setRefreshing(false);
            refreshRecommendAppWidgetView();
        }
    }

    class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnUserAppsSuccess(final GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion) {
            swipeRefreshLayout.setRefreshing(false);
            myAppSaveTask = new MyAppSaveTask(clientConfigMyAppVersion);
            myAppSaveTask.execute(getAppGroupResult);
//            appListSizeExceptCommonlyUse = getAppGroupResult.getAppGroupBeanList().size();
        }

        @Override
        public void returnUserAppsFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            //          WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }


    }
}
