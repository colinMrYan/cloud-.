package com.inspur.emmcloud.web.plugin.photo;

import static android.Manifest.permission.CAMERA;
import static com.inspur.emmcloud.web.ui.ImpFragment.SELECTOR_SERVICE_GALLERY_REQUEST;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Base64;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.media.selector.basic.PictureSelector;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.selector.FileSelectorService;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.ui.ImpActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okio.ByteString;

public class PhotoService extends ImpPlugin {

    private String successCb, failCb, selectAlbumCb;
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
            if (checkPermission()) {
                takePhotoAndUpload();
            } else {
                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA, new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {
                        takePhotoAndUpload();
                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(BaseApplication.getInstance(), PermissionRequestManagerUtils.getInstance().getPermissionToast(BaseApplication.getInstance(), permissions));
                    }
                });
            }
        } else if ("viewImage".equals(action)) {
            viewImage();
        } else if ("savePhoto".equals(action)) {
            savePhotoToGallery(paramsObject);
        } else if ("selectPicsFromAlbum".equals(action)) {
            selectImageFromAlbum(paramsObject);
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

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void savePhotoToGallery(JSONObject paramsObject) {
        String base64Url, imageUrl, fileName;
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        try {
            final JSONObject optionsObj = paramsObject.getJSONObject("options");
            fileName = optionsObj.optString("fileName", "default");
            base64Url = optionsObj.optString("base64Content");
            imageUrl = optionsObj.optString("imageUrl");
            if (!StringUtils.isEmpty(base64Url)) {
                saveBitmapFile(decodeBase64ToBitmap(base64Url), fileName);
                return;
            }
            if (!StringUtils.isEmpty(imageUrl)) {
                saveImageFromUrl(imageUrl, fileName);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            byte[] input = ByteString.decodeBase64(base64Str).toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 2;
            return BitmapFactory.decodeByteArray(input, 0, input.length, options);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveBitmapFile(Bitmap bitmap, String name) {
        String saveImageFolder = "IMP-Cloud/cache/save/";
        File temp = new File(Environment.getExternalStorageDirectory() + "/" + saveImageFolder);// 要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        String savedImagePath = temp.getPath() + System.currentTimeMillis() + (name + ".jpg");
        File file = new File(savedImagePath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            }
            bos.flush();
            bos.close();
            AppUtils.refreshMedia(getActivity(), savedImagePath);
            operateCallback(true, savedImagePath);
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(com.inspur.baselib.R.string.save_success));
        } catch (IOException e) {
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(com.inspur.baselib.R.string.save_fail));
            operateCallback(false, e.toString());
            e.printStackTrace();
        }
    }

    private void operateCallback(boolean success, String info) {
        try {
            if (success) {
                JSONObject json = new JSONObject();
                json.put("state", 1);
                JSONObject result = new JSONObject();
                result.put("path", info);
                json.put("result", result);
                jsCallback(successCb, json);
            } else {
                jsCallback(failCb, info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveImageFromUrl(String imageUrl, final String name) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().loadImage(imageUrl, options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        if (getActivity() != null) {
                            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(com.inspur.baselib.R.string.save_fail));
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        if (getActivity() != null) {
                            saveBitmapFile(loadedImage, name);
                        }
                    }
                });
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
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        imagePicker.setSupportOrigin(false);
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
            ToastUtils.show(getFragmentContext(), Res.getStringID("baselib_sd_not_exist"));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ImpFragment.PHOTO_SERVICE_CAMERA_REQUEST) {
            if (resultCode == getActivity().RESULT_OK) {
                loadingDlg.show();
                String originImagePath = intent.getStringExtra(MyCameraActivity.OUT_FILE_PATH);
                try {
                    File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(parm_resolution).setMaxWidth(parm_resolution).setQuality(parm_qualtity).setDestinationDirectoryPath(FilePathUtils.LOCAL_IMP_USER_OPERATE_INTERNAL_IMAGE_DIC)
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
                                File thumbnailFile = new Compressor(getFragmentContext()).setMaxHeight(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setMaxWidth(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(FilePathUtils.LOCAL_IMP_USER_OPERATE_INTERNAL_IMAGE_DIC)
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
                        File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(parm_resolution).setMaxWidth(parm_resolution).setQuality(parm_qualtity).setDestinationDirectoryPath(FilePathUtils.LOCAL_IMP_USER_OPERATE_INTERNAL_IMAGE_DIC)
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
                                    File thumbnailFile = new Compressor(getFragmentContext()).setMaxHeight(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setMaxWidth(MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE).setQuality(90).setDestinationDirectoryPath(FilePathUtils.LOCAL_IMP_USER_OPERATE_INTERNAL_IMAGE_DIC)
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
        } else if (requestCode == SELECTOR_SERVICE_GALLERY_REQUEST) {
            ArrayList<LocalMedia> mediaResult = PictureSelector.obtainSelectorList(intent);
            ArrayList<String> uploadInfos = new ArrayList<>();
            HashMap<String, String> mediaMap = new HashMap<>();
            for (LocalMedia media : mediaResult) {
                String mediaPath = media.getRealPath();
                mediaMap.put("mediaPath",mediaPath);
                mediaMap.put("mimeType", media.getMimeType());
                mediaMap.put("fileName", media.getFileName());
                uploadInfos.add(mediaMap.toString());
            }
            try {
                JSONObject json = new JSONObject();
                json.put("state", 1);
                JSONObject result = new JSONObject();
                result.put("data", JSONUtils.toJSONArray(uploadInfos));
                json.put("result", result);
                jsCallback(selectAlbumCb, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectImageFromAlbum(JSONObject paramsObject) {
        int maxNum = 9;
        // 0:图片，1：视频;
        int defaultType = 0;
        try {
            if (!paramsObject.isNull("success")) {
                selectAlbumCb = paramsObject.getString("success");
            } else {
                selectAlbumCb = null;
            }
            if (!paramsObject.isNull("options")) {
                JSONObject optionsObj = paramsObject.getJSONObject("options");
                if (!optionsObj.isNull("maxNum")) {
                    maxNum = optionsObj.getInt("maxNum");
                }
                if (!optionsObj.isNull("mType")) {
                    defaultType = optionsObj.getInt("mType");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (maxNum > 9 || maxNum < 0) {
            maxNum = 9;
        }
        if (defaultType > 1 || defaultType < 0) {
            defaultType = 0;
        }
        Router router = Router.getInstance();
        if (router.getService(FileSelectorService.class) != null) {
            FileSelectorService service = router.getService(FileSelectorService.class);
            service.selectImagesFromAlbum(((ImpActivity)getActivity()).getFragment(), maxNum, defaultType, SELECTOR_SERVICE_GALLERY_REQUEST);
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
