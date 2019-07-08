package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/15.
 */

public class MeetingOfficeAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<MeetingLocation> locationList = new ArrayList<>();


    public MeetingOfficeAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<MeetingLocation> locationList) {
        this.locationList = locationList;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return locationList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return locationList.get(groupPosition).getOfficeBuildingList().size();
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
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);
        TextView textView = new TextView(context);
        int paddingLeft = DensityUtil.dip2px(context, 16);
        int paddingTop = paddingLeft / 2;
        textView.setPadding(paddingLeft, paddingTop, 0, paddingTop);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(Color.parseColor("#999999"));
        textView.setText(locationList.get(groupPosition).getName());
        return textView;
    }

    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        Building building = locationList.get(groupPosition).getOfficeBuildingList().get(childPosition);
        convertView = LayoutInflater.from(context).inflate(
                R.layout.meeting_location_expandale_child_item, null);
        TextView nameText = convertView.findViewById(R.id.tv_name);
        ImageView selectImg = convertView.findViewById(R.id.iv_select);
        nameText.setText(building.getName());
        selectImg.setImageResource(building.isFavorite() ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
