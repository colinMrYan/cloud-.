package com.inspur.emmcloud.ui.mine.myinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.Enterprise;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.UserProfileInfoBean;
import com.inspur.emmcloud.ui.login.ModifyUserPsdActivity;
import com.inspur.emmcloud.ui.login.ModifyUserPwdBySMSActivity;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.mine.setting.SwitchEnterpriseActivity;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.imagepicker.view.CropImageView;

import java.util.ArrayList;
import java.util.List;


public class MyInfoActivity extends BaseActivity {

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int UPDATE_MY_HEAD = 3;
    private static final int USER_INFO_CHANGE = 10;

    private ImageView userHeadImg;
    private TextView userMailText;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    private RelativeLayout resetLayout;
    private String photoLocalPath;
    private ImageDisplayUtils imageDisplayUtils;
    private GetMyInfoResult getMyInfoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplicationContext()).addActivity(this);
        setContentView(R.layout.activity_my_info);
        initView();
        getUserProfile();
        getUserInfoConfig();
        showMyInfo();

    }

    private void initView() {
        // TODO Auto-generated method stub
        loadingDlg = new LoadingDialog(MyInfoActivity.this);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        userHeadImg = (ImageView) findViewById(R.id.myinfo_userheadimg_img);
        userMailText = (TextView) findViewById(R.id.myinfo_usermail_text);
        resetLayout = (RelativeLayout) findViewById(R.id.myinfo_reset_layout);
        imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_photo_default);
        apiService = new MineAPIService(MyInfoActivity.this);
        apiService.setAPIInterface(new WebService());
    }

    /**
     * 显示个人信息数据
     **/
    private void showMyInfo() {
        if (getMyInfoResult != null) {
            String photoUri = UriUtils
                    .getChannelImgUri(MyInfoActivity.this, getMyInfoResult.getID());
            imageDisplayUtils.displayImage(userHeadImg, photoUri);
            String userName = getMyInfoResult.getName();
            ((TextView) findViewById(R.id.myinfo_username_text)).setText(userName.equals("null") ? getString(R.string.not_set) : userName);
            String mail = getMyInfoResult.getMail();
            userMailText.setText(mail.equals("null") ? getString(R.string.not_set) : mail);
            String phoneNumber = getMyInfoResult.getPhoneNumber();
            ((TextView) findViewById(R.id.myinfo_userphone_text)).setText(phoneNumber.equals("null") ? getString(R.string.not_set) : phoneNumber);
            ((TextView) findViewById(R.id.myinfo_usercompanytext_text)).setText(((MyApplication) getApplicationContext()).getCurrentEnterprise().getName());
        }

    }

    private void dimissDlg() {
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.myinfo_userhead_layout:
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    initImagePicker();
                    Intent intent = new Intent(getApplicationContext(),
                            ImageGridActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_IMG);

                } else {
                    Toast.makeText(MyInfoActivity.this,
                            getString(R.string.user_no_storage),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.back_layout:
                finish();
                break;
            case R.id.myinfo_modifypsd_layout:
                IntentUtils.startActivity(MyInfoActivity.this,
                        ModifyUserPsdActivity.class);
                break;
            case R.id.myinfo_reset_layout:
                Bundle bundle = new Bundle();
                bundle.putString("phoneNum", getMyInfoResult.getPhoneNumber());
                IntentUtils.startActivity(MyInfoActivity.this,
                        ModifyUserPwdBySMSActivity.class, bundle);
                break;
            case R.id.switch_enterprese_text:
                IntentUtils.startActivity(MyInfoActivity.this, SwitchEnterpriseActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == USER_INFO_CHANGE) {
            String userName = intent.getExtras().getString("newname", "");
            userMailText.setText(userName);
        } else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (intent != null && requestCode == REQUEST_CODE_SELECT_IMG) {
                ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) intent
                        .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                uploadUserHead(imageItemList.get(0).path);
            }
        }
    }


    /**
     * 初始化图片选择控件
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageDisplayUtils()); // 设置图片加载器
        imagePicker.setShowCamera(true); // 显示拍照按钮
        imagePicker.setCrop(true); // 允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
        imagePicker.setMultiMode(false);
        imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
        imagePicker.setFocusWidth(1000); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(1000); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000); // 保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000); // 保存文件的高度。单位像素
    }

    /**
     * 保存更新头像时间
     */
    private void saveUpdateHeadTime() {
        Contact contact = ContactCacheUtils.getUserContact(MyInfoActivity.this, ((MyApplication) getApplication()).getUid());
        contact.setLastUpdateTime(System.currentTimeMillis() + "");
        ContactCacheUtils.saveContact(MyInfoActivity.this, contact);
        ((MyApplication) getApplicationContext()).clearUserPhotoUrl(((MyApplication) getApplication()).getUid());
    }

    /**
     * 配置用户信息的显示和隐藏
     *
     * @param userProfileInfoBean
     */
    private void setUserInfoConfig(UserProfileInfoBean userProfileInfoBean) {
        if (userProfileInfoBean == null) {
            String response = PreferencesByUserAndTanentUtils.getString(getApplicationContext(), "user_profiles");
            if (!StringUtils.isBlank(response)) {
                userProfileInfoBean = new UserProfileInfoBean(response);
            }
        }
        if (userProfileInfoBean != null) {
            if (userProfileInfoBean.getShowHead() == 0) {
                (findViewById(R.id.myinfo_userhead_layout)).setVisibility(View.GONE);
            }
            if (userProfileInfoBean.getShowUserName() == 0) {
                (findViewById(R.id.myinfo_username_layout)).setVisibility(View.GONE);
            }
            if (userProfileInfoBean.getShowUserMail() == 0) {
                (findViewById(R.id.myinfo_usermail_layout)).setVisibility(View.GONE);
            }
            if (userProfileInfoBean.getShowUserPhone() == 0) {
                (findViewById(R.id.myinfo_userphone_layout)).setVisibility(View.GONE);
            }
            if (userProfileInfoBean.getShowEpInfo() == 0) {
                (findViewById(R.id.myinfo_usercompany_layout)).setVisibility(View.GONE);
            }
            if (userProfileInfoBean.getShowModifyPsd() == 1) {
                (findViewById(R.id.myinfo_modifypsd_layout)).setVisibility(View.VISIBLE);
            }
            if (userProfileInfoBean.getShowResetPsd() == 1) {
                resetLayout.setVisibility(View.VISIBLE);
            }
            //这里手机号格式的正确性由服务端保证，客户端只关心是否为空
            if (StringUtils.isBlank(getMyInfoResult.getPhoneNumber())) {
                resetLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置多企业切换按钮的显示和隐藏
     */
    private void setSwitchEnterpriseState() {
        List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
        if (enterpriseList.size() > 1) {
            (findViewById(R.id.switch_enterprese_text)).setVisibility(View.VISIBLE);
        }
    }


    /**
     * 上传用户头像
     *
     * @param
     */
    private void uploadUserHead(String photoPath) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            photoLocalPath = photoPath;
            apiService.updateUserHead(photoPath);
        }
    }

    /**
     * 获取用户profile信息
     */
    private void getUserProfile() {
        if (NetUtils.isNetworkConnected(MyInfoActivity.this, false)) {
            LoginAPIService apiServices = new LoginAPIService(MyInfoActivity.this);
            apiServices.setAPIInterface(new WebService());
            apiServices.getMyInfo();
        } else {
            dimissDlg();
            setSwitchEnterpriseState();
        }
    }

    /**
     * 获取用户信息配置
     */
    private void getUserInfoConfig() {
        if (NetUtils.isNetworkConnected(MyInfoActivity.this, false)) {
            loadingDlg.show();
            apiService.getUserProfileInfo();
        } else {
            setUserInfoConfig(null);
        }
    }


    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadMyHeadSuccess(
                GetUploadMyHeadResult getUploadMyHeadResult) {
            // TODO Auto-generated method stub
            dimissDlg();
            saveUpdateHeadTime();
            /**
             * 向更多页面发送消息修改头像
             */
            String userHeadImgUrl = getUploadMyHeadResult.getUrl();
            imageDisplayUtils.displayImage(userHeadImg, photoLocalPath);
            Message msg = new Message();
            msg.what = UPDATE_MY_HEAD;
            msg.obj = userHeadImgUrl;
            MoreFragment.handler.sendMessage(msg);
            // 通知消息页面重新创建群组头像
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "creat_group_icon");
            sendBroadcast(intent);
        }

        @Override
        public void returnUploadMyHeadFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            dimissDlg();
            WebServiceMiddleUtils.hand(MyInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnUserProfileSuccess(UserProfileInfoBean userProfileInfoBean) {
            setUserInfoConfig(userProfileInfoBean);
            PreferencesByUserAndTanentUtils.putString(getApplicationContext(), "user_profiles", userProfileInfoBean.getResponse());
            getUserProfile();
        }

        @Override
        public void returnUserProfileFail(String error, int errorCode) {
            setUserInfoConfig(null);
            getUserProfile();
        }

        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            // TODO Auto-generated method stub
            dimissDlg();
            MyInfoActivity.this.getMyInfoResult = getMyInfoResult;
            PreferencesUtils.putString(MyInfoActivity.this, "myInfo", getMyInfoResult.getResponse());
            showMyInfo();
            setSwitchEnterpriseState();
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            dimissDlg();
            setSwitchEnterpriseState();
        }
    }

}
