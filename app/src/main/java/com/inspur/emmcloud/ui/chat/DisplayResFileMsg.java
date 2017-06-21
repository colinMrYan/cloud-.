package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;
import com.inspur.emmcloud.widget.RoundAngleImageView;

import org.xutils.common.Callback.ProgressCallback;

import java.io.File;

/**
 * DisplayResFileMsg
 * 
 * @author Fortune Yu 展示文件卡片 2016-08-19
 */
public class DisplayResFileMsg {
	/**
	 * 文件卡片
	 * 
	 * @param context
	 * @param convertView
	 * @param msg
	 */
	public static void displayResFileMsg(final Context context,
			View convertView, final Msg msg) {
		TextView fileTitleText = (TextView) convertView
				.findViewById(R.id.file_name_text);
		TextView fileSizeText = (TextView) convertView
				.findViewById(R.id.file_size_text);
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication) context.getApplicationContext()).getUid());
//		((LinearLayout) convertView.findViewById(R.id.root_layout))
//				.setBackgroundColor(context.getResources().getColor(
//				isMyMsg ? R.color.header_bg : R.color.white));
//		((View) convertView.findViewById(R.id.line_view))
//				.setBackgroundColor(context.getResources().getColor(
//						isMyMsg ? R.color.white : R.color.content_bg));
//		fileTitleText.setTextColor(context.getResources().getColor(
//				isMyMsg ? R.color.white : R.color.text_grey));
//		fileSizeText.setTextColor(context.getResources().getColor(
//				isMyMsg ? R.color.white : R.color.text_grey));
		if (context instanceof ChannelActivity) {
			int arrowPadding = DensityUtil.dip2px(context, 7);
			if (isMyMsg) {
				((LinearLayout) convertView.findViewById(R.id.root_layout)).setPadding(0, 0, arrowPadding, 0);
			} else {
				((LinearLayout) convertView.findViewById(R.id.root_layout)).setPadding(arrowPadding, 0,
						0, 0);
			}
		}
		
		
		final ImageView fileDownLoadImg = (ImageView) convertView
				.findViewById(R.id.filecard_download_img);
		String msgBody = msg.getBody();
		String fileSize = JSONUtils.getString(msgBody, "size", "");
		String fileName = JSONUtils.getString(msgBody, "name", "");
		final String downloadUri = JSONUtils.getString(msgBody, "key", "");
		RoundAngleImageView roundAngleImageView = (RoundAngleImageView) convertView
				.findViewById(R.id.file_type_img);
		ShowFileIconUtils.showFileIcon(context,downloadUri,
				roundAngleImageView);
		fileTitleText.setText(fileName);
		fileSizeText.setText(FileUtils.formatFileSize(fileSize));
		File dir = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}
		final String target = MyAppConfig.LOCAL_DOWNLOAD_PATH + fileName;

		if (FileUtils.isFileExist(target) || FileUtils.isFileExist(downloadUri)) {
			fileDownLoadImg.setVisibility(View.GONE);
		} else {
			fileDownLoadImg.setVisibility(View.VISIBLE);
		}
		final HorizontalProgressBarWithNumber fileProgressBar = (HorizontalProgressBarWithNumber) convertView
				.findViewById(R.id.file_download_progressbar);
		fileProgressBar.setTag(target);
		fileProgressBar.setVisibility(View.VISIBLE);

		((RelativeLayout) convertView.findViewById(R.id.header_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if ((0 < fileProgressBar.getProgress())
								&& (fileProgressBar.getProgress() < 100)) {
							return;
						}

						ProgressCallback<File> progressCallback = new ProgressCallback<File>() {

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
								fileProgressBar.setVisibility(View.GONE);
								ToastUtils.show(context, context
										.getString(R.string.download_fail));
							}

							@Override
							public void onFinished() {
								// TODO Auto-generated method stub

							}

							@Override
							public void onSuccess(File arg0) {
								// TODO Auto-generated method stub
								fileProgressBar.setVisibility(View.INVISIBLE);
								fileDownLoadImg.setVisibility(View.GONE);
								ToastUtils.show(
										context,
										context.getString(R.string.chat_file_download_success));
							}

							@Override
							public void onLoading(long total, long current,
									boolean arg2) {
								// TODO Auto-generated method stub
								if (total == 0) {
									total = 1;
								}
								int progress = (int) ((current * 100) / total);
								if (!(fileProgressBar.getVisibility() == View.INVISIBLE)) {
									fileProgressBar.setVisibility(View.VISIBLE);
								}
								fileProgressBar.setProgress(progress);
								fileProgressBar.refreshDrawableState();
							}

							@Override
							public void onStarted() {
								// TODO Auto-generated method stub
								if ((fileProgressBar.getTag() != null)
										&& (fileProgressBar.getTag() == target)) {
									fileProgressBar.setVisibility(View.VISIBLE);
								} else {
									fileProgressBar
											.setVisibility(View.INVISIBLE);
								}
							}

							@Override
							public void onWaiting() {
								// TODO Auto-generated method stub

							}
						};
						showOrDownLoadFile(context, downloadUri, target,
								fileDownLoadImg, progressCallback);
					}
				});
	}

	/**
	 * 展示或下载文件
	 * @param context
	 * @param downloadUri
	 * @param target
	 * @param fileDownLoadImg
	 * @param fileProgressBar
     */
	protected static void showOrDownLoadFile(Context context,
			String downloadUri, String target, ImageView fileDownLoadImg,
			ProgressCallback<File> fileProgressBar) {
		DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
		if (FileUtils.isFileExist(target)) {
			FileUtils.openFile(context, target);
		} else if (FileUtils.isFileExist(downloadUri)) {
			fileDownLoadImg.setVisibility(View.GONE);
			FileUtils.openFile(context, downloadUri);
		} else {
			String completeDownloadUrl = UriUtils.getPreviewUri(downloadUri);
			downLoaderUtils.startDownLoad(completeDownloadUrl, target,
					fileProgressBar);
		}

	}

}
