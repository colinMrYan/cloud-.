package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.util.mycamera.CameraUtils;
import com.inspur.emmcloud.basemodule.util.mycamera.FocusSurfaceView;
import com.inspur.emmcloud.bean.mine.GetFaceSettingResult;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.privates.ProfileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static android.Manifest.permission.CAMERA;


/**
 * 面容解锁识别页面
 */
public class FaceVerifyActivity extends BaseActivity implements SurfaceHolder.Callback {

    public static final String FACE_VERIFT_IS_OPEN = "face_verify_isopen";
    private static final int TIMEOUT_TIME = 20000;
    private FocusSurfaceView previewSFV;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int currentCameraFacing;
    private int currentOrientation = 0;
    private String cameraFlashModel = Camera.Parameters.FLASH_MODE_AUTO;
    private DetectScreenOrientation detectScreenOrientation;
    private MineAPIService apiService;
    private boolean isFaceSetting = false;
    private boolean isFaceSettingOpen = true;
    private boolean isFaceVerityTest = false;
    private TextView tipText;
    private Handler handler;
    private Runnable takePhotoRunnable;
    private Runnable keepBodyRunnable;
    private long startTime;
    private boolean isFaceLogin = false;
    private String token = "";

    /**
     * 根据用户获取是否打开了gesturecode
     *
     * @param context
     * @return
     */
    public static boolean getFaceVerifyIsOpenByUser(Context context) {
        return PreferencesByUsersUtils.getBoolean(context, FaceVerifyActivity.FACE_VERIFT_IS_OPEN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(this, R.string.baselib_sd_not_exist);
            finish();
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        new ProfileUtils(FaceVerifyActivity.this, new CommonCallBack() {
            @Override
            public void execute() {

            }
        }).initProfile(false);
        init();
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_face_verification;
    }

    protected int getStatusType() {
        return STATUS_WHITE;
    }


    private void init() {
        previewSFV = (FocusSurfaceView) findViewById(R.id.preview_sv);
        int previewSFVWidth = (int) (ResolutionUtils.getWidth(FaceVerifyActivity.this) * 0.65);
        int previewSFVHeight = (int) (previewSFVWidth * 4 / 3);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(previewSFVWidth, previewSFVHeight);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(0, DensityUtil.dip2px(MyApplication.getInstance(), 60), 0, 0);
        previewSFV.setLayoutParams(params);
        previewSFV.setEnabled(false);
        if (detectScreenOrientation == null) {
            detectScreenOrientation = new DetectScreenOrientation(this);
        }
        detectScreenOrientation.enable();
        handler = new Handler();
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        tipText = (TextView) findViewById(R.id.tip_text);
        isFaceVerityTest = getIntent().getExtras().getBoolean("isFaceVerifyExperience", false);
        isFaceSetting = getIntent().hasExtra("isFaceSettingOpen");
        if (isFaceSetting) {
            isFaceSettingOpen = getIntent().getBooleanExtra("isFaceSettingOpen", true);
        }
        isFaceLogin = getIntent().getBooleanExtra("isFaceLogin", false);
        if (isFaceLogin) {
            token = getIntent().getStringExtra("token");
        }
        takePhotoRunnable = new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        };
        keepBodyRunnable = new Runnable() {
            @Override
            public void run() {
                tipText.setVisibility(View.VISIBLE);
                tipText.setText(getString(R.string.put_body_right));
            }
        };
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHolder = previewSFV.getHolder();
        mHolder.addCallback(FaceVerifyActivity.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        previewSFV.setCustomRatio(1, 1);
        currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        initCamera();
        setCameraParams();
        delayToNotifyKeepBody();
        delayTotakePicture(2000);
    }

    private void initCamera() {
        if (checkPermission()) {
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
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 10000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10000:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        initCamera();
                        setCameraParams();
                        break;
                    }
                }
            ToastUtils.show(getApplicationContext(), R.string.open_camera_fail_by_perminssion);
                finish();
                break;
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
            List<Camera.Size> PictureSizeList = parameters.getSupportedPictureSizes();
            Camera.Size pictureSize = CameraUtils.getInstance(this).getPictureSize(PictureSizeList, 1000);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = CameraUtils.getInstance(this).getPreviewSize(previewSizeList, 1400);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            List<String> modelList = parameters.getSupportedFlashModes();
            if (modelList != null && modelList.contains(cameraFlashModel)) {
                parameters.setFlashMode(cameraFlashModel);
            }
            List<String> focusModeList = parameters.getSupportedFocusModes();
            if (focusModeList != null && focusModeList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();// 如果要实现连续的自动对焦，这一句必须加上，这句必须要在startPreview后面加上去


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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

    private void delayToNotifyKeepBody() {
        handler.postDelayed(keepBodyRunnable, 1500);
    }

    private void delayTotakePicture(long time) {
        handler.postDelayed(takePhotoRunnable, time);
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mCamera == null) {
            return;
        }
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                mCamera.setPreviewCallback(null);
                Camera.Size size = camera.getParameters().getPreviewSize();
                try {
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 90, stream);

                        Bitmap originBitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                        stream.close();
                        //前置摄像头拍摄的照片和预览界面成镜面效果，需要翻转。
                        Bitmap mirrorOriginBitmap = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(mirrorOriginBitmap);
                        Paint paint = new Paint();
                        paint.setColor(Color.BLACK);
                        paint.setAntiAlias(true);
                        Matrix matrix = new Matrix();
                        //镜子效果
                        matrix.setScale(-1, 1);
                        matrix.postTranslate(originBitmap.getWidth(), 0);
                        canvas.drawBitmap(originBitmap, matrix, paint);
                        originBitmap = mirrorOriginBitmap;
                        originBitmap = ImageUtils.rotaingImageView(90, originBitmap);
                        //通过各种旋转和镜面操作，使originBitmap显示出preview界面
                        Bitmap cropBitmap = previewSFV.getPicture(originBitmap);
                        // String filePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + System.currentTimeMillis() + ".png";
                        //  ImageUtils.saveImageToSD(getApplicationContext(),filePath, cropBitmap, 100);
                        cropBitmap = ImageUtils.scaleBitmap(cropBitmap, 250);
                        //ImageUtils.saveImageToSD(getApplicationContext(), MyAppConfig.LOCAL_DOWNLOAD_PATH + System.currentTimeMillis() + ".png", cropBitmap, 100);
                        FaceDetector faceDetector = new FaceDetector(cropBitmap.getWidth(), cropBitmap.getHeight(), 1);
                        FaceDetector.Face[] faces = new FaceDetector.Face[1];
                        int faceNum = faceDetector.findFaces(cropBitmap, faces);
                        //当检测不到脸部时重新拍照
                        if (faceNum < 1) {
                            handResultCode(201);
                        } else {
                            if (isFaceLogin) {
                                ImageUtils.saveImage(getApplicationContext(), "face_unlock.png", cropBitmap);
                            }
                            tipText.setVisibility(View.VISIBLE);
                            tipText.setText(R.string.face_verifying);
                            if (isFaceSetting) {
                                faceSetting(cropBitmap);
                            } else {
                                faceVerify(cropBitmap);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    delayTotakePicture(1000);
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_camera_btn:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isFaceSetting || isFaceVerityTest) {
            finish();
        } else {
            MyApplication.getInstance().exit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (handler != null) {
            handler.removeCallbacks(takePhotoRunnable);
            handler.removeCallbacks(keepBodyRunnable);
            handler = null;
        }
        releaseCamera();
    }

    /**
     * @param code
     */
    private void handResultCode(int code) {
        switch (code) {
            case 200:
                tipText.setVisibility(View.GONE);
                ToastUtils.show(getApplicationContext(), getString(R.string.face_verify_success));
                if (isFaceLogin) {
                    Bundle bundle = new Bundle();
                    bundle.putString("token", token);
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_GS).with(bundle).navigation();
                } else if (isFaceSetting) {
                    PreferencesByUsersUtils.putBoolean(FaceVerifyActivity.this, FaceVerifyActivity.FACE_VERIFT_IS_OPEN, isFaceSettingOpen);
                } else if (!isFaceVerityTest) {
                    MyApplication.getInstance().setSafeLock(false);
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SAFE_UNLOCK));
                }
                finish();
                break;
            case 201:
                if (!checkIsTimeout()) {
                    tipText.setVisibility(View.VISIBLE);
                    tipText.setText(getString(R.string.no_face));
                    delayTotakePicture(1000);
                }
                break;
            case 202:
                if (!checkIsTimeout()) {
                    tipText.setVisibility(View.VISIBLE);
                    tipText.setText(getString(R.string.put_body_right));
                    delayTotakePicture(1000);
                }
                break;
            case 203:
                if (!checkIsTimeout()) {
                    tipText.setVisibility(View.VISIBLE);
                    tipText.setText(getString(R.string.get_closer));
                    delayTotakePicture(1000);
                }
                break;
            case 204:
                if (!checkIsTimeout()) {
                    tipText.setVisibility(View.VISIBLE);
                    tipText.setText(getString(R.string.blink));
                    delayTotakePicture(1000);
                }
                break;
            case 400:
                tipText.setVisibility(View.GONE);
                showFaceVerifyFailDlg();
                break;
            default:
                if (!checkIsTimeout()) {
                    tipText.setVisibility(View.GONE);
                    delayTotakePicture(1000);
                }
                break;
        }
    }

