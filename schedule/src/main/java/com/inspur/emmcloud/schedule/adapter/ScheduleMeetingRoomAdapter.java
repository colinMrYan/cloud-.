package com.inspur.emmcloud.schedule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.bean.meeting.MeetingRoom;
import com.inspur.emmcloud.schedule.bean.meeting.MeetingRoomArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/10.
 */

public class ScheduleMeetingRoomAdapter extends BaseExpandableListAdapter {
    private List<MeetingRoomArea> meetingRoomAreaList = new ArrayList<>();
    private Context context;

    public ScheduleMeetingRoomAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<MeetingRoomArea> meetingRoomAreaList) {
        this.meetingRoomAreaList.clear();
        this.meetingRoomAreaList.addAll(meetingRoomAreaList);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return meetingRoomAreaList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return meetingRoomAreaList.get(groupPosition).getMeetingRoomList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return meetingRoomAreaList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return meetingRoomAreaList.get(groupPosition).getMeetingRoomList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);
        TextView textView = new TextView(context);
        int paddingLeft = DensityUtil.dip2px(context, 16);
        int paddingTop = paddingLeft / 2;
        textView.setPadding(paddingLeft, paddingTop, 0, paddingTop);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(Color.parseColor("#999999"));
        textView.setText(meetingRoomAreaList.get(groupPosition).getName());
        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        MeetingRoom meetingRoom = meetingRoomAreaList.get(groupPosition).getMeetingRoomList().get(childPosition);
        convertView = LayoutInflater.from(context).inflate(R.layout.schedule_meeting_room_expandale_child_item, null);
        TextView nameText = convertView.findViewById(R.id.tv_name);
        TextView statusText = convertView.findViewById(R.id.tv_status);
        ImageView statusPointImg = convertView.findViewById(R.id.iv_status_point);
        TextView peopleNumText = convertView.findViewById(R.id.tv_people_num);
        LinearLayout equipmentLayout = convertView.findViewById(R.id.ll_equipment);
        LinearLayout dayStatusLayout = convertView.findViewById(R.id.ll_day_status);
        showMeetingRoomEquipment(equipmentLayout, meetingRoom.getEquipmentList());
        showMeetingRoomDayStatus(dayStatusLayout, meetingRoom.getBusyDegreeList());
        View dividerView = convertView.findViewById(R.id.view_divider);
        dividerView.setVisibility(isLastChild ? View.GONE : View.VISIBLE);
        nameText.setText(meetingRoom.getName());
        peopleNumText.setText(meetingRoom.getCapacity() + "");
        boolean isInMeeting = meetingRoom.getLight().equals("RED");
        statusText.setText(isInMeeting ? context.getResources().getString(R.string.schedule_meeting_in_meeting) :
                context.getResources().getString(R.string.schedule_meeting_be_free));
        statusPointImg.setImageResource(isInMeeting ? R.drawable.schedule_meeting_room_busy_ic : R.drawable.schedule_meeting_room_free_ic);
        statusText.setTextColor(Color.parseColor(isInMeeting ? "#FF0000" : "#7ED321"));
        return convertView;
    }

    private void showMeetingRoomDayStatus(LinearLayout dayStatusLayout, List<Integer> busyDegressList) {
        dayStatusLayout.removeAllViews();
        for (Integer degree : busyDegressList) {
            ImageView imageView = new ImageView(context);
            int height = DensityUtil.dip2px(context, 17);
            int marginLeft = DensityUtil.dip2px(context, 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height, height);
            params.setMargins(marginLeft, 0, 0, 0);
            imageView.setLayoutParams(params);
            if (degree < 40) {
                imageView.setImageResource(R.drawable.ischedule_meeting_room_status_empty_ic);
            } else if (degree > 70) {
                imageView.setImageResource(R.drawable.schedule_meeting_room_status_full_ic);
            } else {
                imageView.setImageResource(R.drawable.schedule_meeting_room_status_half_ic);
            }
            dayStatusLayout.addView(imageView);
        }
    }

    private void showMeetingRoomEquipment(LinearLayout equipmentLayout, List<String> equipmentList) {
        for (String equipment : equipmentList) {
            int equipmentResId = -1;
            switch (equipment) {
                case "PROJECTOR":
                    equipmentResId = R.drawable.schedule_meeting_room_equipment_projector_ic;
                    break;
                case "WHITE_BOARD":
                    equipmentResId = R.drawable.schedule_meeting_room_equipment_white_borad_ic;
                    break;
                case "CONFERENCE_PHONE":
                    equipmentResId = R.drawable.schedule_meeting_room_equipment_conference_phone_ic;
                    break;
                case "WIFI":
                    equipmentResId = R.drawable.schedule_meeting_room_equipment_wifi_ic;
                    break;
                default:
                    continue;
            }
            ImageView imageView = new ImageView(context);
            int height = DensityUtil.dip2px(context, 17);
            int marginLeft = DensityUtil.dip2px(context, 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height, height);
            params.setMargins(marginLeft, 0, 0, 0);
            imageView.setLayoutParams(params);
            imageView.setImageResource(equipmentResId);
            equipmentLayout.addView(imageView);
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
