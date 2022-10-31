package com.inspur.emmcloud.application.adapter;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.util.List;

/**
 * 实现ViewPager页卡
 */
public class MyViewPagerAdapter extends PagerAdapter {
    private List<View> viewList;
    private String[] titles;

    public MyViewPagerAdapter(List<View> array, String[] titles) {
        this.viewList = array;
        this.titles = titles;
    }

    public MyViewPagerAdapter(Context context, List<View> array) {
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
