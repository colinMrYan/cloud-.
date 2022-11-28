package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.mycamera.CameraUtils;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.ErrorListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.CheckPermission;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.DeviceUtil;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.FileUtil;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.ScreenUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.createBitmap;

@SuppressWarnings("deprecation")
public class CameraInterface implements Camera.PreviewCallback {

    public static final int TYPE_RECORDER = 0x090;
    public static final int TYPE_CAPTURE = 0x091;
    private static final String TAG = "CJT";
    private volatile static CameraInterface mCameraInterface;
    int handlerTime = 0;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private int SELECTED_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int CAMERA_POST_POSITION = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int CAMERA_FRONT_POSITION = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private float screenProp = -1.0f;
    private boolean isRecorder = false;
    private MediaRecorder mediaRecorder;
    private String videoFileName;
    private String saveVideoPath;
    private String videoFileAbsPath;
    private Bitmap videoFirstFrame = null;
    private ErrorListener errorLisenter;
    private ImageView mSwitchView;
    private ImageView mFlashLamp;
    private int preview_width;
    private int preview_height;
    private int angle = 0;
    private int cameraAngle = 90;//摄像头角度   默认为90度
    private int rotation = 0;
    private byte[] firstFrame_data;
    private int nowScaleRate = 0;
    private int recordScleRate = 0;
    private JCameraView jCameraView;
    //视频质量
    private int mediaQuality = JCameraView.MEDIA_QUALITY_MIDDLE;
    private SensorManager sm = null;
    private SensorController sensorController;
    public static String flashMode = Camera.Parameters.FLASH_MODE_AUTO;
    /**
     * 拍照
     */
    private int nowAngle;

    private CameraInterface() {
        findAvailableCameras();
        SELECTED_CAMERA = CAMERA_POST_POSITION;
        saveVideoPath = "";
    }

    public static void destroyCameraInterface() {
        if (mCameraInterface != null) {
            mCameraInterface = null;
        }
    }

//    private SensorEventListener sensorEventListener = new SensorEventListener() {
//        public void onSensorChanged(SensorEvent event) {
//            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
//                return;
//            }
//            float[] values = event.values;
//            angle = AngleUtil.getSensorAngle(values[0], values[1]);
//            rotationAnimation();
//        }
//
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//    };

    //获取CameraInterface单例
    public static synchronized CameraInterface getInstance() {
        if (mCameraInterface == null) {
            synchronized (CameraInterface.class) {
                if (mCameraInterface == null) {
                    mCameraInterface = new CameraInterface();
                }
            }
        }
        return mCameraInterface;
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Context context) {
        float focusAreaSize = 300;
//        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
//        int centerX = (int) (x / ScreenUtils.getScreenWidth(context) * 2000 - 1000);
//        int centerY = (int) (y / ScreenUtils.getScreenHeight(context) * 2000 - 1000);
//        int left = clamp(centerX - areaSize / 2, -1000, 1000);
//        int top = clamp(centerY - areaSize / 2, -1000, 1000);
//        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
//        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
//                .bottom));
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int left = clamp(Float.valueOf((y / ScreenUtils.getScreenHeight(context)) * 2000 - 1000).intValue(), areaSize);
        int top = clamp(Float.valueOf(((ScreenUtils.getScreenWidth(context) - x) / ScreenUtils.getScreenWidth(context)) * 2000 - 1000).intValue(), areaSize);
        return new Rect(left, top, left + areaSize, top + areaSize);

    }

