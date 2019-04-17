package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.adapter.ScheduleMeetingRoomDurationAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.bean.work.MeetingSchedule;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@ContentView(R.layout.activity_meeting_room_info)
public class MeetingRoomInfoActivity extends BaseActivity {

    public static final String EXTRA_MEETING_ROOM = "extra_meeting_room";
    private static final int REQUEST_MEETING_INFO = 1;
    private final String dayStartTime = "08:00";
    private final String dayEndTime = "18:00";
    private MeetingRoom meetingRoom;
    @ViewInject(R.id.tv_meeting_room_name)
    private TextView meetingRoomNameText;
    @ViewInject(R.id.tv_meeting_room_floor)
    private TextView meetingRoomFloorText;
    @ViewInject(R.id.view_pager)
    private ViewPager viewPager;
    @ViewInject(R.id.tv_people_num)
    private TextView peopleNumText;
    @ViewInject(R.id.ll_equipment)
    private LinearLayout equipmentLayout;
    @ViewInject(R.id.tv_day_before)
    private TextView dayBeforeText;
    @ViewInject(R.id.tv_day)
    private TextView dayText;
    @ViewInject(R.id.tv_day_after)
    private TextView dayAfterText;
    private ScheduleApiService apiService;
    private LoadingDialog loadingDlg;
    private int viewPagerIndex = 0;
    private List<List<MeetingSchedule>> allDaysMeetingScheduleList;
    private List<View> viewList = new ArrayList<>();
    private List<Meeting> allMeetingList = new ArrayList<>();
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        meetingRoom = (MeetingRoom) getIntent().getExtras().getSerializable(EXTRA_MEETING_ROOM);
        initView();
        getMeetingListByMeetingRoom();
    }

    private void initView() {
        // TODO Auto-generated method stub
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(this);
        meetingRoomNameText.setText(meetingRoom.getName());
        meetingRoomFloorText.setText(meetingRoom.getBuilding().getName());
        peopleNumText.setText(meetingRoom.getGalleryful() + "");
        showMeetingRoomEquipment(equipmentLayout, meetingRoom.getEquipmentList());
    }


    private void showMeetingRoomEquipment(LinearLayout equipmentLayout, List<String> equipmentList) {
        for (String equipment : equipmentList) {
            int equipmentResId = -1;
            switch (equipment) {
                case "PROJECTOR":
                    equipmentResId = R.drawable.ic_schedule_meeting_room_equipment_projector;
                    break;
                case "WHITE_BOARD":
                    equipmentResId = R.drawable.ic_schedule_meeting_room_equipment_white_borad;
                    break;
                case "CONFERENCE_PHONE":
                    equipmentResId = R.drawable.ic_schedule_meeting_room_equipment_conference_phone;
                    break;
                case "WIFI":
                    equipmentResId = R.drawable.ic_schedule_meeting_room_equipment_wifi;
                    break;
                default:
                    continue;
            }
            ImageView imageView = new ImageView(this);
            int height = DensityUtil.dip2px(this, 17);
            int marginLeft = DensityUtil.dip2px(this, 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height, height);
            params.setMargins(marginLeft, 0, 0, 0);
            imageView.setLayoutParams(params);
            imageView.setImageResource(equipmentResId);
            equipmentLayout.addView(imageView);
        }
    }

    private void setSelect() {
//        if (meetingRoom.getMaxAhead()>2){
//
//        }else {
            dayBeforeText.setText(TimeUtils.getFormatStringFromTargetTime(
                    MeetingRoomInfoActivity.this, currentCalendar, 0));
            dayAfterText.setText(TimeUtils.getFormatStringFromTargetTime(
                    MeetingRoomInfoActivity.this, currentCalendar, 1));
            dayBeforeText.setTextColor(Color.parseColor((viewPagerIndex == 0)?"#36A5F6":"#333333"));
            dayAfterText.setTextColor(Color.parseColor((viewPagerIndex == 1)?"#36A5F6":"#333333"));
//        }
    }

    /**
     * 初始化listview的显示信息
     */
    private void initListView() {
        // TODO Auto-generated method stub
        viewList.clear();
        for (int i = 0; i < meetingRoom.getMaxAhead(); i++) {
            View allDayMeetingView = LayoutInflater.from(this).inflate(
                    R.layout.meeting_room_use_day_view, null);
            viewList.add(allDayMeetingView);
            ListView meetingListView = allDayMeetingView
                    .findViewById(R.id.lv_meeting);
            final List<MeetingSchedule> meetingScheduleList = allDaysMeetingScheduleList
                    .get(i);
            meetingListView.setAdapter(new ScheduleMeetingRoomDurationAdapter(this, meetingScheduleList));
            meetingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MeetingSchedule meetingSchedule = meetingScheduleList.get(position);
                    Meeting meeting = meetingSchedule.getMeeting();
                    if (meeting == null){
                        Intent intent = new Intent();
                        intent.putExtra(MeetingRoomListActivity.EXTRA_START_TIME,TimeUtils.timeLong2Calendar(meetingSchedule.getFrom()));
                        intent.putExtra(MeetingRoomListActivity.EXTRA_END_TIME,TimeUtils.timeLong2Calendar(meetingSchedule.getTo()));
                        setResult(RESULT_OK,intent);
                        finish();
                    }else {

                    }
                }
            });

        }
        viewPager.setAdapter(new MyViewPagerAdapter(this, viewList));
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                viewPagerIndex = arg0;
                setSelect();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
        viewPager.setCurrentItem(viewPagerIndex);
        setSelect();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;

            case R.id.tv_day_before:
                viewPagerIndex =0;
                viewPager.setCurrentItem(0);
                setSelect();
                break;
            case R.id.tv_day:
                if (meetingRoom.getMaxAhead()>2){

                }
                break;
            case R.id.tv_day_after:
                viewPagerIndex =1;
                viewPager.setCurrentItem(1);
                setSelect();
                break;

            default:
                break;
        }

    }

    /**
     * 启动详情
     *
     * @param meeting
     */
    protected void showMeetingInfo(Meeting meeting) {
        Intent intent = new Intent();
        intent.putExtra("meeting", meeting);
        intent.setClass(this, MeetingDetailActivity.class);
        startActivityForResult(intent, REQUEST_MEETING_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            if (data.hasExtra("delete")) {
                Meeting meeting = (Meeting) data.getSerializableExtra("delete");
                allMeetingList.remove(meeting);
                initData();
            } else if (data.hasExtra("update")) {
                Meeting meeting = (Meeting) data.getSerializableExtra("update");
                int index = allMeetingList.indexOf(meeting);
                if (index != -1) {
                    allMeetingList.remove(index);
                    allMeetingList.add(index, meeting);
                }
                initData();
            }
        }
    }

    /**
     * 弹出取消会议提示框
     *
     * @param meeting
     */
    private void showDeleteMeetingDlg(final Meeting meeting) {
        // TODO Auto-generated method stub
        new MyQMUIDialog.MessageDialogBuilder(MeetingRoomInfoActivity.this)
                .setMessage(R.string.meeting_list_cirform)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        deleteMeeting(meeting);
                    }
                })
                .show();
    }

    /**
     * 初始化从网络获取来的数据
     */
    private void initData() {
        allDaysMeetingScheduleList = new ArrayList<>();
        for (int i = 0; i < meetingRoom.getMaxAhead(); i++) {
            boolean isMeetingBeforeDayStartTime = false;
            boolean isMeetingAfterDayEndTime = false;
            long dayStartTimeLong = TimeUtils.getTimeLongFromTargetTime(currentCalendar, i, dayStartTime);
            long dayEndTimeLong = TimeUtils.getTimeLongFromTargetTime(currentCalendar, i, dayEndTime);
            List<Meeting> dayMeetingList = new ArrayList<>();
            Calendar calendar = (Calendar) currentCalendar.clone();
            calendar.add(Calendar.DAY_OF_YEAR, i);
            HashSet<Long> set = new HashSet<>();
            for (Meeting meeting : allMeetingList) {
                LogUtils.jasonDebug("000="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),calendar,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
                LogUtils.jasonDebug("111="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),meeting.getStartTimeCalendar(),TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
                LogUtils.jasonDebug("222="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),meeting.getEndTimeCalendar(),TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
                if (TimeUtils.isContainTargetCalendarDay(calendar, meeting.getStartTimeCalendar(), meeting.getEndTimeCalendar())) {
                    dayMeetingList.add(meeting);
                    long meetingFromLong = meeting.getDayStartTime(calendar);
                    long meetingToLong = meeting.getDayEndTime(calendar);
                    if (meetingFromLong <= dayStartTimeLong) {
                        isMeetingBeforeDayStartTime = true;
                    }
                    if (meetingToLong >= dayEndTimeLong) {
                        isMeetingAfterDayEndTime = true;
                    }
                    set.add(meetingFromLong);
                    set.add(meetingToLong);
                }
            }
            List<MeetingSchedule> dayMeetingScheduleList = new ArrayList<>();
            if (!isMeetingBeforeDayStartTime) {
                set.add(dayStartTimeLong);
            }
            if (!isMeetingAfterDayEndTime) {
                set.add(dayEndTimeLong);
            }
            List<Long> listWithoutDup = new ArrayList<Long>();
            listWithoutDup.addAll(set);
            Collections.sort(listWithoutDup);// 排序
            // 将所有时间点整理成时间片段
            for (int j = 1; j < listWithoutDup.size(); j++) {
                MeetingSchedule meetingSchedule = new MeetingSchedule();
                meetingSchedule.setFrom(listWithoutDup.get(j - 1));
                meetingSchedule.setTo(listWithoutDup.get(j));
                dayMeetingScheduleList.add(meetingSchedule);
            }

            // 给meetingSchedule的meeting 成员变量赋值
            for (int j = 0; j < dayMeetingScheduleList.size(); j++) {
                MeetingSchedule meetingSchedule = dayMeetingScheduleList.get(j);
                for (int k = 0; k < dayMeetingList.size(); k++) {
                    Meeting meeting = dayMeetingList.get(k);
                    if (meetingSchedule.getFrom() == meeting.getStartTime())
                        meetingSchedule.setMeeting(meeting);
                }
            }
            allDaysMeetingScheduleList.add(dayMeetingScheduleList);
        }
        initListView();
    }

    /**
     * 获取此会议室所有的会议
     */
    private void getMeetingListByMeetingRoom() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            Calendar startCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
            Calendar endCalendar = TimeUtils.getDayEndCalendar(Calendar.getInstance());
            endCalendar.add(Calendar.DAY_OF_YEAR,meetingRoom.getMaxAhead()-1);
            apiService.getRoomMeetingListByMeetingRoom(meetingRoom.getId(),startCalendar.getTimeInMillis(),endCalendar.getTimeInMillis());
        }
    }



    /**
     * 删除会议
     */
    private void deleteMeeting(Meeting meeting) {
        if (NetUtils.isNetworkConnected(MeetingRoomInfoActivity.this)) {
            loadingDlg.show();
            apiService.deleteMeeting(meeting);
        }
    }


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnMeetingListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            allMeetingList = getMeetingListByMeetingRoomResult.getMeetingList();
            initData();
        }

        @Override
        public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingRoomInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDeleteMeetingSuccess(Meeting meeting) {
            LoadingDialog.dimissDlg(loadingDlg);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Constant.ACTION_MEETING);
            intent.putExtra("refreshMeeting", true);
            LocalBroadcastManager.getInstance(MeetingRoomInfoActivity.this).sendBroadcast(intent);
            ToastUtils.show(MeetingRoomInfoActivity.this,
                    getString(R.string.meeting_list_cancel_success));
            allMeetingList.remove(meeting);
            initData();
        }

        @Override
        public void returnDeleteMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingRoomInfoActivity.this, error, errorCode);
        }

    }

}
