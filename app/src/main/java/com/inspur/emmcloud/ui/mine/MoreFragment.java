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

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.LoginDesktopCloudPlusBean;
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
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;

import java.io.Serializable;

import static android.app.Activity.RESULT_OK;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int UPDATE_MY_HEAD = 3;
    private static final int SCAN_LOGIN_QRCODE_RESULT = 5;

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
    private LoadingDialog loadingDialog;

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

    /**
     * 初始化views
     */
    private void initViews() {
        loadingDialog = new LoadingDialog(getActivity());
        setContentItem = (RelativeLayout) rootView.findViewById(R.id.more_set_layout);
        userHeadLayout = (RelativeLayout) rootView.findViewById(R.id.more_userhead_layout);
        setContentItem.setOnClickListener(onClickListener);
        userHeadLayout.setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_help_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_message_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.more_invite_friends_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.about_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.customer_layout)).setOnClickListener(onClickListener);
        (rootView.findViewById(R.id.scan_login_desktop_layout)).setOnClickListener(onClickListener);
        moreHeadImg = (ImageView) rootView.findViewById(R.id.more_head_img);
        userNameText = (TextView) rootView.findViewById(R.id.more_head_textup);
        userOrgText = (TextView) rootView.findViewById(R.id.more_head_textdown);
        userCodeImg = (ImageView) rootView.findViewById(R.id.more_head_codeImg);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
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
        userNameText.setText(userName);
        userOrgText.setText(((MyApplication)getActivity().getApplicationContext()).getCurrentEnterprise().getName());
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
                    recordUserClickSetting();
                    break;
                case R.id.more_userhead_layout:
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    intent.putExtra("getMyInfoResult", (Serializable) getMyInfoResult);
                    startActivity(intent);
                    recordUserClickProfile();
                    break;
                case R.id.more_help_layout:
				    intent.setClass(getActivity(), FeedBackActivity.class);
				    startActivity(intent);
                    recordUserClickFeedback();
                    break;
                case R.id.more_message_layout:
                    ToastUtils.show(getActivity(), R.string.function_not_implemented);
                    break;
                case R.id.scan_login_desktop_layout:
                    //登录桌面版云+逻辑，还没有开放，有可能不从这里发起
                    intent.setClass(getActivity(), CaptureActivity.class);
                    intent.putExtra("from","MoreFragment");
                    startActivityForResult(intent,SCAN_LOGIN_QRCODE_RESULT);
                    break;
                case R.id.about_layout:
                    IntentUtils.startActivity(getActivity(),
                            AboutActivity.class);
                    recordUserClickAbout();
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
                        recordUserClickCustomservice();
                    }
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 记录用户使用了我的信息功能
     */
    private void recordUserClickProfile(){
        PVCollectModel pvCollectModel = new PVCollectModel();
        pvCollectModel.setCollectTime(System.currentTimeMillis());
        pvCollectModel.setFunctionID("profile");
        pvCollectModel.setFunctionType("mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }

    /**
     * 记录用户使用了设置功能
     */
    private void recordUserClickSetting(){
        PVCollectModel pvCollectModel = new PVCollectModel();
        pvCollectModel.setCollectTime(System.currentTimeMillis());
        pvCollectModel.setFunctionID("setting");
        pvCollectModel.setFunctionType("mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }

    /**
     * 记录用户使用了反馈功能
     */
    private void recordUserClickFeedback(){
        PVCollectModel pvCollectModel = new PVCollectModel();
        pvCollectModel.setCollectTime(System.currentTimeMillis());
        pvCollectModel.setFunctionID("feedback");
        pvCollectModel.setFunctionType("mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }

    /**
     * 记录用户使用了云+功能
     */
    private void recordUserClickCustomservice(){
        PVCollectModel pvCollectModel = new PVCollectModel();
        pvCollectModel.setCollectTime(System.currentTimeMillis());
        pvCollectModel.setFunctionID("customservice");
        pvCollectModel.setFunctionType("mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }

    /**
     * 记录用户使用了关于功能
     */
    private void recordUserClickAbout(){
        PVCollectModel pvCollectModel = new PVCollectModel();
        pvCollectModel.setCollectTime(System.currentTimeMillis());
        pvCollectModel.setFunctionID("about");
        pvCollectModel.setFunctionType("mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((resultCode == RESULT_OK) && (requestCode == SCAN_LOGIN_QRCODE_RESULT)){
            if(data.hasExtra("isDecodeSuccess")){
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess",false);
                if(isDecodeSuccess){
                    String msg = data.getStringExtra("msg");
                    LogUtils.YfcDebug("解析到的信息："+msg);
                    loginDesktopCloudPlus(msg);
                }else{
                    LogUtils.YfcDebug("解析失败");
                }
            }
        }
    }

    /**
     * 扫码登录云加逻辑
     * @param msg
     */
    private void loginDesktopCloudPlus(String msg){
        if(NetUtils.isNetworkConnected(getActivity())){
            if((loadingDialog != null) && !loadingDialog.isShowing()){
                loadingDialog.show();
                AppAPIService appAPIService = new AppAPIService(getActivity());
                appAPIService.setAPIInterface(new WebService());
                appAPIService.sendLoginDesktopCloudPlusInfo();
            }
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
            titleText.setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
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

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean) {
            if((loadingDialog != null) && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
        }

        @Override
        public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {
            if((loadingDialog != null) && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
        }

    }
}
