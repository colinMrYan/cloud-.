package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LoginUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能介绍页面 com.inspur.emmcloud.ui.GuideActivity
 * 
 * @author Jason Chen; create at 2016年8月29日 下午2:36:49
 */
public class GuideActivity extends BaseActivity {

	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;
	private List<View> funcIntroductionViewList;
	private Handler handler;
	private LoadingDialog loadingDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		StateBarColor.changeStateBarColor(this,R.color.white);
		setContentView(R.layout.activity_guide);
		// 存入当前版本号,方便判断新功能介绍显示的时机
		String appVersion = AppUtils.getVersion(GuideActivity.this);
		PreferencesUtils.putString(getApplicationContext(), "previousVersion",
				appVersion);
		initView();

	}

	private void initView() {
		// TODO Auto-generated method stub
		loadingDlg = new LoadingDialog(this);
		funcIntroductionViewList = new ArrayList<View>();
		addFuncIntroductionView();
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(new MyViewPagerAdapter(getApplicationContext(), funcIntroductionViewList));
		handMessage();
	}

	/**
	 * 将各个功能介绍页面添加到ViewList中
	 */
	private void addFuncIntroductionView() {
		// TODO Auto-generated method stub
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View intrFirst = layoutInflater.inflate(R.layout.guide_page1, null);
		View intrSecond = layoutInflater.inflate(R.layout.guide_page2, null);
		View intrThird = layoutInflater.inflate(R.layout.guide_page3, null);
		View intrForth = layoutInflater.inflate(R.layout.guide_page4, null);
		View intrFifth = layoutInflater.inflate(R.layout.guide_page5, null);
		((ImageView) intrFirst.findViewById(R.id.skip_btn))
				.setOnClickListener(clickListener);
		((ImageView) intrSecond.findViewById(R.id.skip_btn))
				.setOnClickListener(clickListener);
		((ImageView) intrThird.findViewById(R.id.skip_btn))
				.setOnClickListener(clickListener);
		((ImageView) intrForth.findViewById(R.id.skip_btn))
				.setOnClickListener(clickListener);

		Button enterButton = ((Button) intrFifth
				.findViewById(R.id.enter_app_btn));
		enterButton.setOnClickListener(clickListener);

		// 当从关于页面跳转到此页面时，按钮显示不同的内容
		if ((getIntent().hasExtra("from"))
				&& (getIntent().getStringExtra("from").equals("about"))) {
			enterButton.setText(getString(R.string.return_app));
		}

		funcIntroductionViewList.add(intrFirst);
		funcIntroductionViewList.add(intrSecond);
		funcIntroductionViewList.add(intrThird);
		funcIntroductionViewList.add(intrForth);
		funcIntroductionViewList.add(intrFifth);
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (loadingDlg != null && loadingDlg.isShowing()) {
					loadingDlg.dismiss();
				}
				switch (msg.what) {
				case LOGIN_SUCCESS:
					IntentUtils.startActivity(GuideActivity.this,
							IndexActivity.class, true);
					break;
				case LOGIN_FAIL:
					IntentUtils.startActivity(GuideActivity.this,
							LoginActivity.class, true);
					break;

				default:
					break;
				}
			}

		};
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.skip_btn:
			case R.id.enter_app_btn:
				if (!getIntent().hasExtra("from")) {
					// 将首次进入应用的标志位置为false
					PreferencesUtils.putBoolean(getApplicationContext(),
							"isFirst", false);
					String accessToken = PreferencesUtils.getString(
							GuideActivity.this, "accessToken", "");
					if (!StringUtils.isBlank(accessToken) && NetUtils.isNetworkConnected(getApplicationContext())) {
						getMyInfo();
					} else {
						IntentUtils.startActivity(GuideActivity.this,
								LoginActivity.class, true);
					}

				} else if (getIntent().getStringExtra("from").equals("about")) {
					finish();
				}
				break;

			default:
				break;
			}
		}

	};
	
	/**
	 * 获取个人基本信息
	 */
	private void getMyInfo() {
		// TODO Auto-generated method stub
		loadingDlg.show();
		LoginUtils loginUtils = new LoginUtils(
				GuideActivity.this, handler);
		loginUtils.getMyInfo();
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (loadingDlg != null && loadingDlg.isShowing()) {
			loadingDlg.dismiss();
		}
		if (handler != null) {
			handler = null;
		}
	}

}
