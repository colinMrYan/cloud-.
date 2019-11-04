package com.inspur.emmcloud.ui.appcenter.volume.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileTransferAdapter;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.ui.BaseMvpFragment;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.ui.appcenter.volume.contract.VolumeFileTransferContract;
import com.inspur.emmcloud.ui.appcenter.volume.presenter.VolumeFileTransferPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 文件传输
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferFragment extends BaseMvpFragment<VolumeFileTransferPresenter> implements VolumeFileTransferContract.View {
    int currentIndex = 0;
    @BindView(R.id.volume_file_transfer_empty_layout)
    View noDataLayout;
    @BindView(R.id.refresh_layout)
    MySwipeRefreshLayout refreshLayout;
    @BindView(R.id.volume_file_transfer_recycler)
    RecyclerView recyclerView;
    VolumeFileTransferAdapter adapter;

    List<VolumeFile> volumeFileList = new ArrayList<>();
    VolumeFileTransferPresenter presenter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volume_file_transfer, null);
        unbinder = ButterKnife.bind(this, rootView);
        init();
        return rootView;
    }

    private void init() {
        presenter = new VolumeFileTransferPresenter();
        presenter.attachView(this);

        presenter.setData();

        if (getArguments() != null) {
            currentIndex = getArguments().getInt("position");
        }

        volumeFileList = presenter.getVolumeFileList(currentIndex);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new VolumeFileTransferAdapter(getActivity(), volumeFileList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showNoDataLayout() {
        noDataLayout.setVisibility(View.VISIBLE);
        refreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void showListLayout() {
        noDataLayout.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
    }
}
