package com.inspur.emmcloud.schedule.ui.meeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.DateTimePickerDialog;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.CalendarIdAndCloudIdBean;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.CalendarIdAndCloudScheduleIdCacheUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.SysCalendarAndCloudPlusScheduleSyncUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.GetIDResult;
import com.inspur.emmcloud.schedule.bean.Location;
import com.inspur.emmcloud.schedule.bean.Participant;
import com.inspur.emmcloud.schedule.bean.RemindEvent;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.bean.meeting.MeetingRoom;
import com.inspur.emmcloud.schedule.ui.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.schedule.ui.ScheduleTypeSelectActivity;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

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
@Route(path = Constant.AROUTER_CLASS_APP_NETWORK_DETAIL)
public class ScheduleAddActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE = "extra_schedule_calendar_type";
    //EXTRA_EVENT_TYPE_FROM_MEETING
    public static final String EXTRA_SCHEDULE_CALENDAR_EVENT = "schedule_calendar_event";
    public static final String EXTRA_SCHEDULE_START_TIME = "extra_schedule_start_time";
    public static final String EXTRA_SCHEDULE_END_TIME = "extra_schedule_end_time";
    public static final int REQUEST_SET_SCHEDULE_TYPE = 6;
    public static final String EXTRA_SELECT_CALENDAR = "extra_select_calendar";
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
    @BindView(R.id.switch_sync_calendar)
    Switch syncCalendarSwitch;
    @BindView(R.id.tv_event_type)
    TextView eventTypeText;
    @BindView(R.id.et_meeting_position)
    EditText positionEditText;
    @BindView(R.id.iv_meeting_position_enter)
    ImageView meetingPositionEnterImageView;
    @BindView(R.id.iv_meeting_position_del)
    ImageView meetingPositionDelImageView;



    private LoadingDialog loadingDlg;
    private ScheduleAPIService apiService;
    private Calendar startTimeCalendar; // 开始时间
    private Calendar endTimeCalendar;   //结束时间
    private List<SearchModel> attendeeSearchModelList = new ArrayList<>();  //参会人
    private List<SearchModel> recorderSearchModelList = new ArrayList<>();  //记录人
    private List<SearchModel> liaisonSearchModelList = new ArrayList<>();   //联系人
    private List<SearchModel> originalSearchModelList = new ArrayList<>();   //组织者
    private MeetingRoom meetingRoom;    //会议室
    private Location location;          // 地点
    private RemindEvent remindEvent = new RemindEvent();    // 提醒
    private Schedule schedule = new Schedule();
    private boolean isEventEditModel = false; //是否是编辑模式
    private boolean isFromMeeting = false;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        apiService = new ScheduleAPIService(this);
        apiService.setAPIInterface(new WebService());
        initSchedule();
        initView();
    }


    private void initSchedule() {
        isEventEditModel = getIntent().hasExtra(EXTRA_SCHEDULE_CALENDAR_EVENT);
        if (isEventEditModel) {    //有对象传入时
            schedule = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_EVENT);
            initScheduleDataByEntity();
        } else {
            if (getIntent().hasExtra(EXTRA_SCHEDULE_END_TIME)
                    && getIntent().hasExtra(EXTRA_SCHEDULE_START_TIME)) {
                //会议室来的数据
                Calendar startTimeFromRoomCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_SCHEDULE_START_TIME);
                Calendar endTimeFromRoomCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_SCHEDULE_END_TIME);
                if (getIntent().hasExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM)) {
                    correctMeetingRoomTime(startTimeFromRoomCalendar, endTimeFromRoomCalendar);
                    meetingRoom = (MeetingRoom) getIntent().getSerializableExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM);
                    location = new Location();
                    location.setId(meetingRoom.getId());
                    location.setBuilding(meetingRoom.getBuilding().getName());
                    location.setDisplayName(meetingRoom.getName());
                    schedule.setScheduleCalendar(AccountType.APP_MEETING.toString());
                    schedule.setLocation(JSONUtils.toJSONString(location));
                } else {
                    startTimeCalendar = (Calendar) startTimeFromRoomCalendar.clone();
                    endTimeCalendar = (Calendar) endTimeFromRoomCalendar.clone();
                    schedule.setScheduleCalendar(AccountType.APP_SCHEDULE.toString());
                }
            }

            if (getIntent().hasExtra(EXTRA_SELECT_CALENDAR)) {
                startTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_SELECT_CALENDAR);
                Calendar currentCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
                startTimeCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
                startTimeCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
                startTimeCalendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
                endTimeCalendar = (Calendar) startTimeCalendar.clone();
                endTimeCalendar.set(Calendar.HOUR_OF_DAY, startTimeCalendar.get(Calendar.HOUR_OF_DAY) + 2);
            }

            if (getIntent().hasExtra(Constant.EXTRA_SCHEDULE_TITLE_EVENT)) {     //来自沟通长按
                String title = getIntent().getStringExtra(Constant.EXTRA_SCHEDULE_TITLE_EVENT);
                schedule.setTitle(title);
                schedule.setScheduleCalendar(AccountType.APP_SCHEDULE.toString());
            }

            if (getIntent().hasExtra(EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE)) {
                String intentScheduleCalendar = getIntent().getStringExtra(EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE);
                schedule.setScheduleCalendar(intentScheduleCalendar);
            } else {
                schedule.setScheduleCalendar(AccountType.APP_SCHEDULE.toString());
            }
            //正常创建跳转
            String myUid = BaseApplication.getInstance().getUid();

            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser myInfo;
            if (contactService != null) {
                myInfo = contactService.getContactUserByUid(myUid);
            } else {
                myInfo = new ContactUser();
            }
            SearchModel myInfoSearchModel = new SearchModel(myInfo);
            attendeeSearchModelList.add(myInfoSearchModel);
            startTimeCalendar = startTimeCalendar != null ? startTimeCalendar : TimeUtils.getNextHalfHourTime(Calendar.getInstance());
            if (endTimeCalendar == null) {
                endTimeCalendar = (Calendar) startTimeCalendar.clone();
                endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
            }
            remindEvent.setName(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), schedule.getAllDay()));
        }
    }



    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        schedule.setAllDay(b);
        timeTextChangeByIsAllDay(b);
        remindEvent = new RemindEvent();
        int data = b ? -32400 : 600;
        remindEvent.setAdvanceTimeSpan(data);
        remindEvent.setName(ScheduleAlertTimeActivity.getAlertTimeNameByTime(data, b));
        reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(data, b));//设置提醒
    }

    /**
     * 全天及非全天UI切换
     */
    private void timeTextChangeByIsAllDay(boolean IsAllDay) {
        String startTime = TimeUtils.calendar2FormatString(this, startTimeCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endTime = TimeUtils.calendar2FormatString(this, endTimeCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        startTimeText.setText(IsAllDay ? TimeUtils.getWeekDay(this, startTimeCalendar) : startTime);
        endTimeText.setText(IsAllDay ? TimeUtils.getWeekDay(this, endTimeCalendar) : endTime);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_add;
    }


    /**
     * 设置日程相关数据
     */
    private void initScheduleDataByEntity() {
        location = StringUtils.isBlank(schedule.getLocation()) ? null : new Location(JSONUtils.getJSONObject(schedule.getLocation()));
        List<String> attendeeList = schedule.getGetParticipantList();
        for (int i = 0; i < attendeeList.size(); i++) {
            JSONObject jsonObject = JSONUtils.getJSONObject(attendeeList.get(i));
            String uid = JSONUtils.getString(jsonObject, "id", "");
            SearchModel searchModel = getSearchModel(uid);
            String role = JSONUtils.getString(jsonObject, "role", "");
            if (Participant.TYPE_COMMON.equals(role) || Participant.TYPE_ORGANIZER.equals(role)) {
                attendeeSearchModelList.add(searchModel);
            } else if (Participant.TYPE_CONTACT.equals(role)) {
                liaisonSearchModelList.add(searchModel);
            } else if (Participant.TYPE_RECORDER.equals(role)) {
                recorderSearchModelList.add(searchModel);
            } else if (Participant.TYPE_ORGANIZER.equals(role)) {
                originalSearchModelList.add(searchModel);
            }
        }
        remindEvent = schedule.getRemindEventObj();
        startTimeCalendar = schedule.getStartTimeCalendar();
        endTimeCalendar = schedule.getEndTimeCalendar();
        if (schedule.getAllDay()) {
            endTimeCalendar.add(Calendar.MILLISECOND, -1);
        }
        String alertTimeName = ScheduleAlertTimeActivity.getAlertTimeNameByTime(JSONUtils.getInt(schedule.getRemindEvent(), "advanceTimeSpan", -1), schedule.getAllDay());
        remindEvent = new RemindEvent(JSONUtils.getString(schedule.getRemindEvent(), "remindType", "in_app"),
                JSONUtils.getInt(schedule.getRemindEvent(), "advanceTimeSpan", -1), alertTimeName);
    }


    /**
     * 获取Search Module
     */
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
        newEventTitleText.setText(isEventEditModel ? R.string.schedule_update : R.string.schedule_add);//设置标题
        EditTextUtils.setText(titleEdit, this.schedule.getTitle()); //设置topic
        allDaySwitch.setChecked(schedule.getAllDay()); //设置全天
        notesEdit.setText(!StringUtils.isBlank(schedule.getNote()) ? schedule.getNote() : "");
        positionEditText.setEnabled(!isFromMeeting);
        positionEditText.setText(location != null ? location.getBuilding() + " " + location.getDisplayName() : "");
        showSelectUser(liaisonLayout, liaisonSearchModelList);
        showSelectUser(recorderLayout, recorderSearchModelList);
        showSelectUser(attendeeLayout, attendeeSearchModelList);
        reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), schedule.getAllDay()));//设置提醒
        setMeetingTime();   //设置时间
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(this, schedule.getScheduleCalendar());
        eventTypeText.setText(CalendarUtils.getScheduleCalendarShowName(scheduleCalendar));
        modifyUIByEventType(scheduleCalendar);
        calendarTypeLayout.setClickable(!isEventEditModel);
        allDaySwitch.setOnCheckedChangeListener(this);
        modifyLocationUI();
        //同步日程的选项默认为打开状态
        syncCalendarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                syncCalendarSwitch.isChecked() == isChecked;
                if (isChecked) {
                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(
                            ScheduleAddActivity.this, Permissions.CALENDAR, new PermissionRequestCallback() {
                                @Override
                                public void onPermissionRequestSuccess(List<String> permissions) {
                                }

                                @Override
                                public void onPermissionRequestFail(List<String> permissions) {
                                    syncCalendarSwitch.setChecked(false);
                                }
                            });
                }
            }
        });
        syncCalendarSwitch.setChecked(schedule.getSyncToLocal());
    }

    /**
     * 修改
     */
    private void modifyUIByEventType(ScheduleCalendar scheduleCalendar) {
        AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
        switch (accountType) {
            case APP_MEETING:
                findViewById(R.id.ll_recorder_liaison).setVisibility(View.VISIBLE);
                schedule.setType(Schedule.CALENDAR_TYPE_MEETING);
                if (!isEventEditModel) {
                    schedule.setMeeting(true);
                    schedule.setScheduleCalendar(scheduleCalendar.getId());
                }
                break;
            case EXCHANGE:
                findViewById(R.id.ll_recorder_liaison).setVisibility(View.GONE);
                schedule.setType(Schedule.CALENDAR_TYPE_EXCHANGE);
                recorderSearchModelList.clear();
                liaisonSearchModelList.clear();
                if (!isEventEditModel) {
                    schedule.setScheduleCalendar(scheduleCalendar.getId());
                }
                break;
            case APP_SCHEDULE:
                findViewById(R.id.ll_recorder_liaison).setVisibility(View.GONE);
                schedule.setType(Schedule.CALENDAR_TYPE_MY_CALENDAR);
                recorderSearchModelList.clear();
                liaisonSearchModelList.clear();
                if (!isEventEditModel) {
                    schedule.setMeeting(false);
                    schedule.setScheduleCalendar(scheduleCalendar.getId());
                }
                break;
        }
    }


    /**
     * 地点逻辑修改地点UI
     */
    private void modifyLocationUI() {
        String positionStr = "";
        if (location != null) {
            positionStr = location.getBuilding() + " " + location.getDisplayName();
            positionStr = StringUtils.isBlank(positionStr) ? "" : positionStr;
        } else {
            positionStr = "";
        }
        positionEditText.setText(positionStr);
        meetingPositionDelImageView.setVisibility(location != null && !StringUtils.isBlank(location.getId()) ? View.VISIBLE : View.GONE);
        meetingPositionEnterImageView.setVisibility(location != null && !StringUtils.isBlank(location.getId()) ? View.GONE : View.VISIBLE);
        if ((location != null && !StringUtils.isBlank(location.getId()))) {
            positionEditText.setEnabled(false);
        } else {
            positionEditText.setEnabled(true);
        }

    }


    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ibt_back) {
            finish();
        } else if (i == R.id.tv_save) {
            if (!isInputValid())
                return;
            Schedule schedule = getScheduleEvent();
            if (isEventEditModel) {
                updateSchedule(schedule);
            } else {
                //添加日程时因为此时日程并未真正创建，没有日程id所以等日程创建完成在存储
                addSchedule(schedule);
            }
        } else if (i == R.id.ll_start_time) {
            InputMethodUtils.hide(ScheduleAddActivity.this);
            showTimeSelectDialog(true);
        } else if (i == R.id.ll_end_time) {
            InputMethodUtils.hide(ScheduleAddActivity.this);
            showTimeSelectDialog(false);
        } else if (i == R.id.iv_meeting_position_enter) {
            Intent intent = new Intent(this, MeetingRoomListActivity.class);
            intent.putExtra(MeetingRoomListActivity.EXTRA_START_TIME, startTimeCalendar);
            intent.putExtra(MeetingRoomListActivity.EXTRA_END_TIME, endTimeCalendar);
            startActivityForResult(intent, REQUEST_SELECT_MEETING_ROOM);
        } else if (i == R.id.ll_add_attendee) {
            selectContact(REQUEST_SELECT_ATTENDEE);
        } else if (i == R.id.ll_add_recorder) {
            selectContact(REQUEST_SELECT_RECORDER);
        } else if (i == R.id.ll_add_liaison) {
            selectContact(REQUEST_SELECT_LIAISON);
        } else if (i == R.id.ll_reminder) {
            setReminder();
        } else if (i == R.id.rl_calendar_type) {
            Intent intent3 = new Intent(this, ScheduleTypeSelectActivity.class);
            intent3.putExtra(ScheduleTypeSelectActivity.SCHEDULE_AC_TYPE, this.schedule.getScheduleCalendar());
            startActivityForResult(intent3, REQUEST_SET_SCHEDULE_TYPE);
        } else if (i == R.id.iv_meeting_position_del) {
            location = null;
            modifyLocationUI();
        }
    }


    /**
     * 判定当前是否有效
     */
    private boolean isInputValid() {
        String title = titleEdit.getText().toString().trim();
        String meetingPosition = positionEditText.getText().toString().trim();
        if (StringUtils.isBlank(title)) {
            ToastUtils.show(BaseApplication.getInstance(), R.string.meeting_room_booking_topic);
            return false;
        }
        if (StringUtils.isBlank(meetingPosition) && schedule.getScheduleCalendar().equals(AccountType.APP_MEETING.toString())) {
            ToastUtils.show(BaseApplication.getInstance(), R.string.meeting_room_booking_choosing_room);
            return false;
        }


        if (attendeeSearchModelList.size() == 0 && schedule.getScheduleCalendar().equals(AccountType.APP_MEETING.toString())) {
            ToastUtils.show(BaseApplication.getInstance(), R.string.meeting_invating_members);
            return false;
        }
        if (title.length() > 149) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.meeting_topic_too_long));
            return false;
        }
        String note = notesEdit.getText().toString().trim();
        if (!StringUtils.isBlank(note) && note.length() > 499) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.meeting_notice_too_long));
            return false;
        }

        if (startTimeCalendar.after(endTimeCalendar)) {
            ToastUtils.show(ScheduleAddActivity.this, R.string.calendar_start_or_end_time_illegal);
            return false;
        }

        if (endTimeCalendar.before(Calendar.getInstance()) && schedule.getScheduleCalendar().equals(AccountType.APP_MEETING.toString())) {
            ToastUtils.show(ScheduleAddActivity.this, R.string.calendar_end_time_no_before_current);
            return false;
        }

        int countHour = TimeUtils.getCeil(endTimeCalendar, startTimeCalendar);
        if (meetingRoom != null && countHour > Integer.parseInt(meetingRoom.getMaxDuration()) && location != null) {
            ToastUtils.show(ScheduleAddActivity.this, getString(R.string.meeting_more_than_max_time));
            return false;
        }
        if (location == null && schedule.getScheduleCalendar().equals(AccountType.APP_MEETING.toString())) {
            location = new Location();
        }

        return true;
    }

    /**
     * 设置提醒*/
    private void setReminder() {
        Intent intent = new Intent(this, ScheduleAlertTimeActivity.class);
        int advanceTimeSpan = -1;
        if (remindEvent != null) {
            advanceTimeSpan = remindEvent.getAdvanceTimeSpan();
        }
        intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, advanceTimeSpan);
        intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_IS_ALL_DAY, schedule.getAllDay());
        startActivityForResult(intent, REQUEST_SET_REMIND_EVENT);
    }


    /**
     * 选择参会人*/
    private void selectContact(int requestCode) {
        String title = "";

        Bundle bundle = new Bundle();
        bundle.putInt("select_content", 2);
        bundle.putBoolean("isMulti_select", true);
        bundle.putBoolean("isContainMe", true);
        if (requestCode == REQUEST_SELECT_ATTENDEE) {
            title = getString(R.string.schedule_meeting_select_attendee_title);
            bundle.putSerializable("hasSearchResult", (Serializable) attendeeSearchModelList);
        } else if (requestCode == REQUEST_SELECT_RECORDER) {
            title = getString(R.string.schedule_meeting_select_record_holder_title);
            bundle.putSerializable("hasSearchResult", (Serializable) recorderSearchModelList);
        } else {
            title = getString(R.string.schedule_meeting_select_conference_title);
            bundle.putSerializable("hasSearchResult", (Serializable) liaisonSearchModelList);
        }
        bundle.putString("title", title);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH).with(bundle).navigation(ScheduleAddActivity.this, requestCode);
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
                    } else {
                        endTimeCalendar = (Calendar) calendar.clone();
                    }
                }
                setMeetingTime();

            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        startDataTimePickerDialog.showDatePickerDialog(schedule.getAllDay(), isStartTime ? startTimeCalendar : endTimeCalendar);
    }


    /**
     * 显示开始和结束时间
     */
    private void setMeetingTime() {
        startDateText.setText(TimeUtils.calendar2FormatString(BaseApplication.getInstance(), startTimeCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY));
        endDateText.setText(TimeUtils.calendar2FormatString(BaseApplication.getInstance(), endTimeCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY));
        timeTextChangeByIsAllDay(schedule.getAllDay());
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
            if (searchModelList != null && searchModelList.size() > 0) {
                searchModelList = getSearchModelListHaveEmail(searchModelList);
            }
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
                    positionEditText.setText(meetingRoom.getBuilding().getName() + " " + meetingRoom.getName());
                    location = new Location();
                    location.setId(meetingRoom.getId());
                    location.setBuilding(meetingRoom.getBuilding().getName());
                    location.setDisplayName(meetingRoom.getName());
                    modifyLocationUI();
                    break;
                case REQUEST_SET_REMIND_EVENT:
                    remindEvent = (RemindEvent) data.getSerializableExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), schedule.getAllDay()));
                    break;
                case REQUEST_SET_SCHEDULE_TYPE:
                    ScheduleCalendar scheduleCalendar = (ScheduleCalendar) data.getSerializableExtra(ScheduleTypeSelectActivity.SCHEDULE_AC_TYPE);
                    eventTypeText.setText(CalendarUtils.getScheduleCalendarShowName(scheduleCalendar));
                    modifyUIByEventType(scheduleCalendar);
                    modifyLocationUI();
                    break;
            }
        }

    }


    /**
     *添加邮箱到联络人*/
    List<SearchModel> getSearchModelListHaveEmail(List<SearchModel> searchModelList) {
        List<SearchModel> searchModelList1 = new ArrayList<>();
        for (int i = 0; i < searchModelList.size(); i++) {
            SearchModel searchModel = searchModelList.get(i);
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser contactUser;
            if (contactService != null) {
                contactUser = contactService.getContactUserByUid(searchModelList.get(i).getId());
            } else {
                contactUser = null;
            }
            if (contactUser != null) {
                searchModel.setEmail(contactUser.getEmail());
            }
            searchModelList1.add(searchModel);
        }
        return searchModelList1;
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
                int width = DensityUtil.dip2px(BaseApplication.getInstance(), 30);
                int marginLeft = DensityUtil.dip2px(BaseApplication.getInstance(), 10);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
                layoutParams.setMargins(marginLeft, 0, 0, 0);
                imageView.setLayoutParams(layoutParams);
                final String uid = searchModelList.get(i).getId();
                String photoUrl = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), uid);
                ImageDisplayUtils.getInstance().displayRoundedImage(imageView, photoUrl, R.drawable.icon_person_default, this, 15);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle).navigation(ScheduleAddActivity.this);
                    }
                });
                layout.addView(imageView);
            }
            TextView textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#888888"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setText(searchModelList.size() + getString(R.string.schedule_task_a_person));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginLeft = DensityUtil.dip2px(BaseApplication.getInstance(), 3);
            layoutParams.setMargins(marginLeft, 0, 0, 0);
            textView.setLayoutParams(layoutParams);
            layout.addView(textView);
        }

    }


    /**
     * 上传数据前获取对象组织数据
     */
    private Schedule getScheduleEvent() {
        schedule.setOwner(StringUtils.isBlank(schedule.getOwner()) ? BaseApplication.getInstance().getUid() : schedule.getOwner());
        schedule.setTitle(titleEdit.getText().toString());
        correctedCalendarTime();
        schedule.setStartTime(startTimeCalendar.getTimeInMillis());
        schedule.setEndTime(endTimeCalendar.getTimeInMillis());
        schedule.setNote(notesEdit.getText().toString().trim());
        schedule.setLocation((location != null && !StringUtils.isBlank(location.getId())) ? JSONUtils.toJSONString(location) :
                JSONUtils.toJSONString(new Location("", "", positionEditText.getText().toString())));
        schedule.setSyncToLocal(syncCalendarSwitch.isChecked());
        JSONArray array = new JSONArray();
        try {
            for (SearchModel searchModel : attendeeSearchModelList) {
                JSONObject obj = new JSONObject();
//                for(SearchModel searchSubModel:originalSearchModelList){
//                    if(searchModel.getId().equals(searchSubModel.getId())){
//
//                    }
//                }
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
            schedule.setParticipants(array.toString());
            if (remindEvent != null) {
                schedule.setRemindEvent(JSONUtils.toJSONString(remindEvent));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * 更新日程
     */
    private void updateSchedule(Schedule schedule) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            schedule.setLastTime(System.currentTimeMillis());
            apiService.updateSchedule(schedule.toCalendarEventJSONObject().toString(), this.schedule);
        }
    }

    /**
     * 添加日程
     */
    private void addSchedule(Schedule schedule) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            try {
                loadingDlg.show();
                apiService.addSchedule(schedule.toCalendarEventJSONObject().toString(),
                        ScheduleCalendarCacheUtils.getScheduleCalendar(this, schedule.getScheduleCalendar()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上传日历时间秒毫秒单位清零矫正，allday 重设时间
     */
    private void correctedCalendarTime() {
        if (schedule.getAllDay()) {
            startTimeCalendar = TimeUtils.getDayBeginCalendar(startTimeCalendar);
            endTimeCalendar = TimeUtils.getDayEndCalendar(endTimeCalendar);
        } else {
            startTimeCalendar.set(Calendar.SECOND, 0);
            startTimeCalendar.set(Calendar.MILLISECOND, 0);
            endTimeCalendar.set(Calendar.SECOND, 0);
            endTimeCalendar.set(Calendar.MILLISECOND, 0);
        }
    }

    /**
     * 发送CalEvent变化通知
     *
     * @param
     */
    public void sendCalendarEventNotification() {
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, ""));
    }


    private class WebService extends ScheduleAPIInterfaceImpl {

        @Override
        public void returnAddScheduleSuccess(GetIDResult getIDResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(getApplicationContext(), R.string.calendar_add_success);
            schedule.setId(getIDResult.getId());
            //向系统日历里添加日历时需要判断同步开关是否打开，打开的时候则添加，不打开则不不管
            if (syncCalendarSwitch.isChecked()) {
                try {
                    JSONObject jsonObject = SysCalendarAndCloudPlusScheduleSyncUtils.saveCloudSchedule(ScheduleAddActivity.this,
                        schedule.getTitle(), schedule.getNote(), schedule.getStartTime()
                            , schedule.getEndTime(), schedule.getId(), schedule.getAllDay(), schedule.getRemindEventObj().getAdvanceTimeSpan() / 60);
                    CalendarIdAndCloudScheduleIdCacheUtils.saveCalendarIdAndCloudIdBean(
                            ScheduleAddActivity.this, new CalendarIdAndCloudIdBean(jsonObject.
                                    getString(CalendarIdAndCloudIdBean.CLOUD_PLUS_CALENDAR_ID), getIDResult.getId(),
                                    jsonObject.getString(CalendarIdAndCloudIdBean.CLOUD_PLUS_SCHEDULE_CALENDAR_ID)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendCalendarEventNotification();
            finish();
        }

        @Override
        public void returnAddScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ScheduleAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateScheduleSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            //更新日历时如果同步开关开着则同步数据，否则关掉
            if (syncCalendarSwitch.isChecked()) {
                CalendarIdAndCloudIdBean calendarIdAndCloudIdBean = CalendarIdAndCloudScheduleIdCacheUtils.
                        getCalendarIdByCloudScheduleId(BaseApplication.getInstance(), schedule.getId());
                if (calendarIdAndCloudIdBean == null) {
                    JSONObject jsonObject = SysCalendarAndCloudPlusScheduleSyncUtils.saveCloudSchedule(ScheduleAddActivity.this,
                            schedule.getTitle(), schedule.getNote(), schedule.getStartTime()
                            , schedule.getEndTime(), schedule.getId(), schedule.getAllDay(), schedule.getRemindEventObj().getAdvanceTimeSpan() / 60);
                    try {
                        CalendarIdAndCloudScheduleIdCacheUtils.saveCalendarIdAndCloudIdBean(
                                ScheduleAddActivity.this, new CalendarIdAndCloudIdBean(jsonObject.getString(CalendarIdAndCloudIdBean.CLOUD_PLUS_CALENDAR_ID), schedule.getId(),
                                        jsonObject.getString(CalendarIdAndCloudIdBean.CLOUD_PLUS_SCHEDULE_CALENDAR_ID)));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    SysCalendarAndCloudPlusScheduleSyncUtils.updateCloudScheduleInSysCalendar(ScheduleAddActivity.this,
                            schedule.getTitle(), schedule.getNote(), schedule.getStartTime()
                            , schedule.getEndTime(), schedule.getId(), schedule.getAllDay(), (schedule.getRemindEventObj().getAdvanceTimeSpan() / 60));
                }

            } else {
                SysCalendarAndCloudPlusScheduleSyncUtils.deleteCalendarEvent(ScheduleAddActivity.this, schedule.getId());
            }
            sendCalendarEventNotification();
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.modify_success));
            //更新系日历事件
            finish();
        }

        @Override
        public void returnUpdateScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ScheduleAddActivity.this, error, errorCode);
        }


    }
}
