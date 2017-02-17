package com.inspur.emmcloud.ui.mine.cardpackage;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.CardPackage;
import com.inspur.emmcloud.util.LogUtils;

/**
 * 卡包详情
 * @author sunqx
 *
 */
public class CardPackageDetailActivity extends BaseActivity {

	private ListView cardPackageList;
	private ArrayList<HashMap<String, String>> cardList = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cardpackage_detail);
		((MyApplication) getApplicationContext())
				.addActivity(CardPackageDetailActivity.this);
		cardPackageList = (ListView) findViewById(R.id.card_packagelist_detail);
		CardPackage cardPackage = (CardPackage) getIntent()
				.getSerializableExtra("CardPackage");
		LogUtils.debug("jason", "cardPackage.getName()="+cardPackage.getName());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("title", getString(R.string.heading));
		map.put("content", cardPackage.getName());
		cardList.add(map);
		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put("title", getString(R.string.taxpayer_identification_number));
		map1.put("content", cardPackage.getNumber());
		cardList.add(map1);

		HashMap<String, String> map2 = new HashMap<String, String>();
		map2.put("title", getString(R.string.address));
		map2.put("content", cardPackage.getAddress());
		cardList.add(map2);

		HashMap<String, String> map3 = new HashMap<String, String>();
		map3.put("title", getString(R.string.phone_number));
		map3.put("content",cardPackage.getTel());
		cardList.add(map3);

		HashMap<String, String> map4 = new HashMap<String, String>();
		map4.put("title", getString(R.string.bank));
		map4.put("content", cardPackage.getBank());
		cardList.add(map4);

		HashMap<String, String> map5 = new HashMap<String, String>();
		map5.put("title", getString(R.string.account_number));
		map5.put("content", cardPackage.getAccount());
		cardList.add(map5);

		cardPackageList.setAdapter(new CardListAdapter());
	}

	class CardListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cardList.size();
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
			// TODO Auto-generated method stub
			convertView = LayoutInflater.from(CardPackageDetailActivity.this)
					.inflate(R.layout.card_packagelist_detail, null);
			((TextView) convertView.findViewById(R.id.cardpackage_title_text))
					.setText(cardList.get(position).get("title"));
			;
			((TextView) convertView.findViewById(R.id.cardpackage_content_text))
					.setText(cardList.get(position).get("content"));
			;
			return convertView;
		}

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;

		default:
			break;
		}
	}
}
