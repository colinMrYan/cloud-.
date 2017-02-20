package com.inspur.emmcloud.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.widget.LoadingDialog;

public class SendFileUtils {

	/**
	 * 发送文件
	 * @param context
	 * @param data
	 * @param apiService
	 * @param loadingDlg
	 */
	public static void sendFileMsg(Context context,Intent data,ChatAPIService apiService,LoadingDialog loadingDlg) {
		Uri uri = data.getData();
		boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		String filePath = "";
		if (isKitKat) {
			// 获取4.4及以上版本的文件路径
			filePath = GetPathFromUri4kitkat.getPath(
					context, uri);
		}else {
			//低版本兼容方法
			filePath = GetPathFromUri4kitkat.getRealPathFromURI(context, uri);
		}
		File tempFile = new File(filePath);
		String fileMsgName = "";
		fileMsgName = tempFile.getName();
		if (TextUtils.isEmpty(FileUtils.getSuffix(tempFile))) {
			ToastUtils.show(context, context.getString(R.string.not_support_upload));
			return;
		}
		if (NetUtils.isNetworkConnected(context)) {
			loadingDlg.show();
			apiService.uploadMsgResource(filePath, System.currentTimeMillis()+"",false);
		}
	}
}
