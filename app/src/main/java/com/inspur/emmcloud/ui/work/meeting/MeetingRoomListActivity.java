package com.inspur.emmcloud.ui.work.meeting;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetMeetingRoomsResult;
import com.inspur.emmcloud.bean.work.GetOfficeResult;
import com.inspur.emmcloud.bean.work.MeetingArea;
import com.inspur.emmcloud.bean.work.MeetingRoom;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MeetingRoomListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int CHANGE_LOCATION = 0;
    private static final int CHECK_MEETING_ROOM_DETAIL = 1;
    private static final int MEETING_ROOM_BEGIN_DATE = 2;
    private static final int MEETING_ROOM_END_DATE = 3;
    private static final int MEETING_ROOM_BEGIN_TIME = 4;
    private static final int MEETING_ROOM_END_TIME = 5;
    private static final int CREATE_COMMON_OFFICE = 6;
    private ExpandableListView expandListView;
    private MyAdapter adapter;
    private LoadingDialog loadingDlg;
    private WorkAPIService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout headLayout;
    private ArrayList<MeetingArea> meetingAreas = new ArrayList<MeetingArea>();
    private TextView beginDateText, beginTimeText, endDateText, endTimeText;

    private PopupWindow popupWindow;
    private Calendar beginCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private List<String> selectCommonOfficeIdList = new ArrayList<String>();
    private ImageView filteImg;
    private boolean isAfterFilte = false;
    private boolean isHasModifyTime = false;
    private boolean isFirstTime = true;
    private boolean isSetIntentTime = true; //是否需要设置从预订页面传过来的会议起止时间
    private BroadcastReceiver meetingReceiver;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_rooms);
        initViews();
        getCommonOfficeSpace(true);
        registerMeetingReceiver();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //当预订会议室界面时间传递过来时候默认设置为过滤条件
        if (hasFocus && isSetIntentTime && getIntent().hasExtra("filterBeginCalendar")) {
            showPopupWindow(headLayout);
        }
    }

    /**
     * 注册刷新任务的广播
     */
    private void registerMeetingReceiver() {
        meetingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("refreshMeeting")) {
                    if (NetUtils.isNetworkConnected(MeetingRoomListActivity.this)) {
                        getFilteMeetingRooms(beginCalendar.getTimeInMillis(), endCalendar.getTimeInMillis(), isAfterFilte, true);
                    }
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constant.ACTION_MEETING);
        LocalBroadcastManager.getInstance(this).registerReceiver(meetingReceiver, myIntentFilter);
    }

    /**
     * 变更会议，取消会议下拉框
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        isHasModifyTime = false;
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(MeetingRoomListActivity.this)
                .inflate(R.layout.pop_filter_window_view, null);
        // 设置按钮的点击事件
        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setTouchable(true);
        initPopViews(popupWindow, contentView);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view);
        popupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (isHasModifyTime) {
                    filteImg.setImageResource(R.drawable.icon_after_filte);
                    getFilteMeetingRooms(beginCalendar.getTimeInMillis(),
                            endCalendar.getTimeInMillis(), true, true);
                    isAfterFilte = true;
                } else if (isAfterFilte) {
                    filteImg.setImageResource(R.drawable.icon_after_filte);
                } else {
                    filteImg.setImageResource(R.drawable.icon_before_filte);
                }
            }
        });
    }

    /**
     * 初始化popupWindow的layout
     *
     * @param popupWindow
     * @param contentView
     */
    private void initPopViews(final PopupWindow popupWindow, View contentView) {
        beginDateText = (TextView) contentView
                .findViewById(R.id.meeting_room_filter_from_date_text);
        beginTimeText = (TextView) contentView
                .findViewById(R.id.meeting_room_filter_from_time_text);
        endDateText = (TextView) contentView
                .findViewById(R.id.meeting_room_filter_end_date_text);
        endTimeText = (TextView) contentView
                .findViewById(R.id.meeting_room_filter_end_time_text);

        resetBtn = (Button) contentView.findViewById(R.id.meeting_room_filter_cancel_btn);
        resetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                handleReset();
                resetBtn.setTextColor(0xff999999);
            }
        });
        if (isSetIntentTime && getIntent().hasExtra("filterBeginCalendar")) {
            isFirstTime = false;
            isSetIntentTime = false;
            isAfterFilte = true;
            long beginCalendarLong = getIntent().getLongExtra("filterBeginCalendar", 0L);
            beginCalendar = TimeUtils.timeLong2Calendar(beginCalendarLong);
            long endCalendarLong = getIntent().getLongExtra("filterEndCalendar", 0L);
            endCalendar = TimeUtils.timeLong2Calendar(endCalendarLong);
        }
        if (isAfterFilte) {
            resetBtn.setTextColor(Color.BLACK);
        }
        if (isFirstTime) {
            endDateText.setClickable(false);
            endDateText.setTextColor(0x33333333);
        } else {
            endDateText.setClickable(true);
            endDateText.setTextColor(0xff0F7BCA);
        }
        if (!isFirstTime && isAfterFilte) {
            beginDateText.setText(TimeUtils.calendar2FormatString(
                    MeetingRoomListActivity.this, beginCalendar,
                    TimeUtils.FORMAT_YEAR_MONTH_DAY));
            beginTimeText.setText(TimeUtils.calendar2FormatString(
                    MeetingRoomListActivity.this, beginCalendar,
                    TimeUtils.FORMAT_HOUR_MINUTE));
            endDateText.setText(TimeUtils.calendar2FormatString(
                    MeetingRoomListActivity.this, endCalendar,
                    TimeUtils.FORMAT_YEAR_MONTH_DAY));
            endTimeText.setText(TimeUtils.calendar2FormatString(
                    MeetingRoomListActivity.this, endCalendar,
                    TimeUtils.FORMAT_HOUR_MINUTE));
        } else {
            beginDateText.setText(getString(R.string.meeting_date_default));
            endDateText.setText(getString(R.string.meeting_date_default));
            beginTimeText.setText("");
            endTimeText.setText("");
            endDateText.setClickable(false);
            endDateText.setTextColor(0x33333333);
        }
    }

    /**
     * 初始化views
     */
    private void initViews() {
        apiService = new WorkAPIService(MeetingRoomListActivity.this);
        apiService.setAPIInterface(new WebService());
        adapter = new MyAdapter(MeetingRoomListActivity.this);
        loadingDlg = new LoadingDialog(MeetingRoomListActivity.this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(MeetingRoomListActivity.this);
        headLayout = (RelativeLayout) findViewById(R.id.rl_header);
        filteImg = (ImageView) findViewById(R.id.filte_img);
        expandListView = (ExpandableListView) findViewById(R.id.expandable_list);
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        expandListView.setOnChildClickListener(new RoomChildClickListener());
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.meeting_room_office_img:
                intent.setClass(MeetingRoomListActivity.this,
                        MyCommonOfficeActivity.class);
                startActivityForResult(intent, CHANGE_LOCATION);
                break;
            case R.id.meeting_room_filter_from_date_text:
                showDateDialog(MEETING_ROOM_BEGIN_DATE);
                break;
            case R.id.meeting_room_filter_from_time_text:
                showTimeDialog(beginCalendar.get(Calendar.HOUR_OF_DAY),
                        beginCalendar.get(Calendar.MINUTE), MEETING_ROOM_BEGIN_TIME);
                break;
            case R.id.meeting_room_filter_end_date_text:
                showDateDialog(MEETING_ROOM_END_DATE);
                break;
            case R.id.meeting_room_filter_end_time_text:
                showTimeDialog(endCalendar.get(Calendar.HOUR_OF_DAY),
                        endCalendar.get(Calendar.MINUTE), MEETING_ROOM_END_TIME);
                break;
            case R.id.meeting_room_filter_outside_layout:
            case R.id.meeting_room_filter_confirm_btn:
            case R.id.meeting_room_filter_layout:
                LogUtils.debug("yfcLog", "isFirstTime:" + isFirstTime);
                if (isFirstTime) {
                    getNoFilteMeetingRooms(true);
//				isAfterFilte = true;
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    break;
                } else {
                    long beginTimeLong = beginCalendar.getTimeInMillis();
                    long endTimeLong = endCalendar.getTimeInMillis();
                    if (beginCalendar.after(endCalendar)) {
                        ToastUtils.show(getApplicationContext(),
                                R.string.calendar_start_or_end_time_illegal);
                        break;
                    }
                    filteImg.setImageResource(R.drawable.icon_after_filte);
                    isAfterFilte = true;
                    isHasModifyTime = false;
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    getFilteMeetingRooms(beginTimeLong, endTimeLong, true, true);
                }

                break;
            case R.id.meeting_room_filter_cancel_btn:
                handleReset();
                break;
            case R.id.filte_img:
                showPopupWindow(headLayout);
                break;
            case R.id.ok_text:
                break;
            default:
                break;
        }

    }

    /**
     * 处理重置逻辑
     */
    private void handleReset() {
        LogUtils.debug("yfcLog", "处理重置逻辑");
        beginDateText.setText(getString(R.string.meeting_date_default));
        endDateText.setText(getString(R.string.meeting_date_default));
        beginTimeText.setText("");
        endTimeText.setText("");
        endDateText.setClickable(false);
        endDateText.setTextColor(0x33333333);
        this.beginCalendar = Calendar.getInstance();
        this.endCalendar = Calendar.getInstance();
        if (isAfterFilte) {
            isHasModifyTime = false;
            filteImg.setImageResource(R.drawable.icon_before_filte);
        }
        isAfterFilte = false;
        isFirstTime = true;
        isHasModifyTime = false;
    }

    /**
     * 日期选择
     *
     * @param beginOrEnd
     */
    private void showDateDialog(final int beginOrEnd) {

        Calendar calendar = Calendar.getInstance();
        if (beginOrEnd == MEETING_ROOM_BEGIN_DATE) {
            calendar = beginCalendar;
        } else {
            calendar = endCalendar;
        }
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(
                MeetingRoomListActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        if (beginOrEnd == MEETING_ROOM_BEGIN_DATE) {
                            beginCalendar.set(Calendar.YEAR, year);
                            beginCalendar.set(Calendar.MONTH, monthOfYear);
                            beginCalendar
                                    .set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            beginDateText.setText(TimeUtils
                                    .calendar2FormatString(
                                            MeetingRoomListActivity.this,
                                            beginCalendar,
                                            TimeUtils.FORMAT_YEAR_MONTH_DAY));
                            if (isFirstTime) {
                                handleFirstSetTime(true);
                            }
                            isHasModifyTime = true;
                            resetBtn.setTextColor(Color.BLACK);
                        } else if (beginOrEnd == MEETING_ROOM_END_DATE) {
                            endCalendar.set(Calendar.YEAR, year);
                            endCalendar.set(Calendar.MONTH, monthOfYear);
                            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            endDateText.setText(TimeUtils
                                    .calendar2FormatString(
                                            MeetingRoomListActivity.this,
                                            endCalendar,
                                            TimeUtils.FORMAT_YEAR_MONTH_DAY));
                        }
                        isFirstTime = false;
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setHideYear();
        datePickerDialog.show();
    }

    /**
     * 初次设置时间的时候进行的时间设置
     *
     * @param isSetBeginTimeHalfHour 是否将开始时间设置为整半小时
     */
    protected void handleFirstSetTime(boolean isSetBeginTimeHalfHour) {
        if (isSetBeginTimeHalfHour) {
            beginCalendar = TimeUtils.getNextHalfHourTime(beginCalendar);
        }
        beginTimeText.setText(TimeUtils.calendar2FormatString(
                MeetingRoomListActivity.this, beginCalendar,
                TimeUtils.FORMAT_HOUR_MINUTE));
        endCalendar = (Calendar) beginCalendar.clone();
        endCalendar.add(Calendar.HOUR_OF_DAY, 2);
        endDateText.setText(TimeUtils.calendar2FormatString(
                MeetingRoomListActivity.this, endCalendar,
                TimeUtils.FORMAT_YEAR_MONTH_DAY));
        endTimeText.setText(TimeUtils.calendar2FormatString(
                MeetingRoomListActivity.this, endCalendar,
                TimeUtils.FORMAT_HOUR_MINUTE));
        endDateText.setClickable(true);
        endDateText.setTextColor(0xff0F7BCA);
    }

    /**
     * 弹出时间选择Dialog
     *
     * @param hour
     * @param minute
     * @param beginOrEnd
     */
    private void showTimeDialog(int hour, int minute, final int beginOrEnd) {
        isHasModifyTime = true;
        TimePickerDialog beginTimePickerDialog = new TimePickerDialog(
                MeetingRoomListActivity.this, AlertDialog.THEME_HOLO_LIGHT, new OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                if (beginOrEnd == MEETING_ROOM_BEGIN_TIME) {
                    beginCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    beginCalendar.set(Calendar.MINUTE, minute);
//							beginTimeText.setText(TimeUtils
//									.calendar2FormatString(
//											MeettingRoomListActivity.this,
//											beginCalendar,
//											TimeUtils.FORMAT_HOUR_MINUTE));
                    handleFirstSetTime(false);
                } else if (beginOrEnd == MEETING_ROOM_END_TIME) {
                    endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endCalendar.set(Calendar.MINUTE, minute);
                    endTimeText.setText(TimeUtils
                            .calendar2FormatString(
                                    MeetingRoomListActivity.this,
                                    endCalendar,
                                    TimeUtils.FORMAT_HOUR_MINUTE));
                }
            }
        }, hour, minute, true);
        beginTimePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHANGE_LOCATION:
                    String uid = ((MyApplication) getApplicationContext()).getUid();
                    String selectCommonOfficeIds = PreferencesUtils.getString(
                            getApplicationContext(), MyApplication.getInstance().getTanent() + uid
                                    + "selectCommonOfficeIds");
                    selectCommonOfficeIdList = JSONUtils.JSONArray2List(selectCommonOfficeIds, new ArrayList<String>());
                    getNoFilteMeetingRooms(true);
                    break;
                case CHECK_MEETING_ROOM_DETAIL:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case CREATE_COMMON_OFFICE:
                    getCommonOfficeSpace(true);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 去创建常用办公地点
     */
    private void creatCommonOffice() {
        Intent intent = new Intent(getApplicationContext(),
                CreateCommonOfficeSpaceActivity.class);
        startActivityForResult(intent, CREATE_COMMON_OFFICE);
    }

    @Override
    public void onRefresh() {
        if (NetUtils.isNetworkConnected(MeetingRoomListActivity.this)) {
            if (isAfterFilte) {
                getFilteMeetingRooms(beginCalendar.getTimeInMillis(),
                        endCalendar.getTimeInMillis(), true, false);
            } else {
                getCommonOfficeSpace(false);
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }


    }

    /**
     * 处理成员数量
     *
     * @param meetingMember
     * @param groupPosition
     * @param childPosition
     */
    public void handleMemberNum(TextView meetingMember, int groupPosition,
                                int childPosition) {
        int meetingMemberNum = meetingAreas.get(groupPosition)
                .getMeetingRooms().get(childPosition).getGalleryful();
        meetingMember.setText("" + meetingMemberNum);
    }

    /**
     * 处理是否会议中
     *
     * @param nowState
     * @param groupPosition
     * @param childPosition
     */
    public void handleLights(ImageView nowState, int groupPosition,
                             int childPosition) {
        String light = "";
        if (!TextUtils.isEmpty(meetingAreas.get(groupPosition)
                .getMeetingRooms().get(childPosition).getLight())) {
            light = meetingAreas.get(groupPosition).getMeetingRooms()
                    .get(childPosition).getLight();
        }
        if (light.equals("GREEN")) {
            nowState.setImageResource(R.drawable.icon_meeting_free);
        } else if (light.equals("RED")) {
            nowState.setImageResource(R.drawable.icon_meeting_busy);
        } else {
            nowState.setImageResource(R.drawable.icon_meeting_free_gray);
        }
    }

    /**
     * 处理equips
     *
     * @param groupPosition
     * @param childPosition
     * @param equipsImg
     */
    public void handleEquips(ImageView[] equipsImg, int groupPosition,
                             int childPosition) {
        ArrayList<String> equipmentList = meetingAreas.get(groupPosition).getMeetingRooms().get(childPosition).getEquipmentList();

        for (int i = 0; i < 4; i++) {
            equipsImg[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < equipmentList.size(); i++) {
            equipsImg[i].setVisibility(View.VISIBLE);
            if (!StringUtils.isBlank(equipmentList.get(i))
                    && equipmentList.get(i).equals("PROJECTOR")) {
                equipsImg[i].setImageResource(R.drawable.icon_meeting_projector);
            } else if (!StringUtils.isBlank(equipmentList.get(i))
                    && equipmentList.get(i).equals("WHITE_BOARD")) {
                equipsImg[i].setImageResource(R.drawable.icon_white_board);
            } else if (!StringUtils.isBlank(equipmentList.get(i))
                    && equipmentList.get(i).equals("CONFERENCE_PHONE")) {
                equipsImg[i].setImageResource(R.drawable.icon_meeting_phone);
            } else if (!StringUtils.isBlank(equipmentList.get(i))
                    && equipmentList.get(i).equals("WIFI")) {
                equipsImg[i].setImageResource(R.drawable.icon_meeting_wifi);
            }
        }
    }

    /**
     * 处理busyDegree
     *
     * @param imageView
     * @param groupPosition
     * @param childPosition
     * @param day
     */
    public void handleBusyDegree(ImageView imageView, int groupPosition,
                                 int childPosition, int day) {
        if (!StringUtils.isEmpty(meetingAreas.get(groupPosition)
                .getMeetingRooms().get(childPosition).getBusyDegreeList().get(day)
                + "")) {
            int busyDegree = Integer.parseInt(meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(childPosition).getBusyDegreeList().get(day));
            if (busyDegree < 40) {
                imageView.setImageResource(R.drawable.icon_meeting_empty);
            } else if (busyDegree < 70) {
                imageView.setImageResource(R.drawable.icon_meeting_full);
            } else {
                imageView.setImageResource(R.drawable.icon_meeting_half);
            }
        } else {
            imageView.setImageResource(R.drawable.icon_meeting_gray);
        }
    }

    /**
     * 获取常用办公地点
     */
    private void getCommonOfficeSpace(boolean isShowDlg) {
        String uid = ((MyApplication) getApplicationContext()).getUid();
        String selectCommonOfficeIds = PreferencesUtils.getString(
                getApplicationContext(), MyApplication.getInstance().getTanent() + uid
                        + "selectCommonOfficeIds");
        selectCommonOfficeIdList = JSONUtils.JSONArray2List(selectCommonOfficeIds, new ArrayList<String>());
        if (selectCommonOfficeIdList.size() > 0) {
            getNoFilteMeetingRooms(isShowDlg);
        } else if (NetUtils.isNetworkConnected(getApplicationContext())) {
            getOffice(isShowDlg);
        }
    }

    /**
     * 获取办公地点
     */
    private void getOffice(boolean isShowDlg) {
        loadingDlg.show(isShowDlg);
        apiService.getOffice();
    }

    /**
     * 获取没有筛选条件下的会议室
     *
     * @param isShowDlg
     */
    private void getNoFilteMeetingRooms(boolean isShowDlg) {
//		Calendar calendar = Calendar.getInstance();
//		long today = calendar.getTimeInMillis();
//		long threeDays = 24 * 60 * 60 * 1000 * 3;
        getFilteMeetingRooms(0, 0, false,
                isShowDlg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (meetingReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(meetingReceiver);
            meetingReceiver = null;
        }
    }

    /**
     * 获取过滤后的会议室
     *
     * @param beginTimeLong
     * @param endTimeLong
     * @param isFilte
     * @param isShowDlg
     */
    private void getFilteMeetingRooms(long beginTimeLong, long endTimeLong,
                                      boolean isFilte, boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(MeetingRoomListActivity.this)) {
            loadingDlg.show(isShowDlg);
            String uid = ((MyApplication) getApplicationContext()).getUid();
            String selectCommonOfficeIds = PreferencesUtils.getString(
                    getApplicationContext(), MyApplication.getInstance().getTanent() + uid
                            + "selectCommonOfficeIds");
            List<String> selectCommonOfficeIdList = JSONUtils.JSONArray2List(selectCommonOfficeIds, new ArrayList<String>());
            // 修改开关
            apiService.getFiltMeetingRooms(beginTimeLong, endTimeLong,
                    selectCommonOfficeIdList, isFilte);
        }

    }

    /**
     * expandableListView适配器
     */
    public class MyAdapter extends BaseExpandableListAdapter {
        ExpandableListView expandableListView;
        private Context context;

        public MyAdapter(Context context, List<String> group,
                         List<List<String>> child) {
            this.context = context;

        }

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getGroupCount() {
            return meetingAreas.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return meetingAreas.get(groupPosition).getMeetingRooms().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return meetingAreas.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return meetingAreas.get(groupPosition).getMeetingRooms()
                    .get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * 显示：group
         */
        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.meeting_rooms_group_item, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView
                        .findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(meetingAreas.get(groupPosition).getName());
            return convertView;

        }

        /**
         * 显示：child
         */
        @Override
        public View getChildView(final int groupPosition,
                                 final int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            ExpandViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.meeting_rooms_child_item, null);
                holder = new ExpandViewHolder();
                holder.roomNum = (TextView) convertView
                        .findViewById(R.id.meeting_room_list_number_text);
                holder.today = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_today_img);
                holder.torrow = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_tommrow_img);
                holder.after = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_after_img);
                holder.meetingMember = (TextView) convertView
                        .findViewById(R.id.meeting_room_list_members_text);
                holder.nowState = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_now_state_img);
                holder.relLayout = (RelativeLayout) convertView
                        .findViewById(R.id.refresh_layout);
                holder.equipsImg[0] = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_equipment_img);
                holder.equipsImg[1] = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_whiteboard_img);
                holder.equipsImg[2] = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_phone_img);
                holder.equipsImg[3] = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_wifi_img);
                holder.nextImage = (ImageView) convertView
                        .findViewById(R.id.meeting_room_list_next_img);
                convertView.setTag(holder);
            } else {
                holder = (ExpandViewHolder) convertView.getTag();
            }

            holder.roomNum.setText(meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(childPosition).getName());
            holder.nextImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    MeetingRoom meetingRoom = meetingAreas.get(groupPosition)
                            .getMeetingRooms().get(childPosition);
                    String bid = meetingRoom.getMeetingId();
                    String roomName = meetingRoom.getName();
