package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.adapter.ScheduleMeetingRoomDurationAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.bean.work.GetMeetingListResult;
import com.inspur.emmcloud.bean.work.MeetingSchedule;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@ContentView(R.layout.activity_meeting_room_info)
public class MeetingRoomInfoActivity extends BaseActivity {

    private static final String EXTRA_MEETING_ROOM = "extra_meeting_room";
    private static final int REQUEST_MEETING_INFO = 1;
    private MeetingRoom meetingRoom;
    @ViewInject(R.id.tv_meeting_room_name)
    private TextView meetingRoomNameText;
    @ViewInject(R.id.tv_meeting_room_floor)
    private TextView meetingRoomFloorText;
    @ViewInject(R.id.view_pager)
    private ViewPager viewPager;
    @ViewInject(R.id.tv_date)
    private TextView dateText;
    @ViewInject(R.id.rl_day_before)
    private RelativeLayout dayBeforeLayout;
    @ViewInject(R.id.rl_day_after)
    private RelativeLayout dayAfterLayout;
    @ViewInject(R.id.tv_people_num)
    private TextView peopleNumText;
    @ViewInject(R.id.ll_equipment)
    private LinearLayout equipmentLayout;

    private final String dayStartTime = "08:00";
    private final String dayEndTime = "18:00";
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
//        getMeetingListByMeetingRoom();
        getMeetingList();
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

    /**
     * 从服务器获取当前时间
     *
     * @param date
     */
    private void setCurrentCalendar(String date) {
        // TODO Auto-generated method stub
        try {
            String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            Date currentDate = format.parse(date);
            currentCalendar.setTime(currentDate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setSelect() {
        dayBeforeLayout.setVisibility((viewPagerIndex == 0) ? View.GONE : View.VISIBLE);
        dayAfterLayout.setVisibility((viewPagerIndex == meetingRoom.getMaxAhead() - 1) ? View.GONE : View.VISIBLE);
        dateText.setText(TimeUtils.getFormatStringFromTargetTime(
                MeetingRoomInfoActivity.this, currentCalendar, viewPagerIndex));
    }

    /**
     * 初始化listview的显示信息
     */
    private void initListView() {
        // TODO Auto-generated method stub
        viewList.clear();
        for (int i = 0; i < meetingRoom.getMaxAhead(); i++) {
            View allDayMeetingView = LayoutInflater.from(this).inflate(
                    R.layout.all_day_meeting_fragment, null);
            viewList.add(allDayMeetingView);
            ListView meetingListView = allDayMeetingView
                    .findViewById(R.id.meeting_list);
            List<MeetingSchedule> meetingScheduleList = allDaysMeetingScheduleList
                    .get(i);
            meetingListView.setAdapter(new ScheduleMeetingRoomDurationAdapter(this,meetingScheduleList));

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

            case R.id.rl_day_before:
                viewPagerIndex--;
                viewPager.setCurrentItem(viewPagerIndex);
                setSelect();
                break;

            case R.id.rl_day_after:
                viewPagerIndex++;
                viewPager.setCurrentItem(viewPagerIndex);
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
                if (TimeUtils.isContainTargetCalendarDay(calendar, meeting.getStartTimeCalendar(), meeting.getEndTimeCalendar())){
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
        }
        initListView();
    }

    /**
     * 构造今天和明天会议显示数据
     */
    private void initMeetingSchedule(List<List<Meeting>> group) {
        // TODO Auto-generated method stub
        allDaysMeetingScheduleList = new ArrayList<List<MeetingSchedule>>();
        for (int i = 0; i < group.size(); i++) {
            List<Meeting> dayMeetingList = group.get(i);
            List<MeetingSchedule> dayMeetingScheduleList = new ArrayList<MeetingSchedule>();
            HashSet<Long> set = new HashSet<Long>();
            long dayStartTimeLong = TimeUtils.getTimeLongFromTargetTime(
                    currentCalendar, i, dayStartTime);
            long dayEndTimeLong = TimeUtils.getTimeLongFromTargetTime(
                    currentCalendar, i, dayEndTime);

            boolean isMeetingBeforeDayStartTime = false;
            boolean isMeetingAfterDayEndTime = false;
            // 添加所有会议的开头和结尾时间点
            for (int j = 0; j < dayMeetingList.size(); j++) {
                long meetingFromLong = dayMeetingList.get(j).getStartTime();
                long meetingToLong = dayMeetingList.get(j).getEndTime();
                if (meetingFromLong <= dayStartTimeLong) {
                    isMeetingBeforeDayStartTime = true;
                }
                if (meetingToLong >= dayEndTimeLong) {
                    isMeetingAfterDayEndTime = true;
                }
                set.add(meetingFromLong);
                set.add(meetingToLong);
            }

            if (!isMeetingBeforeDayStartTime) {
                set.add(dayStartTimeLong);
            }
            if (!isMeetingAfterDayEndTime) {
                set.add(dayEndTimeLong);
            }

            List<Long> listWithoutDup = new ArrayList<>();
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

    }


    /**
     * 获取此会议室所有的会议
     */
    private void getMeetingListByMeetingRoom() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.getRoomMeetingListByMeetingRoom(meetingRoom.getId());
        }
    }

    private void getMeetingList(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            Calendar startCalendar = TimeUtils.getDayBeginCalendar(Calendar.getInstance());
            Calendar endCalendar = (Calendar) startCalendar.clone();
            endCalendar.add(Calendar.DAY_OF_YEAR,2);
            apiService.getScheduleList(startCalendar, endCalendar,
                    0, 0, 0, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
            loadingDlg.show();
            apiService.getRoomMeetingListByMeetingRoom(meetingRoom.getId());
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
        public void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar,
                                              Calendar endCalendar, List<String> calendarIdList, List<String> meetingIdList, List<String> taskIdList) {
            List<Meeting> meetingList = getScheduleListResult.getMeetingList();
            allMeetingList.clear();
            for (Meeting meeting:meetingList){
                if (meeting.getScheduleLocationObj().getId().equals(meetingRoom.getId())){
                    allMeetingList.add(meeting);
                }
            }
            initData();
        }

        @Override
        public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult, String date) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
//            beforeDayLayout.setVisibility(View.VISIBLE);
//            afterDayLayout.setVisibility(View.VISIBLE);
//            setCurrentCalendar(date);
//            allMeetingList = getMeetingListResult.getMeetingsList();
            initData();

        }

        @Override
        public void returnMeetingListFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
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
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(MeetingRoomInfoActivity.this, error, errorCode);
        }

    }

}
