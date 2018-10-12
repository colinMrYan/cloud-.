package com.inspur.emmcloud.ui.appcenter.webex;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.bean.appcenter.webex.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_schedule_meeting)
public class WebexScheduleMeetingActivity extends BaseActivity {
    private static final int REQUEST_ADD_ATTENDEES = 1;
    @ViewInject(R.id.et_title)
    private EditText titleEdit;
    @ViewInject(R.id.tv_start_date)
    private TextView startDateText;
    @ViewInject(R.id.tv_start_time)
    private TextView startTimeText;
    @ViewInject(R.id.tv_duration_hour)
    private TextView durationHourText;
    @ViewInject(R.id.tv_duration_min)
    private TextView durationMinText;
    @ViewInject(R.id.tv_invite)
    private TextView inviteText;
    @ViewInject(R.id.et_password)
    private ClearEditText passwordEdit;
    @ViewInject(R.id.iv_password_visible)
    private ImageView passwordVisibleImg;
    private final String[] durationHourItems = new String[]{"0小时", "1小时", "2小时", "3小时", "4小时", "5小时", "6小时", "7小时", "8小时", "9小时", "10小时", "11小时", "12小时", "18小时", "24小时"};
    private final Integer[] durationHourSumMin = new Integer[]{0, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 1080, 1440};
    private final String[] durationMinItems = new String[]{"0分钟", "10分钟", "15分钟", "20分钟", "30分钟", "40分钟", "45分钟", "50分钟"};
    private final Integer[] durationMinSumMin = new Integer[]{0, 10, 15, 20, 30, 40, 45, 50};
    private int durationHourChoiceIndex = 1;
    private int durationMinChoiceIndex = 0;
    private Calendar startCalendar;
    private ArrayList<String> attendeesList = new ArrayList<>();
    private WebexMeeting webexMeeting;
    private WebexAPIService apiService;
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
        startDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startCalendar, TimeUtils.FORMAT_MONTH_DAY));
        startTimeText.setText(TimeUtils.calendar2FormatString(getApplicationContext(), startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
        durationHourText.setText(durationHourItems[durationHourChoiceIndex]);
        durationMinText.setText(durationMinItems[durationMinChoiceIndex]);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        String userName = PreferencesUtils.getString(MyApplication.getInstance(), "userRealName", "");
        EditTextUtils.setText(titleEdit,getString(R.string.webex_meeting_title, userName));
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new Webservice());
        loadingDlg = new LoadingDialog(this);
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        EditTextUtils.setText(passwordEdit,code+"");
    }


    /**
     * 弹出日期选择Dialog
     */
    private void showDatePickerDlg() {
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(WebexScheduleMeetingActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        startCalendar.set(year, monthOfYear, dayOfMonth);
                        startDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startCalendar, TimeUtils.FORMAT_MONTH_DAY));
                    }
                }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        datePickerDialog.setHideYear();
    }


    /**
     * 弹出时间选择Dialog
     */
    private void showTimePickerDlg() {
        TimePickerDialog beginTimePickerDialog = new TimePickerDialog(
                WebexScheduleMeetingActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalendar.set(Calendar.MINUTE, minute);
                startTimeText.setText(TimeUtils.calendar2FormatString(getApplicationContext(), startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
            }
        }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), true);
        beginTimePickerDialog.show();
    }

    private void showDurationHourChoiceDialog() {
        new MyQMUIDialog.CheckableSumDialogBuilder(WebexScheduleMeetingActivity.this)
                .setTitle("会议持续时间")
                .setCheckedIndex(durationHourChoiceIndex)
                .addItems(durationHourItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        durationHourChoiceIndex = which;
                        durationHourText.setText(durationHourItems[durationHourChoiceIndex]);
                    }
                })
                .show();
    }

    private void showDurationMinChoiceDialog() {
        new MyQMUIDialog.CheckableSumDialogBuilder(WebexScheduleMeetingActivity.this)
                .setTitle("会议持续时间")
                .setCheckedIndex(durationMinChoiceIndex)
                .addItems(durationMinItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        durationMinChoiceIndex = which;
                        durationMinText.setText(durationMinItems[durationMinChoiceIndex]);
                    }
                })
                .show();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start:
                String confName = titleEdit.getText().toString();
                if (StringUtils.isBlank(confName)) {
                    ToastUtils.show(MyApplication.getInstance(), "请输入会议主题");
                    return;
                }
                String meetingPassword = passwordEdit.getText().toString();
                if (StringUtils.isBlank(meetingPassword)) {
                    ToastUtils.show(MyApplication.getInstance(), "请输入会议密码");
                    return;
                }
                int duration = durationHourSumMin[durationHourChoiceIndex] + durationMinSumMin[durationMinChoiceIndex];
                if (duration == 0) {
                    ToastUtils.show(MyApplication.getInstance(), "请正确设置会议持续时间");
                    return;
                }
                webexMeeting = new WebexMeeting();
                webexMeeting.setConfName(confName);
                webexMeeting.setAgenda("");
                webexMeeting.setAttendeesList(attendeesList);
                webexMeeting.setDuration(duration);
                webexMeeting.setMeetingPassword(meetingPassword);
                webexMeeting.setStartDateCalendar(startCalendar);
                scheduleMeeting();
                break;
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_start_date:
                showDatePickerDlg();
                break;
            case R.id.tv_start_time:
                showTimePickerDlg();
                break;
            case R.id.tv_duration_hour:
                showDurationHourChoiceDialog();
                break;
            case R.id.tv_duration_min:
                showDurationMinChoiceDialog();
                break;
            case R.id.rl_invite:
                Intent intent = new Intent(WebexScheduleMeetingActivity.this, WebexAddAttendeesActivity.class);
                intent.putStringArrayListExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST, attendeesList);
                startActivityForResult(intent, REQUEST_ADD_ATTENDEES);
                break;
            case R.id.iv_password_visible:
                if (passwordEdit.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                    passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisibleImg.setImageResource(R.drawable.icon_no_see_pw);
                } else {
                    passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisibleImg.setImageResource(R.drawable.icon_see_pw);
                }
                passwordEdit.setSelection(passwordEdit.getText().toString().length());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            if (requestCode == REQUEST_ADD_ATTENDEES) {
                attendeesList = data.getStringArrayListExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST);
                inviteText.setText(attendeesList.size());
            }
        }
    }

    private void scheduleMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.scheduleWebexMeetingList(webexMeeting.toJsonObject());
        }
    }

    private class Webservice extends APIInterfaceInstance {
        @Override
        public void returnScheduleWebexMeetingSuccess(GetScheduleWebexMeetingSuccess getScheduleWebexMeetingSuccess) {
            LoadingDialog.dimissDlg(loadingDlg);
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnScheduleWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(),error,errorCode);
        }
    }
}
