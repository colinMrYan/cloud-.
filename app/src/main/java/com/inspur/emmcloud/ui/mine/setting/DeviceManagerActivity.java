package com.inspur.emmcloud.ui.mine.setting;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.BindingDevice;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceManagerActivity extends BaseActivity {

	private ListView deviceListView;
	private LoadingDialog loadingDlg;
	private List<BindingDevice> bindingDeviceList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_device_manager);
		initView();
		getBindDeviceList();
	}

	private void initView(){
		((TextView)findViewById(R.id.current_device_text)).setText(Build.MODEL);
		loadingDlg = new LoadingDialog(this);
		deviceListView = (ListView)findViewById(R.id.device_list);
		deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		});
	}

	/**
	 * 获取设备绑定列表
	 */
	private void getBindDeviceList(){
		if (NetUtils.isNetworkConnected(getApplicationContext())){
		//	loadingDlg.show();
		}
	}

	public void onClick(View v){
		switch (v.getId()){
			case R.id.back_layout:
				finish();
				break;
			case R.id.current_device_layout:
				IntentUtils.startActivity(DeviceManagerActivity.this,DeviceInfoActivity.class);
				break;
		}
	}
}
