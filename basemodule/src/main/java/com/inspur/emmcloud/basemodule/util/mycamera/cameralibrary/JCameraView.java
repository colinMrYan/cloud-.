package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.CaptureListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.ClickListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.ErrorListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.JCameraListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.listener.TypeListener;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.state.CameraMachine;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.FileUtil;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.util.ScreenUtils;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.view.CameraView;

import java.io.IOException;


public class JCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder
        .Callback, CameraView {
//    private static final String TAG = "JCameraView";

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;
    public static final int TYPE_DEFAULT = 0x004;
    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;
    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以
    //闪关灯状态
    private static final int TYPE_FLASH_AUTO = 0x021;
    private static final int TYPE_FLASH_ON = 0x022;
    private static final int TYPE_FLASH_OFF = 0x023;
    //Camera状态机
    private CameraMachine machine;
    private int type_flash = TYPE_FLASH_OFF;
    //回调监听
    private JCameraListener jCameraLisenter;
    private ClickListener leftClickListener;
    private ClickListener rightClickListener;

    private Context mContext;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private CaptureLayout mCaptureLayout;
    private FocusView mFocusView;
    private MediaPlayer mMediaPlayer;
    private VideoView mVideoView;
    private CameraCropView cameraCropView;

    private int layout_width;
    private float screenProp = 0f;

    private Bitmap captureBitmap;   //捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL


    //切换摄像头按钮的参数
    private int iconSize = 0;       //图标大小
    private int iconMargin = 0;     //右上边距
    private int iconSrc = 0;        //图标资源
    private int iconLeft = 0;       //左图标
    private int iconRight = 0;      //右图标
    private int duration = 0;       //录制时间

    //缩放梯度
    private int zoomGradient = 0;

    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private ErrorListener errorListener;
    private String cropJson;

    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //get AttributeSet
        iconSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics());
        iconMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());
        iconSrc = R.drawable.plugin_cemera_btn_camera_switch;
        iconLeft = 0;
        iconRight = 0;
        duration = 10 * 1000;       //没设置默认为10s
        initData();
        initView();
    }

    private void initData() {
        layout_width = ScreenUtils.getScreenWidth(mContext);
        //缩放梯度
        zoomGradient = (int) (layout_width / 50f);
        machine = new CameraMachine(getContext(), this, this);
    }

    private void initView() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.plugin_camera_view, this);
        mVideoView = (VideoView) view.findViewById(R.id.video_preview);
        CameraInterface.getInstance().setJCameraView(this);
        mPhoto = (ImageView) view.findViewById(R.id.image_photo);
        mSwitchCamera = (ImageView) view.findViewById(R.id.image_switch);
        mSwitchCamera.setImageResource(iconSrc);
        machine.flash(Camera.Parameters.FLASH_MODE_AUTO);
        mCaptureLayout = (CaptureLayout) view.findViewById(R.id.capture_layout);
        mCaptureLayout.setDuration(duration);
        mCaptureLayout.setIconSrc(iconLeft, iconRight);
        mFocusView = (FocusView) view.findViewById(R.id.fouce_view);
        cameraCropView = findViewById(R.id.camera_crop_view);
        mVideoView.getHolder().addCallback(this);

        //切换摄像头
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                machine.switchCamera(mVideoView.getHolder(), screenProp);
            }
        });
        //拍照 录像
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                mSwitchCamera.setVisibility(INVISIBLE);
                mFocusView.setVisibility(INVISIBLE);
//                mFlashLamp.setVisibility(INVISIBLE);
                machine.capture();
            }

            @Override
            public void recordStart() {
                mSwitchCamera.setVisibility(INVISIBLE);
//                mFlashLamp.setVisibility(INVISIBLE);
                machine.record(mVideoView.getHolder().getSurface(), screenProp);
            }

            @Override
            public void recordShort(final long time) {
                mCaptureLayout.setTextWithAnimation("录制时间过短");
                mSwitchCamera.setVisibility(VISIBLE);
//                mFlashLamp.setVisibility(VISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        machine.stopRecord(true, time);
                    }
                }, 1500 - time);
            }

            @Override
            public void recordEnd(long time) {
                machine.stopRecord(false, time);
            }

            @Override
            public void recordZoom(float zoom) {
                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            @Override
            public void recordError() {
                if (errorListener != null) {
                    errorListener.AudioPermissionError();
                }
            }
        });
        //确认 取消
        mCaptureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {
                machine.cancel(mVideoView.getHolder(), screenProp);
            }

            @Override
            public void confirm() {
                machine.confirm();
            }
        });
        //退出
