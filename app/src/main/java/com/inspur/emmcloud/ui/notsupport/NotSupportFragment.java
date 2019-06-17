package com.inspur.emmcloud.ui.notsupport;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;

/**
 * 如果有不支持的功能时显示这个界面
 */
public class NotSupportFragment extends BaseFragment {


    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private View rootView;
    private LayoutInflater inflater;
    private TextView unknownFuctionText;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_NEED_UPGRADE:
                    ToastUtils.show(getActivity(), R.string.app_is_lastest_version);
                    break;
                case UPGRADE_FAIL:
                    ToastUtils.show(getActivity(), R.string.check_update_fail);
                    break;
                case DONOT_UPGRADE:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_unknown, null);
        unknownFuctionText = (TextView) rootView.findViewById(R.id.app_unknow_text);
        String title = getTabTitle();
        ((TextView) rootView.findViewById(R.id.header_text)).setText(title);
        //应用功能已改版，请升级到最新版本
        unknownFuctionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeUtils upgradeUtils = new UpgradeUtils(getActivity(), handler, true);
                upgradeUtils.checkUpdate(true);
            }
        });
        unknownFuctionText.setText(Html.fromHtml(getResources().getString(R.string.tab_not_support_tips, title)));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarCommon();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_unknown, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 设置标题，根据当前Fragment类名获取显示名称
     */
    private String getTabTitle() {
        String title = "";
        if (!StringUtils.isBlank(getArguments().getString("uri"))) {
            String uri = getArguments().getString("uri");
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            if (!StringUtils.isBlank(appTabs)) {
                title = AppTabUtils.getTabTitle(getActivity(), NotSupportFragment.class.getSimpleName(), uri);
            }
        }
        return title;

    }

}

