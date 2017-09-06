package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.List;

public class AppCenterAdapter extends BaseAdapter {

	private static final int ADD_APP_FAIL = 3;
	private static final String ACTION_NAME = "add_app";
	private ImageDisplayUtils imageDisplayUtils;
	private List<App> appList;
	private Activity activity;
	private LoadingDialog loadingDialog;
	private MyAppAPIService apiService;
	private GetAddAppResult getAddAppResult;

	public AppCenterAdapter(Activity activity, List<App> appList) {
		// TODO Auto-generated constructor stub
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_empty_icon);
		this.appList = appList;
		this.activity = activity;
		loadingDialog = new LoadingDialog(activity);
		apiService = new MyAppAPIService(activity);
		apiService.setAPIInterface(new WebService());
	}


	public void addApp(App app){
		int addPosition = -1;
		for (int i = 0; i < appList.size(); i++) {
			if (appList.get(i).getAppID()
					.equals(app.getAppID())) {
				addPosition = i;
				break;
			}
		}
		
		if (addPosition == -1) {
			return;
		}
		appList.get(addPosition).setUseStatus(1);
		AppCenterAdapter.this.notifyDataSetChanged();
	}

	public void setListData(List<App> AppList) {
		this.appList = AppList;
	}

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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		 Holder holder = null;
		if(convertView == null){
			holder = new Holder();
			LayoutInflater vi = (LayoutInflater) activity
					.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.all_app_item_view, null);
			holder.iconImg = (ImageView) convertView
					.findViewById(R.id.app_icon_img);
			holder.nameText = (TextView) convertView
					.findViewById(R.id.title_text);
			holder.statusBtn = (Button) convertView
					.findViewById(R.id.app_status_btn);
			convertView.setTag(holder); 
		}else {
			holder = (Holder) convertView.getTag();
		}
		final App app = appList.get(position);
		
		final String appID = app.getAppID();
		imageDisplayUtils.displayImage(holder.iconImg, app.getAppIcon());
		holder.nameText.setText(app.getAppName());
		if (app.getUseStatus() == 1) {
			holder.statusBtn.setText(activity.getString(R.string.open));
		} else if (app.getUseStatus() == 0) {
			holder.statusBtn.setText(activity.getString(R.string.add));
		} else {
			holder.statusBtn.setText(activity.getString(R.string.update));
		}
		holder.statusBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int type = app.getAppType();
				if (app.getUseStatus() == 0) {
					installApp(type, appID, (Button)v);
				} else if (app.getUseStatus() == 1) {
					UriUtils.openApp(activity, app);
				} else {
					// 更新
				}

			}

		});
		return convertView;
	}

	private void installApp(int type, String appID, Button statusBtn) {
		// TODO Auto-generated method stub
		switch (type) {
		case 2:
		case 3:
		case 4:
			if (NetUtils.isNetworkConnected(activity)) {
				statusBtn.setText(activity.getString(R.string.adding));
				loadingDialog.show();
				apiService.addApp(appID);
			}
			break;

		default:
			break;
		}
	}

	public class WebService extends APIInterfaceInstance {

		@Override
		public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {
			// TODO Auto-generated method stub
			AppCenterAdapter.this.getAddAppResult = getAddAppResult;
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			int addPosition = -1;
			for (int i = 0; i < appList.size(); i++) {
				if (appList.get(i).getAppID()
						.equals(getAddAppResult.getAppID())) {
					addPosition = i;
					break;
				}
			}
			
			if (addPosition == -1) {
				return;
			}

			Intent mIntent = new Intent(ACTION_NAME);
			mIntent.putExtra("app", appList.get(addPosition));
			// 发送广播
			activity.sendBroadcast(mIntent);
		}

		@Override
		public void returnAddAppFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(activity,error,errorCode);
			AppCenterAdapter.this.notifyDataSetChanged();
		}


	}
	
	public static class Holder {
		ImageView iconImg ;
		TextView nameText;
		Button statusBtn;
	}

}
