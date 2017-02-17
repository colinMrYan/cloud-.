package com.inspur.emmcloud.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
/**
 * 实现ViewPager页卡
 *
 */
public class MyViewPagerAdapter extends PagerAdapter{
	private List<View> mLists;
	private String[] titles;
	public MyViewPagerAdapter( List<View> array,String[] titles) {
		this.mLists=array;
		this.titles = titles;
	}
	public MyViewPagerAdapter(Context context, List<View> array) {
		this.mLists=array;
	}
	@Override
	public int getCount() {
		return mLists.size();
	}

	
	
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
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
	public Object instantiateItem(View arg0, int arg1)
	{
		((ViewPager) arg0).addView(mLists.get(arg1));
		return mLists.get(arg1);
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2)
	{
		((ViewPager) arg0).removeView((View) arg2);
	}


}
