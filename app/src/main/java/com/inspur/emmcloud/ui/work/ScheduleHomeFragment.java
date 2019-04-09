package com.inspur.emmcloud.ui.work;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragment;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleHomeFragmentAdapter;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.ui.schedule.task.TaskAddActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingBookingActivity;
import com.inspur.emmcloud.ui.work.task.AllTaskListFragment;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.CustomScrollViewPager;
import com.inspur.emmcloud.widget.popmenu.DropPopMenu;
import com.inspur.emmcloud.widget.popmenu.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/3/28.
 */
public class ScheduleHomeFragment extends BaseFragment implements View.OnClickListener {

    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private View rootView;
    private TabLayout tabLayout;
    private ImageButton backToToDayImgBtn;
    private CustomScrollViewPager allScheduleFragmentViewPager;
    private ScheduleFragment scheduleFragment;
    private ScheduleFragment meetingFragment;
    private AllTaskListFragment allTaskFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_schedule_home, null);
        initView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarWhite();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_schedule_home, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
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
        rootView.findViewById(R.id.ibt_add).setOnClickListener(this);
        backToToDayImgBtn = rootView.findViewById(R.id.ibt_back_to_today);
        backToToDayImgBtn.setOnClickListener(this);
        allScheduleFragmentViewPager = rootView.findViewById(R.id.view_pager_all_schedule);
        allScheduleFragmentViewPager.setScrollable(false);
        allScheduleFragmentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        meetingFragment = new ScheduleFragment();
        allTaskFragment = new AllTaskListFragment();
        List<Fragment> list = new ArrayList<Fragment>();
        list.add(scheduleFragment);
        list.add(meetingFragment);
        list.add(allTaskFragment);

        //初始化adapter
        ScheduleHomeFragmentAdapter adapter = new ScheduleHomeFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        //将适配器和ViewPager结合
        allScheduleFragmentViewPager.setAdapter(adapter);

    }

    private void initTabLayout() {
        tabLayout = rootView.findViewById(R.id.tab_layout_schedule);
        int[] tabTitleResIds = {R.string.work_schedule, R.string.work_meeting_text, R.string.work_mession};
        for (int i = 0; i < tabTitleResIds.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setCustomView(R.layout.schedule_tablayout_text_item_view);
            TextView textView = tab.getCustomView().findViewById(R.id.tv_tab);
            textView.setText(getString(tabTitleResIds[i]));
            updateTabLayoutTextStatus(tab, (i == 0));
            tabLayout.addTab(tab);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (allScheduleFragmentViewPager != null) {
                    allScheduleFragmentViewPager.setCurrentItem(position);
                }
                backToToDayImgBtn.setVisibility((position == 0) ? View.VISIBLE : View.INVISIBLE);
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
     * @param tab
     * @param isSelect
     */
    private void updateTabLayoutTextStatus(TabLayout.Tab tab, boolean isSelect) {
        TextView textView = tab.getCustomView().findViewById(R.id.tv_tab);
        tab.getCustomView().findViewById(R.id.tv_tab).setSelected(isSelect);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, isSelect ? 20 : 18);
        textView.setTextColor(Color.parseColor(isSelect ? "#333333" : "#888888"));
    }


    private void showAddPopMenu(View view) {
        DropPopMenu dropPopMenu = new DropPopMenu(getActivity());
        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 1:
                        recordUserClickWorkFunction(PV_COLLECTION_CAL);
                        IntentUtils.startActivity(getActivity(), CalendarAddActivity.class);
                        break;
                    case 2:
                        recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                        IntentUtils.startActivity(getActivity(), TaskAddActivity.class);
                        break;
                    case 3:
                        break;
                    case 4:
                        recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                        IntentUtils.startActivity(getActivity(), MeetingBookingActivity.class);
                        break;
                    case 5:
                        if (allScheduleFragmentViewPager.getCurrentItem() == 0) {
                            IntentUtils.startActivity(getActivity(), CalendarSettingActivity.class);
                        }
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
        switch (allScheduleFragmentViewPager.getCurrentItem()) {
            case 0:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_calendar, 1, "新建日程"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_task, 2, "新建任务"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting, 3, "新建会议"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting_room, 4, "预定会议室"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_setting, 5, getString(R.string.settings)));
                break;
            case 1:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting, 3, "新建会议"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting_room, 4, "预定会议室"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_setting, 5, "历史会议"));
                break;
            case 2:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_task, 2, "新建任务"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_setting, 5, getString(R.string.settings)));
                break;
        }
        return menuItemList;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_add:
                showAddPopMenu(view);
                break;
            case R.id.ibt_back_to_today:
                scheduleFragment.setScheduleBackToToday();
                break;
        }
    }
}
