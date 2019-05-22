package com.inspur.emmcloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.inspur.emmcloud.ui.schedule.task.TaskListFragment;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/4.
 */

public class AllTaskFragmentAdapter extends FragmentPagerAdapter {
    //存放fragment的集合
    private List<TaskListFragment> taskListFragmentList;

    public AllTaskFragmentAdapter(FragmentManager fm, List<TaskListFragment> taskListFragmentList) {
        super(fm);
        this.taskListFragmentList = taskListFragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return taskListFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return taskListFragmentList.size();
    }

    public List<TaskListFragment> getTaskListFragment() {
        return taskListFragmentList;
    }

}
