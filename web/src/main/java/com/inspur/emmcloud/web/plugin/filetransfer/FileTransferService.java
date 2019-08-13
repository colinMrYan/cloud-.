package com.inspur.emmcloud.web.plugin.filetransfer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileManagerActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;
import com.inspur.emmcloud.web.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import static android.app.Activity.RESULT_OK;

public class FileTransferService extends ImpPlugin {
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";
    // 上传成功回传参数
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10 * 10000000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;
    private static final String SAVE_FILE = "save_file";
    // 文件
    private File file;
    private String downloadUrl = "", filepath = "", fileName = "", fileType = "",
            absoluteFilePath = "", reallyPath = ""; //reallyPath 文件的整体路径
    // 下载回调
    private String downloadSucCB, downloadFailCB, fileInfo, saveFileCallBack;
    // 上传文件参数
    private String uploadUrl = "", uploadpath = "", uploadName = "";
    // 上传回调
    private String uploadProgress, uploadSucCB, uploadFailCB;
    // 提示不含有sd卡
    private AlertDialog msgDlg;
    // 获取SDCard根目录
    private String sdcard = Environment.getExternalStorageDirectory() + "/";
    // 下载的返回值
    private String result;
    // 判断是否是下载文件
    private boolean flag;
    // 通知
    private Notification notification;
    // 通知栏ID
    private int notificationId;
    private NotificationManager nManager;
    private PendingIntent pendingIntent;
    // 停止下载
    private boolean stopConn = false;
    private AlertDialog fileDownloadDlg;
    private TextView ratioText;
    private long totalSize;
    private long downloadSize;
    /**
     * 记录进度条数量*
     */
    private int progress;
    private ProgressBar progressBar;
    private String downloadFileType = "";
    // 回传下载结果
    Handler handler = new Handler() {

        @SuppressLint("ShowToast")
        @SuppressWarnings("synthetic-access")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //下载中
                case 0:
                    progressBar.setProgress(progress);
                    if (totalSize <= 0) {
                        ratioText.setText(getFragmentContext().getString(Res.getStringID("has_downloaded"))
                                + setFormat(downloadSize));
                    } else {
                        String text = progress + "%" + "," + "  "
                                + setFormat(downloadSize) + "/"
                                + setFormat(totalSize);
                        ratioText.setText(text);
                    }
                    break;
                // 下载失败
                case 1:
                    if (fileDownloadDlg != null && fileDownloadDlg.isShowing()) {
                        fileDownloadDlg.dismiss();
                    }
                    ToastUtils.show(getFragmentContext(), Res.getStringID("download_fail"));
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (StrUtil.strIsNotNull(downloadFailCB)) {
                                jsCallback(downloadFailCB, fileName);
                            } else if (downloadFileType.equals(SAVE_FILE) && StrUtil.strIsNotNull(saveFileCallBack)) {
                                try {
                                    if (downloadFileType.equals(SAVE_FILE)) {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("path", reallyPath);
                                        jsonObject.put("status", 0);
                                        jsCallback(saveFileCallBack, jsonObject.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    });

                    break;
                // 下载文件成功
                case 2:
                    if (fileDownloadDlg != null && fileDownloadDlg.isShowing()) {
                        fileDownloadDlg.dismiss();
                    }
                    if (StrUtil.strIsNotNull(downloadSucCB)) {
                        new FileOpen(getActivity(), reallyPath, fileType).showOpenDialog();
                    }
                    fileInfo = (String) msg.obj;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (StrUtil.strIsNotNull(downloadSucCB)) {
                                String[] return_param = {fileInfo, fileName};
                                jsCallback(downloadSucCB, return_param);
                            } else if (downloadFileType.equals(SAVE_FILE) && StrUtil.strIsNotNull(saveFileCallBack)) {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("status", 1);
                                    jsonObject.put("errorMessage", "");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                jsCallback(saveFileCallBack, jsonObject.toString());
                            }
                        }
                    });
                    break;
                // 上传进度
                case 3:
                    int pro = (Integer) msg.obj;
                    jsCallback(uploadProgress, pro + "");
                    break;
                // 上传成功
                case 4:
                    String fileName = (String) msg.obj;
                    jsCallback(uploadSucCB, fileName);
                    break;
                // 上传失败
                case 5:
                    String filename = (String) msg.obj;
                    jsCallback(uploadFailCB, filename);
                    break;
            }
        }

    };
    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        switch (action) {
            case "upload":   // 上传文件
                upload(paramsObject);
                break;
            case "download": // 下载文件
                download(paramsObject);
                break;
            case "saveFile":
                saveFile(paramsObject);
                break;
            case "selectFile": // 选择文件
                selectFile(paramsObject);
                break;
            case "base64File":
                getFileBase64(paramsObject);
                break;
            case "writeFile":
                //写文件
                writeFile(paramsObject);
                break;
            case "readFile":
                //读文件
                readFile(paramsObject);
                break;
            case "deleteFile":
                //删除指定文件
                deleteFile(paramsObject);
                break;
            case "listFile":
                //列出在指定目录下的文件名
                listFile(paramsObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }
    }

    /**
     * 下载文件插件为新加一个方法
     *
     * @param paramsObject
     */
    private void saveFile(JSONObject paramsObject) {
        downloadFileType = SAVE_FILE;
        saveFileCallBack = JSONUtils.getString(paramsObject, "success", "");
        JSONObject jsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        downloadUrl = JSONUtils.getString(jsonObject, "url", "");
        fileName = JSONUtils.getString(jsonObject, "saveName", "");
        try {
            JSONObject jsonObjectParam = new JSONObject();
            jsonObject.put("url", downloadUrl);
            jsonObject.put("filePath", "/IMP-Cloud/download/");
            execute("download", jsonObjectParam);
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 列出文件夹中所有文件
     *
     * @param paramsObject
     */
    private void listFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String fileDicPath = JSONUtils.getString(optionsJsonObject, "directory", "");
        List<String> fileArrayList = FileUtils.getFileNamesInFolder(fileDicPath, false);
        List<String> folderArrayList = FileUtils.getFileFolderNamesInFolder(fileDicPath);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("folders", new JSONArray(folderArrayList));
            jsonObject.put("files", new JSONArray(fileArrayList));
        } catch (Exception e) {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), e.getMessage());
            e.printStackTrace();
        }
        jsCallback(JSONUtils.getString(paramsObject, "success", ""), jsonObject.toString());
    }

    /**
     * 删除文件或文件夹
     *
     * @param paramsObject
     */
    private void deleteFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String fileDeletePath = JSONUtils.getString(optionsJsonObject, "directory", "")
                + JSONUtils.getString(optionsJsonObject, "fileName", "");
        if (StrUtil.strIsNotNull(fileDeletePath)) {
            boolean isDel = FileUtils.deleteFile(fileDeletePath);
            jsCallback(isDel ? JSONUtils.getString(paramsObject, "success", "") :
                    JSONUtils.getString(paramsObject, "fail", ""));
        } else {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""));
        }
    }

    /**
     * 读文件
     *
     * @param paramsObject
     */
    private void readFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String fileReadPath = JSONUtils.getString(optionsJsonObject, "directory", "")
                + JSONUtils.getString(optionsJsonObject, "fileName", "");
        String readContent = FileUtils.readFile(fileReadPath, "utf-8").toString();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", readContent);
        } catch (Exception e) {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), e.getMessage());
            e.printStackTrace();
        }
        jsCallback(JSONUtils.getString(paramsObject, "success", ""), jsonObject);
    }

    /**
     * 写文件
     *
     * @param paramsObject
     */
    private void writeFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String fileSavePath = JSONUtils.getString(optionsJsonObject, "directory", "")
                + JSONUtils.getString(optionsJsonObject, "fileName", "");
        FileUtils.writeFile(fileSavePath
                , JSONUtils.getString(optionsJsonObject, "content", ""),
                JSONUtils.getBoolean(optionsJsonObject, "append", true));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path", fileSavePath);
        } catch (Exception e) {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), e.getMessage());
            e.printStackTrace();
        }
        jsCallback(JSONUtils.getString(paramsObject, "success", ""), jsonObject.toString());
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        String result = "";
        // 下载文件
        if ("download".equals(action)) {
            download(paramsObject);
        } else if ("downloadFile".equals(action)) { // 为了兼容自定义的imp插件
            if (!paramsObject.isNull("key")){
                try {
                    String key = paramsObject.getString("key");
                    //key = "http://10.24.14.63:8080/test/inspur_cloud_mobileclient_1.0.0.apk";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("url", key);
                    jsonObject.put("filePath", "/IMP-Cloud/download/");
                    execute("download", jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                }
            }
        } else {
            showCallIMPMethodErrorDlg();
        }

        return result;
    }

    // 下载
    private void download(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("url")) {
                downloadUrl = jsonObject.getString("url");
            }
            if (!jsonObject.isNull("filePath")) {
                filepath = jsonObject.getString("filePath");
            }
            if (!jsonObject.isNull("fileName")) {
                fileName = jsonObject.getString("fileName");
            }
            if (!jsonObject.isNull("flag")) {
                flag = jsonObject.getBoolean("flag");
            }
            if (!jsonObject.isNull("successCallback")) {
                downloadSucCB = jsonObject.getString("successCallback");
            }
            if (!jsonObject.isNull("errorCallback")) {
                downloadFailCB = jsonObject.getString("errorCallback");
            }
            filepath = "/IMP-Cloud/download/";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 判断是否含有sd卡
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(getFragmentContext(), Res.getString("baselib_sd_not_exist"));
            return;
        }
        // 判断网络是否连接
        if (!NetUtils.isNetworkConnected(getFragmentContext())) {
            ToastUtils.show(getFragmentContext(), Res.getString("network_exception"));
            return;
        }
        // 文件存放路径
        filepath = sdcard + filepath;
        File tmpFile = new File(filepath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        // 判断是否有分隔符
        absoluteFilePath = tmpFile.getAbsolutePath();
        if (!absoluteFilePath.endsWith("/")) {
            absoluteFilePath = absoluteFilePath + "/";
        }

        downloadUrl = downloadUrl.trim();
        downloadUrl = StrUtil.changeUrl(downloadUrl);
        showDownloadStatus();
    }

    // 下载
    private void selectFile(JSONObject jsonObject) {
        successCb = JSONUtils.getString(jsonObject, "success", "");
        failCb = JSONUtils.getString(jsonObject, "failCb", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
        int maximum = JSONUtils.getInt(optionsObj, "maximum", 1);
        if (maximum == 0) {
            maximum = 200;
        }
        String fileType = JSONUtils.getString(optionsObj, "fileType", "");

        ArrayList<String> fileTypeList = new ArrayList<>();
        if (!StringUtils.isBlank(fileType)) {
            String[] fileTypes = fileType.split("\\|");
            for (int i = 0; i < fileTypes.length; i++) {
                fileTypeList.add(fileTypes[i]);
                LogUtils.jasonDebug("fileType=" + fileTypes[i]);
            }
        }
        Intent intent = new Intent(getActivity(), FileManagerActivity.class);
        intent.putExtra(FileManagerActivity.EXTRA_MAXIMUM, maximum);
        intent.putStringArrayListExtra(FileManagerActivity.EXTRA_FILTER_FILE_TYPE, fileTypeList);
        getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.SELECT_FILE_SERVICE_REQUEST);
    }

    private void getFileBase64(JSONObject jsonObject) {
        successCb = JSONUtils.getString(jsonObject, "success", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
        String filePath = JSONUtils.getString(optionsObj, "filePath", "");
        if (!StringUtils.isBlank(filePath)) {
            String result = "";
            try {
                result = FileUtils.encodeBase64File(filePath);
                callbackSuccess(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ImpFragment.SELECT_FILE_SERVICE_REQUEST:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> pathList = data.getStringArrayListExtra("pathList");
                    JSONArray array = new JSONArray();
                    for (String path : pathList) {
                        File file = new File(path);
                        if (file.exists()) {
                            JSONObject object = new JSONObject();
                            try {
                                object.put("name", file.getName());
                                object.put("size", FileUtils.getFileSize(path));
                                object.put("path", path);
                                object.put("md5", FileUtils.getFileMD5(file));
                                array.put(object);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    callbackSuccess(array.toString());
                } else {
                    callbackFail(Res.getString("cancel_select"));
                }
                break;
        }

    }

    private void callbackSuccess(String result) {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb, result);
        }
    }

    private void callbackFail(String result) {
        if (!StringUtils.isBlank(failCb)) {
            this.jsCallback(failCb, result);
        }
    }

    /**
     * 弹出提示SD卡不存在的对话框
     */
    private void showDialog() {
        // 创建退出对话框
        msgDlg = new AlertDialog.Builder(getActivity()).create();
        // 设置对话框标题
        msgDlg.setTitle("温馨提示");
        // 设置对话框消息
        msgDlg.setMessage("内存卡不存在！");
        // 添加选择按钮并注册监听
        msgDlg.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                msgDlg.dismiss();
            }
        });
        // 显示对话框
        msgDlg.show();
    }

    //显示下载进度框
    private void showDownloadStatus() {
        // TODO Auto-generated method stub
        stopConn = false;
        LayoutInflater layoutInflater = LayoutInflater.from(getFragmentContext());
        View view = layoutInflater
                .inflate(Res.getLayoutID("web_filetransfer_dialog_file_download_progress")
                        ,
                        null);
        ratioText = (TextView) view.findViewById(Res.getWidgetID("ratio_text"));
        progressBar = (ProgressBar) view.findViewById(Res.getWidgetID("update_progress"));

        fileDownloadDlg = new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog)
                .setTitle(Res.getStringID("file_downloading"))
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(Res.getStringID("cancel"),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                fileDownloadDlg.dismiss();
                                // 设置取消状态
                                stopConn = true;
                                if (downloadFileType.equals(SAVE_FILE)) {
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("status", 2);
                                        jsonObject.put("errorMessage", "");
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                    jsCallback(saveFileCallBack, jsonObject.toString());
                                }
                            }
                        }).create();
        fileDownloadDlg.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        fileDownloadDlg.show();
        // 下载文件
        Thread thread = new Thread(new UpdateRunnable());
        thread.start();
    }

    /**
     * 下载文件
     * 文件名先以指定的文件名为准，如果没有指定文件名则以getFileName方法里返回的为准
     *
     * @param urlString 下载的url
     * @return 返回文件下载的详细路径
     * @throws Exception
     */
    private void downLoadFile(String urlString) {
        // 已下载的大小
        long downnum = 0;
        // 下载百分比
        int downcount = 0;
        URL url = null;
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        OutputStream outputStream = null;
        try {
            LogUtils.jasonDebug("是否主线程==" + (Looper.myLooper() == Looper.getMainLooper()));
            //替换空格
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            String filename = getFileName(urlConnection);
            filename = URLDecoder.decode(filename, "UTF-8");   //防止文件名乱码
            String[] array = filename.split("\\.");
            int arrayLength = array.length;
            fileType = array[arrayLength - 1];
            if (!StrUtil.strIsNotNull(fileType)) {
                fileType = urlConnection.getContentType();
            }
            if (StrUtil.strIsNotNull(fileName)) {
                file = new File(absoluteFilePath + fileName);
            } else {
                file = new File(absoluteFilePath + filename);
            }
            reallyPath = file.getAbsolutePath();
            long length = urlConnection.getContentLength();
            totalSize = length;
            if (urlConnection.getResponseCode() >= 400) {
                handler.sendEmptyMessage(1);
                return;
            } else {
                inputStream = urlConnection.getInputStream();
                outputStream = new FileOutputStream(file);
                byte buffer[] = new byte[1024];
                int readsize = 0;
                while (!stopConn && (readsize = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readsize);
                    downnum += readsize;
                    if ((downcount == 0)
                            || (int) (downnum * 100 / length) - 1 > downcount) {
                        downcount += 1;
                        progress = (int) (downnum * 100 / (float) length);
                        downloadSize = downnum;
                        // 更新进度
                        if (fileDownloadDlg != null
                                && fileDownloadDlg.isShowing()) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                }
                if (!stopConn) {
                    // 下载成功，将下载成功的文件相关信息返回前台
                    Message msg = handler.obtainMessage(2);
                    msg.obj = file.getPath();
                    handler.sendMessage(msg);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(1);
        } finally {
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * url里获取文件名
     * 先以头部filename为准
     * 没有则取connection里的名字
     * 再没有则生成一个.tmp名字
     *
     * @param urlConnection
     * @return
     */
    private String getFileName(HttpURLConnection urlConnection) {
        String filename = null;
        String headField = urlConnection.getHeaderField("Content-Disposition");
        if (!StringUtils.isBlank(headField)) {
            headField = headField.toLowerCase();
            if (headField.contains("filename=")) {
                //有些下载链接检测到的文件名带双引号，此处给去掉
                try {
                    filename = headField.split("filename=")[1];
                    if (filename.length() > 2 && filename.startsWith("\"") && filename.endsWith("\"")) {
                        filename = filename.substring(1, filename.length() - 1);
                    }
                    return filename;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        filename = urlConnection.getURL().getFile();
        if (filename.contains("/")) {
            String[] array = filename.split("/");
            filename = array[array.length - 1];

        }
        if (StringUtils.isBlank(filename)) {
            // 默认取一个文件名
            filename = UUID.randomUUID() + ".tmp";
        }
        return filename;
    }

    /**
     * 上传文件
     *
     * @param jsonObject
     */
    private void upload(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("url"))
                uploadUrl = jsonObject.getString("url");
            if (!jsonObject.isNull("filePath"))
                uploadpath = jsonObject.getString("filePath");
            if (!jsonObject.isNull("fileName"))
                uploadName = jsonObject.getString("fileName");
            if (!jsonObject.isNull("progressCallback"))
                uploadProgress = jsonObject.getString("progressCallback");
            if (!jsonObject.isNull("successCallback"))
                uploadSucCB = jsonObject.getString("successCallback");
            if (!jsonObject.isNull("errorCallback"))
                uploadFailCB = jsonObject.getString("errorCallback");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 上传的文件
        file = new File(uploadpath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadFile(file);
            }

        }).start();
    }

    /**
     * 上传文件
     *
     * @param
     * @return 上传结果
     */
    private String uploadFile(File uploadfile) {
        // 上传文件的大小
        int downnum = 0;
        // 上传百分比
        int downcount = 0;
        // 边界标识 随机生成
        String BOUNDARY = UUID.randomUUID().toString();
        // 结束标识
        String PREFIX = "--", LINE_END = "\r\n";
        // 内容类型
        String CONTENT_TYPE = "multipart/form-data";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection conn = null;
            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection httpsConn = null;
                httpsConn = (HttpsURLConnection) url
                        .openConnection();
                MyX509TrustManager xtm = new MyX509TrustManager();

                MyHostnameVerifier hnv = new MyHostnameVerifier();
                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance("TLS"); //或SSL
                X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};
                sslContext.init(null, xtmArray, new java.security.SecureRandom());
                if (sslContext != null) {
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                }
                HttpsURLConnection.setDefaultHostnameVerifier(hnv);
                conn = httpsConn;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            // 允许输入流
            conn.setDoInput(true);
            // 允许输出流
            conn.setDoOutput(true);
            // 不允许使用缓存
            conn.setUseCaches(false);
            // 请求方式
            conn.setRequestMethod("POST");
            // 设置编码
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                    + BOUNDARY);
            if (uploadfile != null) {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                OutputStream outputSteam = conn.getOutputStream();

                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件 filename是文件的名字
                 */

                sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""
                        + encode(uploadfile.getName()) + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(uploadfile);
                byte[] bytes = new byte[1024];
                int len = 0;
                int fileLength = (int) uploadfile.length();
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                    downnum += len;
                    if ((downcount == 0)
                            || downnum * 100 / fileLength - 1 > downcount) {
                        downcount += 1;
                        Message msg = handler.obtainMessage(3);
                        msg.obj = downnum * 100 / fileLength;
                        handler.sendMessage(msg);
                    }
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                if (res == 200) {
                    // 上传成功
                    Message msg = handler.obtainMessage(4);
                    msg.obj = uploadName;
                    handler.sendMessage(msg);
                    return SUCCESS;
                }
            }
        } catch (Exception e) {
            // 回调上传失败方法
            Message msg = handler.obtainMessage(5);
            msg.obj = uploadName;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
        return SUCCESS;
    }

    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式化数据
     **/
    private String setFormat(long data) {
        // TODO Auto-generated method stub
        Format format = new DecimalFormat(("####0.00"));
        if (data < 1024) {
            return data + "B";
        } else if (data < 1024 * 1024) {
            return format.format(data / KBDATA) + "KB";
        } else {
            return format.format(data / MBDATA) + "MB";
        }
    }

    @Override
    public void onDestroy() {

    }

    /**
     * 下载文件线程
     */
    class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            Looper.prepare();
            try {
                downLoadFile(downloadUrl);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(1);
            }
            Looper.loop();
        }
    }

}