    private static int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize;
            } else {
                result = -1000 + focusAreaSize;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;

    }

    public void setSwitchView(ImageView mSwitchView, ImageView mFlashLamp) {
        this.mSwitchView = mSwitchView;
        this.mFlashLamp = mFlashLamp;
        if (mSwitchView != null) {
            cameraAngle = CameraUtils.getInstance().getCameraDisplayOrientation(mSwitchView.getContext(),
                    SELECTED_CAMERA);
        }
    }

    public void setJCameraView(JCameraView jCameraView) {
        this.jCameraView = jCameraView;
    }

    public void resetCamera() {
        SELECTED_CAMERA = CAMERA_POST_POSITION;
    }

    //切换摄像头icon跟随手机角度进行旋转
    private void rotationAnimation() {
        if (mSwitchView == null) {
            return;
        }
        if (rotation != angle) {
            int start_rotaion = 0;
            int end_rotation = 0;
            switch (rotation) {
                case 0:
                    start_rotaion = 0;
                    switch (angle) {
                        case 90:
                            end_rotation = -90;
                            break;
                        case 270:
                            end_rotation = 90;
                            break;
                    }
                    break;
                case 90:
                    start_rotaion = -90;
                    switch (angle) {
                        case 0:
                            end_rotation = 0;
                            break;
                        case 180:
                            end_rotation = -180;
                            break;
                    }
                    break;
                case 180:
                    start_rotaion = 180;
                    switch (angle) {
                        case 90:
                            end_rotation = 270;
                            break;
                        case 270:
                            end_rotation = 90;
                            break;
                    }
                    break;
                case 270:
                    start_rotaion = 90;
                    switch (angle) {
                        case 0:
                            end_rotation = 0;
                            break;
                        case 180:
                            end_rotation = 180;
                            break;
                    }
                    break;
            }
            ObjectAnimator animC = ObjectAnimator.ofFloat(mSwitchView, "rotation", start_rotaion, end_rotation);
            ObjectAnimator animF = ObjectAnimator.ofFloat(mFlashLamp, "rotation", start_rotaion, end_rotation);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animC, animF);
            set.setDuration(500);
            set.start();
            rotation = angle;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void setSaveVideoPath(String saveVideoPath) {
        this.saveVideoPath = saveVideoPath;
        File file = new File(saveVideoPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void setZoom(float zoom, int type) {
        if (mCamera == null) {
            return;
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        if (!mParams.isZoomSupported() && !mParams.isSmoothZoomSupported()) {
            return;
        }
        switch (type) {
            case TYPE_RECORDER:
                //如果不是录制视频中，上滑不会缩放
                if (!isRecorder) {
                    return;
                }
                if (zoom >= 0) {
                    //每移动50个像素缩放一个级别
                    int scaleRate = (int) (zoom / 50);
                    if (scaleRate <= mParams.getMaxZoom() && scaleRate >= nowScaleRate && recordScleRate != scaleRate) {
                        mParams.setZoom(scaleRate);
                        mCamera.setParameters(mParams);
                        recordScleRate = scaleRate;
                    }
                }
                break;
            case TYPE_CAPTURE:
                if (isRecorder) {
                    return;
                }

                //每移动50个像素缩放一个级别
                int scaleRate = (int) (zoom / 30);
                if (scaleRate < mParams.getMaxZoom()) {
                    nowScaleRate += scaleRate;
                    if (nowScaleRate < 0) {
                        nowScaleRate = 0;
                    } else if (nowScaleRate > mParams.getMaxZoom()) {
                        nowScaleRate = mParams.getMaxZoom();
                    }
                    mParams.setZoom(nowScaleRate);
                    mCamera.setParameters(mParams);
                }
                break;
        }

    }

    void setMediaQuality(int quality) {
        this.mediaQuality = quality;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        firstFrame_data = data;
    }

    public void setFlashMode(String flashMode) {
        if (!StringUtils.isEmpty(flashMode)) {
            this.flashMode = flashMode;
        } else {
            return;
        }
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(flashMode);
            mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * open Camera
     */
    void doOpenCamera(CameraOpenOverCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!CheckPermission.isCameraUseable(SELECTED_CAMERA) && this.errorLisenter != null) {
                this.errorLisenter.onError();
                return;
            }
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA);
        }
        callback.cameraHasOpened();
    }

    private synchronized void openCamera(int id) {
        try {
            this.mCamera = Camera.open(id);
        } catch (Exception var3) {
            var3.printStackTrace();
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        }

        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CJT", "enable shutter sound faild");
            }
        }
    }

    public synchronized void switchCamera(SurfaceHolder holder, float screenProp) {
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            SELECTED_CAMERA = CAMERA_FRONT_POSITION;
        } else {
            SELECTED_CAMERA = CAMERA_POST_POSITION;
        }
        doDestroyCamera();
        openCamera(SELECTED_CAMERA);
