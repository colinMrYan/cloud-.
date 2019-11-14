package com.inspur.emmcloud.ui.appcenter.volume.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllTaskFragmentAdapter;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.TabLayoutUtil;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
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
public class VolumeFileTransferActivity extends BaseMvpActivity implements VolumeFileTransferFragment.SelectCallBack {

    @BindView(R.id.ibt_back)
    ImageButton backBtn;
    @BindView(R.id.header_left_text)
    TextView headerLeftTv;
    @BindView(R.id.header_right_text)
    TextView headerRightTv;
    @BindView(R.id.tl_file_transfer)
    TabLayout tabLayout;
    @BindView(R.id.vp_file_transfer)
    ViewPager viewPager;
    FragmentPagerAdapter adapter;
    VolumeFileTransferFragment downloadedFragment;

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
        headerLeftTv.setText(R.string.button_cancel);
        headerRightTv.setText(R.string.select_all);
        mPresenter = new VolumeFileTransferPresenter();
        mPresenter.attachView(this);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_download_list), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_upload_list), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.has_downloaded), true);
        initFragmentList();
        TabLayoutUtil.setTabLayoutWidth(this, tabLayout);
        tabLayout.getTabAt(0).select();
        onSelect(new ArrayList<VolumeFile>());

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
                if (position != 2) {
                    onSelect(new ArrayList<VolumeFile>());
                    if (downloadedFragment != null) {
                        downloadedFragment.hideBottomOperationItemShow();
                    }
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
            if (i == 2) {
                downloadedFragment = fragment;
            }
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

    @Override
    public void onSelect(List<VolumeFile> selectVolumeFileList) {
        if (selectVolumeFileList == null || selectVolumeFileList.size() == 0) {
            setTitleText(R.string.volume_file_transfer);
            headerLeftTv.setVisibility(View.GONE);
            headerRightTv.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
        } else {
            setTitleText(getString(R.string.clouddriver_has_selected, selectVolumeFileList.size()));
            headerLeftTv.setVisibility(View.VISIBLE);
            headerRightTv.setVisibility(View.VISIBLE);
            backBtn.setVisibility(View.GONE);
        }
    }

    public TextView getHeaderLeftTv() {
        return headerLeftTv;
    }

    public TextView getHeaderRightTv() {
        return headerRightTv;
    }
}
