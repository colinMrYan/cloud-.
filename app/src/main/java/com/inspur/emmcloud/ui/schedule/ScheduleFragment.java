package com.inspur.emmcloud.ui.schedule;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleEventListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.util.common.LunarUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.MeetingCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;
import com.inspur.emmcloud.util.privates.cache.TaskCacheUtils;
import com.inspur.emmcloud.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.widget.calendardayview.Event;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;
import com.inspur.emmcloud.widget.calendarview.EmmCalendar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

@ContentView(R.layout.fragment_schedule)
public class ScheduleFragment extends ScheduleBaseFragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener, View.OnClickListener {
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";

    @ViewInject(R.id.calendar_view_schedule)
    private CalendarView calendarView;
    @ViewInject(R.id.calendar_layout_schedule)
    private CalendarLayout calendarLayout;
    @ViewInject(R.id.tv_schedule_date)
    private TextView scheduleDataText;
    @ViewInject(R.id.iv_calendar_view_expand)
    private ImageView calendarViewExpandImg;

    @ViewInject(R.id.calendar_day_view)
    private CalendarDayView calendarDayView;

    @ViewInject(R.id.tv_schedule_sum)
    private TextView scheduleSumText;
    @ViewInject(R.id.scroll_view_event)
    private ScrollView eventScrollView;
    @ViewInject(R.id.recycler_view_event)
    private RecyclerView eventRecyclerView;
    private ScheduleEventListAdapter scheduleEventListAdapter;
    private Boolean isEventShowTypeList;
    private WorkAPIService apiService;
    private Calendar selectCalendar;
    //    private List<Meeting> meetingList = new ArrayList<>();
//    private List<Task> taskList = new ArrayList<>();
//    private List<CalendarEvent> calendarEventList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private Calendar pageStartCalendar = Calendar.getInstance();
    private Calendar pageEndCalendar = Calendar.getInstance();
    private Calendar newDataStartCalendar = null;
    private Calendar newDataEndCalendar = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        pageStartCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
        pageEndCalendar = TimeUtils.getDayEndCalendar(Calendar.getInstance());
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                // getMyCalendar();
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_DATA_CHANGED:
                // getMeetings();
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED:
//                getTasks();
                break;
        }
    }

    private void initView() {
        calendarLayout.shrink(0);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarView.setOnYearChangeListener(this);
        calendarViewExpandImg.setOnClickListener(this);
        calendarDayView.setOnEventClickListener(new CalendarDayView.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
            }
        });
        isEventShowTypeList = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE
                , CalendarSettingActivity.SHOW_TYPE_DAY_VIEW).equals(CalendarSettingActivity.SHOW_TYPE_LIST);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventRecyclerView.setVisibility(isEventShowTypeList ? View.VISIBLE : View.GONE);
        scheduleEventListAdapter = new ScheduleEventListAdapter(getActivity());
        eventRecyclerView.setAdapter(scheduleEventListAdapter);
        eventScrollView.setVisibility(isEventShowTypeList ? View.GONE : View.VISIBLE);
        initData();
        calendarView.post(new Runnable() {
            @Override
            public void run() {
                calendarView.scrollToCurrent(true);
            }
        });

    }


    private void initData() {
        int year = calendarView.getCurYear();
        int month = calendarView.getCurMonth();
        Map<String, EmmCalendar> map = new HashMap<>();
        map.put(getSchemeCalendar(year, month, 3, 0xff36A5F6, "休").toString(),
                getSchemeCalendar(year, month, 3, 0xff36A5F6, "休"));
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map);
    }

    private EmmCalendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(year);
        emmCalendar.setMonth(month);
        emmCalendar.setDay(day);
        emmCalendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        emmCalendar.setScheme(text);
        emmCalendar.addScheme(new EmmCalendar.Scheme());
        emmCalendar.addScheme(0xFF36A5F6, "假");
        emmCalendar.addScheme(0xFF36A5F6, "节");
        return emmCalendar;
    }

    /**
     * 日历返回今天的接口
     */
    public void setScheduleBackToToday() {
        if (calendarView != null) {
            calendarView.scrollToCurrent();
        }
    }

    @Override
    public void onYearChange(int year) {

    }

    @Override
    public void onCalendarOutOfRange(EmmCalendar calendar) {

    }

    @Override
    public void onCalendarSelect(EmmCalendar calendar, boolean isClick) {
        selectCalendar = Calendar.getInstance();

        selectCalendar.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 0, 0, 0);
        selectCalendar.set(Calendar.MILLISECOND, 0);
        setSelectCalendarTimeInfo();

        List<EmmCalendar> currentPageCalendarList = calendarView.getCurrentPageCalendars();
        EmmCalendar startEmmCalendar = currentPageCalendarList.get(0);
        EmmCalendar endEmmCalendar = currentPageCalendarList.get(currentPageCalendarList.size() - 1);
        pageStartCalendar.set(startEmmCalendar.getYear(), startEmmCalendar.getMonth() - 1, startEmmCalendar.getDay());
        pageEndCalendar.set(endEmmCalendar.getYear(), endEmmCalendar.getMonth() - 1, endEmmCalendar.getDay());

        showCalendarEvent();
    }

    private void setSelectCalendarTimeInfo() {
        StringBuilder builder = new StringBuilder();
        boolean isToday = TimeUtils.isCalendarToday(selectCalendar);
        calendarDayView.setCurrentTimeLineShow(isToday);
        if (isToday) {
            builder.append(getString(R.string.today) + " ");
        }
        builder.append(LunarUtil.oneDay(selectCalendar.get(Calendar.YEAR), selectCalendar.get(Calendar.MONTH) + 1, selectCalendar.get(Calendar.DAY_OF_MONTH)));
        builder.append(" ");
        builder.append(TimeUtils.getWeekDay(MyApplication.getInstance(), selectCalendar));
        scheduleDataText.setText(builder.toString());
    }

    @Override
    public void isExpand(boolean isExpand) {
        if (isExpand){
            showCalendarEvent();
        }
        calendarViewExpandImg.setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
    }


    private void showCalendarEvent() {
        List<Schedule> scheduleList = ScheduleCacheUtils.getScheduleList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        List<Meeting> meetingList = MeetingCacheUtils.getMeetingList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        boolean isNeedGetDataFromNet = newDataStartCalendar == null || newDataEndCalendar == null || pageStartCalendar.before(newDataStartCalendar) || pageEndCalendar.after(newDataEndCalendar);
        if (isNeedGetDataFromNet) {
            long calendarLastTime = 0L;
            long meetingLastTime = 0L;
            long taskLastTime = 0L;
            if (scheduleList.size() > 0) {
                calendarLastTime = scheduleList.get(0).getLastTime();
            }
            if (meetingList.size() > 0) {
                meetingLastTime = meetingList.get(0).getLastTime();
            }
            getScheduleList(calendarLastTime, meetingLastTime, taskLastTime);
        }
        eventList.clear();
        eventList.addAll(Meeting.meetingEvent2EventList(meetingList, selectCalendar));
//        eventList.addAll(Task.taskList2EventList(taskList,selectCalendar));
        eventList.addAll(Schedule.calendarEvent2EventList(scheduleList, selectCalendar));
        if (isEventShowTypeList) {
            scheduleEventListAdapter.setEventList(selectCalendar, eventList);
            scheduleEventListAdapter.notifyDataSetChanged();
        } else {
            calendarDayView.setEventList(eventList, selectCalendar);
        }
        int eventListSize = eventList.size();
        scheduleSumText.setText(eventListSize > 0 ? eventListSize + "项日程" : "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_calendar_view_expand:
                calendarLayout.switchStatus();
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void getScheduleList(long calendarLastTime, long meetingLastTime, long taskLastTime) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getScheduleList(pageStartCalendar, pageEndCalendar, calendarLastTime, meetingLastTime, taskLastTime);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {
            super.returnDeviceCheckSuccess(getDeviceCheckResult);
        }

        @Override
        public void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar, Calendar endCalendar) {
            newDataStartCalendar = startCalendar;
            newDataEndCalendar = endCalendar;
            List<Schedule> scheduleList = getScheduleListResult.getScheduleList();
            List<Meeting> meetingList = getScheduleListResult.getMeetingList();
            List<Task> taskList = getScheduleListResult.getTaskList();
            ScheduleCacheUtils.saveScheduleList(MyApplication.getInstance(), scheduleList);
            MeetingCacheUtils.saveMeetingList(MyApplication.getInstance(), meetingList);
            TaskCacheUtils.saveTaskList(MyApplication.getInstance(), taskList);
            showCalendarEvent();
        }

        @Override
        public void returnScheduleListFail(String error, int errorCode) {
            super.returnScheduleListFail(error, errorCode);
        }

//        @Override
//        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
////            taskList = getTaskListResult.getTaskList();
//            showCalendarEvent(false);
//        }
//
//        @Override
//        public void returnRecentTasksFail(String error, int errorCode) {
//        }
    }
}
