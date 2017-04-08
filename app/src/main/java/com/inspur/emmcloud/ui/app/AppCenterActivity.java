package com.inspur.emmcloud.ui.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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
import com.inspur.emmcloud.bean.AppGroupBean;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.util.DeviceUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.CircularProgress;
import com.inspur.emmcloud.widget.DecoratorViewPager;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.draggrid.AppCenterDragAdapter;
import com.inspur.emmcloud.widget.draggrid.DragGridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用中心页面 com.inspur.emmcloud.ui.AppCenterActivity create at 2016年8月31日
 * 下午2:54:47
 */
public class AppCenterActivity extends BaseActivity {

    private static final String ACTION_NAME = "add_app";
    private MyAppAPIService apiService;
    private ViewPager viewPager;
    private CircularProgress recommandCircleProgress, classCircleProgress;
    private ListView recommandListView, classListView;
    private List<App> recommandAppList = new ArrayList<App>();
    private List<AppGroupBean> categorieAppList = new ArrayList<AppGroupBean>();
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
        recommandListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("app", recommandAppList.get(position));
                IntentUtils.startActivity(AppCenterActivity.this, AppDetailActivity.class, bundle);
            }
        });
    }

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
                    int recommandAppIndex = recommandAppList.indexOf(addApp);
                    if (recommandAppIndex != -1) {
                        recommandAppList.get(recommandAppIndex).setUseStatus(1);
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

    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnAllAppsSuccess(GetAllAppResult getAllAppResult) {
            recommandCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
            recommandAppList = getAllAppResult.getRecommandAppList();
            recommandAppAdapter = new RecommondAppAdapter();
            recommandListView.setAdapter(recommandAppAdapter);
            recommandAppAdapter.notifyDataSetChanged();
            categorieAppList = getAllAppResult.getCategoriesGroupBeanList();
            categoriesAppAdapter = new CategoriesAppAdapter();
        }

        @Override
        public void returnAllAppsFail(String error) {
            recommandCircleProgress.setVisibility(View.GONE);
            classCircleProgress.setVisibility(View.GONE);
        }

    }

    class RecommondAppAdapter extends BaseAdapter {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//			App app = recommandAppList.get(position);
            if (recommandAppList.size() > 0 && position == 0) {
                convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.my_app_recommand_banner_app_item_view, null);
                RelativeLayout appRecomandLayout = (RelativeLayout) convertView.findViewById(R.id.app_center_recomand_viewpager_layout);
                DecoratorViewPager viewPager = (DecoratorViewPager) convertView.findViewById(R.id.app_center_banner_viewpager);
                viewPager.setNestedpParent((ViewGroup) viewPager.getParent());
                initRecomandViewPager(appRecomandLayout, viewPager);
            } else {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_app_recommand_app_item_view, null);
                RecyclerView recomandRecyclerView = (RecyclerView) convertView.findViewById(R.id.app_center_recomand_recycleview);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppCenterActivity.this);
                recomandRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(15));
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recomandRecyclerView.setLayoutManager(linearLayoutManager);
                RecommandAppListAdapter recommandAppListAdapter = new RecommandAppListAdapter(AppCenterActivity.this,null);
                recommandAppListAdapter.setOnRecommandItemClickListener( new OnRecommandItemClickListener() {
                    @Override
                    public void onRecommandItemClick(View view, int position) {
                        ToastUtils.show(AppCenterActivity.this,"点击了第"+position+"个应用");
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
            return 5;
        }


    }


    class CategoriesAppAdapter extends BaseAdapter {
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.app_center_drag_item, null);
            TextView appGroupNameText = (TextView) convertView.findViewById(R.id.app_center_title_text);
            appGroupNameText.setText(categorieAppList.get(position).getCategoryName());
            TextView appGroupMoreText = (TextView) convertView.findViewById(R.id.app_center_more_text);
            if (categorieAppList.get(position).getAppItemList().size() > 4) {
                appGroupMoreText.setVisibility(View.VISIBLE);
            } else {
                appGroupMoreText.setVisibility(View.GONE);
            }
            AppCenterDragAdapter dragGridViewAdapter = new AppCenterDragAdapter(
                    AppCenterActivity.this, categorieAppList.get(position).getAppItemList());
            DragGridView dragGridView = (DragGridView) convertView
                    .findViewById(R.id.app_list_draggrid);
            dragGridView.setAdapter(dragGridViewAdapter);
            appGroupMoreText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("appList", (Serializable) categorieAppList.get(position).getAppItemList());
                    IntentUtils.startActivity(AppCenterActivity.this, AppCenterMoreActivity.class, bundle);
                }
            });
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

    ;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (addAppReceiver != null) {
            unregisterReceiver(addAppReceiver);
            addAppReceiver = null;
        }
    }


    private void initRecomandViewPager(RelativeLayout mViewPagerContainer, final ViewPager mViewPager) {
        //设置ViewPager的布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                DeviceUtils.getWindowWidth(this) * 8 / 10,
                DeviceUtils.getWindowHeight(this) * 2 / 10);
        /**** 重要部分  ******/
        //clipChild用来定义他的子控件是否要在他应有的边界内进行绘制。 默认情况下，clipChild被设置为true。 也就是不允许进行扩展绘制。
        mViewPager.setClipChildren(false);
        //父容器一定要设置这个，否则看不出效果
        mViewPagerContainer.setClipChildren(false);


