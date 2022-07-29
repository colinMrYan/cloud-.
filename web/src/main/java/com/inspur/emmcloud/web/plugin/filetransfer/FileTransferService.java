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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.Base64Utils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.UrlParseUtils;
import com.inspur.emmcloud.web.bean.WebFileDownloadBean;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileManagerActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;
import com.inspur.emmcloud.web.ui.WebFileDownloadActivity;
import com.inspur.emmcloud.web.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * FileTransferService中下载，读取，写入内容，列出文件列表，删除文件等新加插件传的路径都认为是相对路径
 * 相对路径前无“/”
 */
public class FileTransferService extends ImpPlugin {
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";
    // 上传成功回传参数
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10 * 10000000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static final String SAVE_FILE = "save_file";
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;
    // 文件
    private File file;
    private String downloadUrl = "", filepath = "", fileName = "", fileType = "",
            absoluteFilePath = "", reallyPath = ""; //reallyPath 文件的整体路径
    // 下载回调
    private String downloadSucCB, downloadFailCB, fileInfo, saveFileCallBack, blockImageCallBack;
    // 上传文件参数
    private String uploadUrl = "", uploadName = "";
    // 上传回调
    private String uploadSucCB, uploadFailCB;
    // 提示不含有sd卡
    private AlertDialog msgDlg;
    // 下载的返回值
    private String result;
    // 判断是否是下载文件
    private boolean flag;

