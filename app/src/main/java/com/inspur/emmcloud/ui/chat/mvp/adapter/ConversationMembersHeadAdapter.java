package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
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

    public static final String TYPE_DELETE_USER = "deleteUser";
    public static final String TYPE_ADD_USER = "addUser";
    public static final String TYPE_FILE_TRANSFER = "fileTransfer";

    Context mContext;
    List<String> mUidList;
    List<String> mAdministratorList;
    String mOwnerUid;

    public ConversationMembersHeadAdapter(Context context, List<String> uidList, String ownerUid, List<String> administratorList) {
        mContext = context;
        mUidList = uidList;
        mOwnerUid = ownerUid;
        mAdministratorList = administratorList;
    }

    public void setUIUidList(List<String> uiUidList) {
        mUidList = uiUidList;
    }

    @Nullable
    public String getUIUidFromIndex(int index) {
        return mUidList.size() > index ? mUidList.get(index) : null;
    }

    public void setAdminList(List<String> adminList) {
        mAdministratorList = adminList;
    }

    public void setOwner(String owner) {
        this.mOwnerUid = owner;
    }

    @Override
    public int getCount() {
        return mUidList.size();
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
            view = View.inflate(mContext, R.layout.chat_group_info_member_head_item, null);
            holder.headImage = view.findViewById(R.id.iv_chat_members_head);
            holder.nameTv = view.findViewById(R.id.tv_chat_members_name);
            holder.ownerTagImage = view.findViewById(R.id.iv_chat_members_owner);
            holder.administratorTagImage = view.findViewById(R.id.iv_chat_members_administrator);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        String uid;
        String userName;
        String userPhotoUrl;
        uid = mUidList.get(i);
        if (TYPE_DELETE_USER.equals(uid)) {
            userName = "";
            holder.headImage.setBackgroundResource(R.drawable.ic_delete_channel_member);
        } else if (TYPE_ADD_USER.equals(uid)) {
            userName = "";
            holder.headImage.setBackgroundResource(R.drawable.ic_add_channel_member);
        } else if (TYPE_FILE_TRANSFER.equals(uid)) {
            userName = mContext.getString(R.string.chat_file_transfer);
            holder.headImage.setBackgroundResource(R.drawable.ic_file_transfer);
        } else {
            userName = ContactUserCacheUtils.getUserName(uid);
            userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
            ImageDisplayUtils.getInstance().displayImageByTag(holder.headImage, userPhotoUrl, R.drawable.icon_person_default);
        }
        holder.ownerTagImage.setVisibility(TextUtils.equals(uid, mOwnerUid) && getCount() > 2 ? View.VISIBLE : View.GONE);
        holder.administratorTagImage.setVisibility((mAdministratorList != null && mAdministratorList.contains(uid) && getCount() > 2 && !TextUtils.equals(uid, mOwnerUid)) ?
                View.VISIBLE : View.GONE);
        holder.nameTv.setText(userName);
        return view;
    }


    /***/
    class ViewHolder {
        private ImageView headImage;
        private TextView nameTv;
        private View itemView;
        private View ownerTagImage;
        private View administratorTagImage;
    }

}
