package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Meeting;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.ScheduleBaseFragment;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.util.common.LunarUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalEventNotificationUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.widget.calendardayview.Event;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;
import com.inspur.emmcloud.widget.calendarview.EmmCalendar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

public class ScheduleFragment extends ScheduleBaseFragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener,View.OnClickListener {
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private CalendarView calendarView;
    private CalendarLayout calendarLayout;
    private TextView scheduleDataText;
    private ImageView calendarViewExpandImg;
    private WorkAPIService apiService;
    private List<String> calendarIdList = new ArrayList<>();
    private CalendarDayView calendarDayView;
    private BroadcastReceiver meetingAndTaskReceiver;
    private java.util.Calendar selectCalendar;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Task> taskList = new ArrayList<>();
    private List<CalendarEvent> calendarEventList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private TextView scheduleSumText;
    private Boolean isEventShowTypeList;
    private ScrollView eventScrollView;
    private RecyclerView eventRecyclerView;
    private ScheduleEventListAdapter scheduleEventListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        initView();
        registerWorkNotifyReceiver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_schedule;
    }

    /**
     * 注册刷新任务和会议的广播
     */
    private void registerWorkNotifyReceiver() {
        meetingAndTaskReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("refreshTask")) {
                    getTasks();
                } else if (intent.hasExtra("refreshMeeting")) {
                    getMeetings();
                } else if (intent.hasExtra("refreshCalendar")) {
                    getMyCalendar();
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constant.ACTION_MEETING);
        myIntentFilter.addAction(Constant.ACTION_CALENDAR);
        myIntentFilter.addAction(Constant.ACTION_TASK);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(meetingAndTaskReceiver, myIntentFilter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                getMyCalendar();
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_DATA_CHANGED:
                getMeetings();
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED:
                getTasks();
                break;
        }
    }

    private void initView() {
        calendarView = rootView.findViewById(R.id.calendar_view_schedule);
        calendarLayout = rootView.findViewById(R.id.calendar_layout_schedule);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarView.setOnYearChangeListener(this);
        scheduleDataText = rootView.findViewById(R.id.tv_schedule_date);
        calendarViewExpandImg = rootView.findViewById(R.id.iv_calendar_view_expand);
        calendarViewExpandImg.setOnClickListener(this);
        calendarDayView = rootView.findViewById(R.id.calendar_day_view);
        scheduleSumText = rootView.findViewById(R.id.tv_schedule_sum);
        calendarDayView.setOnEventClickListener(new CalendarDayView.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
            }
        });
        isEventShowTypeList = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE
                , CalendarSettingActivity.SHOW_TYPE_DAY_VIEW).equals(CalendarSettingActivity.SHOW_TYPE_LIST);
        eventRecyclerView = rootView.findViewById(R.id.recycler_view_event);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventScrollView = rootView.findViewById(R.id.scroll_view_event);
        eventRecyclerView.setVisibility(isEventShowTypeList?View.VISIBLE:View.GONE);
        scheduleEventListAdapter = new ScheduleEventListAdapter(getActivity());
        eventRecyclerView.setAdapter(scheduleEventListAdapter);
        eventScrollView.setVisibility(isEventShowTypeList?View.GONE:View.VISIBLE);
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

        selectCalendar = java.util.Calendar.getInstance();
        selectCalendar.set(calendar.getYear(),calendar.getMonth()-1,calendar.getDay(),0,0,0);
        selectCalendar.set(java.util.Calendar.MILLISECOND,0);
        setSelectCalendarTimeInfo();
        showCalendarEvent();
        getMeetings();
        getMyCalendar();
        getTasks();
    }

    private void setSelectCalendarTimeInfo() {
        StringBuilder builder = new StringBuilder();
        boolean isToday = TimeUtils.isCalendarToday(selectCalendar);
        calendarDayView.setCurrentTimeLineShow(isToday);
        if (isToday){
            builder.append(getString(R.string.today) + " ");
        }
        builder.append(LunarUtil.oneDay(selectCalendar.get(java.util.Calendar.YEAR),selectCalendar.get(java.util.Calendar.MONTH)+1,selectCalendar.get(java.util.Calendar.DAY_OF_MONTH)));
        builder.append(" ");
        builder.append(TimeUtils.getWeekDay(MyApplication.getInstance(),selectCalendar));
        scheduleDataText.setText(builder.toString());
    }

    @Override
    public void isExpand(boolean isExpand) {
        calendarViewExpandImg.setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
    }


    private void showCalendarEvent(){
        eventList.clear();
        eventList.addAll(Meeting.MeetingList2EventList(meetingList,selectCalendar));
        eventList.addAll(Task.taskList2EventList(taskList,selectCalendar));
        eventList.addAll(CalendarEvent.calendarEvent2EventList(calendarEventList,selectCalendar));
        if (isEventShowTypeList){
            scheduleEventListAdapter.setEventList(selectCalendar,eventList);
            scheduleEventListAdapter.notifyDataSetChanged();
        }else {
            calendarDayView.setEventList(eventList,selectCalendar);
        }
        int eventListSize = eventList.size();
        scheduleSumText.setText(eventListSize>0?eventListSize+"项日程":"");
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
        if (meetingAndTaskReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(meetingAndTaskReceiver);
            meetingAndTaskReceiver = null;
        }
    }


    /**
     * 获取会议
     */
    private void getMeetings() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            apiService.getMeetings(7);
        }
    }

    /**
     * 获取日历中Event
     */
    private void getMyCalendar() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            apiService.getMyCalendar(0, 30);
        }
    }

    /**
     * 获取任务
     */
    private void getTasks() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            String orderBy = PreferencesUtils.getString(getActivity(),
                    "order_by", "PRIORITY");
            String orderType = PreferencesUtils.getString(getActivity(),
                    "order_type", "DESC");
            apiService.getRecentTasks(orderBy, orderType);
        }
    }



    /**
     * 获取今明两天所有日历的所有event
     */
    private void getCalEventsForTwoDays() {
        if (calendarIdList.size() > 0) {
            if (NetUtils.isNetworkConnected(getActivity())) {
                java.util.Calendar afterCalendar = java.util.Calendar.getInstance();
                java.util.Calendar beforeCalendar = java.util.Calendar.getInstance();
                beforeCalendar.set(beforeCalendar.get(java.util.Calendar.YEAR),
                        beforeCalendar.get(java.util.Calendar.MONTH),
                        beforeCalendar.get(java.util.Calendar.DAY_OF_MONTH) + 2, 0, 0, 0);
                afterCalendar.set(afterCalendar.get(java.util.Calendar.YEAR),
                        afterCalendar.get(java.util.Calendar.MONTH),
                        afterCalendar.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0);
                afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
                beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
                apiService.getAllCalEvents(calendarIdList, afterCalendar,
                        beforeCalendar, 5, 0, true);
            }
        } else {
            calendarEventList.clear();
        }

    }


