package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
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
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.Calendar;

/**
 * Created by libaochao on 2019/3/29.
 */
@ContentView(R.layout.activity_calendar_add)
public class CalendarAddActivity extends BaseActivity {
    @ViewInject(R.id.tv_save)
    private TextView saveText;
    @ViewInject(R.id.et_input_content)
    private EditText inputContentEdit;
    @ViewInject(R.id.switch_all_day)
    private Switch allDaySwitch;
    @ViewInject(R.id.rl_calendar_type_tip)
    private RelativeLayout calenderTypeTipLayout;
    @ViewInject(R.id.tv_calendar_type_name)
    private TextView calendarTypeNameText;
    @ViewInject(R.id.iv_calendar_type_flag)
    private ImageView calendarTypeFlagImage;
    @ViewInject(R.id.tv_start_date)
    private TextView startDateText;
    @ViewInject(R.id.tv_start_time)
    private TextView startTimeText;
    @ViewInject(R.id.tv_end_date)
    private TextView endDateText;
    @ViewInject(R.id.tv_end_time)
    private TextView endTimeText;
    @ViewInject(R.id.tv_alert_text)
    private TextView alertText;
    @ViewInject(R.id.tv_repeat_text)
    private TextView repeatText;
    @ViewInject(R.id.tv_title)
    private TextView titleText;
    @ViewInject(R.id.rl_calendar_type)
    private RelativeLayout calendarTypeLayout;
    @ViewInject(R.id.rl_start_time)
    private RelativeLayout startTimeLayout;
    @ViewInject(R.id.rl_end_time)
    private RelativeLayout endTimeLayout;
    @ViewInject(R.id.rl_alert_time)
    private RelativeLayout alertTimeLayout;

    private static final int CAL_TYPE_REQUEST_CODE = 1;
    private static final int REPEAT_TYPE_REQUEST_CODE = 2;
    private static final int CAL_ALERT_TIME_REQUEST_CODE = 3;

    public static final String EXTRA_SCHEDULE_CALENDAR_EVENT = "schedule_calendar_event";
    public static final String EXTRA_SCHEDULE_CALENDAR_REPEAT_TIME = "schedule_calendar_repeattime";
    public static final String EXTRA_SCHEDULE_CALENDAR_TYPE = "schedule_calendar_type";
    public static final String EXTRA_SCHEDULE_CALENDAR_ADD_EVENT = "schedule_calendar_add_event";
    public static final String EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT = "schedule_calendar_type_select";

    private WorkAPIService apiService;
    private LoadingDialog loadingDlg;
    private CalendarEvent calEvent;
    private MyCalendar myCalendar;
    private Boolean isAllDay = false;
    private Boolean isEditable = true;
    private String addCalendarStr;
    private DataTimePickerDialog startDataTimePickerDialog;
    private DataTimePickerDialog endDataTimePickerDialog;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private String contentText;

