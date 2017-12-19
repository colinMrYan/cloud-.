package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
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
	private List<Contact> memberList;
	private ChannelMemDelAdapter adapter;
	private ArrayList<String> delMemberList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channelmem_del);
		channelMemberListView = (ListView) findViewById(R.id.channel_mem_del);
		memberList = ChannelGroupCacheUtils.getMembersList(
				ChannelMembersDelActivity.this,
				getIntent().getStringExtra("cid"));
		delMyself();
		adapter = new ChannelMemDelAdapter();
		channelMemberListView.setAdapter(adapter);
	}

	/**
	 * 从成员列表里去掉自己
	 */
	private void delMyself() {
		String myUid = PreferencesUtils.getString(
				ChannelMembersDelActivity.this, "userID", "");
		Iterator<Contact> sListIterator = memberList.iterator();
		while (sListIterator.hasNext()) {
			Contact contact = sListIterator.next();
			if (contact.getInspurID().equals(myUid)) {
				sListIterator.remove();
			}
		}
	}

	class ChannelMemDelAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return memberList.size();
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
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.channel_member_list_item, null);
			CircleImageView circleImageView = (CircleImageView) convertView
					.findViewById(R.id.head);
			ImageDisplayUtils.getInstance().displayImage(circleImageView, APIUri
					.getChannelImgUrl(ChannelMembersDelActivity.this, memberList.get(position).getInspurID()),R.drawable.icon_person_default);
			((TextView) convertView.findViewById(R.id.title))
					.setText(memberList.get(position).getName());
			((CheckBox) convertView.findViewById(R.id.choose_check))
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								delMemberList.add(memberList.get(position)
										.getInspurID());
							} else {
								delMemberList.remove(memberList.get(position)
										.getInspurID());
							}
						}
					});
			return convertView;
		}
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		intent.putExtra("selectMemList", delMemberList);
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
