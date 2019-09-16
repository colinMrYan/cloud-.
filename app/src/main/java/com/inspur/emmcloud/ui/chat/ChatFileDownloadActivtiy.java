package com.inspur.emmcloud.ui.chat;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;

import org.xutils.common.Callback;

import java.io.File;

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
    @BindView(R.id.tv_file_size)
    TextView fileSizeText;
    @BindView(R.id.tv_file_open_tips)
    TextView fileOpenTipsText;
    private String fileSavePath = "";
    private Callback.Cancelable cancelable;
    private Message message;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        message = (Message) getIntent().getSerializableExtra("message");
        MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
        fileNameText.setText(msgContentFile.getName());
        fileTypeImg.setImageResource(FileUtils.getFileIconResIdByFileName(msgContentFile.getName()));
        fileSizeText.setText(FileUtils.formatFileSize(msgContentFile.getSize()));
        fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
        if (!StringUtils.isBlank(fileSavePath)) {
            setDownloadingStatus(true);
        } else {
            setDownloadingStatus(false);
            boolean isStartDownload = getIntent().getBooleanExtra("isStartDownload", false);
            if (isStartDownload) {
                downloadFile();
            }
        }
    }

    private void setDownloadingStatus(boolean isDownloaded) {
        if (isDownloaded) {
            if (FileUtils.canFileOpenByApp(fileSavePath)) {
                downloadBtn.setText(R.string.open);
                fileOpenTipsText.setVisibility(View.GONE);
            } else {
                downloadBtn.setText(R.string.file_open_by_other_app);
                fileOpenTipsText.setText(getString(R.string.file_open_by_other_app_tips, AppUtils.getAppName(this)));
                fileOpenTipsText.setVisibility(View.VISIBLE);

            }
        } else {
            downloadBtn.setText(R.string.download);
            fileOpenTipsText.setVisibility(View.GONE);
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
                if (FileUtils.isFileExist(fileSavePath)) {
                    FileUtils.openFile(BaseApplication.getInstance(), fileSavePath);
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
        fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() + FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), message.getMsgContentAttachmentFile().getName());
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
                String totleSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);
                progressText.setText(getString(R.string.clouddriver_downloading_status, currentSize, totleSize));

            }

            @Override
            public void callbackSuccess(File file) {
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), message.getMsgContentAttachmentFile().getName(), fileSavePath);
                ToastUtils.show(getApplicationContext(), R.string.download_success);
                downloadStatusLayout.setVisibility(View.GONE);
                progressBar.setProgress(0);
                progressText.setText("");
                downloadBtn.setVisibility(View.VISIBLE);
                setDownloadingStatus(true);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                if (downloadStatusLayout.getVisibility() == View.VISIBLE) {
                    ToastUtils.show(getApplicationContext(), R.string.download_fail);
                    downloadStatusLayout.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    progressText.setText("");
                    downloadBtn.setVisibility(View.VISIBLE);
                    setDownloadingStatus(false);
                }
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };
        cancelable = new DownLoaderUtils().startDownLoad(source, fileSavePath, callBack);
    }

}
