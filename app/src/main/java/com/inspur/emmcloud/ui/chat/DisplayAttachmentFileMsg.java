package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentFile;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import java.io.File;

/**
 * DisplayAttachmentFileMsg
 *
 * @author Fortune Yu 展示文件卡片 2016-08-19
 */
public class DisplayAttachmentFileMsg {
    /**
     * 文件卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context,
                               final MsgRobot msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_attachment_file_view, null);
        TextView fileNameText = (TextView) convertView
                .findViewById(R.id.file_name_text);
        TextView fileSizeText = (TextView) convertView
                .findViewById(R.id.file_size_text);
        ImageView img = (ImageView)convertView.findViewById(R.id.file_icon_img);
        final MsgContentAttachmentFile msgContentFile = msg.getMsgContentAttachmentFile();
        ImageDisplayUtils.getInstance().displayImage(img, "drawable://" + FileUtils.getIconResIdRobot(msgContentFile.getCategory()));
        fileNameText.setText(msgContentFile.getName());
        fileSizeText.setText(FileUtils.formatFileSize(msgContentFile.getSize()));
        final String downloadUri = "https://emm.inspur.com/api/bot/v6.0/getfile/"+msgContentFile.getMedia();
        final String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + msgContentFile.getName();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIDownloadCallBack progressCallback = new APIDownloadCallBack(context, downloadUri) {
                    @Override
                    public void callbackStart() {
                    }

                    @Override
                    public void callbackLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void callbackSuccess(File file) {
                        ToastUtils.show(
                                context,
                                context.getString(R.string.download_success));
                        FileUtils.openFile(context, fileDownloadPath);
                    }

                    @Override
                    public void callbackError(Throwable arg0, boolean arg1) {
                        ToastUtils.show(context, context
                                .getString(R.string.download_fail));
                    }

                    @Override
                    public void callbackCanceled(CancelledException e) {

                    }

                };
                DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
                if (FileUtils.isFileExist(fileDownloadPath)) {
                    FileUtils.openFile(context, fileDownloadPath);
                } else {
                    downLoaderUtils.startDownLoad(downloadUri, fileDownloadPath,
                            progressCallback);
                }
            }
        });
        return convertView;
    }


}
