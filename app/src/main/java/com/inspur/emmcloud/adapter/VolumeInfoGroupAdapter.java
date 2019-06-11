package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.volume.Group;

import java.util.List;

/**
 * Created by chenmch on 2018/1/22.
 */

public class VolumeInfoGroupAdapter extends BaseAdapter {

    private Context context;
    private List<Group> groupList;

    public VolumeInfoGroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @Override
    public int getCount() {
        return groupList.size();
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
        Group group = groupList.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.app_volume_info_group_item_view, null);
        TextView nameText = (TextView) convertView.findViewById(R.id.tv_name);
        TextView numText = (TextView) convertView.findViewById(R.id.num_text);
        nameText.setText(group.getName());
        numText.setText(group.getMemberUidList().size() + "人");
        return convertView;
    }
}
