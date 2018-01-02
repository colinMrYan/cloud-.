package com.inspur.emmcloud.ui.appcenter;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.DragAdapter;
import com.inspur.emmcloud.adapter.RecommendAppWidgetListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppBadgeBean;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;
import com.inspur.emmcloud.bean.appcenter.AppOrder;
import com.inspur.emmcloud.bean.appcenter.GetAppBadgeResult;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.bean.appcenter.RecommendAppWidgetBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnRecommendAppWidgetItemClickListener;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;
import com.inspur.emmcloud.util.privates.AppTitleUtils;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;
import com.inspur.emmcloud.util.privates.MyAppWidgetUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ShortCutUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ECMRecyclerViewLinearLayoutManager;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;
import com.inspur.emmcloud.widget.draggrid.DragGridView;
import com.inspur.emmcloud.widget.draggrid.DragGridView.OnChanageListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.inspur.emmcloud.util.privates.cache.AppCacheUtils.getCommonlyUseAppList;

/**
 * classes : com.inspur.emmcloud.ui.app.MyAppFragment Create at 2016年12月13日
 * 上午11:10:20
 */
public class MyAppFragment extends Fragment {

    private static final String ACTION_NAME = "add_app";
    private View rootView;
    private ListView appListView;
    private AppListAdapter appListAdapter;
    private ImageView editBtn;
    private Button editBtnFinish;
    private MyAppAPIService apiService;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private PopupWindow popupWindow;
    private boolean isNeedCommonlyUseApp = false;
    private List<String> shortCutAppList = new ArrayList<>();
    private MyAppSaveTask myAppSaveTask;
    private Map<String,AppBadgeBean> appBadgeBeanMap = new HashMap<>();
    private RecyclerView recommendAppWidgetListView = null;
    private RecommendAppWidgetListAdapter recommendAppWidgetListAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_app, null);
        initViews();
        registerReceiver();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
     * 检查获取我的应用推荐应用小部件更新
     * 过期则更新不过期不更新
     */
    private void getMyAppRecommendWidgetsUpdate() {
        if(!MyAppWidgetUtils.isEffective(PreferencesByUserAndTanentUtils.getLong(getContext(),Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE,0L))){
            MyAppWidgetUtils.getInstance(getActivity().getApplicationContext()).getMyAppWidgetsFromNet();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getMyApp();
        getAppBadgeNum();
        refreshRecommendAppWidgetView();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        apiService = new MyAppAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        initPullRefreshLayout();
        appListView = (ListView) rootView
                .findViewById(R.id.my_app_list);
        refreshAppListView();
        editBtn = (ImageView) rootView.findViewById(R.id.app_edit_btn);
        editBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
        editBtnFinish = (Button) rootView.findViewById(R.id.app_edit_finish);
        editBtnFinish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                appListAdapter.setCanEdit(false);
                appListAdapter.notifyDataSetChanged();
                editBtn.setVisibility(View.VISIBLE);
                editBtnFinish.setVisibility(View.GONE);
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        OnAppCenterClickListener listener = new OnAppCenterClickListener();
        (rootView.findViewById(R.id.appcenter_layout)).setOnClickListener(listener);
        setTabTitle();
        //当Fragment创建时重置时间
        PreferencesByUserAndTanentUtils.putInt(getActivity(),Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR,0);
//        shortCutAppList.add("mobile_checkin_hcm");
//        shortCutAppList.add("inspur_news_esg");//目前，除在此处添加id还需要为每个需要生成快捷方式的应用配置图标
    }

    /**
     * 刷新推荐应用小部件
     * 每个小时都有可能有变化
     */
    private void refreshRecommendAppWidgetView() {
        //判断时间是否点击了叉号，不在显示时间内，或者推荐应用已经过了有效期
        if(!(MyAppWidgetUtils.isNeedShowMyAppRecommendWidgets(getActivity())) ||
                !MyAppWidgetUtils.isEffective(PreferencesByUserAndTanentUtils.getLong(getContext()
                        ,Constant.PREF_MY_APP_RECOMMEND_EXPIREDDATE,0L))){
            (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
            return;
        }
        //是否是需要刷新的时间，即过了当前小时内appId的显示时间，这是只控制刷新，不控制显示隐藏，MyAPPFragment Destroy时会重置这个时间，使下次进入时不会影响刷新UI
        boolean isRefreshTime = PreferencesByUserAndTanentUtils.getInt(getActivity(),Constant.PREF_MY_APP_RECOMMEND_LASTUPDATE_HOUR,-1) != MyAppWidgetUtils.getNowHour();
        if(!isRefreshTime){
            return;
        }
        GetRecommendAppWidgetListResult getRecommendAppWidgetListResult = new GetRecommendAppWidgetListResult(PreferencesByUserAndTanentUtils.
                getString(getActivity(),Constant.PREF_MY_APP_RECOMMEND_DATA,""));
        List<RecommendAppWidgetBean> recommendAppWidgetBeanList = getRecommendAppWidgetListResult.getRecommendAppWidgetBeanList();
        List<App> appList = MyAppWidgetUtils.getShouldShowAppList(recommendAppWidgetBeanList,appListAdapter.getAppAdapterList());
        if(appList.size() > 0){
            if(recommendAppWidgetListView == null){
                recommendAppWidgetListView = (RecyclerView) rootView.findViewById(R.id.my_app_recommend_app_wiget_recyclerview);
                (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.VISIBLE);
                ECMRecyclerViewLinearLayoutManager layoutManager = new ECMRecyclerViewLinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                layoutManager.setCanScrollHorizontally(false);
                recommendAppWidgetListView.setLayoutManager(layoutManager);
                recommendAppWidgetListView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(getActivity(), 4)));
                recommendAppWidgetListAdapter = new RecommendAppWidgetListAdapter(getActivity());
                recommendAppWidgetListView.setAdapter(recommendAppWidgetListAdapter);
                recommendAppWidgetListAdapter.setOnRecommendAppWidgetItemClickListener(new OnRecommendAppWidgetItemClickListener() {
                    @Override
                    public void onRecommendAppWidgetItemClick(App app) {
                        UriUtils.openApp(getActivity(),app);
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
        }else{
            //当前小时没有需要显示的appId或者列表中没有当前小时内的应用
            (rootView.findViewById(R.id.my_app_recommend_app_widget_layout)).setVisibility(View.GONE);
        }
    }


    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout(){
        swipeRefreshLayout = (MySwipeRefreshLayout) rootView
                .findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyApp();
                getAppBadgeNum();
            }
        });
    }


    /**
     * 初始化AppListView，加载缓存数据
     */
    private void refreshAppListView() {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(getContext());
        if(appListAdapter != null){
            appListAdapter.setAppAdapterList(appGroupList);
        }else{
            appListAdapter = new AppListAdapter(appGroupList);
            appListView.setAdapter(appListAdapter);
            appListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置标题，根据当前Fragment类名获取显示名称
     */
    private void setTabTitle() {
        String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), "app_tabbar_info_current", "");
        if (!StringUtils.isBlank(appTabs)) {
            ((TextView) rootView.findViewById(R.id.header_text)).setText(AppTitleUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
        }
    }

    /**
     * 获取我的apps
     */
    private void getMyApp() {
        if (NetUtils.isNetworkConnected(getActivity(),false)) {
            apiService.getUserApps();
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
                    (rootView.findViewById(R.id.app_edit_finish)).setVisibility(View.GONE);
                    editBtn.setVisibility(View.VISIBLE);
                    appListAdapter.setCanEdit(false);
                    appListAdapter.notifyDataSetChanged();
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        // 注册广播
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    /**
     * 记录用户使用了应用中心功能
     */
    private void recordUserClickAppCenter() {
        PVCollectModel pvCollectModel = new PVCollectModel("appcenter", "application");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
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
            ((TextView) convertView.findViewById(R.id.app_title_text))
                    .setText(appAdapterList.get(listPosition).getCategoryName());
            DragGridView dragGridView = (DragGridView) convertView
                    .findViewById(R.id.app_list_draggrid);
            final List<App> appGroupItemList = appAdapterList.get(
                    listPosition).getAppItemList();
            final DragAdapter dragGridViewAdapter = new DragAdapter(
                    getActivity(), appGroupItemList, listPosition,appBadgeBeanMap);
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
                        //可以再定具体出现的时机和是否需要对用户进行提示
                        //快捷方式逻辑，因不同手机rom定制问题，暂时不打开这个功能
//                        String appId = app.getIdentifiers();
//                        if (shortCutAppList.indexOf(appId) != -1) {
//                            boolean needCreateShortCut = PreferencesByUserAndTanentUtils.getBoolean(getActivity(), "need_create_shortcut" + app.getAppID(), true);
//                            if (needCreateShortCut && !ShortCutUtils.isShortCutExist(getActivity(), app.getAppName())) {
//                                //目前只识别的移动签到和集团新闻两个应用，设置了两个图标，以后可以改成可配置的
//                                if(appId.equals("mobile_checkin_hcm")){
//                                    //保留指定BItmap的指定方式
////                                    InputStream is = null;
////                                    try {
////                                        is = getActivity().getAssets().open("icon_test1.png");
////                                        Bitmap bitmap = BitmapFactory.decodeStream(is);
////                                        showCreateShortCutDialog(app, "ecc-app-web-hcm",
////                                                ImpActivity.class, 0,bitmap);
////                                    } catch (IOException e) {
////                                        e.printStackTrace();
////                                    }
//                                    showCreateShortCutDialog(app, "ecc-app-web-hcm", ImpActivity.class,
//                                            R.drawable.icon_shortcut_register,null);
//                                }else if(appId.equals("inspur_news_esg")){
//                                    //暂时保留，这里可以指定新闻
////                                    showCreateShortCutDialog(app, "ecc-app-native", GroupNewsActivity.class,
////                                            R.drawable.news_icon,null);
//                                    UriUtils.openApp(getActivity(),app);
//                                }
//                            } else {
//                                UriUtils.openApp(getActivity(), app);
//                            }
//                        }else{
//                            UriUtils.openApp(getActivity(), app);
//                        }
                        if (NetUtils.isNetworkConnected(getActivity())) {
                            UriUtils.openApp(getActivity(), app);
                        }
                        if (getNeedCommonlyUseApp()) {
                            saveOrChangeCommonlyUseAppList(app, appAdapterList);
                            MyAppCacheUtils.saveMyAppList(getContext(),appAdapterList);
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
                        editBtn.setVisibility(View.GONE);
                        editBtnFinish.setVisibility(View.VISIBLE);
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
                            handCommonlyUseAppChange(appAdapterList, app);
                            appListAdapter.notifyDataSetChanged();
                            dragGridViewAdapter.notifyDataSetChanged();
                            MyAppCacheUtils.saveMyAppList(getActivity(), appListAdapter.getAppAdapterList());
                        }
                    });
            if (canEdit) {
                if (getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0 && (listPosition == 0)) {
                    //如果应用列表可以编辑，并且有常用应用分组，则把常用应用的可编辑属性设置false（也就是第0行设为false）
                    dragGridViewAdapter.setCanEdit(false);
                } else {
                    //如果应用列表可以编辑，不是常用应用分组
                    dragGridViewAdapter.setCanEdit(true);
                    dragGridView.setCanEdit(true);
                }
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
         * 设置是否可以删除
         */
        public void setCanEdit(boolean canDelete) {
            this.canEdit = canDelete;
        }

        /**
         * 获取是否可以删除
         */
        public boolean getCanEdit() {
            return this.canEdit;
        }
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
        if(getNeedCommonlyUseApp()){
            showCommonlyUseApps(app,  appAdapterList);
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
        if (AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0) {
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
    private void handCommonlyUseAppChange(List<AppGroupBean> appAdapterList,
                                          App app) {
        List<App> commonlyAppItemList = appAdapterList.get(0)
                .getAppItemList();
        if (getNeedCommonlyUseApp() && (commonlyAppItemList.indexOf(app) != -1)) {
            commonlyAppItemList.remove(app);
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

    /**
     * 获取appBadge
     */
    private void getAppBadgeNum() {
        if(NetUtils.isNetworkConnected(getActivity(),false)){
            apiService.getAppBadgeNum();
        }
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
            getActivity().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (myAppSaveTask != null && !myAppSaveTask.isCancelled()
                && myAppSaveTask.getStatus() == AsyncTask.Status.RUNNING) {
            myAppSaveTask.cancel(true);
            myAppSaveTask = null;
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
        final SwitchView switchView = (SwitchView) contentView.findViewById(R.id.app_hide_switch);
        //为了在打开PopWindow时立刻显示当前状态
        switchView.setOpened(getNeedCommonlyUseApp());
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
        switchView.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                if (view == null || switchView == null) {
                    return;
                }
                switchView.toggleSwitch(true);
                saveNeedCommonlyUseApp(true);
                handCommonlyUseAppData(appListAdapter.getAppAdapterList(), true);
            }

            @Override
            public void toggleToOff(View view) {
                if (view == null || switchView == null) {
                    return;
                }
                switchView.toggleSwitch(false);
                if (getNeedCommonlyUseApp() && AppCacheUtils.getCommonlyUseNeedShowList(getActivity()).size() > 0) {
                    appListAdapter.getAppAdapterList().remove(0);
                    appListAdapter.notifyDataSetChanged();
                }
                saveNeedCommonlyUseApp(false);
            }
        });
        TextView changeOrderLayout = (TextView) contentView.findViewById(R.id.app_change_btn);
        changeOrderLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appListAdapter != null) {
                    appListAdapter.setCanEdit(true);
                    appListAdapter.notifyDataSetChanged();
                    editBtn.setVisibility(View.GONE);
                    editBtnFinish.setVisibility(View.VISIBLE);
                    popupWindow.dismiss();
                }
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 这里是API的一个bug
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        backgroundAlpha(0.8f);
        // 设置好参数之后再show
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
        String userId = ((MyApplication) getActivity().getApplication()).getUid();
        isNeedCommonlyUseApp = PreferencesUtils.getBoolean(getActivity(), MyApplication.getInstance().getTanent()
                + userId + "needCommonlyUseApp", true);
        return isNeedCommonlyUseApp;
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

    /**
     * 打开应用中心
     */
    class OnAppCenterClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            IntentUtils.startActivity(getActivity(), AppCenterActivity.class);
            recordUserClickAppCenter();
        }
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
        okBtn.setOnClickListener(new View.OnClickListener() {
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
                UriUtils.openApp(getActivity(), app);
                hasIntrcutionDialog.dismiss();
            }
        });
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    PreferencesByUserAndTanentUtils.putBoolean(getActivity(), "need_create_shortcut" + app.getAppID(), false);
                }
                UriUtils.openApp(getActivity(), app);
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


    class MyAppSaveTask extends AsyncTask<GetAppGroupResult, Void, List<AppGroupBean>> {

        @Override
        protected List<AppGroupBean> doInBackground(GetAppGroupResult... params) {
            try {
                List<AppGroupBean> appGroupList = handleAppList((params[0])
                        .getAppGroupBeanList());
                MyAppCacheUtils.saveMyAppList(getActivity(), appGroupList);
                return appGroupList;
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<AppGroupBean>();
            }

        }

        @Override
        protected void onPostExecute(List<AppGroupBean> appGroupList) {
            super.onPostExecute(appGroupList);
            appListAdapter.setAppAdapterList(appGroupList);
            swipeRefreshLayout.setRefreshing(false);
            refreshRecommendAppWidgetView();
        }
    }


    /**
     * 获取需要显示的appId列表
     *
     * @return
     */
    public  void calculateBadgeNumberAndSendToIndex(GetAppBadgeResult getAppBadgeResult) {
        int badageNum = 0;
        Set<String> appBadgeSet = appBadgeBeanMap.keySet();
        for (String appId:appBadgeSet) {
            App app = new App();
            app.setAppID(appId);
            for (int i = 0; i < appListAdapter.getAppAdapterList().size(); i++){
                int index = appListAdapter.getAppAdapterList().get(i).getAppItemList().indexOf(app);
                if(index != -1){
                    badageNum = badageNum + appBadgeBeanMap.get(appId).getBadgeNum();
                    break;
                }
            }
        }
        getAppBadgeResult.setTabBadgeNumber(badageNum);
        //发送到IndexActivity updateBadgeNumber
        EventBus.getDefault().post(getAppBadgeResult);
    }

    /**
     * 网络请求处理类，一般放最后
     */
    class WebService extends APIInterfaceInstance {
        @Override
        public void returnUserAppsSuccess(final GetAppGroupResult getAppGroupResult) {
            myAppSaveTask = new MyAppSaveTask();
            myAppSaveTask.execute(getAppGroupResult);
        }

        @Override
        public void returnUserAppsFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnGetAppBadgeResultSuccess(GetAppBadgeResult getAppBadgeResult) {
            swipeRefreshLayout.setRefreshing(false);
            appBadgeBeanMap = getAppBadgeResult.getAppBadgeBeanMap();
            appListAdapter.notifyDataSetChanged();
            calculateBadgeNumberAndSendToIndex(getAppBadgeResult);
        }

        @Override
        public void returnGetAppBadgeResultFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }
    }
}
