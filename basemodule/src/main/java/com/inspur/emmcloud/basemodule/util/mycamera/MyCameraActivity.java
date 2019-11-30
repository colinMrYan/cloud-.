package com.inspur.emmcloud.basemodule.util.mycamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.BitmapUtils;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.R2;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MyCameraActivity extends BaseFragmentActivity implements View.OnClickListener, SurfaceHolder.Callback {

    public static final String EXTRA_PHOTO_DIRECTORY_PATH = "IMAGE_SAVE_PATH";
    public static final String EXTRA_PHOTO_NAME = "IMAGE_NAME";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String EXTRA_RECT_SCALE_JSON = "CAMERA_SCALE_JSON";
    public static final String OUT_FILE_PATH = "OUT_FILE_PATH";
    private static final int REQ_IMAGE_EDIT = 1;
    @ViewInject(R2.id.set_radio_list)
    RecyclerView setRadioRecycleView;
    @ViewInject(R2.id.rl_preview)
    RelativeLayout previewLayout;
    @ViewInject(R2.id.iv_preview)
    ImageView previewImg;
    @ViewInject(R2.id.focus_view)
    FoucsView foucsView;
    private int encodingType = 0;
    private String photoFilePath;
    private String photoName;
    private FocusSurfaceView previewSFV;
    private RelativeLayout parentLayout;
    private ImageButton switchCameraBtn, cameraLightSwitchBtn;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int currentCameraFacing;
    private int currentOrientation = 0;
    private String cameraFlashModel = Camera.Parameters.FLASH_MODE_AUTO;
    private DetectScreenOrientation detectScreenOrientation;
    private String photoSaveDirectoryPath;
    private String defaultRectScale;
    private List<RectScale> rectScaleList = new ArrayList<>();
    private int radioSelectPosition = 0;
    private Bitmap originBitmap;
    private Bitmap cropBitmap;
    private boolean safeToTakePicture = false;

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
        if (getIntent().hasExtra(EXTRA_RECT_SCALE_JSON)) {
            String json = getIntent().getStringExtra(EXTRA_RECT_SCALE_JSON);
            JSONObject optionsObj = JSONUtils.getJSONObject(json, "options", new JSONObject());
            defaultRectScale = JSONUtils.getString(optionsObj, "rectScale", null);
            String rectScaleListJson = JSONUtils.getString(optionsObj, "rectScaleList", "");
            rectScaleList = new GetReatScaleResult(rectScaleListJson).getRectScaleList();
            if (!StringUtils.isBlank(defaultRectScale) && rectScaleList.size() > 0) {
                boolean isSelectionRadio = false;
                for (int i = 0; i < rectScaleList.size(); i++) {
                    String rectScale = rectScaleList.get(i).getRectScale();
                    if (rectScale.equals(defaultRectScale)) {
                        radioSelectPosition = i;
                        isSelectionRadio = true;
                        break;
                    }
                }
                if (!isSelectionRadio) {
                    for (int i = 0; i < rectScaleList.size(); i++) {
                        String rectScale = rectScaleList.get(i).getRectScale();
                        if (rectScale.equals("custom")) {
                            radioSelectPosition = i;
                            break;
                        }
                    }
                }
            }
        }


    }

    private void initView() {
        previewSFV = (FocusSurfaceView) findViewById(R.id.preview_sv);
        parentLayout = findViewById(R.id.rl_parent);
        mHolder = previewSFV.getHolder();
        mHolder.addCallback(MyCameraActivity.this);
        switchCameraBtn = (ImageButton) findViewById(R.id.switch_camera_btn);
        cameraLightSwitchBtn = (ImageButton) findViewById(R.id.camera_light_switch_btn);
        if (Camera.getNumberOfCameras() < 2) {
            switchCameraBtn.setVisibility(View.GONE);
        }
        if (rectScaleList.size() > 0) {
            setRadioRecycleView.setVisibility(View.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            setRadioRecycleView.setLayoutManager(linearLayoutManager);
            setRadioRecycleView.setAdapter(new Adapter());
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (rectScaleList.size() > 0) {
            previewSFV.setCustomRectScale(rectScaleList.get(radioSelectPosition).getRectScale());
        } else if (defaultRectScale != null) {
            previewSFV.setCustomRectScale(defaultRectScale);
        } else {
            previewSFV.setCropEnabled(false);
        }
        currentCameraFacing = hasBackFacingCamera() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        initCamera();
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
            if (modelList != null && modelList.contains(cameraFlashModel)) {
                parameters.setFlashMode(cameraFlashModel);
            }
            List<String> focusModeList = parameters.getSupportedFocusModes();
            if (focusModeList != null && focusModeList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            safeToTakePicture = true;
            mCamera.cancelAutoFocus();// 如果要实现连续的自动对焦，这一句必须加上，这句必须要在startPreview后面加上去

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) parentLayout.getLayoutParams();
            params.height = ResolutionUtils.getWidth(this) * pictureSize.width / pictureSize.height;
            parentLayout.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getPointerCount() == 1) {
                //显示对焦指示器
                setFocusViewWidthAnimation(event.getX(), event.getY());
            }
        }
        return super.onTouchEvent(event);
    }


    public void setFocusViewWidthAnimation(final float x, final float y, final FocusCallback callback) {
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f, context);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
            callback.focusSuccess();
            return;
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
                        callback.focusSuccess();
                    } else {
                        handlerTime++;
                        handleFocus(context, x, y, callback);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "autoFocus failer");
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient, Context context) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / ResolutionUtils.getWidth(context) * 2000 - 1000);
        int centerY = (int) (y / ResolutionUtils.getHeight(context) * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
                .bottom));
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
            cameraLightSwitchBtn.setVisibility((currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) ? View.INVISIBLE : View.VISIBLE);
            releaseCamera();
            initCamera();
            setCameraParams();

        } else if (i == R.id.close_camera_btn) {
            finish();

        } else if (i == R.id.camera_light_switch_btn) {
            Camera.Parameters parameters = mCamera.getParameters();
            boolean isCameraFlashAutoModel = cameraFlashModel.equals(Camera.Parameters.FLASH_MODE_AUTO);
            parameters.setFlashMode(isCameraFlashAutoModel ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_AUTO);
            cameraLightSwitchBtn.setImageResource(isCameraFlashAutoModel ? R.drawable.plugin_cemera_btn_camera_light_close : R.drawable.plugin_cemera_btn_camera_light_on);
            cameraFlashModel = isCameraFlashAutoModel ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_AUTO;
            mCamera.setParameters(parameters);

        } else if (i == R.id.btn_retry) {
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
                cropBitmap = previewSFV.getPicture(originBitmap);
                //界面进行旋转
                if (orientation != 0) {
                    cropBitmap = ImageUtils.rotaingImageView(orientation, cropBitmap);
                }
                savePhoto();
                previewImg.setImageBitmap(cropBitmap);
                previewLayout.setVisibility(View.VISIBLE);
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

    public class Adapter extends
            RecyclerView.Adapter<Adapter.ViewHolder> {


        @Override
        public int getItemCount() {
            return rectScaleList.size();
        }

        /**
         * 创建ViewHolder
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
            TextView textView = new TextView(MyCameraActivity.this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setPadding(DensityUtil.dip2px(MyCameraActivity.this, 20), 0, DensityUtil.dip2px(MyCameraActivity.this, 20), 0);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ViewHolder viewHolder = new ViewHolder(textView);
            viewHolder.textView = textView;
            return viewHolder;
        }

        /**
         * 设置值
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            viewHolder.textView.setText(rectScaleList.get(i).getName());
            viewHolder.textView.setTextColor((radioSelectPosition == i) ? Color.parseColor("#CB602D") : Color.parseColor("#FFFFFB"));
            viewHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioSelectPosition = i;
                    Adapter.this.notifyDataSetChanged();
                    RectScale rectScale = rectScaleList.get(i);
                    previewSFV.setCustomRectScale(rectScale.getRectScale());
                }
            });

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View arg0) {
                super(arg0);
            }
        }

    }


}
