package com.inspur.emmcloud.ui.mine.cardpackage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.CardPackage;
import com.inspur.emmcloud.bean.GetCardPackageListResult;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡包列表
 * @author sunqx
 *
 */
public class CardPackageListActivity extends BaseActivity{

	private ListView cardPackageListView;
	private List<CardPackage> cardPackageList = new ArrayList<CardPackage>();
	private LoadingDialog loadingDlg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cardpackage_list);
		((MyApplication) getApplicationContext())
		.addActivity(CardPackageListActivity.this);
		loadingDlg= new LoadingDialog(this);
		cardPackageListView = (ListView) findViewById(R.id.card_packagelist_list);
		getCardPackageList();
		cardPackageListView.setAdapter(new CardListAdapter());
		
	}
	
	private void getCardPackageList() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show();
			MineAPIService apiService = new MineAPIService(this);
			apiService.setAPIInterface(new WebService());
			apiService.getCardPackageList();
		}
	}

	class CardListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cardPackageList.size();
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
			convertView = LayoutInflater.from(CardPackageListActivity.this).inflate(
					R.layout.card_packagelist_list, null);
			((RelativeLayout)convertView.findViewById(R.id.card_pacakage_item)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.putExtra("CardPackage", cardPackageList.get(position));
					intent.setClass(CardPackageListActivity.this, CardPackageDetailActivity.class);
					startActivity(intent);
				}
			});;
			((TextView)convertView.findViewById(R.id.cardpackage_item_text)).setText(cardPackageList.get(position).getTitle());;
			return convertView;
		}
		
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;

		default:
			break;
		}
	}
	
	public class WebService extends APIInterfaceInstance{

		@Override
		public void returnCardPackageListSuccess(
				GetCardPackageListResult getCardPackageListResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			cardPackageList = getCardPackageListResult.getCardPackageList();
			CardListAdapter adapter = new CardListAdapter();
			cardPackageListView.setAdapter(adapter);
		}

		@Override
		public void returnCardPackageListFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(CardPackageListActivity.this,error,errorCode );
		}
		
	}
}
