package com.inspur.emmcloud.ui.app.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.imp.plugin.file.FileUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;


/**
 * Created by chenmch on 2017/11/17.
 */

@ContentView(R.layout.activity_volume_file_info)
public class VolumeFileInfoActivtiy extends BaseActivity {

    @ViewInject(R.id.download_status_layout)
    private LinearLayout downloadStatusLayout;

    @ViewInject(R.id.download_btn)
    private Button downloadBtn;

    @ViewInject(R.id.download_progress)
    private ProgressBar progressBar;

    @ViewInject(R.id.progress_text)
    private TextView progressText;

    private DownLoaderUtils downLoaderUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.rename_text:
                break;
            case R.id.download_btn:
                if (NetUtils.isNetworkConnected(getApplicationContext()) && AppUtils.isHasSDCard(getApplicationContext())){
                    downloadBtn.setVisibility(View.GONE);
                    downloadStatusLayout.setVisibility(View.VISIBLE);
                    downloadFile();
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
     * 下载文件
     */
    private void downloadFile(){
        String fileName = "test.apk";
        String source = "https://emm.inspur.com/upgrade/download/android_dev?version=1.2.12";
        APIDownloadCallBack callBack = new APIDownloadCallBack(getApplicationContext(),source) {
            @Override
            public void callbackStart() {
                progressBar.setProgress(0);
                progressText.setText("");
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress =(int)(current*100.0/total);
                progressBar.setProgress(progress);
                String totleSize = FileUtil.formetFileSize(total);
                String currentSize = FileUtil.formetFileSize(current);
                progressText.setText("下载中（"+currentSize+"/"+totleSize+")");

            }

            @Override
            public void callbackSuccess(File file) {
                progressText.setText("");
                ToastUtils.show(getApplicationContext(),R.string.download_success);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                if (downloadStatusLayout.getVisibility() == View.VISIBLE){
                    ToastUtils.show(getApplicationContext(),R.string.download_fail);
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
        downLoaderUtils = new DownLoaderUtils();
        downLoaderUtils.startDownLoad(source, MyAppConfig.LOCAL_DOWNLOAD_PATH,fileName,callBack);
    }
}
