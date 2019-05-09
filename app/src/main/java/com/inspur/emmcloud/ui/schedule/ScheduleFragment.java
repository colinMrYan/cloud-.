package com.inspur.emmcloud.ui.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleAllDayEventListAdapter;
import com.inspur.emmcloud.adapter.ScheduleEventListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.Holiday;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResolutionUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ScheduleAlertUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.HolidayCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MeetingCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;
import com.inspur.emmcloud.util.privates.cache.TaskCacheUtils;
import com.inspur.emmcloud.widget.MaxHeightListView;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

@ContentView(R.layout.fragment_schedule)
public class ScheduleFragment extends ScheduleBaseFragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarLayout.CalendarExpandListener, View.OnClickListener, CalendarDayView.OnEventClickListener, ScheduleEventListAdapter.OnItemClickLister,AdapterView.OnItemClickListener {
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
    @ViewInject(R.id.rl_all_day)
    private RelativeLayout allDayLayout;
    @ViewInject(R.id.iv_event_all_day)
    private ImageView eventAllDayImg;
    @ViewInject(R.id.tv_event_title_all_day)
    private TextView eventAllDayTitleText;
    private ScheduleEventListAdapter scheduleEventListAdapter;
    private Boolean isEventShowTypeList;
    private ScheduleApiService apiService;
    private Calendar selectCalendar;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> allDayEventList = new ArrayList<>();
    private Calendar pageStartCalendar = Calendar.getInstance();
    private Calendar pageEndCalendar = Calendar.getInstance();
    private Calendar newDataStartCalendar = null;
    private Calendar newDataEndCalendar = null;
    private List<Holiday> holidayList= new ArrayList<>();
    private PopupWindow allDayEventPop;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        pageStartCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
        pageEndCalendar = TimeUtils.getDayEndCalendar(Calendar.getInstance());
        holidayList = HolidayCacheUtils.getHolidayList(MyApplication.getInstance());
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED:
                setEventShowType();
                showCalendarEvent(true);
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
            case Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED:
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED:
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
        allDayLayout.setOnClickListener(this);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduleEventListAdapter = new ScheduleEventListAdapter(getActivity());
        scheduleEventListAdapter.setOnItemClickLister(this);
        eventRecyclerView.setAdapter(scheduleEventListAdapter);
        setEventShowType();
        calendarView.post(new Runnable() {
            @Override
            public void run() {
                setScheduleBackToToday();
            }
        });
        switch (getLocaleByLanguage(getActivity()).getLanguage()){
            case "en":
                calendarView.setIsLunarAndFestivalShow(false);
                break;
            default:
                calendarView.setIsLunarAndFestivalShow(true);
                break;
        }

    }

    private Locale getLocaleByLanguage(Context context) {
        String languageJson = null;
        if (MyApplication.getInstance() == null || MyApplication.getInstance().getTanent() == null) {
            languageJson = PreferencesUtils.getString(context, Constant.PREF_LAST_LANGUAGE);

        } else {
            languageJson = PreferencesUtils
                    .getString(context, MyApplication.getInstance().getTanent()
                            + "appLanguageObj");
        }
        if (StringUtils.isBlank(languageJson)) {
            return Locale.getDefault();
        }
        String[] array = new Language(languageJson).getIso().split("-");
        String country = "";
        String variant = "";
        try {
            country = array[0];
            variant = array[1];
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return new Locale(country, variant);

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

    private EmmCalendar getSchemeCalendar(int year, int month, int day, String text, boolean isShowSchemePoint,boolean isDuty) {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(year);
        emmCalendar.setMonth(month);
        emmCalendar.setDay(day);

        if (!StringUtils.isBlank(text)){
            emmCalendar.setSchemeColor(isDuty?0xfff0906b:0xff36A5F6);
        }
        //如果单独标记颜色、则会使用这个颜色
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

    public Calendar getSelectCalendar(){
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
        builder.append(TimeUtils.calendar2FormatString(MyApplication.getInstance(),selectCalendar,TimeUtils.getFormat(MyApplication.getInstance(),TimeUtils.FORMAT_MONTH_DAY)));
        builder.append(" ");
        builder.append(TimeUtils.getWeekDay(MyApplication.getInstance(), selectCalendar));
        scheduleDataText.setText(builder.toString());
    }


    /**
     * 显示event事件
     * @param isForceUpdate  是否强制刷新数据
      */
    private void showCalendarEvent(boolean isForceUpdate) {
        List<Schedule> scheduleList = ScheduleCacheUtils.getScheduleList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        List<Meeting> meetingList = MeetingCacheUtils.getMeetingList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        ScheduleAlertUtils.setScheduleListAlert(MyApplication.getInstance(),scheduleList);
        ScheduleAlertUtils.setMeetingListAlert(MyApplication.getInstance(),meetingList);
        boolean isNeedGetDataFromNet = isForceUpdate || newDataStartCalendar == null || newDataEndCalendar == null || pageStartCalendar.before(newDataStartCalendar) || pageEndCalendar.after(newDataEndCalendar);
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
        allDayLayout.setVisibility(View.GONE);
        int eventListSize = eventList.size();
        scheduleSumText.setText(eventListSize > 0 ? eventListSize + "项日程" : "");
        if (isEventShowTypeList) {
            scheduleEventListAdapter.setEventList(selectCalendar, eventList);
            scheduleEventListAdapter.notifyDataSetChanged();
        } else {
            setAllDayEventList();
            if (allDayEventList.size()>0){
                Event event = allDayEventList.get(0);
                allDayLayout.setVisibility(View.VISIBLE);
                eventAllDayImg.setImageResource(event.getEventIconResId());
                String eventTitle = event.getEventTitle();
                if (allDayEventList.size()>1){
                    if (eventTitle.length()>14){
                        eventTitle = eventTitle.substring(0,13);
                        eventTitle = eventTitle+"...";
                    }
                    eventTitle = eventTitle+" 等"+allDayEventList.size()+"项日程";
                }
                eventAllDayTitleText.setText(eventTitle);
            }
            calendarDayView.setEventList(eventList, selectCalendar);
            calendarDayView.post(new Runnable() {
                @Override
                public void run() {
                    eventScrollView.scrollTo(0,calendarDayView.getScrollOffset());
                }
            });
        }
    }

    /**
     * 日视图区分全天和非全天事件
     */
    private void setAllDayEventList(){
        allDayEventList.clear();
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()){
            Event event = iterator.next();
            if (event.isAllDay()){
                allDayEventList.add(event);
                iterator.remove();
            }
        }

    }

    /**
     * 展示日历事件标志
     * @param scheduleList
     * @param meetingList
     */
    private void showCalendarViewEventMark(List<Schedule> scheduleList, List<Meeting> meetingList) {
        calendarView.clearSchemeDate();
        Map<String, EmmCalendar> map = new HashMap<>();
        for (Holiday holiday:holidayList){
            Calendar calendar = Calendar.getInstance();
            calendar.set(holiday.getYear(),holiday.getMonth()-1,holiday.getDay(),0,0,0);
            calendar.set(Calendar.MILLISECOND,0);
            if (calendar.before(pageEndCalendar) && !calendar.before(pageStartCalendar)){
                map.put(getSchemeCalendar(holiday.getYear(), holiday.getMonth(), holiday.getDay(), holiday.isDuty()?"班":"休", false,holiday.isDuty()).toString(),
                        getSchemeCalendar(holiday.getYear(), holiday.getMonth(), holiday.getDay(),holiday.isDuty()?"班":"休", false,holiday.isDuty()));
            }
        }

        for (Schedule schedule : scheduleList) {
            Calendar eventStartDayBeginCalendar = TimeUtils.getDayBeginCalendar(schedule.getStartTimeCalendar());
            for (Calendar calendar = eventStartDayBeginCalendar; calendar.before(schedule.getEndTimeCalendar()); calendar.add(Calendar.DAY_OF_YEAR, 1)) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                EmmCalendar emmCalendar = getSchemeCalendar(year, month, day, " ", true,false);
                EmmCalendar existEmmCalendar = map.get(emmCalendar.toString());
                if (existEmmCalendar == null){
                    existEmmCalendar = emmCalendar;
                }else {
                    existEmmCalendar.setShowSchemePoint(true);
                }

                map.put(existEmmCalendar.toString(),existEmmCalendar);
            }
        }
        for (Meeting meeting : meetingList) {
            Calendar eventStartDayBeginCalendar = TimeUtils.getDayBeginCalendar(meeting.getStartTimeCalendar());
            for (Calendar calendar = eventStartDayBeginCalendar; calendar.before(meeting.getEndTimeCalendar()); calendar.add(Calendar.DAY_OF_YEAR, 1)) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                EmmCalendar emmCalendar = getSchemeCalendar(year, month, day, " ", true,false);
                EmmCalendar existEmmCalendar = map.get(emmCalendar.toString());
                if (existEmmCalendar == null){
                    existEmmCalendar = emmCalendar;
                }else {
                    existEmmCalendar.setShowSchemePoint(true);
                }

                map.put(existEmmCalendar.toString(),existEmmCalendar);
            }
        }
        calendarView.setSchemeDate(map);
    }

    private void showAllDayEventListPop(View anchor){
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.schedule_all_day_event_pop, null);
        int width = ResolutionUtils.getWidth(MyApplication.getInstance());
        width = width-2* DensityUtil.dip2px(MyApplication.getInstance(),20);
        BubbleLayout bubbleLayout = contentView.findViewById(R.id.bubble_layout);
        MaxHeightListView listView = contentView.findViewById(R.id.lv_all_day_event);
        listView.setMaxHeight(DensityUtil.dip2px(MyApplication.getInstance(),150));
        listView.setAdapter(new ScheduleAllDayEventListAdapter(getActivity(),allDayEventList));
        contentView.findViewById(R.id.iv_close).setOnClickListener(this);
        bubbleLayout.setArrowPosition(width/2-DensityUtil.dip2px(MyApplication.getInstance(),7));
        allDayEventPop = new PopupWindow(contentView,width,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        allDayEventPop.setOutsideTouchable(false);
        allDayEventPop.setTouchable(true);
        allDayEventPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        allDayEventPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        listView.setOnItemClickListener(this);
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        allDayEventPop.showAsDropDown(anchor,DensityUtil.dip2px(MyApplication.getInstance(),20),0);
    }

    private void openEvent(Event event) {
        Bundle bundle = new Bundle();
        switch (event.getEventType()) {
            case Schedule.TYPE_MEETING:
                Meeting meeting = (Meeting) event.getEventObj();
                bundle.putSerializable(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
                IntentUtils.startActivity(getActivity(), MeetingDetailActivity.class, bundle);
                break;
            case Schedule.TYPE_CALENDAR:
                Schedule schedule = (Schedule) event.getEventObj();
                bundle.putSerializable(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_EVENT, schedule);
                IntentUtils.startActivity(getActivity(), CalendarAddActivity.class, bundle);
                break;
            case Schedule.TYPE_TASK:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_calendar_view_expand:
                calendarLayout.switchStatus();
                break;
            case R.id.rl_all_day:
                if (allDayEventList.size()>1){
                    showAllDayEventListPop(view);
                }else {
                    onEventClick(allDayEventList.get(0));
                }

                break;
            case R.id.iv_close:
                allDayEventPop.dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position, Event event) {
        onEventClick(event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onEventClick(allDayEventList.get(position));
        allDayEventPop.dismiss();
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
