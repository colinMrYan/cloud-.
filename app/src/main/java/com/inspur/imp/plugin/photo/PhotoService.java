package com.inspur.imp.plugin.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.privates.FileUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.api.ImpFragment;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;
import com.inspur.imp.util.compressor.Compressor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoService extends ImpPlugin {

    private String successCb, failCb;
    private JSONObject paramsObject;
    private int parm_resolution = MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE;
    private int encodingType = 0;
    private int parm_qualtity = 90;
    private String parm_uploadUrl, parm_context;
    private JSONObject watermarkObj;
    private LoadingDialog loadingDlg;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        this.paramsObject = paramsObject;
        LogUtils.jasonDebug("paramsObject=" + paramsObject.toString());
        if ("selectAndUpload".equals(action)) {
            selectAndUpload();
        } else if ("takePhotoAndUpload".equals(action)) {
            takePhotoAndUpload();
        } else if ("viewImage".equals(action)) {
            viewImage();
        } else {
            showCallIMPMethodErrorDlg();
        }
        loadingDlg = new LoadingDialog(getActivity());
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 浏览图片原生方法
     */
    private void viewImage() {
        ArrayList<String> imageOriginUrlList = new ArrayList<>();
        ArrayList<String> imageThumbnailUrlList = new ArrayList<>();
        JSONArray jsonParamArray = JSONUtils.getJSONArray(paramsObject, "img", new JSONArray());
        int imageIndex = JSONUtils.getInt(paramsObject, "index", 0);
        for (int i = 0; i < jsonParamArray.length(); i++) {
            imageThumbnailUrlList.add(JSONUtils.getString(JSONUtils.getJSONObject(jsonParamArray, i, new JSONObject()), "imgUrl", ""));
            imageOriginUrlList.add(JSONUtils.getString(JSONUtils.getJSONObject(jsonParamArray, i, new JSONObject()), "originImgUrl", ""));
        }
        if (imageOriginUrlList.size() > 0) {
            startImagePagerActivity(imageOriginUrlList, imageThumbnailUrlList, imageIndex);
        }
    }

    /**
     * 调起ImagePager
     *
     * @param imagePathList
     */
    private void startImagePagerActivity(ArrayList<String> imagePathList, ArrayList<String> imageThumbnailUrlList, int imageIndex) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), ImageGalleryActivity.class);
        intent.putStringArrayListExtra(ImageGalleryActivity.EXTRA_IMAGE_SOURCE_URLS, imagePathList);
        intent.putStringArrayListExtra(ImageGalleryActivity.EXTRA_IMAGE_THUMB_URLS, imageThumbnailUrlList);
        intent.putExtra(ImageGalleryActivity.EXTRA_IMAGE_INDEX, imageIndex);
        getActivity().startActivity(intent);
    }

    private void selectAndUpload() {
        // TODO Auto-generated method stub
        try {
            if (!paramsObject.isNull("success"))
                successCb = paramsObject.getString("success");
            if (!paramsObject.isNull("fail"))
                failCb = paramsObject.getString("fail");

            int picTotal = 9;
            this.parm_uploadUrl = JSONUtils.getString(paramsObject, "uploadUrl", null);
            if (!paramsObject.isNull("options")) {
                JSONObject optionsObj = paramsObject.getJSONObject("options");
                if (optionsObj.has("picTotal")) {
                    picTotal = optionsObj.getInt(
                            "picTotal");
                }
                if (!optionsObj.isNull("encodingType")) {
                    encodingType = optionsObj.getInt("encodingType");
                }
                parm_resolution = JSONUtils.getInt(optionsObj, "resolution", MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE);
                parm_resolution = parm_resolution < MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE ? parm_resolution : MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
                parm_qualtity = JSONUtils.getInt(optionsObj, "quality", 90);
                this.parm_context = JSONUtils.getString(optionsObj, "context", "");
                this.watermarkObj = JSONUtils.getJSONObject(optionsObj, "watermark", null);
            }
            openGallery(picTotal);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    private void openGallery(int picTotal) {
        // TODO Auto-generated method stub
        initImagePicker(picTotal);
        Intent intent = new Intent(getFragmentContext(), ImageGridActivity.class);
        intent.putExtra("paramsObject", paramsObject.toString());
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.PHOTO_SERVICE_GALLERY_REQUEST);
        }
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
        if (picTotal < 0 || picTotal > 9) {
            picTotal = 9;
        }
        imagePicker.setSelectLimit(picTotal);
        imagePicker.setMultiMode(true);
    }

    private void takePhotoAndUpload() {
        // TODO Auto-generated method stub
        try {
            if (!paramsObject.isNull("success"))
                successCb = paramsObject.getString("success");
            if (!paramsObject.isNull("fail"))
                failCb = paramsObject.getString("fail");
            this.parm_uploadUrl = JSONUtils.getString(paramsObject, "uploadUrl", null);
            if (!paramsObject.isNull("options")) {
                JSONObject optionsObj = paramsObject.getJSONObject("options");
                if (!optionsObj.isNull("encodingType")) {
                    encodingType = optionsObj.getInt("encodingType");
                }
                parm_resolution = JSONUtils.getInt(optionsObj, "resolution", MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE);
                parm_resolution = parm_resolution < MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE ? parm_resolution : MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
                parm_qualtity = JSONUtils.getInt(optionsObj, "quality", 90);
                this.parm_context = JSONUtils.getString(optionsObj, "context", "");
                this.watermarkObj = JSONUtils.getJSONObject(optionsObj, "watermark", null);
            }
            openCamera();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void openCamera() {
        // TODO Auto-generated method stub
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File appDir = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            // 指定文件名字
            String fileName = PhotoNameUtils.getFileName(getFragmentContext(), encodingType);
            Intent intent = new Intent(getFragmentContext(), MyCameraActivity.class);
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_DIRECTORY_PATH, MyAppConfig.LOCAL_IMG_CREATE_PATH);
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_NAME, fileName);
            intent.putExtra(MyCameraActivity.EXTRA_RECT_SCALE_JSON, paramsObject.toString());
            if (getImpCallBackInterface() != null) {
                getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.PHOTO_SERVICE_CAMERA_REQUEST);
            }
        } else {
            ToastUtils.show(getFragmentContext(), Res.getStringID("filetransfer_sd_not_exist"));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ImpFragment.PHOTO_SERVICE_CAMERA_REQUEST) {
            if (resultCode == getActivity().RESULT_OK) {
                loadingDlg.show();
                String originImagePath = intent.getStringExtra(MyCameraActivity.OUT_FILE_PATH);
                try {
                    File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(parm_resolution).setMaxWidth(parm_resolution).setQuality(parm_qualtity).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                            .compressToFile(new File(originImagePath));
                    final String originImagefinalPath = originImgFile.getAbsolutePath();
                    new UploadPhoto(getActivity(), new UploadPhoto.OnUploadPhotoListener() {

                        @Override
                        public void uploadPhotoSuccess(String result) {
                            // TODO Auto-generated method stub
                            Bitmap thumbnailBitmap = null;
                            try {
                                JSONObject jsonObject = new JSONObject();
                                JSONObject contextObj = new JSONObject(result);
                                jsonObject.put("context", contextObj);
                                String thumbnailName = System.currentTimeMillis() + ".jpg";
                                File thumbnailFile = new Compressor(getFragmentContext()).setMaxHeight(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setMaxWidth(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                        .compressToFile(new File(originImagefinalPath), thumbnailName);
                                String thumbnailFilePath = thumbnailFile.getAbsolutePath();
                                byte[] thumbnailFileBytes = FileUtils.file2Bytes(thumbnailFilePath);
                                byte[] thumbnailFileBytesBase64 = Base64.encode(thumbnailFileBytes, Base64.NO_WRAP);
                                String thumbnailOutput = new String(thumbnailFileBytesBase64);
                                jsonObject.put("thumbnailData", thumbnailOutput);
                                String returnData = jsonObject.toString();
                                LoadingDialog.dimissDlg(loadingDlg);
                                PhotoService.this.jsCallback(successCb, returnData);
                            } catch (Exception e) {
                                e.printStackTrace();
                                LoadingDialog.dimissDlg(loadingDlg);
                                PhotoService.this.failPicture(Res.getString("camera_error"));
                                saveNetException("PhotoService.camera", e.toString());
                            } finally {
                                recycleBitmap(thumbnailBitmap);
                                System.gc();
                            }

                        }

                        @Override
                        public void uploadPhotoFail() {
                            // TODO Auto-generated method stub
                            LoadingDialog.dimissDlg(loadingDlg);
                            ToastUtils.show(getFragmentContext(), R.string.img_upload_fail);
                        }
                    }).upload(parm_uploadUrl, originImagePath, encodingType, parm_context, watermarkObj);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    LoadingDialog.dimissDlg(loadingDlg);
                    this.failPicture(Res.getString("camera_error"));
                    saveNetException("PhotoService.camera", e.toString());
                    e.printStackTrace();
                }
            } else {
                this.failPicture(Res.getString("cancel_camera"));
                saveNetException("PhotoService.camera", "system_camera_error");
            }
        } else if (requestCode == ImpFragment.PHOTO_SERVICE_GALLERY_REQUEST) {
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) intent
                        .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                try {
                    loadingDlg.show();
                    final List<String> originImagePathList = new ArrayList<>();
                    for (ImageItem imageItem : selectedList) {
                        String originImagePath = imageItem.path;
                        File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(parm_resolution).setMaxWidth(parm_resolution).setQuality(parm_qualtity).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                .compressToFile(new File(originImagePath));
                        originImagePathList.add(originImgFile.getAbsolutePath());
                    }
                    new UploadPhoto(getActivity(), new UploadPhoto.OnUploadPhotoListener() {

                        @Override
                        public void uploadPhotoSuccess(String result) {
                            // TODO Auto-generated method stub
                            if (loadingDlg != null && loadingDlg.isShowing()) {
                                loadingDlg.dismiss();
                            }
                            Bitmap thumbnailBitmaps[] = new Bitmap[originImagePathList.size()];
                            try {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    JSONObject contextObj = new JSONObject(result);
                                    jsonObject.put("context", contextObj);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    jsonObject.put("context", result);
                                }
                                JSONArray dataArray = new JSONArray();
                                for (int i = 0; i < originImagePathList.size(); i++) {
                                    String imagePath = originImagePathList.get(i);
                                    File file = new File(imagePath);
                                    String thumbnailName = System.currentTimeMillis() + ".jpg";
                                    File thumbnailFile = new Compressor(getFragmentContext()).setMaxHeight(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setMaxWidth(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                            .compressToFile(new File(imagePath), thumbnailName);
                                    String thumbnailFilePath = thumbnailFile.getAbsolutePath();
                                    byte[] thumbnailFileBytes = FileUtils.file2Bytes(thumbnailFilePath);
                                    byte[] thumbnailFileBytesBase64 = Base64.encode(thumbnailFileBytes, Base64.NO_WRAP);
                                    String thumbnailOutput = new String(thumbnailFileBytesBase64);
                                    JSONObject obj = new JSONObject();
                                    obj.put("data", thumbnailOutput);
                                    obj.put("name", file.getName());
                                    dataArray.put(obj);
                                }
                                jsonObject.put("thumbnailData", dataArray);
                                String returnData = jsonObject.toString();
                                LogUtils.jasonDebug("returnData=" + returnData);
                                PhotoService.this.jsCallback(successCb, returnData);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                LoadingDialog.dimissDlg(loadingDlg);
                                e.printStackTrace();
                                PhotoService.this.saveNetException("PhotoService.gallery", e.toString());
                                PhotoService.this.failPicture(Res.getString("select_error"));
                            } finally {
                                for (int i = 0; i < thumbnailBitmaps.length; i++) {
                                    recycleBitmap(thumbnailBitmaps[i]);
                                }
                                System.gc();
                            }
                        }

                        @Override
                        public void uploadPhotoFail() {
                            // TODO Auto-generated method stub
                            if (loadingDlg != null && loadingDlg.isShowing()) {
                                loadingDlg.dismiss();
                            }
                            ToastUtils.show(getFragmentContext(), R.string.img_upload_fail);
                        }
                    }).upload(parm_uploadUrl, originImagePathList, encodingType, parm_context, watermarkObj);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    LoadingDialog.dimissDlg(loadingDlg);
                    e.printStackTrace();
                    saveNetException("PhotoService.gallery", e.toString());
                    this.failPicture(Res.getString("select_error"));
                }
            } else {
                this.failPicture(Res.getString("cancel_select"));
            }
        }
    }

    /**
     * 回收bitmap
     *
     * @param bitmap
     */
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void failPicture(String err) {
        this.jsCallback(failCb, err);
    }

    /**
     * 处理异常网络请求
     *
     * @param function
     * @param error
     */
    private void saveNetException(String function, String error) {
        AppExceptionCacheUtils.saveAppException(getFragmentContext(), 4, function, error, 0);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

}