    private String headerObj;
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
    private AlertDialog fileUploadDlg;
    /**
     * 记录进度条数量*
     */
    private int progress;
    private ProgressBar progressBar;
    private String downloadFileType = "";
    private boolean needOpenFile = true;
    private String replaceBasePath = FilePathUtils.BASE_PATH + "/";
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
                        if (saveFileCallBack != null) {
                            try {
                                JSONObject json = new JSONObject();
                                json.put("state", 1);
                                JSONObject result = new JSONObject();
                                result.put("progress", progress);
                                json.put("result", result);
                                jsCallback(saveFileCallBack, json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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
                                        jsonObject.put("state", 0);
                                        jsonObject.put("errorMessage", "");
                                        jsCallback(saveFileCallBack, jsonObject);
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
                    if (needOpenFile) {
                        new FileOpen(getActivity(), reallyPath, fileType).openFile();
                        // 不再显示下载完成提示框。与IOS端保持一致
//                        if (getActivity() != null) {
//                            new FileOpen(getActivity(), reallyPath, fileType).showOpenDialog();
//                        }
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
                                    jsonObject.put("state", 1);
                                    jsonObject.put("path", reallyPath.replace(replaceBasePath, ""));
                                    JSONObject result = new JSONObject();
                                    result.put("result", file.getPath());
                                    result.put("fileSize", totalSize);
                                    result.put("type", fileType);
                                    result.put("name", fileName);
                                    jsonObject.put("result", result);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                jsCallback(saveFileCallBack, jsonObject);
                            }
                        }
                    });
                    break;
                // 上传进度
                case 3:
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
    private long fileSize;
    private String fileId; // 文件ID
    private String createTime; // 文件创建时间
    private boolean isUploadingFile = false;

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
            //分段上传文件
            case "getBlockLocalImg":
            case "getBlockLocalVideo":
                getBlockLocalImg(paramsObject);
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
        needOpenFile = JSONUtils.getBoolean(jsonObject, "autoOpen", true);
        fileSize = JSONUtils.getLong(jsonObject, "fileSize", 0);
        createTime = JSONUtils.getString(jsonObject, "createTime", "");
        fileId = JSONUtils.getString(jsonObject, "fileId", "");
        try {
            JSONObject jsonObjectParam = new JSONObject();
            jsonObject.put("url", downloadUrl);
            jsonObject.put("filePath", FilePathUtils.BASE_PATH);
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
        String folderName = JSONUtils.getString(optionsJsonObject, "directory", "");
        folderName = FilePathUtils.getRealPath(folderName);
        List<String> fileArrayList = FileUtils.getFileNamesInFolder(folderName, false);
        List<String> folderArrayList = FileUtils.getFileFolderNamesInFolder(folderName);
        JSONObject jsonObject = new JSONObject();
        List<String> fileArrayListFilter = new ArrayList<>();
        List<String> folderArrayListFilter = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            fileArrayListFilter.add(fileArrayList.get(i).replace(replaceBasePath, ""));
        }
        for (int i = 0; i < folderArrayList.size(); i++) {
            folderArrayListFilter.add(folderArrayList.get(i).replace(replaceBasePath, ""));
        }
        try {
            jsonObject.put("folders", new JSONArray(folderArrayListFilter));
            jsonObject.put("files", new JSONArray(fileArrayListFilter));
            jsCallback(JSONUtils.getString(paramsObject, "success", ""), jsonObject);
        } catch (Exception e) {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), getErrorJson(e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 列出文件夹中所有文件
     *
     * @param paramsObject
     */
    private void getBlockLocalImg(JSONObject paramsObject) {
        if (isUploadingFile) return;
        isUploadingFile = true;
        blockImageCallBack = JSONUtils.getString(paramsObject, "success", "");
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        int blockSize = JSONUtils.getInt(optionsJsonObject, "blockSize", 4096);
        // 绝对路径、相对路径
        int persistentStorage = JSONUtils.getInt(optionsJsonObject, "persistentStorage", 0);
        String filePath = JSONUtils.getString(optionsJsonObject, "filePath", "");
        uploadFileInBlock(filePath, blockSize);
    }

    /**
     * @param offset    偏移量
     * @param blockSize 每块的大小
     * @return 这一片的数据
     */
    private String getBlock(int offset, int blockSize, String base64Str) {
        return base64Str.substring(offset, blockSize);
    }

    private void uploadFileInBlock(String filePath, int blockSize) {
        // imp方法接受文件名称，
        String base64Stream = null;
        try {
            base64Stream = Base64Utils.encodeBase64File(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!StringUtils.isEmpty(base64Stream)) {
            //计算文件分片的总块数
            long totalBlock = base64Stream.length() / blockSize + (base64Stream.length() % blockSize > 0 ? 1 : 0);
            //包含上传可能是0长度但有内容的文件
            uploadFileInBlock(base64Stream, Math.max(totalBlock, 1), 1, blockSize);
            isUploadingFile = false;
        } else {
            //不存在则回调错误方法
            try {
                JSONObject json = new JSONObject();
                json.put("state", 0);
                jsCallback(blockImageCallBack, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isUploadingFile = false;
        }
    }

    private void uploadFileInBlock(String base64Stream, long totalBlock, int currentBlock, int PART_SIZE) {
        String uploadStream = base64Stream;
        if (currentBlock == totalBlock) {
            uploadStream = uploadStream.substring((currentBlock - 1) * PART_SIZE, base64Stream.length());
        } else {
            uploadStream = uploadStream.substring((currentBlock - 1) * PART_SIZE, currentBlock * PART_SIZE);
        }
        if (StringUtils.isEmpty(uploadStream) && currentBlock != totalBlock) {
            try {
                JSONObject json = new JSONObject();
                json.put("state", 0);
                jsCallback(blockImageCallBack, json);
            } catch (JSONException e) {
                isUploadingFile = false;
                e.printStackTrace();
            }
            return;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("state", 1);
            JSONObject result = new JSONObject();
            result.put("currentContent", uploadStream);
            result.put("allBlockSize", totalBlock);
            result.put("currentBlockNum", currentBlock);
            json.put("result", result);
            jsCallback(blockImageCallBack, json);
            if (currentBlock == totalBlock) return;
            uploadFileInBlock(base64Stream, totalBlock, currentBlock + 1, PART_SIZE);
        } catch (Exception e1) {
            e1.printStackTrace();
            isUploadingFile = false;
        }
    }

    /**
     * 删除文件或文件夹
     *
     * @param paramsObject
     */
    private void deleteFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String folderName = JSONUtils.getString(optionsJsonObject, "directory", "");
        String fileName = JSONUtils.getString(optionsJsonObject, "fileName", "");
        folderName = FilePathUtils.getRealPath(folderName);
        String relativePath = new File(folderName, fileName).getPath();
        if (FilePathUtils.isSafePath(relativePath)) {
            boolean isDel = FileUtils.deleteFile(relativePath);
            jsCallback(isDel ? JSONUtils.getString(paramsObject, "success", "") :
                    JSONUtils.getString(paramsObject, "fail", ""), "");
        } else {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), getErrorJson(""));
        }
    }

    /**
     * 读文件
     * 不需要关心是否在相对目录下
     *
     * @param paramsObject
     */
    private void readFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String folderName = JSONUtils.getString(optionsJsonObject, "directory", "");
        String fileName = JSONUtils.getString(optionsJsonObject, "fileName", "");
        folderName = FilePathUtils.getRealPath(folderName);
        String relativePath = new File(folderName, fileName).getPath();
        String readContent = FileUtils.readFile(relativePath, "utf-8").toString();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", readContent);
            jsCallback(JSONUtils.getString(paramsObject, "success", ""), jsonObject);
        } catch (Exception e) {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), getErrorJson(e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 组装错误信息
     *
     * @param message
     * @return
     */
    private JSONObject getErrorJson(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("errorMessage", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 写文件
     * 需要关心文件路径是否在相对目录下
     *
     * @param paramsObject
     */
    private void writeFile(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());

        String folderName = JSONUtils.getString(optionsJsonObject, "directory", "");
        String fileName = JSONUtils.getString(optionsJsonObject, "fileName", "");
        folderName = FilePathUtils.getRealPath(folderName);
        String relativePath = new File(folderName, fileName).getPath();
        if (FilePathUtils.isSafePath(relativePath)) {
            FileUtils.writeFile(relativePath, JSONUtils.getString(optionsJsonObject, "content", ""),
                    JSONUtils.getBoolean(optionsJsonObject, "append", true));
            jsCallback(JSONUtils.getString(paramsObject, "success", ""));
        } else {
            jsCallback(JSONUtils.getString(paramsObject, "fail", ""), getErrorJson("file path error"));
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        String result = "";
        // 下载文件
        if ("download".equals(action)) {
            download(paramsObject);
        } else if ("downloadFile".equals(action)) { // 为了兼容自定义的imp插件
            if (!paramsObject.isNull("key")) {
                try {
                    String key = paramsObject.getString("key");
                    //key = "http://10.24.14.63:8080/test/inspur_cloud_mobileclient_1.0.0.apk";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("url", key);
                    jsonObject.put("filePath", FilePathUtils.BASE_PATH);
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
            if (!jsonObject.isNull("headers")) {
                JSONObject headerJsonObject = JSONUtils.getJSONObject(jsonObject, "headers", null);
                headerObj = headerJsonObject.toString();
            }
//            filepath = "/IMP-Cloud/download/";
            filepath = FilePathUtils.BASE_PATH;
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
//        filepath = sdcard + filepath;
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
        // 返回ID的应用则跳转到本地下载页面，否则执行原有逻辑
        if (TextUtils.isEmpty(fileId)) {
            showDownloadStatus();
        } else {
            Intent intent = new Intent(getFragmentContext(), WebFileDownloadActivity.class);
            WebFileDownloadBean webFileDownloadBean = new WebFileDownloadBean(fileId, fileSize, createTime, downloadUrl, fileName, headerObj);
            intent.putExtra("webFileDownload", webFileDownloadBean);
            getFragmentContext().startActivity(intent);
        }
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
        failCb = JSONUtils.getString(jsonObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
        String filePath = JSONUtils.getString(optionsObj, "filePath", "");
        filePath = FilePathUtils.getRealPath(filePath);
        if (!StringUtils.isBlank(filePath)) {
            String result = "";
            try {
                result = FileUtils.encodeBase64File(filePath);
                callbackSuccess(result);
            } catch (Exception e) {
                callbackFail(e.getMessage());
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
                                object.put("path", FilePathUtils.SDCARD_PREFIX + path);
                                object.put("md5", FileUtils.getFileMD5(file));
                                array.put(object);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    callbackSuccess(array);
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

    private void callbackSuccess(JSONArray array) {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb, array);
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
                .inflate(Res.getLayoutID("web_filetransfer_dialog_file_download_progress"), null);
        ratioText = (TextView) view.findViewById(Res.getWidgetID("ratio_text"));
        progressBar = (ProgressBar) view.findViewById(Res.getWidgetID("update_progress"));
        fileDownloadDlg = new AlertDialog.Builder(getActivity(), PreferencesUtils.getInt(getFragmentContext(),
                "app_theme_num_v1", 0) != 3 ? android.R.style.Theme_Holo_Light_Dialog : AlertDialog.THEME_HOLO_DARK)
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
                                        jsonObject.put("state", 2);
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
            String cookie = PreferencesUtils.getString(getFragmentContext(), "web_cookie", "");
            if (!StringUtils.isBlank(cookie)) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }
            String filename = getFileName(urlConnection, urlString);
            filename = URLDecoder.decode(filename, "UTF-8");   //防止文件名乱码
            String[] array = filename.split("\\.");
            int arrayLength = array.length;
            fileType = array[arrayLength - 1];
            if (!StrUtil.strIsNotNull(fileType)) {
                fileType = urlConnection.getContentType();
            }
            clearFiles(filename);
            if (StrUtil.strIsNotNull(fileName)) {
                file = new File(absoluteFilePath + fileName);
            } else {
                file = new File(absoluteFilePath + filename);
            }
            reallyPath = file.getAbsolutePath();
            long length = urlConnection.getContentLength();
            totalSize = length;
            if (urlConnection.getResponseCode() >= 400) {
                LogUtils.YfcDebug("异常：" + urlConnection.getResponseCode() + "---" + urlConnection.getResponseMessage());
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
     * 清除文件
     *
     * @param fileName
     */
    private void clearFiles(String fileName) {
        String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);
        String fileExtension = FileUtils.getFileExtension(fileName);
        FileUtils.deleteFile(StrUtil.strIsNotNull(fileName) ? (absoluteFilePath + fileName) : (absoluteFilePath + fileName));
        if (fileExtension.contains("db")) {
            FileUtils.deleteFile(absoluteFilePath + fileNameWithoutExtension + "." + fileExtension + "-shm");
            FileUtils.deleteFile(absoluteFilePath + fileNameWithoutExtension + "." + fileExtension + "-wal");
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
    private String getFileName(HttpURLConnection urlConnection, String urlConnectString) {
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
                    filename = filename.replaceAll("/", "_");
                    return filename;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            filename = new URL(UrlParseUtils.getUrlHostAndPath(urlConnectString)).getFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filename.contains("/")) {
            String[] array = filename.split("/");
            filename = array[array.length - 1];

        }
        if (StringUtils.isBlank(filename)) {
            // 默认取一个文件名
            filename = UUID.randomUUID() + ".tmp";
        }
        filename = filename.replaceAll("/", "_");
        return filename;
    }

    /**
     * 上传文件
     *
     * @param jsonObject
     */
    private void upload(JSONObject jsonObject) {
        uploadSucCB = JSONUtils.getString(jsonObject, "success", "");
        uploadFailCB = JSONUtils.getString(jsonObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
        uploadUrl = JSONUtils.getString(optionsObj, "url", "");
        boolean isShowProgress = JSONUtils.getBoolean(optionsObj, "showProgress", false);
        JSONArray fileArray = JSONUtils.getJSONArray(optionsObj, "files", new JSONArray());
        List<String> uploadPathList = new ArrayList<>();
        for (int i = 0; i < fileArray.length(); i++) {
            JSONObject fileObj = JSONUtils.getJSONObject(fileArray, i, new JSONObject());
            String filePath = JSONUtils.getString(fileObj, "filePath", "");
            filePath = FilePathUtils.getRealPath(filePath);
            uploadPathList.add(filePath);
        }
        JSONObject dataObj = JSONUtils.getJSONObject(optionsObj, "data", null);
        JSONObject headerObj = JSONUtils.getJSONObject(optionsObj, "header", null);
        showFileUploadDlg(uploadPathList, isShowProgress, dataObj, headerObj);

    }

    private void showFileUploadDlg(List<String> uploadPathList, final boolean isShowProgress, JSONObject dataObj, JSONObject headerObj) {
        RequestParams params = new RequestParams(uploadUrl);
        params.setConnectTimeout(30000);
        params.setMultipart(true);
        if (dataObj != null) {
            Iterator<String> keys = dataObj.keys();
            while (keys.hasNext()) {
                try {
                    String key = keys.next();
                    params.addBodyParameter(key, dataObj.getString(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        if (headerObj != null) {
            Iterator<String> keys = headerObj.keys();
            while (keys.hasNext()) {
                try {
                    String key = keys.next();
                    params.addHeader(key, headerObj.getString(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < uploadPathList.size(); i++) {
            File file = new File(uploadPathList.get(i));
            params.addBodyParameter("" + i, file);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getFragmentContext());
        View view = layoutInflater
                .inflate(Res.getLayoutID("web_filetransfer_dialog_file_download_progress"), null);
        final TextView ratioText = view.findViewById(Res.getWidgetID("ratio_text"));
        final ProgressBar progressBar = view.findViewById(Res.getWidgetID("update_progress"));

        final Callback.Cancelable cancelable = x.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isShowProgress) {
                    int progress = (int) (current * 100 / total);
                    progressBar.setProgress(progress);
                    if (total <= 0) {
                        ratioText.setText(getFragmentContext().getString(Res.getStringID("has_uploaded"))
                                + setFormat(current));
                    } else {
                        String text = progress + "%" + "," + "  "
                                + setFormat(current) + "/"
                                + setFormat(total);
                        ratioText.setText(text);
                    }
                }

            }

            @Override
            public void onSuccess(String result) {
                callbackFileUploadSuccess(result);
                if (fileDownloadDlg != null) {
                    fileDownloadDlg.dismiss();
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                callbackFileUploadFail(1, throwable.getMessage());
                if (fileDownloadDlg != null) {
                    fileDownloadDlg.dismiss();
                }
            }

            @Override
            public void onCancelled(CancelledException e) {
                callbackFileUploadFail(2, "取消上传！");
                if (fileDownloadDlg != null) {
                    fileDownloadDlg.dismiss();
                }
            }

            @Override
            public void onFinished() {

            }
        });
        fileUploadDlg = new AlertDialog.Builder(getActivity(), PreferencesUtils.getInt(getFragmentContext(),
                "app_theme_num_v1", 0) != 3 ? android.R.style.Theme_Holo_Light_Dialog : AlertDialog.THEME_HOLO_DARK)
                .setTitle(Res.getStringID("file_uploading"))
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(Res.getStringID("cancel"),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                                cancelable.cancel();
                            }
                        }).create();
        fileUploadDlg.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        if (isShowProgress) {
            fileUploadDlg.show();
        }
    }

    private void callbackFileUploadSuccess(String result) {
        if (!StringUtils.isBlank(uploadSucCB)) {
            JSONObject obj = JSONUtils.getJSONObject(result);
            this.jsCallback(uploadSucCB, obj);
        }
    }

    private void callbackFileUploadFail(int status, String errorMessage) {
        if (!StringUtils.isBlank(uploadFailCB)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("state", status);
                obj.put("errorMessage", errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.jsCallback(uploadFailCB, obj);
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
                LogUtils.YfcDebug("下载异常：" + e.getMessage());
                e.printStackTrace();
                handler.sendEmptyMessage(1);
            }
            Looper.loop();
        }
    }

}
