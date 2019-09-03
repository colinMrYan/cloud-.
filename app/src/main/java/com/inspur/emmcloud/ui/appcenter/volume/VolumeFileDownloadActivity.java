package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.util.privates.VolumeFileIconUtils;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;

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
    @BindView(R.id.file_type_img)
    ImageView fileTypeImg;
    @BindView(R.id.tv_file_size)
    TextView fileSizeText;
    @BindView(R.id.tv_file_open_tips)
    TextView fileOpenTipsText;
    private String fileSavePath = "";
    private Callback.Cancelable cancelable;
    private VolumeFile volumeFile;
    private String currentDirAbsolutePath;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volumeFile = (VolumeFile) getIntent().getSerializableExtra("volumeFile");
        fileNameText.setText(volumeFile.getName());
        currentDirAbsolutePath = getIntent().getStringExtra("currentDirAbsolutePath");
        if (volumeFile.getFormat().startsWith("image/")) {
            String url = APIUri.getVolumeFileUploadSTSTokenUrl(volumeFile.getVolume()) + "?path=" + StringUtils.encodeURIComponent(currentDirAbsolutePath);
            LogUtils.jasonDebug("url===" + url);
            ImageDisplayUtils.getInstance().displayImage(fileTypeImg, url, R.drawable.ic_volume_file_typ_img);
        } else {
            fileTypeImg.setImageResource(VolumeFileIconUtils.getIconResId(volumeFile));
        }

        fileSizeText.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        fileSavePath = MyAppConfig.getVolumeFileDownloadDirPath() + volumeFile.getName();
        if (FileUtils.isFileExist(fileSavePath)) {
            setDownloadingStatus(true);
        } else {
            setDownloadingStatus(false);
            boolean isStartDownload = getIntent().getBooleanExtra("isStartDownload", false);
            if (isStartDownload && checkDownloadEnvironment()) {
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
                    if (checkDownloadEnvironment()) {
                        downloadFile();
                    }

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
        String volumeId = getIntent().getStringExtra("volumeId");
        String source = APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);
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
                String totalSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);
                progressText.setText(getString(R.string.clouddriver_downloading_status, currentSize, totalSize));

            }

            @Override
            public void callbackSuccess(File file) {
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

        RequestParams params = ((MyApplication) getApplicationContext()).getHttpRequestParams(source);
        params.addParameter("volumeId", volumeId);
        params.addQueryStringParameter("path", currentDirAbsolutePath);
        params.setRedirectHandler(new RedirectHandler() {
            @Override
            public RequestParams getRedirectParams(UriRequest uriRequest) throws Throwable {
                String locationUrl = uriRequest.getResponseHeader("Location");
                RequestParams params = new RequestParams(locationUrl);
                params.setAutoResume(true);// 断点下载
                params.setSaveFilePath(fileSavePath);
                params.setCancelFast(true);
                params.setMethod(HttpMethod.GET);
                return params;
            }
        });
        cancelable = x.http().get(params, callBack);
    }

}
