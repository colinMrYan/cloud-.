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
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.UserProfileInfoBean;
import com.inspur.emmcloud.ui.login.ModifyUserPsdActivity;
import com.inspur.emmcloud.ui.login.ModifyUserPwdBySMSActivity;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.imagepicker.view.CropImageView;

import java.util.ArrayList;


public class MyInfoActivity extends BaseActivity {

	private static final int REQUEST_CODE_SELECT_IMG = 1;
	private static final int UPDATE_MY_HEAD = 3;
	private static final int USER_INFO_CHANGE = 10;

	private ImageView userHeadImg;
	private TextView userNameText;
	private TextView userMailText;
	private TextView userPhoneText;
	private TextView tenantNameText;

	private MineAPIService apiService;
	private LoadingDialog loadingDlg;

	private RelativeLayout userHeadLayout;
	private RelativeLayout userNameLayout;
	private RelativeLayout userMailLayout;
	private RelativeLayout userPhoneLayout;
	private RelativeLayout userCompanyLayout;
	private RelativeLayout backLayout;
	private RelativeLayout modifyLayout;
	private RelativeLayout resetLayout;
	private String photoLocalPath;
	private ImageDisplayUtils imageDisplayUtils;
	private GetMyInfoResult getMyInfoResult;
	private LoadingDialog loadingDialog;
	private View passWordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_my_info);
		initView();
		getUserProfile();
		showMyInfo();

	}

	/**
	 *
	 */
	private void getUserProfile() {
		if(NetUtils.isNetworkConnected(MyInfoActivity.this)){
			if(loadingDialog != null && !loadingDialog.isShowing()){
				loadingDialog.show();
			}
			apiService.getUserProfileInfo();
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		loadingDialog = new LoadingDialog(MyInfoActivity.this);
		getMyInfoResult = (GetMyInfoResult) getIntent().getExtras()
				.getSerializable("getMyInfoResult");
		userHeadImg = (ImageView) findViewById(R.id.myinfo_userheadimg_img);
		userNameText = (TextView) findViewById(R.id.myinfo_username_text);
		userMailText = (TextView) findViewById(R.id.myinfo_usermail_text);
		userPhoneText = (TextView) findViewById(R.id.myinfo_userphone_text);
		tenantNameText = (TextView) findViewById(R.id.myinfo_usercompanytext_text);
		loadingDlg = new LoadingDialog(this);
		userHeadLayout = (RelativeLayout) findViewById(R.id.myinfo_userhead_layout);
		userNameLayout = (RelativeLayout) findViewById(R.id.myinfo_username_layout);
		userMailLayout = (RelativeLayout) findViewById(R.id.myinfo_usermail_layout);
		userPhoneLayout = (RelativeLayout) findViewById(R.id.myinfo_userphone_layout);
		userCompanyLayout = (RelativeLayout) findViewById(R.id.myinfo_usercompany_layout);
		backLayout = (RelativeLayout) findViewById(R.id.back_layout);
		modifyLayout = (RelativeLayout) findViewById(R.id.myinfo_modifypsd_layout);
		resetLayout = (RelativeLayout) findViewById(R.id.myinfo_reset_layout);
		passWordView = findViewById(R.id.myinfo_password_line);
		imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(),
				R.drawable.icon_photo_default);
		apiService = new MineAPIService(MyInfoActivity.this);
		apiService.setAPIInterface(new WebService());
		//这里手机号格式的正确性由服务端保证，客户端只关心是否为空
		if (StringUtils.isBlank(getMyInfoResult.getPhoneNumber())) {
			resetLayout.setVisibility(View.GONE);
		}

	}

	/**
	 * 显示个人信息数据
	 **/
	private void showMyInfo() {
		if (getMyInfoResult != null) {
			String photoUri = UriUtils
					.getChannelImgUri(getMyInfoResult.getID());
			imageDisplayUtils.display(userHeadImg, photoUri);
			String userName = getMyInfoResult.getName();
			userNameText.setText(userName.equals("null") ? getString(R.string.not_set) : userName);
			String mail = getMyInfoResult.getMail();
			userMailText.setText(mail.equals("null") ? getString(R.string.not_set) : mail);
			String phoneNumber = getMyInfoResult.getPhoneNumber();
			userPhoneText.setText(phoneNumber.equals("null") ? getString(R.string.not_set) : phoneNumber);
			tenantNameText.setText(getMyInfoResult.getEnterpriseName());
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
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent intent) {
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

	public class WebService extends APIInterfaceInstance {

		@Override
		public void returnUploadMyHeadSuccess(
				GetUploadMyHeadResult getUploadMyHeadResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}

			/**
			 * 向更多页面发送消息修改头像
			 */
			String userHeadImgUrl = getUploadMyHeadResult.getUrl();
			imageDisplayUtils.display(userHeadImg, photoLocalPath);
			Message msg = new Message();
			msg.what = UPDATE_MY_HEAD;
			msg.obj = userHeadImgUrl;
			MoreFragment.handler.sendMessage(msg);
		}

		@Override
		public void returnUploadMyHeadFail(String error) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}

			WebServiceMiddleUtils.hand(MyInfoActivity.this, error);
		}

		@Override
		public void returnUserProfileSuccess(UserProfileInfoBean userProfileInfoBean) {
			if(loadingDialog != null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			updateInfoState(userProfileInfoBean);
		}

		@Override
		public void returnUserProfileFail(String error) {
			if(loadingDialog != null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}
	}

	/**
	 * 处理
	 * @param userProfileInfoBean
     */
	private void updateInfoState(UserProfileInfoBean userProfileInfoBean) {
		if(userProfileInfoBean.getShowHead() == 0){
			userHeadLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowUserName() == 0){
			userNameLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowUserMail() == 0){
			userMailLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowUserPhone() == 0){
			userPhoneLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowEpInfo() == 0){
			userCompanyLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowModifyPsd() == 0){
			passWordView.setVisibility(View.GONE);
			modifyLayout.setVisibility(View.GONE);
		}
		if(userProfileInfoBean.getShowResetPsd() == 0){
			passWordView.setVisibility(View.GONE);
			resetLayout.setVisibility(View.GONE);
		}
	}


}
