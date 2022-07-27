package com.inspur.emmcloud.basemodule.media.record.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.AudioFocusManager;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordConfig;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordSDK;
import com.inspur.emmcloud.basemodule.media.record.basic.VideoKitResult;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IRecordButton;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IVideoRecordKit;
import com.inspur.emmcloud.basemodule.media.record.utils.TelephonyUtil;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;

import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.REQUEST_CODE_IMAGE_EDIT;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description 拍照，录像 view
 */
public class VideoRecordView extends RelativeLayout implements IRecordButton.OnRecordButtonListener,
        View.OnClickListener, IVideoRecordKit, VideoRecordSDK.OnVideoRecordListener {
    private Activity mActivity;
    private TXCloudVideoView mVideoView;
    private RecordButton mButtonRecord;  // 录制按钮
    private RecordModeView mRecordModeView;  // 录制模式：拍照，拍摄
    private ImageView mSwitchIv;
    private ImageView mBackIv;
    private boolean mFrontCameraFlag = false; // 是否前置摄像头
    private OnRecordListener mOnRecordListener;
    private boolean isInStopProcessing; // 正在停止录制
    private RecordGestureView mRecordGestureView;

    public VideoRecordView(Context context) {
        this(context, null);
    }

    public VideoRecordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        // 初始化SDK:TXUGCRecord
        VideoRecordSDK.getInstance().initSDK();
        inflate(mActivity, R.layout.cloud_video_record_view, this);
        mButtonRecord = findViewById(R.id.record_button);
        mVideoView = findViewById(R.id.video_view);
        mRecordModeView = findViewById(R.id.record_mode_view);
        mRecordGestureView = findViewById(R.id.gesture_view);
        mBackIv = findViewById(R.id.iv_back);
        mSwitchIv = findViewById(R.id.iv_switch);
        mBackIv.setOnClickListener(this);
        mSwitchIv.setOnClickListener(this);
        VideoRecordSDK.getInstance().setVideoRecordListener(this);
        mButtonRecord.setOnRecordButtonListener(this);
        // 根据不同的拍摄模式，更新拍摄按钮
        mRecordModeView.setOnRecordModeListener(new RecordModeView.OnRecordModeListener() {
            @Override
            public void onRecordModeSelect(int currentMode) {
                mButtonRecord.setCurrentRecordMode(currentMode);
            }
        });
        mRecordGestureView.setOnSelectModeListener(new RecordGestureView.OnSelectModeListener() {
            @Override
            public void onModeSelect(boolean isRecord) {
                mRecordModeView.setSelectMode(isRecord);
            }
        });
        TelephonyUtil.getInstance().initPhoneListener();
        // 初始化默认配置
        VideoRecordConfig config = VideoRecordConfig.getInstance();
        VideoRecordSDK.getInstance().initConfig(config);
        // 初始化图片文件名
        VideoRecordSDK.getInstance().initPhotoName();
        VideoRecordSDK.getInstance().setBeautyParams();

    }

    @Override
    public void start() {
        // 打开录制预览界面
        VideoRecordSDK.getInstance().startCameraPreview(mVideoView);
    }

    @Override
    public void stop() {
        isInStopProcessing = false;
        TelephonyUtil.getInstance().uninitPhoneListener();
//        mButtonRecord.pauseRecordAnim();
        // 停止录制预览界面
        VideoRecordSDK.getInstance().stopCameraPreview();
        // 暂停录制
//        VideoRecordSDK.getInstance().pauseRecord();
    }

    @Override
    public void release() {
        // 停止录制
        VideoRecordSDK.getInstance().releaseRecord();
        VideoRecordConfig.getInstance().clear();
        AudioFocusManager.getInstance().setAudioFocusListener(null);
        VideoRecordSDK.getInstance().setVideoRecordListener(null);
    }

    @Override
    public void screenOrientationChange() {
        VideoRecordSDK.getInstance().stopCameraPreview();

//        VideoRecordSDK.getInstance().pauseRecord();

        VideoRecordSDK.getInstance().startCameraPreview(mVideoView);
    }

    @Override
    public void backPressed() {
    }

    @Override
    public void setConfig(VideoRecordConfig config) {
        VideoRecordSDK.getInstance().setConfig(config);
        // 设置默认的录制模式：拍照/录像
        mButtonRecord.setCurrentRecordMode(VideoRecordConfig.getInstance().mRecordMode);
    }

    @Override
    public void setOnRecordListener(OnRecordListener listener) {
        mOnRecordListener = listener;
    }

    @Override
    public void setOnMusicChooseListener(OnMusicChooseListener listener) {

    }

    @Override
    public void setEditVideoFlag(boolean enable) {

    }

    @Override
    public void disableRecordSpeed() {

    }

    @Override
    public void disableTakePhoto() {

    }

    @Override
    public void disableLongPressRecord() {

    }

    @Override
    public void disableAspect() {

    }

    @Override
    public void disableBeauty() {

    }


    @Override
    public void onRecordStart() {
        mRecordModeView.setVisibility(INVISIBLE);
        mBackIv.setVisibility(INVISIBLE);
        int retCode = VideoRecordSDK.getInstance().startRecord();
        if (retCode == VideoRecordSDK.START_RECORD_FAIL) { //点击开始录制失败，录制按钮状态变为暂停
//            mButtonRecord.pauseRecordAnim();
            return;
        }
        // 监听音频占用情况
        AudioFocusManager.getInstance().setAudioFocusListener(new AudioFocusManager.OnAudioFocusListener() {
            @Override
            public void onAudioFocusChange() {
                // TODO: 2022/7/5 被打断后应该结束录制
//                VideoRecordSDK.getInstance().pauseRecord();
            }
        });
        AudioFocusManager.getInstance().requestAudioFocus();
    }

    // 停止录制
    @Override
    public void onRecordFinish(boolean byClick) {
        mRecordModeView.setVisibility(VISIBLE);
        mBackIv.setVisibility(VISIBLE);
//        VideoRecordSDK.getInstance().pauseRecord();
        if (byClick) {
            VideoRecordSDK.getInstance().stopRecord();
        }
        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    // 拍照事件
    @Override
    public void onTakePhoto() {
        VideoRecordSDK.getInstance().takePhoto(new RecordModeView.OnSnapListener() {
            @Override
            public void onSnap(Bitmap bitmap, String path, Uri uri) {
                // 适配AndroidQ以下刷新相册
//                if (Build.VERSION.SDK_INT < 29) {
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, null);
//                }
                ((FragmentActivity) getContext()).startActivityForResult(
                        new Intent(getContext(), IMGEditActivity.class)
                                .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, path)
                                .putExtra(IMGEditActivity.EXTRA_ENCODING_TYPE, 0), REQUEST_CODE_IMAGE_EDIT
                );
                ((FragmentActivity) getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.ps_anim_down_out);
        } else if (id == R.id.iv_switch) {
            mFrontCameraFlag = !mFrontCameraFlag;
            VideoRecordSDK.getInstance().switchCamera(mFrontCameraFlag);
        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        mButtonRecord.setProgress(milliSecond);
    }

    @Override
    public void onRecordEvent(int event) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            //相当于点击了暂停按钮
            mButtonRecord.pauseRecordAnim(false);
        }
    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
        if (result.retCode >= 0) {
            // 录制后不需要进行编辑视频，直接输出录制视频路径
            if (mOnRecordListener != null) {
                VideoKitResult ugcKitResult = new VideoKitResult();
                String outputPath = VideoRecordSDK.getInstance().getRecordVideoPath();
                ugcKitResult.errorCode = result.retCode;
                ugcKitResult.descMsg = result.descMsg;
                ugcKitResult.outputPath = outputPath;
                ugcKitResult.coverPath = result.coverPath;
                mOnRecordListener.onRecordCompleted(ugcKitResult);
            }
        }
    }
}