//        mViewPager.setLayoutParams(params);
        //为ViewPager设置PagerAdapter
        mViewPager.setAdapter(new RecommandAppPagerAdapter());
        //设置ViewPager切换效果，即实现画廊效果
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        //设置预加载数量
        mViewPager.setOffscreenPageLimit(2);
        //设置每页之间的左右间隔
        mViewPager.setPageMargin(-60);

        //将容器的触摸事件反馈给ViewPager
        mViewPagerContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return mViewPager.dispatchTouchEvent(event);
            }

        });


    }


    class RecommandAppPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //return viewList==null?0:viewList.size();
            return Integer.MAX_VALUE;
//            return recommandAppList == null?0:recommandAppList.size();//ViewPager里的个数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int newPosition = position % recommandAppList.size();
            App app = recommandAppList.get(newPosition);
            ImageView imageView = new ImageView(AppCenterActivity.this);
            new ImageDisplayUtils(getApplicationContext(), R.drawable.icon_empty_icon).display(imageView, app.getAppIcon());
            ((ViewPager) container).addView(imageView);
            return imageView;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private RelativeLayout mViewPagerContainer;

        public MyOnPageChangeListener(RelativeLayout mViewPagerContainer) {
            this.mViewPagerContainer = mViewPagerContainer;
        }

        @Override
        public void onPageSelected(int position) {
            //这里做切换ViewPager时，底部RadioButton的操作
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mViewPagerContainer != null) {
                mViewPagerContainer.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /**
     * 实现的原理是，在当前显示页面放大至原来的MAX_SCALE
     * 其他页面才是正常的的大小MIN_SCALE
     */
    class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MAX_SCALE = 1.2f;
        private static final float MIN_SCALE = 1.0f;//0.85f

        @Override
        public void transformPage(View view, float position) {
            //setScaleY只支持api11以上
            if (position < -1) {
                view.setScaleX(MIN_SCALE);
                view.setScaleY(MIN_SCALE);
            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            { // [-1,1]
                float scaleFactor = MIN_SCALE + (1 - Math.abs(position)) * (MAX_SCALE - MIN_SCALE);
                view.setScaleX(scaleFactor);
                //每次滑动后进行微小的移动目的是为了防止在三星的某些手机上出现两边的页面为显示的情况
                if (position > 0) {
                    view.setTranslationX(-scaleFactor * 2);
                } else if (position < 0) {
                    view.setTranslationX(scaleFactor * 2);
                }
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                view.setScaleX(MIN_SCALE);
                view.setScaleY(MIN_SCALE);
            }

        }

    }

    public class RecommandAppListAdapter extends RecyclerView.Adapter<RecommandAppListAdapter.RecommandViewHolder>{

        private LayoutInflater inflater;
        public RecommandAppListAdapter(Context context,List<App> recommandList){
            inflater = LayoutInflater.from(context);
        }
        private OnRecommandItemClickListener onRecommandItemClickListener;

        public void setOnRecommandItemClickListener(OnRecommandItemClickListener l){
            this.onRecommandItemClickListener = l;
        }

        @Override
        public RecommandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app_center_recommand_app_item,null);
            RecommandViewHolder viewHolder = new RecommandViewHolder(view);
            viewHolder.recommandAppImg = (ImageView) view.findViewById(R.id.app_center_recommand_app_img);
            viewHolder.recommandAppText = (TextView) view.findViewById(R.id.app_center_recommand_app_text);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecommandViewHolder holder, final int position) {
            holder.recommandAppImg.setImageResource(R.drawable.ic_launcher);
            holder.recommandAppText.setText("App名称");
            if(onRecommandItemClickListener != null){
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRecommandItemClickListener.onRecommandItemClick(holder.itemView,position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
//            return recommandAppList == null? 0:recommandAppList.size();
            return 10;
        }

        public  class RecommandViewHolder extends RecyclerView.ViewHolder{
            ImageView recommandAppImg;
            TextView recommandAppText;
            public RecommandViewHolder(View itemView) {
                super(itemView);
            }
        }

    }

    public interface OnRecommandItemClickListener{
        void onRecommandItemClick(View view,int position);
    }

}