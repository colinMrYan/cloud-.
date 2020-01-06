package com.inspur.emmcloud.setting.ui.myinfo;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.imagepicker.view.CropImageView;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.setting.bean.UserProfileInfoBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyInfoActivity extends BaseActivity {

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int USER_INFO_CHANGE = 10;

    @BindView(R2.id.iv_photo)
    ImageView photoImg;
    @BindView(R2.id.tv_name)
    TextView nameText;
    @BindView(R2.id.rl_employee_no)
    RelativeLayout employeeNOLayout;
    @BindView(R2.id.rl_office_phone)
    RelativeLayout officePhoneLayout;
    @BindView(R2.id.rl_phone)
    RelativeLayout phoneLayout;
    @BindView(R2.id.rl_enterprise)
    RelativeLayout enterpriseLayout;
    @BindView(R2.id.rl_mail)
    RelativeLayout mailLayout;
    @BindView(R2.id.rl_photo)
    RelativeLayout photoLayout;
    @BindView(R2.id.tv_employee_no)
    TextView employeeNOText;
    @BindView(R2.id.tv_office_phone)
    TextView officePhoneText;
    @BindView(R2.id.tv_phone)
    TextView phoneText;
    @BindView(R2.id.tv_enterprise)
    TextView enterpriseText;
    @BindView(R2.id.tv_mail)
    TextView mailText;
    @BindView(R2.id.tv_department)
    TextView departmentText;

    private SettingAPIService apiService;
    private LoadingDialog loadingDlg;
    private String photoLocalPath;
    private GetMyInfoResult getMyInfoResult;
    private boolean isUpdateUserPhoto = false; // 标记是否更改了头像

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        apiService = new SettingAPIService(MyInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        getUserProfile();
        getUserInfoConfig();
        showMyInfo();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_my_info_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    /**
     * 显示个人信息数据
     **/
    private void showMyInfo() {
        if (getMyInfoResult == null) {
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            getMyInfoResult = new GetMyInfoResult(myInfo);
        }
        String photoUri = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), getMyInfoResult.getID());
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
        enterpriseText.setText(BaseApplication.getInstance().getCurrentEnterprise().getName());
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.iv_photo) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                initImagePicker();
                Intent intent = new Intent(getApplicationContext(), ImageGridActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMG);
            } else {
                ToastUtils.show(MyInfoActivity.this, getString(R.string.user_no_storage));
            }
        } else if (id == R.id.ibt_back) {
            finishActivity();
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
        ContactService contactService = Router.getInstance().getService(ContactService.class);
        ContactUser contactUser = null;
        if (contactService != null) {
            contactUser = contactService.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
        if (contactUser != null) {
            contactUser.setLastQueryTime(System.currentTimeMillis() + "");
            contactUser.setHasHead(1);
            contactService.saveContactUser(contactUser);
        }
        BaseApplication.getInstance().clearUserPhotoUrl(BaseApplication.getInstance().getUid());
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
            BaseModuleApiService apiServices = new BaseModuleApiService(MyInfoActivity.this);
            apiServices.setAPIInterface(new BaseModuleWebService());
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

    public class BaseModuleWebService extends BaseModuleAPIInterfaceInstance {

        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            // TODO Auto-generated method stub
            MyInfoActivity.this.getMyInfoResult = getMyInfoResult;
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.login_user_not_bound_enterprise);
                BaseApplication.getInstance().signout();
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

    public class WebService extends SettingAPIInterfaceImpl {

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

    }

}
