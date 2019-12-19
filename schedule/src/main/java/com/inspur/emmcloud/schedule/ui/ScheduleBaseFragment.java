package com.inspur.emmcloud.schedule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseLayoutFragment;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.GetScheduleBasicDataResult;
import com.inspur.emmcloud.schedule.bean.calendar.Holiday;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.ui.meeting.ScheduleAddActivity;
import com.inspur.emmcloud.schedule.util.HolidayCacheUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;
import com.inspur.emmcloud.schedule.widget.DragScaleView;
import com.inspur.emmcloud.schedule.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.schedule.widget.calendardayview.Event;
import com.inspur.emmcloud.schedule.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.schedule.widget.calendarview.CalendarView;
import com.inspur.emmcloud.schedule.widget.calendarview.EmmCalendar;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by chenmch on 2019/7/12.
 */

public class ScheduleBaseFragment extends BaseLayoutFragment implements View.OnLongClickListener,
        CalendarView.OnCalendarSelectListener, CalendarLayout.CalendarExpandListener,
        DragScaleView.OnMoveListener, View.OnTouchListener {
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private static final int REQUEST_EXCHANGE_LOGIN = 1;
    protected Calendar pageStartCalendar = Calendar.getInstance();
    protected Calendar pageEndCalendar = Calendar.getInstance();
    protected Map<Integer, List<Holiday>> yearHolidayListMap = new HashMap<>();
    protected Calendar selectCalendar = Calendar.getInstance();
    protected CalendarView calendarView;
    protected CalendarLayout calendarLayout;
    protected TextView scheduleDataText;
    protected ImageView calendarViewExpandImg;
    protected CalendarDayView calendarDayView;
    protected RelativeLayout contentLayout;
    protected DragScaleView dragScaleView;
    protected ScrollView eventScrollView;
    private float contentLayoutTouchY = -1;
    private ScheduleAPIService apiService;
    private Event modifyEvent;
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate() {
        init();
        apiService = new ScheduleAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_schedule;
    }

    protected void init() {

    }

    /**
     * 同步一下当天的时间
     */
    public void updateCurrentDate() {
        if (calendarView != null) {
            Calendar newCalendar = Calendar.getInstance();
            if ((newCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) ||
                    (newCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) ||
                    (newCalendar.get(Calendar.DAY_OF_MONTH) != currentCalendar.get(Calendar.DAY_OF_MONTH))) {
                currentCalendar = newCalendar;
                calendarView.updateCurrentDate();
            }
        }

    }
    protected void showCalendarEvent(boolean isForceUpdate) {

    }

    /**
     * 日历返回今天的接口
     */
    public void setScheduleBackToToday() {
        if (calendarView != null) {
            updateCurrentDate();
            //calendarView.scrollToCurrent();存在bug，导致本地日期更改时mDelegate.mSelectedEmmCalendar也随之更改
            calendarView.scrollToCalendar(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH) + 1, currentCalendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    public Calendar getSelectCalendar() {
        return selectCalendar;
    }


    @Override
    public void onCalendarOutOfRange(EmmCalendar calendar) {

    }

    @Override
    public void isExpand(boolean isExpand) {
        if (isExpand) {
            onCalendarSelect(calendarView.getSelectedCalendar(), false);
        }
        calendarViewExpandImg.setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.calendar_day_view) {
            if (dragScaleView != null && dragScaleView.getVisibility() == View.VISIBLE) {
                removeEventAddDragScaleView();
            } else {
                showScheduleEventAddDragView(null, 0, 0);
            }

            return true;
        }
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.calendar_day_view) {
            contentLayoutTouchY = event.getY();
        }
        return false;
    }

    @Override
    public void moveTo(final boolean isNeedScroll, final int ScrollOffset, final int top, final int height) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (calendarDayView != null && dragScaleView != null) {
                    if (isNeedScroll) {
                        int scrollOffset = ScrollOffset;
                        int currentScrollY = eventScrollView.getScrollY();
                        eventScrollView.scrollBy(0, scrollOffset);
                        if (ScrollOffset < 0) {//向下滚动
                            if (currentScrollY < -ScrollOffset) {
                                scrollOffset = -currentScrollY;
                            }
                        } else {
                            int maxScrollOffset = eventScrollView.getChildAt(0).getHeight() - currentScrollY - eventScrollView.getHeight();
                            if (scrollOffset > maxScrollOffset) {
                                scrollOffset = maxScrollOffset;
                            }

                        }
                        //ScrollView滚动后，DragScaleView也会随之滚动，为了让DragScaleView能够跟随手势
                        dragScaleView.updateLastY(-scrollOffset);
                    }
                    calendarDayView.showDragViewTime(top, height);
                }
            }
        }, 10);
    }

    @Override
    public void onCalendarSelect(EmmCalendar calendar, boolean isClick) {
        removeEventAddDragScaleView();
        selectCalendar = Calendar.getInstance();
        selectCalendar.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 0, 0, 0);
        selectCalendar.set(Calendar.MILLISECOND, 0);
        setSelectCalendarTimeInfo();
        List<EmmCalendar> currentPageCalendarList = calendarView.getCurrentPageCalendars();
        if (currentPageCalendarList != null) {
            EmmCalendar startEmmCalendar = currentPageCalendarList.get(0);
            EmmCalendar endEmmCalendar = currentPageCalendarList.get(currentPageCalendarList.size() - 1);
            pageStartCalendar.set(startEmmCalendar.getYear(), startEmmCalendar.getMonth() - 1, startEmmCalendar.getDay());
            pageEndCalendar.set(endEmmCalendar.getYear(), endEmmCalendar.getMonth() - 1, endEmmCalendar.getDay());
            showCalendarEvent(false);
        }
    }


    /**
     * 显示选中时间
     */
    private void setSelectCalendarTimeInfo() {
        StringBuilder builder = new StringBuilder();
        boolean isToday = TimeUtils.isCalendarToday(selectCalendar);
        calendarDayView.setCurrentTimeLineShow(isToday);
        builder.append(TimeUtils.calendar2FormatString(BaseApplication.getInstance(), selectCalendar,
                TimeUtils.getFormat(BaseApplication.getInstance(),
                        Calendar.getInstance().get(Calendar.YEAR) == selectCalendar.get(Calendar.YEAR) ? TimeUtils.FORMAT_MONTH_DAY : TimeUtils.FORMAT_YEAR_MONTH_DAY)));
        builder.append(" ");
        builder.append(TimeUtils.getWeekDay(BaseApplication.getInstance(), selectCalendar));
        scheduleDataText.setText(builder.toString());
    }

    /**
     * 删除日历添加DragView
     */
    protected boolean removeEventAddDragScaleView() {
        if (dragScaleView != null && dragScaleView.getVisibility() == View.VISIBLE) {
            if (dragScaleView.isEventModify()) {
                updateEventTime();
            }
            contentLayout.removeView(dragScaleView);
            calendarDayView.hideDragViewTime();
            dragScaleView = null;
            return true;
        }
        this.modifyEvent = null;
        return false;
    }


    /**
     * 删除添加事件的View
     */
    protected void showScheduleEventAddDragView(Event event, int top, int dragScaleViewHeight) {
        this.modifyEvent = event;
        dragScaleView = new DragScaleView(getActivity());
        if (event == null) {
            dragScaleViewHeight = DensityUtil.dip2px(40) + 2 * dragScaleView.getOffset();
            int halfHourHeight = DensityUtil.dip2px(20);
            //保证DragScaleView显示在两个整点或两个半点之间
            top = (int) Math.round((contentLayoutTouchY - halfHourHeight) * 1.0 / halfHourHeight) * halfHourHeight;
        } else {
            dragScaleView.setContent(event.getEventTitle());
            dragScaleViewHeight += 2 * dragScaleView.getOffset();
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dragScaleViewHeight);
        params.setMargins(0, top, 0, 0);
        dragScaleView.setParentView(eventScrollView);
        contentLayout.addView(dragScaleView, params);
        calendarDayView.showDragViewTime(top, dragScaleViewHeight - 2 * dragScaleView.getOffset());
        dragScaleView.setOnMoveListener(this);
        dragScaleView.getParent().requestDisallowInterceptTouchEvent(true);
        dragScaleView.setFocusable(true);
        dragScaleView.setFocusableInTouchMode(true);
        dragScaleView.requestFocus();
        dragScaleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyEvent == null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_START_TIME, calendarDayView.getDragViewStartTime(selectCalendar));
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_END_TIME, calendarDayView.getDragViewEndTime(selectCalendar));
                    String scheduleCalendar = getExchangeScheduleCalendar();
                    bundle.putString(ScheduleAddActivity.EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE,
                            StringUtils.isBlank(scheduleCalendar) ? AccountType.APP_SCHEDULE.toString() : scheduleCalendar);
                    IntentUtils.startActivity(getActivity(), ScheduleAddActivity.class, bundle);
                    removeEventAddDragScaleView();
                }

            }
        });
        dragScaleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    private String getExchangeScheduleCalendar() {
        List<ScheduleCalendar> scheduleCalendars = ScheduleCalendarCacheUtils.getScheduleCalendarList(getActivity());
        for (int i = 0; i < scheduleCalendars.size(); i++) {
            if (scheduleCalendars.get(i).getAcType().equals(AccountType.EXCHANGE.toString())) {
                return scheduleCalendars.get(i).getId();
            }
        }
        return "";
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXCHANGE_LOGIN && resultCode == RESULT_OK) {
            showCalendarEvent(true);
        }
    }

    protected void getScheduleBasicData(int year) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            String version = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_BASIC_DATA_VERSION, "0");
            apiService.getScheduleBasicData(year, version);
        }
    }

    private void updateEventTime() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            Calendar startTime = calendarDayView.getDragViewStartTime(selectCalendar);
            Calendar endTime = calendarDayView.getDragViewEndTime(selectCalendar);
            Schedule schedule = (Schedule) modifyEvent.getEventObj();
            if (!startTime.equals(schedule.getStartTimeCalendar()) || !endTime.equals(schedule.getEndTimeCalendar())) {
                schedule.setStartTime(startTime.getTimeInMillis());
                schedule.setEndTime(endTime.getTimeInMillis());
                apiService.updateSchedule(schedule.toCalendarEventJSONObject().toString(), schedule);
            }
        }
    }

    class WebService extends ScheduleAPIInterfaceImpl {
        @Override
        public void returnScheduleBasicDataSuccess(GetScheduleBasicDataResult getScheduleBasicDataResult) {
            boolean isEnableExchangePrevious = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
            PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_BASIC_DATA_VERSION, getScheduleBasicDataResult.getVersion());
            if (getScheduleBasicDataResult.getCommand().equals("FORWARD")) {
                boolean isEnableExchange = getScheduleBasicDataResult.isEnableExchange();
                if (isEnableExchangePrevious != isEnableExchange) {
                    PreferencesByUserAndTanentUtils.putBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, getScheduleBasicDataResult.isEnableExchange());
                }
                List<Holiday> holidayList = getScheduleBasicDataResult.getHolidayList();
                int year = getScheduleBasicDataResult.getYear();
                    yearHolidayListMap.put(year, holidayList);
                if (pageStartCalendar.get(Calendar.YEAR) == year || pageEndCalendar.get(Calendar.YEAR) == year) {
                    showCalendarEvent(false);
                }
                HolidayCacheUtils.saveHolidayList(BaseApplication.getInstance(), getScheduleBasicDataResult.getYear(), holidayList);
            }
        }

        @Override
        public void returnScheduleBasicDataFail(String error, int errorCode) {
        }

        @Override
        public void returnUpdateScheduleSuccess() {
            showCalendarEvent(true);
        }

        @Override
        public void returnUpdateScheduleFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnUpdateMeetingSuccess() {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, null));
        }

        @Override
        public void returnUpdateMeetingFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }
    }
}
