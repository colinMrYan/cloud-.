package com.inspur.emmcloud.ui.appcenter.volume;

import java.io.File;
import java.text.SimpleDateFormat;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileIconUtils;
import com.inspur.imp.plugin.file.FileUtil;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘下载
 */
public class VolumeFileDownloadActivtiy extends BaseActivity {

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
    private VolumeFile volumeFile;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volumeFile = (VolumeFile) getIntent().getSerializableExtra("volumeFile");
        fileNameText.setText(volumeFile.getName());
        fileTypeImg.setImageResource(VolumeFileIconUtils.getIconResId(volumeFile));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        fileTimeText.setText(TimeUtils.getTime(volumeFile.getCreationDate(), format));
        fileSavePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + volumeFile.getName();
        if (FileUtils.isFileExist(fileSavePath)) {
            downloadBtn.setText(R.string.open);
        } else {
            downloadBtn.setText(getString(R.string.clouddriver_download_size, FileUtils.formatFileSize(volumeFile.getSize())));
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
        String volumeId = getIntent().getStringExtra("volumeId");
        String currentDirAbsolutePath = getIntent().getStringExtra("currentDirAbsolutePath");
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
