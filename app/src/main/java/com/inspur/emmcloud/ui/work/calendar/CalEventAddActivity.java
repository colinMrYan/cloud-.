package com.inspur.emmcloud.ui.work.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;

import java.util.Calendar;
import java.util.Locale;

/**
 * 日历事件添加界面 com.inspur.emmcloud.ui.CalEventAddActivity create at 2016年9月12日
 * 下午5:49:06
 */
public class CalEventAddActivity extends BaseActivity {

    private static final int CAL_TYPE_REQUEST_CODE = 1;
    private static final int REPEAT_TYPE_REQUEST_CODE = 2;
    private static final int CAL_ALERT_TIME_REQUEST_CODE = 3;
    private TextView startDateText;
    private TextView startTimeText;
    private TextView endDateText;
    private TextView endTimeText;
    private SwitchView allDaySwitch;
    private boolean isAllDay = false;
    private TextView repeatText;
    private TextView calTypeText;
    private TextView alertTimeText;
    private TextView addText;
    private MyCalendar calendar;
    private CircleTextImageView calendarImg;
    private LoadingDialog loadingDlg;
    private CalendarEvent calEvent;
    private RelativeLayout startTimeLayout;
    private RelativeLayout endTimeLayout;
    private EditText titleEdit;
    private EditText remarkEdit;
    private boolean isEditStatus = true;
    private WorkAPIService apiService;
    private CalendarEvent addCalendarEvent;
    private String addCalendarJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calevent_add);
        initView();
        initData();

    }

    /**
     * 初始化控件
     */
    private void initView() {
        // TODO Auto-generated method stub
        startDateText = (TextView) findViewById(R.id.start_date_text);
        startTimeText = (TextView) findViewById(R.id.start_time_text);
        endDateText = (TextView) findViewById(R.id.end_date_text);
        endTimeText = (TextView) findViewById(R.id.end_time_text);
        alertTimeText = (TextView) findViewById(R.id.alert_text);
        titleEdit = (EditText) findViewById(R.id.title_edit);
        alertTimeText.setText(getString(R.string.nothing));
        allDaySwitch = (SwitchView) findViewById(R.id.all_day_switch);
        repeatText = (TextView) findViewById(R.id.repeat_text);
        calTypeText = (TextView) findViewById(R.id.cal_type_text);
        calendarImg = (CircleTextImageView) findViewById(R.id.calendar_img);
        addText = (TextView) findViewById(R.id.add_text);
        loadingDlg = new LoadingDialog(this);
        startTimeLayout = (RelativeLayout) findViewById(R.id.start_time_layout);
        endTimeLayout = (RelativeLayout) findViewById(R.id.end_time_layout);
        remarkEdit = (EditText) findViewById(R.id.remark_edit);
        apiService = new WorkAPIService(getApplicationContext());
        apiService.setAPIInterface(new WebService());
        allDaySwitch.setOnStateChangedListener(new OnStateChangedListener() {

            @Override
            public void toggleToOn(View view) {
                // TODO Auto-generated method stub

                allDaySwitch.toggleSwitch(true);
                isAllDay = true;
                startTimeLayout.setVisibility(View.INVISIBLE);
                endTimeLayout.setVisibility(View.INVISIBLE);

            }

            @Override
            public void toggleToOff(View view) {
                // TODO Auto-generated method stub
                allDaySwitch.toggleSwitch(false);
                isAllDay = false;
                startTimeLayout.setVisibility(View.VISIBLE);
                endTimeLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initData() {
        // TODO Auto-generated method stub
        Calendar startCalendar = null;
        Calendar endCalendar = null;
        if (getIntent().hasExtra("calEvent")) {
            isEditStatus = false;
            allDaySwitch.setEnable(false);
            calEvent = (CalendarEvent) getIntent().getSerializableExtra(
                    "calEvent");
            ((TextView) findViewById(R.id.header_text))
                    .setText(getString(R.string.calendar_detail));
            if (calEvent.getCalendar().getCommunity()) {
                addText.setVisibility(View.GONE);
            }
            addText.setText(getString(R.string.calendar_adjust));
            isAllDay = calEvent.isAllday();
            allDaySwitch.setOpened(isAllDay);
            startCalendar = calEvent.getStartDate();
            endCalendar = calEvent.getEndDate();
            String title = calEvent.getTitle();
            titleEdit.setText(title);
            setEditTextState(titleEdit, false);
            setEditTextState(remarkEdit, false);
            calendar = calEvent.getCalendar();
            if (calendar != null) {
                calTypeText.setText(calendar.getName());
                calendarImg.setVisibility(View.VISIBLE);
                calendarImg.setText(" ");
                int color = CalendarColorUtils.getColor(
                        CalEventAddActivity.this, calendar.getColor());
                calendarImg.setFillColor(color);
            }

        } else if (getIntent().hasExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE)) {
            String data = getIntent().getStringExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE);
            titleEdit.setText(data);
        }
        setEventTime(startCalendar, endCalendar);
        if (isAllDay) {
            startTimeLayout.setVisibility(View.INVISIBLE);
            endTimeLayout.setVisibility(View.INVISIBLE);
        } else {
            startTimeLayout.setVisibility(View.VISIBLE);
            endTimeLayout.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 设置事件的初始开始和结束时间
     */
    private void setEventTime(Calendar startCalendar, Calendar endCalendar) {
        // TODO Auto-generated method stub
        if (startCalendar == null) {
            startCalendar = Calendar.getInstance();
        }

        if (endCalendar == null) {
            endCalendar = Calendar.getInstance();
            endCalendar.setTime(startCalendar.getTime());
            if (!isAllDay) {
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
            }
        }
        String startDateStr = TimeUtils.calendar2FormatString(CalEventAddActivity.this, startCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String startTimeStr = TimeUtils.calendar2FormatString(CalEventAddActivity.this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endDateStr = TimeUtils.calendar2FormatString(CalEventAddActivity.this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String endTimeStr = TimeUtils.calendar2FormatString(CalEventAddActivity.this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);

        startDateText.setText(startDateStr);
        startTimeText.setText(startTimeStr);
        endDateText.setText(endDateStr);
        endTimeText.setText(endTimeStr);
    }

    /**
     * 设置EditText 是否编辑
     *
     * @param editText
     * @param isEdit
     */
    public void setEditTextState(EditText editText, boolean isEdit) {
        if (!isEdit) {
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
        } else {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
        }
    }

    public void onClick(View v) {
        String startDateStr = "";
        Calendar startCalendar = null;
        String endDateStr = "";
        Calendar endCalendar = null;
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;

            case R.id.add_text:
                if (isEditStatus == false) {
                    isEditStatus = true;
                    setEditTextState(titleEdit, true);
                    setEditTextState(remarkEdit, true);
                    allDaySwitch.setEnable(true);
                    addText.setText(getString(R.string.save));
                } else {
                    String title = titleEdit.getText().toString();
                    if (isAllDay) {
                        startDateStr = startDateText.getText() + "00:00";
                    }
                    startDateStr = startDateText.getText() + " "
                            + startTimeText.getText();
                    startCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, startDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    endDateStr = endDateText.getText() + " "
                            + endTimeText.getText();
                    endCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, endDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    if (StringUtils.isBlank(title)) {
                        ToastUtils.show(getApplicationContext(),
                                R.string.calendar_please_input_title);
                        return;
                    }
                    if (endCalendar.before(startCalendar)) {
                        ToastUtils.show(getApplicationContext(),
                                R.string.calendar_start_or_end_time_illegal);
                        return;
                    }
                    if (calendar == null) {
                        ToastUtils.show(getApplicationContext(),
                                R.string.calendar_select_calendar);
                        return;
                    }
                    if (title.length() > 64) {
                        ToastUtils.show(getApplicationContext(),
                                R.string.calendar_tilte_cannot_exceed_64);
                        return;
                    }
                    if (getIntent().hasExtra("calEvent")) {
                        calEvent.setTitle(title);
                        calEvent.setAllday(isAllDay);
                        calEvent.setStartDate(TimeUtils
                                .localCalendar2UTCCalendar(startCalendar));
                        calEvent.setCalendar(calendar);
                        if (!isAllDay) {
                            calEvent.setEndDate(TimeUtils
                                    .localCalendar2UTCCalendar(endCalendar));
                        }
                        updateCalEvent();
                    } else {
                        combineCalEvent(title, isAllDay, startCalendar, endCalendar);
                        addCalEvent();
                    }
                }

                break;

            case R.id.start_date_layout:
                if (isEditStatus == true) {
                    startDateStr = startDateText.getText() + " "
                            + startTimeText.getText();
                    Log.d("jason", "startDateStr=" + startDateStr);
                    startCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, startDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    showDatePickerDlg(true, startCalendar);
                }

                break;

            case R.id.start_time_layout:
                if (isEditStatus == true) {
                    startDateStr = startDateText.getText() + " "
                            + startTimeText.getText();
                    startCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, startDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    showTimePickerDlg(true, startCalendar);
                }

                break;

            case R.id.end_date_layout:
                if (isEditStatus == true) {
                    endDateStr = endDateText.getText() + " "
                            + endTimeText.getText();

                    endCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, endDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    showDatePickerDlg(false, endCalendar);
                }

                break;
            case R.id.end_time_layout:
                if (isEditStatus == true) {
                    endDateStr = endDateText.getText() + " "
                            + endTimeText.getText();
                    endCalendar = TimeUtils.timeString2Calendar(CalEventAddActivity.this, endDateStr,
                            TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                    showTimePickerDlg(false, endCalendar);
                }

                break;

            case R.id.repeat_layout:
                if (isEditStatus == true) {
                    intent.setClass(getApplicationContext(),
                            CalRepeatActivity.class);
                    intent.putExtra("repeatType", repeatText.getText());
                    startActivityForResult(intent, REPEAT_TYPE_REQUEST_CODE);
                }
                break;

            case R.id.cal_type_layout:
                if (isEditStatus == true) {
                    intent.setClass(getApplicationContext(),
                            CalTypeSelectActivity.class);
                    intent.putExtra("calType", repeatText.getText());
                    if (calendar != null) {
                        intent.putExtra("selectCalendar", calendar);
                    }
                    startActivityForResult(intent, CAL_TYPE_REQUEST_CODE);
                }
                break;
            case R.id.alert_layout:
                if (isEditStatus == true) {
                    intent.setClass(getApplicationContext(),
                            AlertTimeActivity.class);
                    intent.putExtra("alertTime", alertTimeText.getText());
                    startActivityForResult(intent, CAL_ALERT_TIME_REQUEST_CODE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 本地数据拼接CalEvent
     *
     * @param title
     * @param isAllDay
     * @param startCalendar
     * @param endCalendar
     */
    private void combineCalEvent(String title, boolean isAllDay,
                                 Calendar startCalendar, Calendar endCalendar) {
        // TODO Auto-generated method stub
        addCalendarEvent = new CalendarEvent();
        addCalendarEvent.setTitle(title);
        addCalendarEvent.setAllday(isAllDay);
        addCalendarEvent.setStartDate(TimeUtils
                .localCalendar2UTCCalendar(startCalendar));
        addCalendarEvent.setState("ACTIVED");
        if (!isAllDay) {
            addCalendarEvent.setEndDate(TimeUtils
                    .localCalendar2UTCCalendar(endCalendar));
        }
        addCalendarJson = JSONUtils.toJSONString(addCalendarEvent);
        addCalendarEvent.setCalendar(calendar);
    }

    /**
     * 更新日程
     */
    private void updateCalEvent() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            String calEventJson = JSONUtils.toJSONString(calEvent);
            apiService.updateCalEvent(calEventJson);
        }
    }

    /**
     * 添加事件
     */
    private void addCalEvent() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.addCalEvent(calendar.getId(), addCalendarJson);

        }

    }

    /**
     * 弹出日期选择框
     */
    private void showDatePickerDlg(final boolean isStartDate, Calendar calendar) {
        // TODO Auto-generated method stub
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(
                CalEventAddActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                String tripDateString = TimeUtils.calendar2FormatString(CalEventAddActivity.this, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                if (isStartDate) {
                    startDateText.setText(tripDateString);
                    if (isAllDay) {
                        endDateText.setText(tripDateString);
                    }
                } else {
                    if (isAllDay) {
                        startDateText.setText(tripDateString);
                    }
                    endDateText.setText(tripDateString);
                }

            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDlg(final boolean isStartTime, Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // TODO Auto-generated method stub
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                CalEventAddActivity.this, new OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                // TODO Auto-generated method stub
                String hourOfDayStr = hourOfDay + "";
                if (hourOfDay < 10) {
                    hourOfDayStr = "0" + hourOfDayStr;
                }

                String minuteStr = minute + "";
                if (minute < 10) {
                    minuteStr = "0" + minuteStr;
                }
                if (isStartTime) {
                    startTimeText.setText(hourOfDayStr + ":"
                            + minuteStr);
                } else {
                    endTimeText.setText(hourOfDayStr + ":" + minuteStr);
                }
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    /**
     * 发送CalEvent变化通知
     *
     * @param
     */
    public void sendBoradcastReceiver() {
        Intent mIntent = new Intent(Constant.ACTION_CALENDAR);
        mIntent.putExtra("refreshCalendar", "");
        // 发送广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REPEAT_TYPE_REQUEST_CODE:
                    String repeatType = data.getStringExtra("result");
                    repeatText.setText(repeatType);
                    break;
                case CAL_TYPE_REQUEST_CODE:
                    calendar = (MyCalendar) data.getSerializableExtra("result");
                    calTypeText.setText(calendar.getName());

                    calendarImg.setVisibility(View.VISIBLE);
                    calendarImg.setText(" ");
                    int color = CalendarColorUtils.getColor(
                            CalEventAddActivity.this, calendar.getColor());
                    calendarImg.setFillColor(color);
                    break;

                case CAL_ALERT_TIME_REQUEST_CODE:
                    String alertTime = data.getStringExtra("alertTime");
                    alertTimeText.setText(alertTime);
                    break;

                default:
                    break;
            }
        }
    }

    private class WebService extends APIInterfaceInstance {
        // {"id":"CEV:2c7d9ef53a9b4bc7905a0377b87f32d2"}
        @Override
        public void returnAddCalEventSuccess(GetIDResult getIDResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.calendar_add_success);
            addCalendarEvent.setId(getIDResult.getId());
            sendBoradcastReceiver();
            Intent intent = new Intent();
            intent.putExtra("addCalendarEvent", addCalendarEvent);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnAddCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(CalEventAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateCalEventSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            sendBoradcastReceiver();
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.modify_success));
            Intent intent = new Intent();
            intent.putExtra("calEvent", calEvent);
            LogUtils.debug("jason", "title=" + calEvent.getTitle());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnUpdateCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(CalEventAddActivity.this, error, errorCode);
        }

    }
}
