package com.inspur.emmcloud.ui.schedule.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.DataTimePickerDialog;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.Calendar;

/**
 * Created by libaochao on 2019/3/27.
 */
@ContentView(R.layout.activity_schedule_add)
public class ScheduleAddActivity extends BaseActivity {
    @ViewInject(R.id.tv_save)
    private TextView saveText;
    @ViewInject(R.id.et_input_content)
    private EditText inputContentEdit;
    @ViewInject(R.id.switch_all_day)
    private SwitchView allDaySwitch;
    @ViewInject(R.id.rl_calendar_type_tip)
    private RelativeLayout calenderTypeLayout;
    @ViewInject(R.id.tv_calendar_type_name)
    private TextView calendarTypeNameText;
    @ViewInject(R.id.iv_calendar_type_flag)
    private ImageView calendarIconImage;
    @ViewInject(R.id.tv_start_date)
    private TextView startDateText;
    @ViewInject(R.id.tv_start_time)
    private TextView startTimeText;
    @ViewInject(R.id.tv_end_date)
    private TextView endDateText;
    @ViewInject(R.id.tv_end_time)
    private TextView endTimeText;
    @ViewInject(R.id.tv_alert_text)
    private TextView timeAlertText;
    @ViewInject(R.id.tv_repeat_text)
    private TextView repeatText;
    @ViewInject(R.id.tv_title)
    private TextView titleText;
    private static final int CAL_TYPE_REQUEST_CODE = 1;
    private static final int REPEAT_TYPE_REQUEST_CODE = 2;
    private static final int CAL_ALERT_TIME_REQUEST_CODE = 3;

    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private CalendarEvent calEvent;
    private MyCalendar myCalendar;
    private Boolean isAllDay = false;
    private Boolean isEditable = true;
    private String addCalendarStr;
    private DataTimePickerDialog startDataTimePickerDialog;
    private DataTimePickerDialog endDataTimePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(getApplicationContext());
        apiService.setAPIInterface(new ScheduleAddActivity.WebService());
        allDaySwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                // TODO Auto-generated method stub
                allDaySwitch.toggleSwitch(true);
                isAllDay = true;
                startTimeText.setVisibility(View.GONE);
                endTimeText.setVisibility(View.GONE);
            }

            @Override
            public void toggleToOff(View view) {
                // TODO Auto-generated method stub
                allDaySwitch.toggleSwitch(false);
                isAllDay = false;
                startTimeText.setVisibility(View.VISIBLE);
                endTimeText.setVisibility(View.VISIBLE);
            }
        });
        startDataTimePickerDialog = new DataTimePickerDialog(this);
        endDataTimePickerDialog = new DataTimePickerDialog(this);
        startDataTimePickerDialog.setDataTimePickerDialogListener(new DataTimePickerDialog.TimePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                String startDateStr = TimeUtils.calendar2FormatString(ScheduleAddActivity.this, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                startDateText.setText(startDateStr);
                startDateStr = TimeUtils.calendar2FormatString(ScheduleAddActivity.this, calendar, TimeUtils.FORMAT_HOUR_MINUTE);
                startTimeText.setText(startDateStr);
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        endDataTimePickerDialog.setDataTimePickerDialogListener(new DataTimePickerDialog.TimePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                String endDataStr = TimeUtils.calendar2FormatString(ScheduleAddActivity.this, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                endDateText.setText(endDataStr);
                endDataStr = TimeUtils.calendar2FormatString(ScheduleAddActivity.this, calendar, TimeUtils.FORMAT_HOUR_MINUTE);
                endTimeText.setText(endDataStr);
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        initDate();
    }

    /**
     * 初始化日期数据
     */
    private void initDate() {
        Calendar startCalendar = null;
        Calendar endCalendar = null;
        if (getIntent().hasExtra("calEvent")) {
            isEditable = false;
            allDaySwitch.setEnable(false);
            calEvent = (CalendarEvent) getIntent().getSerializableExtra("calEvent");
            titleText.setText(getString(R.string.calendar_detail));
            if (calEvent.getCalendar().getCommunity()) {
                saveText.setVisibility(View.GONE);
            }
            saveText.setText(getString(R.string.calendar_adjust));
            isAllDay = calEvent.getAllday();
            allDaySwitch.setOpened(isAllDay);
            startCalendar = calEvent.getLocalStartDate();
            endCalendar = calEvent.getLocalEndDate();
            String title = calEvent.getTitle();
            inputContentEdit.setText(title);
            setEditTextState(inputContentEdit, false);
            myCalendar = calEvent.getCalendar();
            if (myCalendar != null) {
                calendarTypeNameText.setText(myCalendar.getName());
                calendarIconImage.setVisibility(View.VISIBLE);
                calendarIconImage.setImageResource(CalendarColorUtils.getColorCircleImage(myCalendar.getColor()));
                calenderTypeLayout.setVisibility(View.VISIBLE);
            }
        }
        setEventTime(startCalendar, endCalendar);
        startTimeText.setVisibility(isAllDay ? View.GONE : View.VISIBLE);
        endTimeText.setVisibility(isAllDay ? View.GONE : View.VISIBLE);
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
        String startDateStr = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String startTimeStr = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endDateStr = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String endTimeStr = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
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
        editText.setFocusable(isEdit);
        editText.setFocusableInTouchMode(isEdit);
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_save:
                saveCalendarEvent();
                break;
            case R.id.rl_calendar_type:
                if (isEditable == true) {
                    intent = new Intent(this, CalendarTypeSelectActivity.class);
                    startActivityForResult(intent, CAL_TYPE_REQUEST_CODE);
                }
                break;
            case R.id.rl_start:
                if (isEditable == true) {
                    startDataTimePickerDialog.showDatePickerDialog(isAllDay, getStartCalendar());
                }
                break;
            case R.id.rl_end_date:
                if (isEditable == true) {
                    endDataTimePickerDialog.showDatePickerDialog(isAllDay, getEndCalendar());
                }
                break;
            case R.id.rl_alert_time:
                if (isEditable == true) {
                    intent.setClass(getApplicationContext(),
                            CalendarAlertTimeActivity.class);
                    intent.putExtra("alertTime", timeAlertText.getText());
                    startActivityForResult(intent, CAL_ALERT_TIME_REQUEST_CODE);
                }
                break;
            case R.id.rl_repeat:
//                if (isEditable == true) {
//                    intent.setClass(getApplicationContext(),
//                            CalendarRepeatActivity.class);
//                    intent.putExtra("repeatType", repeatText.getText());
//                    startActivityForResult(intent, REPEAT_TYPE_REQUEST_CODE);
//                }
                break;
        }
    }

    /**
     * 存储日历事件
     */
    private void saveCalendarEvent() {
        if (isEditable == false) {
            isEditable = true;
            setEditTextState(inputContentEdit, true);
            allDaySwitch.setEnable(true);
            saveText.setText(getString(R.string.save));
        } else {
            String title = inputContentEdit.getText().toString();
            Calendar startCalendar = getStartCalendar();
            Calendar endCalendar = getEndCalendar();
            if (!isAbleSaveAndTips(title, startCalendar, endCalendar)) {
                return;
            }
            if (calEvent == null) {
                calEvent = new CalendarEvent();
            }
            calEvent.setTitle(title);
            calEvent.setAllday(isAllDay);
            calEvent.setState("ACTIVED");
            calEvent.setStartDate(TimeUtils
                    .localCalendar2UTCCalendar(startCalendar));
            calEvent.setEndDate(TimeUtils
                    .localCalendar2UTCCalendar(endCalendar));
            addCalendarStr = JSONUtils.toJSONString(calEvent);
            calEvent.setCalendar(myCalendar);
            if (getIntent().hasExtra("calEvent")) {
                updateCalEvent();
            } else {
                String string = JSONUtils.toJSONString(calEvent);
                LogUtils.LbcDebug("JasonData" + string);
                // long calendarEventId= SysCalendarEventUtils.addCalendarEventToSys(this, calEvent);
                // LogUtils.LbcDebug("calendarEventId:"+calendarEventId);
                // calEvent.setSysCalendarEventId(calendarEventId);
                addCalEvent();
            }
        }
    }

    private boolean isAbleSaveAndTips(String title, Calendar startCalendar, Calendar endCalendar) {
        if (StringUtils.isBlank(title)) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_please_input_title);
            return false;
        }
        if (endCalendar.before(startCalendar)) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_start_or_end_time_illegal);
            return false;
        }
        if (myCalendar == null) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_select_calendar);
            return false;
        }
        if (title.length() > 64) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_tilte_cannot_exceed_64);
            return false;
        }
        return true;
    }

    /**
     * 生成StartCalendar
     */
    private Calendar getStartCalendar() {
        Calendar startCalendar = null;
        String startDateStr = "";
        if (isAllDay) {
            startDateStr = startDateText.getText() + " 08:00";
        } else {
            startDateStr = startDateText.getText() + " "
                    + startTimeText.getText();
        }
        startCalendar = TimeUtils.timeString2Calendar(ScheduleAddActivity.this, startDateStr,
                TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
        return startCalendar;
    }

    /**
     * 生成EndCalendar
     */
    private Calendar getEndCalendar() {
        String endDateStr = "";
        Calendar endCalendar = null;
        if (isAllDay) {
            endDateStr = endDateText.getText()
                    + " 23:59";
        } else {
            endDateStr = endDateText.getText() + " "
                    + endTimeText.getText();
        }
        endCalendar = TimeUtils.timeString2Calendar(ScheduleAddActivity.this, endDateStr,
                TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
        return endCalendar;
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
            apiService.addCalEvent(myCalendar.getId(), addCalendarStr);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REPEAT_TYPE_REQUEST_CODE:
                    String calendarType = data.getStringExtra("repeatResult");
                    repeatText.setText(calendarType);
                    break;
                case CAL_TYPE_REQUEST_CODE:
                    myCalendar = (MyCalendar) data.getSerializableExtra("result");
                    calenderTypeLayout.setVisibility(View.VISIBLE);
                    calendarTypeNameText.setText(myCalendar.getName());
                    LogUtils.LbcDebug("CalendarId::" + myCalendar.getId());
                    calendarIconImage.setImageResource(CalendarColorUtils.getColorCircleImage(myCalendar.getColor()));
                    break;
                case CAL_ALERT_TIME_REQUEST_CODE:
                    String alertTime = data.getStringExtra("alertTime");
                    timeAlertText.setText(alertTime);
                    break;
                default:
                    break;
            }
        }
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

    /**
     * 本地数据拼接CalEvent
     *
     * @param title
     * @param isAllDay
     * @param startCalendar
     * @param endCalendar
     */
    private void combineCalEvent(String title, boolean isAllDay,
                                 Calendar startCalendar, Calendar endCalendar, long calendarEventId) {
        // TODO Auto-generated method stub
//        addCalendarEvent = new CalendarEvent();
//        addCalendarEvent.setTitle(title);
//        addCalendarEvent.setAllday(isAllDay);
//        addCalendarEvent.setStartDate(TimeUtils
//                .localCalendar2UTCCalendar(startCalendar));
//        addCalendarEvent.setState("ACTIVED");
//        if (!isAllDay) {
//            addCalendarEvent.setEndDate(TimeUtils
//                    .localCalendar2UTCCalendar(endCalendar));
//        }
//        addCalendarEvent.setSysCalendarEventId(calendarEventId);
//        addCalendarEvent.setCalendar(myCalendar);
//        addCalendarJson = JSONUtils.toJSONString(addCalendarEvent);
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnAddCalEventSuccess(GetIDResult getIDResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.calendar_add_success);
            calEvent.setId(getIDResult.getId());
            sendBoradcastReceiver();
            Intent intent = new Intent();
            intent.putExtra("addCalendarEvent", calEvent);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnAddCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ScheduleAddActivity.this, error, errorCode);
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
            //更新系日历事件
            finish();
        }

        @Override
        public void returnUpdateCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(ScheduleAddActivity.this, error, errorCode);
        }

    }

}
