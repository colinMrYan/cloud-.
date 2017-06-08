package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
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
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Enterprise;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import java.util.List;

/**
 * Created by Administrator on 2017/5/25.
 */

public class SwitchEnterpriseActivity extends BaseActivity {
	private List<Enterprise> enterpriseList;
	private ListView enterpriseListView;
	private GetMyInfoResult getMyInfoResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_device_manager);
		getEnterpriseList();
		initView();
	}

	private void getEnterpriseList(){
		String myInfo = PreferencesUtils.getString(this, "myInfo", "");
		getMyInfoResult = new GetMyInfoResult(myInfo);
		enterpriseList = getMyInfoResult.getEnterpriseList();
	}

	private void initView(){
		((TextView)findViewById(R.id.header_text)).setText(R.string.select_enterprise);
		enterpriseListView = (ListView)findViewById(R.id.device_list);
		enterpriseListView.setAdapter(adapter);
		enterpriseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Enterprise enterprise = enterpriseList.get(position);
				if (!enterprise.getId().equals(((MyApplication)getApplicationContext()).getCurrentEnterprise().getId())){
					showSwitchWarningDlg(enterprise);
				}
			}
		});

	}

	private void showSwitchWarningDlg(final Enterprise enterprise) {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == -1) {
					switchToEnterprise(enterprise);
				}
				dialog.dismiss();
			}
		};
		EasyDialog.showDialog(SwitchEnterpriseActivity.this,
				getString(R.string.prompt),
				getString(R.string.sure_switch_to,enterprise.getName()),
				getString(R.string.ok), getString(R.string.cancel),
				listener, true);
	}

	/**
	 * 切换企业信息
	 * @param enterprise
	 */
	private void switchToEnterprise(Enterprise enterprise){
		try {
			PreferencesByUsersUtils.putString(getApplicationContext(),"current_enterprise_id",enterprise.getId());
			((MyApplication)getApplicationContext()).initTanent();
			((MyApplication)getApplicationContext()).stopWebSocket();
			((MyApplication)getApplicationContext()).clearNotification();
			PreferencesUtils.putString(SwitchEnterpriseActivity.this, "myInfo", "");
			Intent intent = new Intent(SwitchEnterpriseActivity.this,
					MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}catch (Exception e){
			e.printStackTrace();
			ToastUtils.show(getApplicationContext(),R.string.fail_to_switch_enterprise);
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

	private BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return enterpriseList.size();
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
			Enterprise enterprise = enterpriseList.get(position);
			convertView = LayoutInflater.from(SwitchEnterpriseActivity.this).inflate(R.layout.mine_setting_enterprise_item_view,null);
			((TextView)convertView.findViewById(R.id.enterprise_text)).setText(enterprise.getName());
			if (enterprise.getId().equals(((MyApplication)getApplicationContext()).getCurrentEnterprise().getId())){
				(convertView.findViewById(R.id.current_enterprise_text)).setVisibility(View.VISIBLE);
			}
			return convertView;
		}
	};
}
