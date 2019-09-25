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
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.basemodule.ui.BaseMvpFragment;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.ui.appcenter.volume.contract.VolumeFileTransferContract;
import com.inspur.emmcloud.ui.appcenter.volume.presenter.VolumeFileTransferPresenter;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VolumeFileTransferFragment extends BaseMvpFragment<VolumeFileTransferPresenter> implements VolumeFileTransferContract.View {
    int currentIndex = 0;
    @BindView(R.id.volume_file_transfer_recycler)
    RecyclerView recyclerView;
    VolumeFileAdapter adapter;

    List<VolumeFile> volumeFileList = new ArrayList<>();//云盘列表
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

        switch (currentIndex) {
            case 0:

                break;
            case 1:
                volumeFileList = VolumeFileUploadManager.getInstance().getAllUploadVolumeFile();
                break;
            case 2:

                break;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new VolumeFileAdapter(getActivity(), volumeFileList);
        recyclerView.setAdapter(adapter);
    }
}
