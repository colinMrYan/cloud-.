package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.DateTimePickerDialog;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.RemindEvent;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/4/9.
 */

public class MeetingAddActivity extends BaseActivity {
    public static final String EXTRA_EVENT_TYPE = "extra_event_type";
    public static final String EXTRA_SCHEDULE_CALENDAR_EVENT = "schedule_calendar_event";
    private static final int REQUEST_SELECT_ATTENDEE = 1;
    private static final int REQUEST_SELECT_RECORDER = 2;
    private static final int REQUEST_SELECT_LIAISON = 3;
    private static final int REQUEST_SELECT_MEETING_ROOM = 4;
    private static final int REQUEST_SET_REMIND_EVENT = 5;
    @BindView(R.id.et_title)
    EditText titleEdit;
    @BindView(R.id.tv_start_date)
    TextView startDateText;
    @BindView(R.id.tv_start_time)
    TextView startTimeText;
    @BindView(R.id.tv_end_date)
    TextView endDateText;
    @BindView(R.id.tv_end_time)
    TextView endTimeText;
    @BindView(R.id.tv_meeting_position)
    TextView meetingPositionText;
    @BindView(R.id.ll_attendee)
    LinearLayout attendeeLayout;
    @BindView(R.id.ll_recorder)
    LinearLayout recorderLayout;
    @BindView(R.id.ll_liaison)
    LinearLayout liaisonLayout;
    @BindView(R.id.et_notes)
    EditText notesEdit;
    @BindView(R.id.tv_reminder)
    TextView reminderText;
    @BindView(R.id.tv_new_event_title)
    TextView newEventTitleText;
    @BindView(R.id.rl_calendar_type)
    RelativeLayout calendarTypeLayout;
    @BindView(R.id.switch_all_day)
    Switch allDaySwitch;
    @BindView(R.id.tv_event_type)
    TextView eventTypeText;

