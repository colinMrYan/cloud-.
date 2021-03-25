package com.inspur.emmcloud.basemodule.ui;

import android.support.v4.app.Fragment;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

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
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        if (currentThemeNo != 3) {
            ImmersionBar.with(getActivity()).statusBarColor(R.color.white).navigationBarColor(R.color.white).statusBarDarkFont(true, 0.2f).navigationBarDarkIcon(true, 1.0f).init();
        } else {
            ImmersionBar.with(getActivity()).statusBarColor(R.color.black).navigationBarColor(R.color.black).statusBarDarkFont(false, 0.2f).navigationBarDarkIcon(false, 1.0f).init();
        }
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
