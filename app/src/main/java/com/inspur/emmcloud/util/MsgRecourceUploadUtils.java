package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.util.imgcompress.Compressor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MsgRecourceUploadUtils {
	
	
	/**
	 * 发送图片类型消息
	 *
	 * @param context
	 * @param data
	 * @param apiService
	 * @return
	 */
	public static Msg uploadMsgImg(Context context, Intent data,
			ChatAPIService apiService){
		String filePath = "";
		if (data.hasExtra("save_file_path")) {
			filePath = data.getStringExtra("save_file_path");
			String fileName = System.currentTimeMillis() + ".jpg";
			new Compressor.Builder(context).setMaxWidth(1200).setMaxHeight(1200).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_CACHE_PATH).setFileName(fileName).build().compressToFile(new File(filePath));
			filePath = MyAppConfig.LOCAL_CACHE_PATH+fileName;
		}else {
			ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
					.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
			filePath = imageItemList.get(0).path;
		}
		//暂时为要显示的消息设定一个假的id
		String fakeMessageId = System.currentTimeMillis() + "";
		String uploadFilePath = filePath;
//		try {
//			String thumbDirPath = MyAppConfig.LOCAL_CACHE_PATH;
//			FileUtils.makeDirs(thumbDirPath);
//			String thumbFilePath = thumbDirPath + new Date().getTime() + ".jpg";
//			ImageUtils.createImageThumbnail(context,
//					filePath, thumbFilePath, 1200, 80);
//			uploadFilePath = thumbFilePath;
//		} catch (IOException e) {
//			e.printStackTrace();
//
//		}
		uploadFile(context, apiService, uploadFilePath, fakeMessageId, true);
		File uploadFile = new File(uploadFilePath);
		uploadFilePath = "file://" + uploadFilePath;
		Bitmap bitmapImg = BitmapFactory.decodeFile(filePath);
//		bitmapImg.getHeight();
//		bitmapImg.getWidth();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("key", uploadFilePath);
			jsonObject.put("name",
					uploadFile.getName());
			jsonObject.put("size", uploadFile.length());
			jsonObject.put("type", "Photos");
			jsonObject.put("height", bitmapImg.getHeight());
			jsonObject.put("width", bitmapImg.getWidth());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		bitmapImg.recycle();
		Msg sendMsg = ConbineMsg.conbineMsg(context, jsonObject.toString(), "",
				"res_image", fakeMessageId);
		return sendMsg;
	}

	
	public static Msg uploadImgFile(Context context, Intent data,
			ChatAPIService apiService) {
		// TODO Auto-generated method stub
		Uri uri = data.getData();
		boolean isAboveKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		String filePath = null;
		if (isAboveKitKat) {
			// 获取4.4及以上版本的文件路径
			filePath = GetPathFromUri4kitkat.getPath(context, uri);
		} else {
			// 低版本兼容方法
			filePath = GetPathFromUri4kitkat.getRealPathFromURI(context, uri);
		}

		File tempFile = new File(filePath);
		String fileMime = FileUtils.getMimeType(tempFile);
		String fileName = tempFile.getName();
		if (StringUtils.isBlank(FileUtils.getSuffix(tempFile))) {
			ToastUtils.show(context,
					context.getString(R.string.not_support_upload));
			return null;
		}
		String fakeMessageId = System.currentTimeMillis() + "";
		uploadFile(context, apiService, filePath, fakeMessageId, false);
		
		
		// 组织文件卡片数据
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("key", filePath);
			jsonObject.put("name", fileName);
			jsonObject.put("size", tempFile.length());
			jsonObject.put("type", fileMime);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		Msg sendMsg = ConbineMsg.conbineMsg(context, jsonObject.toString(), "",
				"res_file", fakeMessageId);
		return sendMsg;

	}

	/**
	 * 上传图片
	 * @param context
	 * @param apiService
	 * @param filePath
	 * @param fakeMessageId
     * @param isImg
     */
	private static void uploadFile(Context context, ChatAPIService apiService,
			final String filePath, String fakeMessageId, boolean isImg) {
		if (NetUtils.isNetworkConnected(context)) {
			apiService.uploadMsgResource(filePath, fakeMessageId, isImg);
		}
	}
}
