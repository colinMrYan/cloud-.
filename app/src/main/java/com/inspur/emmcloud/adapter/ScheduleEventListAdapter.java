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
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.calendardayview.Event;

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

    public ScheduleEventListAdapter(Context context,List<Event> eventList){
        this.context = context;
        this.eventList = eventList;
        this.selectCalendar = selectCalendar;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_event_list_item,null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTileText.setText(event.getEventTitle());

        int eventImgResId;
        String startTime="";
        String endTime="";
        if (event.getEventType().equals(Event.TYPE_CALENDAR)) {
            eventImgResId = R.drawable.ic_schedule_event_calendar;
            endTime="截止";
        } else if (event.getEventType().equals(Event.TYPE_MEETING)) {
            eventImgResId = R.drawable.ic_schedule_event_meeing;
            startTime = TimeUtils.calendar2FormatString(context,event.getEventStartTime(),TimeUtils.FORMAT_HOUR_MINUTE);
            endTime = TimeUtils.calendar2FormatString(context,event.getEventEndTime(),TimeUtils.FORMAT_HOUR_MINUTE);
        } else {
            eventImgResId = R.drawable.ic_schedule_event_task;
            endTime="截止";
        }
        holder.eventImg.setImageResource(eventImgResId);
        holder.eventStartTimeText.setText(startTime);
    }


    @Override
    public int getItemCount() {
        return 0;
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister){
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView eventImg;
        private TextView eventStartTimeText;
        private TextView eventEndTimeText;
        private TextView eventTileText;
        private TextView eventPositionText;

        public ViewHolder(View convertView){
            super(convertView);
            itemView.setOnClickListener(this);
            eventImg = convertView.findViewById(R.id.iv_event);
            eventStartTimeText = convertView.findViewById(R.id.tv_event_start_time);
            eventEndTimeText = convertView.findViewById(R.id.tv_event_end_time);
            eventTileText = convertView.findViewById(R.id.tv_event_title);
            eventPositionText = convertView.findViewById(R.id.tv_event_position);


        }
        @Override
        public void onClick(View view) {
            if (onItemClickLister != null){
                onItemClickLister.onItemClick(view,getAdapterPosition());
            }

        }
    }

    public interface OnItemClickLister{
        void onItemClick(View view, int position);
    }
}
