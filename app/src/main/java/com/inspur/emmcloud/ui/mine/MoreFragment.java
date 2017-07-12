package com.inspur.emmcloud.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.AppTitleUtils;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;

import java.io.Serializable;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int UPDATE_MY_HEAD = 3;
    public static Handler handler;
    private View rootView;
    private LayoutInflater inflater;
    private ImageView moreHeadImg;
    private ImageDisplayUtils imageDisplayUtils;
    private GetMyInfoResult getMyInfoResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        handMessage();
        initViews();
        getMyInfo();
        setTabTitle();
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
        rootView.findViewById(R.id.more_set_layout).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.more_userhead_layout).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_help_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_message_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_invite_friends_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.about_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.customer_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.scan_login_desktop_layout)).setOnClickListener(onClickListener);
        moreHeadImg = (ImageView) rootView.findViewById(R.id.more_head_img);
        imageDisplayUtils = new ImageDisplayUtils(getActivity(), R.drawable.icon_photo_default);
    }


    private void getMyInfo() {
        // TODO Auto-generated method stub
        String myInfo = PreferencesUtils.getString(getActivity(), "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        String inspurId = getMyInfoResult.getID();
        String photoUri = UriUtils.getChannelImgUri(getActivity(),inspurId);
        imageDisplayUtils.display(moreHeadImg, photoUri);
        String userName = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
        ((TextView) rootView.findViewById(R.id.more_head_textup)).setText(userName);
        ((TextView) rootView.findViewById(R.id.more_head_textdown)).setText(((MyApplication)getActivity().getApplicationContext()).getCurrentEnterprise().getName());
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPDATE_MY_HEAD:
                        getMyInfoResult.setAvatar((String) msg.obj);
                        String userheadUrl = "https://mob.inspur.com" + getMyInfoResult.getAvatar();
                        imageDisplayUtils.display(moreHeadImg, userheadUrl);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.more_set_layout:
                    intent.setClass(getActivity(), SettingActivity.class);
                    startActivity(intent);
                    recordUserClick("setting");
                    break;
                case R.id.more_userhead_layout:
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    intent.putExtra("getMyInfoResult", (Serializable) getMyInfoResult);
                    startActivity(intent);
                    recordUserClick("profile");
                    break;
                case R.id.more_help_layout:
				    intent.setClass(getActivity(), FeedBackActivity.class);
				    startActivity(intent);
                    recordUserClick("feedback");
                    break;
                case R.id.more_message_layout:
                    ToastUtils.show(getActivity(), R.string.function_not_implemented);
                    break;
                case R.id.about_layout:
                    IntentUtils.startActivity(getActivity(),
                            AboutActivity.class);
                    recordUserClick("about");
                    break;
                case R.id.customer_layout:
                    Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
                    if (customerChannel != null ){
                        Bundle bundle = new Bundle();
                        bundle.putString("title", customerChannel.getTitle());
                        bundle.putString("channelId", customerChannel.getCid());
                        bundle.putString("channelType", customerChannel.getType());
                        //为区分来自云+客服添加一个from值，在ChannelActivity里使用
                        bundle.putString("from","customer");
                        IntentUtils.startActivity(getActivity(),
                                ChannelActivity.class, bundle);
                        recordUserClick("customservice");
                    }
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 记录用户点击的functionId
     * @param functionId
     */
    private void recordUserClick(String functionId){
        PVCollectModel pvCollectModel = new PVCollectModel(functionId,"mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
        //如果找不到云+客服频道就隐藏
        if (customerChannel == null){
            (rootView.findViewById(R.id.customer_layout)).setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题
     */
    private void setTabTitle(){
        String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(),"app_tabbar_info_current","");
        if(!StringUtils.isBlank(appTabs)){
            ((TextView) rootView.findViewById(R.id.header_text)).setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
    }

}
