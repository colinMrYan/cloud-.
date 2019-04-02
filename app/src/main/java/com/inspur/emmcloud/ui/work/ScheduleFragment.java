package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragment;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarUtil;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.widget.calendardayview.Event;
import com.inspur.emmcloud.widget.calendarview.Calendar;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;

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

public class ScheduleFragment extends BaseFragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener,View.OnClickListener {
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private CalendarView calendarView;
    private CalendarLayout calendarLayout;
    private View rootView;
    private TextView scheduleDataText;
    private ImageView calendarViewExpandImg;
    private WorkAPIService apiService;
    private ArrayList<Task> taskList = new ArrayList<>();
    private List<CalendarEvent> calEventList = new ArrayList<>();
    private List<String> calendarIdList = new ArrayList<>();
    private CalendarDayView calendarDayView;
    private BroadcastReceiver meetingAndTaskReceiver;
    private java.util.Calendar selectCalendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_schedule, null);
        initView();
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        EventBus.getDefault().register(this);
        registerWorkNotifyReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_schedule, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
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
        rootView.findViewById(R.id.iv_add).setOnClickListener(this);
        calendarViewExpandImg = rootView.findViewById(R.id.iv_calendar_view_expand);
        calendarViewExpandImg.setOnClickListener(this);
        calendarDayView = rootView.findViewById(R.id.calendar_day_view);
        calendarDayView.setOnEventClickListener(new CalendarDayView.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
            }
        });
        onCalendarSelect(java.util.Calendar.getInstance(),false);
        initData();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                calendarDayView.setEventList(getEventList());