    private boolean checkIsTimeout() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= TIMEOUT_TIME) {
            showFaceVerifyFailDlg();
            return true;
        }
        return false;
    }

    /**
     * 弹出人脸验证失败弹出框
     */
    private void showFaceVerifyFailDlg() {
        if (isFaceSetting || isFaceVerityTest) {
            new CustomDialog.MessageDialogBuilder(FaceVerifyActivity.this)
                    .setMessage(R.string.face_verify_fail)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else if (CreateGestureActivity.getGestureCodeIsOpenByUser(FaceVerifyActivity.this)) {
            new CustomDialog.MessageDialogBuilder(FaceVerifyActivity.this)
                    .setMessage(R.string.face_verify_fail)
                    .setNegativeButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startTime = System.currentTimeMillis();
                            delayTotakePicture(1000);
                        }
                    })
                    .setPositiveButton(R.string.switch_gesture_unlock, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(FaceVerifyActivity.this, GestureLoginActivity.class);
                            intent.putExtra("gesture_code_change", "login");
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            new CustomDialog.MessageDialogBuilder(FaceVerifyActivity.this)
                    .setMessage(R.string.face_verify_fail)
                    .setNegativeButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startTime = System.currentTimeMillis();
                            delayTotakePicture(1000);
                        }
                    })
                    .setPositiveButton(R.string.off_face_verify_relogin, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MyApplication) getApplication()).signout();
                            PreferencesByUsersUtils.putBoolean(FaceVerifyActivity.this, FaceVerifyActivity.FACE_VERIFT_IS_OPEN, false);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_SAFE_UNLOCK)) {
            finish();
        }
    }

    /**
     * 设置脸部图像
     *
     * @param bitmap
     */
    private void faceSetting(Bitmap bitmap) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpeg_data);
            byte[] code = jpeg_data.toByteArray();
            byte[] output = Base64.encode(code, Base64.NO_WRAP);
            String js_out = new String(output);
            apiService.faceSetting(js_out);
        } else {
            delayTotakePicture(1500);
            tipText.setVisibility(View.GONE);

        }

    }

    /**
     * 验证脸部图像
     *
     * @param bitmap
     */
    private void faceVerify(Bitmap bitmap) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpeg_data);
            byte[] code = jpeg_data.toByteArray();
            byte[] output = Base64.encode(code, Base64.NO_WRAP);
            String js_out = new String(output);
            apiService.faceVerify(js_out);
        } else {
            delayTotakePicture(1500);
            tipText.setVisibility(View.GONE);
        }

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

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnFaceSettingSuccess(GetFaceSettingResult getFaceSettingResult) {
            int code = getFaceSettingResult.getCode();
            handResultCode(code);

        }

        @Override
        public void returnFaceSettingFail(String error, int errorCode) {
            handResultCode(-1);
        }

        @Override
        public void returnFaceVerifySuccess(GetFaceSettingResult getFaceSettingResult) {
            int code = getFaceSettingResult.getCode();
            handResultCode(code);
        }

        @Override
        public void returnFaceVerifyFail(String error, int errorCode) {
            handResultCode(-1);
        }
    }
}
