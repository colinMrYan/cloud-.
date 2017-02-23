package com.inspur.imp.plugin.camera.imagepicker.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.plugin.camera.editimage.EditImageActivity;
import com.inspur.imp.plugin.camera.editimage.utils.BitmapUtils;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.photo.PhotoNameUtils;
import com.inspur.imp.plugin.photo.UploadPhoto;
import com.inspur.imp.plugin.photo.UploadPhoto.OnUploadPhotoListener;
import com.inspur.imp.plugin.camera.imagepicker.ImageDataSource;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.adapter.ImageFolderAdapter;
import com.inspur.imp.plugin.camera.imagepicker.adapter.ImageGridAdapter;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageFolder;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.util.Utils;
import com.inspur.imp.plugin.camera.imagepicker.view.FolderPopUpWindow;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class ImageGridActivity extends ImageBaseActivity implements
		ImageDataSource.OnImagesLoadedListener,
		ImageGridAdapter.OnImageItemClickListener,
		ImagePicker.OnImageSelectedListener, View.OnClickListener {

	public static final int REQUEST_PERMISSION_STORAGE = 0x01;
	public static final int REQUEST_PERMISSION_CAMERA = 0x02;
	protected static final int CUT_IMG_SUCCESS = 1;
	
	private int parm_resolution = 1080;
	private int parm_qualtity = 100;
	private int parm_encodingType;
	private String parm_context;
	private String parm_uploadUrl;

	private ImagePicker imagePicker;

	private boolean isOrigin = false; // 是否选中原图
	private GridView mGridView; // 图片展示控件
	private View mFooterBar; // 底部栏
	private Button mBtnOk; // 确定按钮
	private Button mBtnDir; // 文件夹切换按钮
	// private Button mBtnPre; // 预览按钮
	private Button mBtnEdit;
	private ImageFolderAdapter mImageFolderAdapter; // 图片文件夹的适配器
	private FolderPopUpWindow mFolderPopupWindow; // ImageSet的PopupWindow
	private List<ImageFolder> mImageFolders; // 所有的图片文件夹
	private ImageGridAdapter mImageGridAdapter; // 图片九宫格展示的适配器
	private String paramObjJson;
	private Handler handler;
	private LoadingDialog loadingDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//
		setContentView(R.layout.activity_image_grid);
		// hideBars();
		imagePicker = ImagePicker.getInstance();
		imagePicker.clear();
		imagePicker.addOnImageSelectedListener(this);

		findViewById(R.id.btn_back).setOnClickListener(this);
		mBtnOk = (Button) findViewById(R.id.btn_ok);
		mBtnOk.setOnClickListener(this);
		mBtnDir = (Button) findViewById(R.id.btn_dir);
		mBtnDir.setOnClickListener(this);
		mBtnEdit = (Button) findViewById(R.id.btn_edit);
		mBtnEdit.setOnClickListener(this);
		mGridView = (GridView) findViewById(R.id.gridview);
		mFooterBar = findViewById(R.id.footer_bar);
		loadingDlg = new LoadingDialog(this);
		if (imagePicker.isMultiMode()) {
			mBtnOk.setVisibility(View.VISIBLE);
			mBtnEdit.setVisibility(View.VISIBLE);
		} else {
			mBtnOk.setVisibility(View.GONE);
			mBtnEdit.setVisibility(View.GONE);
		}

		mImageGridAdapter = new ImageGridAdapter(this, null);
		mImageFolderAdapter = new ImageFolderAdapter(this, null);
		handMessage();
		onImageSelected(0, null, false);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			new ImageDataSource(this, null, this);

			//
			// if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			// {
			// new ImageDataSource(this, null, this);
			// } else {
			// ActivityCompat
			// .requestPermissions(
			// this,
			// new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
			// REQUEST_PERMISSION_STORAGE);
			// }
		}
		
		if (getIntent().hasExtra("paramsObject")) {
			paramObjJson = getIntent().getExtras().getString("paramsObject");
			try {
				JSONObject paramObj = new JSONObject(paramObjJson);
				if (!paramObj.isNull("options")) {
					JSONObject obj = paramObj.getJSONObject("options");
					if (obj.has("resolution")) {
						this.parm_resolution = obj.getInt(
								"resolution");
					}
					if (obj.has("quality")) {
						this.parm_qualtity = obj.getInt(
								"quality");
					}
					if (obj.has("encodingType")) {
						this.parm_encodingType = obj.getInt(
								"encodingType");
					}
					if (obj.has("context")) {
						this.parm_context = obj.getString(
								"context");
					}
				}
				if (!paramObj.isNull("uploadUrl")) {
					this.parm_uploadUrl = paramObj.getString("uploadUrl");
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	// @Override
	// public void onRequestPermissionsResult(int requestCode, @NonNull String[]
	// permissions, @NonNull int[] grantResults) {
	// super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	// if (requestCode == REQUEST_PERMISSION_STORAGE) {
	// if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	// new ImageDataSource(this, null, this);
	// } else {
	// showToast("权限被禁止，无法选择本地图片");
	// }
	// } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
	// if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	// imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
	// } else {
	// showToast("权限被禁止，无法打开相机");
	// }
	// }
	// }


	private void hideStatusBar(boolean isHide) {
		if (isHide) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(lp);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else {
			WindowManager.LayoutParams attr = getWindow().getAttributes();
			attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attr);
			// 如果不注释下面这句话，状态栏将把界面挤下去
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	/**
	 * 隐藏navigationbar
	 */
	private void hideNavigationBar() {
		// TODO Auto-generated method stub
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and
		// higher, but as
		// a general rule, you should design your app to hide the status bar
		// whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
	}

	/**
	 * 隐藏状态栏
	 */
	private void hideStatusBar() {
		if (Build.VERSION.SDK_INT < 16) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		// TODO Auto-generated method stub
		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		ActionBar actionBar = getActionBar();
		actionBar.hide();
	}

	/**
	 * 隐藏状态栏
	 */
	public void toggleHideyBar() {

		// BEGIN_INCLUDE (get_current_ui_flags)
		// The UI options currently enabled are represented by a bitfield.
		// getSystemUiVisibility() gives us that bitfield.
		int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
		int newUiOptions = uiOptions;
		// END_INCLUDE (get_current_ui_flags)
		// BEGIN_INCLUDE (toggle_ui_flags)
		boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
		if (isImmersiveModeEnabled) {
			Log.i("123", "Turning immersive mode mode off. ");
		} else {
			Log.i("123", "Turning immersive mode mode on.");
		}

		// Navigation bar hiding: Backwards compatible to ICS.
		if (Build.VERSION.SDK_INT >= 14) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}

		// Status bar hiding: Backwards compatible to Jellybean
		if (Build.VERSION.SDK_INT >= 16) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		// Immersive mode: Backward compatible to KitKat.
		// Note that this flag doesn't do anything by itself, it only augments
		// the behavior
		// of HIDE_NAVIGATION and FLAG_FULLSCREEN. For the purposes of this
		// sample
		// all three flags are being toggled together.
		// Note that there are two immersive mode UI flags, one of which is
		// referred to as "sticky".
		// Sticky immersive mode differs in that it makes the navigation and
		// status bars
		// semi-transparent, and the UI flag does not get cleared when the user
		// interacts with
		// the screen.
		if (Build.VERSION.SDK_INT >= 18) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		// getWindow().getDecorView().setSystemUiVisibility(newUiOptions);//上边状态栏和底部状态栏滑动都可以调出状态栏
		getWindow().getDecorView().setSystemUiVisibility(4108);// 这里的4108可防止从底部滑动调出底部导航栏
		// END_INCLUDE (set_ui_flags)
	}

	@Override
	protected void onDestroy() {
		imagePicker.removeOnImageSelectedListener(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_ok) {
			if (paramObjJson != null) {
				loadingDlg.show();
				cutImg();
			}else {
				returnDataAndClose(null);
			}
			
		} else if (id == R.id.btn_dir) {
			if (mImageFolders == null) {
				Log.i("ImageGridActivity", "您的手机没有图片");
				return;
			}
			// 点击文件夹按钮
			createPopupFolderList();
			mImageFolderAdapter.refreshData(mImageFolders); // 刷新数据
			if (mFolderPopupWindow.isShowing()) {
				mFolderPopupWindow.dismiss();
			} else {
				mFolderPopupWindow.showAtLocation(mFooterBar,
						Gravity.NO_GRAVITY, 0, 0);
				// 默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
				int index = mImageFolderAdapter.getSelectIndex();
				index = index == 0 ? index : index - 1;
				mFolderPopupWindow.setSelection(index);
			}
		} else if (id == R.id.btn_edit) {
			if (paramObjJson == null) {
				EditImageActivity.start(this, imagePicker.getSelectedImages()
						.get(0).path, MyAppConfig.LOCAL_IMG_CREATE_PATH);
			}else {
				EditImageActivity.start(this, imagePicker.getSelectedImages()
						.get(0).path, MyAppConfig.LOCAL_IMG_CREATE_PATH, true, paramObjJson.toString());
			}
		} else if (id == R.id.btn_back) {
			// 点击返回按钮
			finish();
		}
	}
	
	private void uploadImg(){
		new UploadPhoto(ImageGridActivity.this, new OnUploadPhotoListener() {
			
			@Override
			public void uploadPhotoSuccess(String result) {
				// TODO Auto-generated method stub
				if (loadingDlg != null && loadingDlg.isShowing()) {
					loadingDlg.dismiss();
				}
				returnDataAndClose(result);
			}
			
			@Override
			public void uploadPhotoFail() {
				// TODO Auto-generated method stub
				if (loadingDlg != null && loadingDlg.isShowing()) {
					loadingDlg.dismiss();
				}
				Toast.makeText(getApplicationContext(), "图片上传失败！", Toast.LENGTH_SHORT).show();
			}
		}).upload(parm_uploadUrl, imagePicker.getSelectedImages(), parm_encodingType, parm_context);
	}
	
	private void returnDataAndClose(String uploadResult){
		Intent intent = new Intent();
		if (uploadResult != null) {
			intent.putExtra("uploadResult", uploadResult);
		}
		intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
				imagePicker.getSelectedImages());
		setResult(ImagePicker.RESULT_CODE_ITEMS, intent); // 多选不允许裁剪裁剪，返回数据
		finish();
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case CUT_IMG_SUCCESS:
					uploadImg();
					break;

				default:
					break;
				}
			}
			
		};
	}
	
	private void cutImg(){
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				File file = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
				if (!file.exists()) {
					file.mkdirs();
				}
				long time = System.currentTimeMillis();
				for (int i = 0; i < imagePicker.getSelectedImages().size(); i++) {
					String newPath = MyAppConfig.LOCAL_IMG_CREATE_PATH+PhotoNameUtils.getListFileName(getApplicationContext(),time,i);
					ImageItem imageItem = imagePicker.getSelectedImages().get(i);
					String path = imageItem.path;
					Bitmap bitmap = BitmapUtils.getImageCompress(path, parm_resolution, parm_qualtity);
					BitmapUtils.saveBitmap(bitmap, newPath);
					imageItem.path = newPath;
				}
				handler.sendEmptyMessage(CUT_IMG_SUCCESS);
			}
		}).start();
		
	}

	/** 创建弹出的ListView */
	private void createPopupFolderList() {
		mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
		mFolderPopupWindow
				.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int position, long l) {
						mImageFolderAdapter.setSelectIndex(position);
						imagePicker.setCurrentImageFolderPosition(position);
						mFolderPopupWindow.dismiss();
						ImageFolder imageFolder = (ImageFolder) adapterView
								.getAdapter().getItem(position);
						if (null != imageFolder) {
							mImageGridAdapter.refreshData(imageFolder.images);
							mBtnDir.setText(imageFolder.name);
						}
						mGridView.smoothScrollToPosition(0);// 滑动到顶部
					}
				});
		mFolderPopupWindow.setMargin(mFooterBar.getHeight());
	}

	@Override
	public void onImagesLoaded(List<ImageFolder> imageFolders) {
		LogUtils.jasonDebug("onImagesLoaded-----------");
		this.mImageFolders = imageFolders;
		imagePicker.setImageFolders(imageFolders);
		if (imageFolders.size() == 0)
			mImageGridAdapter.refreshData(null);
		else
			mImageGridAdapter.refreshData(imageFolders.get(0).images);
		LogUtils.jasonDebug("src==========="+imageFolders.get(0).images.get(1).path);
		mImageGridAdapter.setOnImageItemClickListener(this);
		mGridView.setAdapter(mImageGridAdapter);
		mImageFolderAdapter.refreshData(imageFolders);
	}

	@Override
	public void onImageItemClick(View view, ImageItem imageItem, int position) {
		// 根据是否有相机按钮确定位置
		position = imagePicker.isShowCamera() ? position - 1 : position;
		// imagePicker.setMultiMode(false);
		// imagePicker.setStyle(CropImageView.Style.CIRCLE);
		if (imagePicker.isMultiMode()) {
			// Intent intent = new Intent(ImageGridActivity.this,
			// ImagePreviewActivity.class);
			// intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION,
			// position);
			// intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS,
			// imagePicker.getCurrentImageFolderItems());
			// intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
			// startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
			// // 如果是多选，点击图片进入预览界面
		} else {
			imagePicker.clearSelectedImages();
			imagePicker.addSelectedImageItem(position, imagePicker
					.getCurrentImageFolderItems().get(position), true);
			if (imagePicker.isCrop()) {
				Intent intent = new Intent(ImageGridActivity.this,
						ImageCropActivity.class);
				startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP); // 单选需要裁剪，进入裁剪界面
			} else {
				if (paramObjJson == null) {
					EditImageActivity.start(this, imagePicker.getSelectedImages()
							.get(0).path, MyAppConfig.LOCAL_IMG_CREATE_PATH);
				}else {
					EditImageActivity.start(this, imagePicker.getSelectedImages()
							.get(0).path, MyAppConfig.LOCAL_IMG_CREATE_PATH, true, paramObjJson);
				}
				
			}
		}
	}

	@Override
	public void onImageSelected(int position, ImageItem item, boolean isAdd) {
		if (imagePicker.getSelectImageCount() > 0) {
			mBtnOk.setText(getString(R.string.select_complete,
					imagePicker.getSelectImageCount(),
					imagePicker.getSelectLimit()));
			mBtnOk.setEnabled(true);
		} else {
			mBtnOk.setText(getString(R.string.complete));
			mBtnOk.setEnabled(false);
		}

		if (imagePicker.getSelectImageCount() == 1 && imagePicker.isMultiMode()) {
			mBtnEdit.setVisibility(View.VISIBLE);
		} else {
			mBtnEdit.setVisibility(View.GONE);
		}
		mImageGridAdapter.notifyDataSetChanged();
	}

	@Override
	public void onImageSelectedRelace(int position, ImageItem imageItem) {
		// TODO Auto-generated method stub
		mImageGridAdapter.replaceData(position, imageItem);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			if (requestCode == ImagePicker.REQUEST_CODE_EDIT) {// 说明是从裁剪页面过来的数据，直接返回就可以
				if (resultCode == RESULT_OK) {
					String newPath = data.getStringExtra(
							"save_file_path");
					imagePicker.getSelectedImages().get(0).path = newPath;
					Intent intent = new Intent();
					intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
							imagePicker.getSelectedImages());
					intent.putExtra("uploadResult", data.getStringExtra("uploadResult"));
					setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
					finish();
				}
			} else {
				if (resultCode == ImagePicker.RESULT_CODE_BACK) {
					isOrigin = data.getBooleanExtra(
							com.inspur.imp.plugin.camera.imagepicker.ui.ImagePreviewActivity.ISORIGIN, false);
				} else {
					// 从拍照界面返回
					// 点击 X , 没有选择照片
					if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
						// 什么都不做
					} else {
						// 说明是从裁剪页面过来的数据，直接返回就可以
						setResult(ImagePicker.RESULT_CODE_ITEMS, data);
						finish();
					}
				}
			}
		} else {
			// 如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
			if (resultCode == RESULT_OK
					&& requestCode == ImagePicker.REQUEST_CODE_TAKE) {

				ImageItem imageItem = new ImageItem();
				imageItem.path = imagePicker.getTakeImageFile()
						.getAbsolutePath();
				try {
					Utils.rotateSamsungImg(imageItem.path);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				// 发送广播通知图片增加了
				ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());
				imagePicker.clearSelectedImages();
				imagePicker.addSelectedImageItem(0, imageItem, true);
				if (imagePicker.isCrop()) {
					Intent intent = new Intent(ImageGridActivity.this,
							ImageCropActivity.class);
					startActivityForResult(intent,
							ImagePicker.REQUEST_CODE_CROP); // 单选需要裁剪，进入裁剪界面
				} else {
					Intent intent = new Intent();
					intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
							imagePicker.getSelectedImages());
					setResult(ImagePicker.RESULT_CODE_ITEMS, intent); // 单选不需要裁剪，返回数据
					finish();
				}
			}
		}
	}

}