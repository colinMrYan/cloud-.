package com.inspur.emmcloud.ui.app.groupnews;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.NewsTitle;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
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

	private MyPagerAdapter pagerAdapter;
	private LoadingDialog loadingDlg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_news);
		loadingDlg = new LoadingDialog(GroupNewsActivity.this);
		getNewTitles();
		recordUserClickNews();
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
	 *获取新闻类别
	 */
	private void getNewTitles() {
		if (NetUtils.isNetworkConnected(GroupNewsActivity.this)) {
			loadingDlg.show();
			MyAppAPIService apiService = new MyAppAPIService(GroupNewsActivity.this);
			apiService.setAPIInterface(new WebService());
			apiService.getNewsTitles();
		}
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {
		List<NewsTitle> titleList;
		public MyPagerAdapter(FragmentManager fm,List<NewsTitle> titleList) {
			super(fm);
			this.titleList = titleList;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titleList.get(position).getTitle();
		}

		@Override
		public int getCount() {
			return titleList.size();
		}

		@Override
		public Fragment getItem(int position) {
			NewsTitle title = titleList.get(position);
			return new GroupNewsCardFragment(position, title.getNcid(), title.getTitle(), title.isHasExtraPermission());
		}
	}

	public void onClick(View v) {
		finish();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		pagerAdapter.getItem(arg0);
	}

	/**
	 * 集团新闻pager设置
	 * @param getNewsTitleResult
	 */
	private void handleGroupNews(GetNewsTitleResult getNewsTitleResult) {
		List<NewsTitle> titleList = getNewsTitleResult.getTitleList();
		if(titleList.size() == 0){
			ToastUtils.show(GroupNewsActivity.this,R.string.news_no_news);
			finish();
		}
		//pagerAdapter
		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),titleList);
		//获取获取viewPager，并设置
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		//设置page的间距
		int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		viewPager.setPageMargin(pageMargin);
		viewPager.setAdapter(pagerAdapter);
		//获取PagerSlidingTabStrip，并初始化设置
		PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pagerSlidingTabStrip.setDividerColor(getResources().getColor(R.color.content_border));
		pagerSlidingTabStrip.setIndicatorColor(getResources().getColor(R.color.header_bg));
		pagerSlidingTabStrip.setTextSize(DensityUtil.sp2px(getApplicationContext(),6));
		pagerSlidingTabStrip.setTextColor(R.drawable.selector_viewpager_tab_text);
		pagerSlidingTabStrip.setOnPageChangeListener(this);
		//设置导航器和viewPager关联
		pagerSlidingTabStrip.setViewPager(viewPager);
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnGroupNewsTitleSuccess(
				GetNewsTitleResult getNewsTitleResult) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			handleGroupNews(getNewsTitleResult);
		}

		@Override
		public void returnGroupNewsTitleFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(GroupNewsActivity.this, error,errorCode);
            finish();
		}
	}
}
