package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.adapter.ScheduleMeetingRoomDurationAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoom;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingSchedule;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeetingRoomInfoActivity extends BaseActivity {

    public static final String EXTRA_MEETING_ROOM = "extra_meeting_room";
    private static final int REQUEST_MEETING_INFO = 1;
    private final String dayStartTime = "08:00";
    private final String dayEndTime = "18:00";
    @BindView(R.id.tv_meeting_room_name)
    TextView meetingRoomNameText;
    @BindView(R.id.tv_meeting_room_floor)
    TextView meetingRoomFloorText;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tv_people_num)
    TextView peopleNumText;
    @BindView(R.id.ll_equipment)
    LinearLayout equipmentLayout;
    @BindView(R.id.tl_meeting_tab)
    TabLayout tabLayout;
    private MeetingRoom meetingRoom;
    private ScheduleApiService apiService;
    private LoadingDialog loadingDlg;
    private List<List<MeetingSchedule>> allDaysMeetingScheduleList;
    private List<View> viewList = new ArrayList<>();
    private List<Meeting> allMeetingList = new ArrayList<>();
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        meetingRoom = (MeetingRoom) getIntent().getExtras().getSerializable(EXTRA_MEETING_ROOM);
        initView();
        getMeetingListByMeetingRoom();
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_room_info;
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
        initTabLayout();
    }

    private void initTabLayout() {
        List<String> tabTitleList = getTabTitleList();
        for (int i = 0; i < tabTitleList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabTitleList.get(i)), i == 0);
        }
        setTabLayoutWidth();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (viewPager != null) {
                    viewPager.setCurrentItem(position);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    private List<String> getTabTitleList() {
        if (meetingRoom.getMaxAhead() > 2) {
            return createTabDay(7);
        } else {
            return createTabDay(2);
        }
    }

    private List<String> createTabDay(int count) {
        ArrayList<String> tabTitleList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tabTitleList.add(TimeUtils.getFormatStringFromTargetTime(
                    MeetingRoomInfoActivity.this, currentCalendar, i));
        }
        return tabTitleList;
    }

    /**
     * 设置layout的宽度
     */
    private void setTabLayoutWidth() {
        try {
            //拿到tabLayout的mTabStrip属性
            Field mTabStripField = tabLayout.getClass().getDeclaredField("mTabStrip");
            mTabStripField.setAccessible(true);
            LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(tabLayout);
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                View tabView = mTabStrip.getChildAt(i);
                //拿到tabView的mTextView属性
                Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                mTextViewField.setAccessible(true);
                TextView mTextView = (TextView) mTextViewField.get(tabView);
                tabView.setPadding(0, 0, 0, 0);
                //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                int width = 0;
                width = mTextView.getWidth();
                if (width == 0) {
                    mTextView.measure(0, 0);
                    width = mTextView.getMeasuredWidth();
                }
                //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                params.width = width;
                params.leftMargin = getTabWith(width);
                params.rightMargin = getTabWith(width);
                tabView.setLayoutParams(params);
                tabView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据当前tab的宽度，计算tab两侧应该加的间距
     *
     * @param width
     * @return
     */
    private int getTabWith(int width) {
        if (meetingRoom.getMaxAhead() > 2) {
            return (ResolutionUtils.getWidth(this) - width * 3) / 6;
        } else {
            return (ResolutionUtils.getWidth(this) - width * 2) / 4;
        }
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
                    if (meeting == null) {
                        Intent intent = new Intent();
                        intent.putExtra(MeetingRoomListActivity.EXTRA_START_TIME, TimeUtils.timeLong2Calendar(meetingSchedule.getFrom()));
                        intent.putExtra(MeetingRoomListActivity.EXTRA_END_TIME, TimeUtils.timeLong2Calendar(meetingSchedule.getTo()));
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
                        IntentUtils.startActivity(MeetingRoomInfoActivity.this, com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity.class, bundle);
                    }
                }
            });

        }
        viewPager.setAdapter(new MyViewPagerAdapter(this, viewList));
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                if (tabLayout != null) {
                    tabLayout.getTabAt(arg0).select();
                }
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
        viewPager.setCurrentItem(0);
        tabLayout.getTabAt(0).select();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                getMeetingListByMeetingRoom();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 弹出取消会议提示框
     *
     * @param meeting
     */
    private void showDeleteMeetingDlg(final Meeting meeting) {
        // TODO Auto-generated method stub
        new CustomDialog.MessageDialogBuilder(MeetingRoomInfoActivity.this)
                .setMessage(R.string.meeting_list_cirform)
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    deleteMeeting(meeting);
                })
                .show();
    }

    /**
     * 初始化从网络获取来的数据
     */
    private void initData() {
        allDaysMeetingScheduleList = new ArrayList<>();
        Collections.sort(allMeetingList, new Comparator<Meeting>() {
            @Override
            public int compare(Meeting o1, Meeting o2) {
                int startDiff = (int) (o1.getStartTime() - o2.getStartTime());
                if (startDiff == 0) {
                    int endDiff = (int) (o1.getEndTime() - o2.getEndTime());
                    return endDiff;
                } else {
                    return startDiff;
                }
            }
        });
        for (int i = 0; i < meetingRoom.getMaxAhead(); i++) {
            long dayStartTimeLong = TimeUtils.getTimeLongFromTargetTime(currentCalendar, i, i == 0 ? TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(System.currentTimeMillis()), TimeUtils.FORMAT_HOUR_MINUTE) : dayStartTime);
            long dayEndTimeLong = TimeUtils.getTimeLongFromTargetTime(currentCalendar, i, dayEndTime);
            List<Meeting> dayMeetingList = new ArrayList<>();
            Calendar calendar = (Calendar) currentCalendar.clone();
            calendar.add(Calendar.DAY_OF_YEAR, i);
            for (Meeting meeting : allMeetingList) {
                if (TimeUtils.isContainTargetCalendarDay(calendar, meeting.getStartTimeCalendar(), meeting.getEndTimeCalendar())) {
                    dayMeetingList.add(meeting);
                }
            }

            List<MeetingSchedule> dayMeetingScheduleList = new ArrayList<>();
            for (int j = 0; j < dayMeetingList.size(); j++) {
                Meeting meeting = dayMeetingList.get(j);

                long meetingDayStartTime = meeting.getDayStartTime(calendar);
                long meetingDayEndTime = meeting.getDayEndTime(calendar);
                long LastMeetingEnd = (j > 0) ? dayMeetingList.get(j - 1).getDayEndTime(calendar) : dayStartTimeLong;
                if (meetingDayStartTime > LastMeetingEnd && (meetingDayStartTime > System.currentTimeMillis())) {
                    MeetingSchedule meetingSchedule = new MeetingSchedule((i == 0 && (LastMeetingEnd < System.currentTimeMillis())) ? System.currentTimeMillis() : LastMeetingEnd, meetingDayStartTime, null);
                    dayMeetingScheduleList.add(meetingSchedule);
                }
                MeetingSchedule meetingSchedule = new MeetingSchedule(meetingDayStartTime, meetingDayEndTime, meeting);
                dayMeetingScheduleList.add(meetingSchedule);
            }
            if (dayMeetingList.size() > 0) {
                long dayLastMeetingEnd = dayMeetingList.get(dayMeetingList.size() - 1).getDayEndTime(calendar);
                if (dayLastMeetingEnd < dayEndTimeLong) {
                    MeetingSchedule meetingSchedule = new MeetingSchedule((i == 0 && dayLastMeetingEnd <
                            System.currentTimeMillis()) ? System.currentTimeMillis() : dayLastMeetingEnd, dayEndTimeLong, null);
                    dayMeetingScheduleList.add(meetingSchedule);
                }
            } else {
                MeetingSchedule meetingSchedule = new MeetingSchedule(dayStartTimeLong, dayEndTimeLong, null);
                dayMeetingScheduleList.add(meetingSchedule);
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
            endCalendar.add(Calendar.DAY_OF_YEAR, meetingRoom.getMaxAhead() - 1);
            apiService.getRoomMeetingListByMeetingRoom(meetingRoom.getId(), startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());
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
            initData();
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
