package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.bean.FestivalDate;
import com.inspur.emmcloud.bean.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.GetMeetingsResult;
import com.inspur.emmcloud.bean.GetMyCalendarResult;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.MyCalendar;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.bean.WorkSetting;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingBookingActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionDetailActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.CalendarUtil;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.FestivalCacheUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.WorkColorUtils;
import com.inspur.emmcloud.util.WorkSettingCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.inspur.emmcloud.util.TimeUtils.FORMAT_MONTH_DAY;

/**
 * 工作页面
 *
 * @author Administrator
 */
public class WorkFragment extends Fragment implements OnRefreshListener {

    private static final String TYPE_CALENDAR = "calendar";
    private static final String TYPE_APPROVAL = "approval";
    private static final String TYPE_MEETING = "meeting";
    private static final String TYPE_TASK = "task";
    private static final int WORK_SETTING = 1;
    private View rootView;
    private WorkAPIService apiService;
    private PullableListView listView;
    private BaseAdapter adapter;
    private PullToRefreshLayout pullToRefreshLayout;
    private List<Meeting> meetingList = new ArrayList<>();
    private ArrayList<TaskResult> taskList = new ArrayList<>();
    private List<CalendarEvent> calEventList = new ArrayList<>();
    private BroadcastReceiver calEventReceiver;
    private BroadcastReceiver meetingAndTaskReceiver;
    private List<String> calendarIdList = new ArrayList<>();
    private ChildAdapter calendarChildAdapter, meetingChildAdapter, taskChildAdapter;
    private List<WorkSetting> workSettingList =new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_work, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_work, null);
        getWorkSettingData();
        initViews();
        getWorkData();
        registerWorkNotifyReceiver();
    }

    /**
     * 当工作页面配置发生改变后进行数据和layout的刷新
     */
    private void refreshWorkLayout(){
        setHeadLayout();
        getWorkSettingData();
        adapter.notifyDataSetChanged();
        getWorkData();
    }

    private void getWorkSettingData(){
       List<WorkSetting> allWorkSettingList = WorkSettingCacheUtils.getAllWorkSettingList(getActivity());
        if (allWorkSettingList.size() == 0){
            workSettingList.add(new WorkSetting(TYPE_MEETING,getString(R.string.meeting),true,0));
            workSettingList.add(new WorkSetting(TYPE_CALENDAR,getString(R.string.work_calendar_text),true,1));
            workSettingList.add(new WorkSetting(TYPE_TASK,getString(R.string.work_task_text),true,2));
            WorkSettingCacheUtils.saveWorkSettingList(getActivity(),workSettingList);
        }else {
            workSettingList = WorkSettingCacheUtils.getOpenWorkSettingList(getActivity());
        }
    }

    /**
     * 初始化views
     */
    private void initViews() {
        setHeadLayout();
        listView = (PullableListView) rootView
                .findViewById(R.id.list);
        pullToRefreshLayout = (PullToRefreshLayout) rootView
                .findViewById(R.id.refresh_view);
        pullToRefreshLayout.setInDarkBackgroud(true);
        pullToRefreshLayout.setOnRefreshListener(WorkFragment.this);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        adapter = new Adapter();
        listView.setAdapter(adapter);
        (rootView.findViewById(R.id.work_config_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),WorkSettingActivity.class);
                startActivityForResult(intent,WORK_SETTING);
            }
        });
        (rootView.findViewById(R.id.work_config_img2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),WorkSettingActivity.class);
                startActivityForResult(intent,WORK_SETTING);
            }
        });
    }

    private void setHeadLayout(){
        boolean isShowDate = PreferencesByUserAndTanentUtils.getBoolean(getActivity(), "work_open_info", true);
        (rootView.findViewById(R.id.work_header_layout)).setVisibility(isShowDate?View.GONE:View.VISIBLE);
        (rootView.findViewById(R.id.calendar_layout)).setVisibility(isShowDate?View.VISIBLE:View.GONE);
    }

    /**
     * 获取数据
     */
    private void getWorkData() {
        getMeetings();
        getMyCalendar();
        getTasks();
        handHeaderDate();
    }

    /**
     * 判断工作中是否开启此卡片
     * @param type
     * @return
     */
    private boolean isContainWork(String type){
        return  (workSettingList.size()>0 && workSettingList.contains(new WorkSetting(type,"",true,0)));
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
                }else if(intent.hasExtra("refreshCalendar")){
                    getMyCalendar();
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("com.inspur.meeting");
        myIntentFilter.addAction("com.inspur.task");
        getActivity().registerReceiver(meetingAndTaskReceiver, myIntentFilter);
    }

    static class ViewHolder {
        ImageView groupIconImg;
        TextView groupTitleText,workAddText;
        RelativeLayout groupHeaderlayout;
        ScrollViewWithListView GroupListView;
        RelativeLayout wordAddLayout;
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return workSettingList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (position<getCount()-1){}
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.work_card_group_item_view_vertical, null);
                holder.groupIconImg = (ImageView) convertView.findViewById(R.id.group_icon_img);
                holder.groupTitleText = (TextView) convertView.findViewById(R.id.group_title_text);
                holder.groupHeaderlayout = (RelativeLayout) convertView.findViewById(R.id.group_header_layout);
                holder.GroupListView = (ScrollViewWithListView) convertView.findViewById(R.id.list);
                holder.wordAddLayout=(RelativeLayout)convertView.findViewById(R.id.work_add_layout);
                holder.workAddText = (TextView)convertView.findViewById(R.id.work_add_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convertView.findViewById(R.id.blank_view).setVisibility((position==getCount()-1)?View.VISIBLE:View.GONE);
            WorkSetting workSetting = workSettingList.get(position);
            final String id = workSetting.getId();
            holder.groupHeaderlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (id.equals(TYPE_CALENDAR)) {
                        IntentUtils.startActivity(getActivity(), CalActivity.class);
                    } else if (id.equals(TYPE_MEETING)) {
                        IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    } else if (id.equals(TYPE_TASK)) {
                        IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    }
                }
            });
            holder.wordAddLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (id.equals(TYPE_CALENDAR)) {
                        IntentUtils.startActivity(getActivity(), CalEventAddActivity.class);
                    } else if (id.equals(TYPE_MEETING)) {
                        IntentUtils.startActivity(getActivity(), MeetingBookingActivity.class);
                    } else if (id.equals(TYPE_TASK)) {
                        IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    }
                }
            });

            if (id.equals(TYPE_CALENDAR)) {
                holder.workAddText.setText(R.string.add_calendar);
                holder.groupIconImg.setImageResource(R.drawable.ic_work_calendar);
                calendarChildAdapter = new ChildAdapter(TYPE_CALENDAR);
                holder.GroupListView.setAdapter(calendarChildAdapter);
                holder.groupTitleText.setText(R.string.work_calendar_text);
            } else if (id.equals(TYPE_MEETING)) {
                holder.workAddText.setText(R.string.meeting);
                holder.groupIconImg.setImageResource(R.drawable.ic_work_meeting);
                meetingChildAdapter = new ChildAdapter(TYPE_MEETING);
                holder.GroupListView.setAdapter(meetingChildAdapter);
                holder.groupTitleText.setText(R.string.meeting);
            } else {
                holder.workAddText.setText(R.string.add_mession);
                holder.groupIconImg.setImageResource(R.drawable.ic_work_task);
                taskChildAdapter = new ChildAdapter(TYPE_TASK);
                holder.GroupListView.setAdapter(taskChildAdapter);
                holder.groupTitleText.setText(R.string.work_task_text);
            }
            holder.GroupListView.setOnItemClickListener(new ListOnItemClickListener(id));
            return convertView;
        }
    }

    private class ChildAdapter extends BaseAdapter {
        private String type;

        public ChildAdapter(String type) {
            this.type = type;
        }

        @Override
        public int getCount() {
            if (type.equals(TYPE_CALENDAR)) {
                return calEventList.size();
            }
            if (type.equals(TYPE_MEETING)) {
                return meetingList.size();
            }
            return taskList.size()<5?taskList.size():5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.work_card_child_item_view_vertical, null);
            TextView countDownText = (TextView) convertView.findViewById(R.id.count_down_text);
            TextView dateText = (TextView) convertView.findViewById(R.id.date_text);
            String countDown = "";
            String content = "";
            switch (type) {
                case TYPE_MEETING:
                    Meeting meeting = meetingList.get(position);
                    content = meeting.getTopic();
                    countDown = TimeUtils.getCountdown(getActivity(), meeting.getFrom());
                    WorkColorUtils.showDayOfWeek(countDownText,
                            TimeUtils
                                    .getCountdownNum(meeting.getFrom()));
                    String time = getMeetingTime(meeting);
                    dateText.setText(time);
                    break;
                case TYPE_TASK:
                    TaskResult task = taskList.get(position);
                    content = task.getTitle();
                    ViewGroup.LayoutParams param = countDownText.getLayoutParams();
                    param.height = DensityUtil.dip2px(getActivity(), 8);
                    param.width = param.height;
                    countDownText.setLayoutParams(param);
                    WorkColorUtils.showDayOfWeek(countDownText,
                            TimeUtils
                                    .getCountdownNum(task.getCreationDate()));
                    Calendar dueDate = task.getLocalDueDate();
                    if (dueDate != null) {
                        dateText.setText(TimeUtils.calendar2FormatString(getActivity(), dueDate, FORMAT_MONTH_DAY));
                    }else {
                        dateText.setText(R.string.not_set);
                    }
                    break;
                case TYPE_CALENDAR:
                    CalendarEvent calendarEvent = calEventList.get(position);
                    content = calendarEvent.getTitle();
                    countDown = TimeUtils.getCountdown(getActivity(), calendarEvent.getLocalStartDate());
                    WorkColorUtils.showDayOfWeek(countDownText,
                            TimeUtils
                                    .getCountdownNum(calendarEvent.getLocalStartDate()));
                    dateText.setText(TimeUtils.getCalEventTimeSelection(getActivity(), calendarEvent));
                    break;
                default:
                    break;
            }
            ((TextView) convertView.findViewById(R.id.content_text)).setText(content);
            countDownText.setText(countDown);
            return convertView;
        }
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {
        private String type;

        public ListOnItemClickListener(String type) {
            this.type = type;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            if (type.equals(TYPE_CALENDAR) ) {
                bundle.putSerializable("calEvent",
                        calEventList.get(position));
                IntentUtils.startActivity(getActivity(), CalEventAddActivity.class, bundle);
                recordUserClickWorkFunction("calendar");
            } else if (type.equals(TYPE_TASK)) {
                bundle.putSerializable("task",
                        taskList.get(position));
                IntentUtils.startActivity(getActivity(), MessionDetailActivity.class, bundle);
            } else if (type.equals(TYPE_MEETING)) {
                Meeting meeting = meetingList.get(position);
                bundle.putSerializable("meeting", meeting);
                IntentUtils.startActivity(getActivity(),
                        MeetingDetailActivity.class, bundle);
                recordUserClickWorkFunction("meeting");
            }
        }

    }

    /**
     * 获取会议时间
     *
     * @param meeting
     * @return
     */
    private String getMeetingTime(Meeting meeting) {
        String from = meeting.getFrom();
        String meetingFromTime = TimeUtils.calendar2FormatString(
                getActivity(), TimeUtils.timeString2Calendar(from),
                TimeUtils.FORMAT_HOUR_MINUTE);
        String to = meeting.getTo();
        String meetingToTime = TimeUtils.calendar2FormatString(
                getActivity(), TimeUtils.timeString2Calendar(to),
                TimeUtils.FORMAT_HOUR_MINUTE);
        return meetingFromTime + " - " + meetingToTime;
    }

    /**
     * 设置头部节假日等信息
     */
    private void handHeaderDate() {
        FestivalDate festivalDate = initFestivalDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(festivalDate.getFestivalTime());
        int betweenQM = 0;
        betweenQM = TimeUtils.getCountdownNum(calendar);
        calendar.setTimeInMillis(System.currentTimeMillis());
        String date = TimeUtils.calendar2FormatString(getActivity(), calendar, TimeUtils.FORMAT_MONTH_DAY);
        if (date.startsWith("0")) {
            date = date.substring(1,date.length());
        }
        ((TextView) (rootView.findViewById(R.id.work_date_text)))
                .setText(date);
        String appLanguageObj = PreferencesUtils.getString(
                getActivity(), UriUtils.tanent + "appLanguageObj", "");
        Language language = new Language(appLanguageObj);
        if (language.getIso().equals("zh-CN")
                || language.equals("zh-TW")
                || language.equals("followSys")) {
            ((TextView) (rootView.findViewById(R.id.work_chinesedate_text)))
                    .setText(CalendarUtil.getChineseToday()
                            + TimeUtils.getWeekDay(getContext(), calendar));
        } else if (language.getIso().equals("en-US")) {
            ((TextView) (rootView.findViewById(R.id.work_chinesedate_text)))
                    .setText(TimeUtils.calendar2FormatString(getActivity(),
                            calendar, TimeUtils.FORMAT_MONTH_DAY)
                            + "  "
                            + TimeUtils.getWeekDay(getContext(), calendar));
        }

        String festivalDateTips = FestivalCacheUtils.getFestivalTips(getActivity(), festivalDate.getFestivalKey());
        ((TextView) (rootView.findViewById(R.id.work_festvaldate_text)))
                .setText(festivalDateTips + "  " + betweenQM
                        + " " + getString(R.string.work_day));
        if (betweenQM < 0) {
            ((TextView) (rootView.findViewById(R.id.work_festvaldate_text)))
                    .setText(festivalDateTips + 0
                            + getString(R.string.work_day));
        }
    }

    /**
     * 初始化节日
     *
     * @return
     */
    private FestivalDate initFestivalDate() {
        FestivalDate festivalDate = null;
        try {
            if (!DbCacheUtils.getDb(getActivity()).tableIsExist(FestivalDate.class)) {
                FestivalCacheUtils.saveFestivalList(getActivity());
            }
            festivalDate = FestivalCacheUtils.getFestival(getActivity());
        } catch (DbException e) {
            e.printStackTrace();
        }
        return festivalDate;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
           if (requestCode == WORK_SETTING){
               refreshWorkLayout();
           }
        }
    }


    /**
     * 获取日历中Event
     */
    private void getMyCalendar() {
        if (NetUtils.isNetworkConnected(getActivity()) && isContainWork(TYPE_CALENDAR)) {
            apiService.getMyCalendar(0, 30);
        }
    }


    /**
     * 获取任务
     */
    private void getTasks() {
        if (NetUtils.isNetworkConnected(getActivity())&& isContainWork(TYPE_TASK)) {
            String orderBy = PreferencesUtils.getString(getActivity(),
                    "order_by", "PRIORITY");
            String orderType = PreferencesUtils.getString(getActivity(),
                    "order_type", "DESC");
            apiService.getRecentTasks(orderBy, orderType);
        }
    }


    /**
     * 获取会议
     */
    private void getMeetings() {
        if (NetUtils.isNetworkConnected(getActivity()) && isContainWork(TYPE_MEETING)) {
            apiService.getMeetings(7);
        }
    }

    /**
     * 获取三条Event
     */
    private void getCalEventsFor3() {
        if (NetUtils.isNetworkConnected(getActivity()) && calendarIdList.size() > 0) {
            Calendar afterCalendar = Calendar.getInstance();
            Calendar beforeCalendar = Calendar.getInstance();
            beforeCalendar.set(beforeCalendar.get(Calendar.YEAR) + 1,
                    beforeCalendar.get(Calendar.MONTH),
                    beforeCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar.set(afterCalendar.get(Calendar.YEAR),
                    afterCalendar.get(Calendar.MONTH),
                    afterCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
            beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
            apiService.getAllCalEvents(calendarIdList, afterCalendar,
                    beforeCalendar, 3, 0, false);
        }

    }


    /**
     * 获取今明两天所有日历的所有event
     */
    private void getCalEventsForTwoDays() {
        if (NetUtils.isNetworkConnected(getActivity()) && calendarIdList.size() > 0) {
            Calendar afterCalendar = Calendar.getInstance();
            Calendar beforeCalendar = Calendar.getInstance();
            beforeCalendar.set(beforeCalendar.get(Calendar.YEAR),
                    beforeCalendar.get(Calendar.MONTH),
                    beforeCalendar.get(Calendar.DAY_OF_MONTH) + 2, 0, 0, 0);
            afterCalendar.set(afterCalendar.get(Calendar.YEAR),
                    afterCalendar.get(Calendar.MONTH),
                    afterCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
            beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
            apiService.getAllCalEvents(calendarIdList, afterCalendar,
                    beforeCalendar, 5, 0, true);
        }
    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            WorkFragment.this.meetingList = getMeetingsResult.getMeetingsList();
            Collections.sort(WorkFragment.this.meetingList, new Meeting());
            meetingChildAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            taskList = getTaskListResult.getTaskList();
            taskChildAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnMyCalendarSuccess(
                GetMyCalendarResult getMyCalendarResult) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            List<MyCalendar> calendarList = getMyCalendarResult
                    .getCalendarList();
            MyCalendarCacheUtils
                    .saveMyCalendarList(getActivity(), calendarList);
            for (int i = 0; i < calendarList.size(); i++) {
                MyCalendar myCalendar = calendarList.get(i);
                if (!myCalendar.getState().equals("REMOVED")
                        && !MyCalendarOperationCacheUtils.getIsHide(
                        getActivity(), myCalendar.getId())) {
                    calendarIdList.add(calendarList.get(i).getId());
                }
            }
            getCalEventsForTwoDays();
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnCalEventsSuccess(
                GetCalendarEventsResult getCalendarEventsResult,
                boolean isRefresh) {
            calEventList = getCalendarEventsResult.getCalEventList();
            CalEventNotificationUtils.setCalEventNotification(getActivity().getApplicationContext(), calEventList);
            if (isRefresh && (calEventList.size() < 3)) { // 获取今明两天的日历不足3条
                getCalEventsFor3();
            } else {
                calendarChildAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            if (workSettingList.size()>0){
                getWorkData();
            }else {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            }
        } else {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
        }
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
    }


    /**
     * 记录用户点击
     *
     * @param functionId
     */
    private void recordUserClickWorkFunction(String functionId) {
        PVCollectModel pvCollectModel = new PVCollectModel(functionId, "work");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
    }

    @Override
    public void onPause() {
        super.onPause();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (calEventReceiver != null) {
            getActivity().unregisterReceiver(calEventReceiver);
            calEventReceiver = null;
        }
        if (meetingAndTaskReceiver != null) {
            getActivity().unregisterReceiver(meetingAndTaskReceiver);
            meetingAndTaskReceiver = null;
        }
    }

}
