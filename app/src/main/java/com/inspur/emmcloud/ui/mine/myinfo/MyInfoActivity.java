package com.inspur.emmcloud.ui.mine.myinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.ui.login.ModifyUserPsdActivity;
import com.inspur.emmcloud.ui.login.ModifyUserPwdBySMSActivity;
import com.inspur.emmcloud.ui.mine.setting.SwitchEnterpriseActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.imagepicker.view.CropImageView;

import java.util.ArrayList;
import java.util.List;


public class MyInfoActivity extends BaseActivity {

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int USER_INFO_CHANGE = 10;

    private ImageView userHeadImg;
    private TextView userMailText;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    private RelativeLayout resetLayout;
    private String photoLocalPath;
    private GetMyInfoResult getMyInfoResult;
    private boolean isUpdateUserPhoto = false; //标记是否更改了头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        initView();
        getUserInfoConfig();
        showMyInfo();

    }

    private void initView() {
        // TODO Auto-generated method stub
        loadingDlg = new LoadingDialog(MyInfoActivity.this);
        userHeadImg = (ImageView) findViewById(R.id.myinfo_userheadimg_img);
        userMailText = (TextView) findViewById(R.id.myinfo_usermail_text);
        resetLayout = (RelativeLayout) findViewById(R.id.myinfo_reset_layout);
        apiService = new MineAPIService(MyInfoActivity.this);
        apiService.setAPIInterface(new WebService());
    }

    /**
     * 显示个人信息数据
     **/
    private void showMyInfo() {
        if (getMyInfoResult == null) {
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            getMyInfoResult = new GetMyInfoResult(myInfo);
        }
        String photoUri = APIUri
                .getChannelImgUrl(MyInfoActivity.this, getMyInfoResult.getID());
        ImageDisplayUtils.getInstance().displayImage(userHeadImg, photoUri, R.drawable.icon_photo_default);
        String userName = getMyInfoResult.getName();
        ((TextView) findViewById(R.id.myinfo_username_text)).setText((StringUtils.isBlank(userName) ||userName.equals("null")) ? getString(R.string.not_set) : userName);
        String mail = getMyInfoResult.getMail();
        userMailText.setText((StringUtils.isBlank(mail) ||mail.equals("null")) ? getString(R.string.not_set) : mail);
        String phoneNumber = getMyInfoResult.getPhoneNumber();
        ((TextView) findViewById(R.id.myinfo_userphone_text)).setText((StringUtils.isBlank(phoneNumber) || phoneNumber.equals("null")) ? getString(R.string.not_set) : phoneNumber);
        ((TextView) findViewById(R.id.myinfo_usercompanytext_text)).setText(MyApplication.getInstance().getCurrentEnterprise().getName());
        List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
        findViewById(R.id.switch_enterprese_text).setVisibility((enterpriseList.size() > 1)?View.VISIBLE:View.GONE);

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
                    ToastUtils.show(MyInfoActivity.this,
                            getString(R.string.user_no_storage));
                }
                break;
            case R.id.back_layout:
                finishActivity();
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

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        if (isUpdateUserPhoto) {
            setResult(RESULT_OK);
        }
        finish();
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
     * 获取用户信息配置
     */
    private void getUserInfoConfig() {
        if (NetUtils.isNetworkConnected(MyInfoActivity.this, false)) {
            apiService.getUserProfileConfigInfo();
        } else {
            setUserInfoConfig(null);
        }
    }


    public class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyHeadResult) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            saveUpdateHeadTime();
            isUpdateUserPhoto = true;
            ImageDisplayUtils.getInstance().displayImage(userHeadImg, photoLocalPath);
            // 通知消息页面重新创建群组头像
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "creat_group_icon");
            LocalBroadcastManager.getInstance(MyInfoActivity.this).sendBroadcast(intent);
        }

        @Override
        public void returnUploadMyHeadFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {
            setUserInfoConfig(userProfileInfoBean);
            PreferencesByUserAndTanentUtils.putString(getApplicationContext(), "user_profiles", userProfileInfoBean.getResponse());
        }

        @Override
        public void returnUserProfileConfigFail(String error, int errorCode) {
            setUserInfoConfig(null);
        }
    }

}
