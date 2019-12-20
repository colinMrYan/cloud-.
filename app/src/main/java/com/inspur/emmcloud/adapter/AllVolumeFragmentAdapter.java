package com.inspur.emmcloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.inspur.emmcloud.ui.appcenter.volume.view.VolumeFileTransferFragment;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/4.
 */

public class AllVolumeFragmentAdapter extends FragmentPagerAdapter {
    //存放fragment的集合
    private List<VolumeFileTransferFragment> taskListFragmentList;

    public AllVolumeFragmentAdapter(FragmentManager fm, List<VolumeFileTransferFragment> taskListFragmentList) {
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

    public List<VolumeFileTransferFragment> getTaskListFragment() {
        return taskListFragmentList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
