package com.inspur.emmcloud.ui.app.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileDownloadUrlResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.VolumeFileIconUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.imp.plugin.file.FileUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;


/**
 * Created by chenmch on 2017/11/17.
 */

@ContentView(R.layout.activity_volume_file_download)
public class VolumeFileDownloadActivtiy extends BaseActivity {

    @ViewInject(R.id.download_status_layout)
    private LinearLayout downloadStatusLayout;

    @ViewInject(R.id.download_btn)
    private Button downloadBtn;

    @ViewInject(R.id.download_progress)
    private ProgressBar progressBar;

    @ViewInject(R.id.progress_text)
    private TextView progressText;

    @ViewInject(R.id.file_name_text)
    private TextView fileNameText;

    @ViewInject(R.id.file_type_img)
    private ImageView fileTypeImg;

    @ViewInject(R.id.file_time_text)
    private TextView fileTimeText;

    private DownLoaderUtils downLoaderUtils;
    private VolumeFile volumeFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volumeFile = (VolumeFile) getIntent().getSerializableExtra("volumeFile");
        fileNameText.setText(volumeFile.getName());
        fileTypeImg.setImageResource(VolumeFileIconUtils.getIconResId(volumeFile));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        fileTimeText.setText(TimeUtils.getTime(volumeFile.getCreationDate(), format));
        downloadBtn.setText("下载"+" ("+ FileUtils.formatFileSize(volumeFile.getSize())+")");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.download_btn:
                if (NetUtils.isNetworkConnected(getApplicationContext()) && AppUtils.isHasSDCard(getApplicationContext())) {
                    downloadBtn.setVisibility(View.GONE);
                    downloadStatusLayout.setVisibility(View.VISIBLE);
                    downloadFile();
                   // getVolumeFileDownloadUrl();
                }
                break;
            case R.id.file_download_close_img:
                downLoaderUtils.pauseDownLoad();
                downloadBtn.setVisibility(View.VISIBLE);
                downloadStatusLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 请求下载的token
     */
    private void getVolumeFileDownloadUrl(){
        if (NetUtils.isNetworkConnected(getApplicationContext())){
            String volumeId = getIntent().getStringExtra("volumeId");
            String absolutePath = getIntent().getStringExtra("absolutePath");
            MyAppAPIService apiService = new MyAppAPIService(VolumeFileDownloadActivtiy.this);
            apiService.setAPIInterface(new WebService());
            apiService.getVolumeFileDownloadUrl(volumeId,absolutePath);
        }

    }

    /**
     * 下载文件
     */
    private void downloadFile() {
        String fileName = volumeFile.getName();
        String volumeId = getIntent().getStringExtra("volumeId");
        String absolutePath = getIntent().getStringExtra("absolutePath");
        String source =  APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);
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
                progressText.setText("下载中（" + currentSize + "/" + totleSize + ")");

            }

            @Override
            public void callbackSuccess(File file) {
                progressText.setText("");
                ToastUtils.show(getApplicationContext(), R.string.download_success);
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
        params.addParameter("volumeId",volumeId);
        params.addQueryStringParameter("path",absolutePath);
        params.setAutoResume(true);// 断点下载
        params.setSaveFilePath( MyAppConfig.LOCAL_DOWNLOAD_PATH+fileName);
        params.setCancelFast(true);
        Callback.Cancelable cancelable = x.http().get(params, callBack);
    }

    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnVolumeFileDownloadUrlSuccess(GetVolumeFileDownloadUrlResult getVolumeFileDownloadUrlResult) {
            super.returnVolumeFileDownloadUrlSuccess(getVolumeFileDownloadUrlResult);
        }

        @Override
        public void returnVolumeFileDownloadUrlFail(String error, int errorCode) {
            LogUtils.jasonDebug("error="+error);
            LogUtils.jasonDebug("errorCode="+errorCode);
            WebServiceMiddleUtils.hand(getApplicationContext(),error,errorCode);
        }
    }
}
