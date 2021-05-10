package com.inspur.emmcloud.schedule.widget.calendardayview;

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

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButtonDrawable;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.api.ScheduleEventListener;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.widget.bub.ArrowDirection;
import com.inspur.emmcloud.schedule.widget.bub.BubbleLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by chenmch on 2019/3/29.
 * 日视图Event排列原理（关键在于计算Event的宽度和X坐标，y坐标和高度很容易计算）
 * 1.将所有日程按开始时间进行排序
 * 2.将所有日程按是否有交集分组（每一组中的日程或直接有交集或同时与另外一个有交集）
 * 3.将每组日程分列（每一列中的日程不相交），
 * 4.通过列数计算出本组日程最大宽度 （总宽度/组中日程总列数 = 组中日程最大宽度）
 * 5.通过列的顺序决定Event的X坐标（列的顺序*组中日程最大宽度=x坐标）
 */

public class CalendarDayView extends RelativeLayout implements View.OnLongClickListener {
    private static final int TIME_HOUR_HEIGHT = DensityUtil.dip2px(BaseApplication.getInstance(), 40);
    private static final int EVENT_GAP = DensityUtil.dip2px(BaseApplication.getInstance(), 2);
    private String[] dayHourTimes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23"};
    private List<Event> eventList = new ArrayList<>();
    private RelativeLayout eventLayout;
    private ScheduleEventListener onEventClickListener;
    private RelativeLayout currentTimeLineLayout;
    private LinearLayout timeHourLayout;
    private Calendar selectCalendar;
    private TextView dragViewStartTmeText;
    private TextView dragViewEndTimeText;
    private int earliestEventOffset = -1;

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
            offset = (int) ((currentCalendar.get(Calendar.HOUR_OF_DAY) - 1 + currentCalendar.get(Calendar.MINUTE) / 60.0f) * TIME_HOUR_HEIGHT - DensityUtil.dip2px(BaseApplication.getInstance(), 3));
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
            int marginTop = (int) ((currentCalendar.get(Calendar.HOUR_OF_DAY) + currentCalendar.get(Calendar.MINUTE) / 60.0f) * TIME_HOUR_HEIGHT - DensityUtil.dip2px(BaseApplication.getInstance(), 3));
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
        List<List<Event>> eventCollisionGroupList = getEventGroupListByCollision();
        showEvent(eventCollisionGroupList);
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
     *
     * @return
     */
    private List<List<Event>> getEventGroupListByCollision() {
        List<List<Event>> eventCollisionGroupList = new ArrayList<>();
        for (Event event : eventList) {
            boolean isPlaced = false;
            outerLoop:
            for (List<Event> eventCollisionGroup : eventCollisionGroupList) {
                for (Event groupEvent : eventCollisionGroup) {
                    if (isEventsCollide(groupEvent, event)) {
                        eventCollisionGroup.add(event);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<Event> newGroup = new ArrayList<>();
                newGroup.add(event);
                eventCollisionGroupList.add(newGroup);
            }
        }
        return eventCollisionGroupList;
    }

    /**
     * 将所有有交集的Event，纵向分列（每列中Event纵向排列无交集）
     *
     * @param eventCollisionGroupList
     */
    private void showEvent(List<List<Event>> eventCollisionGroupList) {
        earliestEventOffset = -1;
        eventLayout.removeAllViews();
        for (List<Event> collisionGroup : eventCollisionGroupList) {
            List<List<Event>> columns = new ArrayList<>();
            columns.add(new ArrayList<Event>());
            for (Event event : collisionGroup) {
                boolean isPlaced = false;
                for (List<Event> column : columns) {
                    if (column.size() == 0) {
                        column.add(event);
                        isPlaced = true;
                        //只需要将Event和本列的最后一个Event判断是否有交集即可（因为是按时间降序排列的）
                        //只有无交集的两个Event才能成一列
                    } else if (!isEventsCollide(event, column.get(column.size() - 1))) {
                        column.add(event);
                        isPlaced = true;
                        break;
                    }
                }
                if (!isPlaced) {
                    List<Event> newColumn = new ArrayList<>();
                    newColumn.add(event);
                    columns.add(newColumn);
                }
            }

            int maxRowCount = columns.size();
            int eventWidth = (eventLayout.getWidth() - (maxRowCount - 1) * EVENT_GAP) / maxRowCount;
            for (int i = 0; i < columns.size(); i++) {
                List<Event> column = columns.get(i);
                for (Event event : column) {
                    int eventIndex = i;
                    int eventHeight = (int) (event.getDayDurationInMillSeconds(selectCalendar) * TIME_HOUR_HEIGHT / 3600000);
                    int marginLeft = (EVENT_GAP + eventWidth) * eventIndex;
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

        }
    }

    private boolean isEventsCollide(Event event1, Event event2) {
        long start1 = event1.getEventStartTime().getTimeInMillis();
        long end1 = event1.getEventEndTime().getTimeInMillis();
        long start2 = event2.getEventStartTime().getTimeInMillis();
        long end2 = event2.getEventEndTime().getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }

    private void setEventLayout(final Event event, final RelativeLayout.LayoutParams eventLayoutParams) {
        View eventView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_calendar_day_event_view, null);
        final Drawable drawableNormal = event.getEventBgNormalDrawable();
        eventView.setBackground(drawableNormal);
        ImageView eventImg = eventView.findViewById(R.id.iv_event);
        TextView eventTitleEvent = eventView.findViewById(R.id.tv_event_title);
        TextView eventSubtitleEvent = eventView.findViewById(R.id.tv_event_subtitle);
        if (eventLayoutParams.height >= DensityUtil.dip2px(BaseApplication.getInstance(), 20)) {
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
                .setMessage(event.getEventType().equals(Schedule.TYPE_MEETING) ? R.string.schedule_meeting_cancel_the_meeting : R.string.schedule_calendar_cancel_the_schedule)
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
        if (TimeUtils.isSameDay(event.getEventStartTime(), event.getEventEndTime())) {
            String date = TimeUtils.calendar2FormatString(getContext(), selectCalendar, TimeUtils.FORMAT_MONTH_DAY);
            String week = TimeUtils.getWeekDay(getContext(), selectCalendar);
            String startTime = TimeUtils.calendar2FormatString(getContext(), event.getDayEventStartTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
            String endTime = TimeUtils.calendar2FormatString(getContext(), event.getDayEventEndTime(selectCalendar), TimeUtils.FORMAT_HOUR_MINUTE);
            popEventTimeText.setText(date + " " + week + " " + startTime + " - " + endTime);
        } else {
            String startTime = TimeUtils.calendar2FormatString(getContext(), event.getEventStartTime(), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
            String endTime = TimeUtils.calendar2FormatString(getContext(), event.getEventEndTime(), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
            popEventTimeText.setText(startTime + " - " + endTime);
        }


        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                eventImg.setImageResource(event.getEventIconResId(false));
                view.setBackground(drawableNormal);
                eventTitleText.setTextColor(DarkUtil.getTextE1TO33());
                eventSubtitleText.setTextColor(DarkUtil.getTextE1TO33());
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