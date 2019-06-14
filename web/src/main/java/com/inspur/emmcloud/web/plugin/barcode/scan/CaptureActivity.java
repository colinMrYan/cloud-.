package com.inspur.emmcloud.web.plugin.barcode.scan;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.barcode.camera.CameraManager;
import com.inspur.emmcloud.web.plugin.barcode.decoding.CaptureActivityHandler;
import com.inspur.emmcloud.web.plugin.barcode.decoding.GetDecodeResultFromServer;
import com.inspur.emmcloud.web.plugin.barcode.decoding.InactivityTimer;
import com.inspur.emmcloud.web.plugin.barcode.view.ViewfinderView;

import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;


public class CaptureActivity extends BaseActivity implements Callback {

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;
    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private SurfaceView surfaceView;
    private Button btn_torch;
    private boolean isTorchOn = false;
    private TextView lampText;
    private boolean isDecodeingFromServer = false;
    private boolean isDecodeFinish = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        CameraManager.init(this);
        btn_torch = (Button) findViewById(Res.getWidgetID("btn_torch"));
        viewfinderView = (ViewfinderView) findViewById(Res.getWidgetID("viewfinder_view"));
        surfaceView = (SurfaceView) findViewById(Res.getWidgetID("preview_view"));
        lampText = (TextView) findViewById(Res.getWidgetID("lamp_text"));
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        (findViewById(Res.getWidgetID("close_camera_btn"))).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        btn_torch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isTorchOn) {
                    isTorchOn = false;
                    lampText.setText(Res.getStringID("turn_on_light"));
                    btn_torch.setBackgroundResource(Res.getDrawableID("imp_lamp_off"));
                    CameraManager.get().setTorch(false);
                } else {
                    isTorchOn = true;
                    lampText.setText(Res.getStringID("turn_off_light"));
                    btn_torch.setBackgroundResource(Res.getDrawableID("imp_lamp_on"));
                    CameraManager.get().setTorch(true);
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.plugin_barcode_capture;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if (handler != null) {
            handler = null;
        }
        super.onDestroy();
    }

    public void handDecodeResult(String result) {
        if (isDecodeFinish) {
            return;
        }
        isDecodeFinish = true;
        Intent intent = new Intent();
        if (result == null || "".equals(result)) {
            result = getString(Res.getStringID("can_not_recognize"));
            intent.putExtra("isDecodeSuccess", false);
        } else {
            intent.putExtra("isDecodeSuccess", true);
        }
        intent.putExtra("msg", result);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    public void surfaceCreated(final SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    initCamera(holder);
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    ToastUtils.show(CaptureActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(CaptureActivity.this, permissions));
                    finish();
                }
            });
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    Res.getRawID("plugin_barcode_beep"));
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    public void uploadImgToDecodeByServer(Bitmap cropBitmap) {
        if (isDecodeingFromServer) return;
        isDecodeingFromServer = true;
        File dir = new File(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String imgSavePath = MyAppConfig.LOCAL_IMG_CREATE_PATH + "file.jpg";
        final File file = new File(imgSavePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            ImageUtils.saveImageToSD(CaptureActivity.this, imgSavePath, cropBitmap, 90);
            final String completeUrl = "http://emm.inspuronline.com:88/api/barcode/decode";
            RequestParams params = new RequestParams(completeUrl);
            params.setMultipart(true);// 使用multipart表单上传文件
            params.addBodyParameter("file", file, "image/jpg");
            x.http().post(params, new CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    if (!isDecodeFinish) {
                        GetDecodeResultFromServer getDecodeResultFromServer = new GetDecodeResultFromServer(s);
                        String data = getDecodeResultFromServer.getData();
                        if (data != null) {
                            data = data.replaceAll("[\\t\\n\\r]", "");
                            if ((CaptureActivity.this != null) && (isDecodeFinish == false) && !data.equals("")) {
                                handDecodeResult(data);
                            }
                        }
                        isDecodeingFromServer = false;
                    }
                }

                @Override
                public void onError(Throwable arg0, boolean b) {
                    isDecodeingFromServer = false;
                }

                @Override
                public void onCancelled(CancelledException e) {
                    isDecodeingFromServer = false;
                }

                @Override
                public void onFinished() {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            isDecodeingFromServer = false;
        }

    }

}