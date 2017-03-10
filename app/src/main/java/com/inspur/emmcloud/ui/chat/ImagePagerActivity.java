package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.ImageDetailFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片查看器
 */
public class ImagePagerActivity extends BaseFragmentActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index"; 
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	public static final String EXTRA_CURRENT_IMAGE_MSG = "channel_current_image_msg";
	public static final String EXTRA_IMAGE_MSG_LIST = "channel_image_msg_list";
	public static final String PHOTO_SELECT_X_TAG = "PHOTO_SELECT_X_TAG";
	public static final String PHOTO_SELECT_Y_TAG = "PHOTO_SELECT_Y_TAG";
	public static final String PHOTO_SELECT_W_TAG = "PHOTO_SELECT_W_TAG";
	public static final String PHOTO_SELECT_H_TAG = "PHOTO_SELECT_H_TAG";

	private HackyViewPager mPager;
	private TextView indicator;
	private int pagerPosition;
	private ArrayList<String> urlList = new ArrayList<>();
	private List<String> midList;
	private String cid;
	private int locationX;
	private int locationY;
	private int locationW;
	private int locationH;
	private Fragment currentFragment;

	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);
		StateBarColor.changeStateBarColor(this,R.color.black);
		init();

	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		getIntent().putExtras(intent);
		init();
	}

	private void init(){
		if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)){
			initIntentData();
		}else {
			urlList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
			pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		}
		locationX = getIntent().getIntExtra(PHOTO_SELECT_X_TAG, 0);
		locationY = getIntent().getIntExtra(PHOTO_SELECT_Y_TAG, 0);
		locationW = getIntent().getIntExtra(PHOTO_SELECT_W_TAG, 0);
		locationH = getIntent().getIntExtra(PHOTO_SELECT_H_TAG, 0);
		mPager = (HackyViewPager) findViewById(R.id.pager);
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urlList,midList);
		mPager.setAdapter(mAdapter);
		indicator = (TextView) findViewById(R.id.indicator);
//		if (mPager.getAdapter().getCount()>1) {
//			indicator.setVisibility(View.VISIBLE);
//		}
//		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
//		indicator.setText(text);
		// 更新下标
		mPager.addOnPageChangeListener(new OnPageChangeListener() {

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
//		if (savedInstanceState != null) {
//			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
//		}
		mPager.setCurrentItem(pagerPosition);
	}

	public Fragment getCurrentFragment(){
		return currentFragment;
	}

	private void initIntentData(){
		Msg currentMsg = (Msg) getIntent().getSerializableExtra(EXTRA_CURRENT_IMAGE_MSG);
		midList = new ArrayList<>();
		urlList = new ArrayList<>();
		cid = currentMsg.getCid();
		List<Msg> imgTypeMsgList = (List<Msg>)getIntent().getSerializableExtra(EXTRA_IMAGE_MSG_LIST);
		for (int i = 0; i < imgTypeMsgList.size(); i++) {
			Msg msg = imgTypeMsgList.get(i);
			LogUtils.jasonDebug("msg.getImgTypeMsgImg()="+msg.getImgTypeMsgImg());
			String url = UriUtils.getPreviewUri(msg.getImgTypeMsgImg());
			urlList.add(url);
			midList.add(msg.getMid());
		}
		pagerPosition = imgTypeMsgList.indexOf(currentMsg);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public List<String> urlList;
		private List<String> midList;

		public ImagePagerAdapter(FragmentManager fm, List<String> urlList,List<String> midList) {
			super(fm);
			this.urlList = urlList;
			this.midList = midList;
		}

		@Override
		public int getCount() {
			return urlList == null ? 0 : urlList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = urlList.get(position);
			String mid = null;
			if (midList != null){
				mid = midList.get(position);
			}
			boolean isTargetPosition = (position ==pagerPosition);
			currentFragment = ImageDetailFragment.newInstance(url,mid,cid, locationW, locationH, locationX, locationY,isTargetPosition);
			return currentFragment;
		}

	}
}
