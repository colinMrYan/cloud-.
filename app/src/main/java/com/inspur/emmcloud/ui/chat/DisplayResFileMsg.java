package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
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
     * @param childView
     * @param msg
     */
    public static View displayResFileMsg(final Context context,
                                          final Msg msg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_file_view, null);
        TextView fileTitleText = (TextView) cardContentView
                .findViewById(R.id.file_name_text);
        TextView fileSizeText = (TextView) cardContentView
                .findViewById(R.id.file_size_text);
        final ImageView fileDownLoadImg = (ImageView) cardContentView
                .findViewById(R.id.filecard_download_img);
        String msgBody = msg.getBody();
        String fileSize = JSONUtils.getString(msgBody, "size", "");
        String fileName = JSONUtils.getString(msgBody, "name", "");
        final String downloadUri = JSONUtils.getString(msgBody, "key", "");
        RoundAngleImageView roundAngleImageView = (RoundAngleImageView) cardContentView
                .findViewById(R.id.file_type_img);
        ImageDisplayUtils.getInstance().displayImage(roundAngleImageView, "drawable://" + FileUtils.getIconResId(downloadUri));
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
        final HorizontalProgressBarWithNumber fileProgressBar = (HorizontalProgressBarWithNumber) cardContentView
                .findViewById(R.id.file_download_progressbar);
        fileProgressBar.setTag(target);
        fileProgressBar.setVisibility(View.VISIBLE);
        cardContentView.findViewById(R.id.header_layout)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((0 < fileProgressBar.getProgress())
                                && (fileProgressBar.getProgress() < 100)) {
                            return;
                        }
                        APIDownloadCallBack progressCallback = new APIDownloadCallBack(context, downloadUri) {
                            @Override
                            public void callbackStart() {
                                if ((fileProgressBar.getTag() != null)
                                        && (fileProgressBar.getTag() == target)) {
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
                                fileDownLoadImg.setVisibility(View.GONE);
                                ToastUtils.show(
                                        context,
                                        context.getString(R.string.chat_file_download_success));
                            }

                            @Override
                            public void callbackError(Throwable arg0, boolean arg1) {
                                fileProgressBar.setVisibility(View.GONE);
                                ToastUtils.show(context, context
                                        .getString(R.string.download_fail));
                            }

                            @Override
                            public void callbackCanceled(CancelledException e) {

                            }

                        };
                        showOrDownLoadFile(context, downloadUri, target,
                                fileDownLoadImg, progressCallback);
                    }
                });
        return cardContentView;
    }

    /**
     * 展示或下载文件
     *
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
            String completeDownloadUrl = APIUri.getPreviewUrl(downloadUri);
            downLoaderUtils.startDownLoad(completeDownloadUrl, target,
                    fileProgressBar);
        }

    }

}
