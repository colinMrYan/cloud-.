package com.inspur.emmcloud.widget.filemanager.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by libaochao on 2019/10/24.
 */

public class FileFragmentPagerAdapter extends FragmentPagerAdapter {

    //存放fragment的集合
    private List<Fragment> fileTypeFragmentList;

    public FileFragmentPagerAdapter(FragmentManager fm, List<Fragment> fileTypeFragmentList) {
        super(fm);
        this.fileTypeFragmentList = fileTypeFragmentList;
    }

    public List<Fragment> getFileTypeFragment() {
        return fileTypeFragmentList;
    }

    @Override
    public Fragment getItem(int i) {
        return fileTypeFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fileTypeFragmentList.size();
    }
}
