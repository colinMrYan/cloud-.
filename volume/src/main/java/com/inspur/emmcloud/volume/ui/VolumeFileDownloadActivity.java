package com.inspur.emmcloud.volume.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.NetworkMobileTipUtil;
import com.inspur.emmcloud.basemodule.util.ShareFile2OutAppUtils;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.api.VolumeAPIUri;
import com.inspur.emmcloud.volume.util.VolumeFileDownloadManager;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘下载
 */
public class VolumeFileDownloadActivity extends BaseActivity {

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
    @BindView(R2.id.iv_file_type_img_preview)
    ImageView fileTypeImg;
    @BindView(R2.id.tv_file_size)
    TextView fileSizeText;
    @BindView(R2.id.tv_share)
    TextView shareFileTextView;
    @BindView(R2.id.tv_file_open_tips)
    TextView fileOpenTipsText;
    @BindView(R2.id.iv_file_type_file_preview)
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
                if (status.equals(VolumeFile.STATUS_LOADING)) {
                    isStartDownload = true;
                } else if (status.equals(VolumeFile.STATUS_PAUSE)) {
                    downloadBtn.setText(R.string.redownload);
                }
            }
            if (isStartDownload) {
                downloadFile();
            }
        }
    }

    private void showVolumeFileTypeImg() {
        if (volumeFile.getFormat().startsWith("image/")) {
            String url = "";
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                url = volumeFile.getLocalFilePath();
            } else {
                url = VolumeAPIUri.getVolumeFileTypeImgThumbnailUrl(volumeFile, currentDirAbsolutePath);
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
        return R.layout.volume_activity_volume_file_download;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.download_btn) {
            if (FileUtils.isFileExist(fileSavePath)) {
                FileUtils.openFile(BaseApplication.getInstance(), fileSavePath);
            } else {
                NetworkMobileTipUtil.checkEnvironment(this, R.string.volume_file_download_network_type_warning,
                        volumeFile.getSize(), new NetworkMobileTipUtil.Callback() {
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
            downloadBtn.setText(R.string.redownload);
            downloadBtn.setVisibility(View.VISIBLE);
            downloadStatusLayout.setVisibility(View.GONE);
            List<VolumeFile> volumeFileList = VolumeFileDownloadManager.getInstance().getAllDownloadVolumeFile();

            if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
                volumeFile.setVolumeFileAbsolutePath(currentDirAbsolutePath);
                for (VolumeFile file : volumeFileList) {
                    if (file.getId().equals(volumeFile.getId())) {
                        VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
                        return;
                    }
                }
            }
        } else if (id == R.id.tv_share) {
            String fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(
                    DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
            if (!StringUtils.isBlank(fileSavePath)) {
                shareFile(fileSavePath);
            } else {
                ToastUtils.show(getString(R.string.volume_clouddriver_volume_frist_download));
            }
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
                        } else if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                            shareToFriends(volumeFile);
                        }
                    }
                });
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_WECHAT)) {
            shareAction.addButton(PlatformName.WEIXIN, "WEIXIN", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_MOBILE_QQ)) {
            shareAction.addButton(PlatformName.QQ, "QQ", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.addButton(getString(R.string.internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();
    }

    /**
     * 分享到频道
     */
    private void shareToFriends(VolumeFile volumeFile) {
        List<String> urlList = new ArrayList<>();
        String filePath = volumeFile.getLocalFilePath();
        if (StringUtils.isBlank(filePath) || !FileUtils.isFileExist(filePath)) {
            ToastUtils.show(this, getString(R.string.share_not_support));
            return;
        }
        urlList.add(filePath);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) urlList);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_SHARE_FILE).with(bundle).navigation();
    }

    /**
     * 下载文件
     */
    private void downloadFile() {
        downloadBtn.setVisibility(View.GONE);
        downloadStatusLayout.setVisibility(View.VISIBLE);
        volumeFile.setStatus(VolumeFile.STATUS_LOADING);

        List<VolumeFile> volumeFileList = VolumeFileDownloadManager.getInstance().getAllDownloadVolumeFile();

        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
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
//                ToastUtils.show(getApplicationContext(), R.string.download_success);
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
                    progressText.setText(getString(R.string.downloading_status, currentSize, totalSize));
                }
            }

            @Override
            public void onFail() {
                Log.d("zhang", "Activity onFail: ");
                if (downloadStatusLayout.getVisibility() == View.VISIBLE) {
//                    ToastUtils.show(getApplicationContext(), R.string.download_fail);
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