    private LoadingDialog loadingDlg;
    private ScheduleApiService apiService;
    private Calendar startTimeCalendar; // 开始时间
    private Calendar endTimeCalendar;   //结束时间
    private boolean isAllDay = false;   //是否全天
    private List<SearchModel> attendeeSearchModelList = new ArrayList<>();  //参会人
    private List<SearchModel> recorderSearchModelList = new ArrayList<>();  //记录人
    private List<SearchModel> liaisonSearchModelList = new ArrayList<>();   //联系人
    private MeetingRoom meetingRoom;    //会议室
    private Location location;          // 地点
    private String title;               // 标题
    private String note;                // 备注
    private String meetingPosition;
    private String meetingOwner = "";   // 所有者
    private RemindEvent remindEvent;    // 提醒
    private Meeting meeting = new Meeting();
    private Schedule schedule = new Schedule();
    private boolean isMeetingEditModel = false;
    private boolean isScheduleEditModel = false;
    private boolean eventTypeIsMeeting = false;
    private boolean isAllDay = false;
    private String id;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        eventTypeIsMeeting = getIntent().getBooleanExtra(EXTRA_EVENT_TYPE, false);
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(new WebService());
        if (eventTypeIsMeeting) {
            initMeetingData();
        } else {

        }



        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_add;
    }


    /**
     * 初始化会议数据
     */
    private void initMeetingData() {
        isMeetingEditModel = getIntent().hasExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY);
        if (isMeetingEditModel) {
            meeting = (Meeting) getIntent().getSerializableExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY);
            meetingOwner = meeting.getOwner();
            location = new Location(JSONUtils.getJSONObject(meeting.getLocation()));
            startTimeCalendar = meeting.getStartTimeCalendar();
            endTimeCalendar = meeting.getEndTimeCalendar();
            title = meeting.getTitle();
            note = meeting.getNote();
            List<String> attendeeList = meeting.getGetParticipantList();
            for (int i = 0; i < attendeeList.size(); i++) {
                meeting.getRoleParticipantList();
                JSONObject jsonObject = JSONUtils.getJSONObject(attendeeList.get(i));
                String uid = JSONUtils.getString(jsonObject, "id", "");
                SearchModel searchModel = getSearchModel(uid);
                String role = JSONUtils.getString(jsonObject, "role", "");
                if (Participant.TYPE_COMMON.equals(role)) {
                    attendeeSearchModelList.add(searchModel);
                } else if (Participant.TYPE_CONTACT.equals(role)) {
                    liaisonSearchModelList.add(searchModel);
                } else if (Participant.TYPE_RECORDER.equals(role)) {
                    recorderSearchModelList.add(searchModel);
                }
            }
            remindEvent = meeting.getRemindEventObj();
        } else {
            String myUid = MyApplication.getInstance().getUid();
            ContactUser myInfo = ContactUserCacheUtils.getContactUserByUid(myUid);
            SearchModel myInfoSearchModel = new SearchModel(myInfo);
            attendeeSearchModelList.add(myInfoSearchModel);
            startTimeCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
            endTimeCalendar = (Calendar) startTimeCalendar.clone();
            endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
            meetingOwner = MyApplication.getInstance().getUid();
            if (getIntent().hasExtra(MeetingRoomListActivity.EXTRA_START_TIME)
                    && getIntent().hasExtra(MeetingRoomListActivity.EXTRA_END_TIME)
                    && getIntent().hasExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM)) {
                Calendar startTimeFromRoomCalendar = (Calendar) getIntent().getSerializableExtra(MeetingRoomListActivity.EXTRA_START_TIME);
                Calendar endTimeFromRoomCalendar = (Calendar) getIntent().getSerializableExtra(MeetingRoomListActivity.EXTRA_END_TIME);
                correctMeetingRoomTime(startTimeFromRoomCalendar, endTimeFromRoomCalendar);
                meetingRoom = (MeetingRoom) getIntent().getSerializableExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM);
                location = new Location();
                location.setId(meetingRoom.getId());
                location.setBuilding(meetingRoom.getBuilding().getName());
                location.setDisplayName(meetingRoom.getName());
            }
        }
    }

    /**
     * 初始化日程数据
     */
    private void initSchedule() {

    }


    /**
     * 初始化View
     */
    private void initScheduleView() {
        allDaySwitch.setOnCheckedChangeListener(this);
        allDaySwitch.setChecked(isAllDay);
        titleEdit.setText(title);
        newEventTitleText.setText(isScheduleEditModel ? getApplication().getString(R.string.schedule_calendar_create) :
                getApplication().getString(R.string.schedule_calendar_detail));
        eventTypeText.setText(getApplication().getString(R.string.schedule_calendar_company));
        initStartEndTimeView();
        alertText.setText(remindEvent.getName());
        setViewIsEditable(isAddCalendar);
    }

    /**
     * 初始化日期数据
     */
    private void init() {
        if (getIntent().hasExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE)) {
            title = getIntent().getStringExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE);
        }

        id = getIntent().getStringExtra(Constant.SCHEDULE_QUERY);   //解析通知字段获取id
        if (!TextUtils.isEmpty(id)) {        ////来自通知
            isScheduleEditModel = false;
            getDbCalendarFromId();
            getNetCalendarFromId();
        } else if (getIntent().hasExtra(EXTRA_SCHEDULE_CALENDAR_EVENT)) {  //通知没有，列表页跳转过来
            isScheduleEditModel = false;
            schedule = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_EVENT);
            initscheduleData();     //直接用传过来的数据
        } else {    //创建日程
            createCalendar();
        }
    }

    /**
     * 设置日程相关数据
     */
    private void initscheduleData() {
        isAllDay = schedule.getAllDay();
        startTimeCalendar = schedule.getStartTimeCalendar();
        endTimeCalendar = schedule.getEndTimeCalendar();
        title = schedule.getTitle();
        //saveTextView.setVisibility(View.GONE);
        // calendarDetailMoreImageView.setVisibility(View.VISIBLE);
        List<MyCalendar> allCalendarList = MyCalendarCacheUtils.getAllMyCalendarList(getApplicationContext());
        String calendarType = schedule.getType();
        String alertTimeName = ScheduleAlertTimeActivity.getAlertTimeNameByTime(JSONUtils.getInt(schedule.getRemindEvent(), "advanceTimeSpan", -1), isAllDay);
        remindEvent = new RemindEvent(JSONUtils.getString(schedule.getRemindEvent(), "remindType", "in_app"),
                JSONUtils.getInt(schedule.getRemindEvent(), "advanceTimeSpan", -1), alertTimeName);
    }

    /**
     * 创建日程
     */
    private void createCalendar() {
        //此参数传过来精确的开始时间和结束时间
        if (getIntent().hasExtra(EXTRA_START_CALENDAR)) {
            startTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_START_CALENDAR);
            endTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_END_CALENDAR);
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            if (getIntent().hasExtra(EXTRA_SELECT_CALENDAR)) {
                startTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_SELECT_CALENDAR);
            }
            if (startTimeCalendar == null) {
                startTimeCalendar = (Calendar) currentCalendar.clone();
            }
            startTimeCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
            startTimeCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
            startTimeCalendar = TimeUtils.getNextHalfHourTime(startTimeCalendar);
            endTimeCalendar = (Calendar) startTimeCalendar.clone();
            if (!isAllDay) {
                endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
            }
        }
        schedule.setOwner(MyApplication.getInstance().getUid());//??默认
        remindEvent.setName(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), isAllDay));
        intervalMin = (int) getIntervalMin();
        initView();
    }

    /**
     * 从数据库获取日程数据
     */
    private void getDbCalendarFromId() {
        schedule = ScheduleCacheUtils.getDBScheduleById(this, id);
        if (schedule != null) {
            initscheduleData();
            // initView();
        }
    }

    /**
     * 从网络获取日程数据
     */
    private void getNetCalendarFromId() {
        if (NetUtils.isNetworkConnected(this)) {
            if (!TextUtils.isEmpty(id)) {
                if (schedule == null || TextUtils.isEmpty(schedule.getId())) { //如果缓存有数据则不显示loading
                    loadingDlg.show();
                }
                apiService.getCalendarDataFromId(id);
            }
        } else {
            ToastUtils.show(this, "");
        }
    }







    private SearchModel getSearchModel(String uid) {
        SearchModel searchModel = new SearchModel();
        Router router = Router.getInstance();
        if (router.getService(ContactService.class) != null) {
            ContactService service = router.getService(ContactService.class);
            ContactUser contactUser = service.getContactUserByUid(uid);
            searchModel.setType(SearchModel.TYPE_USER);
            searchModel.setId(contactUser.getId());
            searchModel.setName(contactUser.getName());
            searchModel.setEmail(contactUser.getEmail());
        }
        return searchModel;
    }


    /**
     * 初始化视图
     */
    private void initView() {
        loadingDlg = new LoadingDialog(this);
        if (isMeetingEditModel) {
            EditTextUtils.setText(titleEdit, title);
            meetingPositionText.setText(location.getBuilding() + " " + location.getDisplayName());
            notesEdit.setText(note);
            showSelectUser(liaisonLayout, liaisonSearchModelList);
            showSelectUser(recorderLayout, recorderSearchModelList);
            reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), isAllDay));
        } else if (location != null) {
            meetingPositionText.setText(location.getBuilding() + " " + location.getDisplayName());
        }
        showSelectUser(attendeeLayout, attendeeSearchModelList);
        setMeetingTime();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_save:
                if (!isInputValid())
                    return;
                addOrUpdateMeeting();
                break;
            case R.id.ll_start_time:
                showTimeSelectDialog(true);
                break;
            case R.id.ll_end_time:
                showTimeSelectDialog(false);
                break;
            case R.id.ll_add_position:
                Intent intent = new Intent(this, MeetingRoomListActivity.class);
                intent.putExtra(MeetingRoomListActivity.EXTRA_START_TIME, startTimeCalendar);
                intent.putExtra(MeetingRoomListActivity.EXTRA_END_TIME, endTimeCalendar);
                startActivityForResult(intent, REQUEST_SELECT_MEETING_ROOM);
                break;
            case R.id.ll_add_attendee:
                selectContact(REQUEST_SELECT_ATTENDEE);
                break;
            case R.id.ll_add_recorder:
                selectContact(REQUEST_SELECT_RECORDER);
                break;
            case R.id.ll_add_liaison:
                selectContact(REQUEST_SELECT_LIAISON);
                break;
            case R.id.ll_reminder:
                setReminder();
                break;
            case R.id.tv_meeting_position:
                Intent intent2 = new Intent(this, MeetingRoomListActivity.class);
                intent2.putExtra(MeetingRoomListActivity.EXTRA_START_TIME, startTimeCalendar);
                intent2.putExtra(MeetingRoomListActivity.EXTRA_END_TIME, endTimeCalendar);
                startActivityForResult(intent2, REQUEST_SELECT_MEETING_ROOM);
                break;
        }
    }


    private boolean isInputValid() {
        title = titleEdit.getText().toString().trim();
        meetingPosition = meetingPositionText.getText().toString();
        if (StringUtils.isBlank(title)) {
            ToastUtils.show(MyApplication.getInstance(), R.string.meeting_room_booking_topic);
            return false;
        }
        if (StringUtils.isBlank(meetingPosition)) {
            ToastUtils.show(MyApplication.getInstance(), R.string.meeting_room_booking_choosing_room);
            return false;
        }


        if (attendeeSearchModelList.size() == 0) {
            ToastUtils.show(MyApplication.getInstance(), R.string.meeting_invating_members);
            return false;
        }
        if (title.length() > 149) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.meeting_topic_too_long));
            return false;
        }
        note = notesEdit.getText().toString();
        if (!StringUtils.isBlank(note) && note.length() > 499) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.meeting_notice_too_long));
            return false;
        }

        if (startTimeCalendar.after(endTimeCalendar)) {
            ToastUtils.show(MeetingAddActivity.this, R.string.calendar_start_or_end_time_illegal);
            return false;
        }

        if (endTimeCalendar.before(Calendar.getInstance())) {
            ToastUtils.show(MeetingAddActivity.this, R.string.calendar_end_time_no_before_current);
            return false;
        }

        int countHour = TimeUtils.getCeil(endTimeCalendar, startTimeCalendar);
        if (meetingRoom != null && countHour > Integer.parseInt(meetingRoom.getMaxDuration())) {
            ToastUtils.show(MeetingAddActivity.this, getString(R.string.meeting_more_than_max_time));
            return false;
        }
        if (location == null) {
            location = new Location();
        }

        return true;
    }

    private void setReminder() {
        Intent intent = new Intent(this, ScheduleAlertTimeActivity.class);
        int advanceTimeSpan = -1;
        if (remindEvent != null) {
            advanceTimeSpan = remindEvent.getAdvanceTimeSpan();
        }
        intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, advanceTimeSpan);
        intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_IS_ALL_DAY, isAllDay);
        startActivityForResult(intent, REQUEST_SET_REMIND_EVENT);
    }

    private void selectContact(int requestCode) {
        String title = "";
        Intent intent = new Intent();
        intent.putExtra("select_content", 2);
        intent.putExtra("isMulti_select", true);
        intent.putExtra("isContainMe", true);
        if (requestCode == REQUEST_SELECT_ATTENDEE) {
            title = getString(R.string.schedule_meeting_select_attendee_title);
            intent.putExtra("hasSearchResult", (Serializable) attendeeSearchModelList);
        } else if (requestCode == REQUEST_SELECT_RECORDER) {
            title = getString(R.string.schedule_meeting_select_record_holder_title);
            intent.putExtra("hasSearchResult", (Serializable) recorderSearchModelList);
        } else {
            title = getString(R.string.schedule_meeting_select_conference_title);
            intent.putExtra("hasSearchResult", (Serializable) liaisonSearchModelList);
        }
        intent.putExtra("title", title);
        intent.setClass(getApplicationContext(), ContactSearchActivity.class);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 弹出日期时间选择框
     *
     * @param isStartTime 是否是开始时间
     */
    private void showTimeSelectDialog(final boolean isStartTime) {
        DateTimePickerDialog startDataTimePickerDialog = new DateTimePickerDialog(this);
        startDataTimePickerDialog.setDataTimePickerDialogListener(new DateTimePickerDialog.TimePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                calendar.set(Calendar.MILLISECOND, 0);
                if (isStartTime) {
                    startTimeCalendar = calendar;
                    endTimeCalendar = (Calendar) startTimeCalendar.clone();
                    endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
                } else {
                    if (!calendar.after(startTimeCalendar)) {
                        endTimeCalendar = (Calendar) startTimeCalendar.clone();
                        endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
                        showTimeInvalidDlg();
                        return;
                    }
                }
                setMeetingTime();

            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        startDataTimePickerDialog.showDatePickerDialog(isAllDay, isStartTime ? startTimeCalendar : endTimeCalendar);
    }


    /**
     * 显示开始和结束时间
     */
    private void setMeetingTime() {
        startDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startTimeCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY));
        startTimeText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startTimeCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE));
        endDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), endTimeCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY));
        endTimeText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), endTimeCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE));
    }

    /**
     * 结束时间早于起始时间提醒
     */
    private void showTimeInvalidDlg() {
        new CustomDialog.MessageDialogBuilder(this).setMessage(R.string.schedule_calendar_time_alert)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<SearchModel> searchModelList = (List<SearchModel>) data.getExtras().getSerializable("selectMemList");
            switch (requestCode) {
                case REQUEST_SELECT_ATTENDEE:
                    attendeeSearchModelList = searchModelList;
                    showSelectUser(attendeeLayout, searchModelList);
                    break;
                case REQUEST_SELECT_RECORDER:
                    recorderSearchModelList = searchModelList;
                    showSelectUser(recorderLayout, searchModelList);
                    break;
                case REQUEST_SELECT_LIAISON:
                    liaisonSearchModelList = searchModelList;
                    showSelectUser(liaisonLayout, searchModelList);
                    break;
                case REQUEST_SELECT_MEETING_ROOM:
                    Calendar backStartTimeCalendar = (Calendar) data.getSerializableExtra(MeetingRoomListActivity.EXTRA_START_TIME);
                    Calendar backEndTimeCalendar = (Calendar) data.getSerializableExtra(MeetingRoomListActivity.EXTRA_END_TIME);
                    correctMeetingRoomTime(backStartTimeCalendar, backEndTimeCalendar);
                    meetingRoom = (MeetingRoom) data.getSerializableExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM);
                    setMeetingTime();
                    meetingPositionText.setText(meetingRoom.getBuilding().getName() + " " + meetingRoom.getName());
                    location = new Location();
                    location.setId(meetingRoom.getId());
                    location.setBuilding(meetingRoom.getBuilding().getName());
                    location.setDisplayName(meetingRoom.getName());
                    break;
                case REQUEST_SET_REMIND_EVENT:
                    remindEvent = (RemindEvent) data.getSerializableExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), isAllDay));
                    break;
            }
        }

    }

    /**
     * 修正会议室可用时间
     */
    private void correctMeetingRoomTime(Calendar meetingRoomStartCalendar, Calendar meetingRoomEndCalendar) {
        // 首先当前时间右半部分与会议室返回时间取交集，如果交集为空时间不做修改
        Calendar currentCalendar = Calendar.getInstance();
        Calendar nextHalfHourCalendar = TimeUtils.getNextHalfHourTime(currentCalendar);
        if (nextHalfHourCalendar.before(meetingRoomEndCalendar)) {   //有交集
            if (nextHalfHourCalendar.after(meetingRoomStartCalendar)) {
                Calendar modifiedCalendar = (Calendar) nextHalfHourCalendar.clone();
                modifiedCalendar.add(Calendar.HOUR_OF_DAY, 2);
                endTimeCalendar = modifiedCalendar.after(meetingRoomEndCalendar) ? meetingRoomEndCalendar : modifiedCalendar;
                startTimeCalendar = nextHalfHourCalendar;
            } else {
                Calendar nextHalfHourStartCalendar = TimeUtils.getNextHalfHourTime(meetingRoomStartCalendar);
                Calendar modifiedStartCalendar = (Calendar) nextHalfHourStartCalendar.clone();
                modifiedStartCalendar.add(Calendar.HOUR_OF_DAY, 2);
                endTimeCalendar = modifiedStartCalendar.after(meetingRoomEndCalendar) ? meetingRoomEndCalendar : modifiedStartCalendar;
                startTimeCalendar = nextHalfHourStartCalendar.after(meetingRoomEndCalendar) ? meetingRoomStartCalendar : nextHalfHourStartCalendar;
            }
        } else {
            //可能存在半小时以内的会议，如果开始时间
            if (meetingRoomEndCalendar.after(currentCalendar)) {
                endTimeCalendar = meetingRoomEndCalendar;
                startTimeCalendar = currentCalendar;
            } else {
                endTimeCalendar = meetingRoomEndCalendar;
                startTimeCalendar = meetingRoomStartCalendar;
            }
        }
    }

    /**
     * 展示选择用户的头像
     *
     * @param layout
     * @param searchModelList
     */
    private void showSelectUser(LinearLayout layout, List<SearchModel> searchModelList) {
        layout.removeAllViews();
        if (searchModelList.size() > 0) {
            for (int i = 0; i < searchModelList.size(); i++) {
                if (i == 3) {
                    break;
                }
                ImageView imageView = new ImageView(this);
                int width = DensityUtil.dip2px(MyApplication.getInstance(), 30);
                int marginLeft = DensityUtil.dip2px(MyApplication.getInstance(), 10);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
                layoutParams.setMargins(marginLeft, 0, 0, 0);
                imageView.setLayoutParams(layoutParams);
                final String uid = searchModelList.get(i).getId();
                String photoUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), uid);
                ImageDisplayUtils.getInstance().displayRoundedImage(imageView, photoUrl, R.drawable.icon_person_default, this, 15);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        IntentUtils.startActivity(MeetingAddActivity.this, UserInfoActivity.class, bundle);
                    }
                });
                layout.addView(imageView);
            }
            TextView textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#888888"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setText(searchModelList.size() + getString(R.string.schedule_task_a_person));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginLeft = DensityUtil.dip2px(MyApplication.getInstance(), 3);
            layoutParams.setMargins(marginLeft, 0, 0, 0);
            textView.setLayoutParams(layoutParams);
            layout.addView(textView);
        }

    }


    private Meeting getMeeting() {
        Meeting meeting = new Meeting();
        meeting.setOwner(meetingOwner);
        meeting.setTitle(title);
        meeting.setType("meeting");
        meeting.setStartTime(startTimeCalendar.getTimeInMillis());
        meeting.setEndTime(endTimeCalendar.getTimeInMillis());
        meeting.setNote(note);
        meeting.setLocation(location.toJSONObject().toString());
        JSONArray array = new JSONArray();
        try {
            for (SearchModel searchModel : attendeeSearchModelList) {
                JSONObject obj = new JSONObject();
                obj.put("id", searchModel.getId());
                obj.put("name", searchModel.getName());
                obj.put("role", Participant.TYPE_COMMON);
                obj.put("email", searchModel.getEmail());
                array.put(obj);
            }
            for (SearchModel searchModel : recorderSearchModelList) {
                JSONObject obj = new JSONObject();
                obj.put("id", searchModel.getId());
                obj.put("name", searchModel.getName());
                obj.put("email", searchModel.getEmail());
                obj.put("role", Participant.TYPE_RECORDER);
                array.put(obj);
            }
            for (SearchModel searchModel : liaisonSearchModelList) {
                JSONObject obj = new JSONObject();
                obj.put("id", searchModel.getId());
                obj.put("name", searchModel.getName());
                obj.put("email", searchModel.getEmail());
                obj.put("role", Participant.TYPE_CONTACT);
                array.put(obj);
            }
            meeting.setParticipants(array.toString());
            if (remindEvent != null && remindEvent.getAdvanceTimeSpan() != -1) {
                meeting.setRemindEvent(remindEvent.toJSONObject().toString());
            }
            if (isMeetingEditModel) {
                meeting.setId(this.meeting.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return meeting;
    }

    /**
     * 添加或更改会议
     */
    private void addOrUpdateMeeting() {
        Meeting meeting = getMeeting();
        LogUtils.LbcDebug("meeting"+meeting.toString());
        loadingDlg.show();
        if (isMeetingEditModel) {
            apiService.updateMeeting(meeting.toJSONObject().toString());
        } else {
            apiService.addMeeting(meeting.toJSONObject().toString());
        }

    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnAddMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED, null));
            finish();
        }

        @Override
        public void returnAddMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnUpdateMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED, null));
            finish();
        }

        @Override
        public void returnUpdateMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
