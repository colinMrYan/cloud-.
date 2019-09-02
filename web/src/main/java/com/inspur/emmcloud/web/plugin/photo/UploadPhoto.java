package com.inspur.emmcloud.web.plugin.photo;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;

import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadPhoto {
    private Context mContext;
    private OnUploadPhotoListener onUploadPhotoListener;

    public UploadPhoto(Context mContext, OnUploadPhotoListener onUploadPhotoListener) {
        this.mContext = mContext;
        this.onUploadPhotoListener = onUploadPhotoListener;
    }

    public void upload(String url, String filePath, int encodeType, String context, JSONObject watermarkObj) {
        List<String> filePathList = new ArrayList<String>();
        filePathList.add(filePath);
        createWatermark(watermarkObj, filePathList);
        httpUpload(filePathList, url, encodeType, context);
    }

    public void upload(String url, List<String> filePathList, int encodeType, String context, JSONObject watermarkObj) {
        createWatermark(watermarkObj, filePathList);
        httpUpload(filePathList, url, encodeType, context);
    }

    private void createWatermark(JSONObject watermarkObj, List<String> filePathList) {
        LogUtils.jasonDebug("watermarkObj=" + watermarkObj);
        if (watermarkObj != null) {
            String watermarkContent = JSONUtils.getString(watermarkObj, "content", null);
            String color = JSONUtils.getString(watermarkObj, "color", "#ffffff");
            int fontSize = JSONUtils.getInt(watermarkObj, "fontSize", 14);
            String background = JSONUtils.getString(watermarkObj, "background", "#00000000");
            String align = JSONUtils.getString(watermarkObj, "align", "left");
            String valign = JSONUtils.getString(watermarkObj, "valign", "top");
            for (int i = 0; i < filePathList.size(); i++) {
                String filePath = filePathList.get(i);
                ImageUtils.createWaterMask(mContext, filePath, watermarkContent, color, background, align, valign, fontSize);
            }
        }
    }

    private void httpUpload(List<String> filePathList, String url, int encodeType, String context) {
        RequestParams params = new RequestParams(url);
        params.setMultipart(true);
        for (int i = 0; i < filePathList.size(); i++) {
            File file = new File(filePathList.get(i));
            params.addBodyParameter("" + i, file, "application/json", file.getName());
        }
        String cookie = PreferencesUtils.getString(mContext, "web_cookie", "");
        params.addHeader("Cookie", cookie);
        params.addBodyParameter("encodeType", encodeType + "");
        params.addBodyParameter("context", context);
        params.setReadTimeout(300000);
        x.http().post(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub
                LogUtils.jasonDebug("fail---------");
                if (arg0 instanceof HttpException) {
                    HttpException httpEx = (HttpException) arg0;
                    String error = httpEx.getResult();
                    int responseCode = httpEx.getCode();
                    LogUtils.jasonDebug("error=" + error);
                    LogUtils.jasonDebug("responseCode=" + responseCode);
                }
                onUploadPhotoListener.uploadPhotoFail();

            }

            @Override
            public void onFinished() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(String arg0) {
                // TODO Auto-generated method stub
                LogUtils.jasonDebug("success=" + arg0);
                try {
                    onUploadPhotoListener.uploadPhotoSuccess(arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public interface OnUploadPhotoListener {
        void uploadPhotoSuccess(String result);

        void uploadPhotoFail();
    }
}
