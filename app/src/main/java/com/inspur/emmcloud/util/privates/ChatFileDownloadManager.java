package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.DownloadInfo;
import com.inspur.emmcloud.bean.chat.Message;

import org.xutils.common.Callback;

import java.io.File;

public class ChatFileDownloadManager {
    private volatile static ChatFileDownloadManager instance;

    public ChatFileDownloadManager() {

    }

    public static ChatFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (ChatFileDownloadManager.class) {
                if (instance == null) {
                    instance = new ChatFileDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 下载文件
     */
    private void downloadFile(final Message message) {
        if (!NetUtils.isNetworkConnected(BaseApplication.getInstance()) || !AppUtils.isHasSDCard(BaseApplication.getInstance())) {
            return;
        }
        final String fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() + FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), message.getMsgContentAttachmentFile().getName());
        String source = APIUri.getChatFileResouceUrl(message.getChannel(), message.getMsgContentAttachmentFile().getMedia());
        DownloadInfo downloadInfo = DownloadInfo.message2DownloadInfo(message);
        downloadInfo.setLocalPath(fileSavePath);
        downloadInfo.setUrl(source);
        DownloadCacheUtils.saveDownloadFile(downloadInfo);
        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {
//                progressBar.setProgress(0);
//                progressText.setText("");
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                String totleSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);

            }

            @Override
            public void callbackSuccess(File file) {
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), message.getMsgContentAttachmentFile().getName(), fileSavePath);
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_fail);
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };
        Callback.Cancelable cancelable = new DownLoaderUtils().startDownLoad(source, fileSavePath, callBack);
    }
}
