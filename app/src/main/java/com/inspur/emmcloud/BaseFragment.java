package com.inspur.emmcloud;

import android.support.v4.app.Fragment;

import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;

/**
 * Created by chenmch on 2019/2/18.
 */

public class BaseFragment extends Fragment {
    protected void setFragmentStatusBarCommon(){
        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo){
            case 1:
                StateBarUtils.translucent(getActivity());
                StateBarUtils.setStateBarTextColor(getActivity(),false);
                break;
            case 2:
                StateBarUtils.translucent(getActivity());
                StateBarUtils.setStateBarTextColor(getActivity(),true);
                break;
            default:
                StateBarUtils.translucent(getActivity());
                StateBarUtils.setStateBarTextColor(getActivity(),true);
                break;
        }

    }

    protected void setFragmentStatusBarWhite(){
        StateBarUtils.translucent(getActivity(),R.color.white);
        StateBarUtils.setStateBarTextColor(getActivity(),true);
    }
}
