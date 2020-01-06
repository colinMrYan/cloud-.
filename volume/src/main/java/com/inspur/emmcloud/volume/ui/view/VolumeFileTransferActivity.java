package com.inspur.emmcloud.volume.ui.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.TabLayoutUtil;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.adapter.AllVolumeFragmentAdapter;
import com.inspur.emmcloud.volume.ui.presenter.VolumeFileTransferPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 文件传输列表
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferActivity extends BaseMvpActivity implements VolumeFileTransferFragment.SelectCallBack {

    @BindView(R2.id.ibt_back)
    ImageButton backBtn;
    @BindView(R2.id.header_left_text)
    TextView headerLeftTv;
    @BindView(R2.id.header_right_text)
    TextView headerRightTv;
    @BindView(R2.id.tl_file_transfer)
    TabLayout tabLayout;
    @BindView(R2.id.vp_file_transfer)
    ViewPager viewPager;
    FragmentPagerAdapter adapter;
    List<VolumeFileTransferFragment> list = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_volume_file_transfer;
    }

    private void init() {
        setTitleText(R.string.volume_file_transfer);
        headerLeftTv.setText(R.string.button_cancel);
        headerRightTv.setText(R.string.select_all);
        mPresenter = new VolumeFileTransferPresenter();
        mPresenter.attachView(this);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_download_list), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.volume_file_transfer_upload_list), true);
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.has_downloaded), true);
        initFragmentList();
        TabLayoutUtil.setTabLayoutWidth(this, tabLayout);
        tabLayout.getTabAt(0).select();
        onSelect(new ArrayList<VolumeFile>());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                currentIndex = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                list.get(tab.getPosition()).clickHeaderLeft();
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
                currentIndex = position;
                if (tabLayout.getTabAt(position) != null) {
                    tabLayout.getTabAt(position).select();
                }
                if (position != 2) {
                    onSelect(new ArrayList<VolumeFile>());
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initFragmentList() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            VolumeFileTransferFragment fragment = new VolumeFileTransferFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            fragment.setArguments(bundle);
            list.add(fragment);
        }
        adapter = new AllVolumeFragmentAdapter(getSupportFragmentManager(), list);
    }

    @OnClick({R2.id.header_left_text, R2.id.header_right_text})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.header_left_text) {
            list.get(currentIndex).clickHeaderLeft();
        } else if (id == R.id.header_right_text) {
            list.get(currentIndex).clickHeaderRight();
        }
    }

    @Override
    public void onSelect(List<VolumeFile> selectVolumeFileList) {
        if (selectVolumeFileList == null) {     //处理“全不选”情况
            setTitleText(R.string.volume_file_transfer);
            headerLeftTv.setVisibility(View.VISIBLE);
            headerRightTv.setVisibility(View.VISIBLE);
            backBtn.setVisibility(View.GONE);
        } else if (selectVolumeFileList.size() == 0) {  //正常无数据
            setTitleText(R.string.volume_file_transfer);
            headerLeftTv.setVisibility(View.GONE);
            headerRightTv.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
        } else {
            setTitleText(getString(R.string.volume_clouddriver_has_selected, selectVolumeFileList.size()));
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