//    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {
//        private String type;
//
//        public ListOnItemClickListener(String type) {
//            this.type = type;
//        }
//
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Bundle bundle = new Bundle();
//            if (type.equals(TYPE_CALENDAR)) {
//                bundle.putSerializable("calEvent",
//                        calendarEventList.get(position));
//                IntentUtils.startActivity(getActivity(), CalEventAddActivity.class, bundle);
//                recordUserClickWorkFunction(TYPE_CALENDAR);
//            } else if (type.equals(TYPE_TASK)) {
//                bundle.putSerializable(TYPE_TASK,
//                        taskList.get(position));
//                IntentUtils.startActivity(getActivity(), MessionDetailActivity.class, bundle);
//                recordUserClickWorkFunction(TYPE_TASK);
//            } else if (type.equals(TYPE_MEETING)) {
//                Meeting meeting = meetingList.get(position);
//                bundle.putSerializable(TYPE_MEETING, meeting);
//                IntentUtils.startActivity(getActivity(),
//                        MeetingDetailActivity.class, bundle);
//                recordUserClickWorkFunction(TYPE_MEETING);
//            }
//        }
//
//    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
            meetingList = getMeetingsResult.getMeetingsList();
            showCalendarEvent();
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            taskList = getTaskListResult.getTaskList();
            showCalendarEvent();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
        }

        @Override
        public void returnMyCalendarSuccess(
                GetMyCalendarResult getMyCalendarResult) {
            List<MyCalendar> calendarList = getMyCalendarResult.getCalendarList();
            MyCalendarCacheUtils.saveMyCalendarList(getActivity(), calendarList);
            calendarIdList.clear();
            for (int i = 0; i < calendarList.size(); i++) {
                MyCalendar myCalendar = calendarList.get(i);
                if (myCalendar.getState() != null && !myCalendar.getState().equals("REMOVED")
                        && !MyCalendarOperationCacheUtils.getIsHide(
                        getActivity(), myCalendar.getId())) {
                    calendarIdList.add(calendarList.get(i).getId());
                }
            }
            getCalEventsForTwoDays();
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
        }

        @Override
        public void returnCalEventsSuccess(
                GetCalendarEventsResult getCalendarEventsResult,
                boolean isRefresh) {
            calendarEventList = getCalendarEventsResult.getCalEventList();
            CalEventNotificationUtils.setCalEventNotification(getActivity().getApplicationContext(), calendarEventList);

        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
        }
    }
}
