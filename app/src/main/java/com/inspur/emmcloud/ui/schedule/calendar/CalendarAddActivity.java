package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.bean.schedule.RemindEvent;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.GetMyCalendarResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;
import com.inspur.emmcloud.widget.DateTimePickerDialog;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/3/29.
 */
public class CalendarAddActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String EXTRA_SCHEDULE_CALENDAR_EVENT = "schedule_calendar_event";
    public static final String EXTRA_SCHEDULE_CALENDAR_REPEAT_TIME = "schedule_calendar_repeattime";
    public static final String EXTRA_SCHEDULE_CALENDAR_TYPE = "schedule_calendar_type";
    public static final String EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT = "schedule_calendar_type_select";
    private static final String EXTRA_SELECT_CALENDAR = "extra_select_calendar";
    private static final int REQUEST_CAL_TYPE = 1;
    private static final int REQUEST_REPEAT_TYPE = 2;
    private static final int REQUEST_CAL_ALERT_TIME = 3;
    @BindView(R.id.et_input_title)
    EditText inputContentEdit;
    @BindView(R.id.switch_all_day)
    Switch allDaySwitch;
    @BindView(R.id.rl_calendar_type_tip)
    RelativeLayout calenderTypeTipLayout;
    @BindView(R.id.tv_calendar_type_name)
    TextView calendarTypeNameText;
    @BindView(R.id.iv_calendar_type_flag)
    ImageView calendarTypeFlagImage;
    @BindView(R.id.tv_start_date)
    TextView startDateText;
    @BindView(R.id.tv_start_time)
    TextView startTimeText;
    @BindView(R.id.tv_end_date)
    TextView endDateText;
    @BindView(R.id.tv_end_time)
    TextView endTimeText;
    @BindView(R.id.tv_alert_text)
    TextView alertText;
    @BindView(R.id.tv_repeat_text)
    TextView repeatText;
    @BindView(R.id.tv_title)
    TextView titleText;
    @BindView(R.id.rl_calendar_type)
    RelativeLayout calendarTypeLayout;
    @BindView(R.id.ll_start_time)
    RelativeLayout startTimeLayout;
    @BindView(R.id.ll_end_time)
    RelativeLayout endTimeLayout;
    @BindView(R.id.rl_alert_time)
    RelativeLayout alertTimeLayout;
    @BindView(R.id.iv_calendar_detail_more)
    ImageView calendarDetailMoreImageView;
    @BindView(R.id.tv_save)
    TextView saveTextView;
    RemindEvent remindEvent = new RemindEvent();
    private ScheduleApiService apiService;
    private LoadingDialog loadingDlg;
    private Schedule scheduleEvent = new Schedule();
    private MyCalendar myCalendar;
    private List<MyCalendar> calendarsList = new ArrayList<>();
    private Boolean isAllDay = false;
    private Boolean isAddCalendar = true;
    private Boolean isEditable = true;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private String contentText = "";
    private int intervalMin = 0;
    private String id;// 日程id

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        apiService = new ScheduleApiService(getApplicationContext());
        apiService.setAPIInterface(new WebService());
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_calendar_add;
    }

    /**
     * 初始化View
     */
    private void initView() {
        allDaySwitch.setOnCheckedChangeListener(this);
        allDaySwitch.setChecked(isAllDay);
        inputContentEdit.setText(contentText);
        titleText.setText(isAddCalendar ? getApplication().getString(R.string.schedule_calendar_add) :
                getApplication().getString(R.string.schedule_calendar_detail));
        calendarTypeNameText.setText(getApplication().getString(R.string.schedule_calendar_company));
        calendarTypeFlagImage.setImageResource(isAddCalendar ? R.drawable.icon_blue_circle : R.drawable.icon_blue_circle);
        calenderTypeTipLayout.setVisibility(View.VISIBLE);
        initStartEndTimeView();
        alertText.setText(remindEvent.getName());
        setViewIsEditable(isAddCalendar);
    }

    /**
     * 初始化日期数据
     */
    private void init() {
        if (getIntent().hasExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE)) {
            contentText = getIntent().getStringExtra(Constant.COMMUNICATION_LONG_CLICK_TO_SCHEDULE);
        }

        id = getIntent().getStringExtra(Constant.SCHEDULE_QUERY);   //解析通知字段获取id
        if (!TextUtils.isEmpty(id)) {        ////来自通知
            isAddCalendar = false;
            isEditable = false;
            getDbCalendarFromId();
            getNetCalendarFromId();
        } else if (getIntent().hasExtra(EXTRA_SCHEDULE_CALENDAR_EVENT)) {  //通知没有，列表页跳转过来
            isAddCalendar = false;
            isEditable = false;
            scheduleEvent = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_EVENT);
            initscheduleData();     //直接用传过来的数据
            initView();
        } else {    //创建日程
            createCalendar();
        }
    }

    /**
     * 设置日程相关数据
     */
    private void initscheduleData() {
        isAllDay = scheduleEvent.getAllDay();
        startCalendar = scheduleEvent.getStartTimeCalendar();
        endCalendar = scheduleEvent.getEndTimeCalendar();
        contentText = scheduleEvent.getTitle();
        saveTextView.setVisibility(View.GONE);
        calendarDetailMoreImageView.setVisibility(View.VISIBLE);
        List<MyCalendar> allCalendarList = MyCalendarCacheUtils.getAllMyCalendarList(getApplicationContext());
        String calendarType = scheduleEvent.getType();
        for (int i = 0; i < allCalendarList.size(); i++) {
            if (calendarType.equals(allCalendarList.get(i).getId())) {
                myCalendar = allCalendarList.get(i);
            }
        }
        String alertTimeName = ScheduleAlertTimeActivity.getAlertTimeNameByTime(JSONUtils.getInt(scheduleEvent.getRemindEvent(), "advanceTimeSpan", -1), isAllDay);
        remindEvent = new RemindEvent(JSONUtils.getString(scheduleEvent.getRemindEvent(), "remindType", "in_app"),
                JSONUtils.getInt(scheduleEvent.getRemindEvent(), "advanceTimeSpan", -1), alertTimeName);
        intervalMin = (int) getIntervalMin();
    }

    /**
     * 创建日程
     */
    private void createCalendar() {
        Calendar currentCalendar = Calendar.getInstance();
        if (getIntent().hasExtra(EXTRA_SELECT_CALENDAR)) {
            startCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_SELECT_CALENDAR);
        }
        if (startCalendar == null) {
            startCalendar = (Calendar) currentCalendar.clone();
        }
        startCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
        startCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
        startCalendar = TimeUtils.getNextHalfHourTime(startCalendar);
        endCalendar = (Calendar) startCalendar.clone();
        if (!isAllDay) {
            endCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        scheduleEvent.setOwner(MyApplication.getInstance().getUid());//??默认
        remindEvent.setName(ScheduleAlertTimeActivity.getAlertTimeNameByTime(remindEvent.getAdvanceTimeSpan(), isAllDay));
        intervalMin = (int) getIntervalMin();
        initView();
    }

    /**
     * 从数据库获取日程数据
     */
    private void getDbCalendarFromId() {
        scheduleEvent = ScheduleCacheUtils.getDBScheduleById(this, id);
        if (scheduleEvent != null) {
            initscheduleData();
            initView();
        }
    }

    /**
     * 从网络获取日程数据
     */
    private void getNetCalendarFromId() {
        if (NetUtils.isNetworkConnected(this)) {
            if (!TextUtils.isEmpty(id)) {
                if (scheduleEvent == null || TextUtils.isEmpty(scheduleEvent.getId())) { //如果缓存有数据则不显示loading
                    loadingDlg.show();
                }
                apiService.getCalendarDataFromId(id);
            }
        } else {
            ToastUtils.show(this, "");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (isEditable) {
            isAllDay = b;
            timeTextChangeByIsAllDay(isAllDay);
            remindEvent = new RemindEvent();
            alertText.setText(ScheduleAlertTimeActivity.getAlertTimeNameByTime(-1, isAllDay));
        }
    }

    /**
     * 通过EditAble  设置可点击性
     *
     * @param isEditable 是否可编辑
     */
    private void setViewIsEditable(Boolean isEditable) {
        setEditTextState(inputContentEdit, isEditable);
        allDaySwitch.setClickable(isEditable);
        calendarTypeLayout.setClickable(isEditable);
        startTimeLayout.setClickable(isEditable);
        endTimeLayout.setClickable(isEditable);
        alertTimeLayout.setClickable(isEditable);
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
     * 结束时间早于起始时间提醒
     */
    private void showEndDateErrorRemindDialog() {
        new CustomDialog.MessageDialogBuilder(this).setMessage(R.string.schedule_calendar_time_alert)
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                }).show();
    }

    /**
     * 全天及非全天UI切换
     */
    private void timeTextChangeByIsAllDay(boolean IsAllDay) {
        String startTime = TimeUtils.calendar2FormatString(this, startCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        String endTime = TimeUtils.calendar2FormatString(this, endCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        startTimeText.setText(IsAllDay ? TimeUtils.getWeekDay(this, startCalendar) : startTime);
        endTimeText.setText(IsAllDay ? TimeUtils.getWeekDay(this, endCalendar) : endTime);
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
        DateTimePickerDialog dataTimePickerDialog = new DateTimePickerDialog(this);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_calendar_detail_more:
                showDialog();
                break;
            case R.id.tv_save:
                saveCalendarEvent();
                break;
            case R.id.rl_calendar_type:
                intent = new Intent(this, CalendarTypeSelectActivity.class);
                if (myCalendar != null) {
                    intent.putExtra(EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT, myCalendar);
                }
                startActivityForResult(intent, REQUEST_CAL_TYPE);
                break;
            case R.id.ll_start_time:
                dataTimePickerDialog.setDataTimePickerDialogListener(new DateTimePickerDialog.TimePickerDialogInterface() {
                    @Override
                    public void positiveListener(Calendar calendar) {
                        startCalendar = calendar;
                        String startDateStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, startCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                        startDateText.setText(startDateStr);
                        endCalendar = (Calendar) startCalendar.clone();
                        endCalendar.add(Calendar.MINUTE, intervalMin);
                        String endDateStr = TimeUtils.calendar2FormatString(CalendarAddActivity.this, endCalendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                        endDateText.setText(endDateStr);
                        timeTextChangeByIsAllDay(isAllDay);
                    }

                    @Override
                    public void negativeListener(Calendar calendar) {

                    }
                });
                dataTimePickerDialog.showDatePickerDialog(isAllDay, startCalendar);
                break;
            case R.id.ll_end_time:
                dataTimePickerDialog.setDataTimePickerDialogListener(new DateTimePickerDialog.TimePickerDialogInterface() {
                    @Override
                    public void positiveListener(Calendar calendar) {
                        if (calendar.getTimeInMillis() - startCalendar.getTimeInMillis() < (60000)) {
                            showEndDateErrorRemindDialog();
                            endCalendar = (Calendar) startCalendar.clone();
                            endCalendar.add(Calendar.MINUTE, intervalMin);
                            return;
                        }
                        endCalendar = (Calendar) calendar.clone();
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
                dataTimePickerDialog.showDatePickerDialog(isAllDay, endCalendar);
                break;
            case R.id.rl_alert_time:
                intent.setClass(getApplicationContext(),
                        ScheduleAlertTimeActivity.class);
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME, remindEvent.getAdvanceTimeSpan());
                intent.putExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_IS_ALL_DAY, isAllDay);
                startActivityForResult(intent, REQUEST_CAL_ALERT_TIME);
                break;
            case R.id.rl_repeat:
                break;
        }
    }

    /**
     * 提示内容
     */
    private void showDialog() {
        new ActionSheetDialog.ActionListSheetBuilder(CalendarAddActivity.this)
                .addItem(getString(R.string.schedule_calendar_modify))
                .addItem(getString(R.string.schedule_calendar_delete))
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                setViewIsEditable(true);
                                isEditable = true;
                                saveTextView.setVisibility(View.VISIBLE);
                                calendarDetailMoreImageView.setVisibility(View.GONE);
                                break;
                            case 1:
                                delCalendarEvent();
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                }).build().show();
    }

    /**
     * 存储日历事件
     */
    private void saveCalendarEvent() {
        if (!checkingSaveCalendarEventAvailable())
            return;
        correctedCalendarTime();
        scheduleEvent.setTitle(contentText);
        scheduleEvent.setAllDay(isAllDay);
        scheduleEvent.setState(-1);
        scheduleEvent.setStartTime(startCalendar.getTimeInMillis());
        scheduleEvent.setEndTime(endCalendar.getTimeInMillis());
        scheduleEvent.setType("default");
        if (remindEvent.getAdvanceTimeSpan() != -1) {
            scheduleEvent.setRemindEvent(remindEvent.toJSONObject().toString());
        }
        if (isAddCalendar) {
            addCalEvent();
        } else {
            updateCalEvent();
        }
    }

    /***/
    private void delCalendarEvent() {
        if (NetUtils.isNetworkConnected(this) && (!isAddCalendar)) {
            loadingDlg.show();
            apiService.deleteSchedule(scheduleEvent.getId());
        }
    }

    /**
     * 能否保存提示
     */
    private boolean checkingSaveCalendarEventAvailable() {
        contentText = inputContentEdit.getText().toString().trim();
        if (StringUtils.isBlank(contentText)) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_please_input_title);
            return false;
        }
        if (endCalendar.before(startCalendar)) {
            LogUtils.LbcDebug(TimeUtils.calendar2FormatString(this,endCalendar,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            LogUtils.LbcDebug(TimeUtils.calendar2FormatString(this,startCalendar,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_start_or_end_time_illegal);
            return false;
        }
        if (contentText.length() > 64) {
            ToastUtils.show(getApplicationContext(),
                    R.string.calendar_title_cannot_exceed_num);
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_REPEAT_TYPE:
                    String calendarType = data.getStringExtra(EXTRA_SCHEDULE_CALENDAR_REPEAT_TIME);
                    repeatText.setText(calendarType);
                    break;
                case REQUEST_CAL_TYPE:
                    myCalendar = (MyCalendar) data.getSerializableExtra(EXTRA_SCHEDULE_CALENDAR_TYPE);
                    calendarTypeLayout.setVisibility(View.VISIBLE);
                    calendarTypeNameText.setText(myCalendar.getName());
                    calendarTypeFlagImage.setImageResource(CalendarColorUtils.getColorCircleImage(myCalendar.getColor()));
                    calenderTypeTipLayout.setVisibility(View.VISIBLE);
                    break;
                case REQUEST_CAL_ALERT_TIME:
                    remindEvent = (RemindEvent) data.getSerializableExtra(ScheduleAlertTimeActivity.EXTRA_SCHEDULE_ALERT_TIME);
                    remindEvent.setRemindType("in_app");
                    alertText.setText(remindEvent.getName());
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
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED, ""));
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
            startCalendar = TimeUtils.getDayBeginCalendar(startCalendar);
            endCalendar = TimeUtils.getDayEndCalendar(endCalendar);
        }
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 更新日程
     */
    private void updateCalEvent() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            scheduleEvent.setLastTime(System.currentTimeMillis());
            apiService.updateSchedule(scheduleEvent.toCalendarEventJSONObject().toString());
        }
    }

    /**
     * 添加事件
     */
    private void addCalEvent() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            try {
                loadingDlg.show();
                apiService.addSchedule(scheduleEvent.toCalendarEventJSONObject().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnAddScheduleSuccess(GetIDResult getIDResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(getApplicationContext(), R.string.calendar_add_success);
            scheduleEvent.setId(getIDResult.getId());
            sendCalendarEventNotification();
            finish();
        }

        @Override
        public void returnAddScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(CalendarAddActivity.this, error, errorCode);
            LogUtils.LbcDebug("add Schedule Fail");
        }

        @Override
        public void returnUpdateScheduleSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            sendCalendarEventNotification();
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.modify_success));
            //更新系日历事件
            finish();
        }

        @Override
        public void returnUpdateScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(CalendarAddActivity.this, error, errorCode);
        }

        @Override
        public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            List<MyCalendar> allCalendarList = getMyCalendarResult.getCalendarList();
            calendarsList.clear();
            calendarsList.addAll(allCalendarList);
            MyCalendarCacheUtils.saveMyCalendarList(CalendarAddActivity.this, calendarsList);
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            super.returnMyCalendarFail(error, errorCode);
        }

        @Override
        public void returnDeleteScheduleSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, null));
            finish();
        }

        @Override
        public void returnDeleteScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            super.returnDeleteScheduleFail(error, errorCode);
        }

        @Override
        public void returnScheduleDataFromIdSuccess(Schedule schedule) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (schedule != null) {
                scheduleEvent = schedule;
                initscheduleData();
                initView();
            }
        }

        @Override
        public void returnScheduleDataFromIdFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
           finish();
        }
    }


}
