package com.inspur.emmcloud.ui.appcenter.volume.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllTaskFragmentAdapter;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.TabLayoutUtil;
import com.inspur.emmcloud.ui.appcenter.volume.presenter.VolumeFileTransferPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 文件传输列表
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferActivity extends BaseMvpActivity {

    @BindView(R.id.tl_file_transfer)
    TabLayout tabLayout;
    @BindView(R.id.vp_file_transfer)
    ViewPager viewPager;
    FragmentPagerAdapter adapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_file_transfer;
    }

    private void init() {
        setTitleText(R.string.volume_file_transfer);
        mPresenter = new VolumeFileTransferPresenter();
        mPresenter.attachView(this);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_download_list), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_upload_list), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.has_downloaded), true);
        initFragmentList();
        TabLayoutUtil.setTabLayoutWidth(this, tabLayout);
        tabLayout.getTabAt(0).select();

        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (tabLayout.getTabAt(position) != null) {
                    tabLayout.getTabAt(position).select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initFragmentList() {
        List<Fragment> list = new ArrayList<>();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            VolumeFileTransferFragment fragment = new VolumeFileTransferFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            fragment.setArguments(bundle);
            list.add(fragment);
        }
        adapter = new AllTaskFragmentAdapter(getSupportFragmentManager(), list);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

}
