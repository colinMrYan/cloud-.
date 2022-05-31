package com.inspur.emmcloud.basemodule.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;

import java.util.Objects;

import butterknife.Unbinder;

/**
 * Created by chenmch on 2019/2/18.
 */

public class BaseFragment extends Fragment {
    public Unbinder unbinder;

    protected void setFragmentStatusBarCommon() {
        int color = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.header_bg_color);
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(getActivity(), R.attr.status_bar_dark_font);
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        ImmersionBar.with(getActivity()).statusBarColor(color).statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(currentThemeNo != 3 ? R.color.white : R.color.black).navigationBarDarkIcon(true, 1.0f).init();

    }

    protected void setFragmentStatusBarWhite() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        if (currentThemeNo != BaseActivity.THEME_DARK) {
            ImmersionBar.with(getActivity()).statusBarColor(R.color.white).navigationBarColor(R.color.white).statusBarDarkFont(true, 0.2f).navigationBarDarkIcon(true, 1.0f).init();
        } else {
            ImmersionBar.with(getActivity()).statusBarColor(R.color.content_bg_dark).navigationBarColor(R.color.black).statusBarDarkFont(false, 0.2f).navigationBarDarkIcon(false, 1.0f).init();
        }
    }

    protected void setMineFragmentStatusBar() {
        int color = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.mine_header_bg_color);
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(getActivity(), R.attr.status_bar_dark_font);
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        ImmersionBar.with(getActivity()).statusBarColor(color).statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(currentThemeNo != 3 ? R.color.white : R.color.black).navigationBarDarkIcon(true, 1.0f).init();

    }

    private void initFontScale() {
        Float fontScale = PreferencesUtils.getFloat(getActivity(), Constant.CARING_SWITCH_FLAG, 1);
        if (0 == Float.compare(1.0f, fontScale)) {
            return;
        }
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale = fontScale;
        DisplayMetrics metrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onResume() {
        LogUtils.debug("TilllLog", this + " onResume");
//        initFontScale();
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.debug("TilllLog", this + " onPause");
        super.onPause();
    }
}
