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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.AppTitleUtils;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
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
    private RelativeLayout setContentItem;
    private RelativeLayout userHeadLayout;
    private ImageView moreHeadImg;
    private TextView userNameText;
    private TextView userOrgText;
    private ImageView userCodeImg;
    private ImageDisplayUtils imageDisplayUtils;
    private GetMyInfoResult getMyInfoResult;
    private String userheadUrl;
    private TextView titleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_mine, null);

        handMessage();


        setContentItem = (RelativeLayout) rootView.findViewById(R.id.more_set_layout);
        userHeadLayout = (RelativeLayout) rootView.findViewById(R.id.more_userhead_layout);
        setContentItem.setOnClickListener(onClickListener);
        userHeadLayout.setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_help_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_message_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_invite_friends_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.about_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.customer_layout)).setOnClickListener(onClickListener);
        Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
        //如果找不到云+客服频道就隐藏
        if (customerChannel == null){
            (rootView.findViewById(R.id.customer_layout)).setVisibility(View.GONE);
        }
        moreHeadImg = (ImageView) rootView.findViewById(R.id.more_head_img);
        userNameText = (TextView) rootView.findViewById(R.id.more_head_textup);
        userOrgText = (TextView) rootView.findViewById(R.id.more_head_textdown);
        userCodeImg = (ImageView) rootView.findViewById(R.id.more_head_codeImg);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        imageDisplayUtils = new ImageDisplayUtils(getActivity(), R.drawable.icon_photo_default);
        getMyInfo();
        setTabTitle();
    }


    private void getMyInfo() {
        // TODO Auto-generated method stub
        String myInfo = PreferencesUtils.getString(getActivity(), "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        String inspurId = getMyInfoResult.getID();
        String photoUri = UriUtils.getChannelImgUri(inspurId);
        imageDisplayUtils.display(moreHeadImg, photoUri);

        if (!getMyInfoResult.getName().equals("null")) {
            userNameText.setText(getMyInfoResult.getName());
        } else {
            userNameText.setText(getString(R.string.not_set));
        }

        userOrgText.setText(getMyInfoResult.getEnterpriseName());
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
                        userheadUrl = "https://mob.inspur.com" + getMyInfoResult.getAvatar();
                        imageDisplayUtils.display(moreHeadImg, userheadUrl);
                        break;
                    default:
                        break;
                }
            }

        };
    }

//    private void showDialog() {
//        View view = getActivity().getLayoutInflater().inflate(R.layout.mine_team_choose_dialog, null);
//        Dialog dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
//        dialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.WRAP_CONTENT));
//        Window window = dialog.getWindow();
//        // 设置显示动画
//        window.setWindowAnimations(R.style.main_menu_animstyle);
//        WindowManager.LayoutParams wl = window.getAttributes();
//        wl.x = 0;
//        wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
//        // 以下这两句是为了保证按钮可以水平满屏
//        wl.width = LayoutParams.MATCH_PARENT;
//        wl.height = LayoutParams.WRAP_CONTENT;
//
//        // 设置显示位置
//        dialog.onWindowAttributesChanged(wl);
//        // 设置点击外围解散
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.show();
//    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.more_set_layout:
                    intent.setClass(getActivity(), SettingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.more_userhead_layout:
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    intent.putExtra("getMyInfoResult", (Serializable) getMyInfoResult);
                    startActivity(intent);
                    break;
                case R.id.more_help_layout:
				    intent.setClass(getActivity(), FeedBackActivity.class);
				    startActivity(intent);
                    break;
                case R.id.more_message_layout:
                case R.id.more_invite_friends_layout:
                    ToastUtils.show(getActivity(), R.string.function_not_implemented);
                    break;
                case R.id.about_layout:
                    IntentUtils.startActivity(getActivity(),
                            AboutActivity.class);
                    break;
                case R.id.customer_layout:
                    Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
                    if (customerChannel != null ){
                        Bundle bundle = new Bundle();
                        bundle.putString("title", customerChannel.getTitle());
                        bundle.putString("channelId", customerChannel.getCid());
                        bundle.putString("channelType", customerChannel.getType());
                        IntentUtils.startActivity(getActivity(),
                                ChannelActivity.class, bundle);
                    }
                    break;
                default:
                    break;
            }

        }
    };

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
     * 设置标题
     */
    private void setTabTitle(){
        String appTabs = PreferencesByUserUtils.getString(getActivity(),"app_tabbar_info_current","");
        if(!StringUtils.isBlank(appTabs)){
            titleText.setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    public class WebService extends APIInterfaceInstance {


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
