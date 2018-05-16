package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;

import java.io.File;

/**
 * DisplayRegularFileMsg
 *
 * @author Fortune Yu 展示文件卡片 2016-08-19
 */
public class DisplayRegularFileMsg {
    /**
     * 文件卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context,
                               final Message message,final int sendStauts) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_attachment_file_view, null);
        TextView fileNameText = (TextView) convertView
                .findViewById(R.id.file_name_text);
        TextView fileSizeText = (TextView) convertView
                .findViewById(R.id.file_size_text);
        ImageView img = (ImageView)convertView.findViewById(R.id.file_icon_img);
        final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
        ImageDisplayUtils.getInstance().displayImage(img, "drawable://" + FileUtils.getRegularFileIconResId(msgContentFile.getName()));
        fileNameText.setText(msgContentFile.getName());
        fileSizeText.setText(FileUtils.formatFileSize(msgContentFile.getSize()));
        final String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + msgContentFile.getName();
        final ProgressBar fileProgressBar = (ProgressBar) convertView
                .findViewById(R.id.pb_download);
        fileProgressBar.setTag(fileDownloadPath);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendStauts != 1){
                    return;
                }
                if (fileProgressBar.getVisibility() == View.VISIBLE) {
                    return;
                }
                final String source = APIUri.getChatFileResouceUrl(message.getChannel(),msgContentFile.getMedia());

                APIDownloadCallBack progressCallback = new APIDownloadCallBack(context, source) {
                    @Override
                    public void callbackStart() {
                        if ((fileProgressBar.getTag() != null)
                                && (fileProgressBar.getTag() == fileDownloadPath)) {
                            fileProgressBar.setVisibility(View.VISIBLE);
                        } else {
                            fileProgressBar
                                    .setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void callbackLoading(long total, long current, boolean isUploading) {
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
                    public void callbackSuccess(File file) {
                        fileProgressBar.setVisibility(View.INVISIBLE);
                        ToastUtils.show(
                                context,
                                context.getString(R.string.chat_file_download_success));
                    }

                    @Override
                    public void callbackError(Throwable arg0, boolean arg1) {
                        fileProgressBar.setVisibility(View.INVISIBLE);
                        ToastUtils.show(context, context
                                .getString(R.string.download_fail));
                    }

                    @Override
                    public void callbackCanceled(CancelledException e) {

                    }

                };
                if (FileUtils.isFileExist(fileDownloadPath)) {
                    FileUtils.openFile(context, fileDownloadPath);
                } else {
                    RequestParams params = MyApplication.getInstance().getHttpRequestParams(source);
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
                    new DownLoaderUtils().startDownLoad(params,progressCallback);

                }
            }
        });
        return convertView;
    }


}
