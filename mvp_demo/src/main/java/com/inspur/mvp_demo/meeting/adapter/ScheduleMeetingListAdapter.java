package com.inspur.mvp_demo.meeting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.mvp_demo.R;
import com.inspur.mvp_demo.meeting.model.bean.Meeting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/2.
 */

public class ScheduleMeetingListAdapter extends BaseAdapter {
    private List<Meeting> meetingList = new ArrayList<>();
    private Context context;
    private OnItemClickLister onItemClickLister;

    public ScheduleMeetingListAdapter(Context context) {
        this.context = context;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.meetingList.clear();
        this.meetingList.addAll(meetingList);
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        this.onItemClickLister = onItemClickLister;
    }

    @Override
    public int getCount() {
        return meetingList.size();
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
        if (null == view) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.schedule_meeting_list_item_mvp_view, null);
            holder.buildingText = view.findViewById(R.id.tv_building);
            holder.dateText = view.findViewById(R.id.tv_date);
            holder.displayNameText = view.findViewById(R.id.tv_display_name);
            holder.iconImg = view.findViewById(R.id.iv_icon);
            holder.timeText = view.findViewById(R.id.tv_time);
            holder.titleText = view.findViewById(R.id.tv_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Meeting meeting = meetingList.get(i);
        Calendar startCalendar = meeting.getStartTimeCalendar();
        Calendar endCalendar = meeting.getEndTimeCalendar();
        holder.titleText.setText(meeting.getTitle());
        String startTime = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(context, TimeUtils.FORMAT_HOUR_MINUTE));
        String endTime = TimeUtils.Calendar2TimeString(endCalendar, TimeUtils.getFormat(context, TimeUtils.FORMAT_HOUR_MINUTE));
        holder.timeText.setText(startTime + "-" + endTime);
        holder.displayNameText.setText(meeting.getScheduleLocationObj().getDisplayName());
        StringBuilder dateBuilder = new StringBuilder();
        dateBuilder.append(TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(context, TimeUtils.FORMAT_MONTH_DAY)));
        if (TimeUtils.isSameDay(startCalendar, endCalendar)) {
            dateBuilder.append(" ").append(TimeUtils.getWeekDay(context, startCalendar));
        } else {
            dateBuilder.append(" ").append(TimeUtils.Calendar2TimeString(endCalendar, TimeUtils.getFormat(context, TimeUtils.FORMAT_MONTH_DAY)));
        }
        holder.dateText.setText(dateBuilder.toString());
        holder.buildingText.setText(meeting.getScheduleLocationObj().getBuilding());
        if (StringUtils.isBlank(meeting.getScheduleLocationObj().getId())) {
            holder.iconImg.setImageResource(R.drawable.ic_schedule_meeting_type_out_mvp);
        } else {
            holder.iconImg.setImageResource(R.drawable.ic_schedule_meeting_type_common_mvp);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickLister != null) {
                    onItemClickLister.onItemClick(view, i);
                }
            }
        });
        return view;
    }

    public interface OnItemClickLister {
        void onItemClick(View view, int position);
    }

    class ViewHolder {
        public ImageView iconImg;
        public TextView titleText;
        public TextView displayNameText;
        public TextView timeText;
        public TextView buildingText;
        public TextView dateText;
    }
}
