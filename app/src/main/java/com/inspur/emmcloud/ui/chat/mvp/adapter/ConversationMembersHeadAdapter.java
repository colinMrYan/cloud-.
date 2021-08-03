package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ConversationMembersHeadAdapter extends BaseAdapter {

    Context context;
    List<String> uidList;
    boolean isOwner = false;
    String mOwnerUid;

    public ConversationMembersHeadAdapter(Context context, boolean isOwner, List<String> uidList, String ownerUid) {
        this.isOwner = isOwner;
        this.context = context;
        this.uidList = uidList;
        mOwnerUid = ownerUid;
    }

    public void setUIUidList(List<String> uiIidList) {
        uidList = uiIidList;
    }

    @Override
    public int getCount() {
        return uidList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.chat_group_info_member_head_item, null);
            holder.headImage = view.findViewById(R.id.iv_chat_members_head);
            holder.nameTv = view.findViewById(R.id.tv_chat_members_name);
            holder.ownerTagImage = view.findViewById(R.id.iv_chat_members_owner);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        String uid;
        String userName;
        String userPhotoUrl;
        uid = uidList.get(i);
        if (uid.equals("deleteUser")) {
            userPhotoUrl = "drawable://" + R.drawable.ic_delete_channel_member;
            userName = "";
        } else if (uid.equals("addUser")) {
            userPhotoUrl = "drawable://" + R.drawable.ic_add_channel_member;
            userName = "";
        } else if (uid.equals("fileTransfer")) {
            userPhotoUrl = "drawable://" + R.drawable.ic_file_transfer;
            userName = context.getString(R.string.chat_file_transfer);
        } else {
            userName = ContactUserCacheUtils.getUserName(uid);
            userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
        }
        holder.ownerTagImage.setVisibility(TextUtils.equals(uid, mOwnerUid) ? View.VISIBLE : View.GONE);
        holder.nameTv.setText(userName);
        ImageDisplayUtils.getInstance().displayImageByTag(holder.headImage, userPhotoUrl, R.drawable.icon_person_default);
        return view;
    }


    /***/
    class ViewHolder {
        private ImageView headImage;
        private TextView nameTv;
        private View itemView;
        private View ownerTagImage;
    }

}
