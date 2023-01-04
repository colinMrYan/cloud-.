package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除成员的Activity
 */
@Route(path = Constant.AROUTER_CLASS_COMMUNICATION_MEMBER_DEL)
public class ChannelMembersDelActivity extends BaseActivity {

    private ListView channelMemberListView;
    private List<ContactUser> memberContactUserList;
    private ChannelMemDelAdapter adapter;
    private ArrayList<String> memberDelUidList = new ArrayList<>();
    private boolean isRemoveMyself = true;
    private String membersDetail = "";
    private JSONArray membersDetailArray;

    @Override
    public void onCreate() {
        final List<String> memberUidList = (List<String>) getIntent().getSerializableExtra("memberUidList");
        memberContactUserList = ContactUserCacheUtils.getContactUserListById(memberUidList);
        if (getIntent().hasExtra("title")) {
            ((TextView) findViewById(R.id.header_text)).setText(getIntent().getStringExtra("title"));
        }
        isRemoveMyself = getIntent().getExtras().getBoolean("isRemoveMyself", true);
        // 获取群成员昵称列表，有昵称则显示昵称，否则显示通讯录
        membersDetail = getIntent().getExtras().getString("membersDetail", "");
        if (!TextUtils.isEmpty(membersDetail)) {
            membersDetailArray = JSONUtils.getJSONArray(membersDetail, new JSONArray());
        }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ContactUser contactUser = memberContactUserList.get(position);
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.channel_member_list_item, null);
            ImageViewRound circleImageView = (ImageViewRound) convertView.findViewById(R.id.head);
            ImageDisplayUtils.getInstance().displayImage(circleImageView, APIUri.getChannelImgUrl(MyApplication.getInstance(),
                    contactUser.getId()), ResourceUtils.getResValueOfAttr(ChannelMembersDelActivity.this, R.attr.design3_icon_person_default));
            // 有群昵称时显示昵称，否则显示通讯录名称
            if (!TextUtils.isEmpty(membersDetail)) {
                for (int j = 0; j < membersDetailArray.length(); j++) {
                    JSONObject obj = JSONUtils.getJSONObject(membersDetailArray, j, new JSONObject());
                    if (contactUser.getId().equals(JSONUtils.getString(obj, "user", ""))) {
                        String nickname = JSONUtils.getString(obj, "nickname", "");
                        if (TextUtils.isEmpty(nickname)) {
                            ((TextView) convertView.findViewById(R.id.title)).setText(contactUser.getName());
                        } else {
                            ((TextView) convertView.findViewById(R.id.title)).setText(nickname);
                        }
                        break;
                    }
                }
            } else {
                ((TextView) convertView.findViewById(R.id.title)).setText(contactUser.getName());
            }
            ((ImageView) convertView.findViewById(R.id.select_img)).setImageResource(memberDelUidList.contains(contactUser.getId()) ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
            return convertView;
        }
    }
}
