package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.util.privates.ShareFile2OutAppUtils;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadManager;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘下载
 */
public class VolumeFileDownloadActivity extends BaseActivity {

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
    @BindView(R.id.iv_file_type_img_preview)
    ImageView fileTypeImg;
    @BindView(R.id.tv_file_size)
    TextView fileSizeText;
    @BindView(R.id.tv_share)
    TextView shareFileTextView;
    @BindView(R.id.tv_file_open_tips)
    TextView fileOpenTipsText;
    @BindView(R.id.iv_file_type_file_preview)
    ImageView typeFileImageView;
    private String fileSavePath = "";
    private VolumeFile volumeFile;
    private String currentDirAbsolutePath;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volumeFile = (VolumeFile) getIntent().getSerializableExtra("volumeFile");
        fileNameText.setText(volumeFile.getName());
        currentDirAbsolutePath = getIntent().getStringExtra("currentDirAbsolutePath");
        showVolumeFileTypeImg();
        fileSizeText.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
        if (!StringUtils.isBlank(fileSavePath)) {
            setDownloadingStatus(true);
        } else {
            fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() + FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), volumeFile.getName());
            setDownloadingStatus(false);
            boolean isStartDownload = getIntent().getBooleanExtra("isStartDownload", false);
            if (!isStartDownload) {
                String status = VolumeFileDownloadManager.getInstance().getFileStatus(volumeFile);
                if (status.equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                    isStartDownload = true;
                } else if (status.equals(VolumeFile.STATUS_DOWNLOAD_PAUSE)) {
                    downloadBtn.setText(R.string.redownload);
                }
            }
            if (isStartDownload && checkDownloadEnvironment()) {
                downloadFile();
            }
        }
    }

    private void showVolumeFileTypeImg() {
        if (volumeFile.getFormat().startsWith("image/")) {
            String url = "";
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
                url = volumeFile.getLocalFilePath();
            } else {
                url = APIUri.getVolumeFileTypeImgThumbnailUrl(volumeFile, currentDirAbsolutePath);
            }
            fileTypeImg.setTag(url);
            fileTypeImg.setVisibility(View.VISIBLE);
            typeFileImageView.setVisibility(View.GONE);
            ImageDisplayUtils.getInstance().displayImage(fileTypeImg, url, R.drawable.baselib_file_type_img);
        } else {
            typeFileImageView.setImageResource(FileUtils.getFileIconResIdByFormat(volumeFile.getFormat()));
            typeFileImageView.setVisibility(View.VISIBLE);
            fileTypeImg.setVisibility(View.GONE);
        }


    }

    private void setDownloadingStatus(boolean isDownloaded) {
        if (isDownloaded) {
            shareFileTextView.setVisibility(View.VISIBLE);
            if (FileUtils.canFileOpenByApp(fileSavePath)) {
                downloadBtn.setText(R.string.open);
                fileOpenTipsText.setVisibility(View.GONE);
            } else {
                downloadBtn.setText(R.string.file_open_by_other_app);
                fileOpenTipsText.setText(getString(R.string.file_open_by_other_app_tips, AppUtils.getAppName(this)));
                fileOpenTipsText.setVisibility(View.VISIBLE);

            }
        } else {
            shareFileTextView.setVisibility(View.GONE);
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
                    if (checkDownloadEnvironment()) {
                        downloadFile();
                    }
                }
                break;
            case R.id.file_download_close_img:
                downloadBtn.setText(R.string.redownload);
                downloadBtn.setVisibility(View.VISIBLE);
                downloadStatusLayout.setVisibility(View.GONE);
                List<VolumeFile> volumeFileList = VolumeFileDownloadManager.getInstance().getAllDownloadVolumeFile();

                if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                    volumeFile.setVolumeFileAbsolutePath(currentDirAbsolutePath);
                    for (VolumeFile file : volumeFileList) {
                        if (file.getId().equals(volumeFile.getId())) {
                            VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
                            return;
                        }
                    }
                }
                break;
            case R.id.tv_share:
                String fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(
                        DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
                if (!StringUtils.isBlank(fileSavePath)) {
                    shareFile(fileSavePath);
                } else {
                    ToastUtils.show(getString(R.string.clouddriver_volume_frist_download));
                }
                break;
            default:
                break;
        }
    }

    public void shareFile(final String filePath) {
        ShareAction shareAction = new ShareAction(this)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (snsPlatform.mKeyword.equals("WEIXIN")) {
                            ShareFile2OutAppUtils.shareFile2WeChat(getApplicationContext(), filePath);
                        } else if (snsPlatform.mKeyword.equals("QQ")) {
                            ShareFile2OutAppUtils.shareFileToQQ(getApplicationContext(), filePath);
                        }
                    }
                });
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_WECHAT)) {
            shareAction.addButton(PlatformName.WEIXIN, "WEIXIN", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_MOBILE_QQ)) {
            shareAction.addButton(PlatformName.QQ, "QQ", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.open();
    }

    private boolean checkDownloadEnvironment() {
        if (!NetUtils.isNetworkConnected(getApplicationContext()) || !AppUtils.isHasSDCard(getApplicationContext())) {
            return false;
        }
        if (volumeFile.getSize() >= MyAppConfig.NETWORK_MOBILE_MAX_SIZE_ALERT && NetUtils.isNetworkTypeMobile(getApplicationContext())) {
            showNetworkMobileAlert();
            return false;
        }
        return true;
    }


    private void showNetworkMobileAlert() {
        new CustomDialog.MessageDialogBuilder(VolumeFileDownloadActivity.this)
                .setMessage(R.string.volume_file_upload_network_type_warning)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        downloadFile();
                    }
                })
                .show();
    }

    /**
     * 下载文件
     */
    private void downloadFile() {
        downloadBtn.setVisibility(View.GONE);
        downloadStatusLayout.setVisibility(View.VISIBLE);
        volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);

        List<VolumeFile> volumeFileList = VolumeFileDownloadManager.getInstance().getAllDownloadVolumeFile();

        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            volumeFile.setVolumeFileAbsolutePath(currentDirAbsolutePath);
            for (VolumeFile file : volumeFileList) {
                if (file.getId().equals(volumeFile.getId())) {
                    VolumeFileDownloadManager.getInstance().reDownloadFile(volumeFile,
                            currentDirAbsolutePath);
                    setProgressListener();
                    return;
                }
            }
            VolumeFileDownloadManager.getInstance().resetVolumeFileStatus(volumeFile);
            VolumeFileDownloadManager.getInstance().downloadFile(volumeFile, currentDirAbsolutePath);
            setProgressListener();
        }
    }

    private void setProgressListener() {
        VolumeFileDownloadManager.getInstance().setBusinessProgressCallback(volumeFile, new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                Log.d("zhang", "Activity onSuccess: ");
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName(), fileSavePath);
                ToastUtils.show(getApplicationContext(), R.string.download_success);
                downloadStatusLayout.setVisibility(View.GONE);
                progressBar.setProgress(0);
                progressText.setText("");
                downloadBtn.setVisibility(View.VISIBLE);
                shareFileTextView.setVisibility(View.VISIBLE);
                setDownloadingStatus(true);
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
                Log.d("zhang", "Activity downLoading: progress = " + progress
                        + ",speed = " + speed + ",status = " + volumeFile.getStatus());
                progressBar.setProgress(progress);
                String totalSize = FileUtils.formatFileSize(volumeFile.getSize());
                String currentSize = FileUtils.formatFileSize(current);
                if (current >= 0) {
                    progressText.setText(getString(R.string.clouddriver_downloading_status, currentSize, totalSize));
                }
            }

            @Override
            public void onFail() {
                Log.d("zhang", "Activity onFail: ");
                if (downloadStatusLayout.getVisibility() == View.VISIBLE) {
                    ToastUtils.show(getApplicationContext(), R.string.download_fail);
                    downloadStatusLayout.setVisibility(View.GONE);
                    shareFileTextView.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    progressText.setText("");
                    downloadBtn.setVisibility(View.VISIBLE);
                    setDownloadingStatus(false);
                }
            }
        });
    }

}
