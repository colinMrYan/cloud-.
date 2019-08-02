package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleMeetingRoomAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.DateTimePickerDialog;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingRoomListResult;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoomArea;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/4/10.
 */
public class MeetingRoomListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ExpandableListView.OnChildClickListener {
    public static final String EXTRA_START_TIME = "extra_start_time";
    public static final String EXTRA_END_TIME = "extra_end_time";
    public static final String EXTRA_MEETING_ROOM = "extra_meeting_room";
    private static final int REQUEST_MEETING_OFFICE_SETTING = 1;
    private static final int REQUEST_ENTER_MEETING_ROOM_INFO = 2;
    @BindView(R.id.swipe_refresh_layout)
    MySwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.expandable_list_view)
    ExpandableListView expandableListView;
    @BindView(R.id.tv_start_date)
    TextView startDateText;
    @BindView(R.id.tv_start_time)
    TextView startTimeText;
    @BindView(R.id.tv_end_date)
    TextView endDateText;
    @BindView(R.id.tv_end_time)
    TextView endTimeText;
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    //    private LoadingDialog loadingDlg;
    private WebService webService;
    private ScheduleApiService apiService;
    private List<String> officeIdList = new ArrayList<>();
    private List<MeetingRoomArea> meetingRoomAreaList = new ArrayList<>();
    private ScheduleMeetingRoomAdapter meetingRoomAdapter;
    private MeetingRoom selectMeetingRoom;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        startTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_START_TIME);
        endTimeCalendar = (Calendar) getIntent().getSerializableExtra(EXTRA_END_TIME);
        setMeetingTime();
        onRefresh();
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_room_list;
    }

    private void initView() {
        swipeRefreshLayout.setOnRefreshListener(this);
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        expandableListView.setOnChildClickListener(this);
        meetingRoomAdapter = new ScheduleMeetingRoomAdapter(MeetingRoomListActivity.this);
        expandableListView.setAdapter(meetingRoomAdapter);
        webService = new WebService();
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(webService);
    }


    @Override
    public void onRefresh() {
        getMeetingRoomList();
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
        selectMeetingRoom = meetingRoomAreaList.get(groupPosition).getMeetingRoomList().get(childPosition);
        Intent intent = new Intent(MeetingRoomListActivity.this, MeetingRoomInfoActivity.class);
        intent.putExtra(MeetingRoomInfoActivity.EXTRA_MEETING_ROOM, selectMeetingRoom);
        startActivityForResult(intent, REQUEST_ENTER_MEETING_ROOM_INFO);
//        Intent intent = new Intent();
//        intent.putExtra(EXTRA_START_TIME, startTimeCalendar);
//        intent.putExtra(EXTRA_END_TIME, endTimeCalendar);
//        intent.putExtra(EXTRA_MEETING_ROOM, meetingRoom);
//        setResult(RESULT_OK, intent);
//        finish();
        return false;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ibt_config:
                setMeetingOffice();
                break;
            case R.id.ll_start_time:
                showTimeSelectDialog(true);
                break;
            case R.id.ll_end_time:
                showTimeSelectDialog(false);
                break;

        }
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
                        showTimeInvalidDlg();
                        return;
                    }
                }
                setMeetingTime();
                getMeetingRoomList();
            }

            @Override
            public void negativeListener(Calendar calendar) {

            }
        });
        startDataTimePickerDialog.showDatePickerDialog(false, isStartTime ? startTimeCalendar : endTimeCalendar);
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
    private void showTimeInvalidDlg() {
        new CustomDialog.MessageDialogBuilder(this).setMessage(R.string.schedule_calendar_time_alert)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_MEETING_OFFICE_SETTING) {
                // getOfficeList();
                getMeetingRoomList();
            } else if (requestCode == REQUEST_ENTER_MEETING_ROOM_INFO) {
                if(getIntent() != null && getIntent().hasExtra(EXTRA_START_TIME)){
                    data.putExtra(EXTRA_MEETING_ROOM, selectMeetingRoom);
                    setResult(RESULT_OK, data);
                    finish();
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_START_TIME, data.getSerializableExtra(EXTRA_START_TIME));
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_END_TIME, data.getSerializableExtra(EXTRA_END_TIME));
                    bundle.putSerializable(MeetingRoomInfoActivity.EXTRA_MEETING_ROOM, selectMeetingRoom);
                    bundle.putString(ScheduleAddActivity.EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE, AccountType.APP_MEETING.toString());
                    IntentUtils.startActivity(this, ScheduleAddActivity.class, bundle, true);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_COMMON_OFFICE_CHANGED:
                getMeetingRoomList();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setMeetingOffice() {
        Intent intent = new Intent(this, MeetingOfficeSettingActivity.class);
        startActivityForResult(intent, REQUEST_MEETING_OFFICE_SETTING);
    }


    private void getMeetingRoomList() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(true);
            }
            apiService.getMeetingRoomList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingRoomListFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(MeetingRoomListActivity.this, error, errorCode);
        }

        @Override
        public void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomListResult) {
            swipeRefreshLayout.setRefreshing(false);
            meetingRoomAreaList.clear();
            meetingRoomAdapter.setData(meetingRoomAreaList);
            meetingRoomAreaList = getMeetingRoomListResult.getMeetingRoomAreaList();
            meetingRoomAdapter.setData(meetingRoomAreaList);
            for (int i=0;i<meetingRoomAreaList.size();i++){
                expandableListView.collapseGroup(i);
                expandableListView.expandGroup(i);
            }
        }
    }
}
