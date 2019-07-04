package com.inspur.emmcloud.ui.appcenter.webex;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MyDatePickerDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.webex.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexScheduleMeetingActivity extends BaseActivity {
    private static final int REQUEST_ADD_ATTENDEES = 1;
    @BindView(R.id.et_title)
    EditText titleEdit;
    @BindView(R.id.tv_start_date)
    TextView startDateText;
    @BindView(R.id.tv_start_time)
    TextView startTimeText;
    @BindView(R.id.tv_duration_hour)
    TextView durationHourText;
    @BindView(R.id.tv_duration_min)
    TextView durationMinText;
    @BindView(R.id.tv_invite)
    TextView inviteText;
    @BindView(R.id.et_password)
    ClearEditText passwordEdit;
    @BindView(R.id.iv_password_visible)
    ImageView passwordVisibleImg;
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
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String hourStr = getString(R.string.hour);
        String minStr = getString(R.string.min);
        String hoursStr = getString(R.string.hours);
        String minsStr = getString(R.string.mins);
        durationHourItems = new String[]{"0" + hourStr, "1" + hourStr, "2" + hoursStr, "3" + hoursStr, "4" + hoursStr, "5" + hoursStr, "6" + hoursStr, "7" + hoursStr, "8" + hoursStr, "9" + hoursStr, "10" + hoursStr, "11" + hoursStr, "12" + hoursStr, "18" + hoursStr, "24" + hoursStr};
        durationMinItems = new String[]{"0" + minStr, "10" + minsStr, "20" + minsStr, "30" + minsStr, "40" + minsStr, "50" + minsStr};
        startCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
        startDateText.setText(TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_MONTH_DAY));
        startTimeText.setText(TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
        durationHourText.setText(durationHourItems[durationHourChoiceIndex]);
        durationMinText.setText(durationMinItems[durationMinChoiceIndex]);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        String userName = PreferencesUtils.getString(MyApplication.getInstance(), "userRealName", "");
        EditTextUtils.setText(titleEdit, getString(R.string.webex_meeting_title, userName));
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new Webservice());
        loadingDlg = new LoadingDialog(this);
        String password = AppUtils.getRandomStr(6);
        EditTextUtils.setText(passwordEdit, password);
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_webex_schedule_meeting;
    }

    /**
     * 弹出日期选择Dialog
     */
    private void showDatePickerDlg() {
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(WebexScheduleMeetingActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        startCalendar.set(year, monthOfYear, dayOfMonth);
                        startDateText.setText(TimeUtils.calendar2FormatString(WebexScheduleMeetingActivity.this, startCalendar, TimeUtils.FORMAT_MONTH_DAY));
                    }
                }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    /**
     * 弹出时间选择Dialog
     */
    private void showTimePickerDlg() {
        TimePickerDialog beginTimePickerDialog = new TimePickerDialog(
                WebexScheduleMeetingActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalendar.set(Calendar.MINUTE, minute);
                startTimeText.setText(TimeUtils.calendar2FormatString(WebexScheduleMeetingActivity.this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
            }
        }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), true);
        beginTimePickerDialog.show();
    }

    private void showDurationHourChoiceDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.cus_dialog_style);
        new CustomDialog.SingleChoiceDialogBuilder(ctw)
                .setTitle(getString(R.string.webex_meeting_duration))
//                .set(durationHourChoiceIndex)
                .setSingleChoiceItems(durationHourItems, durationHourChoiceIndex, new DialogInterface.OnClickListener() {
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
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.cus_dialog_style);
        new CustomDialog.SingleChoiceDialogBuilder(ctw)
                .setTitle(getString(R.string.webex_meeting_duration))
//                .setCheckedIndex(durationMinChoiceIndex)
                .setSingleChoiceItems(durationMinItems, durationMinChoiceIndex, new DialogInterface.OnClickListener() {
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
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_enter_meeting_name);
                    return;
                }
                String meetingPassword = passwordEdit.getText().toString();
                if (StringUtils.isBlank(meetingPassword)) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_enter_meeting_password);
                    return;
                }
                if (meetingPassword.length() < 6 || meetingPassword.length() > 10) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_password_length_error);
                    return;
                }

                if (!FomatUtils.isLetterOrDigits(meetingPassword)) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_password_invalid);
                    return;
                }

                if (startCalendar.before(Calendar.getInstance())) {
                    showStartDateErrorDlg();
                    return;
                }
                int duration = durationHourSumMin[durationHourChoiceIndex] + durationMinSumMin[durationMinChoiceIndex];
                if (duration == 0) {
                    ToastUtils.show(WebexScheduleMeetingActivity.this, R.string.webex_set_duration_correct);
                    return;
                }
                webexMeeting = new WebexMeeting();
                webexMeeting.setConfName(confName);
                webexMeeting.setWebexAttendeesList(webexAttendeesList);
                webexMeeting.setDuration(duration);
                webexMeeting.setMeetingPassword(meetingPassword);
                webexMeeting.setStartDateCalendar(startCalendar);
                scheduleMeeting();
                break;
            case R.id.ibt_back:
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
                intent.putExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
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
        new CustomDialog.MessageDialogBuilder(WebexScheduleMeetingActivity.this)
                .setTitle(getString(R.string.webex_start_time_error))
                .setMessage(getString(R.string.webex_start_time_error_info))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            if (requestCode == REQUEST_ADD_ATTENDEES) {
                webexAttendeesList = (List<WebexAttendees>) data.getSerializableExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST);
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
