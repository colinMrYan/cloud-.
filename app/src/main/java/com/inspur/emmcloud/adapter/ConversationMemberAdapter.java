package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/9.
 */

public class ConversationMemberAdapter extends BaseAdapter {
    private List<String> uidList = new ArrayList<>();
    private boolean isOwner = false;
    private Context context;

    public ConversationMemberAdapter(Context context, List<String> uidList, boolean isOwner) {
        this.uidList = uidList;
        this.isOwner = isOwner;
        this.context = context;
    }

//    public void setUidList(List<String> uidList) {
//        this.uidList.clear();
//        this.uidList.addAll(uidList);
//    }

    @Override
    public int getCount() {
        int size = uidList.size();
        if (isOwner)
            return size > 5 ? 7 : size + 2;
        return size > 6 ? 7 : size + 1;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.channel_member_item_view, null);
        CircleTextImageView photoImg = (CircleTextImageView) convertView.findViewById(R.id.member_head_img);
        TextView nameText = (TextView) convertView.findViewById(R.id.tv_name);
        String userPhotoUrl;
        String userName;
        if ((position == getCount() - 1) && isOwner) {
            userPhotoUrl = "drawable://" + R.drawable.icon_group_delete;
            userName = context.getString(R.string.delete);

        } else if (((position == getCount() - 2) && isOwner)
                || ((position == getCount() - 1) && !isOwner)) {

            userPhotoUrl = "drawable://" + R.drawable.icon_member_add;
            userName = context.getString(R.string.add);

        } else {
            String uid = uidList.get(position);
            userName = ContactUserCacheUtils.getUserName(uid);
            userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
        }
        nameText.setText(userName);
        ImageDisplayUtils.getInstance().displayImage(photoImg, userPhotoUrl, R.drawable.icon_person_default);
        return convertView;
    }
}
