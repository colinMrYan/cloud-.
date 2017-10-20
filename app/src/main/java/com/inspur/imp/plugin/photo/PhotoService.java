package com.inspur.imp.plugin.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.DataCleanManager;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.camera.Bimp;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.camera.editimage.EditImageActivity;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class PhotoService extends ImpPlugin {

    private static final int RESULT_CAMERA = 1;
    private static final int RESULT_GELLERY = 2;
    private String successCb, failCb;
    private JSONObject paramsObject;
    private String takePhotoImgPath = "";

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        this.paramsObject = paramsObject;
        if ("selectAndUpload".equals(action)) {
            selectAndUpload();
        }
        if ("takePhotoAndUpload".equals(action)) {
            takePhotoAndUpload();
        }
    }

    private void selectAndUpload() {
        // TODO Auto-generated method stub
        try {
            if (!paramsObject.isNull("success"))
                successCb = paramsObject.getString("success");
            if (!paramsObject.isNull("fail"))
                failCb = paramsObject.getString("fail");

            int picTotal = 6;
            if (!paramsObject.isNull("options")) {
                JSONObject obj = paramsObject.getJSONObject("options");
                if (obj.has("picTotal")) {
                    picTotal = obj.getInt(
                            "picTotal");
                }
            }
            openGallery(picTotal);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    private void openGallery(int picTotal) {
        // TODO Auto-generated method stub
        PublicWay.uploadPhotoService = this;
        initImagePicker(picTotal);
        Intent intent = new Intent(context, ImageGridActivity.class);
        intent.putExtra("paramsObject", paramsObject.toString());
        ((Activity) context).startActivityForResult(intent, RESULT_GELLERY);
    }

    /**
     * 初始化图片选择控件
     */
    private void initImagePicker(int picTotal) {
        LogUtils.jasonDebug("picTotal=" + picTotal);
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(ImageDisplayUtils.getInstance()); // 设置图片加载器
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        if (picTotal < 0 || picTotal > 6) {
            picTotal = 6;
        }
        imagePicker.setSelectLimit(picTotal);
        imagePicker.setMultiMode(true);
    }

    private void takePhotoAndUpload() {
        // TODO Auto-generated method stub
        try {
            LogUtils.jasonDebug("paramsObject=" + paramsObject.toString());
            if (!paramsObject.isNull("success"))
                successCb = paramsObject.getString("success");
            if (!paramsObject.isNull("fail"))
                failCb = paramsObject.getString("fail");
            int encodingType = 0;
            if (!paramsObject.isNull("encodingType"))
                encodingType = paramsObject.getInt("encodingType");
            openCamera(encodingType);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void openCamera(int encodingType) {
        // TODO Auto-generated method stub
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            PublicWay.uploadPhotoService = this;
            File appDir = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            // 指定文件名字
            String fileName = PhotoNameUtils.getFileName(getActivity(), encodingType);
            takePhotoImgPath = MyAppConfig.LOCAL_IMG_CREATE_PATH + fileName;
            Intent intent = new Intent(getActivity(), MyCameraActivity.class);
            intent.putExtra(MyCameraActivity.PHOTO_DIRECTORY_PATH, MyAppConfig.LOCAL_IMG_CREATE_PATH);
            intent.putExtra(MyCameraActivity.PHOTO_NAME, fileName);
            intent.putExtra(MyCameraActivity.PHOTO_PARAM, paramsObject.toString());
            getActivity().startActivityForResult(intent, RESULT_CAMERA);
        } else {
            Toast.makeText(context,
                    Res.getStringID("filetransfer_sd_not_exist"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        PublicWay.uploadPhotoService = null;
        LogUtils.jasonDebug("requestCode=" + requestCode);
        LogUtils.jasonDebug("resultCode=" + resultCode);
        if (requestCode == RESULT_CAMERA) {
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    String uploadResult = intent.getStringExtra("uploadResult");
                    String imagePath = intent.getStringExtra("save_file_path");
                    File file = new File(imagePath);
                    JSONObject contextObj = new JSONObject(uploadResult);
                    jsonObject.put("context", contextObj);

                    Bitmap bitmap = Bimp.revitionImageSize(imagePath);
                    String bitmapBase64 = Bimp.bitmapToBase64(bitmap);
                    jsonObject.put("thumbnailData", bitmapBase64);
                    String returnData = jsonObject.toString();
                    // LogUtils.jasonDebug("returnData="+returnData);
                    this.jsCallback(successCb, returnData);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                this.failPicture(Res.getString("cancel_camera"));
            }
            clearImgCache();
        } else if (requestCode == EditImageActivity.ACTION_REQUEST_EDITIMAGE) {
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    String uploadResult = intent.getStringExtra("uploadResult");
                    String imagePath = intent.getStringExtra("save_file_path");
                    JSONObject contextObj = new JSONObject(uploadResult);
                    jsonObject.put("context", contextObj);

                    Bitmap bitmap = Bimp.revitionImageSize(imagePath);
                    String bitmapBase64 = Bimp.bitmapToBase64(bitmap);
                    jsonObject.put("thumbnailData", bitmapBase64);
                    String returnData = jsonObject.toString();
                    // LogUtils.jasonDebug("returnData="+returnData);
                    this.jsCallback(successCb, returnData);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                this.failPicture(Res.getString("cancel_camera"));
            }
            clearImgCache();
        } else if (requestCode == RESULT_GELLERY) {
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    String uploadResult = intent.getStringExtra("uploadResult");
                    LogUtils.jasonDebug("uploadResult=======" + uploadResult);
                    JSONObject contextObj = new JSONObject(uploadResult);
                    jsonObject.put("context", contextObj);
                    JSONArray dataArray = new JSONArray();
                    ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) intent
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    for (int i = 0; i < selectedList.size(); i++) {
                        String imagePath = selectedList.get(i).path;
                        File file = new File(imagePath);
                        JSONObject obj = new JSONObject();
                        Bitmap bitmap = Bimp.revitionImageSize(imagePath);
                        String bitmapBase64 = Bimp.bitmapToBase64(bitmap);
                        obj.put("data", bitmapBase64);
                        obj.put("name", file.getName());
                        dataArray.put(obj);
                    }
                    jsonObject.put("thumbnailData", dataArray);
                    String returnData = jsonObject.toString();
                    LogUtils.jasonDebug("returnData=" + returnData);
                    this.jsCallback(successCb, returnData);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                this.failPicture(Res.getString("cancel_select"));
            }
            clearImgCache();
        }
    }

    /**
     * 清除生成的图片cache
     */
    private void clearImgCache() {
        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_IMG_CREATE_PATH);
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
        // TODO Auto-generated method stub

    }

}
