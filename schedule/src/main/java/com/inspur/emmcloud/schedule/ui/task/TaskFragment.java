package com.inspur.emmcloud.schedule.ui.task;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.adapter.AllTaskFragmentAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2019/4/1.
 * 工作主页面下任务页面
 */

public class TaskFragment extends BaseFragment {

    public static final String MY_TASK_TYPE = "task_type";
    public static final int MY_MINE = 0;
    public static final int MY_INVOLVED = 1;
    public static final int MY_FOCUSED = 2;
    public static final int MY_DONE = 3;
    public static final int MY_ALL = 4;
    private static final int MESSION_SET = 5;
    private TabLayout tabLayoutSchedule;
    private ViewPager taskViewPager;

    private boolean injected = false;
    private TaskListFragment allTaskListFragment, mineTaskListFragment, involvedTaskListFragment, focusedTaskListFragment, allReadyDoneTaskListFragment;
    private AllTaskFragmentAdapter adapter;
    private int lastTaskPosition = 0;
    private View rootView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.schedule_all_task_list_fragment, null);
        initViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.schedule_all_task_list_fragment, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }



    /**
     * 初始化任务列表，并传入type类型
     */
    private void initFragmentList() {
        //建一个存放fragment的集合，并且把新的fragment放到集合中
        List<Fragment> list = new ArrayList<>();
//        Bundle bundle = new Bundle();
//        bundle.putInt(MY_TASK_TYPE,MY_MINE);
//        allTaskListFragment = new TaskListFragment();
//        allTaskListFragment.setArguments(bundle);
        Bundle bundleMine = new Bundle();
        bundleMine.putInt(MY_TASK_TYPE, MY_MINE);
        mineTaskListFragment = new TaskListFragment();
        mineTaskListFragment.setArguments(bundleMine);
        Bundle bundleInvolved = new Bundle();
        bundleInvolved.putInt(MY_TASK_TYPE, MY_INVOLVED);
        involvedTaskListFragment = new TaskListFragment();
        involvedTaskListFragment.setArguments(bundleInvolved);
        Bundle bundleFocused = new Bundle();
        bundleFocused.putInt(MY_TASK_TYPE, MY_FOCUSED);
        focusedTaskListFragment = new TaskListFragment();
        focusedTaskListFragment.setArguments(bundleFocused);
        Bundle bundleDone = new Bundle();
        bundleDone.putInt(MY_TASK_TYPE, MY_DONE);
        allReadyDoneTaskListFragment = new TaskListFragment();
        allReadyDoneTaskListFragment.setArguments(bundleDone);

//        list.add(allTaskListFragment);
        list.add(mineTaskListFragment);
        list.add(involvedTaskListFragment);
        list.add(focusedTaskListFragment);
        list.add(allReadyDoneTaskListFragment);
        //初始化adapter
        adapter = new AllTaskFragmentAdapter(this.getChildFragmentManager(), list);
    }


    private void initViews() {
        //带“全部”代码
//        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_mine),getIsSelect(0));
//        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_involved),getIsSelect(1));
//        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_focused),getIsSelect(2));
//        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_done),getIsSelect(3));
        initFragmentList();
        tabLayoutSchedule = rootView.findViewById(R.id.tl_schedule_task);
        taskViewPager = rootView.findViewById(R.id.viewpager_calendar_holder);
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.schedule_task_mine), true);
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.schedule_task_involved), false);
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.schedule_task_focused), false);
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.schedule_task_done), false);

        tabLayoutSchedule.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                //带“全部”代码
                taskViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //带“全部”代码
//                int position = tab.getPosition();
//                if(position == 0){
//                    taskViewPager.setCurrentItem(position + 1);
//                }
//                taskViewPager.setCurrentItem(tab.getPosition());
            }
        });

        taskViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if(lastTaskPosition != position){
//                    setSearchState(lastTaskPosition);
//                    lastTaskPosition = position;
//                }
//                if(position > 0){
//                    tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#36A5F6"));
//                    tabLayoutSchedule.getTabAt(position - 1).select();
//                    allTaskView.setBackgroundColor(Color.parseColor("#00ffffff"));
//                }else{
//                    tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#00ffffff"));
//                    allTaskView.setBackgroundColor(Color.parseColor("#36A5F6"));
//                }


                tabLayoutSchedule.getTabAt(position).select();
//                ((AllTaskFragmentAdapter) taskViewPager.getAdapter()).getTaskListFragment().get(taskViewPager.getCurrentItem()).setCurrentIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //有“全部”的代码
//        allTaskLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#00ffffff"));
//                allTaskView.setBackgroundColor(Color.parseColor("#36A5F6"));
//                taskViewPager.setCurrentItem(0);
//            }
//        });
        //将适配器和ViewPager结合
        taskViewPager.setAdapter(adapter);
//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                ((AllTaskFragmentAdapter) taskViewPager.getAdapter()).getTaskListFragment().get(taskViewPager.getCurrentItem()).setSearchContent(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
    }

//    /**
//     * 根据滑动状态改变搜索框和展示数据状态
//     */
//    private void setSearchState(int lastTaskPosition) {
//        searchEditText.setText("");
//        ((AllTaskFragmentAdapter) taskViewPager.getAdapter()).getTaskListFragment().get(lastTaskPosition).setSearchContent("");
//    }


    public boolean getIsSelect(int i) {
        if (tabLayoutSchedule.getChildAt(i) != null) {
            return tabLayoutSchedule.getChildAt(i).isSelected();
        }
        return false;
    }
}