//        mCamera = Camera.open();
        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doStartPreview(holder, screenProp);
    }

    /**
     * doStartPreview
     */
    public void doStartPreview(SurfaceHolder holder, float screenProp) {
        if (this.screenProp < 0) {
            this.screenProp = screenProp;
        }
        if (holder == null) {
            return;
        }
        if (mCamera != null) {
            try {
                mParams = mCamera.getParameters();
                Camera.Size previewSize = CameraUtils.getInstance().getPreviewSize(mParams
                        .getSupportedPreviewSizes(), 700, screenProp);
                Camera.Size pictureSize = CameraUtils.getInstance().getPictureSize(mParams
                        .getSupportedPictureSizes(), 1000, screenProp);
                mParams.setPreviewSize(previewSize.width, previewSize.height);
                preview_height = previewSize.height;
                mParams.setPictureSize(pictureSize.width, pictureSize.height);
                if (jCameraView.isHasCameraCropView() && CameraUtils.getInstance().isSupportedFocusMode(
                        mParams.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (CameraUtils.getInstance().isSupportedFocusMode(
                        mParams.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                if (CameraUtils.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                        ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                // imp 回调，后置相机添加闪光灯光操作
                if (CameraInterface.getInstance().getSELECTED_CAMERA() == 0) {
                    mParams.setFlashMode(flashMode);
                }
                mCamera.setParameters(mParams);
                mParams = mCamera.getParameters();
                mCamera.setPreviewDisplay(holder);  //SurfaceView
                mCamera.setDisplayOrientation(cameraAngle);//浏览角度
                mCamera.setPreviewCallback(this); //每一帧回调
                mCamera.startPreview();//启动浏览
                isPreviewing = true;


                float prevewProp = (float) previewSize.width / (float) previewSize.height;
                int width = jCameraView.getWidth();
                int height = (int) (width * prevewProp);
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) jCameraView.getLayoutParams();
                params.width = width;
                params.height = height;
                LogUtils.jasonDebug("a==" + System.currentTimeMillis());
                jCameraView.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.jasonDebug("b==" + System.currentTimeMillis());
                        jCameraView.setLayoutParams(params);
                        jCameraView.showCaptureLayout();
                        jCameraView.reFocus();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止预览
     */
    public void doStopPreview() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                isPreviewing = false;
                Log.i(TAG, "=== Stop Preview ===");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 销毁Camera
     */
    void doDestroyCamera() {
        errorLisenter = null;
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mSwitchView = null;
                mFlashLamp = null;
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
                nowScaleRate = 1;
//                destroyCameraInterface();
                Log.i(TAG, "=== Destroy Camera ===");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "=== Camera  Null===");
        }
    }

    public void takePicture(final TakePictureCallback callback) {
        if (mCamera == null) {
            return;
        }
        switch (cameraAngle) {
            case 90:
                nowAngle = Math.abs(angle + cameraAngle) % 360;
                break;
            case 270:
                nowAngle = Math.abs(cameraAngle - angle);
                break;
        }
//
        Log.i("CJT", angle + " = " + cameraAngle + " = " + nowAngle);
        if (firstFrame_data.length > 0) {
            Camera.Parameters parameters = mCamera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            YuvImage yuv = new YuvImage(firstFrame_data, parameters.getPreviewFormat(), width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
            byte[] bytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix matrix = new Matrix();
            if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                matrix.setRotate(nowAngle);
            } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                matrix.setRotate(360 - nowAngle);
                matrix.postScale(-1, 1);
            }
            bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (callback != null) {
                if (nowAngle == 90 || nowAngle == 270) {
                    callback.captureResult(bitmap, true);
                } else {
                    callback.captureResult(bitmap, false);
                }
            }
        } else {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                        matrix.setRotate(nowAngle);
                    } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                        matrix.setRotate(360 - nowAngle);
                        matrix.postScale(-1, 1);
                    }
                    bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if (callback != null) {
                        if (nowAngle == 90 || nowAngle == 270) {
                            callback.captureResult(bitmap, true);
                        } else {
                            callback.captureResult(bitmap, false);
                        }
                    }
                }
            });
        }
    }

    //启动录像
    public void startRecord(Surface surface, float screenProp, ErrorCallback callback) {
        mCamera.setPreviewCallback(null);
        final int nowAngle = (angle + 90) % 360;
        //获取第一帧图片
        Camera.Parameters parameters = mCamera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(firstFrame_data, parameters.getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] bytes = out.toByteArray();
        videoFirstFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            matrix.setRotate(nowAngle);
        } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            matrix.setRotate(270);
        }
        videoFirstFrame = createBitmap(videoFirstFrame, 0, 0, videoFirstFrame.getWidth(), videoFirstFrame
                .getHeight(), matrix, true);

        if (isRecorder) {
            return;
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA);
        }
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(mParams);
        mCamera.unlock();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


        Camera.Size videoSize;
        if (mParams.getSupportedVideoSizes() == null) {
            videoSize = CameraUtils.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), 600,
                    screenProp);
        } else {
            videoSize = CameraUtils.getInstance().getPreviewSize(mParams.getSupportedVideoSizes(), 600,
                    screenProp);
        }
        Log.i(TAG, "setVideoSize    width = " + videoSize.width + "height = " + videoSize.height);
        if (videoSize.width == videoSize.height) {
            mediaRecorder.setVideoSize(preview_width, preview_height);
        } else {
            mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        }
