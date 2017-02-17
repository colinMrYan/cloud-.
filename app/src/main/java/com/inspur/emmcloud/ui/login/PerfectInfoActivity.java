package com.inspur.emmcloud.ui.login;

import com.inspur.emmcloud.BaseActivity;

/**
 * 完善资料页面
 * 
 * @author Administrator
 *
 */
public class PerfectInfoActivity extends BaseActivity {
//
//	private EditText nickNameEdit;
//	private EditText passwordEdit;
//	private EditText passwordConfirmEdit;
//	private EditText sexEdit;
//	private EditText mailEdit;
//
//	MyDialog myDialog;
//	TextView camaralText, galleryText;
//	private Boolean isHasSdcard;
//	private CircleImageView circleImageView;
//	private APIService apiService;
//
//	private static final String IMAGE_FILE_NAME = "photoImage.jpg";
//	protected static final int CAMERA_REQUEST_CODE = 0;
//	protected static final int IMAGE_REQUEST_CODE = 1;
//	protected static final int RESULT_REQUEST_CODE = 2;
//	protected static final int PHOTO_SIZE = 100;
//	protected static final int HAND_SET_USERHEAD = 9;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		((MyApplication) getApplicationContext())
//				.addActivity(PerfectInfoActivity.this);
//		setContentView(R.layout.activity_perfect_info);
//		
//		nickNameEdit = (ClearEditText) findViewById(R.id.nickname_edit);
//		passwordEdit = (ClearEditText) findViewById(R.id.password_edit);
//		passwordConfirmEdit = (ClearEditText) findViewById(R.id.password_confirm_edit);
//		sexEdit = (ClearEditText) findViewById(R.id.sex_edit);
//		mailEdit = (ClearEditText) findViewById(R.id.mail_edit);
//
//		nickNameEdit.addTextChangedListener(new EditWatcher());
//		passwordEdit.addTextChangedListener(new EditWatcher());
//		passwordConfirmEdit.addTextChangedListener(new EditWatcher());
//		((RelativeLayout) findViewById(R.id.main_layout))
//				.setOnTouchListener(new OnTouchListener() {
//
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						// TODO Auto-generated method stub
//						InputMethodUtils.hide(PerfectInfoActivity.this);
//						return false;
//					}
//				});
//
//		circleImageView = (CircleImageView) findViewById(R.id.photo_img);
//		isHasSdcard = Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED);
//		myDialog = new MyDialog(PerfectInfoActivity.this,
//				R.layout.dialog_modify_portrait, R.style.userhead_dialog_bg);
//		camaralText = (TextView) myDialog.findViewById(R.id.take_camera);
//		galleryText = (TextView) myDialog.findViewById(R.id.mobile_album);
//		camaralText.setOnClickListener(new UserHeadListener());
//		galleryText.setOnClickListener(new UserHeadListener());
//		apiService = new APIService(PerfectInfoActivity.this);
//		apiService.setAPIInterface(new WebService());
//	}
//
//	public void onClick(View v) {
//		Intent intent = new Intent();
//		switch (v.getId()) {
//		case R.id.photo_img:
//			myDialog.show();
//			break;
//		case R.id.back_layout:
//			finish();
//
//			myDialog.dismiss();
//			break;
//		case R.id.enter_app_btn:
//			Intent intentFill = getIntent();
//			String mobile = intentFill.getStringExtra("mobile");
//			String registerId = intentFill.getStringExtra("registerId");
//
//			String username = nickNameEdit.getText().toString();
//			String userpsd = passwordEdit.getText().toString();
//			String xx = "xx";
//			if (NetUtils.isNetworkConnected(PerfectInfoActivity.this)) {
//				apiService.uploadSMSRegInfo(mobile, username, userpsd,
//						registerId, xx);
//			}
//
//			myDialog.dismiss();
//			Intent loginIntent = new Intent();
//			loginIntent.setClass(PerfectInfoActivity.this, LoginActivity.class);
//			startActivity(loginIntent);
//			finish();
//			break;
//
//		default:
//			break;
//		}
//	}
//
//	/**
//	 * 检测输入框输入
//	 *
//	 */
//	private class EditWatcher implements TextWatcher {
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s) {
//			Boolean isNickNameBlank = StringUtils.isBlank(nickNameEdit
//					.getText().toString());
//			Boolean isPWBlank = StringUtils.isBlank(passwordEdit.getText()
//					.toString());
//			Boolean isPWConfirmBlank = StringUtils.isBlank(passwordConfirmEdit
//					.getText().toString());
//			// TODO Auto-generated method stub
//			if (isNickNameBlank || isPWBlank || isPWConfirmBlank) {
//				((Button) findViewById(R.id.enter_app_btn)).setEnabled(false);
//				((Button) findViewById(R.id.enter_app_btn))
//						.setBackgroundDrawable(getResources().getDrawable(
//								R.drawable.bg_login_btn_unable));
//			} else {
//				((Button) findViewById(R.id.enter_app_btn)).setEnabled(true);
//				((Button) findViewById(R.id.enter_app_btn))
//						.setBackgroundDrawable(getResources().getDrawable(
//								R.drawable.selector_login_btn));
//			}
//		}
//
//	}
//
//	class UserHeadListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			switch (v.getId()) {
//			case R.id.take_camera:
//				LogUtils.debug("camera", "从照相机获取");
//				goTakePhoto();
//				myDialog.dismiss();
//				break;
//			case R.id.mobile_album:
//				LogUtils.debug("gallery", "从相册获取");
//				goGallery();
//				myDialog.dismiss();
//				break;
//
//			default:
//				break;
//			}
//		}
//
//	}
//
//	/** 调用摄像头拍照 **/
//	private void goTakePhoto() {
//		// TODO Auto-generated method stub
//		Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		// 判断存储卡是否可以用，可用进行存储
//		if (isHasSdcard) {
//			intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri
//					.fromFile(new File(Environment
//							.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
//		}
//		startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
//	}
//
//	/** 调用图库选择图片 **/
//	private void goGallery() {
//		// TODO Auto-generated method stub
//		Intent intent = new Intent(Intent.ACTION_PICK, null);
//		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//				"image/*");
//		startActivityForResult(intent, IMAGE_REQUEST_CODE);
//	}
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode,
//			Intent intent) {
//		// TODO Auto-generated method stub
//		if (resultCode != RESULT_CANCELED) {
//			switch (requestCode) {
//			case IMAGE_REQUEST_CODE:
//				startPhotoZoom(intent.getData());
//				break;
//			case CAMERA_REQUEST_CODE:
//				if (isHasSdcard) {
//					File tempFile = new File(
//							Environment.getExternalStorageDirectory(),
//							IMAGE_FILE_NAME);
//					startPhotoZoom(Uri.fromFile(tempFile));
//				} else {
//					Toast.makeText(getApplicationContext(), "没有内存卡",
//							Toast.LENGTH_SHORT).show();
//				}
//
//				break;
//			case RESULT_REQUEST_CODE:
//				if (intent != null) {
//					setImageToView(intent);
//				}
//				break;
//			}
//		}
//		super.onActivityResult(requestCode, resultCode, intent);
//
//	}
//
//	/** 裁剪图片方法实现 **/
//	private void startPhotoZoom(Uri uri) {
//		if (uri == null) {
//			Log.i("tag", "The uri is not exist.");
//		}
//		Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setDataAndType(uri, "image/*");
//		// 设置裁剪
//		intent.putExtra("crop", "true");
//		// aspectX aspectY 是宽高的比例
//		intent.putExtra("aspectX", 1);
//		intent.putExtra("aspectY", 1);
//		// outputX outputY 是裁剪图片宽高
//		intent.putExtra("outputX", PHOTO_SIZE);
//		intent.putExtra("outputY", PHOTO_SIZE);
//		intent.putExtra("return-data", true);
//		startActivityForResult(intent, RESULT_REQUEST_CODE);
//	}
//
//	/** 设置头像图片 **/
//	private void setImageToView(Intent intent) {
//		// TODO Auto-generated method stub
//		Bundle extras = intent.getExtras();
//		if (extras != null) {
//
//			Bitmap photoBitmap = extras.getParcelable("data");
//			byte[] userHeadArray = getBitmapByte(photoBitmap);
//			String userHead = Base64.encodeToString(userHeadArray,
//					Base64.NO_WRAP);
//			// 上传头像部分,后面已经添加了网络判断
//			uploadUserHead(userHead);
//
//			circleImageView.setImageBitmap(photoBitmap);
//		}
//
//	}
//
//	/**
//	 * 将Bitmap转化为二进制数组
//	 * 
//	 * @param bitmap
//	 * @return
//	 */
//	private byte[] getBitmapByte(Bitmap bitmap) {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//		try {
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return out.toByteArray();
//	}
//
//	/**
//	 * 上传用户头像
//	 * 
//	 * @param userHead
//	 */
//	private void uploadUserHead(final String userHead) {
//		// TODO Auto-generated method stub\
//		if (NetUtils.isNetworkConnected(getApplicationContext())) {
//			myDialog.show();
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					String nickName = nickNameEdit.getText().toString();
//					String passWord = passwordEdit.getText().toString();
//					String sex = sexEdit.getText().toString();
//					String mail = mailEdit.getText().toString();
//					APIService apiService = new APIService(
//							getApplicationContext());
//					apiService.saveUserHead(userHead, nickName, passWord, sex,
//							mail);
//				}
//			}).start();
//		}
//	}
//	
//	class WebService extends APIInterfaceInstance{
//
//		@Override
//		public void returnUserHeadUploadSuccess(
//				GetUserHeadUploadResult getUserHeadUploadResult) {
//			// TODO Auto-generated method stub
//			super.returnUserHeadUploadSuccess(getUserHeadUploadResult);
//		}
//
//		@Override
//		public void returnUserHeadUploadFail(String error) {
//			// TODO Auto-generated method stub
//			WebServiceMiddleUtils.hand(PerfectInfoActivity.this, error);
//			super.returnUserHeadUploadFail(error);
//		}
//		
//	}
}
