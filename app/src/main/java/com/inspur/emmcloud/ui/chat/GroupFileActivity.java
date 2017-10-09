package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.bean.GroupFileInfo;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroupFileActivity extends BaseActivity {

	private ListView fileListView;
	private String cid;
	private ImageDisplayUtils imageDisplayUtils;
	private List<GroupFileInfo> fileInfoList = new ArrayList<GroupFileInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_file);
		cid = getIntent().getExtras().getString("cid");
		getFileMsgList();
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_file_unknown);
		fileListView = (ListView) findViewById(R.id.file_list);
		fileListView.setAdapter(new Adapter());
	}

	/**
	 * 获取图片消息列表
	 */
	private void getFileMsgList() {
		// TODO Auto-generated method stub
		List<Msg> fileTypeMsgList = MsgCacheUtil.getFileTypeMsgList(
				GroupFileActivity.this, cid);
		for (int i = 0; i < fileTypeMsgList.size(); i++) {
			GroupFileInfo groupFileInfo = new GroupFileInfo(
					fileTypeMsgList.get(i));
			fileInfoList.add(groupFileInfo);
		}

	}

	public void onClick(View v) {
		finish();
	}

	private class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.group_file_item_view, null);
			displayFiles(convertView,vi,position);
			return convertView;
		}

	}

	/**展示文件卡片**/
	public void displayFiles(View convertView,LayoutInflater vi,int position) {
		// TODO Auto-generated method stub
		ImageView fileImg = (ImageView) convertView
				.findViewById(R.id.file_img);
		TextView fileNameText = (TextView) convertView
				.findViewById(R.id.file_name_text);
		TextView fileSizeText = (TextView) convertView
				.findViewById(R.id.file_size_text);
		TextView fileOwnerText = (TextView) convertView
				.findViewById(R.id.file_owner_text);
		TextView fileTimeText = (TextView) convertView
				.findViewById(R.id.file_time_text);
		final HorizontalProgressBarWithNumber progressBar = (HorizontalProgressBarWithNumber) convertView
				.findViewById(R.id.file_download_progressbar);
		GroupFileInfo groupFileInfo = fileInfoList.get(position);
		final String fileName = groupFileInfo.getName();
		final String fileUrl = groupFileInfo.getUrl();
		fileNameText.setText(fileName);
		fileSizeText.setText(groupFileInfo.getSize());
		fileOwnerText.setText(groupFileInfo.getOwner());
		fileTimeText.setText(groupFileInfo.getTime(getApplicationContext()));
		if (fileName.endsWith("doc") || fileName.endsWith("docx")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_word);
		} else if (fileName.endsWith("xls") || fileName.endsWith("xlsx")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_excel);
		} else if (fileName.endsWith("ppt") || fileName.endsWith("pptx")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_ppt);
		} else if (fileName.endsWith("pdf")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_pdf);
		} else if (fileName.endsWith("txt")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_txt);
		} else if (fileName.endsWith("zip")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_zip);
		} else if (fileName.endsWith("rar")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_rar);
		} else if (fileName.contains("jpg") || fileName.contains("png")) {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_photos);
		} else {
			imageDisplayUtils.displayImage(fileImg, "drawable://"
					+ R.drawable.icon_file_unknown);
		}
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 当文件正在下载中 点击不响应
				final String fileLocalPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + fileName;
				progressBar.setTag(fileLocalPath);
				// 当文件正在下载中 点击不响应
				if ((0 < progressBar.getProgress())
						&& (progressBar.getProgress() < 100)) {
					return;
				}

				APIDownloadCallBack progressCallback = new APIDownloadCallBack(GroupFileActivity.this,fileUrl){

					@Override
					public void callbackStart() {
						if ((progressBar.getTag() != null)
								&& (progressBar.getTag() == fileLocalPath)) {

							progressBar.setVisibility(View.VISIBLE);
						} else {
							progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void callbackLoading(long total, long current, boolean isUploading) {
						if (total == 0) {
							total = 1;
						}
						int progress = (int) ((current * 100) / total);
						if (!(progressBar.getVisibility() == View.VISIBLE)) {
							progressBar.setVisibility(View.VISIBLE);
						}
						progressBar.setProgress(progress);
						progressBar.refreshDrawableState();
					}

					@Override
					public void callbackSuccess(File file) {
						progressBar.setVisibility(View.GONE);
						ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_success);
					}

					@Override
					public void callbackError(Throwable arg0, boolean arg1) {
						progressBar.setVisibility(View.GONE);
						ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_failed);
					}

					@Override
					public void callbackCanceled(CancelledException e) {

					}
				};
				if (FileUtils.isFileExist(fileLocalPath)) {
					FileUtils.openFile(getApplicationContext(), fileLocalPath);
				}else {
					new DownLoaderUtils().startDownLoad(fileUrl, fileLocalPath,
							progressCallback);
				}
			}
		});
	}
}
