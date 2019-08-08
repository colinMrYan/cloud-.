package com.inspur.emmcloud.widget.calendardayview;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButtonDrawable;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.interf.ScheduleEventListener;
import com.inspur.emmcloud.util.privates.CalendarUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2019/3/29.
 */

public class CalendarDayView extends RelativeLayout implements View.OnLongClickListener {
    private static final int TIME_HOUR_HEIGHT = DensityUtil.dip2px(MyApplication.getInstance(), 40);
    private static final int EVENT_GAP = DensityUtil.dip2px(MyApplication.getInstance(), 2);
    private String[] dayHourTimes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23"};
    private List<TimeHourRow> timeHourRowList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private RelativeLayout eventLayout;
    private ScheduleEventListener onEventClickListener;
    private RelativeLayout currentTimeLineLayout;
    private LinearLayout timeHourLayout;
    private Calendar selectCalendar;
    private TextView dragViewStartTmeText;
    private TextView dragViewEndTimeText;
    private int earliestEventOffset = -1;
    private List<List<Event>> intersectionGroupList = new ArrayList<>();

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.schedule_calendar_day_view, this, true);
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
        dragViewStartTmeText = view.findViewById(R.id.tv_drag_view_start_time);
        dragViewEndTimeText = view.findViewById(R.id.tv_drag_view_end_time);
        for (int i = 0; i < dayHourTimes.length; i++) {
            View hourLayout = LayoutInflater.from(getContext()).inflate(R.layout.schedule_calendar_day_view_hour, null, false);
            hourLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TIME_HOUR_HEIGHT));
            TextView hourText = hourLayout.findViewById(R.id.tv_hour);
            hourText.setText(dayHourTimes[i]);
            timeHourLayout.addView(hourLayout);
        }
        currentTimeLineLayout = view.findViewById(R.id.tl_current_time_line);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    /**
     * 显示添加日程Event的DragView的时间
     *
     * @param top
     * @param height
     */
    public void showDragViewTime(int top, int height) {
        RelativeLayout.LayoutParams paramsStart = (RelativeLayout.LayoutParams) dragViewStartTmeText.getLayoutParams();
        paramsStart.setMargins(0, top, 0, 0);
        dragViewStartTmeText.setLayoutParams(paramsStart);
        RelativeLayout.LayoutParams paramsEnd = (RelativeLayout.LayoutParams) dragViewEndTimeText.getLayoutParams();
        paramsEnd.setMargins(0, top + height, 0, 0);
        dragViewEndTimeText.setLayoutParams(paramsEnd);
        String startTime = getFormatTime(top);
        String endTime = getFormatTime(top + height);

        dragViewStartTmeText.setText(startTime);
        dragViewEndTimeText.setText(endTime);
        dragViewStartTmeText.setVisibility(View.VISIBLE);
        dragViewEndTimeText.setVisibility(View.VISIBLE);
    }

    private String getFormatTime(int offset) {
        int min = (int) (60 * offset * 1.0 / TIME_HOUR_HEIGHT);
        int hour = min / 60;
        min = min % 60;
        min = min / 15 * 15; //保证分钟数是15倍数
        return hour + (min > 0 ? ":" + (min < 10 ? "0" + min : min) : "");
    }

    public Calendar getDragViewStartTime(Calendar selectCalendar) {
        String startTime = dragViewStartTmeText.getText().toString();
        return getDragViewTime(selectCalendar, startTime);
    }

    public Calendar getDragViewEndTime(Calendar selectCalendar) {
        String endTime = dragViewEndTimeText.getText().toString();
        return getDragViewTime(selectCalendar, endTime);
    }


    public Calendar getDragViewTime(Calendar selectCalendar, String time) {
        String[] startTimeArray = time.split(":");
        int hour = Integer.valueOf(startTimeArray[0]);
        int min = 0;
        if (startTimeArray.length > 1) {
            min = Integer.valueOf(startTimeArray[1]);
        }
        Calendar calendar = (Calendar) selectCalendar.clone();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        return calendar;
    }

    /**
     * 隐藏添加日程Event的DragView
     */
    public void hideDragViewTime() {
        dragViewStartTmeText.setVisibility(View.GONE);
        dragViewEndTimeText.setVisibility(View.GONE);
    }

    /**
     * 每次打开日视图需要滚动到当前时间前一个小时
     */
    public int getScrollOffset() {
        int offset = 0;
        if (eventList.size() > 0) {
            offset = earliestEventOffset - TIME_HOUR_HEIGHT;
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            offset = (int) ((currentCalendar.get(Calendar.HOUR_OF_DAY) - 1 + currentCalendar.get(Calendar.MINUTE) / 60.0f) * TIME_HOUR_HEIGHT - DensityUtil.dip2px(MyApplication.getInstance(), 3));
        }
        if (offset < 0) {
            offset = 0;
        }
        return offset;
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
            params.setMargins(DensityUtil.dip2px(getContext(), 44), marginTop, 0, 0);
            currentTimeLineLayout.setLayoutParams(params);
            currentTimeLineLayout.setVisibility(VISIBLE);
        } else {
            currentTimeLineLayout.setVisibility(INVISIBLE);
        }

    }


    public void setEventList(List<Event> eventList, final Calendar selectCalendar) {
        this.selectCalendar = selectCalendar;
        this.eventList = eventList;
        sortEventList();
        setEventIntersectionGroup();
        showEventLayout();
    }

    /**
     * 按开始时间排序，便于按次序显示
     */
    private void sortEventList() {
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                long eventStartTime1 = event1.getEventStartTime().getTimeInMillis();
                long eventStartTime2 = event2.getEventStartTime().getTimeInMillis();
                if (eventStartTime1 > eventStartTime2) {
                    return 1;
                }

                if (eventStartTime1 < eventStartTime2) {
                    return -1;
                }
                return 0;
            }
        });
    }


    /**
     * 找出所有相交的Event并进行分组，不同组之间不相交，同组之间会有相交
     */
    private void setEventIntersectionGroup() {
        List<Event> copyEventList = new ArrayList<>();
        copyEventList.addAll(eventList);
        intersectionGroupList.clear();
        while (copyEventList.size() > 0) {
            List<Event> eventGroup = new ArrayList<>();
            Iterator<Event> it = copyEventList.iterator();
            while (it.hasNext()) {
                Event event = it.next();
                if (eventGroup.size() == 0) {
                    eventGroup.add(event);
                    it.remove();
                    continue;
                }
                MatheSet matheSet = new MatheSet(((Schedule) event.getEventObj()).getStartTime(), ((Schedule) event.getEventObj()).getEndTime());
                for (Event eventK : eventGroup) {
                    MatheSet matheSetK = new MatheSet(((Schedule) eventK.getEventObj()).getStartTime(), ((Schedule) eventK.getEventObj()).getEndTime());
                    if (MatheSet.isIntersectionWithoutBoundary(matheSet, matheSetK)) {
                        eventGroup.add(event);
                        it.remove();
                        break;
                    }

                }
            }
            int intersectionSize = eventGroup.size();
            int eventWidth = (eventLayout.getWidth() - (intersectionSize - 1) * EVENT_GAP) / intersectionSize;
            for (int i = 0; i < eventGroup.size(); i++) {
                Event event = eventGroup.get(i);
                int index = eventList.indexOf(event);
                eventList.get(index).setIndex(i);
                eventList.get(index).setMinWidth(eventWidth);
            }
            intersectionGroupList.add(eventGroup);
        }
    }

    private void showEventLayout() {
        earliestEventOffset = -1;
        eventLayout.removeAllViews();
        for (Event event : eventList) {
            int eventWidth = event.getMinWidth();
            int eventHeight = (int) (event.getDayDurationInMillSeconds(selectCalendar) * TIME_HOUR_HEIGHT / 3600000);
            int marginLeft = (EVENT_GAP + eventWidth) * event.getIndex();
            Calendar startTime = event.getDayEventStartTime(selectCalendar);
            Calendar dayStartTime = (Calendar) startTime.clone();
            dayStartTime.set(Calendar.HOUR_OF_DAY, 0);
            dayStartTime.set(Calendar.MINUTE, 0);
            int marginTop = (int) ((startTime.getTimeInMillis() - dayStartTime.getTimeInMillis()) * TIME_HOUR_HEIGHT / 3600000);
            //为了在打开当天日程时滚动到相应的位置
            if (earliestEventOffset == -1 || marginTop < earliestEventOffset) {
                earliestEventOffset = marginTop;
            }
            RelativeLayout.LayoutParams eventLayoutParams = new RelativeLayout.LayoutParams(eventWidth,
                    eventHeight);
            eventLayoutParams.setMargins(marginLeft, marginTop, 0, 0);
            setEventLayout(event, eventLayoutParams);
        }
    }

    private void setEventLayout(final Event event, final RelativeLayout.LayoutParams eventLayoutParams) {
        View eventView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_calendar_day_event_view, null);
        final Drawable drawableNormal = event.getEventBgNormalDrawable();
        eventView.setBackground(drawableNormal);
        ImageView eventImg = eventView.findViewById(R.id.iv_event);
        TextView eventTitleEvent = eventView.findViewById(R.id.tv_event_title);
        TextView eventSubtitleEvent = eventView.findViewById(R.id.tv_event_subtitle);
        if (eventLayoutParams.height >= DensityUtil.dip2px(MyApplication.getInstance(), 20)) {
            eventTitleEvent.setVisibility(VISIBLE);
            eventTitleEvent.setText(event.getEventTitle());
            eventImg.setVisibility(VISIBLE);
            eventSubtitleEvent.setVisibility(VISIBLE);
            eventImg.setImageResource(event.getEventIconResId(false));
            String subTitle = event.getShowEventSubTitle(getContext(), selectCalendar);
            if (event.getEventType().equals(Schedule.TYPE_TASK)) {
                subTitle += "截止";
            }
            eventSubtitleEvent.setText(subTitle);
        }
        if (event.getEventType().equals(Schedule.TYPE_CALENDAR)) {
            eventSubtitleEvent.setVisibility(GONE);
        }
        eventLayout.addView(eventView, eventLayoutParams);
        eventView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(onEventClickListener != null && onEventClickListener.onRemoveEventAddDragScaleView())) {
                    showEventDetailPop(view, event, drawableNormal, eventLayoutParams.leftMargin);
                }
            }
        });
        eventView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onEventClickListener != null && event.canModify()) {
                    onEventClickListener.onEventTimeUpdate(event, eventLayoutParams.topMargin, eventLayoutParams.height);
                }
                return true;
            }
        });
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog(final Event event, final PopupWindow popupWindow) {
        new CustomDialog.MessageDialogBuilder(getContext())
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
                        if (onEventClickListener != null) {
                            onEventClickListener.onEventDelete(event);
                        }
                        dialog.dismiss();
                        popupWindow.dismiss();
                    }
                })
                .show();
    }

    private void showEventDetailPop(final View view, final Event event, final Drawable drawableNormal, int marginLeft) {
        CustomRoundButtonDrawable drawableSelected = event.getEventBgSelectDrawable();
        final TextView eventTitleText = view.findViewById(R.id.tv_event_title);
        final TextView eventSubtitleText = view.findViewById(R.id.tv_event_subtitle);
        final ImageView eventImg = view.findViewById(R.id.iv_event);
        eventImg.setImageResource(event.getEventIconResId(true));
        eventTitleText.setTextColor(Color.parseColor("#ffffff"));
        eventSubtitleText.setTextColor(Color.parseColor("#ffffff"));
        int popViewGap = DensityUtil.dip2px(10);
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.schedule_pop_calendarview_event_detail, null);
        contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        BubbleLayout bubbleLayout = contentView.findViewById(R.id.bubble_layout);
        bubbleLayout.setArrowPosition(marginLeft + view.getWidth() / 2 - DensityUtil.dip2px(22));
        final PopupWindow popupWindow = new PopupWindow(contentView,
                eventLayout.getWidth() - DensityUtil.dip2px(30),
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        ImageView shareImage = contentView.findViewById(R.id.iv_share);
        ImageView deleteImage = contentView.findViewById(R.id.iv_delete);
        ImageView groupChatImage = contentView.findViewById(R.id.iv_group_chat);
        contentView.findViewById(R.id.tv_detail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventClickListener != null) {
                    onEventClickListener.onShowEventDetail(event);
                }
                popupWindow.dismiss();
            }
        });
        //删除
        if (event.canDelete()) {
            deleteImage.setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.iv_delete).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmClearDialog(event, popupWindow);
                }
            });
        } else {
            deleteImage.setVisibility(View.GONE);
        }
        //发起群聊
        if (event.getEventType().equals(Schedule.TYPE_MEETING)) {
            groupChatImage.setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.iv_group_chat).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onEventClickListener != null) {
                        onEventClickListener.onGroupChat(event);
                    }
                    popupWindow.dismiss();
                }
            });
        } else {
            groupChatImage.setVisibility(View.GONE);
        }
        //V0环境不显示分享按钮
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            shareImage.setVisibility(View.GONE);
        }
        shareImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventShare(event);
                }
                popupWindow.dismiss();

            }
        });
        TextView popCalendarNameText = contentView.findViewById(R.id.tv_calendar_name);
        ImageView popCalendarTypeImg = contentView.findViewById(R.id.iv_calendar_type);
        TextView popEventTitleText = contentView.findViewById(R.id.tv_event_title);
        TextView popEventTimeText = contentView.findViewById(R.id.tv_event_time);
        ImageView popEventTitleImg = contentView.findViewById(R.id.iv_event_title);
        popEventTitleImg.setImageResource(event.getEventIconResId(false));
        popCalendarNameText.setText(CalendarUtils.getCalendarName((Schedule) event.getEventObj()));
        popCalendarNameText.setTextColor(event.getCalendarTypeColor());
        int resId = CalendarUtils.getCalendarIconResId((Schedule) event.getEventObj());
        if (resId != -1) {
            popCalendarTypeImg.setImageResource(resId);
        }
        popEventTitleText.setText(event.getEventTitle());
        String date = TimeUtils.calendar2FormatString(getContext(), selectCalendar, TimeUtils.FORMAT_MONTH_DAY);
        String week = TimeUtils.getWeekDay(getContext(), selectCalendar);
        String startTime = TimeUtils.calendar2FormatString(getContext(), event.getDayEventStartTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
        String endTime = TimeUtils.calendar2FormatString(getContext(), event.getDayEventEndTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
        popEventTimeText.setText(date + " " + week + " " + startTime + " - " + endTime);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                eventImg.setImageResource(event.getEventIconResId(false));
                view.setBackground(drawableNormal);
                eventTitleText.setTextColor(Color.parseColor("#333333"));
                eventSubtitleText.setTextColor(Color.parseColor("#333333"));
            }
        });
        int mDeviceHeight = ResolutionUtils.getHeight(getContext());
        Rect location = locateView(view);
        if (location != null) {
            //view中心点Y坐标
            int yMiddle = location.top + view.getHeight() / 2;
            if (yMiddle > mDeviceHeight / 2) {
                bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                popupWindow.showAtLocation(eventLayout, Gravity.NO_GRAVITY, (int) eventLayout.getX() + DensityUtil.dip2px(15), location.top - popViewGap - popupWindow.getContentView().getMeasuredHeight());
            } else {
                popupWindow.showAtLocation(eventLayout, Gravity.NO_GRAVITY, (int) eventLayout.getX() + DensityUtil.dip2px(15), location.bottom + popViewGap);
            }

        }
        view.setBackground(drawableSelected);
    }

    public Rect locateView(View v) {
        if (v == null) return null;
        int[] loc_int = new int[2];
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }


    public void setOnEventClickListener(ScheduleEventListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

}