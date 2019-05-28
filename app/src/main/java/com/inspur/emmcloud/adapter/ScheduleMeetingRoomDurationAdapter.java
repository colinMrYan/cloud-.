package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingSchedule;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/15.
 */

public class ScheduleMeetingRoomDurationAdapter extends BaseAdapter {
    private Context context;
    private List<MeetingSchedule> meetingScheduleList = new ArrayList<>();

    public ScheduleMeetingRoomDurationAdapter(Context context, List<MeetingSchedule> meetingScheduleList) {
        this.context = context;
        this.meetingScheduleList = meetingScheduleList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return meetingScheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub
        MeetingSchedule meetingSchedule = meetingScheduleList.get(position);
        Meeting meeting = meetingSchedule.getMeeting();
        convertView = LayoutInflater.from(context).inflate(R.layout.meeting_room_info_list_item_view,null);
        RelativeLayout meetingContentLayout = convertView.findViewById(R.id.rl_meeting_content);
        LinearLayout meetingAddLayout = convertView.findViewById(R.id.ll_meeting_add);
        TextView timeText = convertView.findViewById(R.id.tv_time);
        Calendar StartTimeCalendar = TimeUtils.timeLong2Calendar(meetingSchedule.getFrom());
        String startTime = TimeUtils.calendar2FormatString(MyApplication.getInstance(),StartTimeCalendar,TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        Calendar endTimeCalendar = TimeUtils.timeLong2Calendar(meetingSchedule.getTo());
        String endTime = TimeUtils.calendar2FormatString(MyApplication.getInstance(),endTimeCalendar,TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        timeText.setText(startTime+"-"+endTime);
        View flagView = convertView.findViewById(R.id.v_flag);
        if (meeting == null){
            timeText.setTextColor(Color.parseColor("#999999"));
            meetingContentLayout.setVisibility(View.GONE);
            meetingAddLayout.setVisibility(View.VISIBLE);
            flagView.setBackgroundColor(Color.parseColor("#36A5F6"));
        }else {
            timeText.setTextColor(Color.parseColor("#333333"));
            meetingContentLayout.setVisibility(View.VISIBLE);
            meetingAddLayout.setVisibility(View.GONE);
            String owner = ContactUserCacheUtils.getUserName(meeting.getOwner());
            ((TextView)convertView.findViewById(R.id.tv_owner)).setText(owner);
            ((TextView)convertView.findViewById(R.id.tv_title)).setText(meeting.getTitle());
            flagView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        return convertView;
    }
}
