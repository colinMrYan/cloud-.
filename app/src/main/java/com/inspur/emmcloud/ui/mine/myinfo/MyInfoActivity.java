package com.inspur.emmcloud.ui.mine.myinfo;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.imagepicker.view.CropImageView;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetUploadMyHeadResult;
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
import com.inspur.emmcloud.widget.dialogs.MyDialog;


public class MyInfoActivity extends BaseActivity {

	private static final int RESULT_REQUEST_CODE = 2;
	private static final int UPDATE_MY_HEAD = 3;
	private static final int USER_INFO_CHANGE = 10;
	private static final int REQUEST_CODE_SELECT_IMG = 1;
	private static final String IMAGE_FILE_NAME = "photoImage.jpg";

	private ImageView userHeadImg;
	private TextView userName;
	private TextView userMail;
	private TextView userPhone;
	private TextView tenantName;

	private MineAPIService apiService;
	private LoadingDialog loadingDlg;

	private RelativeLayout userHeadLayout;
	private RelativeLayout userNameLayout;
	private RelativeLayout userDepartLayout;
	private RelativeLayout userMailLayout;
	private RelativeLayout userPhoneLayout;
	private RelativeLayout userSexLayout;
	private RelativeLayout userCodeLayout;
	private RelativeLayout userCompanyLayout;
	private RelativeLayout backLayout;
	private RelativeLayout modifyLayout;
	private RelativeLayout resetLayout;
	private boolean isHasSdcard;

	private MyDialog myDialog;
	private TextView camaralText, galleryText;

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
		showMyInfo();

	}

	private void initView() {
		// TODO Auto-generated method stub
		getMyInfoResult = (GetMyInfoResult) getIntent().getExtras()
				.getSerializable("getMyInfoResult");
		userHeadImg = (ImageView) findViewById(R.id.myinfo_userheadimg_img);
		userName = (TextView) findViewById(R.id.myinfo_username_text);
		userMail = (TextView) findViewById(R.id.myinfo_usermail_text);
		userPhone = (TextView) findViewById(R.id.myinfo_userphone_text);
		tenantName = (TextView) findViewById(R.id.myinfo_usercompanytext_text);
		loadingDlg = new LoadingDialog(this);

		myDialog = new MyDialog(MyInfoActivity.this,
				R.layout.dialog_modify_portrait, R.style.userhead_dialog_bg);
		userHeadLayout = (RelativeLayout) findViewById(R.id.myinfo_userhead_layout);
		userHeadLayout.setOnClickListener(new OnMoreClickItemListener());
		userNameLayout = (RelativeLayout) findViewById(R.id.myinfo_username_layout);
		userNameLayout.setOnClickListener(new OnMoreClickItemListener());
		userDepartLayout = (RelativeLayout) findViewById(R.id.myinfo_userdepart_layout);
		userDepartLayout.setOnClickListener(new OnMoreClickItemListener());
		userMailLayout = (RelativeLayout) findViewById(R.id.myinfo_usermail_layout);
		userMailLayout.setOnClickListener(new OnMoreClickItemListener());
		userPhoneLayout = (RelativeLayout) findViewById(R.id.myinfo_userphone_layout);
		userPhoneLayout.setOnClickListener(new OnMoreClickItemListener());
		userSexLayout = (RelativeLayout) findViewById(R.id.myinfo_usersex_layout);
		userSexLayout.setOnClickListener(new OnMoreClickItemListener());
		userCodeLayout = (RelativeLayout) findViewById(R.id.myinfo_usercode_layout);
		userCodeLayout.setOnClickListener(new OnMoreClickItemListener());
		userCompanyLayout = (RelativeLayout) findViewById(R.id.myinfo_usercompany_layout);
		userCompanyLayout.setOnClickListener(new OnMoreClickItemListener());
		backLayout = (RelativeLayout) findViewById(R.id.back_layout);
		backLayout.setOnClickListener(new OnMoreClickItemListener());
		modifyLayout = (RelativeLayout) findViewById(R.id.myinfo_modifypsd_layout);
		modifyLayout.setOnClickListener(new OnMoreClickItemListener());
		resetLayout = (RelativeLayout) findViewById(R.id.myinfo_reset_layout);
		resetLayout.setOnClickListener(new OnMoreClickItemListener());
		imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(),
				R.drawable.icon_photo_default);
		apiService = new MineAPIService(MyInfoActivity.this);
		apiService.setAPIInterface(new WebService());
		//这里手机号格式的正确性由服务端保证，客户端只关心是否为空
		if(StringUtils.isBlank(getMyInfoResult.getPhoneNumber())){
			resetLayout.setVisibility(View.GONE);
		}
	}

	/** 显示个人信息数据 **/
	private void showMyInfo() {
		if (getMyInfoResult != null) {
			// String inspurId = getMyInfoResult.getOldId();
			String photoUri = UriUtils
					.getChannelImgUri(getMyInfoResult.getID());
			imageDisplayUtils.display(userHeadImg, photoUri);

			if (!getMyInfoResult.getName().equals("null")) {
				userName.setText(getMyInfoResult.getName());
			} else {
				userName.setText(getString(R.string.not_set));
			}

			if (!getMyInfoResult.getMail().equals("null")) {
				userMail.setText(getMyInfoResult.getMail());
			} else {
				userMail.setText(getString(R.string.not_set));
			}

			if (!getMyInfoResult.getPhoneNumber().equals("null")
					&& !getMyInfoResult.getPhoneNumber().equals("")) {
				userPhone.setText(getMyInfoResult.getPhoneNumber());
			} else {
				userPhone.setText(getString(R.string.not_set));
			}

			// if (!getMyInfoResult.getTenantName().equals("null")) {
			// tenantName.setText(getMyInfoResult.getTenantName());
			// } else {
			tenantName.setText(getMyInfoResult.getEnterpriseName());
			// }
		}

	}

	class OnMoreClickItemListener implements OnClickListener {

		Intent intentModify = new Intent();

		@Override
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
							ModifyUserPwdBySMSActivity.class,bundle);
				break;
			default:
				break;
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

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_OK && requestCode == USER_INFO_CHANGE) {
			String userNametext = null;
			if (intent != null) {
				userNametext = intent.getStringExtra("newname");
			}
			userName.setText(userNametext);
		} else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
			if (intent != null && requestCode == REQUEST_CODE_SELECT_IMG) {
				ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) intent
						.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
				String photoPath = imageItemList.get(0).path;
				uploadUserHead(photoPath);
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


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

}
