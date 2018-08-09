package com.inspur.emmcloud.ui.appcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppAdsBean;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircularProgress;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.imp.api.ImpActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.inspur.emmcloud.ui.appcenter.AppCenterMoreActivity.APP_CENTER_APPLIST;
import static com.inspur.emmcloud.ui.appcenter.AppCenterMoreActivity.APP_CENTER_CATEGORY_NAME;

/**
 * 应用中心页面 com.inspur.emmcloud.ui.AppCenterActivity create at 2016年8月31日
 * 下午2:54:47
 */
public class AppCenterActivity extends BaseActivity {
    private static final String ACTION_NAME = "add_app";
    private static final int UPDATE_VIEWPAGER = 1;
    private static final String APP_CENTER_CATEGORY_PROTOCOL = "ecc-app-store://category";
    private static final String APP_CENTER_APP_NAME_PROTOCOL = "ecc-app-store://app";
    private ViewPager viewPager;
    private CircularProgress recommendCircleProgress, classCircleProgress;
    private ListView recommendListView, classListView;
    private SwipeRefreshLayout recommendSwipeRefreshLayout, classSwipeRefreshLayout;
    private List<AppAdsBean> adsList = new ArrayList<>();
    private List<AppGroupBean> categoryAppList = new ArrayList<AppGroupBean>();
    private List<List<App>> appList = new ArrayList<>();
    private BaseAdapter recommendAppAdapter;
    private BaseAdapter categoriesAppAdapter;
    private BroadcastReceiver addAppReceiver;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_center);
        initView();
        getAllApp();
        registerReceiver();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        View recommendView = LayoutInflater.from(this).inflate(
                R.layout.app_recommend_layout, null);
        View classView = LayoutInflater.from(this).inflate(
                R.layout.app_categories_layout, null);
        recommendListView = (ListView) recommendView.findViewById(R.id.list);
        classListView = (ListView) classView.findViewById(R.id.app_center_categories_list);
        recommendSwipeRefreshLayout = (SwipeRefreshLayout) recommendView.findViewById(R.id.refresh_layout);
        recommendSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        recommendSwipeRefreshLayout.setOnRefreshListener(new AppCenterRefreshListener());
        classSwipeRefreshLayout = (SwipeRefreshLayout) classView.findViewById(R.id.refresh_layout);
        classSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        classSwipeRefreshLayout.setOnRefreshListener(new AppCenterRefreshListener());
        recommendAppAdapter = new RecommendAppAdapter();
        recommendListView.setAdapter(recommendAppAdapter);
        categoriesAppAdapter = new CategoriesAppAdapter();
        recommendCircleProgress = (CircularProgress) recommendView.findViewById(R.id.circle_progress);
        classCircleProgress = (CircularProgress) classView.findViewById(R.id.app_center_categories_circle_progress);
        List<View> viewList = new ArrayList<View>();
        viewList.add(recommendView);
        viewList.add(classView);
        viewPager.setAdapter(new MyViewPagerAdapter(viewList, null));
        viewPager.addOnPageChangeListener(new PageChangeListener());
    }

    class AppCenterRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            getAllApp();
        }
    }

    /**
     * 最外层两个tab的监听器
     */
    private class PageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            int recommendTabTextColor = arg0 == 0 ? Color.parseColor("#4990E2") : Color.parseColor("#999999");
            int classTabTextColor = arg0 == 1 ? Color.parseColor("#4990E2") : Color.parseColor("#999999");
            int recommendTabFooterViewVisible = arg0 == 0 ? View.VISIBLE : View.INVISIBLE;
            int classTabFooterViewVisible = arg0 == 1 ? View.VISIBLE : View.INVISIBLE;
            ((TextView) findViewById(R.id.recommand_tab_text)).setTextColor(recommendTabTextColor);
            ((TextView) findViewById(R.id.class_tab_text)).setTextColor(classTabTextColor);
            findViewById(R.id.recommand_tab_footer_view).setVisibility(recommendTabFooterViewVisible);
            findViewById(R.id.class_tab_footer_view).setVisibility(classTabFooterViewVisible);
            if (arg0 == 0) {
                recommendListView.setAdapter(recommendAppAdapter);
            } else if (arg0 == 1) {
                classListView.setAdapter(categoriesAppAdapter);
                classListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(AppCenterMoreActivity.APP_CENTER_APPLIST, (Serializable) categoryAppList.get(position).getAppItemList());
                        bundle.putString(AppCenterMoreActivity.APP_CENTER_CATEGORY_NAME, categoryAppList.get(position).getCategoryName());
                        IntentUtils.startActivity(AppCenterActivity.this, AppCenterMoreActivity.class, bundle);
                    }
                });
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.search_img:
                IntentUtils.startActivity(AppCenterActivity.this,
                        AppSearchActivity.class);
                break;
            case R.id.recommand_tab_text:
                viewPager.setCurrentItem(0);
                break;
            case R.id.class_tab_text:
                viewPager.setCurrentItem(1);
                break;
            default:
                break;
        }
    }

    /**
     * 获取所有应用
     */
    private void getAllApp() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            MyAppAPIService apiService = new MyAppAPIService(AppCenterActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getNewAllApps();
        } else {
            recommendSwipeRefreshLayout.setRefreshing(false);
            classSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 注册添加应用检测广播
     */
    private void registerReceiver() {
        addAppReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION_NAME)) {
                    App addApp = (App) intent.getExtras()
                            .getSerializable("app");
                    int recommendAppIndex = -1, groupIndex = 0;
                    Iterator<List<App>> appItemList = appList.listIterator();
                    while (appItemList.hasNext()) {
                        recommendAppIndex = appItemList.next().indexOf(addApp);
                        if (recommendAppIndex != -1) {
                            break;
                        }
                        groupIndex = groupIndex + 1;
                    }
                    if (recommendAppIndex != -1) {
                        List<App> recommendAppItemList = appList.get(groupIndex);
                        if (recommendAppItemList != null) {
                            recommendAppItemList.get(recommendAppIndex).setUseStatus(1);
                        }
                    }
                    for (int i = 0; i < categoryAppList.size(); i++) {
                        int categoriesAppIndex = categoryAppList.get(i).getAppItemList().indexOf(addApp);
                        if (categoriesAppIndex != -1) {
                            categoryAppList.get(i).getAppItemList().get(categoriesAppIndex).setUseStatus(1);
                        }
                    }
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(addAppReceiver, myIntentFilter);
    }

    /**
     * 推荐的adapter
     */
    class RecommendAppAdapter extends BaseAdapter {
        @Override
        public View getView(final int listPosition, View convertView, ViewGroup parent) {
            if (listPosition == 0 && adsList.size() > 0) {
                convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.my_app_recommand_banner_app_item_view, null);
                RelativeLayout appRecommendLayout = (RelativeLayout) convertView.findViewById(R.id.app_center_recomand_viewpager_layout);
                ViewPager viewPager = (ViewPager) convertView.findViewById(R.id.app_center_banner_viewpager);
                viewPager.setOffscreenPageLimit(DensityUtil.dip2px(AppCenterActivity.this, 3));
                //推荐带横向滑动时解决滑动冲突
//                viewPager.getParent().requestDisallowInterceptTouchEvent(true);
//                viewPager.setClipChildren(false);
                viewPager.setPageMargin(DensityUtil.dip2px(AppCenterActivity.this, 5));
                initRecommendViewPager(appRecommendLayout, viewPager);
            } else {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_app_recommand_app_item_view, null);
                final int appListIndex = adsList.size() == 0 ? listPosition : (listPosition - 1);
                List<App> appItemList = appList.get(appListIndex);
                if (appItemList.size() > 0) {
                    ((TextView) convertView.findViewById(R.id.app_center_recommand_text)).setText(appItemList.get(0).getCategoryName());
                }
                if (appItemList.size() <= 10) {
                    (convertView.findViewById(R.id.app_center_more_text)).setVisibility(View.GONE);
                }
                convertView.findViewById(R.id.app_center_recommand_layout).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(APP_CENTER_APPLIST, (Serializable) appList.get(appListIndex));
                        bundle.putString(APP_CENTER_CATEGORY_NAME, appList.get(appListIndex).get(0).getCategoryName());
                        IntentUtils.startActivity(AppCenterActivity.this, AppCenterMoreActivity.class, bundle);
                    }
                });
                RecyclerView recommendRecyclerView = (RecyclerView) convertView.findViewById(R.id.app_center_recomand_recycleview);
                recommendRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(AppCenterActivity.this, 11)));
                GridLayoutManager gridLayoutManager = new GridLayoutManager(AppCenterActivity.this, 5);
                recommendRecyclerView.setLayoutManager(gridLayoutManager);
                RecommendAppListAdapter recommendAppListAdapter = new RecommendAppListAdapter(AppCenterActivity.this, listPosition);
                recommendRecyclerView.setAdapter(recommendAppListAdapter);
                recommendAppListAdapter.setOnRecommendItemClickListener(new OnRecommendItemClickListener() {
                    @Override
                    public void onRecommendItemClick(View view, int position) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("app", appList.get(appListIndex).get(position));
                        IntentUtils.startActivity(AppCenterActivity.this, AppDetailActivity.class, bundle);
                    }
                });
            }
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return (adsList.size() == 0 ? appList.size() : (appList.size() + 1));
        }
    }

    /**
     * 分类的adapter
     */
    class CategoriesAppAdapter extends BaseAdapter {
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(AppCenterActivity.this).
                    inflate(R.layout.app_center_category_item, null);
            ((TextView) convertView.findViewById(R.id.app_center_categories_item_txt)).
                    setText(categoryAppList.get(position).getCategoryName());
            ImageDisplayUtils.getInstance().displayImage((ImageView) convertView.
                            findViewById(R.id.app_center_categories_icon_img),
                    categoryAppList.get(position).getCategoryIco(), R.drawable.icon_app_center_categories);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return categoryAppList.size();
        }
    }

    /**
     * 初始化banner
     *
     * @param mViewPagerContainer
     * @param mViewPager
     */
    private void initRecommendViewPager(RelativeLayout mViewPagerContainer, final ViewPager mViewPager) {
        mViewPager.setAdapter(new AdsAppPagerAdapter());
        startAutoSlide(mViewPager);
        if (adsList.size() > 1) {
            mViewPager.setCurrentItem(1);
        }
        //将容器的触摸事件反馈给ViewPager
        mViewPagerContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });
    }

    /**
     * ViewPager自动滚动
     *
     * @param mViewPager
     */
    private void startAutoSlide(final ViewPager mViewPager) {
        if (!(mViewPager.getAdapter().getCount() > 2)) {
            return;
        }
        //定时轮播图片，需要在主线程里面修改 UI
        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_VIEWPAGER:
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        break;
                }
            }
        };
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(UPDATE_VIEWPAGER);
            }
        };
        timer.schedule(timerTask, 3000, 3000);
    }

    /**
     * banner的adapter
     */
    class AdsAppPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return adsList.size() == 0 ? 0 : Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final int newPosition = position % (adsList.size() == 0 ? 1 : adsList.size());
            AppAdsBean appAdsBean = adsList.get(newPosition);
            ImageView imageView = new ImageView(AppCenterActivity.this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageDisplayUtils.getInstance().displayImage(imageView, appAdsBean.getLegend(), R.drawable.app_center_banner);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = adsList.get(newPosition).getUri();
                    openDetailByUri(uri);
                }
            });
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    /**
     * 根据uri分发打开请求
     *
     * @param uri
     */
    private void openDetailByUri(String uri) {
        if (!StringUtils.isBlank(uri)) {
            Intent intent = new Intent();
            if (uri.startsWith("http")) {
                intent.setClass(AppCenterActivity.this, ImpActivity.class);
                intent.putExtra("uri", uri);
                startActivity(intent);
            } else if (uri.startsWith(APP_CENTER_APP_NAME_PROTOCOL)) {
                Uri appUri = Uri.parse(uri);
                String appId = appUri.getPathSegments().get(1);
                openAppDetailByAppId(appId);
            } else if (uri.startsWith(APP_CENTER_CATEGORY_PROTOCOL)) {
                Uri categoryIdUri = Uri.parse(uri);
                String categoryId = categoryIdUri.getPathSegments().get(1);
                openCategoryDetailByCategoryId(categoryId);
            }
        }
    }

    /**
     * 根据appId打开App详情
     *
     * @param appId
     */
    private void openAppDetailByAppId(String appId) {
        App appWithId = new App();
        appWithId.setAppID(appId);
        for (int i = 0; i < categoryAppList.size(); i++) {
            int appIndex = categoryAppList.get(i).getAppItemList().indexOf(appWithId);
            if (appIndex != -1) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("app", categoryAppList.get(i).getAppItemList().get(appIndex));
                IntentUtils.startActivity(AppCenterActivity.this, AppDetailActivity.class, bundle);
                break;
            }
        }
    }

    /**
     * 根据categoryId打开category
     *
     * @param categoryId
     */
    private void openCategoryDetailByCategoryId(String categoryId) {
        AppGroupBean appGroupBean = new AppGroupBean();
        appGroupBean.setCategoryID(categoryId);
        int appCategoryIndex = categoryAppList.indexOf(appGroupBean);
        if (appCategoryIndex != -1) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(APP_CENTER_APPLIST, (Serializable) categoryAppList.get(appCategoryIndex).getAppItemList());
            bundle.putString(APP_CENTER_CATEGORY_NAME, categoryAppList.get(appCategoryIndex).getCategoryName());
            IntentUtils.startActivity(AppCenterActivity.this, AppCenterMoreActivity.class, bundle);
        }
    }

    /**
     * 推荐应用的Adapter
     */
    public class RecommendAppListAdapter extends RecyclerView.Adapter<RecommendAppListAdapter.RecommendViewHolder> {
        private LayoutInflater inflater;
        private int listPosition;
        private OnRecommendItemClickListener onRecommendItemClickListener;

        public RecommendAppListAdapter(Context context, int listPosition) {
            inflater = LayoutInflater.from(context);
            this.listPosition = listPosition;
        }

        @Override
        public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app_center_recommand_app_item, null);
            RecommendViewHolder viewHolder = new RecommendViewHolder(view);
            viewHolder.recommendAppImg = (ImageView) view.findViewById(R.id.app_center_recommand_app_img);
            viewHolder.recommendAppText = (TextView) view.findViewById(R.id.app_center_recommand_app_text);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecommendViewHolder holder, final int position) {
            int size = adsList.size() == 0 ? listPosition : (listPosition - 1);
            ImageDisplayUtils.getInstance().displayImage(holder.recommendAppImg, appList.get(size).get(position).getAppIcon(), R.drawable.ic_app_default);
            holder.recommendAppText.setText(appList.get(size).get(position).getAppName());
            if (onRecommendItemClickListener != null) {
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRecommendItemClickListener.onRecommendItemClick(holder.itemView, position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int size = (adsList.size() == 0 ? listPosition : (listPosition - 1));
            return (appList.get(size).size() > 10 ? 10 : appList.get(size).size());
        }

        public void setOnRecommendItemClickListener(OnRecommendItemClickListener l) {
            this.onRecommendItemClickListener = l;
        }

        public class RecommendViewHolder extends RecyclerView.ViewHolder {
            ImageView recommendAppImg;
            TextView recommendAppText;
            public RecommendViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public interface OnRecommendItemClickListener {
        void onRecommendItemClick(View view, int position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (addAppReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(addAppReceiver);
            addAppReceiver = null;
        }
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnAllAppsSuccess(GetAllAppResult getAllAppResult) {
            recommendCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
            appList = getAllAppResult.getRecommendList();
            adsList = getAllAppResult.getAdsList();
            categoryAppList = getAllAppResult.getCategoriesGroupBeanList();
            recommendAppAdapter.notifyDataSetChanged();
            categoriesAppAdapter.notifyDataSetChanged();
            recommendSwipeRefreshLayout.setRefreshing(false);
            classSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void returnAllAppsFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(AppCenterActivity.this, error, errorCode);
            recommendCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
            recommendSwipeRefreshLayout.setRefreshing(false);
            classSwipeRefreshLayout.setRefreshing(false);
        }
    }
}