//        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
//            mediaRecorder.setOrientationHint(270);
//        } else {
//            mediaRecorder.setOrientationHint(nowAngle);
////            mediaRecorder.setOrientationHint(90);
//        }

        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            //手机预览倒立的处理
            if (cameraAngle == 270) {
                //横屏
                if (nowAngle == 0) {
                    mediaRecorder.setOrientationHint(180);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(270);
                } else {
                    mediaRecorder.setOrientationHint(90);
                }
            } else {
                if (nowAngle == 90) {
                    mediaRecorder.setOrientationHint(270);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(90);
                } else {
                    mediaRecorder.setOrientationHint(nowAngle);
                }
            }
        } else {
            mediaRecorder.setOrientationHint(nowAngle);
        }


        if (DeviceUtil.isHuaWeiRongyao()) {
            mediaRecorder.setVideoEncodingBitRate(4 * 100000);
        } else {
            mediaRecorder.setVideoEncodingBitRate(mediaQuality);
        }
        mediaRecorder.setPreviewDisplay(surface);

        videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        if (saveVideoPath.equals("")) {
            saveVideoPath = BaseApplication.getInstance().getExternalCacheDir().getPath();
        }
        videoFileAbsPath = saveVideoPath + File.separator + videoFileName;
        mediaRecorder.setOutputFile(videoFileAbsPath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecorder = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.i("CJT", "startRecord IllegalStateException");
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("CJT", "startRecord IOException");
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        } catch (RuntimeException e) {
            Log.i("CJT", "startRecord RuntimeException");
        }
    }

    //停止录像
    public void stopRecord(boolean isShort, StopRecordCallback callback) {
        if (!isRecorder) {
            return;
        }
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                mediaRecorder = null;
                mediaRecorder = new MediaRecorder();
            } finally {
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }
                mediaRecorder = null;
                isRecorder = false;
            }
            if (isShort) {
                if (FileUtil.deleteFile(videoFileAbsPath)) {
                    callback.recordResult(null, null);
                }
                return;
            }
            doStopPreview();
            String fileName = saveVideoPath + File.separator + videoFileName;
            callback.recordResult(fileName, videoFirstFrame);
        }
    }

    private void findAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT_POSITION = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_POST_POSITION = info.facing;
                    break;
            }
        }
    }

    public void handleFocus(final Context context, final float x, final float y, final FocusCallback callback) {
        if (mCamera == null || jCameraView.isHasCameraCropView()) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f, context);
        Camera.Area cameraArea = new Camera.Area(focusRect, 1000);
        if (params.getMaxNumFocusAreas() > 0) {
            mCamera.cancelAutoFocus();
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(cameraArea);
            params.setFocusAreas(focusAreas);
        }
        if (params.getMaxNumMeteringAreas() > 0) { // Check if it is safe to set meteringArea.
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(cameraArea);
            params.setMeteringAreas(meteringAreas);
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            if (params.getMaxNumFocusAreas() > 0) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success || handlerTime > 1) {
                            Camera.Parameters params = camera.getParameters();
                            params.setFocusMode(currentFocusMode);
                            camera.setParameters(params);
                            handlerTime = 0;
                            callback.focusSuccess();
                            sensorController.lockFocus();
                        } else {
                            handlerTime++;
                            handleFocus(context, x, y, callback);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "autoFocus failer");
        }
    }

    void setErrorLinsenter(ErrorListener errorLisenter) {
        this.errorLisenter = errorLisenter;
    }

    void registerSensorManager(Context context) {
//        if (sm == null) {
//            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        }
//        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
//                .SENSOR_DELAY_NORMAL);

        sensorController = SensorController.getInstance(context);
        sensorController.setCameraFocusListener(new SensorController.CameraFocusListener() {
            @Override
            public void onFocus() {
                if (mCamera != null) {
                    if (!sensorController.isFocusLocked()) {
                        jCameraView.reFocus();
                    }
                }
            }
        });
        sensorController.start();

    }

    void unregisterSensorManager(Context context) {
//        if (sm == null) {
//            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        }
//        sm.unregisterListener(sensorEventListener);
        if (sensorController != null) {
            sensorController.stop();
        }
    }

    public void setFlashKeep() {
        setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
    }

    public void setFlashClose() {
        setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    }

    public void setFlashOpen() {
        setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    }

    public void setFlashAuto() {
        setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }

    public void unlockFocus() {
        if (sensorController != null) {
            sensorController.unlockFocus();
        }

    }

    public int getSELECTED_CAMERA() {
        return SELECTED_CAMERA;
    }

    void isPreview(boolean res) {
        this.isPreviewing = res;
    }

    public interface CameraOpenOverCallback {
        void cameraHasOpened();
    }


    public interface StopRecordCallback {
        void recordResult(String url, Bitmap firstFrame);
    }


    interface ErrorCallback {
        void onError();
    }

    public interface TakePictureCallback {
        void captureResult(Bitmap bitmap, boolean isVertical);
    }

    public interface FocusCallback {
        void focusSuccess();

    }
}
