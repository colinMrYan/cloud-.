package com.inspur.emmcloud.ui.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.widget.CircularProgress;
import com.inspur.emmcloud.widget.draggrid.AppCenterDragAdapter;
import com.inspur.emmcloud.widget.draggrid.DragGridView;

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
			if(arg0 == 0){
				recommandListView.setAdapter(recommandAppAdapter);
			}else if(arg0 == 1){
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
					if(recommandAppIndex != -1){
						recommandAppList.get(recommandAppIndex).setUseStatus(1);
					}
					for (int i = 0; i < categorieAppList.size(); i++) {
						int categoriesAppIndex = categorieAppList.get(i).getAppItemList().indexOf(addApp);
						if(categoriesAppIndex != -1){
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
	
	class RecommondAppAdapter extends BaseAdapter{
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			App app = recommandAppList.get(position);
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_app_recommand_app_item_view, null);
			ImageView appIconImg = (ImageView)convertView.findViewById(R.id.app_icon_img);
			TextView appNameText = (TextView) convertView.findViewById(R.id.app_name_text);
			TextView appNoteText = (TextView) convertView.findViewById(R.id.app_note_text);
			new ImageDisplayUtils(getApplicationContext(), R.drawable.icon_empty_icon).display(appIconImg, app.getAppIcon());
			appNameText.setText(app.getAppName());
			appNoteText.setText(app.getNote());
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
			return recommandAppList.size();
		}
	};
	
	class CategoriesAppAdapter extends BaseAdapter {
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(AppCenterActivity.this).inflate(R.layout.app_center_drag_item, null);
			TextView appGroupNameText = (TextView) convertView.findViewById(R.id.app_center_title_text);
			appGroupNameText.setText(categorieAppList.get(position).getCategoryName());
			TextView appGroupMoreText = (TextView) convertView.findViewById(R.id.app_center_more_text);
			if(categorieAppList.get(position).getAppItemList().size()>4){
				appGroupMoreText.setVisibility(View.VISIBLE);
			}else {
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
					bundle.putSerializable("appList", (Serializable)categorieAppList.get(position).getAppItemList());
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
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (addAppReceiver != null) {
			unregisterReceiver(addAppReceiver);
			addAppReceiver = null;
		}
	}
}