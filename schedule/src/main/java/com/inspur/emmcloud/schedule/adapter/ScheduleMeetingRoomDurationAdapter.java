package com.inspur.emmcloud.schedule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.bean.meeting.Meeting;
import com.inspur.emmcloud.schedule.bean.meeting.MeetingSchedule;

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
        convertView = LayoutInflater.from(context).inflate(R.layout.schedule_meeting_room_info_list_item_view, null);
        RelativeLayout meetingContentLayout = convertView.findViewById(R.id.rl_meeting_content);
        LinearLayout meetingAddLayout = convertView.findViewById(R.id.ll_meeting_add);
        TextView timeText = convertView.findViewById(R.id.tv_time);
        Calendar StartTimeCalendar = TimeUtils.timeLong2Calendar(meetingSchedule.getFrom());
        String startTime = TimeUtils.calendar2FormatString(BaseApplication.getInstance(), StartTimeCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        Calendar endTimeCalendar = TimeUtils.timeLong2Calendar(meetingSchedule.getTo());
        String endTime = TimeUtils.calendar2FormatString(BaseApplication.getInstance(), endTimeCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        timeText.setText(startTime + "-" + endTime);
        View flagView = convertView.findViewById(R.id.v_flag);
        if (meeting == null) {
            timeText.setTextColor(Color.parseColor("#999999"));
            meetingContentLayout.setVisibility(View.GONE);
            meetingAddLayout.setVisibility(View.VISIBLE);
            flagView.setBackgroundColor(Color.parseColor("#36A5F6"));
        } else {
//            timeText.setTextColor(ResourceUtils.getResValueOfAttr(context, com.inspur.baselib.R.attr.text_color));
            meetingContentLayout.setVisibility(View.VISIBLE);
            meetingAddLayout.setVisibility(View.GONE);
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser contactUser = contactService != null ? contactService.getContactUserByUid(meeting.getOwner()) : null;
            String owner = contactUser == null ? contactUser.getName() : "";
            ((TextView) convertView.findViewById(R.id.tv_owner)).setText(owner);
            ((TextView) convertView.findViewById(R.id.tv_title)).setText(meeting.getTitle());
            flagView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        return convertView;
    }
}
