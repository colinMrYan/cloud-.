package com.inspur.emmcloud.web.plugin.ocr;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/6/1.
 */

public class OCRService extends ImpPlugin {

    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("startPhotoOCR".equals(action)) {
            startPhotoOCR(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    private void startPhotoOCR(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", null);
        failCb = JSONUtils.getString(paramsObject, "fail", null);
        String options = JSONUtils.getString(paramsObject, "options", "");
        int OCRType = JSONUtils.getInt(options, "OCRType", 1);
//        JSONObject Json = new JSONObject();
//        try {
//            Json.put("ocrType", OCRType);
//            Json.put("isBack", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        PhotoSDkApi.init(getFragmentContext()).StartPhotoOCR(Json, new OCROptCallBack() {
//            @Override
//            public void OCRBack(String result) {
//                callbackResult(result);
//            }
//
//            @Override
//            public void callBackMsg(boolean result) {
//                if (result) {
//                    failPicture(Res.getString("cancel"));
//                }
//            }
//        });

    }
//
//    private void callbackResult(String result) {
//        String path = JSONUtils.getString(result, "path", "");
//        File imgFile = new File(path);
//        String js_out = "";
//        if (imgFile.exists()) {
//            Bitmap bitmap = ImageUtils.getBitmapByFile(imgFile);
//            ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
//            try {
//                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
//                        originalJpeg_data)) {
//                    byte[] code = originalJpeg_data.toByteArray();
//                    byte[] output = Base64.encode(code, Base64.NO_WRAP);
//                    js_out = new String(output);
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//               // failPicture(Res.getString("compress_error"));
//            }
//        }
//        JSONObject jsonObject = new JSONObject();
//        try {
//            JSONObject object = new JSONObject(result);
//            jsonObject.put("OCRResult", object);
//            jsonObject.put("photoData", js_out);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        this.jsCallback(successCb, jsonObject.toString());
//    }

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
