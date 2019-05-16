package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.RemindEvent;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.DateTimePickerDialog;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/9.
 */

@ContentView(R.layout.activity_meeting_add)
public class MeetingAddActivity extends BaseActivity {
    private static final int REQUEST_SELECT_ATTENDEE = 1;
    private static final int REQUEST_SELECT_RECORDER = 2;
    private static final int REQUEST_SELECT_LIAISON = 3;
    private static final int REQUEST_SELECT_MEETING_ROOM = 4;
    private static final int REQUEST_SET_REMIND_EVENT = 5;
    @ViewInject(R.id.et_title)
    private EditText titleEdit;
    @ViewInject(R.id.tv_start_date)
    private TextView startDateText;
    @ViewInject(R.id.tv_start_time)
    private TextView startTimeText;
    @ViewInject(R.id.tv_end_date)
    private TextView endDateText;
    @ViewInject(R.id.tv_end_time)
    private TextView endTimeText;
    @ViewInject(R.id.et_meeting_position)
    private ClearEditText meetingPositionEdit;
    @ViewInject(R.id.ll_attendee)
    private LinearLayout attendeeLayout;
    @ViewInject(R.id.ll_recorder)
    private LinearLayout recorderLayout;
    @ViewInject(R.id.ll_liaison)
    private LinearLayout liaisonLayout;
    @ViewInject(R.id.et_notes)
    private EditText notesEdit;
    @ViewInject(R.id.tv_reminder)
    private TextView reminderText;
    private LoadingDialog loadingDlg;
    private ScheduleApiService apiService;
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    private boolean isAllDay = false;
    private List<SearchModel> attendeeSearchModelList = new ArrayList<>();
    private List<SearchModel> recorderSearchModelList = new ArrayList<>();
    private List<SearchModel> liaisonSearchModelList = new ArrayList<>();
    private MeetingRoom meetingRoom;
    private Location location;
    private String title;
    private String note;
    private String meetingPosition;
    private RemindEvent remindEvent;
    private Meeting meeting = new Meeting();
    private boolean isMeetingEditModel = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(new WebService());
        isMeetingEditModel = getIntent().hasExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY);
        if (isMeetingEditModel) {
            meeting = (Meeting) getIntent().getSerializableExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY);
            location = new Location(JSONUtils.getJSONObject(meeting.getLocation()));
            startTimeCalendar = meeting.getStartTimeCalendar();
            endTimeCalendar = meeting.getEndTimeCalendar();
            title = meeting.getTitle();
            note = meeting.getNote();
            List<String> attendeeList = meeting.getGetParticipantList();
            for (int i = 0; i < attendeeList.size(); i++) {
                JSONObject jsonObject = JSONUtils.getJSONObject(attendeeList.get(i));
                SearchModel searchModel = new SearchModel();
                searchModel.setId(JSONUtils.getString(jsonObject, "id", ""));
                searchModel.setName(JSONUtils.getString(jsonObject, "name", ""));
                if (Participant.TYPE_COMMON.equals(JSONUtils.getString(jsonObject, "role", ""))) {
                    attendeeSearchModelList.add(searchModel);
                } else if (Participant.TYPE_CONTACT.equals(JSONUtils.getString(jsonObject, "role", ""))) {
                    liaisonSearchModelList.add(searchModel);
                } else if (Participant.TYPE_RECORDER.equals(JSONUtils.getString(jsonObject, "role", ""))) {
                    recorderSearchModelList.add(searchModel);
                }
            }
            remindEvent = meeting.getRemindEventObj();
        } else {
            String myUid= MyApplication.getInstance().getUid();
            SearchModel myInfoSearchModel = new SearchModel();
            ContactUser myInfo = ContactUserCacheUtils.getContactUserByUid(myUid);
            myInfoSearchModel.setName(myInfo.getName());
            myInfoSearchModel.setId(myUid);
            attendeeSearchModelList.add(myInfoSearchModel);
            startTimeCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
            endTimeCalendar = (Calendar) startTimeCalendar.clone();
            endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
            if(getIntent().hasExtra(MeetingRoomListActivity.EXTRA_START_TIME)
                    && getIntent().hasExtra(MeetingRoomListActivity.EXTRA_END_TIME)
                    && getIntent().hasExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM)){
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
     * 初始化视图
     */
    private void initView() {
        loadingDlg = new LoadingDialog(this);
        if (isMeetingEditModel) {
            titleEdit.setText(title);
            meetingPositionEdit.setText(location.getDisplayName());
            notesEdit.setText(note);
            showSelectUser(liaisonLayout, liaisonSearchModelList);
            showSelectUser(recorderLayout, recorderSearchModelList);
            reminderText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), isAllDay));
        }else if(location != null){
            meetingPositionEdit.setText(location.getDisplayName());
        }
        showSelectUser(attendeeLayout, attendeeSearchModelList);
        setMeetingTime();
        getIsMeetingAdmin();
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
        }
    }


    private boolean isInputValid() {
        title = titleEdit.getText().toString();
        meetingPosition = meetingPositionEdit.getText().toString();
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
        if (title.length() > 128) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.meeting_topic_too_long));
            return false;
        }
        note = notesEdit.getText().toString();
        if (!StringUtils.isBlank(note) && note.length() > 127) {
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

        int count = TimeUtils.getCountdownNum(endTimeCalendar);
        if (meetingRoom != null && count >= meetingRoom.getMaxAhead()) {
            ToastUtils.show(MeetingAddActivity.this, getString(R.string.meeting_more_than_max_day));
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

        if (!location.getDisplayName().equals(meetingPosition)) {
            location.setDisplayName(meetingPosition);
            location.setId("");
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
        new QMUIDialog.MessageDialogBuilder(this).setMessage(R.string.schedule_calendar_time_alert)
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog qmuiDialog, int i) {
                        qmuiDialog.dismiss();
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
                    meetingPositionEdit.setText(meetingRoom.getName());
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


    private Meeting getMeeting(){
        Meeting meeting = new Meeting();
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
                array.put(obj);
            }
            for (SearchModel searchModel : recorderSearchModelList) {
                JSONObject obj = new JSONObject();
                obj.put("id", searchModel.getId());
                obj.put("name", searchModel.getName());
                obj.put("role", Participant.TYPE_RECORDER);
                array.put(obj);
            }
            for (SearchModel searchModel : liaisonSearchModelList) {
                JSONObject obj = new JSONObject();
                obj.put("id", searchModel.getId());
                obj.put("name", searchModel.getName());
                obj.put("role", Participant.TYPE_CONTACT);
                array.put(obj);
            }
            meeting.setParticipants(array.toString());
            if (remindEvent != null && remindEvent.getAdvanceTimeSpan() != -1) {
                meeting.setRemindEvent(remindEvent.toJSONObject().toString());
            }
            if (isMeetingEditModel){
                meeting.setId(this.meeting.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  meeting;
    }

    /**
     * 添加或更改会议
     */
    private void addOrUpdateMeeting() {
        Meeting meeting = getMeeting();
        loadingDlg.show();
        if (isMeetingEditModel) {
            apiService.updateMeeting(meeting.toJSONObject().toString());
        } else {
            apiService.addMeeting(meeting.toJSONObject().toString());
        }

    }

    /**
     * 判断当前用户是否会议室管理员
     */
    private void getIsMeetingAdmin() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.getIsMeetingAdmin(MyApplication.getInstance().getUid());
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {
            LoadingDialog.dimissDlg(loadingDlg);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                    getIsAdmin.isAdmin());
        }

        @Override
        public void returnIsMeetingAdminFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
        }

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
