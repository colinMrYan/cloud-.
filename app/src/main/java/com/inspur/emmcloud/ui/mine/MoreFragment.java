package com.inspur.emmcloud.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MineLayoutItemGroup;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.mine.card.CardPackageActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.ui.mine.setting.SwitchEnterpriseActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.imagepicker.view.CropImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int REQUEST_SELECT_PHOTO = 1;
    private View rootView;
    private List<MineLayoutItemGroup> mineLayoutItemGroupList = new ArrayList<>();
    private BaseExpandableListAdapter adapter;
    private WebService webService;
    private LoadingDialog loadingDlg;
    private Button switchEnterpriseBtn;
    private GetMyInfoResult getMyInfoResult;
    private ImageView photoImg;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        webService = new WebService();
        loadingDlg = new LoadingDialog(getActivity());
        initData();
        initViews();
    }

    private void initData(){
        MainTabProperty mainTabProperty = AppTabUtils.getMainTabProperty(MyApplication.getInstance(),getClass().getSimpleName());
        if (mainTabProperty != null){
            mineLayoutItemGroupList = mainTabProperty.getMineLayoutItemGroupList();
        }
        if (mineLayoutItemGroupList.size() == 0){
            MineLayoutItemGroup mineLayoutItemGroupPersonnalInfo = new MineLayoutItemGroup();
            mineLayoutItemGroupPersonnalInfo.getMineLayoutItemList().add("my_personalInfo_function");
            MineLayoutItemGroup mineLayoutItemGroupSetting = new MineLayoutItemGroup();
            mineLayoutItemGroupSetting.getMineLayoutItemList().add("my_setting_function");
            MineLayoutItemGroup mineLayoutItemGroupCardbox = new MineLayoutItemGroup();
            mineLayoutItemGroupCardbox.getMineLayoutItemList().add("my_cardbox_function");
            MineLayoutItemGroup mineLayoutItemGroupAboutUs = new MineLayoutItemGroup();
            mineLayoutItemGroupAboutUs.getMineLayoutItemList().add("my_aboutUs_function");
            mineLayoutItemGroupList.add(mineLayoutItemGroupPersonnalInfo);
            mineLayoutItemGroupList.add(mineLayoutItemGroupSetting);
            mineLayoutItemGroupList.add(mineLayoutItemGroupCardbox);
            mineLayoutItemGroupList.add(mineLayoutItemGroupAboutUs);
        }
        getMyInfo();
        getMyInfoConfig();
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
        ExpandableListView expandListView =  rootView.findViewById(R.id.expandable_list);
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String layoutItem = mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().get(childPosition);
                switch (layoutItem){
                    case "my_personalInfo_function":
//                        Intent intent = new Intent();
//                        intent.setClass(getActivity(), MyInfoActivity.class);
//                        startActivityForResult(intent, REQUEST_CODE_UPDATE_USER_PHOTO);
//                        recordUserClick("profile");
                        break;
                    case "my_setting_function":
                        IntentUtils.startActivity(getActivity(), SettingActivity.class);
                        recordUserClick("setting");
                        break;
                    case "my_cardbox_function":
                        IntentUtils.startActivity(getActivity(), CardPackageActivity.class);
                        recordUserClick("wallet");
                        break;
                    case "my_aboutUs_function":
                        IntentUtils.startActivity(getActivity(),
                                AboutActivity.class);
                        recordUserClick("about");
                        break;
                    case "my_feedback_function":
                        IntentUtils.startActivity(getActivity(), FeedBackActivity.class);
                        recordUserClick("feedback");
                        break;
                    case "my_customerService_function":
                        if (MyApplication.getInstance().isV0VersionChat()){
                            Channel customerChannel = ChannelCacheUtils.getCustomerChannel(MyApplication.getInstance());
                            if (customerChannel != null){
                                Bundle bundle = new Bundle();
                                bundle.putString("cid", customerChannel.getCid());
                                //为区分来自云+客服添加一个from值，在ChannelActivity里使用
                                bundle.putString("from", "customer");
                                IntentUtils.startActivity(getActivity(),ChannelV0Activity.class, bundle);
                            }
                        }else if(MyApplication.getInstance().isV1xVersionChat()){
                            Conversation conversation = ConversationCacheUtils.getCustomerConversation(MyApplication.getInstance());
                            if (conversation != null){
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                                bundle.putString("from", "customer");
                                IntentUtils.startActivity(getActivity(),ConversationActivity.class, bundle);
                            }
                        }

                        recordUserClick("customservice");
                        break;
                    default:
                        break;
                }


                return false;
            }
        });
        adapter = new MyAdapter();
        expandListView.setAdapter(adapter);
        switchEnterpriseBtn = rootView.findViewById(R.id.bt_switch_enterprise);
        switchEnterpriseBtn.setVisibility(getMyInfoResult.getEnterpriseList().size()>0?View.VISIBLE:View.INVISIBLE);
        switchEnterpriseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtils.startActivity(getActivity(), SwitchEnterpriseActivity.class);
            }
        });
    }

    /**
     * expandableListView适配器
     */
    public class MyAdapter extends BaseExpandableListAdapter {
        //private List<List<String>> child;

        @Override
        public int getGroupCount() {
            return mineLayoutItemGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mineLayoutItemGroupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ExpandableListView expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            View view = new View(getActivity());
            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(MyApplication.getInstance(),0)));
            return view;
        }

        @Override
        public View getChildView(final int groupPosition,
                                 final int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            String layoutItem = (String) getChild(groupPosition, childPosition);
            if (layoutItem.equals("my_personalInfo_function")){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_my_info_card,null);
                photoImg = convertView.findViewById(R.id.iv_photo);
                TextView nameText = convertView.findViewById(R.id.tv_name);
                photoImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            initImagePicker();
                            Intent intent = new Intent(getActivity(),ImageGridActivity.class);
                            startActivityForResult(intent, REQUEST_SELECT_PHOTO);
                        } else {
                            ToastUtils.show(MyApplication.getInstance(),getString(R.string.user_no_storage));
                        }
                    }
                });
                String photoUri = APIUri.getUserIconUrl(getActivity(), MyApplication.getInstance().getUid());
                ImageDisplayUtils.getInstance().displayImage(photoImg, photoUri, R.drawable.icon_photo_default);
                String username = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                nameText.setText(username);
                setUserInfoConfig(convertView);
            }else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_common_item_view,null);
                setViewByLayoutItem(convertView,layoutItem);
            }
            return convertView;
        }

        private void setViewByLayoutItem(View convertView,String layoutItem){
            CircleTextImageView iconImg = convertView.findViewById(R.id.iv_icon);
            TextView titleText = convertView.findViewById(R.id.tv_name_tips);
            switch (layoutItem){
                case "my_setting_function":
                    iconImg.setImageResource(R.drawable.ic_mine_setting);
                    titleText.setText(R.string.settings);
                    break;
                case "my_cardbox_function":
                    iconImg.setImageResource(R.drawable.ic_mine_wallet);
                    titleText.setText(R.string.wallet);
                    break;
                case "my_aboutUs_function":
                    iconImg.setImageResource(R.drawable.ic_mine_about);
                    //ImageDisplayUtils.getInstance().displayImage(iconImg,"drawable://"+ AppUtils.getAppIconRes(MyApplication.getInstance()),R.drawable.ic_launcher);
                    titleText.setText(R.string.about_text);
                    break;
                case "my_feedback_function":
                    iconImg.setImageResource(R.drawable.icon_help);
                    titleText.setText(R.string.more_feedback);
                    break;
                case "my_customerService_function":
                    iconImg.setImageResource(R.drawable.ic_customer);
                    titleText.setText(R.string.app_customer);
                    break;
                default:
                    break;
            }
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }


    private void setUserInfoConfig(View view) {
        String myInfoShowConfig = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MY_INFO_SHOW_CONFIG);
        UserProfileInfoBean userProfileInfoBean = null;
        if (StringUtils.isBlank(myInfoShowConfig)){
            userProfileInfoBean = new UserProfileInfoBean(myInfoShowConfig);
        }else {
            userProfileInfoBean = new UserProfileInfoBean();
        }

        RelativeLayout mailLayout = view.findViewById(R.id.rl_mail);
        if (userProfileInfoBean.getShowUserMail() == 1){
            mailLayout.setVisibility((userProfileInfoBean.getShowUserMail() == 1)?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.tv_mail)).setText(getMyInfoResult.getMail());
        }else {
            mailLayout.setVisibility(View.GONE);
        }
        RelativeLayout phoneLayout = view.findViewById(R.id.rl_phone);
        if (userProfileInfoBean.getShowUserPhone() == 1){
            phoneLayout.setVisibility((userProfileInfoBean.getShowUserMail() == 1)?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.tv_phone)).setText(getMyInfoResult.getPhoneNumber());
        }else {
            phoneLayout.setVisibility(View.GONE);
        }

        RelativeLayout jobNumberLayout = view.findViewById(R.id.rl_job_number);
        if (userProfileInfoBean.getShowEmpNum() == 1){
            jobNumberLayout.setVisibility((userProfileInfoBean.getShowUserMail() == 1)?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.tv_job_number)).setText(userProfileInfoBean.getEmpNum());
        }else {
            jobNumberLayout.setVisibility(View.GONE);
        }

        RelativeLayout companyLayout = view.findViewById(R.id.rl_company);
        if (userProfileInfoBean.getShowEpInfo() == 1){
            companyLayout.setVisibility((userProfileInfoBean.getShowUserMail() == 1)?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.tv_company)).setText(MyApplication.getInstance().getCurrentEnterprise().getName());
        }else {
            companyLayout.setVisibility(View.GONE);
        }
        RelativeLayout orgLayout = view.findViewById(R.id.rl_org);
        if (userProfileInfoBean.getShowEmpNum() == 1){
            orgLayout.setVisibility((userProfileInfoBean.getShowUserMail() == 1)?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.tv_org)).setText(userProfileInfoBean.getEmpNum());
        }else {
            orgLayout.setVisibility(View.GONE);
        }
    }


    /**
     * 初始化图片选择控件
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(ImageDisplayUtils.getInstance()); // 设置图片加载器
        imagePicker.setShowCamera(true); // 显示拍照按钮
        imagePicker.setCrop(true); // 允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
        imagePicker.setMultiMode(false);
        imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
        imagePicker.setFocusWidth(800); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(450); // 保存文件的宽度。单位像素
        imagePicker.setOutPutY(450); // 保存文件的高度。单位像素
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS && requestCode == REQUEST_SELECT_PHOTO) {
            ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                    .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            uploadUserHead(imageItemList.get(0).path);
        }
    }


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
     * 保存更新头像时间
     */
    private void saveUpdateHeadTime() {
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid());
        contactUser.setLastQueryTime(System.currentTimeMillis()+"");
        contactUser.setHasHead(1);
        ContactUserCacheUtils.saveContactUser(contactUser);
        MyApplication.getInstance().clearUserPhotoUrl(MyApplication.getInstance().getUid());
    }

    /**
     * 获取用户profile信息
     */
    private void getMyInfo() {
        String myInfo = PreferencesUtils.getString(getContext(), "myInfo", "");
        this.getMyInfoResult = new GetMyInfoResult(myInfo);
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            LoginAPIService apiServices = new LoginAPIService(getActivity());
            apiServices.setAPIInterface(webService);
            apiServices.getMyInfo();
        }
    }


    /**
     * 获取用户信息配置
     */
    private void getMyInfoConfig() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            MineAPIService apiService = new MineAPIService(getActivity());
            apiService.setAPIInterface(webService);
            apiService.getUserProfileConfigInfo();
        }
    }

    /**
     * 上传用户头像
     *
     * @param
     */
    private void uploadUserHead(String photoPath) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getActivity())) {
            loadingDlg.show();
            MineAPIService apiService = new MineAPIService(getActivity());
            apiService.setAPIInterface(webService);
            apiService.updateUserHead(photoPath);
        }
    }


    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyHeadResult,String filePath) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            saveUpdateHeadTime();
            ImageDisplayUtils.getInstance().displayImage(photoImg, filePath);
            // 通知消息页面重新创建群组头像
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "creat_group_icon");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        @Override
        public void returnUploadMyHeadFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(),Constant.PREF_MY_INFO_SHOW_CONFIG, userProfileInfoBean.getResponse());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnUserProfileConfigFail(String error, int errorCode) {
        }

        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            // TODO Auto-generated method stub
            MoreFragment.this.getMyInfoResult = getMyInfoResult;
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            switchEnterpriseBtn.setVisibility(enterpriseList.size()>0?View.VISIBLE:View.INVISIBLE);
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null){
                ToastUtils.show(MyApplication.getInstance(),  R.string.login_user_not_bound_enterprise);
                MyApplication.getInstance().signout();
            }else {
                PreferencesUtils.putString(getActivity(), "myInfo", getMyInfoResult.getResponse());
            }
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
        }
    }
}
