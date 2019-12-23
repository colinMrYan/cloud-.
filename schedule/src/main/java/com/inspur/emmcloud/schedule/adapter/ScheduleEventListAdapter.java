package com.inspur.emmcloud.schedule.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.widget.calendardayview.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/2.
 */

public class ScheduleEventListAdapter extends RecyclerView.Adapter<ScheduleEventListAdapter.ViewHolder> {
    private List<Event> eventList = new ArrayList<>();
    private Context context;
    private OnItemClickLister onItemClickLister;
    private Calendar selectCalendar;

    public ScheduleEventListAdapter(Context context) {
        this.context = context;
    }

    public void setEventList(Calendar selectCalendar, List<Event> eventList) {
        this.selectCalendar = selectCalendar;
        this.eventList.clear();
        this.eventList.addAll(eventList);
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        this.onItemClickLister = onItemClickLister;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_event_list_item_view, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTileText.setText(event.getEventTitle());

        String startTime = "";
        String endTime = "";
        if (event.getEventType().equals(Schedule.TYPE_CALENDAR)) {
            holder.eventPositionText.setVisibility(View.GONE);
        } else if (event.getEventType().equals(Schedule.TYPE_MEETING)) {
            holder.eventPositionText.setVisibility(View.VISIBLE);
            if (!StringUtils.isBlank(event.getEventSubTitle())) {
                holder.eventPositionText.setText(context.getString(R.string.schedule_every_detail_location) + event.getEventSubTitle());
            }

        }
        startTime = TimeUtils.calendar2FormatString(context, event.getDayEventStartTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
        if (event.isAllDay() || (!event.getEventStartTime().after(TimeUtils.getDayBeginCalendar(selectCalendar)) && !event.getEventEndTime().before(TimeUtils.getDayEndCalendar(selectCalendar)))) {
            startTime = context.getString(R.string.all_day);
        } else {
            if (!event.getEventEndTime().before(TimeUtils.getDayEndCalendar(selectCalendar))) {
                endTime = "24:00";
            } else {
                endTime = TimeUtils.calendar2FormatString(context, event.getDayEventEndTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
            }
        }
        holder.eventImg.setImageResource(event.getEventIconResId(false));
        holder.eventColorImage.setImageResource(event.getCalendarIconResId());
        holder.eventStartTimeText.setText(startTime);
        holder.eventEndTimeText.setText(endTime);
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }


    public interface OnItemClickLister {
        void onItemClick(View view, int position, Event event);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView eventImg;
        private TextView eventStartTimeText;
        private TextView eventEndTimeText;
        private TextView eventTileText;
        private TextView eventPositionText;
        private ImageView eventColorImage;

        public ViewHolder(View convertView) {
            super(convertView);
            itemView.setOnClickListener(this);
            eventImg = convertView.findViewById(R.id.iv_event);
            eventStartTimeText = convertView.findViewById(R.id.tv_event_start_time);
            eventEndTimeText = convertView.findViewById(R.id.tv_event_end_time);
            eventTileText = convertView.findViewById(R.id.tv_event_title);
            eventPositionText = convertView.findViewById(R.id.tv_event_position);
            eventColorImage = convertView.findViewById(R.id.iv_event_color);


        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (onItemClickLister != null && position > -1) {
                onItemClickLister.onItemClick(view, getAdapterPosition(), eventList.get(getAdapterPosition()));
            }

        }
    }
}
