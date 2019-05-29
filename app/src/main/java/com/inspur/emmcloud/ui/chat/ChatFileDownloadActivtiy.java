package com.inspur.emmcloud.ui.chat;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.imp.plugin.file.FileUtil;

import org.xutils.common.Callback;

import java.io.File;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 聊天文件下载
 */
public class ChatFileDownloadActivtiy extends BaseActivity {

    @BindView(R.id.download_status_layout)
    LinearLayout downloadStatusLayout;
    @BindView(R.id.download_btn)
    Button downloadBtn;
    @BindView(R.id.download_progress)
    ProgressBar progressBar;
    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.tv_file_name)
    TextView fileNameText;
    @BindView(R.id.file_type_img)
    ImageView fileTypeImg;
    @BindView(R.id.file_time_text)
    TextView fileTimeText;
    private String fileSavePath = "";
    private Callback.Cancelable cancelable;
    private Message message;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        message = (Message) getIntent().getSerializableExtra("message");
        MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
        fileNameText.setText(msgContentFile.getName());
        fileTypeImg.setImageResource(FileUtils.getRegularFileIconResId(msgContentFile.getName()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        fileTimeText.setText(TimeUtils.getTime(message.getCreationDate(), format));
        fileSavePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + msgContentFile.getName();
        if (FileUtils.isFileExist(fileSavePath)) {
            downloadBtn.setText(R.string.open);
        } else {
            downloadBtn.setText(getString(R.string.clouddriver_download_size, FileUtils.formatFileSize(msgContentFile.getSize())));
            boolean isStartDownload = getIntent().getBooleanExtra("isStartDownload", false);
            if (isStartDownload) {
                downloadFile();
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_file_download;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.download_btn:
                if (downloadBtn.getText().equals(getString(R.string.open))) {
                    FileUtils.openFile(getApplicationContext(), fileSavePath);
                } else {
                    downloadFile();
                }
                break;
            case R.id.file_download_close_img:
                if (cancelable != null) {
                    cancelable.cancel();
                }
                downloadBtn.setVisibility(View.VISIBLE);
                downloadStatusLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


    /**
     * 下载文件
     */
    private void downloadFile() {
        if (!NetUtils.isNetworkConnected(getApplicationContext()) || !AppUtils.isHasSDCard(getApplicationContext())) {
            return;
        }
        downloadBtn.setVisibility(View.GONE);
        downloadStatusLayout.setVisibility(View.VISIBLE);
        String source = APIUri.getChatFileResouceUrl(message.getChannel(), message.getMsgContentAttachmentFile().getMedia());
        APIDownloadCallBack callBack = new APIDownloadCallBack(getApplicationContext(), source) {
            @Override
            public void callbackStart() {
                progressBar.setProgress(0);
                progressText.setText("");
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                progressBar.setProgress(progress);
                String totleSize = FileUtil.formetFileSize(total);
                String currentSize = FileUtil.formetFileSize(current);
                progressText.setText(getString(R.string.clouddriver_downloading_status, currentSize, totleSize));

            }

            @Override
            public void callbackSuccess(File file) {
                ToastUtils.show(getApplicationContext(), R.string.download_success);
                downloadStatusLayout.setVisibility(View.GONE);
                progressBar.setProgress(0);
                progressText.setText("");
                downloadBtn.setVisibility(View.VISIBLE);
                downloadBtn.setText(R.string.open);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                if (downloadStatusLayout.getVisibility() == View.VISIBLE) {
                    ToastUtils.show(getApplicationContext(), R.string.download_fail);
                    downloadStatusLayout.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    progressText.setText("");
                    downloadBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };
        cancelable = new DownLoaderUtils().startDownLoad(source, fileSavePath, callBack);
    }

}
