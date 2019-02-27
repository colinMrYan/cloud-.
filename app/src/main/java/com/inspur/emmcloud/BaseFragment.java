package com.inspur.emmcloud;

import android.support.v4.app.Fragment;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;

/**
 * Created by chenmch on 2019/2/18.
 */

public class BaseFragment extends Fragment {
    protected void setFragmentStatusBarCommon() {
//        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
//        switch (currentThemeNo){
//            case 1:
//                StateBarUtils.translucent(getActivity());
//                StateBarUtils.setStateBarTextColor(getActivity(),false);
//                break;
//            case 2:
//                StateBarUtils.translucent(getActivity());
//                StateBarUtils.setStateBarTextColor(getActivity(),true);
//                break;
//            default:
//                StateBarUtils.translucent(getActivity());
//                StateBarUtils.setStateBarTextColor(getActivity(),true);
//                break;
//        }

        int color = ResourceUtils.getValueOfAttr(getActivity(), R.attr.header_bg_color);
        boolean isStatusFontDark = true;
        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo) {
            case 1:
                isStatusFontDark = false;
                break;
            case 2:
                isStatusFontDark = true;
                break;
            default:
                isStatusFontDark = true;
                break;
        }
        ImmersionBar.with(getActivity()).statusBarColor(color).statusBarDarkFont(isStatusFontDark).init();

    }

    protected void setFragmentStatusBarWhite() {
//        StateBarUtils.translucent(getActivity(),R.color.white);
//        StateBarUtils.setStateBarTextColor(getActivity(),true);
        ImmersionBar.with(getActivity()).statusBarColor(R.color.white).statusBarDarkFont(true).init();
    }
}
