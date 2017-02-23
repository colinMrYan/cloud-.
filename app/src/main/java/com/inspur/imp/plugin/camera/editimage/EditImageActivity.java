package com.inspur.imp.plugin.camera.editimage;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.plugin.camera.editimage.fragment.CropFragment;
import com.inspur.imp.plugin.camera.editimage.fragment.MainMenuFragment;
import com.inspur.imp.plugin.camera.editimage.fragment.RotateFragment;
import com.inspur.imp.plugin.camera.editimage.utils.BitmapUtils;
import com.inspur.imp.plugin.camera.editimage.view.CropImageView;
import com.inspur.imp.plugin.camera.editimage.view.CustomViewPager;
import com.inspur.imp.plugin.camera.editimage.view.LabelTextView;
import com.inspur.imp.plugin.camera.editimage.view.RotateImageView;
import com.inspur.imp.plugin.camera.editimage.view.imagezoom.ImageViewTouch;
import com.inspur.imp.plugin.camera.editimage.view.imagezoom.ImageViewTouchBase;
import com.inspur.imp.plugin.photo.PhotoNameUtils;
import com.inspur.imp.plugin.photo.UploadPhoto;
import com.inspur.imp.plugin.photo.UploadPhoto.OnUploadPhotoListener;

import org.json.JSONObject;

import java.io.File;


/**
 * 图片编辑 主页面
 *
 * @author panyi
 *         <p/>
 *         包含 1.贴图 2.滤镜 3.剪裁 4.底图旋转 5.添加艺术字 功能
 */
public class EditImageActivity extends FragmentActivity {
	public static final String FILE_PATH = "file_path";
	public static final String EXTRA_OUTPUT = "extra_output";
	public static final String EXTRA_NEED_UPLOAD = "is_need_upload";
	public static final String EXTRA_PARAM = "upload_parm";

	public static final int ACTION_REQUEST_EDITIMAGE = 224;
	public static final int MODE_NONE = 0;
	public static final int MODE_STICKERS = 1;// 贴图模式
	public static final int MODE_FILTER = 2;// 滤镜模式
	public static final int MODE_CROP = 3;// 剪裁模式
	public static final int MODE_ROTATE = 4;// 旋转模式
	public static final int MODE_TEXT = 5;// 文字模式
	protected static final int CUT_IMG_SUCCESS = 7;
	
	private int parm_resolution = 1080;
	private int parm_qualtity = 100;
	private int parm_encodingType;
	private String parm_context;
	private String parm_uploadUrl;
	
	private LoadingDialog loadingDlg;
	public boolean isNeedUpload = false;
	public String filePath;// 需要编辑图片路径
	public String saveFilePath;// 生成的新图片路径
	public String currentFilePath;
	private int imageWidth, imageHeight;// 展示图片控件 宽 高
	private LoadImageTask mLoadImageTask;

	public int mode = MODE_NONE;// 当前操作模式
	public Bitmap mainBitmap;// 底层显示Bitmap
	public ImageViewTouch mainImage;
	private View backBtn;

	public ViewFlipper bannerFlipper;
	private View applyBtn;// 应用按钮
	private View saveBtn;// 保存按钮

	// public StickerView mStickerView;// 贴图层View
	public CropImageView mCropPanel;// 剪切操作控件
	public RotateImageView mRotatePanel;// 旋转操作控件
	public LabelTextView mTextPanel;// 文本操作控件

	public CustomViewPager bottomGallery;// 底部gallery
	private BottomGalleryAdapter mBottomGalleryAdapter;// 底部gallery
	private MainMenuFragment mMainMenuFragment;// Menu
	// public StirckerFragment mStirckerFragment;// 贴图Fragment
	// public FliterListFragment mFliterListFragment;// 滤镜FliterListFragment
	private CropFragment mCropFragment;// 图片剪裁Fragment
	public RotateFragment mRotateFragment;// 图片旋转Fragment
	// public TextFragment mTextFragment;// 文字Fragment
	private Handler handle;
	/**
	 * @param srcPath
	 *            原图片路径
	 * @param targetPath
	 *            　图片修改后保存的位置
	 */
	public static void start(Activity activity, String srcPath,
			String targetPath) {
		Intent intent = new Intent(activity, EditImageActivity.class);
		intent.putExtra(EditImageActivity.FILE_PATH, srcPath);
		LogUtils.jasonDebug("srcPath="+srcPath);
		intent.putExtra(EditImageActivity.EXTRA_OUTPUT, targetPath);
		activity.startActivityForResult(intent, ACTION_REQUEST_EDITIMAGE);
	}
	
