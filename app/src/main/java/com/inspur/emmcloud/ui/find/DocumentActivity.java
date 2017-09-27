package com.inspur.emmcloud.ui.find;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xutils.common.Callback.ProgressCallback;

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
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;

/**
 * 文档页面 com.inspur.emmcloud.ui.DocumentActivity create at 2016年9月5日 上午10:03:42
 */
public class DocumentActivity extends BaseActivity {

	private ListView fileListView;
	private String cid;
	private ImageDisplayUtils imageDisplayUtils;
	private List<DocumentInfo> documentInfoList = new ArrayList<DocumentInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_file);
		((TextView) findViewById(R.id.header_text))
				.setText(getString(R.string.docunment));
		setDocumentList();
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_file_unknown);
		fileListView = (ListView) findViewById(R.id.file_list);
		fileListView.setAdapter(new Adapter());
	}

	/**
	 * 获取图片消息列表
	 */
	private void setDocumentList() {
		// TODO Auto-generated method stub
		DocumentInfo documentInfo1 = new DocumentInfo();
		documentInfo1.setFileName("无需交换签名实现邮件加密的方法V2.0.docx");
		documentInfo1.setSize("1.4M");
		documentInfo1
				.setUri("https://ecm.inspur.com/res_dev/stream/IUYIOKUUJE8.docx");

		DocumentInfo documentInfo2 = new DocumentInfo();
		documentInfo2.setFileName("安卓版手机收发加密邮件配置文档V2.0.docx");
		documentInfo2.setSize("968.0K");
		documentInfo2
				.setUri("https://ecm.inspur.com/res_dev/stream/M7BIOKVCHWI.docx");

		DocumentInfo documentInfo3 = new DocumentInfo();
		documentInfo3.setFileName("苹果版手机收发加密邮件配置文档V2.0.docx");
		documentInfo3.setSize("4.4M");
		documentInfo3
				.setUri("https://ecm.inspur.com/res_dev/stream/157IOKVD2FD.docx");

		DocumentInfo documentInfo4 = new DocumentInfo();
		documentInfo4.setFileName("浪潮集团数字证书备份及导入手册V2.0.doc");
		documentInfo4.setSize("4.4M");
		documentInfo4
				.setUri("https://ecm.inspur.com/res_dev/stream/V1JIOL3LX11.doc");

		DocumentInfo documentInfo5 = new DocumentInfo();
		documentInfo5.setFileName("集团数字证书申请说明V4.0.doc");
		documentInfo5.setSize("4.4M");
		documentInfo5
				.setUri("https://ecm.inspur.com/res_dev/stream/FYAIOL3LNYD.doc");
		documentInfoList.add(documentInfo1);
		documentInfoList.add(documentInfo2);
		documentInfoList.add(documentInfo3);
		documentInfoList.add(documentInfo4);
		documentInfoList.add(documentInfo5);

	}

	public void onClick(View v) {
		finish();
	}

	private class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return documentInfoList.size();
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
			convertView = vi.inflate(R.layout.document_file_item_view, null);
			ImageView fileImg = (ImageView) convertView
					.findViewById(R.id.file_img);
			TextView fileNameText = (TextView) convertView
					.findViewById(R.id.file_name_text);
			final HorizontalProgressBarWithNumber progressBar = (HorizontalProgressBarWithNumber) convertView
					.findViewById(R.id.file_download_progressbar);
			DocumentInfo documentInfo = documentInfoList.get(position);
			final String fileName = documentInfo.getFileName();
			final String source = documentInfo.getUri();
			fileNameText.setText(fileName);

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
					final String target = MyAppConfig.LOCAL_DOWNLOAD_PATH
							+ fileName;

					progressBar.setTag(target);

					// 当文件正在下载中 点击不响应
					if ((0 < progressBar.getProgress())
							&& (progressBar.getProgress() < 100)) {
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
							progressBar.setVisibility(View.GONE);
							ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_failed);
						}

						@Override
						public void onFinished() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess(File arg0) {
							// TODO Auto-generated method stub
							progressBar.setVisibility(View.GONE);
							ToastUtils.show(getApplicationContext(), R.string.filetransfer_download_success);
						}

						@Override
						public void onLoading(long total, long current,
								boolean isUploading) {
							// TODO Auto-generated method stub
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
						public void onStarted() {
							// TODO Auto-generated method stub
							if ((progressBar.getTag() != null)
									&& (progressBar.getTag() == target)) {

								progressBar.setVisibility(View.VISIBLE);
							} else {
								progressBar.setVisibility(View.GONE);
							}
						}

						@Override
						public void onWaiting() {
							// TODO Auto-generated method stub
							
						}
						
					};
					
					if (FileUtils.isFileExist(target)) {
						FileUtils.openFile(DocumentActivity.this, target);
					} else {
						new DownLoaderUtils().startDownLoad(source, target, progressCallback);
					}

				}
			});

			return convertView;
		}

	}


	private class DocumentInfo {
		private String fileName;
		private String size;
		private String uri;

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getFileName() {
			return fileName;
		}

		public String getSize() {
			return size;
		}

		public String getUri() {
			return uri;
		}
	}

}
