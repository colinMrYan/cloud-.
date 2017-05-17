package com.inspur.emmcloud.ui.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.bean.AppGroupBean;
import com.inspur.emmcloud.bean.AppOrder;
import com.inspur.emmcloud.bean.GetAppGroupResult;
import com.inspur.emmcloud.util.AppCacheUtils;
import com.inspur.emmcloud.util.AppTitleUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ShortCutUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;
import com.inspur.emmcloud.widget.draggrid.DragAdapter;
import com.inspur.emmcloud.widget.draggrid.DragAdapter.NotifyCommonlyUseListener;
import com.inspur.emmcloud.widget.draggrid.DragGridView;
import com.inspur.emmcloud.widget.draggrid.DragGridView.OnChanageListener;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.inspur.emmcloud.util.AppCacheUtils.getCommonlyUseAppList;

/**
 * classes : com.inspur.emmcloud.ui.app.MyAppFragment Create at 2016年12月13日
 * 上午11:10:20
 */
public class MyAppFragment extends Fragment implements OnRefreshListener {

    private static final String ACTION_NAME = "add_app";
    private View rootView;
    private LayoutInflater inflater;
    private PullableListView appListView;
    private AppListAdapter appListAdapter;
    private ImageView editBtn;
    private Button editBtnFinish;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDialog;
    private boolean hasCommonlyApp = false;
    private PullToRefreshLayout pullToRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private PopupWindow popupWindow;
    private boolean isNeedCommonlyUseApp = false;
//    private SwitchView switchView;
//    private View contentView;
    private TextView titleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_app, null);
        initViews();
        registerReceiver();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        loadingDialog = new LoadingDialog(getActivity());
        apiService = new MyAppAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        pullToRefreshLayout = (PullToRefreshLayout) rootView
                .findViewById(R.id.refresh_view);
        pullToRefreshLayout.setOnRefreshListener(this);
        appListView = (PullableListView) rootView
                .findViewById(R.id.my_app_list);
        appListView.setCanPullDown(true);
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
                if(popupWindow != null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        OnAppCenterClickListener listener = new OnAppCenterClickListener();
        ((RelativeLayout)rootView.findViewById(R.id.appcenter_layout)).setOnClickListener(listener);
        getMyApp(true);
        setTabTitle();

    }

    /**
     * 设置标题
     */
    private void setTabTitle(){
        String appTabs = PreferencesByUserUtils.getString(getActivity(),"app_tabbar_info_current","");
        if(!StringUtils.isBlank(appTabs)){
            titleText.setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
        }
    }

    /**
     * 获取app
     */
    private void getMyApp(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            loadingDialog.show(isShowDlg);
            apiService.getUserApps();
        } else {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
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
                    getMyApp(true);
                }
            }
        };

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        // 注册广播
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
    }


    /**
     * 打开应用中心
     */
    class OnAppCenterClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            IntentUtils.startActivity(getActivity(), AppCenterActivity.class);
        }
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
        return rootView;
    }

    /**
     * App组列表
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
            convertView = inflater.inflate(R.layout.app_drag_item, null);
            ((TextView) convertView.findViewById(R.id.app_title_text))
                    .setText(appAdapterList.get(listPosition).getCategoryName());
            DragGridView dragGridView = (DragGridView) convertView
                    .findViewById(R.id.app_list_draggrid);
            final List<App> appGroupItemList = appAdapterList.get(
                    listPosition).getAppItemList();
            final DragAdapter dragGridViewAdapter = new DragAdapter(
                    getActivity(), appGroupItemList, listPosition);
            dragGridView.setCanScroll(false);
            dragGridView.setPosition(listPosition);
            dragGridView.setPullToRefreshLayout(pullToRefreshLayout);
            dragGridViewAdapter.setGroupPosition(listPosition);
            dragGridView.setOnChangeListener(new OnChanageListener() {
                @Override
                public void onChange(int listPosition, int from, int to) {
                    handAppOrderChange(appGroupItemList, from, to);
                    dragGridViewAdapter.notifyDataSetChanged();
                    saveAppChangeOrder(listPosition);
                }
            });
            dragGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    if(!canEdit){
                        App app = appGroupItemList.get(position);
                        //可以再定具体出现的时机和是否需要对用户进行提示
                        if(app.getAppID().equals("1e169160-0e1f-11e7-8c5c-15b1be8e5981")){
                            LogUtils.YfcDebug("移动签到功能："+app.toString());
                            ShortCutUtils.createShortCut(getActivity(),ShortCutFunctionActivity.class,
                                    app.getAppName(),app.getUri(),"ecc-app-web-hcm",R.drawable.icon_shortcut_register);
                            /*为了ReactNative应用创建的*/
