package com.inspur.emmcloud.widget.filemanager;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.widget.filemanager.adapter.FileFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/23.
 */

public class NativeVolumeFileManagerActivity extends BaseFragmentActivity implements View.OnClickListener {

    NativeFileManagerFragment nativeFileManagerFragment;
    VolumeFileManagerFragment volumeFileManagerFragment;
    FileFragmentPagerAdapter fileFragmentPagerAdapter;
    TabLayout fileTablayout;
    TextView okTextView;
    ImageButton finishButton;
    private ViewPager fileViewPager;
    private LoadingDialog loadingDialog;
    private Volume myVolume;
    private MyAppAPIService apiService;

    @Override
    public void onCreate() {
        setContentView(R.layout.activity_native_volume_file_manager);
        setStatus();
        fileTablayout = findViewById(R.id.tl_files_source);
        fileViewPager = findViewById(R.id.viewpager_file_fragment);
        okTextView = findViewById(R.id.tv_ok);
        finishButton = findViewById(R.id.ibt_back);
        nativeFileManagerFragment = new NativeFileManagerFragment();
        volumeFileManagerFragment = new VolumeFileManagerFragment();
        initView();
    }

    private void initView() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(nativeFileManagerFragment);
        fragmentList.add(volumeFileManagerFragment);
        fileFragmentPagerAdapter = new FileFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList);
        fileTablayout.addTab(fileTablayout.newTab().setText(getString(R.string.internal_shared_storage)), true);
        fileTablayout.addTab(fileTablayout.newTab().setText(getString(R.string.chat_filemanager_volume)), false);
        fileTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fileViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fileViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                fileTablayout.getTabAt(i).select();
                if (i == 1) {
                    volumeFileManagerFragment.getMyVolume();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        fileViewPager.setAdapter(fileFragmentPagerAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_ok:
                finish();
                break;
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        switch (fileViewPager.getCurrentItem()) {
            case 0:
                nativeFileManagerFragment.onBackPress();
                break;
            case 1:
                volumeFileManagerFragment.onBackPress();
                break;
        }
    }
}
