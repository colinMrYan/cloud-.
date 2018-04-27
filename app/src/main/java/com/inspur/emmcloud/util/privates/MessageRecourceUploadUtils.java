package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.oss.OssService;

import java.io.File;


public class MessageRecourceUploadUtils {
	private Context context;
	private ChatAPIService apiService;
	private String cid;
	private String filePath;
	private String tracer;
	public MessageRecourceUploadUtils(Context context,String cid){
		this.context = context;
		apiService = new ChatAPIService(context);
		apiService.setAPIInterface(new WebService());
		this.cid = cid;

	}


	public Message uploadResFile(Intent data,
										ChatAPIService apiService) {
		// TODO Auto-generated method stub
		Uri uri = data.getData();
		boolean isAboveKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (isAboveKitKat) {
			// 获取4.4及以上版本的文件路径
			filePath = GetPathFromUri4kitkat.getPath(context, uri);
		} else {
			// 低版本兼容方法
			filePath = GetPathFromUri4kitkat.getRealPathFromURI(context, uri);
		}

		File tempFile = new File(filePath);
		if (StringUtils.isBlank(FileUtils.getSuffix(tempFile))) {
			ToastUtils.show(context,
					context.getString(R.string.not_support_upload));
			return null;
		}
		String fileName = tempFile.getName();
		Message message = CommunicationUtils.combinLocalRegularFileMessage(cid,filePath);
		tracer= message.getId();
		getFileUploadToken(fileName);
		return message;

	}

	private void getFileUploadToken(String fileName){
		if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
			apiService.getFileUploadToken(fileName,cid);
		}
	}

	/**
	 * 根据不同的storage选择不同的存储服务
	 * @param getVolumeFileUploadTokenResult
	 * @param mockVolumeFile
	 * @return
	 */
	private VolumeFileUploadService getVolumeFileUploadService(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile){
		VolumeFileUploadService volumeFileUploadService = null;
		switch (getVolumeFileUploadTokenResult.getStorage()){
			case "ali_oss":  //阿里云
				volumeFileUploadService = new OssService(getVolumeFileUploadTokenResult,mockVolumeFile);
				break;
			default:
				break;
		}
		return  volumeFileUploadService;
	}

	private class WebService extends APIInterfaceInstance {

		@Override
		public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {
			VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(getVolumeFileUploadTokenResult,null);
			volumeFileUploadService.setProgressCallback(new ProgressCallback() {
				@Override
				public void onSuccess(VolumeFile volumeFile) {
					WebSocketPush.getInstance().sendFileMsg(cid,tracer,volumeFile);
				}

				@Override
				public void onLoading(int progress) {
					LogUtils.jasonDebug("progress----------------="+progress);
				}

				@Override
				public void onFail() {
					LogUtils.jasonDebug("onFail----------------");

				}
			});
			volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(),filePath);


		}

		@Override
		public void returnChatFileUploadTokenFail(String error, int errorCode) {

		}
	}
}
