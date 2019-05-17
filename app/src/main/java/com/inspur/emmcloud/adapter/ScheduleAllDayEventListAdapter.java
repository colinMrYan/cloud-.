package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.widget.calendardayview.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/2.
 */

public class ScheduleAllDayEventListAdapter extends BaseAdapter {
    private List<Event> eventList = new ArrayList<>();
    private Context context;

    public ScheduleAllDayEventListAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList.clear();
        this.eventList.addAll(eventList);
    }


    @Override
    public int getCount() {
        return eventList.size();
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
        Event event = eventList.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.schedule_all_day_event_list_item_view,null);
        ImageView iconImg = convertView.findViewById(R.id.iv_event);
        TextView titleText = convertView.findViewById(R.id.tv_event_title);
        TextView positionText = convertView.findViewById(R.id.tv_event_position);
        iconImg.setImageResource(event.getEventIconResId());
        titleText.setText(event.getEventTitle());
        if (event.getEventType().equals(Schedule.TYPE_MEETING)){
            positionText.setVisibility(View.VISIBLE);
            positionText.setText(event.getEventSubTitle());
        }else {
            positionText.setVisibility(View.GONE);
        }
        return convertView;
    }
}
