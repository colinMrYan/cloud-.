package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.BindingDevice;
import com.inspur.emmcloud.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceManagerActivity extends BaseActivity {

	private final static int UNBIND_DEVICE = 1;
	private ListView deviceListView;
	private LoadingDialog loadingDlg;
	private List<BindingDevice> bindingDeviceList = new ArrayList<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_device_manager);
		initView();
		getBindDeviceList();
	}

	private void initView(){
		loadingDlg = new LoadingDialog(this);
		deviceListView = (ListView)findViewById(R.id.device_list);
		deviceListView.setAdapter(adapter);
		deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(),DeviceInfoActivity.class);
				intent.putExtra("binding_device",bindingDeviceList.get(position));
				startActivityForResult(intent,UNBIND_DEVICE);
			}
		});
	}

	/**
	 * 获取设备绑定列表
	 */
	private void getBindDeviceList(){
		if (NetUtils.isNetworkConnected(getApplicationContext())){
			loadingDlg.show();
			MineAPIService apiService = new MineAPIService(DeviceManagerActivity.this);
			apiService.setAPIInterface(new WebService());
			apiService.getBindingDeviceList();
		}
	}


	public void onClick(View v){
		switch (v.getId()){
			case R.id.back_layout:
				finish();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UNBIND_DEVICE && resultCode == RESULT_OK){
			BindingDevice bindingDevice = (BindingDevice)data.getSerializableExtra("binding_device");
			bindingDeviceList.remove(bindingDevice);
			adapter.notifyDataSetChanged();
		}
	}

	private BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return bindingDeviceList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BindingDevice bindingDevice = bindingDeviceList.get(position);
			convertView = LayoutInflater.from(DeviceManagerActivity.this).inflate(R.layout.mine_setting_binding_devcie_item_view,null);
			((TextView)convertView.findViewById(R.id.device_text)).setText(bindingDevice.getDeviceModel());
			if (bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(DeviceManagerActivity.this))){
				(convertView.findViewById(R.id.current_device_text)).setVisibility(View.VISIBLE);
			}
			String bindingTime = TimeUtils.getTime(bindingDevice.getDeviceBindTime(),TimeUtils.getFormat(DeviceManagerActivity.this,TimeUtils.FORMAT_DEFAULT_DATE));
			((TextView)convertView.findViewById(R.id.device_bind_time_text)).setText(bindingTime);
			return convertView;
		}
	};

	private class WebService extends APIInterfaceInstance{
		@Override
		public void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult) {
			if (loadingDlg != null && loadingDlg.isShowing()){
				loadingDlg.dismiss();
			}
			bindingDeviceList = getBindingDeviceResult.getBindingDeviceList();
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnBindingDeviceListFail(String error, int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()){
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(DeviceManagerActivity.this,error,errorCode);
		}
	}
}
