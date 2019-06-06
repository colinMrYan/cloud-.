package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除成员的Activity
 */
public class ChannelMembersDelActivity extends BaseActivity {

    private ListView channelMemberListView;
    private List<ContactUser> memberContactUserList;
    private ChannelMemDelAdapter adapter;
    private ArrayList<String> memberDelUidList = new ArrayList<>();
    private boolean isRemoveMyself = true;

    @Override
    public void onCreate() {
        final List<String> memberUidList = (List<String>) getIntent().getSerializableExtra("memberUidList");
        memberContactUserList = ContactUserCacheUtils.getContactUserListById(memberUidList);
        if (getIntent().hasExtra("title")) {
            ((TextView) findViewById(R.id.header_text)).setText(getIntent().getStringExtra("title"));
        }
        isRemoveMyself = getIntent().getExtras().getBoolean("isRemoveMyself", true);
        if (isRemoveMyself) {
            memberContactUserList.remove(new ContactUser(MyApplication.getInstance().getUid()));
        }
        channelMemberListView = (ListView) findViewById(R.id.member_list);
        adapter = new ChannelMemDelAdapter();
        channelMemberListView.setAdapter(adapter);
        channelMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uid = memberContactUserList.get(position).getId();
                boolean isSelct = memberDelUidList.contains(uid);
                if (isSelct) {
                    memberDelUidList.remove(uid);
                } else {
                    memberDelUidList.add(uid);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channelmem_del;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.header_del_text:
                if (memberDelUidList.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("selectMemList", memberDelUidList);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            default:
                break;
        }
    }

    class ChannelMemDelAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return memberContactUserList.size();
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
            ContactUser contactUser = memberContactUserList.get(position);
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.channel_member_list_item, null);
            CircleTextImageView circleImageView = (CircleTextImageView) convertView
                    .findViewById(R.id.head);
            ImageDisplayUtils.getInstance().displayImage(circleImageView, APIUri
                    .getChannelImgUrl(MyApplication.getInstance(), contactUser.getId()), R.drawable.icon_person_default);
            ((TextView) convertView.findViewById(R.id.title))
                    .setText(contactUser.getName());
            ((ImageView) convertView.findViewById(R.id.select_img)).setImageResource(memberDelUidList.contains(contactUser.getId()) ? R.drawable.checkbox_pressed : R.drawable.checkbox_normal);
            return convertView;
        }
    }
}
