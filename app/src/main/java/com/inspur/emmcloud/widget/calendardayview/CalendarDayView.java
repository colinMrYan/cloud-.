package com.inspur.emmcloud.widget.calendardayview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.util.common.DensityUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2019/3/29.
 */

public class CalendarDayView extends RelativeLayout {
    private static final int TIME_HOUR_HEIGHT = DensityUtil.dip2px(MyApplication.getInstance(), 40);
    private static final int EVENTT_GAP = DensityUtil.dip2px(MyApplication.getInstance(), 2);
    private String[] dayHourTimes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "10", "10"};
    private List<TimeHourRow> timeHourRowList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private RelativeLayout eventLayout;
    private OnEventClickListener onEventClickListener;
    private RelativeLayout currentTimeLineLayout;
    private LinearLayout timeHourLayout;
    private Calendar selectCalendar;

    public CalendarDayView(Context context) {
        this(context, null);
    }

    public CalendarDayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.calendar_day_view, this, true);
        initTimeHourLayout(view);
    }

    /**
     * 初始化24小时时间轴
     *
     * @param view
     */
    private void initTimeHourLayout(View view) {
        eventLayout = view.findViewById(R.id.rl_event);
        timeHourLayout = view.findViewById(R.id.ll_time_hour);
        for (int i = 0; i < dayHourTimes.length; i++) {
            View hourLayout = LayoutInflater.from(getContext()).inflate(R.layout.calendar_day_view_hour, null, false);
            hourLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TIME_HOUR_HEIGHT));
            TextView hourText = hourLayout.findViewById(R.id.tv_hour);
            hourText.setText(dayHourTimes[i]);
            TextView amText = hourLayout.findViewById(R.id.tv_am);
            if (i == 7) {
                amText.setText("上午");
            } else if (i == 13) {
                amText.setText("下午");
            }

            timeHourLayout.addView(hourLayout);
        }
        currentTimeLineLayout = view.findViewById(R.id.tl_current_time_line);
    }

    public void setEventList(List<Event> eventList, Calendar selectCalendar) {
        this.selectCalendar = selectCalendar;
        this.eventList = eventList;
        initTimeHourRow();
        showEventList();
    }

    /**
     * 显示时间轴中当前时间
     *
     * @param isShow
     */
    public void setCurrentTimeLineShow(boolean isShow) {
        if (isShow) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) currentTimeLineLayout.getLayoutParams();
            Calendar currentCalendar = Calendar.getInstance();
            int marginTop = (int) ((currentCalendar.get(Calendar.HOUR_OF_DAY) + currentCalendar.get(Calendar.MINUTE) / 60.0f) * TIME_HOUR_HEIGHT - DensityUtil.dip2px(MyApplication.getInstance(), 3));
            params.setMargins(DensityUtil.dip2px(getContext(), 24), marginTop, 0, 0);
            currentTimeLineLayout.setLayoutParams(params);
            currentTimeLineLayout.setVisibility(VISIBLE);
        } else {
            currentTimeLineLayout.setVisibility(INVISIBLE);
        }

    }

    /**
     * 初始化每个小时时间段，将所有有交集的时间段整合起来，这些事件段汇总event的宽度是一致的。
     */
    private void initTimeHourRow() {
        timeHourRowList.clear();
        int parentWidth = this.getWidth();
        for (int i = 0; i < 24; i++) {
            timeHourRowList.add(new TimeHourRow(i, parentWidth));
        }
        List<MatheSet> matheSetList = new ArrayList<>();

        for (Event event : eventList) {
            int startHour = event.getDayEventStartTime(selectCalendar).get(Calendar.HOUR_OF_DAY);
            int endHour = event.getDayEventEndTime(selectCalendar).get(Calendar.HOUR_OF_DAY);
            int endMin = event.getDayEventEndTime(selectCalendar).get(Calendar.MINUTE);
            if (endMin == 0) {
                endHour = endHour - 1;
            }
            for (int i = startHour; i <= endHour; i++) {
                timeHourRowList.get(i).getEventList().add(event);
            }
            MatheSet matheSet = new MatheSet((long) startHour, (long) endHour);
            Iterator<MatheSet> it = matheSetList.iterator();
            while (it.hasNext()) {
                MatheSet temp = it.next();
                if (MatheSet.isIntersection(temp, matheSet)) {
                    matheSet.merge(temp);
                    it.remove();
                }
            }
            matheSetList.add(matheSet);
        }
        setEventMaxWidth(matheSetList);
    }


    /**
     * 设置每个时间段event最大宽度
     *
     * @param matheSetList
     */
    private void setEventMaxWidth(List<MatheSet> matheSetList) {
        for (TimeHourRow timeHourRow : timeHourRowList) {
            int timeHourRowEventSize = timeHourRow.getEventList().size();
            if (timeHourRowEventSize > 1) {
                timeHourRow.setEventWidth((this.getWidth() - timeHourRowEventSize * EVENTT_GAP) / timeHourRowEventSize);
            }
        }

        for (MatheSet matheSet : matheSetList) {
            int minChildWidth = this.getWidth();
            for (int i = (int) matheSet.getStart(); i <= matheSet.getEnd(); i++) {
                if (minChildWidth > timeHourRowList.get(i).getEventWidth()) {
                    minChildWidth = timeHourRowList.get(i).getEventWidth();
                }
            }
            for (int i = (int) matheSet.getStart(); i <= matheSet.getEnd(); i++) {
                timeHourRowList.get(i).setEventWidth(minChildWidth);
            }

        }
    }

    private void showEventList() {
        eventLayout.removeAllViews();
        for (TimeHourRow timeHourRow : timeHourRowList) {
            List<Event> eventList = timeHourRow.getEventList();
            Collections.sort(eventList, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    if (event1.getIndex() >= 0 && event2.getIndex() >= 0) {
                        return (event1.getIndex() - event2.getIndex());
                    }

                    if (event1.getIndex() >= 0 && event2.getIndex() < 0) {
                        return -1;
                    }

                    if (event1.getIndex() < 0 && event2.getIndex() >= 0) {
                        return 1;
                    }
                    return event2.getDayDurationInMillSeconds(selectCalendar) >= event1.getDayDurationInMillSeconds(selectCalendar) ? 1 : 0;
                }
            });
            List<Event> currentHourStartEventList = new ArrayList<>();
            List<Event> currentOtherEventList = new ArrayList<>();
            for (Event event : eventList) {
                if (event.getIndex() < 0) {
                    currentHourStartEventList.add(event);
                } else {
                    currentOtherEventList.add(event);
                }
            }
            eventList.clear();
            eventList.addAll(currentHourStartEventList);
            for (Event event : currentOtherEventList) {
                if (event.getIndex() <= eventList.size()) {
                    eventList.add(event.getIndex(), event);
                }
            }
            for (int i = 0; i < eventList.size(); i++) {
                Event event = eventList.get(i);
                if (event.getIndex() < 0) {
                    event.setIndex(i);
                    int eventWidth = timeHourRow.getEventWidth();
                    int eventHeight = (int) (event.getDayDurationInMillSeconds(selectCalendar) * DensityUtil.dip2px(getContext(), 40) / 3600000);
                    int eventMinHeight = DensityUtil.dip2px(MyApplication.getInstance(), 18);
                    if (eventHeight < eventMinHeight) {
                        eventHeight = eventMinHeight;
                    }
                    int maginLeft = EVENTT_GAP * i + eventWidth * i;
                    Calendar startTime = event.getDayEventStartTime(selectCalendar);
                    Calendar dayStartTime = (Calendar) startTime.clone();
                    dayStartTime.set(Calendar.HOUR_OF_DAY, 0);
                    dayStartTime.set(Calendar.MINUTE, 0);
                    int marginTop = (int) ((startTime.getTimeInMillis() - dayStartTime.getTimeInMillis()) * DensityUtil.dip2px(getContext(), 40) / 3600000);
                    RelativeLayout.LayoutParams eventLayoutParams = new RelativeLayout.LayoutParams(eventWidth,
                            eventHeight);
                    eventLayoutParams.setMargins(maginLeft, marginTop, 0, 0);
                    setEventLayout(event, eventLayoutParams);
                }

            }
        }
    }

    private void setEventLayout(final Event event, RelativeLayout.LayoutParams eventLayoutParams) {
        View eventView = LayoutInflater.from(getContext()).inflate(R.layout.calendar_day_event_view, null);
        eventView.setBackgroundResource(R.drawable.ic_schedule_calendar_view_event_bg);
        ImageView eventImg = eventView.findViewById(R.id.iv_event);
        TextView eventTitleEvent = eventView.findViewById(R.id.tv_event_title);
        TextView eventSubtitleEvent = eventView.findViewById(R.id.tv_event_subtitle);
        eventImg.setImageResource(event.getEventIconResId());
        eventTitleEvent.setText(event.getEventTitle());
        String subTitle = event.getShowEventSubTitle(getContext(), selectCalendar);
        if (event.getEventType().equals(Event.TYPE_TASK)) {
            subTitle += "截止";
        }
        eventSubtitleEvent.setText(subTitle);
        eventLayout.addView(eventView, eventLayoutParams);
        eventView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventClick(event);
                }

            }
        });
    }

    public void setOnEventClickListener(OnEventClickListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}