package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.interf.ScheduleEventListener;
import com.inspur.emmcloud.util.privates.CalendarUtils;
import com.inspur.emmcloud.widget.calendardayview.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/2.
 */

public class ScheduleAllDayEventListAdapter extends BaseAdapter {
    private List<Event> eventList = new ArrayList<>();
    private Context context;
    private Calendar selectCalendar;
    private ScheduleEventListener onEventClickListener;

    public ScheduleAllDayEventListAdapter(Context context, List<Event> eventList, Calendar selectCalendar) {
        this.context = context;
        this.eventList.clear();
        this.eventList.addAll(eventList);
        this.selectCalendar = selectCalendar;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList.clear();
        this.eventList.addAll(eventList);
    }

    public void setOnEventClickListener(ScheduleEventListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog(final Event event) {
        new CustomDialog.MessageDialogBuilder(context)
                .setMessage(event.getEventType().equals(Schedule.TYPE_MEETING) ? R.string.meeting_cancel_the_meeting : R.string.calendar_cancel_the_schedule)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (onEventClickListener != null) {
                            onEventClickListener.onEventDelete(event);
                        }
                    }
                })
                .show();
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
    public View getView(int position, View contentView, ViewGroup parent) {
        final Event event = eventList.get(position);
        contentView = LayoutInflater.from(context).inflate(R.layout.schedule_all_day_event_list_item_view, null);
        TextView popCalendarNameText = contentView.findViewById(R.id.tv_calendar_name);
        ImageView popCalendarTypeImg = contentView.findViewById(R.id.iv_calendar_type);
        TextView popEventTitleText = contentView.findViewById(R.id.tv_event_title);
        TextView popEventTimeText = contentView.findViewById(R.id.tv_event_time);
        ImageView popEventTitleImg = contentView.findViewById(R.id.iv_event_title);
        popEventTitleImg.setImageResource(event.getEventIconResId(false));
        popCalendarNameText.setText(CalendarUtils.getCalendarName(event));
        popCalendarNameText.setTextColor(CalendarUtils.getCalendarTypeColor(event));
        int resId = CalendarUtils.getCalendarTypeImgResId(event);
        if (resId != -1) {
            popCalendarTypeImg.setImageResource(resId);
        }
        popEventTitleText.setText(event.getEventTitle());
        String date = TimeUtils.calendar2FormatString(context, selectCalendar, TimeUtils.FORMAT_MONTH_DAY);
        String week = TimeUtils.getWeekDay(context, selectCalendar);
        popEventTimeText.setText(date + " " + week);

        ImageView shareImage = contentView.findViewById(R.id.iv_share);
        ImageView deleteImage = contentView.findViewById(R.id.iv_delete);
        ImageView groupChatImage = contentView.findViewById(R.id.iv_group_chat);
        contentView.findViewById(R.id.tv_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventClickListener != null) {
                    onEventClickListener.onShowEventDetail(event);
                    onEventClickListener.dismissAllDayEventDlg();
                }
            }
        });
        //删除
        if (event.canDelete()) {
            deleteImage.setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.iv_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmClearDialog(event);
                }
            });
        } else {
            deleteImage.setVisibility(View.GONE);
        }
        //发起群聊
        if (event.getEventType().equals(Schedule.TYPE_MEETING)) {
            groupChatImage.setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.iv_group_chat).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onEventClickListener != null) {
                        onEventClickListener.onGroupChat(event);
                        onEventClickListener.dismissAllDayEventDlg();
                    }
                }
            });
        } else {
            groupChatImage.setVisibility(View.GONE);
        }
        //V0环境不显示分享按钮
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            shareImage.setVisibility(View.GONE);
        }
        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventShare(event);
                    onEventClickListener.dismissAllDayEventDlg();
                }


            }
        });
        return contentView;
    }
}
