package com.inspur.emmcloud.ui.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用详情中图片放大显示页面
 * @author Administrator
 *
 */
public class AppImgDisPlayActivity extends BaseActivity {

	private ImageView[] pointImgs;
	private List<View> intrList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		((MyApplication)getApplicationContext()).addActivity(AppImgDisPlayActivity.this);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		intrList = new ArrayList<View>();
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		ViewGroup pointGroup = (ViewGroup) findViewById(R.id.viewgroup);
		int pageNum = 5;
		for (int i = 0; i < pageNum; i++) {
			View view = layoutInflater.inflate(R.layout.guide_page1, null);
			((ImageView)view.findViewById(R.id.skip_btn)).setVisibility(View.GONE);

			((ImageView)view.findViewById(R.id.intr_img)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			intrList.add(view);
		}
		pointImgs = new ImageView[intrList.size()];
		int POINTSIZE = 15;
		for (int i = 0; i < intrList.size(); i++) {
			ImageView pointImg = new ImageView(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					POINTSIZE, POINTSIZE);
			lp.setMargins(POINTSIZE, 0, POINTSIZE, 0);
			pointImg.setLayoutParams(lp);

			if (i == 0) {
				pointImg.setBackgroundResource(R.drawable.icon_indicator_sel);
			} else {
				pointImg.setBackgroundResource(R.drawable.icon_indicator_nor);
			}
			pointImgs[i] = pointImg;
			pointGroup.addView(pointImgs[i]);
		}
		int currentIndex = getIntent().getExtras().getInt("currentIndex");
		viewPager.setAdapter(new MyPagerAdapter());
		viewPager.setOnPageChangeListener(pageChangeListener);
		viewPager.setCurrentItem(currentIndex);
	}
	
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		public void onPageSelected(int arg0) {
			for (int i = 0; i < pointImgs.length; i++) {
				if (i == arg0) {
					pointImgs[i]
							.setBackgroundResource(R.drawable.icon_indicator_sel);
				} else {
					pointImgs[i]
							.setBackgroundResource(R.drawable.icon_indicator_nor);
				}
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

	private class MyPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView(intrList.get(position));
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return intrList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(intrList.get(position));
			return intrList.get(position);
		}

	}


}
