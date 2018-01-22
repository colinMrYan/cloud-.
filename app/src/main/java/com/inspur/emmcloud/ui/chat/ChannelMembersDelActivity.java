package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.widget.CircleImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 删除成员的Activity
 *
 */
public class ChannelMembersDelActivity extends BaseActivity {

	private ListView channelMemberListView;
	private List<Contact> memberContactList;
	private ChannelMemDelAdapter adapter;
	private ArrayList<String> memberDelUidList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channelmem_del);
		final List<String> memberUidList = (List<String>) getIntent().getSerializableExtra("memberUidList");
		memberContactList = ContactCacheUtils.getContactListById(getApplicationContext(), memberUidList);
		if (getIntent().hasExtra("title")){
			((TextView)findViewById(R.id.header_text)).setText(getIntent().getStringExtra("title"));
		}
		delMyself();
        channelMemberListView = (ListView) findViewById(R.id.member_list);
        adapter = new ChannelMemDelAdapter();
        channelMemberListView.setAdapter(adapter);
        channelMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uid = memberContactList.get(position).getInspurID();
                boolean isSelct = memberDelUidList.contains(uid);
                if (isSelct){
                    memberDelUidList.remove(uid);
                }else {
                    memberDelUidList.add(uid);
                }
                adapter.notifyDataSetChanged();
            }
        });

	}

	/**
	 * 从成员列表里去掉自己
	 */
	private void delMyself() {
		Iterator<Contact> sListIterator = memberContactList.iterator();
		while (sListIterator.hasNext()) {
			Contact contact = sListIterator.next();
			if (contact.getInspurID().equals(MyApplication.getInstance().getUid())) {
				sListIterator.remove();
			}
		}
	}

	class ChannelMemDelAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return memberContactList.size();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
		    Contact contact =  memberContactList.get(position);
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.channel_member_list_item, null);
			CircleImageView circleImageView = (CircleImageView) convertView
					.findViewById(R.id.head);
			ImageDisplayUtils.getInstance().displayImage(circleImageView, APIUri
					.getChannelImgUrl(ChannelMembersDelActivity.this, contact.getInspurID()),R.drawable.icon_person_default);
			((TextView) convertView.findViewById(R.id.title))
					.setText(contact.getName());
            ((ImageView)convertView.findViewById(R.id.select_img)).setImageResource(memberDelUidList.contains(contact.getInspurID())?R.drawable.checkbox_pressed:R.drawable.checkbox_normal);
			return convertView;
		}
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		intent.putExtra("selectMemList", memberDelUidList);
		switch (v.getId()) {
		case R.id.back_layout:
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.header_del_text:
			setResult(RESULT_OK, intent);
			finish();
			break;
		default:
			break;
		}
	}
}
