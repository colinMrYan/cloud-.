package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.GroupFileInfo;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_group_file)
public class GroupFileActivity extends BaseActivity {

    @ViewInject(R.id.lv_file)
    private ListView fileListView;

    @ViewInject(R.id.rl_no_channel_file)
    private RelativeLayout noChannelFileLayout;

    private String cid;
    private List<GroupFileInfo> fileInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        cid = getIntent().getExtras().getString("cid");
        getFileMsgList();
        noChannelFileLayout.setVisibility(fileInfoList.size() == 0 ? View.VISIBLE:View.GONE);
        fileListView.setAdapter(new Adapter());
    }

    /**
     * 获取图片消息列表
     */
    private void getFileMsgList() {
        // TODO Auto-generated method stub
        if (MyApplication.getInstance().isV0VersionChat()) {
            List<Msg> fileTypeMsgList = MsgCacheUtil.getFileTypeMsgList(
                    GroupFileActivity.this, cid);
            for (Msg msg : fileTypeMsgList) {
                GroupFileInfo groupFileInfo = new GroupFileInfo(
                        msg);
                fileInfoList.add(groupFileInfo);
            }

        } else {
            List<Message> fileTypeMessageList = MessageCacheUtil.getFileTypeMsgList(MyApplication.getInstance(), cid);
            for (Message message : fileTypeMessageList) {
                MsgContentRegularFile msgContentRegularFile = message.getMsgContentAttachmentFile();
                String url = APIUri.getChatFileResouceUrl(message.getChannel(),msgContentRegularFile.getMedia());
                GroupFileInfo groupFileInfo = new GroupFileInfo(url, msgContentRegularFile.getName(), msgContentRegularFile.getSize() + "", message.getCreationDate(), ContactUserCacheUtils.getUserName(message.getFromUser()));
                fileInfoList.add(groupFileInfo);
            }
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
            convertView = LayoutInflater.from(GroupFileActivity.this).inflate(R.layout.group_file_item_view, null);
            displayFiles(convertView, position);
            return convertView;
        }

    }

    /**
     * 展示文件卡片
     **/
    public void displayFiles(View convertView, int position) {
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
        ImageDisplayUtils.getInstance().displayImage(fileImg, "drawable://" + FileUtils.getIconResId(fileName));
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 当文件正在下载中 点击不响应
                final String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + fileName;
                progressBar.setTag(fileDownloadPath);
                // 当文件正在下载中 点击不响应
                if ((0 < progressBar.getProgress())
                        && (progressBar.getProgress() < 100)) {
                    return;
                }

                APIDownloadCallBack progressCallback = new APIDownloadCallBack(GroupFileActivity.this, fileUrl) {

                    @Override
                    public void callbackStart() {
                        if ((progressBar.getTag() != null)
                                && (progressBar.getTag() == fileDownloadPath)) {

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
                if (FileUtils.isFileExist(fileDownloadPath)) {
                    FileUtils.openFile(getApplicationContext(), fileDownloadPath);
                } else {
                    RequestParams params = MyApplication.getInstance().getHttpRequestParams(fileUrl);
                    if (MyApplication.getInstance().isV0VersionChat()){
                        params.setAutoResume(true);// 断点下载
                        params.setSaveFilePath(fileDownloadPath);
                        params.setCancelFast(true);
                    }else {
                        params.setRedirectHandler(new RedirectHandler() {
                            @Override
                            public RequestParams getRedirectParams(UriRequest uriRequest) throws Throwable {
                                String locationUrl = uriRequest.getResponseHeader("Location");
                                RequestParams params = new RequestParams(locationUrl);
                                params.setAutoResume(true);// 断点下载
                                params.setSaveFilePath(fileDownloadPath);
                                params.setCancelFast(true);
                                params.setMethod(HttpMethod.GET);
                                return params;
                            }
                        });
                    }
                    new DownLoaderUtils().startDownLoad(params,progressCallback);
                }
            }
        });
    }
}
