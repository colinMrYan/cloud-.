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
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
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
        CalendarLayout.CalendarExpandListener, View.OnClickListener, CalendarDayView.OnEventClickListener, ScheduleEventListAdapter.OnItemClickLister {
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
    private ScheduleApiService apiService;
    private Calendar selectCalendar;
    private List<Event> eventList = new ArrayList<>();
    private Calendar pageStartCalendar = Calendar.getInstance();
    private Calendar pageEndCalendar = Calendar.getInstance();
    private Calendar newDataStartCalendar = null;
    private Calendar newDataEndCalendar = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        pageStartCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
        pageEndCalendar = TimeUtils.getDayEndCalendar(Calendar.getInstance());
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED:
                LogUtils.jasonDebug("EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED----------------");
                setEventShowType();
                showCalendarEvent(true);
                break;
        }
    }

    private void initView() {
        calendarLayout.shrink(0);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarViewExpandImg.setOnClickListener(this);
        calendarDayView.setOnEventClickListener(this);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduleEventListAdapter = new ScheduleEventListAdapter(getActivity());
        scheduleEventListAdapter.setOnItemClickLister(this);
        eventRecyclerView.setAdapter(scheduleEventListAdapter);
        setEventShowType();
        calendarView.post(new Runnable() {
            @Override
            public void run() {
                calendarView.scrollToCurrent(true);
            }
        });

    }

    /**
     * 设置事件展示样式-日视图和列表视图
     */
    private void setEventShowType() {
        isEventShowTypeList = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE
                , CalendarSettingActivity.SHOW_TYPE_DAY_VIEW).equals(CalendarSettingActivity.SHOW_TYPE_LIST);
        eventRecyclerView.setVisibility(isEventShowTypeList ? View.VISIBLE : View.GONE);
        eventScrollView.setVisibility(isEventShowTypeList ? View.GONE : View.VISIBLE);
    }

    private EmmCalendar getSchemeCalendar(int year, int month, int day, String text, boolean isShowSchemePoint) {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(year);
        emmCalendar.setMonth(month);
        emmCalendar.setDay(day);
        emmCalendar.setSchemeColor(0xff36A5F6);//如果单独标记颜色、则会使用这个颜色
        emmCalendar.setScheme(text);
        emmCalendar.setShowSchemePoint(isShowSchemePoint);
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
        showCalendarEvent(false);
    }

    /**
     * 显示选中时间
     */
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


    private void showCalendarEvent(boolean isForceUpdate) {
        List<Schedule> scheduleList = ScheduleCacheUtils.getScheduleList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        List<Meeting> meetingList = MeetingCacheUtils.getMeetingList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        boolean isNeedGetDataFromNet =isForceUpdate || newDataStartCalendar == null || newDataEndCalendar == null || pageStartCalendar.before(newDataStartCalendar) || pageEndCalendar.after(newDataEndCalendar);
        if (isNeedGetDataFromNet) {
            List<String> scheduleIdList = new ArrayList<>();
            List<String> meetingIdList = new ArrayList<>();
            List<String> taskIdList = new ArrayList<>();
            long calendarLastTime = 0L;
            long meetingLastTime = 0L;
            long taskLastTime = 0L;
            if (scheduleList.size() > 0) {
                for (Schedule schedule : scheduleList) {
                    scheduleIdList.add(schedule.getId());
                }
                calendarLastTime = scheduleList.get(0).getLastTime();
            }
            if (meetingList.size() > 0) {
                for (Meeting meeting : meetingList) {
                    meetingIdList.add(meeting.getId());
                }
                meetingLastTime = meetingList.get(0).getLastTime();
            }
            getScheduleList(calendarLastTime, meetingLastTime, taskLastTime, scheduleIdList, meetingIdList, taskIdList);
        }
        eventList.clear();
        eventList.addAll(Meeting.meetingEvent2EventList(meetingList, selectCalendar));
//        eventList.addAll(Task.taskList2EventList(taskList,selectCalendar));
        eventList.addAll(Schedule.calendarEvent2EventList(scheduleList, selectCalendar));
        showCalendarViewEventMark(scheduleList, meetingList);
        if (isEventShowTypeList) {
            scheduleEventListAdapter.setEventList(selectCalendar, eventList);
            scheduleEventListAdapter.notifyDataSetChanged();
        } else {
            calendarDayView.setEventList(eventList, selectCalendar);
        }
        int eventListSize = eventList.size();
        scheduleSumText.setText(eventListSize > 0 ? eventListSize + "项日程" : "");
    }

    private void showCalendarViewEventMark(List<Schedule> scheduleList, List<Meeting> meetingList) {
        calendarView.clearSchemeDate();
        Map<String, EmmCalendar> map = new HashMap<>();
        for (Schedule schedule : scheduleList) {
            Calendar eventStartDayBeginCalendar = TimeUtils.getDayBeginCalendar(schedule.getStartTimeCalendar());
            for (Calendar calendar = eventStartDayBeginCalendar; calendar.before(schedule.getEndTimeCalendar()); calendar.add(Calendar.DAY_OF_YEAR, 1)) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                map.put(getSchemeCalendar(year, month, day, " ", true).toString(),
                        getSchemeCalendar(year, month, day, " ", true));
            }
        }
        for (Meeting meeting : meetingList) {
            Calendar eventStartDayBeginCalendar = TimeUtils.getDayBeginCalendar(meeting.getStartTimeCalendar());
            for (Calendar calendar = eventStartDayBeginCalendar; calendar.before(meeting.getEndTimeCalendar()); calendar.add(Calendar.DAY_OF_YEAR, 1)) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                map.put(getSchemeCalendar(year, month, day, " ", true).toString(),
                        getSchemeCalendar(year, month, day, " ", true));
            }
        }

        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map);
    }

    private void openEvent(Event event) {
        Bundle bundle = new Bundle();
        switch (event.getEventType()) {
            case Event.TYPE_MEETING:
                Schedule meeting = (Schedule) event.getEventObj();
                bundle.putSerializable(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
                IntentUtils.startActivity(getActivity(), MeetingDetailActivity.class, bundle);
                break;
            case Event.TYPE_CALENDAR:
                Schedule schedule = (Schedule) event.getEventObj();
                bundle.putSerializable(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_EVENT, schedule);
                IntentUtils.startActivity(getActivity(), CalendarAddActivity.class, bundle);
                break;
            case Event.TYPE_TASK:
                break;
        }
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
    public void onItemClick(View view, int position, Event event) {
        onEventClick(event);
    }

    @Override
    public void onEventClick(Event event) {
        openEvent(event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void getScheduleList(long calendarLastTime, long meetingLastTime, long taskLastTime, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getScheduleList((Calendar) pageStartCalendar.clone(), (Calendar) pageEndCalendar.clone(),
                    calendarLastTime, meetingLastTime, taskLastTime, calendarIdList, meetingIdList, taskIdList);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {
            super.returnDeviceCheckSuccess(getDeviceCheckResult);
        }

        @Override
        public void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar,
                                              Calendar endCalendar, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList) {
            newDataStartCalendar = startCalendar;
            newDataEndCalendar = endCalendar;
            if (getScheduleListResult.isForward()) {
                if (getScheduleListResult.isScheduleForward()) {
                    List<Schedule> scheduleList = getScheduleListResult.getScheduleList();
                    ScheduleCacheUtils.removeScheduleList(MyApplication.getInstance(), calendarIdList);
                    ScheduleCacheUtils.saveScheduleList(MyApplication.getInstance(), scheduleList);
                }
                if (getScheduleListResult.isMeetingForward()) {
                    List<Meeting> meetingList = getScheduleListResult.getMeetingList();
                    MeetingCacheUtils.removeMeetingList(MyApplication.getInstance(), meetingIdList);
                    MeetingCacheUtils.saveMeetingList(MyApplication.getInstance(), meetingList);
                }
                if (getScheduleListResult.isTaskForward()) {
                    List<Task> taskList = getScheduleListResult.getTaskList();
                    //
                    TaskCacheUtils.saveTaskList(MyApplication.getInstance(), taskList);
                }
                showCalendarEvent(false);
            }

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
