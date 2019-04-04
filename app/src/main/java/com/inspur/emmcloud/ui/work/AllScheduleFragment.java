package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.inspur.emmcloud.BaseFragment;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllScheduleFragmentAdapter;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarSettingActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.popmenu.DropPopMenu;
import com.inspur.emmcloud.widget.popmenu.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/3/28.
 */
public class AllScheduleFragment extends BaseFragment implements View.OnClickListener {

    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private View rootView;
    private TabLayout scheduleTabLayout;
    private ImageButton backToToDayImgBtn;
    private ViewPager allScheduleFragmentViewPager;
    private ScheduleFragment scheduleFragment;
    private Fragment meetingFragment;
    private Fragment taskFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_all_schedule, null);
        initView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarWhite();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_all_schedule, container,
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
        scheduleTabLayout = rootView.findViewById(R.id.tab_layout_schedule);
        scheduleTabLayout.addTab(scheduleTabLayout.newTab().setText(R.string.work_schedule));
        scheduleTabLayout.addTab(scheduleTabLayout.newTab().setText(R.string.work_meeting_text));
        scheduleTabLayout.addTab(scheduleTabLayout.newTab().setText(R.string.work_mession));
        scheduleTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(allScheduleFragmentViewPager != null){
                    allScheduleFragmentViewPager.setCurrentItem(position);
                }
                backToToDayImgBtn.setVisibility((position==0)?View.VISIBLE:View.INVISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        rootView.findViewById(R.id.ibt_add).setOnClickListener(this);
        backToToDayImgBtn = rootView.findViewById(R.id.ibt_back_to_today);
        backToToDayImgBtn.setOnClickListener(this);
        allScheduleFragmentViewPager = rootView.findViewById(R.id.all_schedule_viewpager);
        allScheduleFragmentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LogUtils.jasonDebug("onPageSelected=="+position);
                if(scheduleTabLayout != null){
                    scheduleTabLayout.getTabAt(position).select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //建一个存放fragment的集合，并且把新的fragment放到集合中
        scheduleFragment = new ScheduleFragment();
        meetingFragment = new NotSupportFragment();
        taskFragment = new NotSupportFragment();
        List<Fragment> list = new ArrayList<Fragment>();
        list.add(scheduleFragment);
        list.add(meetingFragment);
        list.add(taskFragment);

        //初始化adapter
        AllScheduleFragmentAdapter adapter = new AllScheduleFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        //将适配器和ViewPager结合
        allScheduleFragmentViewPager.setAdapter(adapter);

    }


    private void showAddPopMenu(View view) {
        DropPopMenu dropPopMenu = new DropPopMenu(getActivity());
        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case 1:
                        recordUserClickWorkFunction(PV_COLLECTION_CAL);
                        IntentUtils.startActivity(getActivity(), CalendarAddActivity.class);
                    break;
                    case 2:
                        recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                        IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                        break;
                    case 3:
                        break;
                    case 4:
                        recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                        IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                        break;
                    case 5:
                        if (allScheduleFragmentViewPager.getCurrentItem() == 0){
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

    private List<MenuItem> getAddMenuList(){
        List<MenuItem> menuItemList = new ArrayList<>();
        switch (allScheduleFragmentViewPager.getCurrentItem()){
            case 0:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_calendar, 1, "新建日程"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_task, 2,  "新建任务"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting, 3, "新建会议"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting_room, 4, "预定会议室"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_setting, 5, getString(R.string.settings)));
                break;
            case 1:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting, 3, "新建会议"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_meeting_room, 4, "预定会议室"));
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_setting, 5, getString(R.string.settings)));
                break;
            case 2:
                menuItemList.add(new MenuItem(R.drawable.ic_schedule_add_task, 2,  "新建任务"));
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