//            }
//        }, 200);

    }


    private List<Event> getEventList(){
        List<Event> eventList = new ArrayList<>();
        java.util.Calendar eventStartCalendar =java.util.Calendar.getInstance();
        java.util.Calendar eventEndCalendar =java.util.Calendar.getInstance();
        eventStartCalendar.set(java.util.Calendar.HOUR_OF_DAY,8);
        eventStartCalendar.set(java.util.Calendar.MINUTE,30);
        eventEndCalendar.set(java.util.Calendar.HOUR_OF_DAY,8);
        eventEndCalendar.set(java.util.Calendar.MINUTE,35);
        Event event1 = new Event("1",Event.TYPE_TASK,"关于防范勒索病毒的紧急预警提醒及处理","23:55截止",eventStartCalendar,eventEndCalendar);
        eventList.add(event1);

       eventStartCalendar =java.util.Calendar.getInstance();
        eventEndCalendar =java.util.Calendar.getInstance();
        eventStartCalendar.set(java.util.Calendar.HOUR_OF_DAY,9);
        eventStartCalendar.set(java.util.Calendar.MINUTE,0);
        eventEndCalendar.set(java.util.Calendar.HOUR_OF_DAY,10);
        eventEndCalendar.set(java.util.Calendar.MINUTE,30);
        Event event2 = new Event("1",Event.TYPE_MEETING,"产品需求讨论","S06楼 N211",eventStartCalendar,eventEndCalendar);
        eventList.add(event2);

        eventStartCalendar =java.util.Calendar.getInstance();
        eventEndCalendar =java.util.Calendar.getInstance();
        eventStartCalendar.set(java.util.Calendar.HOUR_OF_DAY,11);
        eventStartCalendar.set(java.util.Calendar.MINUTE,30);
        eventEndCalendar.set(java.util.Calendar.HOUR_OF_DAY,13);
        eventEndCalendar.set(java.util.Calendar.MINUTE,00);
        Event event3 = new Event("3",Event.TYPE_CALENDAR,"运动化","",eventStartCalendar,eventEndCalendar);
        eventList.add(event3);
        return eventList;
    }

    private void initData() {
        int year = calendarView.getCurYear();
        int month = calendarView.getCurMonth();
        Map<String, Calendar> map = new HashMap<>();
        map.put(getSchemeCalendar(year, month, 3, 0xFF40db25, "假").toString(),
                getSchemeCalendar(year, month, 3, 0xFF40db25, "假"));
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map);
    }

    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        calendar.addScheme(new Calendar.Scheme());
        calendar.addScheme(0xFF008800, "假");
        calendar.addScheme(0xFF008800, "节");
        return calendar;
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
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        java.util.Calendar selectCalendar = java.util.Calendar.getInstance();
        selectCalendar.set(calendar.getYear(),calendar.getMonth(),calendar.getDay(),0,0,0);
        selectCalendar.set(java.util.Calendar.MILLISECOND,0);
        onCalendarSelect(selectCalendar,isClick);
    }

    /**
     * 选中日期
     * @param calendar
     * @param isClick
     */
    private void onCalendarSelect(java.util.Calendar calendar,boolean isClick){
        selectCalendar = calendar;
        setCalendarTime();
        getMeetings();
        getMyCalendar();
        getTasks();
//        calendarDayView.post(new Runnable() {
//            @Override
//            public void run() {
//                calendarDayView.setEventList(getEventList());
//            }
//        });
    }

    private void setCalendarTime() {
        String time = TimeUtils.calendar2FormatString(getActivity(), selectCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY_BY_DASH) + "·" +
                CalendarUtil.getWeekDay(selectCalendar);
        boolean isToday = TimeUtils.isCalendarToday(selectCalendar);
        if (isToday) {
            time = getString(R.string.today) + "·" + time;
            calendarDayView.setCurrentTimeLineShow(true);
        }else {
            calendarDayView.setCurrentTimeLineShow(false);
        }
        scheduleDataText.setText(time);
    }

    @Override
    public void isExpand(boolean isExpand) {
        calendarViewExpandImg.setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
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
            apiService.getMeetings(30);
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
            calEventList.clear();
//            calendarChildAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 获取三条Event
     */
    private void getCalEventsFor3() {
        if (NetUtils.isNetworkConnected(getActivity()) && calendarIdList.size() > 0) {
            java.util.Calendar afterCalendar = java.util.Calendar.getInstance();
            java.util.Calendar beforeCalendar = java.util.Calendar.getInstance();
            beforeCalendar.set(beforeCalendar.get(java.util.Calendar.YEAR) + 1,
                    beforeCalendar.get(java.util.Calendar.MONTH),
                    beforeCalendar.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar.set(afterCalendar.get(java.util.Calendar.YEAR),
                    afterCalendar.get(java.util.Calendar.MONTH),
                    afterCalendar.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
            beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
            apiService.getAllCalEvents(calendarIdList, afterCalendar,
                    beforeCalendar, 3, 0, false);
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
//                        calEventList.get(position));
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
//            meetingList = getMeetingsResult.getMeetingsList();
//            if (meetingChildAdapter != null) {
//                meetingChildAdapter.notifyDataSetChanged();
//            }
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
//            taskList = getTaskListResult.getTaskList();
//            if (taskChildAdapter != null) {
//                taskChildAdapter.notifyDataSetChanged();
//            }
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnMyCalendarSuccess(
                GetMyCalendarResult getMyCalendarResult) {
//            List<MyCalendar> calendarList = getMyCalendarResult
//                    .getCalendarList();
//            MyCalendarCacheUtils
//                    .saveMyCalendarList(getActivity(), calendarList);
//            calendarIdList.clear();
//            for (int i = 0; i < calendarList.size(); i++) {
//                MyCalendar myCalendar = calendarList.get(i);
//                if (myCalendar.getState() != null && !myCalendar.getState().equals("REMOVED")
//                        && !MyCalendarOperationCacheUtils.getIsHide(
//                        getActivity(), myCalendar.getId())) {
//                    calendarIdList.add(calendarList.get(i).getId());
//                }
//            }
//            getCalEventsForTwoDays();
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnCalEventsSuccess(
                GetCalendarEventsResult getCalendarEventsResult,
                boolean isRefresh) {
//            calEventList = getCalendarEventsResult.getCalEventList();
//            CalEventNotificationUtils.setCalEventNotification(getActivity().getApplicationContext(), calEventList);
//            if (isRefresh && (calEventList.size() < 3)) { // 获取今明两天的日历不足3条
//                getCalEventsFor3();
//            } else if (calendarChildAdapter != null) {
//                calendarChildAdapter.notifyDataSetChanged();
//            }

        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }
    }
}
