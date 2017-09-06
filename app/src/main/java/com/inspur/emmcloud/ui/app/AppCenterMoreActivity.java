package com.inspur.emmcloud.ui.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;

import java.util.List;

public class AppCenterMoreActivity extends BaseActivity{

	private ListView appCenterMoreListView;
	private List<App> appList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_center_more);
		initView();
		((MyApplication) getApplicationContext())
				.addActivity(AppCenterMoreActivity.this);
	}

	/**
	 * 初始化views
	 */
	private void initView() {
		appCenterMoreListView = (ListView) findViewById(R.id.app_center_more_apps);
		if(getIntent().hasExtra("appList")){
			appList = (List<App>) getIntent().getSerializableExtra("appList");
			if(appList != null){
				AppMoreAdapter adapter = new AppMoreAdapter();
				appCenterMoreListView.setAdapter(adapter);
				appCenterMoreListView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						Bundle bundle = new Bundle();
						bundle.putSerializable("app", appList.get(position));
						IntentUtils.startActivity(AppCenterMoreActivity.this, AppDetailActivity.class, bundle);
					}
				});
			}
		}
		if(getIntent().hasExtra("category_name")){
			((TextView)findViewById(R.id.header_text)).setText(getIntent().getStringExtra("category_name"));
		}
	}
	
	/**
	 * 关闭
	 * @param v
	 */
	public void onClick(View v){
		finish();
	}
	
	class AppMoreAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			App app = appList.get(position);
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_center_more_app_item_view, null);
			ImageView appIconImg = (ImageView)convertView.findViewById(R.id.app_icon_img);
			TextView appNameText = (TextView) convertView.findViewById(R.id.app_name_text);
			new ImageDisplayUtils(R.drawable.icon_empty_icon).displayImage(appIconImg, app.getAppIcon());
			appNameText.setText(app.getAppName());
            ((TextView)convertView.findViewById(R.id.app_group_name_text)).setText(app.getAppName());
			return convertView;
		}
		
	}
}