	/**
	 * @param srcPath
	 *            原图片路径
	 * @param targetPath
	 *            　图片修改后保存的位置
	 */
	public static void start(Activity activity, String srcPath,
			String targetPath,boolean isNeedUpload,String paramsObject) {
		Intent intent = new Intent(activity, EditImageActivity.class);
		intent.putExtra(EditImageActivity.FILE_PATH, srcPath);
		intent.putExtra(EditImageActivity.EXTRA_OUTPUT, targetPath);
		intent.putExtra(EditImageActivity.EXTRA_NEED_UPLOAD, isNeedUpload);
		intent.putExtra(EditImageActivity.EXTRA_PARAM, paramsObject);
		activity.startActivityForResult(intent, ACTION_REQUEST_EDITIMAGE);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_edit);
		initView();
		getData();
	}

	private void getData() {
		filePath = getIntent().getStringExtra(FILE_PATH);
		currentFilePath = filePath;
		
		saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);// 保存图片路径
		if (getIntent().hasExtra(EXTRA_NEED_UPLOAD)) {
			isNeedUpload = getIntent().getBooleanExtra(EXTRA_NEED_UPLOAD, false);
			String json = getIntent().getStringExtra(EXTRA_PARAM);
			LogUtils.jasonDebug("json="+json);
			try {
				JSONObject jsonObject = new JSONObject(json);
				if (!jsonObject.isNull("options")) {
					JSONObject obj = jsonObject.getJSONObject("options");
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
				if (!jsonObject.isNull("uploadUrl")) {
					this.parm_uploadUrl = jsonObject.getString("uploadUrl");
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		File file = new File(saveFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		saveFilePath = saveFilePath+PhotoNameUtils.getFileName(getApplicationContext());
		loadImage(filePath);
	}

	private void initView() {
		loadingDlg = new LoadingDialog(this);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		imageWidth = (int) ((float) metrics.widthPixels / 1.5);
		imageHeight = (int) ((float) metrics.heightPixels / 1.5);

		bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
		bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
		bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
		applyBtn = findViewById(R.id.apply);
		applyBtn.setOnClickListener(new ApplyBtnClick());
		saveBtn = findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(new SaveBtnClick());

		mainImage = (ImageViewTouch) findViewById(R.id.main_image);
		backBtn = findViewById(R.id.back_btn);// 退出按钮
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				forceReturnBack();
			}
		});

		// mStickerView = (StickerView) findViewById(R.id.sticker_panel);
		mCropPanel = (CropImageView) findViewById(R.id.crop_panel);
		mRotatePanel = (RotateImageView) findViewById(R.id.rotate_panel);
		// mTextPanel = (LabelTextView) findViewById(R.id.text_panel);

		// 底部gallery
		bottomGallery = (CustomViewPager) findViewById(R.id.bottom_gallery);
		bottomGallery.setOffscreenPageLimit(2);
		mMainMenuFragment = MainMenuFragment.newInstance(this);
		mBottomGalleryAdapter = new BottomGalleryAdapter(
				this.getSupportFragmentManager());
		// mStirckerFragment = StirckerFragment.newInstance(this);
		// mFliterListFragment = FliterListFragment.newInstance(this);
		mCropFragment = CropFragment.newInstance(this);
		mRotateFragment = RotateFragment.newInstance(this);
		// mTextFragment = TextFragment.newInstance(this);

		bottomGallery.setAdapter(mBottomGalleryAdapter);
		handMessage();
	}

	/**
	 * @author panyi
	 */
	private final class BottomGalleryAdapter extends FragmentPagerAdapter {
		public BottomGalleryAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			LogUtils.jasonDebug("index=" + index);
			// System.out.println("createFragment-->"+index);
			if (index == 0)
				return mMainMenuFragment;// 主菜单
				// if (index == 1)
				// return mStirckerFragment;// 贴图
				// if (index == 2)
				// return mFliterListFragment;// 滤镜
			if (index == 1)
				return mCropFragment;// 剪裁
			if (index == 2)
				return mRotateFragment;// 旋转
				// if (index == 5)
				// return mTextFragment;//文本
			return MainMenuFragment.newInstance(EditImageActivity.this);
		}

		@Override
		public int getCount() {
			return 3;
		}
	}// end inner class

	/**
	 * 异步载入编辑图片
	 *
	 * @param filepath
	 */
	public void loadImage(String filepath) {
		if (mLoadImageTask != null) {
			mLoadImageTask.cancel(true);
		}
		mLoadImageTask = new LoadImageTask();
		mLoadImageTask.execute(filepath);
	}

	private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapUtils.loadImageByPath(params[0], imageWidth,
						imageHeight,parm_resolution,parm_qualtity);
			}catch (Exception e){
				e.printStackTrace();
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (mainBitmap != null) {
				mainBitmap.recycle();
				mainBitmap = null;
				System.gc();
			}
			if (result == null){
				ToastUtils.show(getApplicationContext(),getResources().getString(R.string.img_decode_fail));
				finish();
			}else {
				mainBitmap = result;
				mainImage.setImageBitmap(result);
				mainImage
						.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
			}

		}
	}// end inner class

	/**
	 * 按下返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (mode) {
			case MODE_STICKERS:
				// mStirckerFragment.backToMain();
				return true;
			case MODE_FILTER:// 滤镜编辑状态
				// mFliterListFragment.backToMain();// 保存滤镜贴图
				return true;
			case MODE_CROP:// 剪切图片保存
				mCropFragment.backToMain();
				return true;
			case MODE_ROTATE:// 旋转图片保存
				mRotateFragment.backToMain();
				return true;
			case MODE_TEXT:// 旋转文字保存
				// mTextFragment.backToMain();
				return true;
			}// end switch

			forceReturnBack();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 强制推出
	 */
	private void forceReturnBack() {
		setResult(RESULT_CANCELED);
		this.finish();
	}

	/**
	 * 保存按钮点击
	 *
	 * @author panyi
	 */
	private final class ApplyBtnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (mode) {
			case MODE_STICKERS:
				// mStirckerFragment.saveStickers();// 保存贴图
				break;
			case MODE_FILTER:// 滤镜编辑状态
				// mFliterListFragment.saveFilterImage();// 保存滤镜贴图
				break;
			case MODE_CROP:// 剪切图片保存
				mCropFragment.saveCropImage();
				break;
			case MODE_ROTATE:// 旋转图片保存
				mRotateFragment.saveRotateImage();
				break;
			case MODE_TEXT:// 文字保存
				// mTextFragment.saveTextSticker();
				break;
			default:
				break;
			}// end switch
		}
	}// end inner class

	/**
	 * 保存按钮 点击退出
	 *
	 * @author panyi
	 */
	private final class SaveBtnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (!isNeedUpload) {
				returnDataAndClose(null);
			}else {
				loadingDlg.show();
				cutImg();
			}
		}

	}// end inner class
	
	private void handMessage() {
		// TODO Auto-generated method stub
		handle = new Handler(){

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

	
	/**
	 * 进行图片裁剪
	 */
	private void cutImg() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!saveFilePath.equals(currentFilePath)) {
					BitmapUtils.saveBitmap(mainBitmap, saveFilePath);
					currentFilePath = saveFilePath;
				}
				handle.sendEmptyMessage(CUT_IMG_SUCCESS);
			}
		}).start();
		
	}
	
	/**
	 * 图片进行上传
	 */
	private void uploadImg(){
		new UploadPhoto(EditImageActivity.this,new OnUploadPhotoListener() {
			
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
				Toast.makeText(getApplicationContext(), R.string.img_upload_fail, Toast.LENGTH_SHORT).show();
			}
		}).upload(parm_uploadUrl, saveFilePath, parm_encodingType, parm_context);
	}
	
	private void returnDataAndClose(String uploadResult){
		Intent returnIntent = new Intent();
		if (uploadResult != null) {
			returnIntent.putExtra("uploadResult", uploadResult);
		}
		returnIntent.putExtra("save_file_path", currentFilePath);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	/**
	 * 切换底图Bitmap
	 *
	 * @param newBit
	 */
	public void changeMainBitmap(Bitmap newBit) {
		if (mainBitmap != null) {
			if (!mainBitmap.isRecycled()) {// 回收
				mainBitmap.recycle();
			}
			mainBitmap = newBit;
		} else {
			mainBitmap = newBit;
		}// end if
		mainImage.setImageBitmap(mainBitmap);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadImageTask != null) {
			mLoadImageTask.cancel(true);
		}
	}

	public static Dialog getLoadingDialog(Context context, String title,
			boolean canCancel) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setCancelable(canCancel);
		dialog.setMessage(title);
		return dialog;
	}

}// end class
