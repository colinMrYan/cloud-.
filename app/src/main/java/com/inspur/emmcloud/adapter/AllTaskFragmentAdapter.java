package com.inspur.emmcloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.inspur.emmcloud.ui.work.task.TaskListFragment;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/4.
 */

public class AllTaskFragmentAdapter extends FragmentPagerAdapter {
    //存放fragment的集合
    private List<TaskListFragment> mFragments;

    public AllTaskFragmentAdapter(FragmentManager fm, List<TaskListFragment> mFragments) {
        super(fm);
        this.mFragments = mFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public List<TaskListFragment> getTaskListFragment(){
        return mFragments;
    }
}
