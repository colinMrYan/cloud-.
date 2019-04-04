package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllScheduleFragmentAdapter;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.ui.work.task.AllTaskListFragment;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/3/28.
 */
public class AllScheduleFragment extends Fragment{

    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private View rootView;
    private TabLayout tabLayoutSchedule;
    private PopupWindow popupWindow;
    private NoScrollViewPager allScheduleFragmentViewPager;
    private ScheduleFragment scheduleFragment;
    private ScheduleFragment meetingFragment;
    private AllTaskListFragment allTaskFragment;

    private View.OnClickListener onViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            popupWindow.dismiss();
            switch (v.getId()) {
                case R.id.rl_schedule_calendar:
                    recordUserClickWorkFunction(PV_COLLECTION_CAL);
                    IntentUtils.startActivity(getActivity(), CalActivity.class);
                    break;
                case R.id.rl_schedule_meeting:
                    recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                    IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    break;
                case R.id.rl_schedule_mission:
                    recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                    IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_all_schedule, null);
        initView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        tabLayoutSchedule = rootView.findViewById(R.id.tl_schedule);
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_schedule));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_meeting_text));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_mession));
        //
        tabLayoutSchedule.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(allScheduleFragmentViewPager != null){
                    allScheduleFragmentViewPager.setCurrentItem(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        rootView.findViewById(R.id.ibt_schedule_function_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ibt_schedule_function_list:
                        showPopupWindow(v);
                        break;
                }
            }
        });

        rootView.findViewById(R.id.ibt_back_to_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleFragment.setScheduleBackToToday();
            }
        });

        allScheduleFragmentViewPager = rootView.findViewById(R.id.all_schedule_viewpager);
        allScheduleFragmentViewPager.setNoScroll(true);
        allScheduleFragmentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(tabLayoutSchedule != null){
                    tabLayoutSchedule.getTabAt(position).select();
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
        AllScheduleFragmentAdapter adapter = new AllScheduleFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        //将适配器和ViewPager结合
        allScheduleFragmentViewPager.setAdapter(adapter);

    }

    /**
     * 通讯录和创建群组，扫一扫合并
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.pop_schedule_window_view, null);

        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        contentView.findViewById(R.id.rl_schedule_calendar).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_mission).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_meeting).setOnClickListener(onViewClickListener);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }
}