    private int intervalMin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        initDate();
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(getApplicationContext());
        apiService.setAPIInterface(new WebService());
        allDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isAllDay = b;
                timeTextextChangeByIsAllday(isAllDay);
            }
        });
        startDataTimePickerDialog = new DataTimePickerDialog(this);
        endDataTimePickerDialog = new DataTimePickerDialog(this);
        startDataTimePickerDialog.setDataTimePickerDialogListener(new DataTimePickerDialog.TimePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                startCalendar = calendar;
                String startDateStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, startCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                startDateText.setText(startDateStr);
                endCalendar = (Calendar) startCalendar.clone();
                endCalendar.add(Calendar.MINUTE, intervalMin);
                String endDateStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                endDateText.setText(endDateStr);
                timeTextextChangeByIsAllday(isAllDay);
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        endDataTimePickerDialog.setDataTimePickerDialogListener(new DataTimePickerDialog.TimePickerDialogInterface() {
            @Override
            public void positiveListener(Calendar calendar) {
                if (calendar.before(startCalendar)) {
                    showEndDateErrorRemindDialog();
                    return;
                }
                endCalendar = calendar;
                String endDataStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                endDateText.setText(endDataStr);
                endDataStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
                endTimeText.setText(isAllDay ? TimeUtils.getWeekDay(CalendarAddActivity.this, endCalendar) : endDataStr);
                intervalMin = (int) getIntervalMin();
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        allDaySwitch.setChecked(isAllDay);
        inputContentEdit.setText(contentText);
        titleText.setText(isEditable ? getString(R.string.schedule_calendar_add) : getString(R.string.schedule_calendar_detail));
        calendarTypeNameText.setText(isEditable ? "" : myCalendar.getName());
        calendarTypeFlagImage.setImageResource(isEditable ? R.drawable.icon_blue_circle : CalendarColorUtils.getColorCircleImage(myCalendar.getColor()));
        calenderTypeTipLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        initStartEndTimeView();
        setViewIsEditable(isEditable);
    }

    /**
     * 通过EditAble  设置可点击性
     *
     * @param isEditable 是否可编辑
     */
    private void setViewIsEditable(Boolean isEditable) {
        setEditTextState(inputContentEdit, isEditable);
        allDaySwitch.setEnabled(isEditable);
        calendarTypeLayout.setClickable(isEditable);
        startTimeLayout.setClickable(isEditable);
        endTimeLayout.setClickable(isEditable);
        alertTimeLayout.setClickable(isEditable);
        saveText.setText(isEditable ? getString(R.string.save) : getString(R.string.calendar_adjust));
    }

    /**
     * 结束时间早于起始时间提醒
     */
    private void showEndDateErrorRemindDialog() {
        new QMUIDialog.MessageDialogBuilder(this).setMessage(R.string.schedule_calendar_time_alert)
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog qmuiDialog, int i) {
                        qmuiDialog.dismiss();
                    }
                }).show();
    }

    /**
     * isAllDay change
     */
    private void timeTextextChangeByIsAllday(boolean IsAllday) {
        String startTime = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endTime = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        startTimeText.setText(IsAllday ? TimeUtils.getWeekDay(this, startCalendar) : startTime);
        endTimeText.setText(IsAllday ? TimeUtils.getWeekDay(this, endCalendar) : endTime);
    }

    /**
     * 初始化日期数据
     */
    private void initDate() {
        if (getIntent().hasExtra(EXTRA_SCHEDULE_CALENDAR_EVENT)) {
            isEditable = false;
            calEvent = (CalendarEvent) getIntent().getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_EVENT);
            isAllDay = calEvent.getAllDay();
            startCalendar = calEvent.getStartDate();
            endCalendar = calEvent.getEndDate();
            contentText = calEvent.getTitle();
            myCalendar = calEvent.getCalendar();
        }
        if (startCalendar == null) {
            startCalendar = Calendar.getInstance();
        }
        if (endCalendar == null) {
            endCalendar = (Calendar) startCalendar.clone();
            if (!isAllDay) {
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
            }
        }
        intervalMin = (int) getIntervalMin();
    }

    /**
     * 设置事件的初始开始和结束时间
     */
    private void initStartEndTimeView() {
        // TODO Auto-generated method stub
        String startDateStr = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String startTimeStr = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endDateStr = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String endTimeStr = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        startDateText.setText(startDateStr);
        startTimeText.setText(isAllDay ? TimeUtils.getWeekDay(this, startCalendar) : startTimeStr);
        endDateText.setText(endDateStr);
        endTimeText.setText(isAllDay ? TimeUtils.getWeekDay(this, endCalendar) : endTimeStr);
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
                intent = new Intent(this, CalendarTypeSelectActivity.class);
                if (myCalendar != null) {
                    intent.putExtra(EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT, myCalendar);
                }
                startActivityForResult(intent, CAL_TYPE_REQUEST_CODE);
                break;
            case R.id.rl_start_time:
                startDataTimePickerDialog.showDatePickerDialog(isAllDay, startCalendar);
                break;
            case R.id.rl_end_time:
                endDataTimePickerDialog.showDatePickerDialog(isAllDay, endCalendar);
                break;
            case R.id.rl_alert_time:
                intent.setClass(getApplicationContext(),
                        ScheduleAlertTimeActivity.class);
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, alertText.getText());
                startActivityForResult(intent, CAL_ALERT_TIME_REQUEST_CODE);
                break;
            case R.id.rl_repeat:
                break;
        }
    }

    /**
     * 存储日历事件
     */
    private void saveCalendarEvent() {
        if (isEditable == false) {
            isEditable = true;
            setViewIsEditable(isEditable);
        } else {
            String title = inputContentEdit.getText().toString();
            if (!isAbleSaveAndTips(title, startCalendar, endCalendar)) {
                return;
            }
            correctedCalendarTime();
            calEvent = ((calEvent == null) ? new CalendarEvent() : calEvent);
            calEvent.setTitle(title);
            calEvent.setAllDay(isAllDay);
            calEvent.setState("ACTIVED");
            calEvent.setStartDate(TimeUtils
                    .localCalendar2UTCCalendar(startCalendar));
            calEvent.setEndDate(TimeUtils
                    .localCalendar2UTCCalendar(endCalendar));
            addCalendarStr = JSONUtils.toJSONString(calEvent);
            calEvent.setCalendar(myCalendar);
            if (getIntent().hasExtra(EXTRA_SCHEDULE_CALENDAR_EVENT)) {
                updateCalEvent();
            } else {
                addCalEvent();
            }
        }
    }

    /**
     * 能否保存提示
     */
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
                    String calendarType = data.getStringExtra(EXTRA_SCHEDULE_CALENDAR_REPEAT_TIME);
                    repeatText.setText(calendarType);
                    break;
                case CAL_TYPE_REQUEST_CODE:
                    myCalendar = (MyCalendar) data.getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_TYPE);
                    calendarTypeLayout.setVisibility(View.VISIBLE);
                    calendarTypeNameText.setText(myCalendar.getName());
                    calendarTypeFlagImage.setImageResource(CalendarColorUtils.getColorCircleImage(myCalendar.getColor()));
                    calenderTypeTipLayout.setVisibility(View.VISIBLE);
                    break;
                case CAL_ALERT_TIME_REQUEST_CODE:
                    String alertTime = data.getStringExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    alertText.setText(alertTime);
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
    public void sendCalendarEventNotification() {
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_DATA_CHANGED, ""));
    }

    /**
     * */
    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnAddCalEventSuccess(GetIDResult getIDResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.calendar_add_success);
            calEvent.setId(getIDResult.getId());
            sendCalendarEventNotification();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SCHEDULE_CALENDAR_ADD_EVENT, calEvent);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnAddCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(CalendarAddActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateCalEventSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            sendCalendarEventNotification();
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.modify_success));
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SCHEDULE_CALENDAR_EVENT, calEvent);
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
            WebServiceMiddleUtils.hand(CalendarAddActivity.this, error, errorCode);
        }

    }

    /**
     * 获取间隔时间 单位Min（分钟）
     */
    private long getIntervalMin() {
        long interval = 0;
        if (isAllDay) {
            long remainder = endCalendar.getTimeInMillis() % (1000 * 24 * 3600);
            interval = (remainder + (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis())) / (1000 * 24 * 3600);
            interval = interval * 24 * 60;
        } else {
            interval = (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis() + 1) / (1000 * 60);
        }
        return interval;
    }

    /**
     * 上传日历时间秒毫秒单位清零矫正，allday 重设时间
     */
    private void correctedCalendarTime() {
        if (isAllDay) {
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
        }
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);
    }

}
