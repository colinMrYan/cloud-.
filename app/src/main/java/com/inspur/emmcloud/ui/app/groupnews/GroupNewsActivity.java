package com.inspur.emmcloud.ui.app.groupnews;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.bean.Titles;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.PagerSlidingTabStrip;

import java.util.List;
/**
 * 集团新闻
 * com.inspur.emmcloud.ui.GroupNewsActivity
 * create at 2016年9月5日 上午10:31:56
 */
public class GroupNewsActivity extends BaseFragmentActivity implements
		OnPageChangeListener {

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	private LoadingDialog loadingDlg;
	private List<Titles> titles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_group_news);
		init();
		getNewTitles();
		//当应用打开方式为scheme方式时才记录，如果从应用打开新闻，由应用记录
		if(getIntent().getScheme() != null){
			recordUserClickNews();
		}
	}

	/**
	 * 记录用户点击新闻功能
	 */
	private void recordUserClickNews() {
		PVCollectModel pvCollectModel = new PVCollectModel();
		pvCollectModel.setFunctionID("news");
		pvCollectModel.setFunctionType("find");
		pvCollectModel.setCollectTime(System.currentTimeMillis());
		PVCollectModelCacheUtils.saveCollectModel(GroupNewsActivity.this,pvCollectModel);
	}

	/**
	 * 初始化
	 */
	private void init() {
		String token = ((MyApplication)getApplicationContext())
				.getToken();
		checkToken(token);
		loadingDlg = new LoadingDialog(GroupNewsActivity.this);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setDividerColor(getResources().getColor(R.color.content_border));
		tabs.setIndicatorColor(getResources().getColor(R.color.header_bg));
		tabs.setTextSize(16);
		tabs.setTextColor(R.drawable.selector_viewpager_tab_text);
		pager = (ViewPager) findViewById(R.id.pager);
		tabs.setOnPageChangeListener(this);

	}

	/**
	 * 检查token，如果token不存在则跳转到登录页面
	 * @param token
	 */
	private void checkToken(String token) {
		if(StringUtils.isBlank(token)){
			ToastUtils.show(GroupNewsActivity.this, GroupNewsActivity.this.getString(R.string.authorization_expired));
			Intent intent = new Intent();
			intent.setClass(GroupNewsActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	}

	/**
	 *获取新闻类别
	 */
	private void getNewTitles() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(GroupNewsActivity.this)) {
			loadingDlg.show();
			MyAppAPIService apiService = new MyAppAPIService(GroupNewsActivity.this);
			apiService.setAPIInterface(new WebService());
			apiService.getNewsTitles();
		}
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles.get(position).getTitle();
		}

		@Override
		public int getCount() {
			return titles.size();
		}

		@Override
		public Fragment getItem(int position) {
			return new GroupNewsCardFragment(position, titles.get(position)
					.getNcid(),titles.get(position).getTitle(),titles.get(position).isHasExtraPermission());
		}

	}

	public void onClick(View v) {
		finish();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		adapter.getItem(arg0);
		adapter.notifyDataSetChanged();
	}

	class WebService extends APIInterfaceInstance {

		@Override
		public void returnGroupNewsTitleSuccess(
				GetNewsTitleResult getNewsTitleResult) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}

			titles = getNewsTitleResult.getTitlesList();
			if(titles.size() == 0){
				ToastUtils.show(GroupNewsActivity.this,R.string.news_no_news);
				finish();
			}
			
//			//调整新闻的显示顺序，把单位新闻和集团新闻交换位置，不用元素交换的方法是因为决定下面新闻内容的是ncid
//			if (titles.get(0).getNcid().equals("1")) {
//				titles.get(0).setNcid("3");
//				titles.get(0).setTitle("集团新闻");
//			}
//			if (titles.get(2).getNcid().equals("3")) {
//				titles.get(2).setNcid("1");
//				titles.get(2).setTitle("单位新闻");;
//			}
			adapter = new MyPagerAdapter(getSupportFragmentManager());
			pager.setAdapter(adapter);

			int pageMargin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
							.getDisplayMetrics());
			pager.setPageMargin(pageMargin);

			tabs.setViewPager(pager);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnGroupNewsTitleFail(String error,int errorCode) {

			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(GroupNewsActivity.this, error,errorCode);
		}

	}

}
