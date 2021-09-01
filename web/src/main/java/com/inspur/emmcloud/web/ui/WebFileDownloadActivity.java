package com.inspur.emmcloud.web.ui;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.NetworkMobileTipUtil;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;
import com.inspur.emmcloud.web.api.WebProgressCallback;
import com.inspur.emmcloud.web.bean.WebFileDownloadBean;
import com.inspur.emmcloud.web.util.WebFileDownloadManager;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Date：2021/8/24
 * Author：wang zhen
 * Description web文件跳转原生下载页面
 */
public class WebFileDownloadActivity extends BaseActivity {
    @BindView(R2.id.download_status_layout)
    LinearLayout downloadStatusLayout;
    @BindView(R2.id.download_btn)
    Button downloadBtn;
    @BindView(R2.id.download_progress)
    ProgressBar progressBar;
    @BindView(R2.id.progress_text)
    TextView progressText;
    @BindView(R2.id.tv_file_name)
    TextView fileNameText;
    @BindView(R2.id.iv_file_type_file_preview)
    ImageView fileTypeImg;
    @BindView(R2.id.tv_file_size)
    TextView fileSizeText;
    @BindView(R2.id.tv_file_open_tips)
    TextView fileOpenTipsText;
    // 文件存储path
    private String fileSavePath;
    private WebFileDownloadBean webFileDownloadBean;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        webFileDownloadBean = (WebFileDownloadBean) getIntent().getSerializableExtra("webFileDownload");
        fileNameText.setText(webFileDownloadBean.getFileName());
        fileTypeImg.setImageResource(FileUtils.getFileIconResIdByFileName(webFileDownloadBean.getFileName()));
        fileSizeText.setText(FileUtils.formatFileSize(webFileDownloadBean.getFileSize()));
        fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_WEB, webFileDownloadBean.getFileId(), webFileDownloadBean.getFileName());
        if (!StringUtils.isBlank(fileSavePath)) {
            setDownloadingStatus(true);
        } else {
            setDownloadingStatus(false);
            boolean isStartDownload = getIntent().getBooleanExtra("isStartDownload", false);
            String status = WebFileDownloadManager.getInstance().getFileStatus(webFileDownloadBean.getFileId());
            if (!isStartDownload) {
                if (status.equals(VolumeFile.STATUS_LOADING)) {
                    isStartDownload = true;
                } else if (status.equals(VolumeFile.STATUS_PAUSE)) {
                    downloadBtn.setText(R.string.download);
                }
            }
            if (isStartDownload) {
                downloadFile();
            }
        }
    }

    // 文件下载
    private void downloadFile() {
        fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() + FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), webFileDownloadBean.getFileName());
        downloadBtn.setVisibility(View.GONE);
        downloadStatusLayout.setVisibility(View.VISIBLE);
        webFileDownloadBean.setStatus(VolumeFile.STATUS_LOADING);
        // 获取下载文件列表
        List<WebFileDownloadBean> webFileDownloadBeanList = WebFileDownloadManager.getInstance().getAllDownloadWebFile();

        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            for (WebFileDownloadBean file : webFileDownloadBeanList) {
                if (file.getFileId().equals(webFileDownloadBean.getFileId())) {
                    WebFileDownloadManager.getInstance().reDownloadFile(webFileDownloadBean);
                    setProgressListener();
                    return;
                }
            }
            WebFileDownloadManager.getInstance().resetWebFileStatus(webFileDownloadBean);
            WebFileDownloadManager.getInstance().downloadFile(webFileDownloadBean);
            setProgressListener();
        }
    }

    // 下载进度监听
    private void setProgressListener() {
        WebFileDownloadManager.getInstance().setBusinessProgressCallback(webFileDownloadBean, new WebProgressCallback() {
            @Override
            public void onSuccess(File file) {
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_WEB, webFileDownloadBean.getFileId(), webFileDownloadBean.getFileName(), fileSavePath);
                downloadStatusLayout.setVisibility(View.GONE);
                progressBar.setProgress(0);
                progressText.setText("");
                downloadBtn.setVisibility(View.VISIBLE);
                setDownloadingStatus(true);
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
                progressBar.setProgress(progress);
                String totalSize = FileUtils.formatFileSize(webFileDownloadBean.getFileSize());
                String currentSize = FileUtils.formatFileSize(current);
                progressText.setText(getString(R.string.downloading_status, currentSize, totalSize));
            }

            @Override
            public void onFail() {
                if (downloadStatusLayout.getVisibility() == View.VISIBLE) {
                    downloadStatusLayout.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    progressText.setText("");
                    downloadBtn.setVisibility(View.VISIBLE);
                    setDownloadingStatus(false);
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.web_activity_web_file_download;
    }


    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.download_btn) {
            if (FileUtils.isFileExist(fileSavePath)) {
                FileUtils.openFile(BaseApplication.getInstance(), fileSavePath);
            } else {
                NetworkMobileTipUtil.checkEnvironment(this, R.string.file_download_network_type_warning,
                        webFileDownloadBean.getFileSize(), new NetworkMobileTipUtil.Callback() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void onNext() {
                                downloadFile();
                            }
                        });
            }
        } else if (id == R.id.file_download_close_img) {
            downloadBtn.setText(R.string.download);
            downloadBtn.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressText.setText("");
            downloadStatusLayout.setVisibility(View.GONE);
            if (webFileDownloadBean != null) {
                WebFileDownloadManager.getInstance().cancelDownloadFile(webFileDownloadBean);
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
}
