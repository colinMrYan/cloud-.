package com.inspur.emmcloud.ui.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.CircleImageView;

public class MentionActivty extends BaseActivity {

	private ListView mentionListView;
	private ImageDisplayUtils imageDisplayUtils;
	private ArrayList<HashMap<String, String>> mentionsList = new ArrayList<HashMap<String, String>>();
	private MentionsAdapter adapter;
	private JSONObject jsonResult;
	private String channelID = "";
	private List<Map<String, String>> memList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mentions_mem);
		mentionListView = (ListView) findViewById(R.id.mentions_mem_list);
		imageDisplayUtils = new ImageDisplayUtils(MentionActivty.this,
				R.drawable.ic_launcher);

		channelID = getIntent().getStringExtra("cid");

		String userid = PreferencesUtils.getString(MentionActivty.this,
				"userID");
		if (!TextUtils.isEmpty(channelID)) {
			memList = ChannelGroupCacheUtils.getMembersMapList(
					MentionActivty.this, channelID);
		}

//		for (int i = 0; i < memList.size(); i++) {
//			if (memList.get(i).containsKey(userid)) {
//				memList.remove(i);
//			}
//		}
		
		//迭代问题修复
		 Iterator<Map<String, String>> sListIterator = memList.iterator();
		    while(sListIterator.hasNext()){
		        Map<String, String> contact = sListIterator.next();
		        if(contact.containsKey(userid)){
		        sListIterator.remove();
		        }
		    }

		adapter = new MentionsAdapter();
		mentionListView.setAdapter(adapter);

		mentionListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				JSONObject peopleObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				jsonResult = new JSONObject();
				try {

					Iterator it = memList.get(position).entrySet().iterator();
					Entry entry = (Entry) it.next();
					String uid = (String) entry.getKey();
					String name = (String) entry.getValue();

					peopleObject.put("cid", uid);
					peopleObject.put("name", name);
					jsonArray.put(peopleObject);
					jsonResult.put("people", jsonArray);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent intent = new Intent();
				intent.putExtra("searchResult", jsonResult.toString());

				setResult(RESULT_OK, intent);
				finish();
			}
		});


	}

	class MentionsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return memList.size();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.mentions_mem_item, null);
			CircleImageView headImage = (CircleImageView) convertView
					.findViewById(R.id.photo_img);
			TextView nameTextView = (TextView) convertView
					.findViewById(R.id.name_text);


//			String userid = PreferencesUtils.getString(MentionActivty.this,
//					"userID");
			String uid = "", name = "";
			Iterator it = memList.get(position).entrySet().iterator();
			Entry entry = (Entry) it.next();
			uid = (String) entry.getKey();
			name = (String) entry.getValue();

			imageDisplayUtils
					.display(headImage, UriUtils.getChannelImgUri(uid));

			nameTextView.setText(name);
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
