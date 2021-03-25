package com.inspur.emmcloud.basemodule.ui;

import android.support.v4.app.Fragment;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.R;

import butterknife.Unbinder;

/**
 * Created by chenmch on 2019/2/18.
 */

public class BaseFragment extends Fragment {
    public Unbinder unbinder;

    protected void setFragmentStatusBarCommon() {
        int color = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.header_bg_color);
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(getActivity(), R.attr.status_bar_dark_font);
        ImmersionBar.with(getActivity()).statusBarColor(color).statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();

    }

    protected void setFragmentStatusBarWhite() {
        int color = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.text_ll_color_nor);
        ImmersionBar.with(getActivity()).statusBarColor(color).navigationBarColor(color).statusBarDarkFont(true, 0.2f).navigationBarDarkIcon(true, 1.0f).init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onResume() {
        LogUtils.debug("TilllLog", this + " onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.debug("TilllLog", this + " onPause");
        super.onPause();
    }
}
