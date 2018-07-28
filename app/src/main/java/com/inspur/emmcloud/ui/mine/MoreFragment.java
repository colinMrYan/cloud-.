package com.inspur.emmcloud.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.mine.card.CardPackageActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppTitleUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;

import static android.app.Activity.RESULT_OK;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int REQUEST_CODE_UPDATE_USER_PHOTO = 3;
    private View rootView;
    private ImageView moreHeadImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        initViews();
        setMyInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
        //如果找不到云+客服频道就隐藏
        if (customerChannel == null) {
            (rootView.findViewById(R.id.customer_layout)).setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mine, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 初始化views
     */
    private void initViews() {
        setTabTitle();
        String isShowFeedback = AppConfigCacheUtils.getAppConfigValue(getContext(), Constant.CONCIG_SHOW_FEEDBACK, "true");
        String isShowCustomerService = AppConfigCacheUtils.getAppConfigValue(getContext(), Constant.CONCIG_SHOW_CUSTOMER_SERVICE, "true");
        RelativeLayout feedbackLayout = (RelativeLayout) rootView.findViewById(R.id.more_feedback_layout);
        if (isShowFeedback.equals("true")) {
            feedbackLayout.setVisibility(View.VISIBLE);
            feedbackLayout.setOnClickListener(onClickListener);
        }
        RelativeLayout customerLayout = (RelativeLayout) rootView.findViewById(R.id.customer_layout);
        if (isShowCustomerService.equals("true")) {
            customerLayout.setVisibility(View.VISIBLE);
            customerLayout.setOnClickListener(onClickListener);
        }
        if (isShowCustomerService.equals("false") && isShowFeedback.equals("false")){
            (rootView.findViewById(R.id.blank_layout)).setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.more_set_layout).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.more_userhead_layout).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.about_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.scan_login_desktop_layout)).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.rl_card_package).setOnClickListener(onClickListener);
        moreHeadImg = (ImageView) rootView.findViewById(R.id.more_head_img);
    }


    private void setMyInfo() {
        // TODO Auto-generated method stub
        String uid = ((MyApplication)getActivity().getApplicationContext()).getUid();
        String photoUri = APIUri.getUserIconUrl(getActivity(), uid);
        ImageDisplayUtils.getInstance().displayImage(moreHeadImg, photoUri, R.drawable.icon_photo_default);
        String userName = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
        ((TextView) rootView.findViewById(R.id.more_head_name_text)).setText(userName);
        ((TextView) rootView.findViewById(R.id.more_head_enterprise_text)).setText(((MyApplication) getActivity().getApplicationContext()).getCurrentEnterprise().getName());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_USER_PHOTO) {
            String uid = ((MyApplication)getActivity().getApplicationContext()).getUid();
            String photoUri = APIUri.getUserIconUrl(getActivity(),uid);
            ImageDisplayUtils.getInstance().displayImage(moreHeadImg, photoUri, R.drawable.icon_photo_default);
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.more_set_layout:
                    IntentUtils.startActivity(getActivity(), SettingActivity.class);
                    recordUserClick("setting");
                    break;
                case R.id.more_userhead_layout:
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_USER_PHOTO);
                    recordUserClick("profile");
                    break;
                case R.id.more_feedback_layout:
                    IntentUtils.startActivity(getActivity(), FeedBackActivity.class);
                    recordUserClick("feedback");
                    break;
                case R.id.about_layout:
                    IntentUtils.startActivity(getActivity(),
                            AboutActivity.class);
                    recordUserClick("about");
                    break;
                case R.id.customer_layout:
                    Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
                    Bundle bundle = new Bundle();
                    bundle.putString("cid", customerChannel.getCid());
                    //为区分来自云+客服添加一个from值，在ChannelActivity里使用
                    bundle.putString("from", "customer");
                    IntentUtils.startActivity(getActivity(), MyApplication.getInstance().isV0VersionChat()?
                            ChannelV0Activity.class:ChannelActivity.class, bundle);
                    recordUserClick("customservice");
                    break;
                case R.id.rl_card_package:
                    IntentUtils.startActivity(getActivity(), CardPackageActivity.class);
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 记录用户点击的functionId
     *
     * @param functionId
     */
    private void recordUserClick(String functionId) {
        PVCollectModel pvCollectModel = new PVCollectModel(functionId, "mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
    }


    /**
     * 设置标题
     */
    private void setTabTitle() {
        String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        if (!StringUtils.isBlank(appTabs)) {
            ((TextView) rootView.findViewById(R.id.header_text)).setText(AppTitleUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
        }
    }


}
