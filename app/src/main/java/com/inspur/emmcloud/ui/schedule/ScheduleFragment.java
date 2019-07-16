package com.inspur.emmcloud.ui.schedule;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleAllDayEventListAdapter;
import com.inspur.emmcloud.adapter.ScheduleEventListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.DatePickerSpinnerDialog;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MaxHeightListView;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.Holiday;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.privates.CalendarUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ScheduleAlertUtils;
import com.inspur.emmcloud.util.privates.cache.HolidayCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MeetingCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;
import com.inspur.emmcloud.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.widget.calendardayview.Event;
import com.inspur.emmcloud.widget.calendarview.EmmCalendar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

public class ScheduleFragment extends ScheduleBaseFragment implements
        View.OnClickListener, CalendarDayView.OnEventClickListener,
        ScheduleEventListAdapter.OnItemClickLister,
        AdapterView.OnItemClickListener {
    private TextView scheduleSumText;
    private RecyclerView eventRecyclerView;
    private LinearLayout scheduleListDefaultLayout;
    private RelativeLayout allDayLayout;
    private RelativeLayout eventAllDayLayout;
    private ImageView eventAllDayImg;
    private TextView eventAllDayTitleText;
    private ScheduleEventListAdapter scheduleEventListAdapter;
    private Boolean isEventShowTypeList;
    private ScheduleApiService apiService;

    private List<Event> eventList = new ArrayList<>();
    private List<Event> allDayEventList = new ArrayList<>();
    private Calendar newDataStartCalendar = null;
    private Calendar newDataEndCalendar = null;
    private MyDialog myDialog = null;
    private Map<Integer, List<Holiday>> yearHolidayListMap = new HashMap<>();
    private LoadingDialog loadingDlg;

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        pageStartCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
        pageEndCalendar = TimeUtils.getDayEndCalendar(Calendar.getInstance());
        yearHolidayListMap = HolidayCacheUtils.getYearHolidayListMap(MyApplication.getInstance());
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
        loadingDlg = new LoadingDialog(getActivity());
        contentLayout = rootView.findViewById(R.id.rl_content);
        calendarView = rootView.findViewById(R.id.calendar_view_schedule);
        calendarLayout = rootView.findViewById(R.id.calendar_layout_schedule);
        scheduleDataText = rootView.findViewById(R.id.tv_schedule_date);
        calendarViewExpandImg = rootView.findViewById(R.id.iv_calendar_view_expand);
        calendarDayView = rootView.findViewById(R.id.calendar_day_view);
        scheduleSumText = rootView.findViewById(R.id.tv_schedule_sum);
        eventScrollView = rootView.findViewById(R.id.scroll_view_event);
        eventRecyclerView = rootView.findViewById(R.id.recycler_view_event);
        scheduleListDefaultLayout = rootView.findViewById(R.id.rl_schedule_list_default);
        allDayLayout = rootView.findViewById(R.id.rl_all_day);
        eventAllDayLayout = rootView.findViewById(R.id.rl_event_all_day);
        eventAllDayImg = rootView.findViewById(R.id.iv_event_all_day);
        eventAllDayTitleText = rootView.findViewById(R.id.tv_event_title_all_day);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarViewExpandImg.setOnClickListener(this);
        scheduleDataText.setOnClickListener(this);
        calendarDayView.setOnEventClickListener(this);
        allDayLayout.setOnClickListener(this);
        calendarLayout.post(new Runnable() {
            @Override
            public void run() {
                calendarLayout.shrink(0);
            }
        });
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
        switch (LanguageManager.getInstance().getCurrentAppLanguage()) {
            case "zh-Hans":
            case "zh-hant":
                calendarView.setIsLunarAndFestivalShow(true);
                break;
            default:
                calendarView.setIsLunarAndFestivalShow(false);
                break;
        }
        calendarDayView.setOnTouchListener(this);
        calendarDayView.setOnLongClickListener(this);
        calendarDayView.setOnClickListener(this);

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

    /**
     * 显示event事件
     *
     * @param isForceUpdate 是否强制刷新数据
     */
    protected void showCalendarEvent(boolean isForceUpdate) {
        List<Schedule> scheduleList = ScheduleCacheUtils.getScheduleList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        List<Meeting> meetingList = MeetingCacheUtils.getMeetingList(MyApplication.getInstance(), pageStartCalendar, pageEndCalendar);
        ScheduleAlertUtils.setScheduleListAlert(MyApplication.getInstance(), scheduleList);
        ScheduleAlertUtils.setMeetingListAlert(MyApplication.getInstance(), meetingList);
        //在非强制刷新情况下如果前一次日历的日期包含此次的日期则不用重新获取数据
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
        boolean isScheduleShow = !MyCalendarOperationCacheUtils.getIsHide(getContext(), "schedule");
        boolean isMeetingShow = !MyCalendarOperationCacheUtils.getIsHide(getContext(), "meeting");
        if (isMeetingShow) {
            eventList.addAll(Meeting.meetingEvent2EventList(meetingList, selectCalendar));
        }
        //  eventList.addAll(Task.taskList2EventList(taskList,selectCalendar));
        if (isScheduleShow) {
            eventList.addAll(Schedule.calendarEvent2EventList(scheduleList, selectCalendar));
        }
        showAllEventCalendarViewMark(scheduleList, meetingList, isScheduleShow, isMeetingShow);
        allDayLayout.setVisibility(View.GONE);
        int eventListSize = eventList.size();
        scheduleListDefaultLayout.setVisibility((isEventShowTypeList && eventListSize < 1) ? View.VISIBLE : View.GONE);
        scheduleSumText.setText(eventListSize > 0 ? eventListSize + " " + getActivity().getString(R.string.schedule_calendar_schedules) : "");
        if (isEventShowTypeList) {
            eventRecyclerView.setVisibility(eventList.size() > 0 ? View.VISIBLE : View.GONE);
            scheduleEventListAdapter.setEventList(selectCalendar, eventList);
            scheduleEventListAdapter.notifyDataSetChanged();
        } else {
            setAllDayEventList();
            if (allDayEventList.size() > 0) {
                Event event = allDayEventList.get(0);
                allDayLayout.setVisibility(View.VISIBLE);
                eventAllDayImg.setImageResource(event.getEventIconResId());
                eventAllDayLayout.setBackground(CalendarUtils.getEventBgNormalDrawable(event));
                String eventTitle = event.getEventTitle();
                if (allDayEventList.size() > 1) {
                    if (eventTitle.length() > 14) {
                        eventTitle = eventTitle.substring(0, 13);
                        eventTitle = eventTitle + "...";
                    }
                    eventTitle = eventTitle + getActivity().getString(R.string.schedule_calendar_so_on) + " " +
                            allDayEventList.size() + " " + getActivity().getString(R.string.schedule_calendar_schedules);
                }
                eventAllDayTitleText.setText(eventTitle);
            }
            calendarDayView.setEventList(eventList, selectCalendar);
            calendarDayView.post(new Runnable() {
                @Override
                public void run() {
                    eventScrollView.scrollTo(0, calendarDayView.getScrollOffset());
                }
            });
        }
    }

    private EmmCalendar getSchemeCalendar(int year, int month, int day, String holidayName, String holidayColor, String badge, String badgeColor, boolean isShowSchemePoint) {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(year);
        emmCalendar.setMonth(month);
        emmCalendar.setDay(day);
        if (!StringUtils.isBlank(holidayName)) {
            emmCalendar.setSchemeLunar(holidayName);
        }
        if (!StringUtils.isBlank(holidayColor)) {
            emmCalendar.setSchemeLunarColor(Color.parseColor(holidayColor));
        }
        if (!StringUtils.isEmpty(badge)) {
            emmCalendar.setScheme(badge);
        } else {
            emmCalendar.setScheme(" ");
        }
        if (!StringUtils.isBlank(badgeColor)) {
            emmCalendar.setSchemeColor(Color.parseColor(badgeColor));
        }
        emmCalendar.setShowSchemePoint(isShowSchemePoint);
        return emmCalendar;
    }

    /**
     * 日视图区分全天和非全天事件
     */
    private void setAllDayEventList() {
        allDayEventList.clear();
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            boolean isCrossSelectDay = !event.getEventStartTime().after(selectCalendar) && !event.getEventEndTime().before(TimeUtils.getDayEndCalendar(selectCalendar));
            if (event.isAllDay() || isCrossSelectDay) {
                allDayEventList.add(event);
                iterator.remove();
            }
        }

    }

    /**
     * 展示日历事件标志
     *
     * @param scheduleList
     * @param meetingList
     * @param isScheduleShow
     * @param isMeetingShow
     */
    private void showAllEventCalendarViewMark(List<Schedule> scheduleList, List<Meeting> meetingList, boolean isScheduleShow, boolean isMeetingShow) {
        calendarView.clearSchemeDate();
        Map<String, EmmCalendar> map = new HashMap<>();
        int startYear = pageStartCalendar.get(Calendar.YEAR);
        int endYear = pageEndCalendar.get(Calendar.YEAR);
        List<Holiday> holidayList = yearHolidayListMap.get(startYear);
        if (holidayList == null) {
            holidayList = new ArrayList<>();
            getScheduleBasicData(startYear);
        }
        if (startYear != endYear) {
            List<Holiday> endYearHolidayList = yearHolidayListMap.get(endYear);
            if (endYearHolidayList == null) {
                getScheduleBasicData(endYear);
            } else {
                holidayList.addAll(endYearHolidayList);
            }
        }
        for (Holiday holiday : holidayList) {
            EmmCalendar schemeCalendar = getSchemeCalendar(holiday.getYear(), holiday.getMonth(), holiday.getDay(), holiday.getName()
                    , holiday.getColor(), holiday.getBadge(), holiday.getBadgeColor(), false);
            map.put(schemeCalendar.toString(), schemeCalendar);
        }
        if (isScheduleShow) {
            for (Schedule schedule : scheduleList) {
                showScheduleEventCalendarViewMark(schedule.getStartTimeCalendar(), schedule.getEndTimeCalendar(), map);
            }
        }
        if (isMeetingShow) {
            for (Meeting meeting : meetingList) {
                showScheduleEventCalendarViewMark(meeting.getStartTimeCalendar(), meeting.getEndTimeCalendar(), map);
            }
        }

        calendarView.setSchemeDate(map);
    }

    private void showScheduleEventCalendarViewMark(Calendar startCalendar, Calendar endCalendar, Map<String, EmmCalendar> map) {
        for (Calendar calendar = TimeUtils.getDayBeginCalendar(startCalendar); calendar.before(endCalendar); calendar.add(Calendar.DAY_OF_YEAR, 1)) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            EmmCalendar emmCalendar = getSchemeCalendar(year, month, day, null, null, "", null, true);
            EmmCalendar existEmmCalendar = map.get(emmCalendar.toString());
            if (existEmmCalendar == null) {
                existEmmCalendar = emmCalendar;
            } else {
                existEmmCalendar.setShowSchemePoint(true);
            }
            map.put(existEmmCalendar.toString(), existEmmCalendar);
        }
    }

    /**
     * 显示说有的全天事件列表
     */
    private void showAllDayEventListDlg() {
        if (myDialog == null)
            myDialog = new MyDialog(getActivity(), R.layout.schedule_all_day_event_pop);
        MaxHeightListView listView = myDialog.findViewById(R.id.lv_all_day_event);
        listView.setMaxHeight(DensityUtil.dip2px(MyApplication.getInstance(), 300));
        listView.setAdapter(new ScheduleAllDayEventListAdapter(getActivity(), allDayEventList));
        myDialog.findViewById(R.id.iv_close).setOnClickListener(this);
        listView.setOnItemClickListener(this);
        myDialog.show();
    }

    /**
     * 弹出日期选择框
     */
    private void showDateSpinnerDlg() {
        DatePickerSpinnerDialog datePickerSpinnerDialog = new DatePickerSpinnerDialog(getActivity());
        datePickerSpinnerDialog.setDataTimePickerDialogListener(new DatePickerSpinnerDialog.DatePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                calendarView.scrollToCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        datePickerSpinnerDialog.showDatePickerDialog();
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
                if (allDayEventList.size() > 1) {
                    showAllDayEventListDlg();
                } else {
                    openEvent(allDayEventList.get(0));
                }

                break;
            case R.id.iv_close:
                myDialog.dismiss();
                myDialog = null;
                break;
            case R.id.calendar_day_view:
                removeEventAddDragScaleView();
                break;
            case R.id.tv_schedule_date:
                showDateSpinnerDlg();
                break;
        }
    }


    @Override
    public void onItemClick(View view, int position, Event event) {
        openEvent(event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openEvent(allDayEventList.get(position));
        if (myDialog != null)
            myDialog.dismiss();
    }

    @Override
    public void onShowEventDetail(Event event) {
        openEvent(event);
    }

    @Override
    public boolean onRemoveEventAddDragScaleView() {
        return removeEventAddDragScaleView();
    }

    @Override
    public void onEventDelete(Event event) {
        deleteScheduleEvent(event);
    }

    @Override
    public void onEventShare(Event event) {
        String startTime = TimeUtils.calendar2FormatString(MyApplication.getInstance(), event.getEventStartTime(), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
        String endTime = TimeUtils.calendar2FormatString(MyApplication.getInstance(), event.getEventEndTime(), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
        StringBuilder builder = new StringBuilder();
        builder.append(event.eventType.endsWith(Schedule.TYPE_CALENDAR) ? getString(R.string.schedule_title) : getString(R.string.schedule_meeting_topic));
        builder.append(" : ").append(event.getEventTitle()).append("\n")
                .append(getString(R.string.meeting_start_time)).append(" : ").append(startTime).append("\n")
                .append(getString(R.string.meeting_end_time)).append(" : ").append(endTime);
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.shareTxtPlainToConversation(builder.toString(), new ShareToConversationListener() {
                @Override
                public void shareSuccess(String cid) {
                    ToastUtils.show(R.string.baselib_share_success);
                }

                @Override
                public void shareFail() {
                    ToastUtils.show(R.string.baselib_share_fail);
                }

                @Override
                public void shareCancel() {

                }
            });
        }

    }

    @Override
    public void onGroupChat(Event event) {
        (new ChatCreateUtils()).startGroupChat(getActivity(), (Meeting) (event.getEventObj()), "", new ChatCreateUtils.ICreateGroupChatListener() {
            @Override
            public void createSuccess() {
            }

            @Override
            public void createFail() {
                ToastUtils.show(R.string.meeting_group_chat_fail);
            }
        });
    }

    @Override
    public void onEventTimeUpdate(Event event, int top, int height) {
        showScheduleEventAddDragView(event, top, height);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 获取日程、会议、任务信息
     *
     * @param calendarLastTime
     * @param meetingLastTime
     * @param taskLastTime
     * @param calendarIdList
     * @param meetingIdList
     * @param taskIdList
     */
    private void getScheduleList(long calendarLastTime, long meetingLastTime, long taskLastTime, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getScheduleList((Calendar) pageStartCalendar.clone(), (Calendar) pageEndCalendar.clone(),
                    calendarLastTime, meetingLastTime, taskLastTime, calendarIdList, meetingIdList, taskIdList);
        }
    }


    private void deleteScheduleEvent(Event event) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            if (event.getEventType().equals(Schedule.TYPE_CALENDAR)) {
                apiService.deleteSchedule(event.getEventId());
            } else {
                Meeting meeting = new Meeting();
                meeting.setId(event.getEventId());
                apiService.deleteMeeting(meeting);
            }

        }
    }


    class WebService extends APIInterfaceInstance {
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
//                if (getScheduleListResult.isTaskForward()) {
//                    List<Task> taskList = getScheduleListResult.getTaskList();
//                    //
//                    TaskCacheUtils.saveTaskList(MyApplication.getInstance(), taskList);
//                }
                if (startCalendar.equals(pageStartCalendar) && endCalendar.equals(pageEndCalendar)) {
                    showCalendarEvent(false);
                }

            }

        }


        @Override
        public void returnDeleteMeetingSuccess(Meeting meeting) {
            LoadingDialog.dimissDlg(loadingDlg);
            MeetingCacheUtils.removeMeeting(BaseApplication.getInstance(), meeting.getId());
            showCalendarEvent(true);
        }

        @Override
        public void returnDeleteMeetingFail(String error, int errorCode) {
            returnDeleteScheduleFail(error, errorCode);
        }

        @Override
        public void returnDeleteScheduleSuccess(String scheduleId) {
            LoadingDialog.dimissDlg(loadingDlg);
            ScheduleCacheUtils.removeSchedule(BaseApplication.getInstance(), scheduleId);
            showCalendarEvent(true);
        }

        @Override
        public void returnDeleteScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }


    }
}
