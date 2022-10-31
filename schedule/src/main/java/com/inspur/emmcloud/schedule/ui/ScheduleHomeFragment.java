package com.inspur.emmcloud.schedule.ui;

import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CustomScrollViewPager;
import com.inspur.emmcloud.baselib.widget.popmenu.DropPopMenu;
import com.inspur.emmcloud.baselib.widget.popmenu.MenuItem;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.adapter.ScheduleHomeFragmentAdapter;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.ui.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.schedule.ui.meeting.MeetingFragment;
import com.inspur.emmcloud.schedule.ui.meeting.MeetingHistoryActivity;
import com.inspur.emmcloud.schedule.ui.meeting.MeetingRoomListActivity;
import com.inspur.emmcloud.schedule.ui.meeting.ScheduleAddActivity;
import com.inspur.emmcloud.schedule.ui.task.TaskAddActivity;
import com.inspur.emmcloud.schedule.ui.task.TaskFragment;
import com.inspur.emmcloud.schedule.ui.task.TaskSetActivity;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yufuchang on 2019/3/28.
 */
public class ScheduleHomeFragment extends BaseFragment {

    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    @BindView(R2.id.tab_layout_schedule)
    TabLayout tabLayout;
    @BindView(R2.id.view_pager_all_schedule)
    CustomScrollViewPager viewPager;
    @BindView(R2.id.ibt_today)
    ImageButton todayImgBtn;
    @BindView(R2.id.tv_date)
    TextView dateText;
    @BindView(R2.id.tv_error_info)
    TextView errorInfoText;
    @BindView(R2.id.rl_error)
    RelativeLayout errorLayout;
    private ScheduleFragment scheduleFragment;
    private MeetingFragment meetingFragment;
    private TaskFragment taskFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        setFragmentStatusBarCommon();
        initView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setFragmentStatusBarCommon();
            refreshDate();
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void refreshDate() {
        dateText.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "");
        scheduleFragment.updateCurrentDate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFragmentScheme(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.OPEN_SCHEDULE_DETAIL:
                openScheduleDetail(eventMessage);
                break;
            case Constant.EVENTBUS_TAG_EWS_401:
                if (errorLayout.getVisibility() != View.VISIBLE) {
                    errorLayout.setVisibility(View.VISIBLE);
                    String accountStr = (eventMessage.getMessageObj() instanceof String) ? eventMessage.getMessageObj().toString() : "";
                    errorInfoText.setText(getString(R.string.schedule_exchange_login_fail, accountStr));
                }
                break;
            case Constant.EVENTBUS_TAG_SCHEDULE_HIDE_EXCHANGE_ACCOUNT_ERROR:
                if (errorLayout.getVisibility() == View.VISIBLE) {
                    errorLayout.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void openScheduleDetail(SimpleEventMessage eventMessage) {
        switch ((String) eventMessage.getMessageObj()) {
            case Constant.ACTION_CALENDAR:
                if (tabLayout != null) {
                    tabLayout.getTabAt(0).select();
                }
                break;
            case Constant.ACTION_MEETING:
                if (tabLayout != null) {
                    tabLayout.getTabAt(1).select();
                    if (meetingFragment != null) {
                        meetingFragment.getMeetingList();
                    }
                }
                break;
            case Constant.ACTION_TASK:
                if (tabLayout != null) {
                    tabLayout.getTabAt(2).select();
                }
                break;
            default:
                if (tabLayout != null) {
                    tabLayout.getTabAt(0).select();
                }
                break;
        }
    }

    /**
     * 记录用户点击
     *
     * @param functionId
     */
    private void recordUserClickWorkFunction(String functionId) {
        PVCollectModelCacheUtils.saveCollectModel(functionId, "work");
    }

    private void initView() {
        initTabLayout();
        viewPager.setScrollable(false);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (tabLayout != null) {
                    tabLayout.getTabAt(position).select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //建一个存放fragment的集合，并且把新的fragment放到集合中
        scheduleFragment = new ScheduleFragment();
        meetingFragment = new MeetingFragment();
        taskFragment = new TaskFragment();
        List<Fragment> list = new ArrayList<Fragment>();
        list.add(scheduleFragment);
        list.add(meetingFragment);
        //开滦云+屏蔽任务
        String appVersionFlag = AppUtils.getManifestAppVersionFlag(BaseApplication.getInstance());
        if (appVersionFlag == null || !appVersionFlag.equals("kailuanyunjia")) {
            list.add(taskFragment);
        }
        //初始化adapter
        ScheduleHomeFragmentAdapter adapter = new ScheduleHomeFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        //将适配器和ViewPager结合
        viewPager.setAdapter(adapter);

    }

    private void initTabLayout() {
        List<Integer> tabTitleIdList = new ArrayList<>();
        tabTitleIdList.add(R.string.schedule_work_schedule);
        tabTitleIdList.add(R.string.schedule_work_meeting_text);
        String appVersionFlag = AppUtils.getManifestAppVersionFlag(BaseApplication.getInstance());
        //开滦云+屏蔽任务
        if (appVersionFlag == null || !appVersionFlag.equals("kailuanyunjia")) {
            tabTitleIdList.add(R.string.schedule_work_mession);
        }
        for (int i = 0; i < tabTitleIdList.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setCustomView(R.layout.schedule_tablayout_text_item_view);
            TextView textView = tab.getCustomView().findViewById(R.id.tv_tab);
            textView.setText(getString(tabTitleIdList.get(i)));
            updateTabLayoutTextStatus(tab, (i == 0));
            tabLayout.addTab(tab);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                dateText.setVisibility((position == 0) ? View.VISIBLE : View.INVISIBLE);
                todayImgBtn.setVisibility((position == 0) ? View.VISIBLE : View.INVISIBLE);
                updateTabLayoutTextStatus(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabLayoutTextStatus(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    /**
     * 更新TabLayout文字状态-选中状态和未选中状态
     *
     * @param tab
     * @param isSelect
     */
    private void updateTabLayoutTextStatus(TabLayout.Tab tab, boolean isSelect) {
        TextView textView = tab.getCustomView().findViewById(R.id.tv_tab);
        tab.getCustomView().findViewById(R.id.tv_tab).setSelected(isSelect);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, isSelect ? 20 : 18);
        int textColorNormal = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.schedule_tab_text_color_normal);
        int textColorSelect = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.schedule_tab_text_color_select);
        textView.setTextColor(ContextCompat.getColor(BaseApplication.getInstance(), isSelect ? textColorSelect : textColorNormal));
    }

    private String getExchangeScheduleCalendar() {
        List<ScheduleCalendar> scheduleCalendars = ScheduleCalendarCacheUtils.getScheduleCalendarList(getActivity());
        for (int i = 0; i < scheduleCalendars.size(); i++) {
            if (scheduleCalendars.get(i).getAcType().equals(AccountType.EXCHANGE.toString())) {
                return scheduleCalendars.get(i).getId();
            }
        }
        return "";
    }

    private void showAddPopMenu(View view) {
        DropPopMenu dropPopMenu = new DropPopMenu(getActivity());
        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                Bundle bundle = new Bundle();
                String calendarType;
                bundle.putSerializable(ScheduleAddActivity.EXTRA_SELECT_CALENDAR, scheduleFragment.getSelectCalendar());
                switch (menuItem.getItemId()) {
                    case 1:
                        recordUserClickWorkFunction(PV_COLLECTION_CAL);
                        calendarType = getExchangeScheduleCalendar();
                        bundle.putString(ScheduleAddActivity.EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE,
                                StringUtils.isBlank(calendarType) ? AccountType.APP_SCHEDULE.toString() : calendarType);
                        IntentUtils.startActivity(getActivity(), ScheduleAddActivity.class, bundle);
                        break;
                    case 2:
                        if (viewPager.getCurrentItem() == 2) {
                            recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                            IntentUtils.startActivity(getActivity(), TaskAddActivity.class);
                        } else {
                            recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                            calendarType = getExchangeScheduleCalendar();
                            bundle.putString(ScheduleAddActivity.EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE,
                                    StringUtils.isBlank(calendarType) ? AccountType.APP_MEETING.toString() : calendarType);
                            IntentUtils.startActivity(getActivity(), ScheduleAddActivity.class, bundle);
                        }
                        break;
                    case 3:
                        if (viewPager.getCurrentItem() == 0) {
                            recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                            IntentUtils.startActivity(getActivity(), TaskAddActivity.class);
                        } else if (viewPager.getCurrentItem() == 1) {
                            recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                            calendarType = getExchangeScheduleCalendar();
                            bundle.putString(ScheduleAddActivity.EXTRA_SCHEDULE_SCHEDULECALENDAR_TYPE,
                                    StringUtils.isBlank(calendarType) ? AccountType.APP_MEETING.toString() : calendarType);
                            IntentUtils.startActivity(getActivity(), ScheduleAddActivity.class, bundle);
                        }
                        break;
                    case 4:
                        recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                        IntentUtils.startActivity(getActivity(), MeetingRoomListActivity.class);
                        break;
                    case 5:
                        if (viewPager.getCurrentItem() == 0) {
                            IntentUtils.startActivity(getActivity(), CalendarSettingActivity.class);
                        } else if (viewPager.getCurrentItem() == 1) {
                            IntentUtils.startActivity(getActivity(), MeetingHistoryActivity.class);
                        } else if (viewPager.getCurrentItem() == 2) {
                            IntentUtils.startActivity(getActivity(), TaskSetActivity.class);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        List<MenuItem> list = getAddMenuList();
        dropPopMenu.setMenuList(list);
        dropPopMenu.show(view);
    }

    private List<MenuItem> getAddMenuList() {
        List<MenuItem> menuItemList = new ArrayList<>();
        switch (viewPager.getCurrentItem()) {
            case 0:
                menuItemList.add(new MenuItem(R.drawable.schedule_add_calendar_ic, 1, getActivity().getString(R.string.schedule_calendar_create)));
                menuItemList.add(new MenuItem(R.drawable.schedule_add_meeting_ic, 2, getString(R.string.schedule_meeting_room_booking)));
                menuItemList.add(new MenuItem(R.drawable.schedule_add_task_ic, 3, getActivity().getString(R.string.schedule_task_create)));
                menuItemList.add(new MenuItem(R.drawable.schedule_add_meeting_room_ic, 4, getActivity().getString(R.string.schedule_meeting_booking_room)));
                menuItemList.add(new MenuItem(R.drawable.schedule_setting_ic, 5, getString(R.string.settings)));
                break;
            case 1:
                menuItemList.add(new MenuItem(R.drawable.schedule_add_meeting_ic, 3, getString(R.string.schedule_meeting_room_booking)));
                menuItemList.add(new MenuItem(R.drawable.schedule_add_meeting_room_ic, 4, getActivity().getString(R.string.schedule_meeting_booking_room)));
                menuItemList.add(new MenuItem(R.drawable.schedule_meeting_history_ic, 5, getActivity().getString(R.string.schedule_meeting_history)));
                break;
            case 2:
                menuItemList.add(new MenuItem(R.drawable.schedule_add_task_ic, 2, getActivity().getString(R.string.schedule_task_create)));
                menuItemList.add(new MenuItem(R.drawable.schedule_setting_ic, 5, getString(R.string.settings)));
                break;
        }
        return menuItemList;
    }

    @OnClick({R2.id.ibt_add, R2.id.ibt_today, R2.id.rl_error})
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ibt_add) {
            showAddPopMenu(view);

        } else if (i == R.id.ibt_today) {
            scheduleFragment.setScheduleBackToToday();

        } else if (i == R.id.rl_error) {
            Bundle bundle = new Bundle();
            bundle.putString("from", "schedule_exchange_login");
            ARouter.getInstance().build(Constant.AROUTER_CLASS_MAIL_LOGIN).with(bundle).navigation();

        }
    }

}
