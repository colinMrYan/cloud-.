package com.inspur.emmcloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by yufuchang on 2019/3/29.
 */

public class ScheduleHomeFragmentAdapter extends FragmentStatePagerAdapter {
    //存放fragment的集合
    private List<Fragment> mFragments;

    public ScheduleHomeFragmentAdapter(FragmentManager fm, List<Fragment> mFragments) {
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
}
