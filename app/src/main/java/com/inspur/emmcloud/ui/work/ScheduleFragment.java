package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Meeting;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.bean.work.WorkSetting;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingBookingActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionDetailActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.CalEventNotificationUtils;
import com.inspur.emmcloud.util.privates.CalendarUtil;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.WorkColorUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.WorkSettingCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.calendarview.Calendar;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.inspur.emmcloud.util.privates.TimeUtils.FORMAT_MONTH_DAY;

/**
 * Created by yufuchang on 2019/2/18.
 */

public class ScheduleFragment extends Fragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener {
    private static final String TYPE_CALENDAR = "calendar";
    private static final String TYPE_APPROVAL = "approval";
    private static final String TYPE_MEETING = "meeting";
    private static final String TYPE_TASK = "task";
    private static final int WORK_SETTING = 1;

    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private CalendarView calendarView;
    private CalendarLayout calendarLayout;
    private View rootView;
    private PopupWindow popupWindow;
    private TextView scheduleDataText;

    private WorkAPIService apiService;
    private ListView listView;
    private BaseAdapter adapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private ArrayList<TaskResult> taskList = new ArrayList<>();
    private List<CalendarEvent> calEventList = new ArrayList<>();
    private BroadcastReceiver calEventReceiver;
    private BroadcastReceiver meetingAndTaskReceiver;
    private List<String> calendarIdList = new ArrayList<>();
    private ChildAdapter calendarChildAdapter, meetingChildAdapter, taskChildAdapter;

    private boolean isWorkPortletConfigUploadSuccess = true;  //flag:判断是否上传配置信息成功
    private List<WorkSetting> workSettingList = new ArrayList<>();

    private View.OnClickListener onViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            popupWindow.dismiss();
            switch (v.getId()) {
                case R.id.rl_schedule_calendar:
                    recordUserClickWorkFunction(PV_COLLECTION_CAL);
                    IntentUtils.startActivity(getActivity(), CalActivity.class);
                    break;
                case R.id.rl_schedule_meeting:
                    recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                    IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    break;
                case R.id.rl_schedule_mission:
                    recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                    IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_schedule, null);
        initView();
//        initData();

        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        initWorkSetting();
        getWorkData();
        registerWorkNotifyReceiver();
        EventBus.getDefault().register(this);
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

