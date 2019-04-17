package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.util.privates.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/2.
 */

public class ScheduleMeetingListAdapter extends RecyclerView.Adapter<ScheduleMeetingListAdapter.ViewHolder> {
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

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_meeting_list_item_view, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);
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
            dateBuilder.append(TimeUtils.getWeekDay(context, startCalendar));
        } else {
            dateBuilder.append("  ").append(TimeUtils.Calendar2TimeString(endCalendar, TimeUtils.getFormat(context, TimeUtils.FORMAT_MONTH_DAY)));
        }
        holder.dateText.setText(dateBuilder.toString());
        holder.buildingText.setText(meeting.getScheduleLocationObj().getBuilding());
    }


    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        this.onItemClickLister = onItemClickLister;
    }

    public interface OnItemClickLister {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iconImg;
        private TextView titleText;
        private TextView displayNameText;
        private TextView timeText;
        private TextView buildingText;
        private TextView dateText;

        public ViewHolder(View convertView) {
            super(convertView);
            itemView.setOnClickListener(this);
            iconImg = convertView.findViewById(R.id.icon_image);
            titleText = convertView.findViewById(R.id.tv_title);
            displayNameText = convertView.findViewById(R.id.tv_display_name);
            timeText = convertView.findViewById(R.id.tv_time);
            buildingText = convertView.findViewById(R.id.tv_building);
            dateText = convertView.findViewById(R.id.tv_date);


        }

        @Override
        public void onClick(View view) {
            if (onItemClickLister != null) {
                onItemClickLister.onItemClick(view, getAdapterPosition());
            }

        }
    }
}