//        mCaptureLayout.setReturnLisenter(new ReturnListener() {
//            @Override
//            public void onReturn() {
//                if (jCameraLisenter != null) {
//                    jCameraLisenter.quit();
//                }
//            }
//        });
        mCaptureLayout.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (leftClickListener != null) {
                    leftClickListener.onClick();
                }
            }
        });
        mCaptureLayout.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (rightClickListener != null) {
                    rightClickListener.onClick();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mVideoView.getMeasuredWidth();
        float heightSize = mVideoView.getMeasuredHeight();
        if (screenProp == 0) {
            screenProp = heightSize / widthSize;
        }
    }

    public void setCropData(String cropJson) {
        this.cropJson = cropJson;
        cameraCropView.setCropData(cropJson);
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
    }

    //生命周期onResume
    public void onResume() {
        resetState(TYPE_DEFAULT); //重置状态
        CameraInterface.getInstance().registerSensorManager(mContext);
        machine.start(mVideoView.getHolder(), screenProp);
    }

    public boolean reFocus() {
        return setFocusViewWidthAnimation(mFocusView.getX() + mFocusView.getWidth() / 2, mFocusView.getY() + mFocusView.getHeight() / 2, false);
    }

    //生命周期onPause
    public void onPause() {
        stopVideo();
        resetState(TYPE_PICTURE);
        CameraInterface.getInstance().isPreview(false);
        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }

    public void onDestroy() {
        CameraInterface.getInstance().resetCamera();
    }

    //SurfaceView生命周期
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mVideoView.setCustomRatio(1,1);
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraInterface.getInstance().doDestroyCamera();
    }

    public boolean isHasCameraCropView() {
        return cameraCropView.getVisibility() == VISIBLE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cameraCropView.getVisibility() == VISIBLE) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY(), true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    mFocusView.setVisibility(View.GONE);
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if ((int) (result - firstTouchLength) / 30 != 0) {
//                        firstTouch = true;
                        machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                        firstTouchLength = result;
                    }
//                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }


    //对焦框指示器动画
    private boolean setFocusViewWidthAnimation(float x, float y, boolean isShowFocusView) {
        boolean isSupportSetFocusAera = machine.focus(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFocusView.setVisibility(INVISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CameraInterface.getInstance().unlockFocus();
                    }
                }, 1000);
            }
        }, isShowFocusView);
        if (!isSupportSetFocusAera) {
            mFocusView.setVisibility(INVISIBLE);
            CameraInterface.getInstance().unlockFocus();
        }
        return isSupportSetFocusAera;
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mVideoView.setLayoutParams(videoViewParam);
        }
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }

    public void setJCameraLisenter(JCameraListener jCameraLisenter) {
        this.jCameraLisenter = jCameraLisenter;
    }

    //启动Camera错误回调
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        CameraInterface.getInstance().setErrorLinsenter(errorListener);
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        this.mCaptureLayout.setButtonFeatures(state);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl);
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
//        mFlashLamp.setVisibility(VISIBLE);
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                if (jCameraLisenter != null) {
                    jCameraLisenter.recordSuccess(videoUrl, firstFrame);
                }
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                if (jCameraLisenter != null) {
                    if (cameraCropView.getVisibility() == VISIBLE) {
                        jCameraLisenter.captureSuccess(cameraCropView.getPicture(captureBitmap));
                    } else {
                        jCameraLisenter.captureSuccess(captureBitmap);
                    }
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        if (jCameraLisenter != null) {
            if (cameraCropView.getVisibility() == VISIBLE) {
                jCameraLisenter.captureSuccess(cameraCropView.getPicture(bitmap));
            } else {
                jCameraLisenter.captureSuccess(bitmap);
            }

        }
//        if (isVertical) {
//            mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
//        } else {
//            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        }
//        captureBitmap = bitmap;
//        mPhoto.setImageBitmap(bitmap);
//        mPhoto.setVisibility(VISIBLE);
//        mCaptureLayout.startAlphaAnimation();
//        mCaptureLayout.startTypeBtnAnimator();
    }

    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        videoUrl = url;
        JCameraView.this.firstFrame = firstFrame;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        mCaptureLayout.setTip(tip);
    }

    @Override
    public void startPreviewCallback() {
        handlerFoucs(mFocusView.getWidth() / 2, mFocusView.getHeight() / 2);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
//        if (y > mCaptureLayout.getTop()) {
//            return false;
//        }
        mFocusView.setVisibility(VISIBLE);
        if (x < mFocusView.getWidth() / 2) {
            x = mFocusView.getWidth() / 2;
        }
        if (x > layout_width - mFocusView.getWidth() / 2) {
            x = layout_width - mFocusView.getWidth() / 2;
        }
        if (y < mFocusView.getWidth() / 2) {
            y = mFocusView.getWidth() / 2;
        }
//        if (y > mCaptureLayout.getTop() - mFocusView.getWidth() / 2) {
//            y = mCaptureLayout.getTop() - mFocusView.getWidth() / 2;
//        }
        mFocusView.setX(x - mFocusView.getWidth() / 2);
        mFocusView.setY(y - mFocusView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFocusView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFocusView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFocusView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    public void setLeftClickListener(ClickListener clickListener) {
        this.leftClickListener = clickListener;
    }

    public void setRightClickListener(ClickListener clickListener) {
        this.rightClickListener = clickListener;
    }
}
