package com.inspur.emmcloud.util;

import java.io.File;

import com.inspur.emmcloud.MyApplication;
import com.thin.downloadmanager.DownloadManager;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;
import com.thin.downloadmanager.DownloadRequest.Priority;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * 下载工具类
 * 如果需要监听下载进度，则需要在调用这个类的地方加上DownloadStatusListener
 *
 */
public class InspurDownLoaderManager {

	//下载到的地址
	private String urlPath;
	//下载管理器
	private ThinDownloadManager downloadManager;
	//下载的线程数
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 4;
    //下载请求序列
    private DownloadRequest request;
    //下载id
    private int downloadId = 0;
    
    //根据文件下载地址和文件名创建
    public InspurDownLoaderManager(String downloadUrl,String fileName){
    	createSDCardDir(fileName);
    	initDownload(downloadUrl, urlPath);
    }

    //根据文件名创建下载目录
    private String createSDCardDir(String name) {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath() + "/MUDOWN";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir + File.separator + name);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                }
                urlPath = file.getPath();
            }
        }
        return urlPath;
    }
    
    //初始化下载
    private void initDownload(String downloadUrl,String urlPath) {
    	
    	downloadManager = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);
        Uri downloadUri = Uri.parse(downloadUrl);
        Uri destinationUri = Uri.parse(urlPath);
        request = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri).setPriority(Priority.HIGH);
        

//                .addCustomHeader("Cookie", cookie);
    }
    
    //设置下载监听
    public void setDownloadListener(DownloadStatusListener l){
    	request.setDownloadListener(l);
    }
    
    //开始下载方法
    public void startDownLoad(){
    	if (downloadManager.query(downloadId) == DownloadManager.STATUS_NOT_FOUND) {
            downloadId = downloadManager.add(request);
        }else {
		}
    }

	public int getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(int downloadId) {
		this.downloadId = downloadId;
	}
    
    
}
