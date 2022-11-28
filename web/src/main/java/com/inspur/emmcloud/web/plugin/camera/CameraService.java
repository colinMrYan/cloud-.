package com.inspur.emmcloud.web.plugin.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.inspur.emmcloud.baselib.util.BitmapUtils;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.imagepicker.view.CropImageView;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.photo.PhotoNameUtils;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * 进入相册选择图片进行上传
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class CameraService extends ImpPlugin {
    private static final String LOG_TAG = "PhotoService";
    private static final int PHOTOLIBRARY = 0; // Choose image from picture
    // library (same as
    // SAVEDPHOTOALBUM for Android)
//    private static final int CAMERA = 1; // Take picture from camera
    private static final int SAVEDPHOTOALBUM = 2; // Choose image from picture
    // library (same as
    // PHOTOLIBRARY for Android)
    private static final int DATA_URL = 0; // Return base64 encoded string
    private static final int FILE_URI = 1; // Return file uri
    // (content://media/external/images/media/2
    // for Android)
    private static final int NATIVE_URI = 2; // On Android, this is the same as
    private static final int JPEG = 0; // Take a picture of type JPEG
    private static final int PNG = 1; // Take a picture of type PNG
    // FILE_URI
    private static final int maxResolution = 1400;
    public static int num = 9;// 可以选择的图片数目
    private static int destType;
    private static int mQuality = 100; // Compression quality hint (0-100: 0=low
    // quality &
    // high compression, 100=compress of max quality)
    private static int targetWidth; // desired width of the image
    private static int targetHeight; // desired height of the image
    private static Uri imageUri; // Uri of captured image
    private static int encodingType = 0; // Type of encoding to use
    private static boolean saveToPhotoAlbum = false; // Should the picture be
    // saved to the
    // device's photo album
    private Bitmap photo;
    private String saveDir = Environment.getExternalStorageDirectory()
            .getPath() + "/DCIM";
    private int numPics;
    private Uri scanMe; // Uri of image to be added to content store
    private ArrayList<String> dataList = new ArrayList<String>();
    private String successCb, failCb;
    private int uploadOriginMaxSize = MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
    private int uploadThumbnailMaxSize = MyAppConfig.UPLOAD_THUMBNAIL_IMG_MAX_SIZE;
    private String watermarkContent, color, background, align, valign;
    private int fontSize;
    private String parm_flash_mode = "";
    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /*
     * 旋转图片
     *
     * @param angle
     *
     * @param bitmap
     *
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    @Override
    public void execute(String action, JSONObject paramsObject) {
        LogUtils.jasonDebug("paramsObject=" + paramsObject);
        if ("open".equals(action)) {
            open(paramsObject);
        } else if ("getPicture".equals(action)) {
            getPicture(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 选择相机拍照进行上传
     */
    private void open(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("options")) {
                JSONObject optionsObj = jsonObject.getJSONObject("options");
                destType = optionsObj.getInt(
                        "destinationType");
                mQuality = optionsObj.getInt(
                        "quality");
                targetWidth = optionsObj.getInt(
                        "targetWidth");
                targetHeight = optionsObj.getInt(
                        "targetHeight");
                encodingType = optionsObj.getInt(
                        "encodingType");
                switch (JSONUtils.getInt(optionsObj, "flashMode", 100)) {
                    case 0:
                        parm_flash_mode = Camera.Parameters.FLASH_MODE_OFF;
                        break;
                    case 1:
                        parm_flash_mode = Camera.Parameters.FLASH_MODE_TORCH;
                        break;
                    case 2:
                        parm_flash_mode = Camera.Parameters.FLASH_MODE_ON;
                        break;
                    case 3:
                        parm_flash_mode = Camera.Parameters.FLASH_MODE_AUTO;
                        break;
                }
                if (!optionsObj.isNull("watermark")) {
                    JSONObject watermarkObj = optionsObj.getJSONObject("watermark");
                    watermarkContent = JSONUtils.getString(watermarkObj, "content", "");
                    LogUtils.jasonDebug("watermarkContent=" + watermarkContent);
                    fontSize = JSONUtils.getInt(watermarkObj, "fontSize", 14);
                    color = JSONUtils.getString(watermarkObj, "color", "#ffffff");
                    background = JSONUtils.getString(watermarkObj, "background", "#00000000");
                    align = JSONUtils.getString(watermarkObj, "align", "left");
                    valign = JSONUtils.getString(watermarkObj, "valign", "top");
                }
            }
            if (!jsonObject.isNull("success"))
                successCb = jsonObject.getString("success");
            if (!jsonObject.isNull("fail"))
                failCb = jsonObject.getString("fail");

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (targetWidth < 1) {
            targetWidth = MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
        }
        if (targetHeight < 1) {
            targetHeight = MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
        }
        destoryImage();
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File cameraFile = createCaptureFile(encodingType);
            Intent intent = new Intent();
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_DIRECTORY_PATH, cameraFile.getParent());
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_NAME, cameraFile.getName());
            intent.putExtra(MyCameraActivity.EXTRA_ENCODING_TYPE, encodingType);
            intent.putExtra(MyCameraActivity.EXTRA_RECT_SCALE_JSON, jsonObject.toString());
            intent.putExtra(MyCameraActivity.EXTRA_FLASH_MODE, parm_flash_mode);
            intent.setClass(getFragmentContext(), MyCameraActivity.class);
            if (getImpCallBackInterface() != null) {
                getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.CAMERA_SERVICE_CAMERA_REQUEST);
            }
        } else {
            ToastUtils.show(getFragmentContext(), Res.getString("invalidSD"));
        }
        File savePath = new File(saveDir);
        if (!savePath.exists()) {
            savePath.mkdirs();
        }
    }

    /**
     * 进入相册文件夹
     */
    private void getPicture(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("options")) {
                JSONObject optionsObj = jsonObject.getJSONObject("options");
                destType = optionsObj.getInt(
                        "destinationType");
                mQuality = optionsObj.getInt(
                        "quality");
                targetWidth = optionsObj.getInt(
                        "targetWidth");
                targetHeight = optionsObj.getInt(
                        "targetHeight");
                encodingType = optionsObj.getInt(
                        "encodingType");
                if (!optionsObj.isNull("num")) {
                    num = jsonObject.getJSONObject("options")
                            .getInt("num");
                }
                if (num < 0 || num > 15) {
                    num = 9;
                }
                if (!optionsObj.isNull("watermark")) {
                    JSONObject watermarkObj = optionsObj.getJSONObject("watermark");
                    watermarkContent = JSONUtils.getString(watermarkObj, "content", "");
                    fontSize = JSONUtils.getInt(watermarkObj, "fontSize", 14);
                    color = JSONUtils.getString(watermarkObj, "color", "#ffffff");
                    background = JSONUtils.getString(watermarkObj, "background", "#00000000");
                    align = JSONUtils.getString(watermarkObj, "align", "left");
                    valign = JSONUtils.getString(watermarkObj, "valign", "top");
                }
            }
            if (!jsonObject.isNull("success"))
                successCb = jsonObject.getString("success");
            if (!jsonObject.isNull("fail"))
                failCb = jsonObject.getString("fail");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (targetWidth < 1) {
            targetWidth = MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
        }
        if (targetHeight < 1) {
            targetHeight = MyAppConfig.UPLOAD_ORIGIN_IMG_MAX_SIZE;
        }
        initImagePicker();
        Intent intent = new Intent(getFragmentContext(),
                ImageGridActivity.class);
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.CAMERA_SERVICE_GALLERY_REQUEST);
        }
    }

    /**
     * 初始化图片选择控件
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
        imagePicker.setSelectLimit(num); // 选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
        imagePicker.setFocusWidth(1000); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(1000); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000); // 保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000); // 保存文件的高度。单位像素
    }

    private String getTempDirectoryPath() {
        File cache = null;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cache = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/DCIM");
        }
        // Use internal storage
        else {
            cache = getFragmentContext().getCacheDir();
        }

        // Create the cache directory if it doesn't exist
        if (!cache.isDirectory()) {
            cache.mkdirs();
        }
        return cache.getAbsolutePath();
    }

    /**
     * 根据相应的encodingType创建相应的格式文件
     *
     * @param encodingType 图片格式
     * @return 返回对应的图片
     */
    private File createCaptureFile(int encodingType) {
        File photo = null;
        if (encodingType == JPEG) {
            photo = new File(getTempDirectoryPath(), System.currentTimeMillis()
                    + ".Pic.jpg");
        } else if (encodingType == PNG) {
            photo = new File(getTempDirectoryPath(), System.currentTimeMillis()
                    + ".Pic.png");
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: "
                    + encodingType);
        }
        return photo;
    }

    /**
     * IMP代码修改处
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        int mOriginHeightSize = targetHeight < uploadOriginMaxSize ? targetHeight : uploadOriginMaxSize;
        int mOriginWidthtSize = targetWidth < uploadOriginMaxSize ? targetWidth : uploadOriginMaxSize;
        Bitmap.CompressFormat format = (encodingType == JPEG) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;
        // 照相取得图片
        if (requestCode == ImpFragment.CAMERA_SERVICE_CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String originImagePath = intent.getStringExtra(MyCameraActivity.OUT_FILE_PATH);
                File originImageFile = new File(originImagePath);
                if (originImageFile != null && originImageFile.exists()) {
                    try {
                        String originImgFileName = PhotoNameUtils.getFileName(getFragmentContext(), encodingType);
                        String thumbnailImgFileName = PhotoNameUtils.getThumbnailFileName(getFragmentContext(), 0, encodingType);
                        LogUtils.jasonDebug("mOriginHeightSize=" + mOriginHeightSize);
                        File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(mOriginHeightSize).setMaxWidth(mOriginWidthtSize).setQuality(mQuality).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                .setCompressFormat(format).compressToFile(originImageFile, originImgFileName);
                        String originImgPath = originImgFile.getAbsolutePath();
                        if (!StringUtils.isBlank(watermarkContent)) {
                            ImageUtils.createWaterMask(getFragmentContext(), originImgPath, watermarkContent, color, background, align, valign, fontSize);
                        }
                        File thumbnailImgFile = new Compressor(getFragmentContext()).setMaxHeight(uploadThumbnailMaxSize).setMaxWidth(uploadThumbnailMaxSize).setQuality(mQuality).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                .setCompressFormat(format).compressToFile(originImgFile, thumbnailImgFileName);
                        String thumbnailImgPath = thumbnailImgFile.getAbsolutePath();
                        callbackData(originImgPath, thumbnailImgPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveNetException("CameraService.camera", e.toString());
                        this.failPicture(Res.getString("camera_error"));
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture(Res.getString("cancel_camera"));
            } else {
                saveNetException("CameraService.camera", "system_camera_error");
                this.failPicture(Res.getString("camera_error"));
            }
        } else if (requestCode == ImpFragment.CAMERA_SERVICE_GALLERY_REQUEST) {  // 从相册取图片
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                if (intent == null) {
                    //解决HCM放弃选择图片时弹出error的问题
//					LogUtils.jasonDebug("00000000");
//					this.failPicture(Res.getString("cancel_select"));
                } else {
                    ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) intent
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    String uris[] = new String[selectedList.size()];
                    for (int i = 0; i < selectedList.size(); i++) {
                        uris[i] = selectedList.get(i).path;
                    }
                    String[] originImgPaths = new String[uris.length];
                    String[] thumbnailImgPaths = new String[uris.length];

                    try {
                        for (int i = 0; i < selectedList.size(); i++) {
                            String imgFilePath = uris[i];
                            String originImgFileName = PhotoNameUtils.getFileName(getFragmentContext(), i, encodingType);
                            String thumbnailImgFileName = PhotoNameUtils.getThumbnailFileName(getFragmentContext(), i, encodingType);
                            File originImgFile = new Compressor(getFragmentContext()).setMaxHeight(mOriginHeightSize).setMaxWidth(mOriginWidthtSize).setQuality(mQuality).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .setCompressFormat(format).compressToFile(new File(imgFilePath), originImgFileName);
                            String originImgPath = originImgFile.getAbsolutePath();
                            if (!StringUtils.isBlank(watermarkContent)) {
                                ImageUtils.createWaterMask(getFragmentContext(), originImgPath, watermarkContent, color, background, align, valign, fontSize);
                            }
                            File thumbnailImgFile = new Compressor(getFragmentContext()).setMaxHeight(uploadThumbnailMaxSize).setMaxWidth(mOriginWidthtSize).setQuality(mQuality).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .setCompressFormat(format).compressToFile(originImgFile, thumbnailImgFileName);
                            String thumbnailImgPath = thumbnailImgFile.getAbsolutePath();
                            originImgPaths[i] = originImgPath;
                            thumbnailImgPaths[i] = thumbnailImgPath;
                        }
                        callbackDatas(originImgPaths, thumbnailImgPaths);
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveNetException("CameraService.gallery", e.toString());
                        this.failPicture(Res.getString("capture_error"));
                    }


                }


            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture(Res.getString("cancel_select"));
            } else {
                this.failPicture(Res.getString("select_error"));
                saveNetException("CameraService.gallery", "system_gallry_error");
            }
        }
    }

    /**
     * 旋转图片，解决三星手机拍照图片被旋转的问题
     */
    private void rotateImg(String imgPath) {
        // TODO Auto-generated method stub
        int degree = readPictureDegree(imgPath);
        if (degree == 0) {
            return;
        }
        FileInputStream inputStream = null;
        try {

            Bitmap bitmap = BitmapUtils.revitionImageSize(imgPath);
            Bitmap destBitmap = rotaingImageView(degree, bitmap);
            bitmap.recycle();
            //save to file
            saveBitmapToSDCard(destBitmap, imgPath);
            destBitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
     * 保存bitmap
     *
     * @param bitmap
     */
    public void saveBitmapToSDCard(Bitmap bitmap, String path) {
        File orignFile = new File(path);
        File file = new File(path + "---");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            if (orignFile.exists()) {
                orignFile.delete();
                file.renameTo(new File(path));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
        }


    }

    /**
     * Determine if we are storing the images in internal or external storage
     *
     * @return Uri
     */
    private Uri whichContentStore() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            return android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }
    }

    /**
     * IMP代码修改处
     *
     * @param originImgPath
     * @param thumbnailImgPath
     */
    private void callbackData(String originImgPath, String thumbnailImgPath) {
        // TODO Auto-generated method stub
        // 将选中的大图和小图地址传回前端
        JSONObject jsonObject = new JSONObject();
        try {
            byte[] thumbnailFileBytes = FileUtils.file2Bytes(thumbnailImgPath);
            byte[] thumbnailFileBytesBase64 = Base64.encode(thumbnailFileBytes, Base64.NO_WRAP);
            String thumbnailOutput = new String(thumbnailFileBytesBase64);
            jsonObject.put("thumbnailUrl", thumbnailImgPath);
            jsonObject.put("thumbnailData", thumbnailOutput);

            byte[] originalFileBytes = FileUtils.file2Bytes(originImgPath);
            byte[] originalFileBytesBase64 = Base64.encode(originalFileBytes, Base64.NO_WRAP);
            String originalOutput = new String(originalFileBytesBase64);
            jsonObject.put("originalUrl", originImgPath);
            jsonObject.put("originalData", originalOutput);
            this.jsCallback(successCb, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            this.failPicture(Res.getString("compress_error"));
        }
    }

    /**
     * IMP代码修改处
     *
     * @param originImgPaths
     * @param thumbnailImgPaths
     */
    private void callbackDatas(String[] originImgPaths, String[] thumbnailImgPaths) {
        // TODO Auto-generated method stub
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < thumbnailImgPaths.length; i++) {
                JSONObject jsonObject = new JSONObject();
                String thumbnailImgPath = thumbnailImgPaths[i];
                byte[] thumbnailFileBytes = FileUtils.file2Bytes(thumbnailImgPath);
                byte[] thumbnailFileBytesBase64 = Base64.encode(thumbnailFileBytes, Base64.NO_WRAP);
                String thumbnailOutput = new String(thumbnailFileBytesBase64);
                jsonObject.put("thumbnailUrl", thumbnailImgPath);
                jsonObject.put("thumbnailData", thumbnailOutput.toString());

                String originImgPath = originImgPaths[i];
                byte[] originalFileBytes = FileUtils.file2Bytes(originImgPath);
                byte[] originalFileBytesBase64 = Base64.encode(originalFileBytes, Base64.NO_WRAP);
                String originalOutput = new String(originalFileBytesBase64);
                jsonObject.put("originalUrl", originImgPath);
                jsonObject.put("originalData", originalOutput.toString());
                jsonArray.put(i, jsonObject);
            }
            // 将选中的大图和小图地址传回前端
            this.jsCallback(successCb, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
            this.failPicture(Res.getString("image_error"));
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

    }

    private void destoryImage() {
        if (photo != null) {
            photo.recycle();
            photo = null;
        }
    }

}
