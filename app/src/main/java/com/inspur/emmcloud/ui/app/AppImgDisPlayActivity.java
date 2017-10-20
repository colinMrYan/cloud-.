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
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用详情中图片放大显示页面
 * @author Administrator
 *
 */
public class AppImgDisPlayActivity extends BaseActivity {
	private ImageView[] pointImgs;
	private List<View> intrList = new ArrayList<View>();
	private ArrayList<String> legendList = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		initView();
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		ViewGroup pointGroup = (ViewGroup) findViewById(R.id.viewgroup);
		legendList = getIntent().getStringArrayListExtra("legends");
		int pageNum = legendList.size();
		for (int i = 0; i < pageNum; i++) {
			View view = layoutInflater.inflate(R.layout.guide_page1, null);
			view.findViewById(R.id.skip_btn).setVisibility(View.GONE);
			view.findViewById(R.id.intr_img).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			intrList.add(view);
		}
		pointImgs = new ImageView[intrList.size()];
		int POINTSIZE = DensityUtil.dip2px(AppImgDisPlayActivity.this,5);
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
		viewPager.addOnPageChangeListener(pageChangeListener);
		viewPager.setCurrentItem(currentIndex);
	}

	/**
	 * 底部圆点
	 */
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
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private class MyPagerAdapter extends PagerAdapter {
		private ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_guide1);
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(intrList.get(position));
		}

		@Override
		public int getCount() {
			return intrList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			View view = intrList.get(position);
			ImageView imageView = (ImageView) view.findViewById(R.id.intr_img);
			imageDisplayUtils.displayImage(imageView,legendList.get(position));
			container.addView(view);
			return intrList.get(position);
		}
	}
}
