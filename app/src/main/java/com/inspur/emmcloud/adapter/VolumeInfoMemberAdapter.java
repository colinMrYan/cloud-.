package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.List;

/**
 * Created by chenmch on 2018/1/20.
 */

public class VolumeInfoMemberAdapter extends BaseAdapter {
    private Context context;
    private List<String> memberList;
    private boolean isOwner;

    public VolumeInfoMemberAdapter(Context context, List<String> memberList, boolean isOwner) {
        this.context = context;
        this.memberList = memberList;
        this.isOwner = isOwner;
    }

    @Override
    public int getCount() {
        if (isOwner) {
            return memberList.size() > 9 ? 10 : memberList.size() + 2;
        } else {
            return memberList.size() > 10 ? 10 : memberList.size();
        }
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
        CircleTextImageView memberHeadImg = (CircleTextImageView) convertView
                .findViewById(R.id.member_head_img);
        TextView nameText = (TextView) convertView
                .findViewById(R.id.tv_name);
        String userPhotoUrl = null;
        String userName = null;
        if ((position == getCount() - 1) && isOwner) {
            userPhotoUrl = "drawable://" + R.drawable.icon_group_delete;
            userName = context.getString(R.string.delete);

        } else if ((position == getCount() - 2) && isOwner) {

            userPhotoUrl = "drawable://" + R.drawable.icon_member_add;
            userName = context.getString(R.string.add);

        } else {
            String uid = memberList.get(position);
            userName = ContactUserCacheUtils.getUserName(uid);
            userPhotoUrl = APIUri.getUserIconUrl(context, uid);
        }
        nameText.setText(userName);
        ImageDisplayUtils.getInstance().displayImage(memberHeadImg, userPhotoUrl, R.drawable.icon_photo_default);
        return convertView;
    }
}
