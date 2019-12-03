package com.inspur.emmcloud.basemodule.util.mycamera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.baselib.util.BitmapUtils;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.R2;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyCameraActivity extends BaseFragmentActivity implements View.OnClickListener, SurfaceHolder.Callback {

    public static final String EXTRA_PHOTO_DIRECTORY_PATH = "IMAGE_SAVE_PATH";
    public static final String EXTRA_PHOTO_NAME = "IMAGE_NAME";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String EXTRA_RECT_SCALE_JSON = "CAMERA_SCALE_JSON";
    public static final String OUT_FILE_PATH = "OUT_FILE_PATH";
    private static final int REQ_IMAGE_EDIT = 1;
    @BindView(R2.id.rl_preview)
    RelativeLayout previewLayout;
    @BindView(R2.id.camera_crop_view)
    CameraCropView cameraCropView;
    @BindView(R2.id.iv_preview)
    ImageView previewImg;
    @BindView(R2.id.focus_view)
    FocusView focusView;
    @BindView(R2.id.surface_view)
    SurfaceView previewSFV;
    @BindView(R2.id.rl_parent)
    RelativeLayout parentLayout;
    @BindView(R2.id.rl_capture)
    RelativeLayout captureLayout;
    private int encodingType = 0;
    private String photoFilePath;
    private String photoName;

    private ImageButton switchCameraBtn;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int currentCameraFacing;
    private int currentOrientation = 0;
    private DetectScreenOrientation detectScreenOrientation;
    private String photoSaveDirectoryPath;

    private Bitmap originBitmap;
    private Bitmap cropBitmap;
    private boolean safeToTakePicture = false;
    private int handlerTime = 0;
    private SensorController sensorController;
    private int screenWidth, screenHeight;

    @Override
    public void onCreate() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(this, R.string.baselib_sd_not_exist);
            finish();
        }
        setContentView(R.layout.activity_my_camera);
        ButterKnife.bind(this);
        initData();

        sensorController = SensorController.getInstance(this);
        sensorController.setCameraFocusListener(new SensorController.CameraFocusListener() {
            @Override
            public void onFocus() {
                if (mCamera != null) {
                    if (!sensorController.isFocusLocked()) {
                        if (setFocusViewWidthAnimation(screenWidth / 2, screenHeight / 2)) {
                            sensorController.lockFocus();
                        }
                    }
                }
            }
        });
        sensorController.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setWindows();

    }

    private void setWindows() {
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.jasonDebug("onResume-----------");
        initView();
    }

    private void initData() {
        if (detectScreenOrientation == null) {
            detectScreenOrientation = new DetectScreenOrientation(this);
        }
        detectScreenOrientation.enable();
        photoSaveDirectoryPath = getIntent().getExtras().getString(EXTRA_PHOTO_DIRECTORY_PATH, Environment.getExternalStorageDirectory() + "/DCIM/");
        photoName = getIntent().getExtras().getString(EXTRA_PHOTO_NAME, System.currentTimeMillis() + ".jpg");
        encodingType = getIntent().getIntExtra(EXTRA_ENCODING_TYPE, 0);
        screenWidth = ResolutionUtils.getWidth(this);
        screenHeight = ResolutionUtils.getHeight(this);

    }

    private void initView() {
        mHolder = previewSFV.getHolder();
        mHolder.addCallback(MyCameraActivity.this);
        switchCameraBtn = findViewById(R.id.switch_camera_btn);
        if (Camera.getNumberOfCameras() < 2) {
            switchCameraBtn.setVisibility(View.GONE);
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        currentCameraFacing = hasBackFacingCamera() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        initCamera();

        if (getIntent().hasExtra(EXTRA_RECT_SCALE_JSON)) {
            String json = getIntent().getStringExtra(EXTRA_RECT_SCALE_JSON);
            cameraCropView.setCropData(json);
        } else {
            cameraCropView.setVisibility(View.GONE);
        }
    }

    private void initCamera() {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {

                setWindows();
                openCamera();
                setCameraParams();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(MyCameraActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(MyCameraActivity.this, permissions));
                finish();
            }
        });
    }


    private void openCamera() {
        try {
            mCamera = Camera.open(currentCameraFacing);//1:采集指纹的摄像头. 0:拍照的摄像头.
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            ToastUtils.show(getApplicationContext(), R.string.open_camera_fail_by_perminssion);
            finish();
            e.printStackTrace();
        }
    }

    private void setCameraParams() {
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            int orientation = judgeScreenOrientation();
            int rotateAngle = 0;
            if (Surface.ROTATION_0 == orientation) {
                rotateAngle = 90;
            } else if (Surface.ROTATION_90 == orientation) {
                rotateAngle = 0;
            } else if (Surface.ROTATION_180 == orientation) {
                rotateAngle = 180;
            } else if (Surface.ROTATION_270 == orientation) {
                rotateAngle = 180;

            }
            mCamera.setDisplayOrientation(rotateAngle);
            parameters.setRotation(rotateAngle);
            float rate = parentLayout.getHeight() * 1.0f / parentLayout.getWidth();
            List<Camera.Size> PictureSizeList = parameters.getSupportedPictureSizes();
            Camera.Size pictureSize = CameraUtils.getInstance(this).getPictureSize(PictureSizeList, 1000, rate);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            LogUtils.jasonDebug("pictureSize.width=" + pictureSize.width + "   pictureSize.height=" + pictureSize.height);
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = CameraUtils.getInstance(this).getPreviewSize(previewSizeList, 700, rate);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            LogUtils.jasonDebug("previewSize.width=" + previewSize.width + "   previewSize.height=" + previewSize.height);
            List<String> modelList = parameters.getSupportedFlashModes();
            if (modelList != null && modelList.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            List<String> focusModeList = parameters.getSupportedFocusModes();
            if (focusModeList != null) {
                if (focusModeList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                } else if (focusModeList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
                }
            }

            mCamera.setParameters(parameters);
            mCamera.startPreview();
            safeToTakePicture = true;

            //todo
            // mCamera.cancelAutoFocus();// 如果要实现连续的自动对焦，这一句必须加上，这句必须要在startPreview后面加上去

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) parentLayout.getLayoutParams();
            params.height = screenWidth * pictureSize.width / pictureSize.height;
            parentLayout.setLayoutParams(params);


            setFocusViewWidthAnimation(focusView.getWidth() / 2, focusView.getHeight() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getPointerCount() == 1) {
                handleFocus(event.getX(), event.getY());
                //显示对焦指示器
                LogUtils.jasonDebug("onTouchEvent==================");
                setFocusViewWidthAnimation(event.getX(), event.getY());
            }
        }
        return super.onTouchEvent(event);
    }


    public boolean setFocusViewWidthAnimation(final float x, final float y) {
        if (mCamera == null) {
            return false;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            LogUtils.jasonDebug("1111111111111");
            focusView.setVisibility(View.INVISIBLE);
            return false;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success || handlerTime > 10) {
                        Camera.Parameters params = camera.getParameters();
                        params.setFocusMode(currentFocusMode);
                        camera.setParameters(params);
                        handlerTime = 0;
                        focusView.setVisibility(View.INVISIBLE);
                    } else {
                        handlerTime++;
                        setFocusViewWidthAnimation(x, y);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sensorController.unlockFocus();
                        }
                    }, 1000);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean handleFocus(float x, float y) {
        LogUtils.jasonDebug("x=" + x);
        LogUtils.jasonDebug("y=" + y);
        if (y > captureLayout.getTop()) {
            return false;
        }
        LogUtils.jasonDebug("000000000000");
        focusView.setVisibility(View.VISIBLE);
        if (x < focusView.getWidth() / 2) {
            x = focusView.getWidth() / 2;
        }
        if (x > screenWidth - focusView.getWidth() / 2) {
            x = screenWidth - focusView.getWidth() / 2;
        }
        if (y < focusView.getWidth() / 2) {
            y = focusView.getWidth() / 2;
        }
        if (y > captureLayout.getTop() - focusView.getWidth() / 2) {
            y = captureLayout.getTop() - focusView.getWidth() / 2;
        }
        focusView.setX(x - focusView.getWidth() / 2);
        focusView.setY(y - focusView.getHeight() / 2);
        LogUtils.jasonDebug("width==" + focusView.getWidth());
        LogUtils.jasonDebug("height==" + focusView.getHeight());
        LogUtils.jasonDebug("x - focusView.getWidth()==" + (x - focusView.getWidth()));
        LogUtils.jasonDebug("y - focusView.getHeight()==" + (y - focusView.getHeight()));
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(focusView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(focusView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(focusView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int left = clamp(Float.valueOf((y / screenHeight) * 2000 - 1000).intValue(), areaSize);
        int top = clamp(Float.valueOf(((screenWidth - x) / screenWidth) * 2000 - 1000).intValue(), areaSize);
        return new Rect(left, top, left + areaSize, top + areaSize);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
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

    /**
     * 判断屏幕方向
     *
     * @return 0：竖屏 1：左横屏 2：反向竖屏 3：右横屏
     */
    private int judgeScreenOrientation() {
        return getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        surfaceHolder.removeCallback(this);
        releaseCamera();
    }

    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.take_bt) {
            if (safeToTakePicture) {
                takePicture(currentOrientation);
                safeToTakePicture = false;
            }

        } else if (i == R.id.switch_camera_btn) {
            currentCameraFacing = 1 - currentCameraFacing;
            releaseCamera();
            initCamera();
            setCameraParams();

        }
//        else if (i == R.id.close_camera_btn) {
//            finish();
//
//        }
        else if (i == R.id.btn_retry) {
            previewLayout.setVisibility(View.GONE);
            mCamera.startPreview();
            safeToTakePicture = true;
            mCamera.cancelAutoFocus();

        } else if (i == R.id.btn_edit) {
            startActivityForResult(
                    new Intent(MyCameraActivity.this, IMGEditActivity.class)
                            .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, photoFilePath)
                            .putExtra(IMGEditActivity.EXTRA_ENCODING_TYPE, encodingType),
                    REQ_IMAGE_EDIT
            );

        } else if (i == R.id.btn_complete) {
            returnData();

        }
    }


    private void returnData() {
        Intent intent = new Intent();
        intent.putExtra(OUT_FILE_PATH, photoFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 拍照
     */
    private void takePicture(final int currentOrientation) {
        final long a = System.currentTimeMillis();
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mCamera.stopPreview();
                mCamera.cancelAutoFocus();
                int orientation = currentOrientation;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                originBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                //如果是三星手机需要先旋转90度
                boolean isSamSungType = originBitmap.getWidth() > originBitmap.getHeight();
                if (isSamSungType) {
                    originBitmap = ImageUtils.rotaingImageView(90, originBitmap);
                }
                //前置摄像头拍摄的照片和预览界面成镜面效果，需要翻转。
                if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    originBitmap = ImageUtils.rotaingImageViewByMirror(originBitmap);
                }
                //通过各种旋转和镜面操作，使originBitmap显示出preview界面
                long b = System.currentTimeMillis();
                LogUtils.jasonDebug("b-a=" + (b - a));
                LogUtils.jasonDebug("00000000000");
                cropBitmap = cameraCropView.getPicture(originBitmap);
                long c = System.currentTimeMillis();
                LogUtils.jasonDebug("c-b=" + (c - b));
                //界面进行旋转
                if (orientation != 0) {
                    cropBitmap = ImageUtils.rotaingImageView(orientation, cropBitmap);
                }
                long d = System.currentTimeMillis();
                LogUtils.jasonDebug("d-c=" + (d - c));
                savePhoto();
                previewImg.setImageBitmap(cropBitmap);
                previewLayout.setVisibility(View.VISIBLE);
                long e = System.currentTimeMillis();
                LogUtils.jasonDebug("e-d=" + (e - d));
            }
        });
    }

    private void savePhoto() {
        File photoDir = new File(photoSaveDirectoryPath);
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }
        File photoFile = new File(photoDir, photoName);
        photoFilePath = photoFile.getAbsolutePath();
        if (photoFile.exists()) {
            photoFile.delete();
        }
        BitmapUtils.saveBitmap(cropBitmap, photoFilePath, 100, encodingType);

    }

    private void showPreview() {
        ImageLoader.getInstance().displayImage("file://" + photoFilePath, previewImg);
        previewLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 回收Bitmap
     *
     * @param bitmap
     */
    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE_EDIT) {
            if (resultCode == RESULT_OK) {
                photoFilePath = data.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
                showPreview();
            }
        }

    }

    private boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        recycleBitmap(originBitmap);
        recycleBitmap(cropBitmap);
    }

    /**
     * 用来监测左横屏和右横屏切换时旋转摄像头的角度
     */
    private class DetectScreenOrientation extends OrientationEventListener {
        DetectScreenOrientation(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation > 350 || orientation < 10) { //0度
                currentOrientation = 0;
            } else if (orientation > 80 && orientation < 100) { //90度
                currentOrientation = 90;
            } else if (orientation > 170 && orientation < 190) { //180度
                currentOrientation = 180;
            } else if (orientation > 260 && orientation < 280) { //270度
                currentOrientation = 270;
            }

            if (260 < orientation && orientation < 280 && currentOrientation != 270) {
                setCameraParams();
            } else if (80 < orientation && orientation < 100 && currentOrientation != 90) {
                setCameraParams();
            }
        }
    }



}
