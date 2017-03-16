package com.inspur.imp.plugin.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.camera.Bimp;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.camera.editimage.EditImageActivity;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class PhotoService extends ImpPlugin {

	private static final int RESULT_CAMERA = 1;
	private static final int RESULT_GELLERY = 2;
	private String successCb, failCb;
	private JSONObject paramsObject;
	private String takePhotoImgPath = "";

	@Override
	public void execute(String action, JSONObject paramsObject) {
		// TODO Auto-generated method stub
		this.paramsObject = paramsObject;
		if ("selectAndUpload".equals(action)) {
			selectAndUpload();
		}
		if ("takePhotoAndUpload".equals(action)) {
			takePhotoAndUpload();
		}
	}

	private void selectAndUpload() {
		// TODO Auto-generated method stub
		try {
			if (!paramsObject.isNull("success"))
				successCb = paramsObject.getString("success");
			if (!paramsObject.isNull("fail"))
				failCb = paramsObject.getString("fail");
			openGallery();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	private void openGallery() {
		// TODO Auto-generated method stub
		PublicWay.uploadPhotoService = this;
		initImagePicker();
		Intent intent = new Intent(context, ImageGridActivity.class);
		intent.putExtra("paramsObject", paramsObject.toString());
		((Activity) context).startActivityForResult(intent, RESULT_GELLERY);
	}

	/**
	 * 初始化图片选择控件
	 */
	private void initImagePicker() {
		ImagePicker imagePicker = ImagePicker.getInstance();
		imagePicker.setImageLoader(new ImageDisplayUtils()); // 设置图片加载器
		imagePicker.setShowCamera(false); // 显示拍照按钮
		imagePicker.setCrop(false); // 允许裁剪（单选才有效）
		imagePicker.setSelectLimit(6);
		imagePicker.setMultiMode(true);
	}

	private void takePhotoAndUpload() {
		// TODO Auto-generated method stub
		try {
			if (!paramsObject.isNull("success"))
				successCb = paramsObject.getString("success");
			if (!paramsObject.isNull("fail"))
				failCb = paramsObject.getString("fail");
			openCamera();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void openCamera() {
		// TODO Auto-generated method stub
		Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 判断存储卡是否可以用，可用进行存储
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			PublicWay.uploadPhotoService = this;
			File appDir = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
			if (!appDir.exists()) {
				appDir.mkdir();
			}
			// 指定文件名字
			String fileName = PhotoNameUtils.getFileName(getActivity());
			takePhotoImgPath = MyAppConfig.LOCAL_IMG_CREATE_PATH + fileName;
			intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(appDir, fileName)));
			((Activity) context).startActivityForResult(intentFromCapture,
					RESULT_CAMERA);
		} else {
			Toast.makeText(context,
					Res.getStringID("filetransfer_sd_not_exist"),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		PublicWay.uploadPhotoService = null;
		if (requestCode == RESULT_CAMERA) {
			if (resultCode == getActivity().RESULT_OK) {
				PublicWay.uploadPhotoService = this;
				EditImageActivity.start(getActivity(), takePhotoImgPath,
						MyAppConfig.LOCAL_IMG_CREATE_PATH, true,
						paramsObject.toString());
			} else {
				this.failPicture(Res.getString("cancel_camera"));
			}
		} else if (requestCode == EditImageActivity.ACTION_REQUEST_EDITIMAGE) {
			if (resultCode == getActivity().RESULT_OK) {
				try {
					JSONObject jsonObject = new JSONObject();
					String uploadResult = intent.getStringExtra("uploadResult");
					String imagePath = intent.getStringExtra("save_file_path");
					File file = new File(imagePath);
					JSONObject contextObj = new JSONObject(uploadResult);
					jsonObject.put("context", contextObj);

					Bitmap bitmap = Bimp.revitionImageSize(imagePath);
					String bitmapBase64 = Bimp.bitmapToBase64(bitmap);
					jsonObject.put("thumbnailData", bitmapBase64);
					String returnData = jsonObject.toString();
					// LogUtils.jasonDebug("returnData="+returnData);
					this.jsCallback(successCb, returnData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				this.failPicture(Res.getString("cancel_camera"));
			}
			clearImgCache();
		} else if (requestCode == RESULT_GELLERY) {
			if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
				try {
					JSONObject jsonObject = new JSONObject();
					String uploadResult = intent.getStringExtra("uploadResult");
					LogUtils.jasonDebug("uploadResult======="+uploadResult);
					JSONObject contextObj = new JSONObject(uploadResult);
					jsonObject.put("context", contextObj);
					JSONArray dataArray = new JSONArray();
					ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) intent
							.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
					for (int i = 0; i < selectedList.size(); i++) {
						String imagePath = selectedList.get(i).path;
						File file = new File(imagePath);
						JSONObject obj = new JSONObject();
						Bitmap bitmap = Bimp.revitionImageSize(imagePath);
						String bitmapBase64 = Bimp.bitmapToBase64(bitmap);
						obj.put("data",bitmapBase64);
						obj.put("name", file.getName());
						dataArray.put(obj);
					}
					jsonObject.put("thumbnailData", dataArray);
					String returnData = jsonObject.toString();
					 LogUtils.jasonDebug("returnData="+returnData);
					this.jsCallback(successCb, returnData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				this.failPicture(Res.getString("cancel_select"));
			}
			clearImgCache();
		}
	}
	
	/**
	 * 清除生成的图片cache
	 */
	private void clearImgCache(){
	//	DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
	}

	/**
	 * Send error message to JavaScript.
	 * 
	 * @param err
	 */
	public void failPicture(String err) {
		this.jsCallback(failCb, err);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

}
