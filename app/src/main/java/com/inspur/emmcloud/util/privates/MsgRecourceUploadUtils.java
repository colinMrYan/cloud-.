package com.inspur.emmcloud.util.privates;

public class MsgRecourceUploadUtils {


//	/**
//	 * 发送图片类型消息
//	 *
//	 * @param context
//	 * @param filePath
//	 * @param apiService
//	 * @return
//	 */
//	public static Msg uploadResImg(Context context, String filePath,
//								   ChatAPIService apiService){
//		int imgHeight = 0;
//		int imgWidth= 0;
//		Bitmap bitmapImg = BitmapFactory.decodeFile(filePath);
//		imgHeight = bitmapImg.getHeight();
//		imgWidth = bitmapImg.getWidth();
//		bitmapImg.recycle();
//		if (imgHeight>MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE || imgWidth>MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE ){
//			String fileName = System.currentTimeMillis() + ".jpg";
//			try {
//				new Compressor(context).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_CACHE_PATH).compressToFile(new File(filePath),fileName);
//                filePath = MyAppConfig.LOCAL_CACHE_PATH+fileName;
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//		}
//		JSONObject jsonObject = new JSONObject();
//		//暂时为要显示的消息设定一个假的id
//		String fakeMessageId = System.currentTimeMillis() + "";
//		String uploadFilePath = filePath;
//		uploadFile(context, apiService, uploadFilePath, fakeMessageId, true);
//		File uploadFile = new File(uploadFilePath);
//		uploadFilePath = "file://" + uploadFilePath;
//		bitmapImg = BitmapFactory.decodeFile(filePath);
//		imgHeight = bitmapImg.getHeight();
//		imgWidth = bitmapImg.getWidth();
//		try {
//			jsonObject.put("tmpId", AppUtils.getMyUUID(context));
//			jsonObject.put("key", uploadFilePath);
//			jsonObject.put("name",
//					uploadFile.getName());
//			jsonObject.put("size", uploadFile.length());
//			jsonObject.put("type", "Photos");
//			jsonObject.put("height", imgHeight);
//			jsonObject.put("width", imgWidth);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		bitmapImg.recycle();
//		Msg sendMsg = ConbineMsg.conbineCommonMsg(context, jsonObject.toString(), "",
//				"res_image", fakeMessageId);
//		return sendMsg;
//	}
//
//
//	public static Msg uploadResFile(Context context, String filePath,
//									ChatAPIService apiService) {
//		// TODO Auto-generated method stub
//		File tempFile = new File(filePath);
//		String fileMime = FileUtils.getMimeType(tempFile);
//		String fileName = tempFile.getName();
//		if (StringUtils.isBlank(FileUtils.getSuffix(tempFile))) {
//			ToastUtils.show(context,
//					context.getString(R.string.not_support_upload));
//			return null;
//		}
//		String fakeMessageId = System.currentTimeMillis() + "";
//		uploadFile(context, apiService, filePath, fakeMessageId, false);
//
//
//		// 组织文件卡片数据
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put("key", filePath);
//			jsonObject.put("name", fileName);
//			jsonObject.put("size", tempFile.length());
//			jsonObject.put("type", fileMime);
//			jsonObject.put("tmpId", AppUtils.getMyUUID(context));
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		Msg sendMsg = ConbineMsg.conbineCommonMsg(context, jsonObject.toString(), "",
//				"res_file", fakeMessageId);
//		return sendMsg;
//
//	}
//
//	/**
//	 * 上传图片
//	 * @param context
//	 * @param apiService
//	 * @param filePath
//	 * @param fakeMessageId
//     * @param isImg
//     */
//	private static void uploadFile(Context context, ChatAPIService apiService,
//			final String filePath, String fakeMessageId, boolean isImg) {
//		if (NetUtils.isNetworkConnected(context)) {
//			apiService.uploadMsgResource(filePath, fakeMessageId, isImg);
//		}
//	}
}
