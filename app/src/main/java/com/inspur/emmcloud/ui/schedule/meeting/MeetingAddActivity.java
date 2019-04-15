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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.DateTimePickerDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

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
    @ViewInject(R.id.et_input_content)
    private EditText inputContentEdit;
    @ViewInject(R.id.tv_start_date)
    private TextView startDateText;
    @ViewInject(R.id.tv_start_time)
    private TextView startTimeText;
    @ViewInject(R.id.tv_end_date)
    private TextView endDateText;
    @ViewInject(R.id.tv_end_time)
    private TextView endTimeText;
    @ViewInject(R.id.et_meeting_position)
    private ClearEditText meetingPostionEdit;
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
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    private boolean isAllDay = false;
    private List<SearchModel> attendeeSearchModelList = new ArrayList<>();
    private List<SearchModel> recorderSearchModelList = new ArrayList<>();
    private List<SearchModel> liaisonSearchModelList = new ArrayList<>();
    private MeetingRoom meetingRoom;
    private Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTimeCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
        endTimeCalendar = (Calendar) startTimeCalendar.clone();
        endTimeCalendar.add(Calendar.HOUR_OF_DAY, 2);
        setMeetingTime();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_save:
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
                break;
        }
    }

    private void selectContact(int requestCode) {
        String title = "";
        Intent intent = new Intent();
        intent.putExtra("select_content", 2);
        intent.putExtra("isMulti_select", true);
        intent.putExtra("isContainMe", true);
        if (requestCode == REQUEST_SELECT_ATTENDEE) {
            title = "选择参会人";
            intent.putExtra("hasSearchResult", (Serializable) attendeeSearchModelList);
        } else if (requestCode == REQUEST_SELECT_RECORDER) {
            title = "选择会议记录人";
            intent.putExtra("hasSearchResult", (Serializable) recorderSearchModelList);
        } else {
            title = "选择会议联络人";
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
                        showTimeInvaladDlg();
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
    private void showTimeInvaladDlg() {
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
                    startTimeCalendar = (Calendar) data.getSerializableExtra(MeetingRoomListActivity.EXTRA_START_TIME);
                    endTimeCalendar = (Calendar) data.getSerializableExtra(MeetingRoomListActivity.EXTRA_END_TIME);
                    meetingRoom = (MeetingRoom)data.getSerializableExtra(MeetingRoomListActivity.EXTRA_MEETING_ROOM);
                    setMeetingTime();
                    meetingPostionEdit.setText(meetingRoom.getName());
                    location = new Location();
                    location.setId(meetingRoom.getId());
                    location.setBuilding(meetingRoom.get);
                    break;
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
                ImageDisplayUtils.getInstance().displayRoundedImage(imageView, photoUrl, R.drawable.default_image, this, 15);
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
            textView.setText(searchModelList.size() + "人");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginLeft = DensityUtil.dip2px(MyApplication.getInstance(), 3);
            layoutParams.setMargins(marginLeft, 0, 0, 0);
            textView.setLayoutParams(layoutParams);
            layout.addView(textView);
        }

    }
}
