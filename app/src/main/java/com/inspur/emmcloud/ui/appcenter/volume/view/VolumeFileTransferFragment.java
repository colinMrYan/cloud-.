package com.inspur.emmcloud.ui.appcenter.volume.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;

public class VolumeFileTransferFragment extends BaseFragment {
    int currentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volume_file_transfer, null);
        init();
        return rootView;
    }

    private void init() {
        if (getArguments() != null) {
            currentIndex = getArguments().getInt("position");
        }

    }
}
