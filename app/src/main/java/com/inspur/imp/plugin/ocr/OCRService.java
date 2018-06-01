package com.inspur.imp.plugin.ocr;

import android.graphics.Bitmap;
import android.util.Base64;

import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.photograph.PhotoSDkApi;
import com.photograph.ui.OCROptCallBack;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by chenmch on 2018/6/1.
 */

public class OCRService extends ImpPlugin {

    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("startPhotoOCR".equals(action)) {
            startPhotoOCR(paramsObject);
        }
    }

    private void startPhotoOCR(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", null);
        failCb = JSONUtils.getString(paramsObject, "fail", null);
        String options = JSONUtils.getString(paramsObject, "options", "");
        int OCRType = JSONUtils.getInt(options, "OCRType", 1);
        JSONObject Json = new JSONObject();
        try {
            Json.put("ocrType", OCRType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PhotoSDkApi.init(getActivity()).StartPhotoOCR(Json, new OCROptCallBack() {
            @Override
            public void OCRBack(String result) {
                callbackResult(result);
            }

            @Override
            public void callBackMsg(boolean result) {
                if (result) {
                    failPicture(Res.getString("cancel"));
                }
            }
        });

    }

    private void callbackResult(String result) {
        String path = JSONUtils.getString(result, "path", "");
        File imgFile = new File(path);
        String js_out = "";
        if (imgFile.exists()) {
            Bitmap bitmap = ImageUtils.getBitmapByFile(imgFile);
            ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
            try {
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        originalJpeg_data)) {
                    byte[] code = originalJpeg_data.toByteArray();
                    byte[] output = Base64.encode(code, Base64.NO_WRAP);
                    js_out = new String(output);

                }
            } catch (Exception e) {
                e.printStackTrace();
               // failPicture(Res.getString("compress_error"));
            }
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("OCRResult", result);
            jsonObject.put("photoData", js_out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.jsCallback(successCb, jsonObject.toString());
    }

    /**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void failPicture(String err) {
        this.jsCallback(failCb, err);
    }


    @Override
    public void onDestroy() {

    }
}
