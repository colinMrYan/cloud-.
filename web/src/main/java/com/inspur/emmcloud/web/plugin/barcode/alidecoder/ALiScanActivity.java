
package com.inspur.emmcloud.web.plugin.barcode.alidecoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.mobile.bqcscanservice.BQCScanCallback;
import com.alipay.mobile.bqcscanservice.BQCScanEngine;
import com.alipay.mobile.bqcscanservice.BQCScanError;
import com.alipay.mobile.bqcscanservice.CameraHandler;
import com.alipay.mobile.bqcscanservice.impl.MPaasScanServiceImpl;
import com.alipay.mobile.common.logging.api.LoggerFactory;
import com.alipay.mobile.mascanengine.MaScanCallback;
import com.alipay.mobile.mascanengine.MaScanEngineService;
import com.alipay.mobile.mascanengine.MaScanResult;
import com.alipay.mobile.mascanengine.impl.MaScanEngineServiceImpl;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.camera.ScanHandler;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.camera.ScanType;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.widget.APTextureView;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.widget.ScanView;
import com.taobao.ma.camera.CameraManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ALiScanActivity extends BaseActivity implements NotSupportLand {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_PHOTO = 2;
    private final String TAG = ALiScanActivity.class.getSimpleName();
    private Button mTorchBtn;
    private TextView lampText;
    private APTextureView mSurfaceView;
    private ScanView mScanView;
    private View coverView;
    private MPaasScanServiceImpl bqcScanService;

    private boolean isFirstStart = true;
    private boolean isScanning;
    private boolean isPaused;

    private CameraHandler cameraHandler;
    private ScanHandler scanHandler;
    private Rect scanRect;
    private long postcode = -1;
    private LoadingDialog loadingDlg;
    private AlertDialog noQrCodeWarningDlg;
    private BQCScanCallback bqcScanCallback = new BQCScanCallback() {
        @Override
        public void onParametersSetted(final long pcode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Field field = bqcScanService.getClass().getSuperclass().getDeclaredField("cameraManager");
                        field.setAccessible(true);
                        CameraManager cameraManager = (CameraManager) field.get(bqcScanService);
                        cameraManager.setAutoFocusInterval(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    postcode = pcode;
                    bqcScanService.setDisplay(mSurfaceView);
                    cameraHandler.onSurfaceViewAvailable();
                    scanHandler.registerAllEngine();
                    scanHandler.setScanType(ScanType.SCAN_MA);
                    scanHandler.enableScan();
                    mScanView.onStartScan();
                }
            });
        }

        @Override
        public void onSurfaceAvaliable() {
            if (!isPaused && bqcScanService != null) {
                cameraHandler.onSurfaceViewAvailable();
            }
        }

        @Override
        public void onPreviewFrameShow() {
            coverView.setVisibility(View.GONE);
            if (!isPaused) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            initScanRect();
                        }
                    }
                });
            }
        }

        @Override
        public void onError(final BQCScanError bqcError) {
            if (!isPaused) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), bqcError.msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onCameraOpened() {
        }

        @Override
        public void onCameraAutoFocus(boolean success) {
        }

        @Override
        public void onOuterEnvDetected(boolean shouldShow) {
        }

        @Override
        public void onCameraReady() {
        }

    };

    @Override
    public void onCreate() {
        // 设置沉浸模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(ContextCompat.getColor(BaseApplication.getInstance(), android.R.color.black));
        }

        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                setContentView(R.layout.web_activity_barcode_ali_scan);
                initView();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(BaseApplication.getInstance(), PermissionRequestManagerUtils.getInstance().getPermissionToast(BaseApplication.getInstance(), permissions));
                finish();
            }

        });
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_view);
        mScanView = findViewById(R.id.scan_view);
        mTorchBtn = findViewById(R.id.bt_torch);
        lampText = findViewById(R.id.tv_lamp);
        coverView = findViewById(R.id.view_cover);
        loadingDlg = new LoadingDialog(this);
        findViewById(R.id.iv_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.openGallery(ALiScanActivity.this, 1, REQUEST_CODE_PHOTO, false);
            }
        });
        mTorchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTorch();
            }
        });
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initScanService();
        initScanHandler();
        cameraHandler = bqcScanService.getCameraHandler();
        startScan();
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    @Override
    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    private void switchTorch() {
        if (bqcScanService != null) {
            boolean torchOn = bqcScanService.isTorchOn();
            bqcScanService.setTorch(!torchOn);
            lampText.setText(torchOn ? getString(R.string.turn_on_light) : getString(R.string.turn_off_light));
            mTorchBtn.setBackgroundResource(torchOn ? R.drawable.web_qrcode_lamp_off : R.drawable.web_qrcode_lamp_on);
        }
    }

    private void initScanService() {
        LoggerFactory.init(getApplicationContext());
        bqcScanService = new MPaasScanServiceImpl();
        bqcScanService.serviceInit();
    }

    private void initScanHandler() {
        scanHandler = new ScanHandler();
        scanHandler.setBqcScanService(bqcScanService);
        scanHandler.setContext(this, new ScanHandler.ScanResultCallbackProducer() {
            @Override
            public BQCScanEngine.EngineCallback makeScanResultCallback(ScanType type) {
                BQCScanEngine.EngineCallback maCallback = null;
                if (type == ScanType.SCAN_MA) {
                    maCallback = new MaScanCallback() {
                        @Override
                        public void onResultMa(MaScanResult maScanResult) {
                            handScanResult(maScanResult);
                        }
                    };

                }
                return maCallback;
            }
        });
    }

    private void handScanResult(MaScanResult maScanResult) {
        if (noQrCodeWarningDlg != null && noQrCodeWarningDlg.isShowing()) {
            noQrCodeWarningDlg.dismiss();
        }
        scanHandler.shootSound();
        String result = "";
        if (maScanResult != null) {
            result = maScanResult.text;
        }
        Intent intent = new Intent();
        if (StringUtils.isBlank(result)) {
            result = getString(Res.getStringID("web_can_not_recognize"));
            intent.putExtra("isDecodeSuccess", false);
        } else {
            intent.putExtra("isDecodeSuccess", true);
        }
        intent.putExtra("msg", result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if (isScanning) {
            stopScan();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        if (!isFirstStart) {
            startScan();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scanHandler != null) {
            scanHandler.removeContext();
            scanHandler.destroy();
        }
    }

    private void scanFromLocalImg(String filePath) {
        final Bitmap bitmap = ImageUtils.getBitmapByPath(filePath);
        ;
        if (bitmap == null) {
            notifyScanResult(true, null);
        } else {
            loadingDlg.show();
            Observable.create(new ObservableOnSubscribe<MaScanResult>() {
                @Override
                public void subscribe(ObservableEmitter<MaScanResult> emitter) throws Exception {
                    MaScanEngineService maScanEngineService = new MaScanEngineServiceImpl();
                    MaScanResult result = maScanEngineService.process(bitmap);
                    if (result == null) {
                        throw new Exception();
                    } else {
                        emitter.onNext(result);
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<MaScanResult>() {
                        @Override
                        public void accept(MaScanResult maScanResult) throws Exception {
                            LoadingDialog.dimissDlg(loadingDlg);
                            handScanResult(maScanResult);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            LoadingDialog.dimissDlg(loadingDlg);
                            showNoQrCodeWarningDlg();
                        }
                    });
        }
    }

    private void showNoQrCodeWarningDlg() {
        noQrCodeWarningDlg = new CustomDialog.MessageDialogBuilder(ALiScanActivity.this)
                .setMessage(R.string.web_qrcode_not_found)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void startScan() {
        try {
            isScanning = true;
            cameraHandler.init(this, bqcScanCallback);
            cameraHandler.openCamera();
        } catch (Exception e) {
            isScanning = false;
            LoggerFactory.getTraceLogger().error(TAG, "startScan: Exception " + e.getMessage());
        }
    }

    private void stopScan() {
        mScanView.onStopScan();
        cameraHandler.closeCamera();
        scanHandler.disableScan();
        cameraHandler.release(postcode);
        isScanning = false;
        if (isFirstStart) {
            isFirstStart = false;
        }
    }

    private void initScanRect() {
        if (scanRect == null) {
            scanRect = mScanView.getScanRect(
                    bqcScanService.getCamera(), mSurfaceView.getWidth(), mSurfaceView.getHeight());

            float cropWidth = mScanView.getCropWidth();
            LoggerFactory.getTraceLogger().debug(TAG, "cropWidth: " + cropWidth);
            if (cropWidth > 0) {
                // 预览放大 ＝ 屏幕宽 ／ 裁剪框宽
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                float screenWith = wm.getDefaultDisplay().getWidth();
                float screenHeight = wm.getDefaultDisplay().getHeight();
                float previewScale = screenWith / cropWidth;
                if (previewScale < 1.0f) {
                    previewScale = 1.0f;
                }
                if (previewScale > 1.5f) {
                    previewScale = 1.5f;
                }
                LoggerFactory.getTraceLogger().debug(TAG, "previewScale: " + previewScale);
                Matrix transform = new Matrix();
                transform.setScale(previewScale, previewScale, screenWith / 2, screenHeight / 2);
                mSurfaceView.setTransform(transform);
            }
        }
        bqcScanService.setScanRegion(scanRect);
    }

    private void notifyScanResult(boolean isProcessed, Intent resultData) {
        ScanHelper.getInstance().notifyScanResult(isProcessed, resultData);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        notifyScanResult(false, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == ImagePicker.RESULT_CODE_ITEMS) {

            ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                    .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            String filePath = imageItemList.get(0).path;
            scanFromLocalImg(filePath);
        }
    }
}
