package com.inspur.emmcloud.ui.work.task;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllTaskFragmentAdapter;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2019/4/1.
 * 工作主页面下任务页面
 */
@ContentView(R.layout.fragment_all_task_list)
public class AllTaskListFragment extends Fragment{

    public static final String MY_TASK_TYPE = "task_type";
    public static final int MY_MINE = 0;
    public static final int MY_INVOLVED = 1;
    public static final int MY_FOCUSED = 2;
    public static final int MY_DONE = 3;
    public static final int MY_ALL = 4;
    private static final int MESSION_SET = 5;
    private boolean injected = false;
    @ViewInject(R.id.tl_schedule_task)
    private TabLayout tabLayoutSchedule;
    @ViewInject(R.id.viewpager_calendar_holder)
    private ViewPager taskViewPager;
    @ViewInject(R.id.v_all_task)
    private View allTaskView;
    @ViewInject(R.id.tv_schedule_all)
    private TextView allTaskText;
    @ViewInject(R.id.rl_search_layout)
    private RelativeLayout searchRelativeLayout;
    @ViewInject(R.id.rl_all_task)
    private RelativeLayout allTaskLayout;
    private TaskListFragment allTaskListFragment,mineTaskListFragment,involvedTaskListFragment,focusedTaskListFragment,allReadyDoneTaskListFragment;
    private AllTaskFragmentAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        injected = true;
        return x.view().inject(this, inflater, container);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!injected) {
            x.view().inject(this, this.getView());
        }
        initViews();
    }

    private void initData() {
        List<Fragment> list = new ArrayList<Fragment>();
        //建一个存放fragment的集合，并且把新的fragment放到集合中
        Bundle bundle = new Bundle();
        bundle.putInt(MY_TASK_TYPE,MY_MINE);
        allTaskListFragment = new TaskListFragment();
        allTaskListFragment.setArguments(bundle);
        Bundle bundleMine = new Bundle();
        bundleMine.putInt(MY_TASK_TYPE,MY_MINE);
        mineTaskListFragment = new TaskListFragment();
        mineTaskListFragment.setArguments(bundle);
        Bundle bundleInvolved = new Bundle();
        bundleInvolved.putInt(MY_TASK_TYPE,MY_INVOLVED);
        involvedTaskListFragment = new TaskListFragment();
        involvedTaskListFragment.setArguments(bundleInvolved);
        Bundle bundleFocused = new Bundle();
        bundleFocused.putInt(MY_TASK_TYPE,MY_FOCUSED);
        focusedTaskListFragment = new TaskListFragment();
        focusedTaskListFragment.setArguments(bundleFocused);
        Bundle bundleDone = new Bundle();
        bundleDone.putInt(MY_TASK_TYPE,MY_MINE);
        allReadyDoneTaskListFragment = new TaskListFragment();
        allReadyDoneTaskListFragment.setArguments(bundle);

        list.add(allTaskListFragment);
        list.add(mineTaskListFragment);
        list.add(involvedTaskListFragment);
        list.add(focusedTaskListFragment);
        list.add(allReadyDoneTaskListFragment);
        //初始化adapter
        adapter = new AllTaskFragmentAdapter(this.getChildFragmentManager(), list);
    }


    private void initViews() {
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_mine),getIsSelect(0));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_involved),getIsSelect(1));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_focused),getIsSelect(2));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText(R.string.work_task_done),getIsSelect(3));

        tabLayoutSchedule.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                taskViewPager.setCurrentItem(index + 1);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0){
                    taskViewPager.setCurrentItem(position + 1);
                }
            }
        });

        taskViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position > 0){
                    tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#36A5F6"));
                    tabLayoutSchedule.getTabAt(position - 1).select();
                    allTaskView.setBackgroundColor(Color.parseColor("#00ffffff"));
                }else{
                    tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#00ffffff"));
                    allTaskView.setBackgroundColor(Color.parseColor("#36A5F6"));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        allTaskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabLayoutSchedule.setSelectedTabIndicatorColor(Color.parseColor("#00ffffff"));
                allTaskView.setBackgroundColor(Color.parseColor("#36A5F6"));
                taskViewPager.setCurrentItem(0);
            }
        });
        //将适配器和ViewPager结合
        taskViewPager.setAdapter(adapter);
    }


    public boolean getIsSelect(int i) {
        if(tabLayoutSchedule.getChildAt(i) != null ){
            return tabLayoutSchedule.getChildAt(i).isSelected();
        }
        return false;
    }
}
