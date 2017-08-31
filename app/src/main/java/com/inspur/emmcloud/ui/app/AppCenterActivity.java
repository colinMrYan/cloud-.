package com.inspur.emmcloud.ui.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
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
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.AppAdsBean;
import com.inspur.emmcloud.bean.AppGroupBean;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircularProgress;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 应用中心页面 com.inspur.emmcloud.ui.AppCenterActivity create at 2016年8月31日
 * 下午2:54:47
 */
public class AppCenterActivity extends BaseActivity {
    private static final String ACTION_NAME = "add_app";
    private static final int UPTATE_VIEWPAGER = 1;
    private MyAppAPIService apiService;
    private ViewPager viewPager;
    private CircularProgress recommandCircleProgress, classCircleProgress;
    private ListView recommandListView, classListView;
    private List<AppAdsBean> adsList = new ArrayList<>();
    private List<AppGroupBean> categorieAppList = new ArrayList<AppGroupBean>();
    private List<List<App>> appList = new ArrayList<>();
    private BaseAdapter recommandAppAdapter;
    private BaseAdapter categoriesAppAdapter;
    private BroadcastReceiver addAppReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_center);
        ((MyApplication) getApplicationContext())
                .addActivity(AppCenterActivity.this);
        initView();
        getAllApp();
        registerReceiver();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        apiService = new MyAppAPIService(AppCenterActivity.this);
        apiService.setAPIInterface(new WebService());
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        View recommendView = LayoutInflater.from(this).inflate(
                R.layout.app_recommend_layout, null);
        View classView = LayoutInflater.from(this).inflate(
                R.layout.app_categories_layout, null);
        recommandListView = (ListView) recommendView.findViewById(R.id.list);
        classListView = (ListView) classView.findViewById(R.id.app_center_categories_list);
        recommandListView.setAdapter(recommandAppAdapter);
        recommandCircleProgress = (CircularProgress) recommendView
                .findViewById(R.id.circle_progress);
        classCircleProgress = (CircularProgress) classView
                .findViewById(R.id.app_center_categories_circle_progress);
        List<View> viewList = new ArrayList<View>();
        viewList.add(recommendView);
        viewList.add(classView);
        viewPager.setAdapter(new MyViewPagerAdapter(viewList, null));
        viewPager.addOnPageChangeListener(new PageChangeListener());
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
            LogUtils.jasonDebug("arg0=" + arg0);
            int recommandTabTextColor = arg0 == 0 ? Color.parseColor("#4990E2")
                    : Color.parseColor("#999999");
            int classTabTextColor = arg0 == 1 ? Color.parseColor("#4990E2")
                    : Color.parseColor("#999999");
            int recommandTabFooterViewVisible = arg0 == 0 ? View.VISIBLE
                    : View.INVISIBLE;
            int classTabFooterViewVisible = arg0 == 1 ? View.VISIBLE
                    : View.INVISIBLE;
            ((TextView) findViewById(R.id.recommand_tab_text))
                    .setTextColor(recommandTabTextColor);
            ((TextView) findViewById(R.id.class_tab_text))
                    .setTextColor(classTabTextColor);
            ((TextView) findViewById(R.id.class_tab_text))
                    .setTextColor(classTabTextColor);
            findViewById(R.id.recommand_tab_footer_view).setVisibility(
                    recommandTabFooterViewVisible);
            findViewById(R.id.class_tab_footer_view).setVisibility(
                    classTabFooterViewVisible);
            if (arg0 == 0) {
                recommandListView.setAdapter(recommandAppAdapter);
            } else if (arg0 == 1) {
                classListView.setAdapter(categoriesAppAdapter);
                classListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("appList", (Serializable) categorieAppList.get(position).getAppItemList());
                        bundle.putString("category_name",categorieAppList.get(position).getCategoryName());
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
            apiService.getNewAllApps();
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
                    int recommandAppIndex = -1,groupIndex = 0;
                    Iterator<List<App>> appItemList = appList.listIterator();
                    while (appItemList.hasNext()){
                        recommandAppIndex = appItemList.next().indexOf(addApp);
                        groupIndex = groupIndex + 1;
                    }
                    if (recommandAppIndex != -1) {
                        appList.get(groupIndex).get(recommandAppIndex).setUseStatus(1);
                    }
                    for (int i = 0; i < categorieAppList.size(); i++) {
                        int categoriesAppIndex = categorieAppList.get(i).getAppItemList().indexOf(addApp);
                        if (categoriesAppIndex != -1) {
                            categorieAppList.get(i).getAppItemList().get(categoriesAppIndex).setUseStatus(1);
                        }
                    }
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        registerReceiver(addAppReceiver, myIntentFilter);
    }

    /**
     * 推荐的adapter
     */
    class RecommondAppAdapter extends BaseAdapter {
        @Override
        public View getView(final int listPosition, View convertView, ViewGroup parent) {
            if ((adsList != null && adsList.size()>0)&&listPosition == 0) {
                convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.my_app_recommand_banner_app_item_view, null);
                RelativeLayout appRecomandLayout = (RelativeLayout) convertView.findViewById(R.id.app_center_recomand_viewpager_layout);
                ViewPager viewPager = (ViewPager) convertView.findViewById(R.id.app_center_banner_viewpager);
                viewPager.setOffscreenPageLimit(3);
                viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                int pagerWidth = (int) (getResources().getDisplayMetrics().widthPixels * 5.0f / 5.0f);
                ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.LayoutParams(pagerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    lp.width = pagerWidth;
                }
                viewPager.setLayoutParams(lp);
                viewPager.setPageMargin(DensityUtil.dip2px(AppCenterActivity.this,5));
                viewPager.setClipChildren(false);
                //需要Scale进入和退出时打开这里
//                viewPager.setPageTransformer(true, new ScaleInTransformer());
//                viewPager.setNestedpParent((ViewGroup) viewPager.getParent());
                initRecomandViewPager(appRecomandLayout, viewPager);
            } else {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_app_recommand_app_item_view, null);
                final int size = adsList.size() == 0 ? listPosition : (listPosition -1);
                List<App> appItemList = appList.get(size);
                if(appList.size() > 0 && appItemList != null && appItemList.size() > 0){
                    ((TextView)convertView.findViewById(R.id.app_center_recommand_text)).setText(appList.get(size).get(0).getCategoryName());
                }
                if(appItemList.size() <= 5){
                    (convertView.findViewById(R.id.app_center_more_text)).setVisibility(View.GONE);
                }
                convertView.findViewById(R.id.app_center_recommand_layout).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("appList", (Serializable) appList.get(size));
                        bundle.putString("category_name",appList.get(size).get(0).getCategoryName());
                        IntentUtils.startActivity(AppCenterActivity.this, AppCenterMoreActivity.class, bundle);
                    }
                });
                RecyclerView recomandRecyclerView = (RecyclerView) convertView.findViewById(R.id.app_center_recomand_recycleview);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppCenterActivity.this);
                recomandRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(AppCenterActivity.this,11)));
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recomandRecyclerView.setLayoutManager(linearLayoutManager);
                RecommandAppListAdapter recommandAppListAdapter = new RecommandAppListAdapter(AppCenterActivity.this, listPosition);
                recommandAppListAdapter.setOnRecommandItemClickListener(new OnRecommandItemClickListener() {
                    @Override
                    public void onRecommandItemClick(View view, int position) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("app", appList.get(size).get(position));
                        IntentUtils.startActivity(AppCenterActivity.this, AppDetailActivity.class, bundle);
                    }
                });
                recomandRecyclerView.setAdapter(recommandAppListAdapter);
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
            int hasdataSize = getAppListHasData();
            return (adsList.size() == 0? appList.size():(hasdataSize + 1));
        }

        private int getAppListHasData() {
            int size = 0;
            for(int i = 0; i < appList.size(); i++){
                if(appList.get(i).size() > 0){
                    size = size + 1;
                }
            }
            return size ;
        }


    }

    /**
     * 分类的adapter
     */
    class CategoriesAppAdapter extends BaseAdapter {
        ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(AppCenterActivity.this,R.drawable.icon_app_center_categories);
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.app_center_category_item, null);
            ((TextView) convertView.findViewById(R.id.app_center_categories_item_txt)).setText(categorieAppList.get(position).getCategoryName());
            ImageView appCenterCategoryIcon = (ImageView) convertView.findViewById(R.id.app_center_categories_icon_img);
            String appCenterCategoryIconUrl = categorieAppList.get(position).getCategoryIco();
            if(!StringUtils.isBlank(appCenterCategoryIconUrl)){
                imageDisplayUtils.display(appCenterCategoryIcon,appCenterCategoryIconUrl);
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
            return categorieAppList.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (addAppReceiver != null) {
            unregisterReceiver(addAppReceiver);
            addAppReceiver = null;
        }
    }

    /**
     * 初始化banner
     * @param mViewPagerContainer
     * @param mViewPager
     */
    private void initRecomandViewPager(RelativeLayout mViewPagerContainer, final ViewPager mViewPager) {
        mViewPager.setAdapter(new RecommandAppPagerAdapter());
        startAutoSlide(mViewPager);
        if (adsList != null && adsList.size() > 1) {
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
                    case UPTATE_VIEWPAGER:
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        break;
                }
            }
        };
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(UPTATE_VIEWPAGER);
            }
        };
        timer.schedule(timerTask, 3000, 3000);
    }


    /**
     * banner的adapter
     */
    class RecommandAppPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (adsList == null ) {
                return 0;
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            int newPosition = position % (adsList.size() == 0 ? 1:adsList.size());
            AppAdsBean app = adsList.get(newPosition);
            ImageView imageView = new ImageView(AppCenterActivity.this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            new ImageDisplayUtils(getApplicationContext(), R.drawable.app_center_banner).display(imageView, app.getLegend());
            ((ViewPager) container).addView(imageView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.YfcDebug("点击了banner第"+(position%(adsList.size()))+"个");//需要对广告banner处理点击事件时在此处编写代码
                }
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }

    /**
     * 推荐应用的Adapter
     */
    public class RecommandAppListAdapter extends RecyclerView.Adapter<RecommandAppListAdapter.RecommandViewHolder> {

        private LayoutInflater inflater;
        private int listPosition;
        ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(AppCenterActivity.this,R.drawable.icon_empty_icon);

        public RecommandAppListAdapter(Context context,int listPosition) {
            inflater = LayoutInflater.from(context);
            this.listPosition = listPosition;
        }
        private OnRecommandItemClickListener onRecommandItemClickListener;
        public void setOnRecommandItemClickListener(OnRecommandItemClickListener l) {
            this.onRecommandItemClickListener = l;
        }

        @Override
        public RecommandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app_center_recommand_app_item, null);
            RecommandViewHolder viewHolder = new RecommandViewHolder(view);
            viewHolder.recommandAppImg = (ImageView) view.findViewById(R.id.app_center_recommand_app_img);
            viewHolder.recommandAppText = (TextView) view.findViewById(R.id.app_center_recommand_app_text);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecommandViewHolder holder, final int position) {
            int size = adsList.size() == 0 ? listPosition:(listPosition - 1);
            imageDisplayUtils.display(holder.recommandAppImg,appList.get(size).get(position).getAppIcon());
            holder.recommandAppText.setText(appList.get(size).get(position).getAppName());
            if (onRecommandItemClickListener != null) {
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRecommandItemClickListener.onRecommandItemClick(holder.itemView, position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int size = (adsList.size() == 0? listPosition:(listPosition -1));
            return appList.get(size).size();
        }

        public class RecommandViewHolder extends RecyclerView.ViewHolder {
            ImageView recommandAppImg;
            TextView recommandAppText;
            public RecommandViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public interface OnRecommandItemClickListener {
        void onRecommandItemClick(View view, int position);
    }


    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnAllAppsSuccess(GetAllAppResult getAllAppResult) {
            recommandCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
            appList = getAllAppResult.getRecommendList();
            adsList = getAllAppResult.getAdsList();
            recommandAppAdapter = new RecommondAppAdapter();
            recommandListView.setAdapter(recommandAppAdapter);
            recommandAppAdapter.notifyDataSetChanged();
            categorieAppList = getAllAppResult.getCategoriesGroupBeanList();
            categoriesAppAdapter = new CategoriesAppAdapter();
        }

        @Override
        public void returnAllAppsFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(AppCenterActivity.this, error, errorCode);
            recommandCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
        }

    }

}