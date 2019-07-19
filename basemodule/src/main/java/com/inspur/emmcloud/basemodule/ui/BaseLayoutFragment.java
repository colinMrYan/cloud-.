package com.inspur.emmcloud.basemodule.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.emmcloud.basemodule.R;

/**
 * Created by chenmch on 2019/2/18.
 */

public abstract class BaseLayoutFragment extends BaseFragment {
    public View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(getLayoutResId(), null);
        onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_schedule, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    public abstract int getLayoutResId();

    protected abstract void onCreate();
}
