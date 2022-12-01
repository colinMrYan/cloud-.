package com.inspur.emmcloud.basemodule.util.mycamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.BitmapUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.R2;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.JCameraView;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.ClickListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.JCameraListener;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyCameraActivity extends BaseFragmentActivity implements JCameraListener, ClickListener ,NotSupportLand{

    public static final String EXTRA_PHOTO_DIRECTORY_PATH = "IMAGE_SAVE_PATH";
    public static final String EXTRA_PHOTO_NAME = "IMAGE_NAME";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String EXTRA_RECT_SCALE_JSON = "CAMERA_SCALE_JSON";
    public static final String OUT_FILE_PATH = "OUT_FILE_PATH";
    public static final int REQ_IMAGE_EDIT = 1;
    @BindView(R2.id.camera_view)
    JCameraView jCameraView;
    private boolean granted = false;
    private String photoFilePath;
    private int encodingType = 0;
    private String photoName;
    private String photoSaveDirectoryPath;


    @Override
    public void onCreate() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(this, R.string.baselib_sd_not_exist);
            finish();
        }
        String[] permissions = new String[]{Permissions.CAMERA, Permissions.READ_EXTERNAL_STORAGE,
                Permissions.WRITE_EXTERNAL_STORAGE, Permissions.FLASHLIGHT};
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, permissions, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                granted = true;
                setContentView(R.layout.activity_my_camera);
                setWindows();
                ButterKnife.bind(MyCameraActivity.this);
                initData();
                initView();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(MyCameraActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(MyCameraActivity.this, permissions));
                finish();
            }
        });

    }

    private void initData() {
        photoSaveDirectoryPath = getIntent().getExtras().getString(EXTRA_PHOTO_DIRECTORY_PATH, Environment.getExternalStorageDirectory() + "/DCIM/");
        photoName = getIntent().getExtras().getString(EXTRA_PHOTO_NAME, System.currentTimeMillis() + ".jpg");
        encodingType = getIntent().getIntExtra(EXTRA_ENCODING_TYPE, 0);
    }

    private void initView() {
        if (getIntent().hasExtra(EXTRA_RECT_SCALE_JSON)) {
            String json = getIntent().getStringExtra(EXTRA_RECT_SCALE_JSON);
            jCameraView.setCropData(json);
        }
        jCameraView.setJCameraLisenter(this);
        jCameraView.setLeftClickListener(this);
    }

    @Override
    public void onClick() {
        finish();
    }

    @Override
    public void captureSuccess(Bitmap bitmap) {
        RectScale rectScale = jCameraView.getSelectRectScale();
        if (rectScale != null && ("增值税".equals(rectScale.getName()) || "火车票".equals(rectScale.getName()))) {
            Matrix m = new Matrix();
            m.postRotate(-90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                    true);
        }
        savePhoto(bitmap);
        startActivityForResult(
                new Intent(MyCameraActivity.this, IMGEditActivity.class)
                        .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, photoFilePath)
                        .putExtra(IMGEditActivity.EXTRA_ENCODING_TYPE, encodingType), REQ_IMAGE_EDIT
        );
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @Override
    public void recordSuccess(String url, Bitmap firstFrame) {

    }


    private void savePhoto(Bitmap bitmap) {
        File photoDir = new File(photoSaveDirectoryPath);
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }
        File photoFile = new File(photoDir, photoName);
        photoFilePath = photoFile.getAbsolutePath();
        if (photoFile.exists()) {
            photoFile.delete();
        }
        BitmapUtils.saveBitmap(bitmap, photoFilePath, 100, encodingType);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE_EDIT) {
            if (resultCode == RESULT_OK) {
                photoFilePath = data.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
                returnData();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setWindows();

    }

    private void setWindows() {
//        //全屏显示
//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        } else {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(option);
//        }
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).fullScreen(true).transparentNavigationBar().init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (jCameraView != null && granted) {
            jCameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (jCameraView != null) {
            jCameraView.onPause();
        }

    }


    private void returnData() {
        Intent intent = new Intent();
        intent.putExtra(OUT_FILE_PATH, photoFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        if (jCameraView != null) {
            jCameraView.onDestroy();
        }
        super.onDestroy();
    }


}