//                            ShortCutUtils.createShortCut(getActivity(),ReactNativeAppActivity.class,app.getAppName(),app.getUri(),"ecc-app-react-native",R.drawable.ic_launcher);
                        }
                        if(app.getAppID().equals("inspur_news_esg")){
                            LogUtils.YfcDebug("这是新闻应用");
                        }
                        UriUtils.openApp(getActivity(), app);
                        if(getNeedCommonlyUseApp()){
//                            saveOrChangeCommonlyUseApp(app, appAdapterList);
                            saveOrChangeCommonlyUseAppList(app,appAdapterList);
                        }
                    }
                }
            });
            dragGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {

                    if(!canEdit){
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
                    .setNotifyCommonlyUseListener(new NotifyCommonlyUseListener() {
                        @Override
                        public void onNotifyCommonlyUseApp(App app) {
                            handCommonlyUseAppChange(appAdapterList, app);
                            dragGridViewAdapter.notifyDataSetChanged();
                        }
                    });
            if (canEdit) {
                if (hasCommonlyApp && (listPosition == 0)) {
                    dragGridViewAdapter.setCanEdit(false);
                } else {
                    dragGridViewAdapter.setCanEdit(true);
                    dragGridView.setCanEdit(true);
                }
            } else {
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
     * @param app
     * @param appAdapterList
     */
    private void saveOrChangeCommonlyUseAppList(App app, List<AppGroupBean> appAdapterList) {
        List<AppCommonlyUse> appCommonlyUseAddCountList = addClickCount(app);
        List<AppCommonlyUse> appCommonlyUseList = calculateAppWeight(appCommonlyUseAddCountList);
        showCommonlyUseApps(app,appCommonlyUseList,appAdapterList);
    }

    /**
     * 展示常用应用
     * @param app
     * @param appCommonlyUseList
     * @param appAdapterList
     */
    private void showCommonlyUseApps(App app,List<AppCommonlyUse> appCommonlyUseList,
                                     List<AppGroupBean> appAdapterList) {
        LogUtils.YfcDebug("常用应用的长度："+appCommonlyUseList.size());
        if(hasCommonlyApp){
            //如果已经有了常用app则需要先移除掉第一组
            appAdapterList.remove(0);
            handCommonlyUseAppData(appAdapterList,true);
//            List<App> appItemList = new ArrayList<App>();
//            for(int i = 0;i < appCommonlyUseList.size();i++){
//                App appCommonlyUse = new App();
//                AppCommonlyUse appCommonlyUseWithWeight = appCommonlyUseList.get(i);
//                appCommonlyUse.setAppID(appCommonlyUseWithWeight.getAppID());
//                for(int j = 1; j < appAdapterList.size(); j++){
//                    List<App> searchAppList = appAdapterList.get(j).getAppItemList();
//                    int searchIndex = searchAppList.indexOf(appCommonlyUse);
//                    int allReadyhas = appItemList.indexOf(app);
////                    if(commonlyUseIndex == -1){
//                        if((searchIndex != -1) && (allReadyhas == -1)){
//                            App appPutInCommonlyUse = searchAppList.get(searchIndex);
//                            appPutInCommonlyUse.setWeight(appCommonlyUseWithWeight.getWeight());
//                            appItemList.add(appPutInCommonlyUse);
//                        }
////                    }
//                }
//            }
//
//
//            Collections.sort(appItemList,new SortCommonlyUseAppClass());
//            appAdapterList.get(0).setAppItemList(appItemList);
//            appListAdapter.notifyDataSetChanged();
        }else{
            AppGroupBean appGroupBean = new AppGroupBean();
            appGroupBean.setCategoryID("commonly");
            appGroupBean.setCategoryName(getString(R.string.commoly_use_app));
            List<App> commonlyUseAppList = new ArrayList<App>();
            commonlyUseAppList.add(app);
            appGroupBean.setAppItemList(commonlyUseAppList);
            appAdapterList.add(0, appGroupBean);
            hasCommonlyApp = true;
            appListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 根据前面计算出的点击次数和当前位置计算权重
     * @param appCommonlyUseAddCountList
     * @return
     */
    private List<AppCommonlyUse> calculateAppWeight(List<AppCommonlyUse> appCommonlyUseAddCountList) {
        int appCommonlyUseListSize = appCommonlyUseAddCountList.size();
        List<AppCommonlyUse> appCommonlyUseList = null;
        for (int i = 0;i < appCommonlyUseListSize;i++){
            AppCommonlyUse appCommonlyUseWeight = appCommonlyUseAddCountList.get(i);
            int count = appCommonlyUseWeight.getClickCount();
            int index = appCommonlyUseAddCountList.indexOf(appCommonlyUseWeight);
            double weight = 0.6*count+(0.4*10*(1-((double)index)/((double)appCommonlyUseListSize)));
            appCommonlyUseAddCountList.get(i).setWeight(weight);
        }


        Collections.sort(appCommonlyUseAddCountList,new SortCommonlyUseApp());
        AppCacheUtils.saveAppCommonlyUseList(getActivity(),appCommonlyUseAddCountList);
        if(appCommonlyUseAddCountList.size()>4){
            appCommonlyUseList = appCommonlyUseAddCountList.subList(0,4);
            return appCommonlyUseList;
        }
        return appCommonlyUseAddCountList;
    }

    /**
     * 点击应用后clickCount加1
     * @param app
     * @return
     */
    private List<AppCommonlyUse> addClickCount(App app) {
        List<AppCommonlyUse> appCommonlyUseList = AppCacheUtils.getCommonlyUseAppList(getActivity());
        AppCommonlyUse appCommonlyUse = new AppCommonlyUse();
        appCommonlyUse.setAppID(app.getAppID());
        int index = appCommonlyUseList.indexOf(appCommonlyUse);
        if(index != -1){
            AppCommonlyUse appCommonlyUseInTable = appCommonlyUseList.get(index);
            int count = appCommonlyUseInTable.getClickCount();
            appCommonlyUseList.get(index).setClickCount(count+1);
        }else{
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
        if (hasCommonlyApp && (commonlyAppItemList.indexOf(app) != -1)) {
            commonlyAppItemList.remove(app);
        }
        Iterator<AppGroupBean> appGroupBeanList = appAdapterList.iterator();
        while (appGroupBeanList.hasNext()) {
            AppGroupBean appGroupBean = appGroupBeanList.next();
            if (appGroupBean.getAppItemList().size() == 0) {
                appGroupBeanList.remove();
            }
        }
    }

    /**
     * 排序之后进行的操作
     *
     * @param appGroupItemList
     * @param from
     * @param to
     */
    protected void handAppOrderChange(List<App> appGroupItemList, int from,
                                      int to) {
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
     * 保存或者改变app的点击权重 废弃方法----------
     *
     * @param app
     * @param appAdapterList
     */
    private void saveOrChangeCommonlyUseApp(App app,
                                            List<AppGroupBean> appAdapterList) {
        //保存应用最后点击时间
        app.setLastUpdateTime(System.currentTimeMillis());
//		AppCommonlyUse appCommonlyUse = AppCacheUtils.getCommonlyUseAppById(getActivity(), app.getAppID());
        //新建一个常用应用对象用于查找点击应用在常用应用列表里的位置
        AppCommonlyUse appCommonlyUse = new AppCommonlyUse();
        //根据id查找所以设置id
        appCommonlyUse.setAppID(app.getAppID());
        //取出当前存储的常用应用列表
        List<AppCommonlyUse> appCommonlyUseList = getCommonlyUseAppList(getActivity());
        //查找点击应用的位置
        int index = appCommonlyUseList.indexOf(appCommonlyUse);
        //计算常用应用列表长度如果为0则置为1防止除0错，同时因为0个的时候是1那么后面的位置序号需要依次加1，因为0占了1的位置
        int appCommonlyUseListSize = (appCommonlyUseList.size()==0? 1:(appCommonlyUseList.size()+1));
        int count = 0;
        double weight = 0;

        if(index != -1){
            //点击应用在常用应用表里存在，根据index取出点击应用
            AppCommonlyUse appCommonlyUseChange = appCommonlyUseList.get(index);
            //取出点击应用现在的点击次数
            int clickCount = appCommonlyUseChange.getClickCount();
            //重新设置最后点击时间和点击次数，时间可能已经没有用
            appCommonlyUseChange.setLastUpdateTime(System.currentTimeMillis());
            appCommonlyUseChange.setClickCount(clickCount + 1);
            //点击次数的值
            count = clickCount + 1;
            //新权重计算并设置
            weight = 0.6*count+(0.4*0*(1-((double)index)/((double)appCommonlyUseListSize)));
            appCommonlyUseChange.setWeight(weight);
            //保存常用应用信息
            AppCacheUtils.saveAppCommonlyUse(getActivity(), appCommonlyUseChange);
        }else {
            //点击应用在常用应用表里没有，新建一个
            AppCommonlyUse appCommonlyUseNull = new AppCommonlyUse();
            //设置id和count
            appCommonlyUseNull.setAppID(app.getAppID());
            appCommonlyUseNull.setClickCount(1);
            count = 1;
            //计算设置权重，并存储
            weight = 0.6*count+(0.4*0*(1-((double)index)/((double)appCommonlyUseListSize)));
            appCommonlyUseNull.setWeight(weight);
            appCommonlyUseNull.setLastUpdateTime(System.currentTimeMillis());
            AppCacheUtils.saveAppCommonlyUse(getActivity(), appCommonlyUseNull);
            appCommonlyUseList.add(appCommonlyUseNull);
        }

//		weight = 0.6*count+(0.4*20*(1-((double)index)/((double)appCommonlyUseListSize)));
        //为当前显示中的应用设置权重
        app.setWeight(weight);
        if (hasCommonlyApp) {
            //如果有常用应用组,取出常用应用的列表
            boolean hasApp = false;
            List<App> appItemList = appAdapterList.get(0).getAppItemList();
            int searchAppItemIndex = appItemList.indexOf(app);
            if (searchAppItemIndex != -1) {
                hasApp = true;
            }
            appItemList.add(0, app);
            if (hasApp) {
                appItemList.remove(searchAppItemIndex + 1);
            }
            Collections.sort(appItemList, new SortCommonlyUseAppClass());
            if (appItemList.size() > 4) {
                List<App> commonlyUseAppList = appItemList.subList(0,4);
                appAdapterList.get(0).setAppItemList(commonlyUseAppList);
//                appItemList.remove(4);
            }else {
                appAdapterList.get(0).setAppItemList(appItemList);
            }
            for (int i = 0; i < appItemList.size(); i++) {
                LogUtils.YfcDebug("app名称："+appItemList.get(i).getAppName()+"点击后常用应用的权重"+appItemList.get(i).getWeight());
            }
            appListAdapter.notifyDataSetChanged();
        } else {
            AppGroupBean appGroupBean = new AppGroupBean();
            appGroupBean.setCategoryID("commonly");
            appGroupBean.setCategoryName(getString(R.string.commoly_use_app));
            List<App> commonlyUseAppList = new ArrayList<App>();
            commonlyUseAppList.add(app);
            appGroupBean.setAppItemList(commonlyUseAppList);
            appAdapterList.add(0, appGroupBean);
            hasCommonlyApp = true;
            appListAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        getMyApp(false);
        editBtn.setVisibility(View.VISIBLE);
        editBtnFinish.setVisibility(View.GONE);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
    }

    @Override
    public void onPause() {
        super.onPause();
        //为解决切换tab卡死的bug
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * 变更会议，取消会议下拉框
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        // 一个自定义的布局，作为popwindowivew显示的内容
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.app_center_popup_window_view, null);
        final  SwitchView switchView = (SwitchView) contentView.findViewById(R.id.app_hide_switch);
        //为了在打开PopWindow时立刻显示当前状态
        switchView.setOpened(getNeedCommonlyUseApp());
        // 设置按钮的点击事件
        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);

        switchView.setOnStateChangedListener(new OnStateChangedListener() {

            @Override
            public void toggleToOn(View view) {
                if(view == null || switchView == null){
                    return;
                }
                switchView.toggleSwitch(true);
                saveNeedCommonlyUseApp(true);
                handCommonlyUseAppData(appListAdapter.getAppAdapterList(), true);
            }

            @Override
            public void toggleToOff(View view) {
                if(view == null || switchView == null){
                    return;
                }
                switchView.toggleSwitch(false);
                saveNeedCommonlyUseApp(false);
                if(hasCommonlyApp){
                    appListAdapter.getAppAdapterList().remove(0);
                    appListAdapter.notifyDataSetChanged();
                    hasCommonlyApp = false;
                }
            }
        });

        RelativeLayout changeOrderLayout = (RelativeLayout) contentView.findViewById(R.id.app_change_layout);
        changeOrderLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appListAdapter != null){
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
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }

    /**
     * 存储是否需要显示常用app
     * @param isNeedCommonlyUseApp
     */
    private void saveNeedCommonlyUseApp(boolean isNeedCommonlyUseApp){
        String userId = ((MyApplication)getActivity().getApplication()).getUid();
        PreferencesUtils.putBoolean(getActivity(), UriUtils.tanent
                        + userId + "needCommonlyUseApp",
                isNeedCommonlyUseApp);
    }

    /**
     * 获取是否需要显示常用app
     * @return
     */
    private boolean getNeedCommonlyUseApp(){
        String userId = ((MyApplication)getActivity().getApplication()).getUid();
        isNeedCommonlyUseApp = PreferencesUtils.getBoolean(getActivity(), UriUtils.tanent
                + userId + "needCommonlyUseApp",true);
        return isNeedCommonlyUseApp;
    }

    /**
     * 存储App顺序
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            List<AppGroupBean> appGroupList = handleAppList(getAppGroupResult
                    .getAppGroupBeanList());
            appListAdapter = new AppListAdapter(appGroupList);
            appListView.setAdapter(appListAdapter);
            appListAdapter.notifyDataSetChanged();
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        }

        @Override
        public void returnUserAppsFail(String error) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            WebServiceMiddleUtils.hand(getActivity(), error);
        }
    }

    /**
     * 获取到网络数据后对排序和显示进行处理
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
                    new SortClass());
        }
        handCommonlyUseAppData(appGroupList,false);
        return appGroupList;
    }

    /**
     * 处理应用加载数据时常用应用部分
     * @param appGroupList
     */
    private void handCommonlyUseAppData(List<AppGroupBean> appGroupList,boolean isNeedRefresh) {
        List<AppCommonlyUse> appCommonlyUseList =
                getCommonlyUseAppList(getActivity());//这里换成获取所有
        if (appCommonlyUseList.size() > 0  && getNeedCommonlyUseApp()) {
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
                    int appCommonlyUseListSize = appCommonlyUseList.size();
                    if (index != -1 && allreadHas == -1) {
                        AppCommonlyUse appCommonlyUseTemp = appCommonlyUseList.get(index);
//						double weight = 0.6*appCommonlyUseTemp.getClickCount()+(0.4*20*(1-((double)index)/(double)appCommonlyUseListSize));
                        app.setWeight(appCommonlyUseTemp.getWeight());
                        myCommonlyUseAppList.add(app);
                    }
                }
            }

            Collections.sort(myCommonlyUseAppList,new SortCommonlyUseAppClass());
            if(myCommonlyUseAppList.size() > 4){
                myCommonlyUseAppList = myCommonlyUseAppList.subList(0, 4);
            }
            Collections.sort(myCommonlyUseAppList, new SortCommonlyUseAppClass());
            for (int i = 0; i < myCommonlyUseAppList.size(); i++) {
                LogUtils.YfcDebug("app名称："+myCommonlyUseAppList.get(i).getAppName()+"常用应用的权重"+myCommonlyUseAppList.get(i).getWeight());
            }

            if(myCommonlyUseAppList.size()>0){
                appGroupBean.setAppItemList(myCommonlyUseAppList);
                appGroupList.add(0, appGroupBean);
                hasCommonlyApp = true;
            }
        }
        if(isNeedRefresh){
            appListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 应用顺序排序接口，比较orderId
     *
     */
    public class SortClass implements Comparator {
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
     * 常用应用排序接口，比较权重
     *
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
     * 常用应用排序接口，比较权重
     *
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
}
