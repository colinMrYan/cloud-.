package com.inspur.emmcloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/4.
 */

public class AllTaskFragmentAdapter extends FragmentPagerAdapter {
    //存放fragment的集合
    private List<? extends Fragment> taskListFragmentList;

    public AllTaskFragmentAdapter(FragmentManager fm, List<? extends Fragment> taskListFragmentList) {
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

    public List<? extends Fragment> getTaskListFragment() {
        return taskListFragmentList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