    private void initView() {
        calendarView = rootView.findViewById(R.id.calendar_view_schedule);
        calendarLayout = rootView.findViewById(R.id.calendar_layout_schedule);
        listView = rootView.findViewById(R.id.list);
        adapter = new Adapter();
        listView.setAdapter(adapter);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarView.setOnYearChangeListener(this);
        scheduleDataText = rootView.findViewById(R.id.tv_schedule_date);
        setCalendarTime(System.currentTimeMillis());
        rootView.findViewById(R.id.iv_schedule_function_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.iv_schedule_function_list:
                        showPopupWindow(v);
                        break;
                }
            }
        });
        rootView.findViewById(R.id.iv_schedule_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarLayout.switchStatus();
            }
        });
        initData();
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
    public void setScheduleBackToToday(){
        if(calendarView != null){
            calendarView.scrollToCurrent();
        }
    }

    /**
     * 通讯录和创建群组，扫一扫合并
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.pop_schedule_window_view, null);

        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        contentView.findViewById(R.id.rl_schedule_calendar).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_mission).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_meeting).setOnClickListener(onViewClickListener);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (calEventReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(calEventReceiver);
            calEventReceiver = null;
        }
        if (meetingAndTaskReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(meetingAndTaskReceiver);
            meetingAndTaskReceiver = null;
        }
        EventBus.getDefault().unregister(this);
        PreferencesUtils.putBoolean(getActivity(), Constant.PREF_WORK_PORTLET_CONFIG_UPLOAD, isWorkPortletConfigUploadSuccess);
    }

    /**
     * 更新附件信息
     *
     * @param taskResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTaskData(TaskResult taskResult) {
        if (taskResult != null) {
            int index = taskList.indexOf(taskResult);
            if (index != -1) {
//                taskList.get(index).setAttachments(taskResult.getAttachments());
                taskList.remove(index);
                taskList.add(index, taskResult);
                taskChildAdapter.notifyDataSetChanged();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == WORK_SETTING) {
                refreshWorkLayout();
                uploadWorkPortletConfig();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isWorkPortletConfigUploadSuccess) {
            uploadWorkPortletConfig();
        }
    }

    /**
     * 上传工作页面配置信息
     */
    private void uploadWorkPortletConfig() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            isWorkPortletConfigUploadSuccess = true;
            apiService.saveWorkPortletConfig(getWorkPortletConfigJson());
        } else {
            isWorkPortletConfigUploadSuccess = false;
        }
    }

    /**
     * 获取工作页面ui配置
     *
     * @return
     */
    private String getWorkPortletConfigJson() {
        List<WorkSetting> allWorkSettingList = WorkSettingCacheUtils.getAllWorkSettingList(getActivity());
        JSONArray array = new JSONArray();
        try {
            JSONObject infoBarObj = new JSONObject();
            boolean isInfoBarOpen = PreferencesByUserAndTanentUtils.getBoolean(getActivity(), Constant.PREF_WORK_INFO_BAR_OPEN, true);
            infoBarObj.put("id", "infoBar");
            infoBarObj.put("isOpen", isInfoBarOpen);
            array.put(infoBarObj);
            for (int i = 0; i < allWorkSettingList.size(); i++) {
                WorkSetting workSetting = allWorkSettingList.get(i);
                JSONObject obj = new JSONObject();
                obj.put("id", workSetting.getId());
                obj.put("isOpen", workSetting.isOpen());
                array.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    /**
     * 当工作页面配置发生改变后进行数据和layout的刷新
     */
    private void refreshWorkLayout() {
        initWorkSetting();
        adapter.notifyDataSetChanged();
        getWorkData();
    }

    /***
     * 初始化工作页面ui配置
     */
    private void initWorkSetting() {
        isWorkPortletConfigUploadSuccess = PreferencesUtils.getBoolean(getActivity(), Constant.PREF_WORK_PORTLET_CONFIG_UPLOAD, true);
        String WorkPortletConfigJson = AppConfigCacheUtils.getAppConfigValue(getActivity(), "WorkPortlet", null);
        List<WorkSetting> allWorkSettingList = WorkSettingCacheUtils.getAllWorkSettingList(getActivity());
        if (allWorkSettingList.size() == 0) { //本地没有缓存
            if (WorkPortletConfigJson == null || WorkPortletConfigJson.equals("null")) {   //服务端没有配置
                workSettingList.add(new WorkSetting(TYPE_MEETING, getString(R.string.meeting), true, 0));
                workSettingList.add(new WorkSetting(TYPE_CALENDAR, getString(R.string.work_calendar_text), true, 1));
                workSettingList.add(new WorkSetting(TYPE_TASK, getString(R.string.work_task_text), true, 2));
                WorkSettingCacheUtils.saveWorkSettingList(getActivity(), workSettingList);
            } else {  //服务端有配置
                JSONArray array = JSONUtils.getJSONArray(WorkPortletConfigJson, new JSONArray());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = JSONUtils.getJSONObject(array, i, new JSONObject());
                    String id = JSONUtils.getString(object, "id", "");
                    boolean isOpen = JSONUtils.getBoolean(object, "isOpen", true);
                    if (i == 0) {
                        PreferencesByUserAndTanentUtils.putBoolean(getContext(), Constant.PREF_WORK_INFO_BAR_OPEN, isOpen);
                    } else {
                        workSettingList.add(new WorkSetting(id, "", isOpen, i - 1));
                    }

                }
                WorkSettingCacheUtils.saveWorkSettingList(getActivity(), workSettingList);
            }
        }
        workSettingList = WorkSettingCacheUtils.getOpenWorkSettingList(getActivity());
        //当服务端还没有保存过配置信息时需要上传
        if (WorkPortletConfigJson == null || WorkPortletConfigJson.equals("null")) {
            uploadWorkPortletConfig();
        }
        //判断此页面如果没有内容则显示空白页
        boolean isHaveContent = isContainWork(TYPE_MEETING) || isContainWork(TYPE_APPROVAL) || isContainWork(TYPE_CALENDAR) || isContainWork(TYPE_TASK);
        rootView.findViewById(R.id.rl_no_work_content).setVisibility(isHaveContent ? View.GONE : View.VISIBLE);
    }

    /**
     * 获取数据
     */
    private void getWorkData() {
        if (NetUtils.isNetworkConnected(getActivity()) && workSettingList.size() > 0) {
            getMeetings();
            getMyCalendar();
            getTasks();
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
     * 获取日历中Event
     */
    private void getMyCalendar() {
        if (NetUtils.isNetworkConnected(getActivity()) && isContainWork(TYPE_CALENDAR)) {
            apiService.getMyCalendar(0, 30);
        }
    }

    /**
     * 判断工作中是否开启此卡片
     *
     * @param type
     * @return
     */
    private boolean isContainWork(String type) {
        return (workSettingList.size() > 0 && workSettingList.contains(new WorkSetting(type, "", true, 0)));
    }

    /**
     * 获取任务
     */
    private void getTasks() {
        if (NetUtils.isNetworkConnected(getActivity()) && isContainWork(TYPE_TASK)) {
            String orderBy = PreferencesUtils.getString(getActivity(),
                    "order_by", "PRIORITY");
            String orderType = PreferencesUtils.getString(getActivity(),
                    "order_type", "DESC");
            apiService.getRecentTasks(orderBy, orderType);
        }
    }

    /**
     * 记录用户点击
     *
     * @param functionId
     */
    private void recordUserClickWorkFunction(String functionId) {
        PVCollectModelCacheUtils.saveCollectModel(functionId, "work");
    }

    @Override
    public void onYearChange(int year) {

    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        setCalendarTime(calendar.getTimeInMillis());
    }

    private void setCalendarTime(long timeInMillis) {
        java.util.Calendar calendar1 = TimeUtils.
                timeLong2Calendar(timeInMillis);
        String time = TimeUtils.calendar2FormatString(getActivity(), calendar1, TimeUtils.FORMAT_YEAR_MONTH_DAY_BY_DASH) + "·" +
                CalendarUtil.getWeekDay(calendar1);
        boolean isToday = TimeUtils.isCalendarToday(calendar1);
        if (isToday) {
            time = getString(R.string.today) + "·" + time;
        }
        scheduleDataText.setText(time);
    }

    @Override
    public void isExpand(boolean isExpand) {
        ((ImageView) rootView.findViewById(R.id.iv_schedule_arrow)).setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
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
            calendarChildAdapter.notifyDataSetChanged();
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

    static class ViewHolder {
        ImageView groupIconImg;
        TextView groupTitleText, workAddText;
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
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.work_card_group_item_view_vertical, null);
                holder.groupIconImg = convertView.findViewById(R.id.group_icon_img);
                holder.groupTitleText = convertView.findViewById(R.id.group_title_text);
                holder.groupHeaderlayout = convertView.findViewById(R.id.group_header_layout);
                holder.GroupListView = convertView.findViewById(R.id.list);
                holder.wordAddLayout = convertView.findViewById(R.id.work_add_layout);
                holder.workAddText = convertView.findViewById(R.id.work_add_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convertView.findViewById(R.id.bottom_blank_view).setVisibility((position == getCount() - 1) ? View.VISIBLE : View.GONE);
            WorkSetting workSetting = workSettingList.get(position);
            final String id = workSetting.getId();
            holder.groupHeaderlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (id.equals(TYPE_CALENDAR)) {
                        recordUserClickWorkFunction(TYPE_CALENDAR);
                        IntentUtils.startActivity(getActivity(), CalActivity.class);
                    } else if (id.equals(TYPE_MEETING)) {
                        recordUserClickWorkFunction(TYPE_MEETING);
                        IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    } else if (id.equals(TYPE_TASK)) {
                        recordUserClickWorkFunction(TYPE_TASK);
                        IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    }
                }
            });
            holder.wordAddLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (id.equals(TYPE_CALENDAR)) {
                        recordUserClickWorkFunction(TYPE_CALENDAR);
                        IntentUtils.startActivity(getActivity(), CalEventAddActivity.class);
                    } else if (id.equals(TYPE_MEETING)) {
                        recordUserClickWorkFunction(TYPE_MEETING);
                        IntentUtils.startActivity(getActivity(), MeetingBookingActivity.class);
                    } else if (id.equals(TYPE_TASK)) {
                        recordUserClickWorkFunction(TYPE_TASK);
                        IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    }
                }
            });

            if (id.equals(TYPE_CALENDAR)) {
                holder.workAddText.setText(R.string.calendar_add_calendar);
                holder.groupIconImg.setImageResource(R.drawable.ic_work_calendar);
                calendarChildAdapter = new ChildAdapter(TYPE_CALENDAR);
                holder.GroupListView.setAdapter(calendarChildAdapter);
                holder.groupTitleText.setText(R.string.work_calendar_text);
            } else if (id.equals(TYPE_MEETING)) {
                holder.workAddText.setText(R.string.meeting_add);
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
            return taskList.size() < 5 ? taskList.size() : 5;
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
            TextView countDownText = convertView.findViewById(R.id.count_down_text);
            TextView dateText = convertView.findViewById(R.id.date_text);
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
                    java.util.Calendar dueDate = task.getLocalDueDate();
                    if (dueDate != null) {
                        dateText.setText(TimeUtils.calendar2FormatString(getActivity(), dueDate, FORMAT_MONTH_DAY));
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
            ((TextView) convertView.findViewById(R.id.tv_content)).setText(content);
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
            if (type.equals(TYPE_CALENDAR)) {
                bundle.putSerializable("calEvent",
                        calEventList.get(position));
                IntentUtils.startActivity(getActivity(), CalEventAddActivity.class, bundle);
                recordUserClickWorkFunction(TYPE_CALENDAR);
            } else if (type.equals(TYPE_TASK)) {
                bundle.putSerializable(TYPE_TASK,
                        taskList.get(position));
                IntentUtils.startActivity(getActivity(), MessionDetailActivity.class, bundle);
                recordUserClickWorkFunction(TYPE_TASK);
            } else if (type.equals(TYPE_MEETING)) {
                Meeting meeting = meetingList.get(position);
                bundle.putSerializable(TYPE_MEETING, meeting);
                IntentUtils.startActivity(getActivity(),
                        MeetingDetailActivity.class, bundle);
                recordUserClickWorkFunction(TYPE_MEETING);
            }
        }

    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
            meetingList = getMeetingsResult.getMeetingsList();
            if (meetingChildAdapter != null) {
                meetingChildAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            taskList = getTaskListResult.getTaskList();
            if (taskChildAdapter != null) {
                taskChildAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnMyCalendarSuccess(
                GetMyCalendarResult getMyCalendarResult) {
            List<MyCalendar> calendarList = getMyCalendarResult
                    .getCalendarList();
            MyCalendarCacheUtils
                    .saveMyCalendarList(getActivity(), calendarList);
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
            } else if (calendarChildAdapter != null) {
                calendarChildAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }


        @Override
        public void returnSaveConfigSuccess() {
            isWorkPortletConfigUploadSuccess = true;
        }

        @Override
        public void returnSaveConfigFail() {
            isWorkPortletConfigUploadSuccess = false;
        }
    }
}
