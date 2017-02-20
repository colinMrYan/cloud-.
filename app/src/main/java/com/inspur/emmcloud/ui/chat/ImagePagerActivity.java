package com.inspur.emmcloud.ui.chat;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.ImageDetailFragment;

/**
 * 图片查看器
 */
public class ImagePagerActivity extends BaseFragmentActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index"; 
	public static final String EXTRA_IMAGE_URLS = "image_urls";

	private HackyViewPager mPager;
	private TextView indicator;

	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);

		ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
		int pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		mPager = (HackyViewPager) findViewById(R.id.pager);
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
		mPager.setAdapter(mAdapter);
		indicator = (TextView) findViewById(R.id.indicator);
		if (mPager.getAdapter().getCount()>1) {
			indicator.setVisibility(View.VISIBLE);
		}
		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
		indicator.setText(text);
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = getString(R.string.viewpager_indicator, arg0 + 1, mPager.getAdapter().getCount());
				indicator.setText(text);
			}

		});
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mPager.setCurrentItem(pagerPosition);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public List<String> urls;

		public ImagePagerAdapter(FragmentManager fm, List<String> urls) {
			super(fm);
			this.urls = urls;
		}

		@Override
		public int getCount() {
			return urls == null ? 0 : urls.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = urls.get(position);
			return ImageDetailFragment.newInstance(url);
		}

	}
}
