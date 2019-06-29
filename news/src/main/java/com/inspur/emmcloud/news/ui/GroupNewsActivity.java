package com.inspur.emmcloud.news.ui;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.gxz.PagerSlidingTabStrip;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.news.R;
import com.inspur.emmcloud.news.api.NewsAPIInsterfaceImpl;
import com.inspur.emmcloud.news.api.NewsApiService;
import com.inspur.emmcloud.news.bean.GetNewsTitleResult;
import com.inspur.emmcloud.news.bean.NewsTitle;

import java.util.List;

/**
 * 集团新闻
 * com.inspur.emmcloud.ui.GroupNewsActivity
 * create at 2016年9月5日 上午10:31:56
 */
@Route(path = "/group/news")
public class GroupNewsActivity extends BaseFragmentActivity {

    private MyPagerAdapter pagerAdapter;
    private LoadingDialog loadingDlg;


    @Override
    public void onCreate() {
        setContentView(R.layout.news_activity_group_news);
        loadingDlg = new LoadingDialog(GroupNewsActivity.this);
        getNewTitles();
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setStatus();
    }

    /**
     * 获取新闻类别
     */
    private void getNewTitles() {
        if (NetUtils.isNetworkConnected(GroupNewsActivity.this)) {
            loadingDlg.show();
            NewsApiService apiService = new NewsApiService(GroupNewsActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getNewsTitles();
        }
    }

    public void onClick(View v) {
        finish();
    }

    /**
     * 集团新闻pager设置
     *
     * @param getNewsTitleResult
     */
    private void handleGroupNews(GetNewsTitleResult getNewsTitleResult) {
        List<NewsTitle> titleList = getNewsTitleResult.getTitleList();
        if (titleList.size() == 0) {
            ToastUtils.show(GroupNewsActivity.this, R.string.news_no_news);
            finish();
        }
        //pagerAdapter
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), titleList);
        //获取获取viewPager，并设置
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        //设置page的间距
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        viewPager.setPageMargin(pageMargin);
        viewPager.setAdapter(pagerAdapter);
        //获取PagerSlidingTabStrip，并初始化设置
        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pagerSlidingTabStrip.setVisibility(View.VISIBLE);
        pagerSlidingTabStrip.setDividerColor(getResources().getColor(R.color.content_border));
        pagerSlidingTabStrip.setIndicatorColor(Color.parseColor("#00000000"));
        pagerSlidingTabStrip.setTextSize(17);
        pagerSlidingTabStrip.setUnderlineHeight(1);
        pagerSlidingTabStrip.setTextColor(Color.parseColor("#333333"));
        pagerSlidingTabStrip.setSelectedTextColor(Color.parseColor("#0F7BCA"));
        pagerSlidingTabStrip.setFadeEnabled(false);
        pagerSlidingTabStrip.setZoomMax(0);
        pagerSlidingTabStrip.setSmoothScrollWhenClickTab(false);
        //设置导航器和viewPager关联
        pagerSlidingTabStrip.setViewPager(viewPager);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        List<NewsTitle> titleList;

        public MyPagerAdapter(FragmentManager fm, List<NewsTitle> titleList) {
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

    class WebService extends NewsAPIInsterfaceImpl {
        @Override
        public void returnGroupNewsTitleSuccess(
                GetNewsTitleResult getNewsTitleResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            handleGroupNews(getNewsTitleResult);
        }

        @Override
        public void returnGroupNewsTitleFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(GroupNewsActivity.this, error, errorCode);
//            finish();
            findViewById(R.id.rl_no_news).setVisibility(View.VISIBLE);
        }
    }
}
