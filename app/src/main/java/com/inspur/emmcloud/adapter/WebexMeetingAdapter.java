package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by chenmch on 2018/10/11.
 */

public class WebexMeetingAdapter extends BaseExpandableListAdapter {
    private Context context;

    public WebexMeetingAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return 10;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 3;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_meeting_group, null);
        TextView weekText = (TextView) convertView.findViewById(R.id.tv_week);
        TextView dateText = (TextView) convertView.findViewById(R.id.tv_date);
        weekText.setText("星期一");
        dateText.setText("2018年10月12日");
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ExpandViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_meeting_child, null);
            holder = new ExpandViewHolder();
            holder.timeText = (TextView) convertView
                    .findViewById(R.id.tv_time);
            holder.titleText = (TextView) convertView
                    .findViewById(R.id.tv_title);
            holder.ownerText = (TextView) convertView
                    .findViewById(R.id.tv_owner);
            holder.line = convertView.findViewById(R.id.v_line);

            convertView.setTag(holder);
        } else {
            holder = (ExpandViewHolder) convertView.getTag();
        }
        holder.timeText.setText("15:00 - 15:30");
        holder.titleText.setText("周例会");
        holder.ownerText.setText("浪潮国际miaochw");
        holder.line.setVisibility(isLastChild?View.INVISIBLE:View.VISIBLE);
        return convertView;
    }

    class ExpandViewHolder {
        TextView timeText;
        TextView titleText;
        TextView ownerText;
        View line;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

