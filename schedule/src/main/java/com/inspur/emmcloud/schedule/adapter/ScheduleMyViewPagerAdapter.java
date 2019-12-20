package com.inspur.emmcloud.schedule.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

/**
 * 实现ViewPager页卡
 */
public class ScheduleMyViewPagerAdapter extends PagerAdapter {
    private List<View> viewList;
    private String[] titles;

    public ScheduleMyViewPagerAdapter(List<View> array, String[] titles) {
        this.viewList = array;
        this.titles = titles;
    }

    public ScheduleMyViewPagerAdapter(Context context, List<View> array) {
        this.viewList = array;
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null) {
            return titles[position];
        }
        return super.getPageTitle(position);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(View arg0, int arg1) {
        ((ViewPager) arg0).addView(viewList.get(arg1));
        return viewList.get(arg1);
    }

    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }
}