//					String equips[] = meetingRoom.getEquipment();
                    ArrayList<String> equipmentList = meetingRoom.getEquipmentList();
                    int meetingMember = meetingRoom.getGalleryful();
                    String shortName = meetingAreas.get(groupPosition)
                            .getName();
                    intent.setClass(MeetingRoomListActivity.this,
                            MeetingsRoomDetailActivity.class);
                    intent.putExtra("maxAhead", meetingRoom.getMaxAhead());
                    intent.putExtra("maxDuration", meetingRoom.getMaxDuration());
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("equips", equipmentList);
                    intent.putExtra("meetingMember", meetingMember);
                    intent.putExtra("shortName", shortName);
                    intent.putExtra("bid", bid);
                    startActivityForResult(intent, CHECK_MEETING_ROOM_DETAIL);
                }
            });

            int busyDegreeSize = meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(childPosition).getBusyDegreeList().size();
            if (busyDegreeSize == 1) {
                handleBusyDegree(holder.today, groupPosition, childPosition, 0);
                holder.torrow.setVisibility(View.GONE);
            } else if (busyDegreeSize >= 2) {
                handleBusyDegree(holder.today, groupPosition, childPosition, 0);
                handleBusyDegree(holder.torrow, groupPosition, childPosition, 1);
            } else {
                holder.today.setVisibility(View.GONE);
                holder.torrow.setVisibility(View.GONE);
            }
            handleEquips(holder.equipsImg, groupPosition, childPosition);
            handleLights(holder.nowState, groupPosition, childPosition);
            handleMemberNum(holder.meetingMember, groupPosition, childPosition);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        class ViewHolder {
            TextView textView;
        }

        class ExpandViewHolder {
            TextView roomNum;
            ImageView today;
            ImageView torrow;
            ImageView after;
            // TextView equiment;
            TextView meetingMember;
            ImageView nowState;
            ImageView nextImage;
            ImageView[] equipsImg = new ImageView[4];

            RelativeLayout relLayout;
        }

    }

    class RoomChildClickListener implements OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {
            String room = meetingAreas.get(groupPosition).getMeetingRooms()
                    .get(childPosition).getName();
            String roomid = meetingAreas.get(groupPosition).getMeetingRooms()
                    .get(childPosition).getMeetingId();
            String roomFlour = meetingAreas.get(groupPosition).getName();
            Intent intent = new Intent();
            intent.putExtra("maxAhead", meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(childPosition).getMaxAhead());
            intent.putExtra("maxDuration", meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(childPosition).getMaxDuration());
            intent.putExtra("flour", roomFlour);
            intent.putExtra("room", room);
            intent.putExtra("roomid", roomid);
            intent.putExtra("admin", meetingAreas.get(groupPosition)
                    .getMeetingRooms().get(0).getAdmin());
            if (isAfterFilte) {
                intent.putExtra("beginTime", beginCalendar.getTimeInMillis());
                intent.putExtra("endTime", endCalendar.getTimeInMillis());
            }
            setResult(RESULT_OK, intent);
            finish();
            return false;
        }

    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingRoomsFail(String error, int errorCode) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(MeetingRoomListActivity.this, error, errorCode);
        }

        @Override
        public void returnMeetingRoomsSuccess(
                GetMeetingRoomsResult getMeetingRoomsResult, boolean isFilte) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            swipeRefreshLayout.setRefreshing(false);
            if (getMeetingRoomsResult.getMeetingAreas().size() > 0) {
                meetingAreas = getMeetingRoomsResult.getMeetingAreas();
                expandListView.setAdapter(adapter);
            } else {
                meetingAreas.clear();
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnOfficeResultSuccess(GetOfficeResult getOfficeResult) {
            if (getOfficeResult.getOfficeList().size() != 0) {
                getNoFilteMeetingRooms(false);
            } else {
                if (loadingDlg.isShowing()) {
                    loadingDlg.dismiss();
                }
                creatCommonOffice();
            }
        }

        @Override
        public void returnOfficeResultFail(String error, int errorCode) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(MeetingRoomListActivity.this, error, errorCode);
        }

    }


}
