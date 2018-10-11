package com.inspur.emmcloud.ui.appcenter.webex;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_schedule_meeting)
public class WebexScheduleMeeting extends BaseActivity {

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
    private final String[] durationMinItems = new String[]{"0分钟", "10分钟", "15分钟", "20分钟", "30分钟", "40分钟", "45分钟", "50分钟"};
    private int durationHourChoiceIndex =1;
    private int durationMinChoiceIndex =0;
    private Calendar startCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCalendar = TimeUtils.getNextHalfHourTime(Calendar.getInstance());
        startDateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(), startCalendar, TimeUtils.FORMAT_MONTH_DAY));
        startTimeText.setText(TimeUtils.calendar2FormatString(getApplicationContext(), startCalendar, TimeUtils.FORMAT_HOUR_MINUTE));
        durationHourText.setText(durationHourItems[durationHourChoiceIndex]);
        durationMinText.setText(durationMinItems[durationMinChoiceIndex]);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }


    /**
     * 弹出日期选择Dialog
     */
    private void showDatePickerDlg() {
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(WebexScheduleMeeting.this, DatePickerDialog.THEME_HOLO_LIGHT,
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
                WebexScheduleMeeting.this,TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
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
        new MyQMUIDialog.CheckableSumDialogBuilder(WebexScheduleMeeting.this)
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
        new MyQMUIDialog.CheckableSumDialogBuilder(WebexScheduleMeeting.this)
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
                break;
            case R.id.iv_password_visible:
                if (passwordEdit.getTransformationMethod() instanceof HideReturnsTransformationMethod){
                    passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisibleImg.setImageResource(R.drawable.icon_no_see_pw);
                }else {
                    passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisibleImg.setImageResource(R.drawable.icon_see_pw);
                }
                passwordEdit.setSelection(passwordEdit.getText().toString().length());
                break;
        }
    }
}
