package com.inspur.emmcloud.util;

import java.io.File;

import org.xutils.x;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.ProgressCallback;
import org.xutils.http.RequestParams;

/**
 * 下载文件模块 需要传入
 *
 */
public class DownLoaderUtils {

	Cancelable cancelable;


	/**
	 * 开始下载方法
	 * 
	 * @param source
	 * @param target
	 * @param callback
	 */
	public void startDownLoad(String source, String target,
			 ProgressCallback<File> progressCallback) {

		RequestParams params = new RequestParams(source);
		params.setAutoResume(true);// 断点下载
		params.setSaveFilePath(target);
		params.setCancelFast(true);
		cancelable = x.http().get(params, progressCallback);
	}

	public void pauseDownLoad() {
		cancelable.cancel();
	}

	public void resumeDownLoad(String source, String target,
			ProgressCallback<File> progressCallback) {
		startDownLoad(source, target, progressCallback);
	}

}
