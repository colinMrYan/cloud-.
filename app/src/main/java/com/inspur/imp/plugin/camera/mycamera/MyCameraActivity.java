package com.inspur.imp.plugin.camera.mycamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.imp.api.ImpBaseActivity;
import com.inspur.imp.plugin.camera.editimage.EditImageActivity;
import com.inspur.imp.plugin.camera.editimage.utils.BitmapUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.R.attr.path;
import static com.inspur.imp.plugin.camera.editimage.EditImageActivity.ACTION_REQUEST_EDITIMAGE;

public class MyCameraActivity extends ImpBaseActivity implements View.OnClickListener, SurfaceHolder.Callback {

    public static final String PHOTO_DIRECTORY_PATH = "save_derectory_path";
    public static final String PHOTO_NAME = "photo_name";
    public static final String PHOTO_PARAM = "upload_parm";
    private FocusSurfaceView previewSFV;
    private Button mTakeBT, mThreeFourBT, mFourThreeBT, mNineSixteenBT, mSixteenNineBT;
    private ImageButton switchCameraBtn, cameraLightSwitchBtn;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int currentCameraFacing;
    private int currentOrientation = 0;
    private String cameraFlashModel = Camera.Parameters.FLASH_MODE_AUTO;
    private DetectScreenOrientation detectScreenOrientation;
    private String photoSaveDirectoryPath;
    private String photoName;
    private String extraParam;
    private String defaultRectScale;
    private RecyclerView setRadioRecycleView;
    private List<RectScale> rectScaleList;
    private int radioSelectPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(this, R.string.filetransfer_sd_not_exist);
            finish();
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        setContentView(R.layout.activity_mycamera);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        setListener();
    }

    private void initData() {
        if (detectScreenOrientation == null) {
            detectScreenOrientation = new DetectScreenOrientation(this);
        }
        detectScreenOrientation.enable();
        photoSaveDirectoryPath = getIntent().getStringExtra(PHOTO_DIRECTORY_PATH);
        photoName = getIntent().getStringExtra(PHOTO_NAME);
        extraParam = getIntent().getStringExtra(PHOTO_PARAM);
        JSONObject optionsObj = JSONUtils.getJSONObject(extraParam,"options",new JSONObject());
        defaultRectScale = JSONUtils.getString(optionsObj,"rectScale",null);
        String rectScaleListJson = JSONUtils.getString(optionsObj,"rectScaleList","");
        rectScaleList = new GetReatScaleResult(rectScaleListJson).getRectScaleList();

    }

    private void initView() {
        previewSFV = (FocusSurfaceView) findViewById(R.id.preview_sv);
        mHolder = previewSFV.getHolder();
        mHolder.addCallback(MyCameraActivity.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mTakeBT = (Button) findViewById(R.id.take_bt);
        mThreeFourBT = (Button) findViewById(R.id.three_four_bt);
        mFourThreeBT = (Button) findViewById(R.id.four_three_bt);
        mNineSixteenBT = (Button) findViewById(R.id.nine_sixteen_bt);
        mSixteenNineBT = (Button) findViewById(R.id.sixteen_nine_bt);
        switchCameraBtn = (ImageButton) findViewById(R.id.switch_camera_btn);
        cameraLightSwitchBtn = (ImageButton) findViewById(R.id.camera_light_switch_btn);
        if (Camera.getNumberOfCameras() < 2) {
            switchCameraBtn.setVisibility(View.GONE);
        }
        setRadioRecycleView = (RecyclerView) findViewById(R.id.set_radio_list);
        if (rectScaleList.size() > 0){
            setRadioRecycleView.setVisibility(View.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            setRadioRecycleView.setLayoutManager(linearLayoutManager);
            setRadioRecycleView.setAdapter(new Adapter());

        }
    }


    private void setListener() {
        mTakeBT.setOnClickListener(this);
        mThreeFourBT.setOnClickListener(this);
        mFourThreeBT.setOnClickListener(this);
        mNineSixteenBT.setOnClickListener(this);
        mSixteenNineBT.setOnClickListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (rectScaleList.size()>0){
            previewSFV.setCustomRectScale(rectScaleList.get(0).getRectScale());
        }else {
            previewSFV.setCustomRectScale(defaultRectScale);
        }

        currentCameraFacing = hasBackFacingCamera() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        initCamera();
        setCameraParams();
    }

    private void initCamera() {
        if (checkPermission()) {
            try {
                mCamera = Camera.open(currentCameraFacing);//1:采集指纹的摄像头. 0:拍照的摄像头.

                Camera.Parameters mParameters = mCamera.getParameters();
                mCamera.setParameters(mParameters);
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),R.string.open_camera_fail_by_perminssion,Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(),R.string.open_camera_fail_by_perminssion,Toast.LENGTH_LONG).show();
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
            Camera.Size previewSize = CameraUtils.getInstance(this).getPreviewSize(previewSizeList, 1300);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            List<String> modelList = parameters.getSupportedFlashModes();
            if (modelList != null && modelList.contains(cameraFlashModel)){
                parameters.setFlashMode(cameraFlashModel);
            }
            List<String> focusModeList = parameters.getSupportedFocusModes();
            if (focusModeList != null && focusModeList.contains(parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_bt:
                takePicture(currentOrientation);
                break;
            case R.id.three_four_bt:
                previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_3_4);
                break;
            case R.id.four_three_bt:
                previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_3_4);
                break;
            case R.id.nine_sixteen_bt:
                previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_9_16);
                break;
            case R.id.sixteen_nine_bt:
                previewSFV.setCropMode(FocusSurfaceView.CropMode.FREE);
                break;
            case R.id.switch_camera_btn:
                currentCameraFacing = 1 - currentCameraFacing;
                releaseCamera();
                initCamera();
                setCameraParams();
                break;
            case R.id.close_camera_btn:
                finish();
                break;
            case R.id.camera_light_switch_btn:
                Camera.Parameters parameters = mCamera.getParameters();
                if (cameraFlashModel.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cameraLightSwitchBtn.setImageResource(R.drawable.plugin_cemera_btn_camera_light_close);
                    cameraFlashModel = Camera.Parameters.FLASH_MODE_OFF;
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    cameraLightSwitchBtn.setImageResource(R.drawable.plugin_cemera_btn_camera_light_on);
                    cameraFlashModel = Camera.Parameters.FLASH_MODE_AUTO;
                }
                mCamera.setParameters(parameters);
                break;
            default:
                break;
        }
    }

    /**
     * 拍照
     */
    private void takePicture(final int currentOrientation) {
        mCamera.cancelAutoFocus();
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
            }
        }, null, null, new Camera.PictureCallback() {


            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                int orientation = currentOrientation;
                Bitmap originBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                boolean isSamSungType = originBitmap.getWidth()>originBitmap.getHeight();
                if (isSamSungType){
                    originBitmap = rotaingImageView(90, originBitmap);
                }
                //前置摄像头和后置摄像头拍照后图像角度旋转
                Bitmap cropBitmap = previewSFV.getPicture(originBitmap);
                if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    orientation = (540 - orientation) % 360;
                }
                if (orientation != 0) {
                    cropBitmap = rotaingImageView(orientation, cropBitmap);
                }
                File photoDir = null;
                if (photoSaveDirectoryPath != null) {
                    photoDir = new File(photoSaveDirectoryPath);
                } else {
                    photoDir = new File(Environment.getExternalStorageDirectory(),
                            "DCIM");
                }
                if (!photoDir.exists()) {
                    photoDir.mkdir();
                }
               if (photoName == null){
                   photoName = System.currentTimeMillis()+".jpg";
               }
                String imgPath = photoDir.getAbsolutePath()+"/"+photoName;
                BitmapUtils.saveBitmap(cropBitmap, imgPath,100,0);
                recycleBitmap(originBitmap);
                recycleBitmap(cropBitmap);
                EditImageActivity.start(MyCameraActivity.this, imgPath, MyAppConfig.LOCAL_IMG_CREATE_PATH,true,extraParam);
            }
        });
    }

    /**
     * 回收Bitmap
     * @param bitmap
     */
    public void recycleBitmap(Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_REQUEST_EDITIMAGE){
            if (resultCode == RESULT_OK ){
                setResult(RESULT_OK,data);
            }
            finish();
        }

    }

    public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
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


    /**
     * 保存并显示把图片展示出来
     * @param cameraPath
     */
    private  void refreshGallery( String cameraPath) {
        File file = new File(cameraPath);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
       sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }


    public class Adapter extends
            RecyclerView.Adapter<Adapter.ViewHolder>
    {


        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public ViewHolder(View arg0)
            {
                super(arg0);
            }

            TextView textView;
        }

        @Override
        public int getItemCount()
        {
            return rectScaleList.size();
        }

        /**
         * 创建ViewHolder
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i)
        {
            TextView textView = new TextView(MyCameraActivity.this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            textView.setPadding(DensityUtil.dip2px(MyCameraActivity.this,20),DensityUtil.dip2px(MyCameraActivity.this,8),DensityUtil.dip2px(MyCameraActivity.this,20),DensityUtil.dip2px(MyCameraActivity.this,8));
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            ViewHolder viewHolder = new ViewHolder(textView);
            viewHolder.textView = textView;
            return viewHolder;
        }

        /**
         * 设置值
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i)
        {
            viewHolder.textView.setText(rectScaleList.get(i).getName());
            viewHolder.textView.setTextColor((radioSelectPosition == i)? Color.parseColor("#CB602D"):Color.parseColor("#FFFFFB"));
            viewHolder.textView.setBackgroundColor((radioSelectPosition == i)?Color.parseColor("#323232"):Color.parseColor("#00000000"));
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

    }


}
