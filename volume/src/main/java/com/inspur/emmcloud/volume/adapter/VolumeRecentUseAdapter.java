package com.inspur.emmcloud.volume.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.volume.R;

/**
 * 最近使用云盘列表adapter
 */

public class VolumeRecentUseAdapter extends BaseAdapter {

    private Context context;

    public VolumeRecentUseAdapter(Context context) {
        this.context = context;

    }

    @Override
    public int getCount() {
        return 2;
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
        convertView = LayoutInflater.from(context).inflate(R.layout.volume_app_volume_recent_use_item_view, null);
        TextView textView = (TextView) convertView.findViewById(R.id.volume_name_text);
        (convertView.findViewById(R.id.volume_capacity_text)).setVisibility(View.GONE);
        return convertView;
    }
}
