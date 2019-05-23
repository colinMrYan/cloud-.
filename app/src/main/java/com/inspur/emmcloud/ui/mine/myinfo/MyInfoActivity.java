package com.inspur.emmcloud.ui.mine.myinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.ui.login.LoginBySmsActivity;
import com.inspur.emmcloud.ui.login.PasswordModifyActivity;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyInfoActivity extends BaseActivity {

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int USER_INFO_CHANGE = 10;

    @BindView(R.id.iv_photo)
    ImageView photoImg;
    @BindView(R.id.tv_name)
    TextView nameText;
    @BindView(R.id.rl_employee_no)
    RelativeLayout employeeNOLayout;
    @BindView(R.id.rl_office_phone)
    RelativeLayout officePhoneLayout;
    @BindView(R.id.rl_phone)
    RelativeLayout phoneLayout;
    @BindView(R.id.rl_enterprise)
    RelativeLayout enterpriseLayout;
    @BindView(R.id.rl_mail)
    RelativeLayout mailLayout;
    @BindView(R.id.rl_photo)
    RelativeLayout photoLayout;
    @BindView(R.id.tv_employee_no)
    TextView employeeNOText;
    @BindView(R.id.tv_office_phone)
    TextView officePhoneText;
    @BindView(R.id.tv_phone)
    TextView phoneText;
    @BindView(R.id.tv_enterprise)
    TextView enterpriseText;
    @BindView(R.id.tv_mail)
    TextView mailText;
    @BindView(R.id.tv_department)
    TextView departmentText;
    @BindView(R.id.rl_password_modify)
    RelativeLayout passwordModifyLayout;
    @BindView(R.id.rl_password_reset)
    RelativeLayout passwordResetLayout;

    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    private String photoLocalPath;
    private GetMyInfoResult getMyInfoResult;
    private boolean isUpdateUserPhoto = false; // 标记是否更改了头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        ButterKnife.bind(this);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
        loadingDlg = new LoadingDialog(this);
        apiService = new MineAPIService(MyInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        getUserProfile();
        getUserInfoConfig();
        showMyInfo();

    }

    /**
     * 显示个人信息数据
     **/
    private void showMyInfo() {
        if (getMyInfoResult == null) {
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            getMyInfoResult = new GetMyInfoResult(myInfo);
        }
        String photoUri = APIUri.getChannelImgUrl(MyInfoActivity.this, getMyInfoResult.getID());
        ImageDisplayUtils.getInstance().displayImage(photoImg, photoUri, R.drawable.icon_photo_default);
        if (!StringUtils.isBlank(getMyInfoResult.getName())) {
            nameText.setText(getMyInfoResult.getName());
        }
        if (!StringUtils.isBlank(getMyInfoResult.getPhoneNumber())) {
            phoneText.setText(getMyInfoResult.getPhoneNumber());
        }
        if (!StringUtils.isBlank(getMyInfoResult.getMail())) {
            mailText.setText(getMyInfoResult.getMail());
        }
        enterpriseText.setText(MyApplication.getInstance().getCurrentEnterprise().getName());
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.iv_photo:
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    initImagePicker();
                    Intent intent = new Intent(getApplicationContext(), ImageGridActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_IMG);
                } else {
                    ToastUtils.show(MyInfoActivity.this, getString(R.string.user_no_storage));
                }
                break;
            case R.id.ibt_back:
                finishActivity();
                break;
            case R.id.rl_password_modify:
                IntentUtils.startActivity(MyInfoActivity.this, PasswordModifyActivity.class);
                break;
            case R.id.rl_password_reset:
                Bundle bundle = new Bundle();
                bundle.putInt(LoginBySmsActivity.EXTRA_MODE, LoginBySmsActivity.MODE_FORGET_PASSWORD);
                bundle.putString(LoginBySmsActivity.EXTRA_PHONE, getMyInfoResult.getPhoneNumber());
                IntentUtils.startActivity(MyInfoActivity.this, LoginBySmsActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == USER_INFO_CHANGE) {
            String userName = intent.getExtras().getString("newname", "");
            mailText.setText(userName);
        } else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (intent != null && requestCode == REQUEST_CODE_SELECT_IMG) {
                ArrayList<ImageItem> imageItemList =
                        (ArrayList<ImageItem>) intent.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
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
        contactUser.setLastQueryTime(System.currentTimeMillis() + "");
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
            enterpriseLayout.setVisibility((userProfileInfoBean.getShowEpInfo() == 0) ? View.GONE : View.VISIBLE);
            photoLayout.setVisibility((userProfileInfoBean.getShowHead() == 0) ? View.GONE : View.VISIBLE);
            nameText.setVisibility((userProfileInfoBean.getShowUserName() == 0) ? View.GONE : View.VISIBLE);
            passwordModifyLayout.setVisibility((userProfileInfoBean.getShowModifyPsd() == 0) ? View.GONE : View.VISIBLE);
            passwordResetLayout.setVisibility((userProfileInfoBean.getShowResetPsd() == 0) ? View.GONE : View.VISIBLE);
            phoneLayout.setVisibility((0 == userProfileInfoBean.getShowUserPhone()) ? View.GONE : View.VISIBLE);
            mailLayout.setVisibility(0 == userProfileInfoBean.getShowUserMail() ? View.GONE : View.VISIBLE);
            officePhoneLayout.setVisibility(
                    (!StringUtils.isBlank(userProfileInfoBean.getTelePhone())) ? View.VISIBLE : View.GONE);
            officePhoneText.setText(userProfileInfoBean.getTelePhone());
            employeeNOLayout
                    .setVisibility((!StringUtils.isBlank(userProfileInfoBean.getEmpNum())) ? View.VISIBLE : View.GONE);
            employeeNOText.setText(userProfileInfoBean.getEmpNum());
            departmentText.setText(userProfileInfoBean.getOrgName());
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
        public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyHeadResult, String filePath) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            saveUpdateHeadTime();
            isUpdateUserPhoto = true;
            ImageDisplayUtils.getInstance().displayImage(photoImg, photoLocalPath);
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
            PreferencesByUserAndTanentUtils.putString(getApplicationContext(), "user_profiles",
                    userProfileInfoBean.getResponse());
        }

        @Override
        public void returnUserProfileConfigFail(String error, int errorCode) {
            setUserInfoConfig(null);
        }

        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            // TODO Auto-generated method stub
            MyInfoActivity.this.getMyInfoResult = getMyInfoResult;
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null) {
                ToastUtils.show(MyApplication.getInstance(), R.string.login_user_not_bound_enterprise);
                MyApplication.getInstance().signout();
            } else {
                PreferencesUtils.putString(MyInfoActivity.this, "myInfo", getMyInfoResult.getResponse());
                showMyInfo();
            }
        }

        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
        }
    }

}
