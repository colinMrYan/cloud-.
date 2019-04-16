package com.inspur.emmcloud.ui.work.meeting;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Selection;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.work.Meeting;
import com.inspur.emmcloud.bean.work.Room;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 会议详情，这里是每个会议的详情内容
 */
public class MeetingDetailActivity extends BaseActivity {

    private static final int MEETTING_CHOOSE_MEM = 1;
    private static final int MEETING_BEGIN_TIME = 3;
    private static final int MEETING_END_TIME = 4;
    int lineCount = 0;
    private String id = "";
    private CircleTextImageView[] circleImg = new CircleTextImageView[5];
    private ImageView meetingChangeImg;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private EditText topicEdit, noticeEdit;
    private TextView headText, meetingChangeCancelText;
    private TextView beginDateText, endDateText, beginTimeText, endTimeText,
            roomText, attendantText;
    private boolean isChange = false;
    private Calendar beginCalendar, endCalendar;
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private String meetingRoomName = "";
    private String meetingRoomFlour = "";
    private String meetingRoomId = "";
    private String userId = "";
    private String organizerUid = "";
    private Button meetingDetailConfirmBtn;
    private Meeting meeting;
    private ArrayList<String> participantList = new ArrayList<String>();
    private int maxAhead = 2, maxDuration = 12;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_detail);
        initViews();
        initData();
    }

    // /**
    // * 展示会务头像 会务暂时去掉,不确定以后是否恢复，
    // * 如需恢复则解开xml中隐藏会务，在initData里的showMembers方法后调用次此方法
    // */
    // private void showConferenceMember() {
    // if (!StringUtils.isEmpty(attendant)) {
    // conferenceImg.setVisibility(View.VISIBLE);
    // imageDisplayUtils.displayImage(conferenceImg,
    // APIUri.getChannelImgUri(attendant));
    // }
    // }

    /**
     * 展示会议成员头像
     */
    private void showMembers() {
        int memberCount = participantList.size() > 5 ? 5 : participantList
                .size();
        for (int i = 0; i < memberCount; i++) {
            circleImg[i].setVisibility(View.VISIBLE);
            String memberId = participantList.get(participantList.size() - i
                    - 1);
            if (!StringUtils.isBlank(memberId)) {
                ImageDisplayUtils.getInstance().displayImage(circleImg[i],
                        APIUri.getChannelImgUrl(MeetingDetailActivity.this, memberId), R.drawable.icon_person_default);
            }
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        meeting = (Meeting) getIntent().getSerializableExtra("meeting");
        topicEdit.setText(meeting.getTopic());

        noticeEdit.setText(meeting.getNotice());
        Room room = meeting.getRooms().get(0);
        maxAhead = room.getMaxAhead();
        if (maxAhead == 0) {
            maxAhead = 2;
        }
        if (isAdmin) {
            maxAhead = 7;
        }
        maxDuration = room.getMaxDuration();
        if (maxDuration == 0) {
            maxDuration = 12;
        }
        String fromTime = meeting.getFrom();
        beginCalendar = TimeUtils.timeString2Calendar(fromTime);
        beginTimeText.setText(TimeUtils.calendar2FormatString(
                MeetingDetailActivity.this, beginCalendar,
                TimeUtils.DATE_FORMAT_HOUR_MINUTE));
        String monthAndDayBegin = TimeUtils.calendar2FormatString(
                MeetingDetailActivity.this, beginCalendar,
                TimeUtils.FORMAT_MONTH_DAY);
        beginDateText.setText(monthAndDayBegin);

        String toTime = meeting.getTo();
        endCalendar = TimeUtils.timeString2Calendar(toTime);
        String monthAndDayEnd = TimeUtils.calendar2FormatString(
                MeetingDetailActivity.this, endCalendar,
                TimeUtils.FORMAT_MONTH_DAY);
        endDateText.setText(monthAndDayEnd);
        endTimeText.setText(TimeUtils.calendar2FormatString(
                MeetingDetailActivity.this, endCalendar,
                TimeUtils.DATE_FORMAT_HOUR_MINUTE));
        id = meeting.getMeetingId();
        roomText.setText(room.getRoomName() + " " + room.getName());
        participantList = meeting.getParticipants();
        attendantText.setText(meeting.getAttendant());
        userId = ((MyApplication) getApplication()).getUid();
        meetingRoomId = meeting.getRooms().get(0).getRoomId();
        getUidsInfoList();
        showMembers();
        organizerUid = meeting.getOrganizer();
        if (!userId.equals(organizerUid)
                || endCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            meetingChangeImg.setVisibility(View.GONE);
        }
        isAdmin = PreferencesUtils.getBoolean(getApplicationContext(),
                MyApplication.getInstance().getTanent() + userId + "isAdmin", false);

    }


    /**
     * 初始化views
     */
    private void initViews() {
        roomText = ((TextView) findViewById(R.id.meeting_detail_room_name_text));
        beginDateText = ((TextView) findViewById(R.id.meeting_detail_begin_date_text));
        endDateText = ((TextView) findViewById(R.id.meeting_detail_end_date_text));
        beginTimeText = ((TextView) findViewById(R.id.meeting_detail_begin_time_text));
        endTimeText = ((TextView) findViewById(R.id.meeting_detail_end_time_text));
        topicEdit = ((EditText) findViewById(R.id.meeting_detail_content_text));
        attendantText = ((TextView) findViewById(R.id.meeting_detial_member_log_text));
        noticeEdit = ((EditText) findViewById(R.id.meeting_notice_name_text));
        circleImg[0] = (CircleTextImageView) findViewById(R.id.meeting_detail_member_head_img5);
        circleImg[1] = (CircleTextImageView) findViewById(R.id.meeting_detail_member_head_img4);
        circleImg[2] = (CircleTextImageView) findViewById(R.id.meeting_detail_member_head_img3);
        circleImg[3] = (CircleTextImageView) findViewById(R.id.meeting_detail_member_head_img2);
        circleImg[4] = (CircleTextImageView) findViewById(R.id.meeting_detail_member_head_img1);
        // conferenceImg = (CircleTextImageView)
        // findViewById(R.id.mession_memhead_img);
        headText = (TextView) findViewById(R.id.header_text);
        meetingChangeCancelText = (TextView) findViewById(R.id.meeting_detail_cancel_text);
        meetingChangeImg = (ImageView) findViewById(R.id.meeting_detail_more_img);
        meetingDetailConfirmBtn = (Button) findViewById(R.id.meeting_detail_confirm_btn);
        apiService = new WorkAPIService(MeetingDetailActivity.this);
        apiService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(MeetingDetailActivity.this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.meeting_detail_cancel_text:
                handleCancelEdit();
                break;
            case R.id.meeting_detail_room_layout:
            case R.id.meeting_detail_room_name_layout:
            case R.id.meeting_detail_room_name_text:
                handleChooseRoom();
                break;
            case R.id.meeting_detail_more_img:
                long toTime = Long.parseLong(meeting.getTo());
                long fromTime = Long.parseLong(meeting.getFrom());
                if (System.currentTimeMillis() > fromTime) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_detail_has_began));
                    break;
                }
                if (System.currentTimeMillis() > toTime) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_detail_has_finished));
                    break;
                }
                showPopupWindow(v);
                break;
            case R.id.meeting_detail_member_layout:
            case R.id.meeting_detail_member_content_layout:
                handleChooseMember();
                break;
            case R.id.meeting_detail_begin_date_text:
            case R.id.meeting_detail_end_date_text:
                if (isChange) {
                    showDateDialog();
                }
                break;
            case R.id.meeting_detail_begin_time_text:
                if (isChange) {
                    handleChooseTime(MEETING_BEGIN_TIME);
                }
                break;
            case R.id.meeting_detail_end_time_text:
                if (isChange) {
                    handleChooseTime(MEETING_END_TIME);
                }
                break;
            case R.id.meeting_detail_confirm_btn:
                String topic = topicEdit.getText().toString();
                if (StringUtils.isBlank(topic)) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_room_booking_topic));
                    break;
                }
                if (beginCalendar == null) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_begin_or_date_time));
                    break;
                }
                if (endCalendar == null) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_end_time_no_empty));
                }

                if (beginCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            R.string.meeting_room_time_late);
                    break;
                }
                if (beginCalendar.after(endCalendar)) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.calendar_start_or_end_time_illegal));
                    break;
                }
                if (StringUtils.isBlank(meetingRoomId)) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_room_booking_choosing_room));
                    break;
                }
                if (selectMemList.size() == 0) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_invating_members));
                    break;
                }
                if (topic.length() > 128) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.meeting_topic_too_long));
                    break;
                }
                String notice = noticeEdit.getText().toString();
                if (!StringUtils.isBlank(notice) && notice.length() > 128) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.meeting_notice_too_long));
                    break;
                }
                // 服务端没有传来maxAhead暂时不控制这里，等服务端开始有数据则解开
                // int count = TimeUtils.getCountdownNum(endCalendar);
                // if (count >= maxAhead) {
                // ToastUtils.show(MeetingDetailActivity.this,
                // getString(R.string.more_than_max_day));
                // break;
                // }
                int countHour = TimeUtils.getCeil(endCalendar,
                        beginCalendar);
                if (countHour > maxDuration) {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_more_than_max_time));
                    break;
                }
                long endLong = endCalendar.getTimeInMillis();
                long beginLong = beginCalendar.getTimeInMillis();
                updateMeeting(topic, notice, beginLong, endLong);
                break;
            default:
                break;
        }
    }

    /**
     * 处理选择房间
     */
    private void handleChooseRoom() {
        if (isChange) {
            Intent intent = new Intent();
            intent.setClass(MeetingDetailActivity.this,
                    MeetingRoomListActivity.class);
            startActivityForResult(intent, 0);
        } else {
            // 会议地点的详情，暂时屏蔽
            // bundle.putString("room", room);
            // IntentUtils.startActivity(MeettingDetailActivity.this,
            // LocationActivity.class, bundle);
        }
    }

    /**
     * 取消编辑的逻辑
     */
    private void handleCancelEdit() {
        isChange = false;
        headText.setText(getString(R.string.meeting_booking_detail));
        meetingChangeImg.setVisibility(View.VISIBLE);
        meetingChangeCancelText.setVisibility(View.GONE);
        meetingDetailConfirmBtn.setVisibility(View.GONE);
        topicEdit.setEnabled(false);
        noticeEdit.setEnabled(false);
    }

    /**
     * 选人逻辑
     */
    private void handleChooseMember() {
        if (isChange) {
            Intent intent = new Intent();
            intent.putExtra("select_content", 2);
            intent.putExtra("isMulti_select", true);
            intent.putExtra("isContainMe", true);
            intent.putExtra("title",
                    getString(R.string.meeting_invating_members));
            if (selectMemList != null) {
                intent.putExtra("hasSearchResult", (Serializable) selectMemList);
            }
            intent.setClass(getApplicationContext(),
                    ContactSearchActivity.class);
            startActivityForResult(intent, MEETTING_CHOOSE_MEM);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("cid", "");
            bundle.putString("title", getString(R.string.meeting_memebers));
            bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
            bundle.putStringArrayList("uidList", participantList);
            IntentUtils.startActivity(MeetingDetailActivity.this,
                    MembersActivity.class, bundle);
        }
    }

    /**
     * 选择时间
     *
     * @param timeType
     */
    private void handleChooseTime(int timeType) {
        int hour = 0, minute = 0;
        if (timeType == MEETING_BEGIN_TIME) {
            hour = beginCalendar.get(Calendar.HOUR_OF_DAY);
            minute = beginCalendar.get(Calendar.MINUTE);
        } else if (timeType == MEETING_END_TIME) {
            hour = endCalendar.get(Calendar.HOUR_OF_DAY);
            minute = endCalendar.get(Calendar.MINUTE);
        }
        showTimeDialog(hour, minute, timeType);
    }

    /**
     * 更新会议
     *
     * @param topic
     * @param notice
     * @param beginLong
     * @param endLong
     */
    private void updateMeeting(String topic, String notice, long beginLong,
                               long endLong) {
        if (NetUtils.isNetworkConnected(MeetingDetailActivity.this)) {
            loadingDialog.show();
            apiService.updateMeeting(topic.trim(), meetingRoomId,
                    meetingRoomFlour + " " + meetingRoomName, 0, notice,
                    meeting.getBookDate(), beginLong, endLong, userId, null,
                    selectMemList, "", id);
        }
        meeting.setTopic(topic);
        meeting.getRooms().get(0).setRoomid(meetingRoomId);
        meeting.getRooms().get(0)
                .setRoomname(meetingRoomFlour + " " + meetingRoomName);
        meeting.setNotice(notice);
        meeting.setFrom(beginLong + "");
        meeting.setTo(endLong + "");
        ArrayList<String> memberIds = new ArrayList<String>();
        for (int i = 0; i < selectMemList.size(); i++) {
            memberIds.add(selectMemList.get(i).getId());
        }
        meeting.setParticipants(memberIds);
        meeting.setMeetingId(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MEETTING_CHOOSE_MEM) {
                chooseMember(data);
            } else if (requestCode == 0 && data != null) {
                getRoomAvailableTime(data);
            }
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
        maxAhead = Integer.parseInt(data.getStringExtra("maxAhead"));
        maxDuration = Integer.parseInt(data.getStringExtra("maxDuration"));
        if (StringUtils.isEmpty(meetingRoomName)) {
            roomText.setText("");
        } else {
            roomText.setText(meetingRoomFlour + " " + meetingRoomName);
        }
        if (data.hasExtra("beginTime")) {
            long fromTime = data.getLongExtra("beginTime", 0);
            if (fromTime != 0) {
                Calendar fromCalendar = Calendar.getInstance();
                fromCalendar.setTimeInMillis(fromTime);
                beginCalendar = fromCalendar;
                beginDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, fromCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                beginTimeText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, fromCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
            }

        }
        if (data.hasExtra("endTime")) {
            long toTime = data.getLongExtra("endTime", 0);
            if (toTime != 0) {
                Calendar toCalendar = Calendar.getInstance();
                toCalendar.setTimeInMillis(toTime);
                endCalendar = toCalendar;
                endDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, toCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                endTimeText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, toCalendar,
                        TimeUtils.FORMAT_HOUR_MINUTE));
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
        int memberCount = getMemberCount();
        hideAllHeadImage();
        showSelectMembers(memberCount);
    }

    /**
     * 把selectMemList填充起来
     */
    private void getUidsInfoList() {
        selectMemList.clear();
        for (String uid : participantList) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
            SearchModel searchModel = new SearchModel(contactUser);
            selectMemList.add(searchModel);
        }
    }

    /**
     * 隐藏所有头像
     */
    private void hideAllHeadImage() {
        for (int i = 0; i < 5; i++) {
            circleImg[i].setVisibility(View.GONE);
        }
    }

    /**
     * 设置成员数量不超过5
     *
     * @return
     */
    private int getMemberCount() {
        int memberCount = selectMemList.size() > 5 ? 5 : selectMemList.size();
        return memberCount;
    }

    /**
     * 显示成员头像
     *
     * @param memberCount
     */
    private void showSelectMembers(int memberCount) {
        for (int i = 0; i < memberCount; i++) {
            circleImg[i].setVisibility(View.VISIBLE);
            ImageDisplayUtils.getInstance().displayImage(circleImg[i],
                    APIUri.getChannelImgUrl(MeetingDetailActivity.this, selectMemList.get(selectMemList.size() - i - 1).getId()), R.drawable.icon_person_default);
        }
    }

    /**
     * 变更会议，取消会议下拉框
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(MeetingDetailActivity.this)
                .inflate(R.layout.pop_window_view, null);
        // 设置按钮的点击事件
        final PopupWindow popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(MeetingDetailActivity.this, 1.0f);
            }
        });


        RelativeLayout meetingPopChange = (RelativeLayout) contentView
                .findViewById(R.id.meeting_change_layout);
        meetingPopChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals(organizerUid)) {
                    changeEditState();
                    setCursorPosition();
                    changeUI();
                } else {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_detail_not_organizer));
                }
                popupWindow.dismiss();
            }
        });

        RelativeLayout meetingPopCancel = (RelativeLayout) contentView
                .findViewById(R.id.meeting_cancel_layout);
        meetingPopCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals(organizerUid)) {
                    calcelMeeting();
                } else {
                    ToastUtils.show(MeetingDetailActivity.this,
                            getString(R.string.meeting_detail_not_organizer));
                }
                popupWindow.dismiss();
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(MeetingDetailActivity.this, 0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

    }

    /**
     * 点击变更会议时修改UI的可点击，可见，是否可编辑的状态
     */
    protected void changeUI() {
        headText.setText(getString(R.string.meeting_detail_change));
        meetingChangeImg.setVisibility(View.GONE);
        meetingChangeCancelText.setVisibility(View.VISIBLE);
        meetingDetailConfirmBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 取消会议
     */
    protected void calcelMeeting() {
        if (NetUtils.isNetworkConnected(MeetingDetailActivity.this)
                && !StringUtils.isBlank(id)) {
            loadingDialog.show();
            apiService.deleteMeeting(id);
        }
    }

    /**
     * 设置光标位置
     */
    protected void setCursorPosition() {
        CharSequence text = topicEdit.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
        InputMethodUtils.display(MeetingDetailActivity.this, topicEdit);
    }

    /**
     * 修改编辑状态
     */
    protected void changeEditState() {
        isChange = true;
        topicEdit.setFocusable(true);
        topicEdit.setEnabled(true);
        topicEdit.requestFocus();
        noticeEdit.setEnabled(true);
    }

    /**
     * 发送广播
     */
    private void sendBroadCast() {
        Intent mIntent = new Intent(Constant.ACTION_MEETING);
        mIntent.putExtra("refreshMeeting", "refreshMeeting");
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

    /**
     * 弹出时间选择Dialog
     *
     * @param hour
     * @param minute
     * @param beginOrEnd
     */
    private void showTimeDialog(int hour, int minute, final int beginOrEnd) {
        TimePickerDialog beginTimePickerDialog = new TimePickerDialog(
                MeetingDetailActivity.this, new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                if (beginOrEnd == MEETING_BEGIN_TIME) {
                    beginCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    beginCalendar.set(Calendar.MINUTE, minute);
                    beginTimeText.setText(TimeUtils
                            .Calendar2TimeString(beginCalendar,
                                    TimeUtils.DATE_FORMAT_HOUR_MINUTE));
                    endCalendar = (Calendar) beginCalendar.clone();
                    handleEndCalendar();
                    endTimeText.setText(TimeUtils
                            .calendar2FormatString(
                                    MeetingDetailActivity.this,
                                    endCalendar,
                                    TimeUtils.FORMAT_HOUR_MINUTE));
                } else if (beginOrEnd == MEETING_END_TIME) {
                    endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endCalendar.set(Calendar.MINUTE, minute);
                    endTimeText.setText(TimeUtils.Calendar2TimeString(
                            endCalendar,
                            TimeUtils.DATE_FORMAT_HOUR_MINUTE));
                }
            }
        }, hour, minute, true);
        beginTimePickerDialog.show();
    }

    /**
     * 是否增加两个小时
     */
    private void handleEndCalendar() {
        endCalendar.add(Calendar.HOUR_OF_DAY, 2);
        if (!TimeUtils.isSameDay(beginCalendar, endCalendar)) {
            int day = beginCalendar.get(Calendar.DAY_OF_MONTH);
            endCalendar.set(Calendar.DAY_OF_MONTH, day);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
        }
    }

    /**
     * 弹出日期选择Dialog
     */
    private void showDateDialog() {
        int year = beginCalendar.get(Calendar.YEAR);
        int month = beginCalendar.get(Calendar.MONTH);
        int day = beginCalendar.get(Calendar.DAY_OF_MONTH);
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(
                MeetingDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                beginCalendar.set(year, monthOfYear, dayOfMonth);
                endCalendar.set(year, monthOfYear, dayOfMonth);
                beginDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, beginCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
                endDateText.setText(TimeUtils.calendar2FormatString(
                        MeetingDetailActivity.this, beginCalendar,
                        TimeUtils.FORMAT_MONTH_DAY));
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * 向会议室详情界面返回数据
     *
     * @param type
     */
    public void callBackActivity(String type) {
        Intent intent = new Intent();
        if (meeting != null) {
            intent.putExtra(type, meeting);
        }
        setResult(RESULT_OK, intent);
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnBookingRoomSuccess(
        ) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            ToastUtils.show(MeetingDetailActivity.this,
                    getString(R.string.meeting_update_success));
            sendBroadCast();
            callBackActivity("update");
            finish();
        }

        @Override
        public void returnBookingRoomFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MeetingDetailActivity.this, error,
                    errorCode);
        }

        @Override
        public void returnDeleteMeetingSuccess(com.inspur.emmcloud.bean.schedule.meeting.Meeting meeting) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            sendBroadCast();
            callBackActivity("delete");
            finish();
        }

        @Override
        public void returnDeleteMeetingFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MeetingDetailActivity.this, error, errorCode);
        }
    }

}
