package com.inspur.emmcloud.ui.work.meeting;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 会议室预定Activity
 */
public class MeetingBookingActivity extends BaseActivity {

    private static final int SELECT_MEETING_ROOM = 0;
    private static final int MEETTING_CHOOSE_MEM = 1;
    private static final int MEETTING_LOG_MEM = 2;
    private static final int MEETING_BEGIN_TIME = 3;
    private static final int MEETING_END_TIME = 4;
    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private TextView meetingRoomText, meetingBeginDateText, meetingBeginTimeText, meetingEndDateText, meetingEndTimeText;
    private EditText topicEdit, noticeEdit;
    private String meetingRoomId = "";
    private Calendar meetingBeginCalendar = Calendar.getInstance();
    private Calendar meetingEndCalendar = Calendar.getInstance();
    private String meetingRoomName = "";
    private String meetingRoomFlour = "";
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private CircleTextImageView[] circleHeadImageView = new CircleTextImageView[5];
    private String userId = "";
    private int maxAhead = 0;
    private int maxDuration = 0;
    private boolean isSetTime = false;//用户是否人为的修改过时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_booking);
        initViews();
        getIsAdmin();
    }

    /**
     * 判断当前用户是否会议室管理员，并存储
     */
    private void getIsAdmin() {
        if (NetUtils.isNetworkConnected(MeetingBookingActivity.this)) {
            loadingDlg.show();
            apiService.getIsAdmin(userId);
        }
    }

    /**
     * 初始化views
     */
    public void initViews() {
        apiService = new WorkAPIService(this);
        apiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(this);
        meetingRoomText = (TextView) findViewById(R.id.meeting_booking_room_name_text);
        topicEdit = (EditText) findViewById(R.id.meeting_booking_content_text);
        noticeEdit = (EditText) findViewById(R.id.meeting_booking_notice_text);
        meetingBeginDateText = (TextView) findViewById(R.id.meeting_booking_time_content_text);
        meetingBeginTimeText = (TextView) findViewById(R.id.meeting_booking_begin_time_text);
        meetingEndDateText = (TextView) findViewById(R.id.meeting_booking_end_time_date_text);
        meetingEndTimeText = (TextView) findViewById(R.id.meeting_booking_end_time_text);
        circleHeadImageView[0] = (CircleTextImageView) findViewById(R.id.meeting_booking_member_head_img5);
        circleHeadImageView[1] = (CircleTextImageView) findViewById(R.id.meeting_booking_member_head_img4);
        circleHeadImageView[2] = (CircleTextImageView) findViewById(R.id.meeting_booking_member_head_img3);
        circleHeadImageView[3] = (CircleTextImageView) findViewById(R.id.meeting_booking_member_head_img2);
        circleHeadImageView[4] = (CircleTextImageView) findViewById(R.id.meeting_booking_member_head_img1);
        userId = ((MyApplication) getApplication())
                .getUid();
        meetingBeginCalendar = TimeUtils.getNextHalfHourTime(meetingBeginCalendar);
        meetingEndCalendar = TimeUtils.getNextHalfHourTime(meetingEndCalendar);
        meetingEndCalendar.add(Calendar.HOUR_OF_DAY, 2);
        meetingBeginDateText.setText(TimeUtils.calendar2FormatString(
                MeetingBookingActivity.this, meetingBeginCalendar, TimeUtils.FORMAT_MONTH_DAY));
        meetingBeginTimeText.setText(TimeUtils
                .calendar2FormatString(MeetingBookingActivity.this, meetingBeginCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
        meetingEndDateText.setText(TimeUtils.calendar2FormatString(
                MeetingBookingActivity.this, meetingEndCalendar, TimeUtils.FORMAT_MONTH_DAY));
        meetingEndTimeText.setText(TimeUtils
                .calendar2FormatString(MeetingBookingActivity.this, meetingEndCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.meeting_booking_time_content_text:
            case R.id.meeting_booking_end_time_date_text:
                showDatePickerDlg();
                break;
            case R.id.meeting_booking_begin_time_text:
                showTimePickerDlg(meetingBeginCalendar, MEETING_BEGIN_TIME);
                break;
            case R.id.meeting_booking_end_time_text:
                showTimePickerDlg(meetingEndCalendar, MEETING_END_TIME);
                break;
            case R.id.meeting_booking_member_layout:
            case R.id.meeting_booking_member_content_layout:
                startContactSearchActivity(true, R.string.meeting_invating_members,
                        MEETTING_CHOOSE_MEM);
                break;
            case R.id.meeting_booking_log_layout:
            case R.id.meeting_booking_log_text:
            case R.id.meeting_booking_member_log_layout:
                startContactSearchActivity(false,
                        R.string.meeting_choose_log_members, MEETTING_LOG_MEM);
                break;
            case R.id.meeting_booking_content_text:
            case R.id.meeting_booking_notice_text:
                v.setFocusableInTouchMode(true);
                v.setFocusable(true);
                Editable editable = ((EditText) v).getEditableText();
                Selection.setSelection(editable, editable.length());
                break;
            case R.id.meeting_booking_cirform_text:
                String topic = topicEdit.getText().toString();
                if (StringUtils.isBlank(topic)) {
                    ToastUtils.show(MeetingBookingActivity.this,
                            getString(R.string.meeting_room_booking_topic));
                    break;
                }
                String notice = noticeEdit.getText().toString();
                if (meetingBeginCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    ToastUtils.show(MeetingBookingActivity.this, R.string.meeting_room_time_late);
                    break;
                }
                if (meetingBeginCalendar.after(meetingEndCalendar)) {
                    ToastUtils.show(MeetingBookingActivity.this, R.string.calendar_start_or_end_time_illegal);
                    break;
                }
                if (StringUtils.isBlank(meetingRoomId)) {
                    ToastUtils.show(MeetingBookingActivity.this,
                            getString(R.string.meeting_room_booking_choosing_room));
                    break;
                }
                if (selectMemList.size() == 0) {
                    ToastUtils.show(MeetingBookingActivity.this,
                            getString(R.string.meeting_invating_members));
                    break;
                }
                if (topic.length() > 128) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.meeting_topic_too_long));
                    break;
                }
                if (!StringUtils.isBlank(notice) && notice.length() > 127) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.meeting_notice_too_long));
                    break;
                }
                int count = TimeUtils.getCountdownNum(meetingEndCalendar);
                if (count >= maxAhead) {
                    ToastUtils.show(MeetingBookingActivity.this, getString(R.string.meeting_more_than_max_day));
                    break;
                }
                int countHour = TimeUtils.getCeil(meetingEndCalendar, meetingBeginCalendar);
                if (countHour > maxDuration) {
                    ToastUtils.show(MeetingBookingActivity.this, getString(R.string.meeting_more_than_max_time));
                    break;
                }
                // 设置预订时间为整分钟
                meetingBeginCalendar.set(Calendar.SECOND, 0);
                meetingBeginCalendar.set(Calendar.MILLISECOND, 0);
                meetingEndCalendar.set(Calendar.SECOND, 0);
                meetingEndCalendar.set(Calendar.MILLISECOND, 0);
                bookMeeting(topic, notice);
                break;
            case R.id.ibt_back:
                finish();
                break;
            case R.id.meeting_booking_room_layout:
            case R.id.meeting_booking_room_name_text:
                Intent intent = new Intent();
                intent.setClass(MeetingBookingActivity.this,
                        MeetingRoomListActivity.class);
                if (isSetTime) {
                    intent.putExtra("filterBeginCalendar", meetingBeginCalendar.getTimeInMillis());
                    intent.putExtra("filterEndCalendar", meetingEndCalendar.getTimeInMillis());
                }
                startActivityForResult(intent, SELECT_MEETING_ROOM);
                break;
            default:
                break;
        }
    }

    /**
     * 弹出时间选择Dialog
     *
     * @param calendar
     * @param beginOrEnd
     */
    private void showTimePickerDlg(Calendar calendar, final int beginOrEnd) {
        TimePickerDialog beginTimePickerDialog = new TimePickerDialog(
                MeetingBookingActivity.this, new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                isSetTime = true;
                if (beginOrEnd == MEETING_BEGIN_TIME) {
                    meetingBeginCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    meetingBeginCalendar.set(Calendar.MINUTE, minute);
                    meetingBeginTimeText.setText(TimeUtils.Calendar2TimeString(meetingBeginCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE));
                    meetingEndCalendar = (Calendar) meetingBeginCalendar.clone();
                    handleEndCalendar();
                    meetingEndTimeText.setText(TimeUtils.calendar2FormatString(
                            MeetingBookingActivity.this, meetingEndCalendar,
                            TimeUtils.FORMAT_HOUR_MINUTE));
                } else if (beginOrEnd == MEETING_END_TIME) {
                    meetingEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    meetingEndCalendar.set(Calendar.MINUTE, minute);
                    meetingEndTimeText.setText(TimeUtils.Calendar2TimeString(meetingEndCalendar, TimeUtils.DATE_FORMAT_HOUR_MINUTE));
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        beginTimePickerDialog.show();
    }

    /**
     * 是否增加两个小时
     */
    private void handleEndCalendar() {
        meetingEndCalendar.add(Calendar.HOUR_OF_DAY, 2);
        if (!TimeUtils.isSameDay(meetingBeginCalendar, meetingEndCalendar)) {
            int day = meetingBeginCalendar.get(Calendar.DAY_OF_MONTH);
            meetingEndCalendar.set(Calendar.DAY_OF_MONTH, day);
            meetingEndCalendar.set(Calendar.HOUR_OF_DAY, 23);
            meetingEndCalendar.set(Calendar.MINUTE, 59);
        }
    }

    /**
     * 弹出日期选择Dialog
     */
    private void showDatePickerDlg() {
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(
                MeetingBookingActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                isSetTime = true;
                meetingBeginCalendar.set(year, monthOfYear, dayOfMonth);
                meetingEndCalendar.set(year, monthOfYear, dayOfMonth);
                meetingBeginDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingBeginCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                meetingEndDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingEndCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
            }
        }, meetingBeginCalendar.get(Calendar.YEAR), meetingBeginCalendar.get(Calendar.MONTH), meetingBeginCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * 预定会议
     *
     * @param topic
     * @param notice
     */
    private void bookMeeting(String topic, String notice) {
        if (NetUtils.isNetworkConnected(MeetingBookingActivity.this)) {
            loadingDlg.show();
            apiService.getBookingRoom(topic.trim(), meetingRoomId, meetingRoomFlour
                            + " " + meetingRoomName, 0, notice, meetingBeginCalendar.getTimeInMillis(),
                    meetingEndCalendar.getTimeInMillis(), userId, null, selectMemList, "");
        }
    }

    /**
     * 打开选择参会人员和会务人员界面
     */
    private void startContactSearchActivity(boolean isMultiSelect, int titleId,
                                            int requestCode) {
        Intent intent = new Intent();
        intent.putExtra("select_content", 2);
        intent.putExtra("isMulti_select", isMultiSelect);
        intent.putExtra("isContainMe", true);
        intent.putExtra("title", getString(titleId));
        if (selectMemList != null && isMultiSelect) {
            intent.putExtra("hasSearchResult", (Serializable) selectMemList);
        }
        intent.setClass(getApplicationContext(), ContactSearchActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_MEETING_ROOM) {
                getRoomAvailableTime(data);
            } else if (requestCode == MEETTING_CHOOSE_MEM) {
                chooseMember(data);
            } else if (requestCode == MEETTING_LOG_MEM) {
                //选择会务人员逻辑，暂时去掉
            }
        }
    }

    /**
     * 选择参会人员
     *
     * @param data
     */
    private void chooseMember(Intent data) {
        if (data.getExtras().containsKey("selectMemList")) {
            selectMemList = (List<SearchModel>) data.getExtras()
                    .getSerializable("selectMemList");
        }
        hideAllHeadImage();
        showMembers();
    }

    /**
     * 隐藏所有头像
     */
    private void hideAllHeadImage() {
        for (int i = 0; i < 5; i++) {
            circleHeadImageView[i].setVisibility(View.GONE);
        }
    }

    /**
     * 显示成员头像，这里残留对群组的识别和操作
     */
    private void showMembers() {
        int count = 0;
        if (selectMemList.size() > 5) {
            count = 5;
        } else {
            count = selectMemList.size();
        }
        for (int i = 0; i < count; i++) {
            circleHeadImageView[i].setVisibility(View.VISIBLE);
            SearchModel searchModel = selectMemList.get(i);
            String icon = APIUri.getChannelImgUrl(MeetingBookingActivity.this, searchModel.getId());
            int defaultIcon = -1;
            if (searchModel.getType().equals("GROUP")) {
                File file = new File(MyAppConfig.LOCAL_CACHE_PATH, MyApplication.getInstance().getTanent()
                        + searchModel.getId() + "_100.png1");
                if (file.exists()) {
                    icon = "file://" + file.getAbsolutePath();
                }
                defaultIcon = R.drawable.icon_channel_group_default;
            } else {
                defaultIcon = R.drawable.icon_person_default;
            }
            ImageDisplayUtils.getInstance().displayImage(
                    circleHeadImageView[count - i - 1], icon, defaultIcon);

        }
    }

    /**
     * 获取房间的可用时间
     *
     * @param data
     */
    private void getRoomAvailableTime(Intent data) {
        meetingRoomName = data.getStringExtra("room");
        meetingRoomFlour = data.getStringExtra("flour");
        meetingRoomId = data.getStringExtra("roomid");
        String maxAheads = data.getStringExtra("maxAhead");
        if (!StringUtils.isBlank(maxAheads)) {
            maxAhead = Integer.parseInt(maxAheads);
        }
        String maxDurations = data.getStringExtra("maxDuration");
        if (!StringUtils.isBlank(data.getStringExtra("maxDuration"))) {
            maxDuration = Integer.parseInt(maxDurations);
        }
        if (StringUtils.isEmpty(meetingRoomName)) {
            meetingRoomText.setText("");
        } else {
            meetingRoomText.setText(meetingRoomFlour + " " + meetingRoomName);
        }
        if (data.hasExtra("beginTime")) {
            isSetTime = true;
            long fromTime = data.getLongExtra("beginTime", 0);
            if (fromTime != 0) {
                meetingBeginCalendar.setTimeInMillis(fromTime);
                meetingBeginDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingBeginCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                meetingBeginTimeText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingBeginCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
            }
        }

        if (data.hasExtra("endTime")) {
            isSetTime = true;
            long toTime = data.getLongExtra("endTime", 0);
            if (toTime != 0) {
                meetingEndCalendar.setTimeInMillis(toTime);
                meetingEndDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingEndCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                meetingEndTimeText.setText(TimeUtils.calendar2FormatString(
                        MeetingBookingActivity.this, meetingEndCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
            }

        }
    }

    /**
     * 发送广播
     */
    public void setBroadCast() {
        Intent mIntent = new Intent(Constant.ACTION_MEETING);
        mIntent.putExtra("refreshMeeting", "refreshMeeting");
        // 发送广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
        setResult(RESULT_OK);
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnBookingRoomSuccess() {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(MeetingBookingActivity.this,
                    getString(R.string.meeting_booking_success));
            setBroadCast();
            MeetingBookingActivity.this.finish();
        }

        @Override
        public void returnBookingRoomFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MeetingBookingActivity.this, error, errorCode);
        }

        @Override
        public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {
            super.returnIsMeetingAdminSuccess(getIsAdmin);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            PreferencesUtils.putBoolean(MeetingBookingActivity.this,
                    MyApplication.getInstance().getTanent() + userId + "isAdmin", getIsAdmin.isAdmin());
        }

        @Override
        public void returnIsMeetingAdminFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            PreferencesUtils.putBoolean(MeetingBookingActivity.this,
                    MyApplication.getInstance().getTanent() + userId + "isAdmin", false);
            WebServiceMiddleUtils.hand(MeetingBookingActivity.this, error, errorCode);
        }

    }

}
