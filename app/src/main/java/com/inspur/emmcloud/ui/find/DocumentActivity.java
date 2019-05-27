package com.inspur.emmcloud.ui.find;

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
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档页面 com.inspur.emmcloud.ui.DocumentActivity create at 2016年9月5日 上午10:03:42
 */
public class DocumentActivity extends BaseActivity {

    private ListView fileListView;
    private List<DocumentInfo> documentInfoList = new ArrayList<DocumentInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ((TextView) findViewById(R.id.header_text))
                .setText(getString(R.string.docunment));
        setDocumentList();
        fileListView = (ListView) findViewById(R.id.lv_file);
        fileListView.setAdapter(new Adapter());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_file;
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
                .setUri(WebServiceRouterManager.getInstance().getClusterEcm() + "res_dev/stream/IUYIOKUUJE8.docx");

        DocumentInfo documentInfo2 = new DocumentInfo();
        documentInfo2.setFileName("安卓版手机收发加密邮件配置文档V2.0.docx");
        documentInfo2.setSize("968.0K");
        documentInfo2
                .setUri(WebServiceRouterManager.getInstance().getClusterEcm() + "res_dev/stream/M7BIOKVCHWI.docx");

        DocumentInfo documentInfo3 = new DocumentInfo();
        documentInfo3.setFileName("苹果版手机收发加密邮件配置文档V2.0.docx");
        documentInfo3.setSize("4.4M");
        documentInfo3
                .setUri(WebServiceRouterManager.getInstance().getClusterEcm() + "res_dev/stream/157IOKVD2FD.docx");

        DocumentInfo documentInfo4 = new DocumentInfo();
        documentInfo4.setFileName("浪潮集团数字证书备份及导入手册V2.0.doc");
        documentInfo4.setSize("4.4M");
        documentInfo4
                .setUri(WebServiceRouterManager.getInstance().getClusterEcm() + "res_dev/stream/V1JIOL3LX11.doc");

        DocumentInfo documentInfo5 = new DocumentInfo();
        documentInfo5.setFileName("集团数字证书申请说明V4.0.doc");
        documentInfo5.setSize("4.4M");
        documentInfo5
                .setUri(WebServiceRouterManager.getInstance().getClusterEcm() + "res_dev/stream/FYAIOL3LNYD.doc");
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
                    .findViewById(R.id.tv_file_name);
            final HorizontalProgressBarWithNumber progressBar = (HorizontalProgressBarWithNumber) convertView
                    .findViewById(R.id.file_download_progressbar);
            DocumentInfo documentInfo = documentInfoList.get(position);
            final String fileName = documentInfo.getFileName();
            final String source = documentInfo.getUri();
            fileNameText.setText(fileName);
            ImageDisplayUtils.getInstance().displayImage(fileImg, "drawable://" + FileUtils.getIconResId(fileName));
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

                    APIDownloadCallBack progressCallback = new APIDownloadCallBack(DocumentActivity.this, source) {

                        @Override
                        public void callbackStart() {
                            if ((progressBar.getTag() != null)
                                    && (progressBar.getTag() == target)) {
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

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

}
