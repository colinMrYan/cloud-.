package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.imp.util.compressor.Compressor;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MsgRecourceUploadUtils {
	
	
	/**
	 * 发送图片类型消息
	 *
	 * @param context
	 * @param filePath
	 * @param apiService
	 * @return
	 */
	public static Msg uploadMsgImg(Context context,String filePath,
			ChatAPIService apiService){
		int imgHeight = 0;
		int imgWidth= 0;
		Bitmap bitmapImg = BitmapFactory.decodeFile(filePath);
		imgHeight = bitmapImg.getHeight();
		imgWidth = bitmapImg.getWidth();
		bitmapImg.recycle();
		if (imgHeight>MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE || imgWidth>MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE ){
			String fileName = System.currentTimeMillis() + ".jpg";
			try {
				new Compressor(context).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_CACHE_PATH).compressToFile(new File(filePath),fileName);
                filePath = MyAppConfig.LOCAL_CACHE_PATH+fileName;
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		JSONObject jsonObject = new JSONObject();
		//暂时为要显示的消息设定一个假的id
		String fakeMessageId = System.currentTimeMillis() + "";
		String uploadFilePath = filePath;
		uploadFile(context, apiService, uploadFilePath, fakeMessageId, true);
		File uploadFile = new File(uploadFilePath);
		uploadFilePath = "file://" + uploadFilePath;
		bitmapImg = BitmapFactory.decodeFile(filePath);
		imgHeight = bitmapImg.getHeight();
		imgWidth = bitmapImg.getWidth();
		try {
			jsonObject.put("tmpId", AppUtils.getMyUUID(context));
			jsonObject.put("key", uploadFilePath);
			jsonObject.put("name",
					uploadFile.getName());
			jsonObject.put("size", uploadFile.length());
			jsonObject.put("type", "Photos");
			jsonObject.put("height", imgHeight);
			jsonObject.put("width", imgWidth);
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
			jsonObject.put("tmpId", AppUtils.getMyUUID(context));
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
