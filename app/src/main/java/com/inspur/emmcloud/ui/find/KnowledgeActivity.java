package com.inspur.emmcloud.ui.find;


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
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.bean.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.KnowledgeInfo;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;

import java.util.ArrayList;
import java.util.List;


public class KnowledgeActivity extends BaseActivity {
	
	private ListView knowledgeListView;
	private List<KnowledgeInfo> knowledgeList = new ArrayList<KnowledgeInfo>();
	
	private FindAPIService apiService;
	private Adapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_group_file);
		((TextView)findViewById(R.id.header_text)).setText(getString(R.string.knowledge));
		apiService = new FindAPIService(KnowledgeActivity.this);
		apiService.setAPIInterface(new WebService());
		setKnowlegeList();
		adapter = new Adapter();
		knowledgeListView = (ListView) findViewById(R.id.file_list);
		knowledgeListView.setAdapter(adapter);
		knowledgeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setClass(getApplicationContext(), ImpActivity.class);
////				intent.putExtra("appName", knowledgeList.get(position).getName());
//				if(StringUtils.isEmpty(knowledgeList.get(position).getUri())){
////					intent.putExtra("uri", "http://www.baidu.com");
//					ToastUtils.show(KnowledgeActivity.this, getString(R.string.no_this_knowledge));
//				}else {
//					intent.putExtra("uri", knowledgeList.get(position).getUri());
//					startActivity(intent);
//				}
			}
		});

	}

	private void setKnowlegeList() {
		apiService.getKnowledgeList();
	}
	
	
	
	private class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return knowledgeList.size();
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
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.knowledge_item_view, null);
			TextView nameText = (TextView) convertView
					.findViewById(R.id.name_text);
			ImageView knowledgeImageView = (ImageView) convertView.findViewById(R.id.knowledge_img);
			nameText.setText(knowledgeList.get(position).getName());

			new ImageDisplayUtils(R.drawable.icon_photo_default).displayImage(knowledgeImageView, UriUtils.getPreviewUri(knowledgeList.get(position).getIcon()));
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



	class WebService extends APIInterfaceInstance{

		@Override
		public void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo) {
			// TODO Auto-generated method stub
			super.returnKnowledgeListSuccess(getKnowledgeInfo);
			knowledgeList.addAll(getKnowledgeInfo.getKnowLedgeLists());
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnKnowledgeListFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			WebServiceMiddleUtils.hand(KnowledgeActivity.this, error,errorCode);
		}
		
	}

}
