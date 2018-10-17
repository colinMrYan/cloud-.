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
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    //    private final String[] durationHourItems = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "18", "24"};
//    private final Integer[] durationHourSumMin = new Integer[]{0, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 1080, 1440};
//    private final String[] durationMinItems = new String[]{"0", "10", "15", "20", "30", "40", "45", "50"};
//    private final Integer[] durationMinSumMin = new Integer[]{0, 10, 15, 20, 30, 40, 45, 50};
    private String[] durationHourItems = null;
    private String[] durationMinItems = null;
    private Integer[] durationHourSumMin = new Integer[]{0, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 1080, 1440};
    private Integer[] durationMinSumMin = new Integer[]{0, 10, 20, 30, 40, 50};
    private int durationHourChoiceIndex = 1;
    private int durationMinChoiceIndex = 0;
    private Calendar startCalendar;
    private List<WebexAttendees> webexAttendeesList = new ArrayList<>();
    private WebexMeeting webexMeeting;
    private WebexAPIService apiService;
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String hourStr = getString(R.string.hour);
        String minStr = getString(R.string.min);
        durationHourItems = new String[]{"0"+hourStr, "1"+hourStr, "2"+hourStr, "3"+hourStr, "4"+hourStr, "5"+hourStr, "6"+hourStr, "7"+hourStr, "8"+hourStr, "9"+hourStr, "10"+hourStr, "11"+hourStr, "12"+hourStr, "18"+hourStr, "24"+hourStr};
        durationMinItems = new String[]{"0"+minStr, "10"+minStr, "20"+minStr, "30"+minStr, "40"+minStr,"50"+minStr};
        startCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
        startDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startCalendar, TimeUtils.FORMAT_MONTH_DAY));
        startTimeText.setText(TimeUtils.calendar2FormatString(getApplicationContext(), startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
        durationHourText.setText(durationHourItems[durationHourChoiceIndex]);
        durationMinText.setText(durationMinItems[durationMinChoiceIndex]);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        String userName = PreferencesUtils.getString(MyApplication.getInstance(), "userRealName", "");
        EditTextUtils.setText(titleEdit, getString(R.string.webex_meeting_title, userName));
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new Webservice());
        loadingDlg = new LoadingDialog(this);
        String password = AppUtils.getRandomStr(6);
        EditTextUtils.setText(passwordEdit, password );
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
                        startDateText.setText(TimeUtils.calendar2FormatString(WebexScheduleMeetingActivity.this, startCalendar, TimeUtils.FORMAT_MONTH_DAY));
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
                .setTitle(getString(R.string.webex_meeting_duration))
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
                .setTitle(getString(R.string.webex_meeting_duration))
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
                String confName = titleEdit.getText().toString().trim();
                if (StringUtils.isBlank(confName)) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.enter_meeting_name);
                    return;
                }
                String meetingPassword = passwordEdit.getText().toString();
                if (StringUtils.isBlank(meetingPassword)) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.enter_meeting_password);
                    return;
                }
                if (meetingPassword.length()<6 || meetingPassword.length()>10){
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_password_length_error);
                    return;
                }
                if (startCalendar.before(Calendar.getInstance())) {
                    showStartDateErrorDlg();
                    return;
                }
                int duration = durationHourSumMin[durationHourChoiceIndex] + durationMinSumMin[durationMinChoiceIndex];
                if (duration == 0) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.set_duration_correct);
                    return;
                }
                List<String> attendeesList = new ArrayList<>();
                for(WebexAttendees webexAttendees:webexAttendeesList){
                    attendeesList.add(webexAttendees.getEmail());
                }
                webexMeeting = new WebexMeeting();
                webexMeeting.setConfName(confName);
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
                intent.putExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable)webexAttendeesList);
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

    private void showStartDateErrorDlg() {
        new MyQMUIDialog.MessageDialogBuilder(WebexScheduleMeetingActivity.this)
                .setTitle(getString(R.string.start_time_error))
                .setMessage(getString(R.string.start_time_error_info))
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();

                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            if (requestCode == REQUEST_ADD_ATTENDEES) {
                webexAttendeesList = (List<WebexAttendees>)data.getSerializableExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST);
                inviteText.setText(webexAttendeesList.size() == 0 ? getString(R.string.none) : webexAttendeesList.size() + "");
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
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
