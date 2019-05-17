package com.inspur.emmcloud.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.BaseFragment;

import org.xutils.x;

/**
 * Created by chenmch on 2019/4/6.
 */

public abstract class ScheduleBaseFragment extends BaseFragment {
    private boolean injected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injected = true;
        x.Ext.setDebug(true);
        return x.view().inject(this, inflater, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!injected) {
            x.view().inject(this, this.getView());
        }
    }
}
