package com.inspur.emmcloud.basemodule.media.record;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.media.record.utils.BackgroundTasks;
import com.inspur.emmcloud.basemodule.media.record.utils.VideoPathUtil;
import com.inspur.emmcloud.basemodule.media.record.view.RecordModeView;
import com.tencent.liteav.audio.TXCAudioUGCRecorder;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCPartsManager;
import com.tencent.ugc.TXUGCRecord;

/**
 * Date：2022/6/14
 * Author：wang zhen
 * Description 录制调用SDK
 */
public class VideoRecordSDK implements TXRecordCommon.ITXVideoRecordListener {
    private static final String TAG = "VideoRecordSDK";

    public static int STATE_START = 1;
    public static int STATE_STOP = 2;
    public static int STATE_RESUME = 3;
    public static int STATE_PAUSE = 4;
    public static int START_RECORD_SUCC = 0;
    public static int START_RECORD_FAIL = -1;

    private int mCurrentState = STATE_STOP;
    @NonNull
    private static VideoRecordSDK sInstance = new VideoRecordSDK();
    @Nullable
    private TXUGCRecord mRecordSDK;
    private VideoRecordConfig mVideoRecordConfig; // 录制配置
    private boolean mPreviewFlag; // 预览
    private OnVideoRecordListener mOnVideoRecordListener;
    private String mRecordVideoPath;
    // 美颜类型
    public int mBeautyStyle = 0;
    // 美颜
    public int mBeautyLevel = 4;
    // 美白
    public int mWhiteLevel = 2;
    private String photoName;
    private String insertImage;
    private Uri photoUri;
    private String photoPath;

    private VideoRecordSDK() {

    }

    @NonNull
    public static VideoRecordSDK getInstance() {
        return sInstance;
    }

    /**
     * 初始化SDK：TXUGCRecord
     */
    public void initSDK() {
        if (mRecordSDK == null) {
            mRecordSDK = TXUGCRecord.getInstance(BaseApplication.getInstance());
        }
        mCurrentState = STATE_STOP;
    }

    @Nullable
    public TXUGCRecord getRecorder() {
        Log.d(TAG, "getRecorder mTXUGCRecord:" + mRecordSDK);
        return mRecordSDK;
    }

    public void initConfig(@NonNull VideoRecordConfig config) {
        mVideoRecordConfig = config;
    }

    public VideoRecordConfig getConfig() {
        return mVideoRecordConfig;
    }

    public void startCameraPreview(TXCloudVideoView videoView) {
        Log.d(TAG, "startCameraPreview");
        if (mPreviewFlag) {
            return;
        }
        mPreviewFlag = true;

        // 默认不允许自定义参数
        if (mVideoRecordConfig.mQuality >= 0) {
            // 推荐配置
            TXRecordCommon.TXUGCSimpleConfig simpleConfig = new TXRecordCommon.TXUGCSimpleConfig();
            simpleConfig.videoQuality = mVideoRecordConfig.mQuality;
            simpleConfig.minDuration = mVideoRecordConfig.mMinDuration;
            simpleConfig.maxDuration = mVideoRecordConfig.mMaxDuration;
            simpleConfig.isFront = mVideoRecordConfig.mFrontCamera;
            simpleConfig.touchFocus = mVideoRecordConfig.mTouchFocus;
            simpleConfig.needEdit = mVideoRecordConfig.mIsNeedEdit;

            if (mRecordSDK != null) {
                mRecordSDK.setVideoRenderMode(TXRecordCommon.VIDEO_RENDER_MODE_FULL_FILL_SCREEN);
                mRecordSDK.setMute(mVideoRecordConfig.mIsMute);
            }
            mRecordSDK.startCameraSimplePreview(simpleConfig, videoView);
        }

        if (mRecordSDK != null) {
            // 设置基础美颜
            mRecordSDK.getBeautyManager().setBeautyStyle(0);
            mRecordSDK.getBeautyManager().setBeautyLevel(4);
            mRecordSDK.getBeautyManager().setWhitenessLevel(1);

            mRecordSDK.setRecordSpeed(mVideoRecordConfig.mRecordSpeed);
            mRecordSDK.setHomeOrientation(mVideoRecordConfig.mHomeOrientation);
            mRecordSDK.setRenderRotation(mVideoRecordConfig.mRenderRotation);
            mRecordSDK.setAspectRatio(mVideoRecordConfig.mAspectRatio);
            mRecordSDK.setVideoRecordListener(this);
        }
    }

    public void stopCameraPreview() {
        Log.d(TAG, "stopCameraPreview");
        if (mRecordSDK != null) {
            mRecordSDK.stopCameraPreview();
        }
        mPreviewFlag = false;
    }

    public int getRecordState() {
        return mCurrentState;
    }

    /**
     * 拍照API {@link TXUGCRecord#snapshot(TXRecordCommon.ITXSnapshotListener)}
     */
    public void takePhoto(@Nullable final RecordModeView.OnSnapListener listener) {
        if (mRecordSDK != null) {
            mRecordSDK.snapshot(new TXRecordCommon.ITXSnapshotListener() {
                @Override
                public void onSnapshot(final Bitmap bitmap) {
                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSnap(bitmap, photoPath, photoUri);
                            }
                        }
                    });
                }
            });
        }
    }

    // 分段
    public TXUGCPartsManager getPartManager() {
        if (mRecordSDK != null) {
            return mRecordSDK.getPartsManager();
        }
        return null;
    }

    // 拍照文件路径
    public void initPhotoName() {
        photoName = System.currentTimeMillis() + ".jpg";

    }

    // 录制参数config
    public void setConfig(VideoRecordConfig config) {
        mVideoRecordConfig = config;
    }

    /**
     * 开始录制
     */
    public int startRecord() {
        if (mCurrentState == STATE_STOP) {
            String customVideoPath = VideoPathUtil.getCustomVideoOutputPath();
//            String customCoverPath = customVideoPath.replace(".mp4", ".jpg");

            int retCode = 0;

            if (mRecordSDK != null) {
                retCode = mRecordSDK.startRecord(customVideoPath, "");
            }
            Log.d(TAG, "startRecord retCode:" + retCode);
            if (retCode != TXRecordCommon.START_RECORD_OK) {
                return START_RECORD_FAIL;
            }
        }
        mCurrentState = STATE_START;
        return START_RECORD_SUCC;
    }

    /**
     * 暂停录制
     * FIXBUG:被打断时调用，暂停录制，修改状态，跳转到音乐界面也会被调用
     */
//    public void pauseRecord() {
//        Log.d(TAG, "pauseRecord");
//        if (mCurrentState == STATE_START || mCurrentState == STATE_RESUME) {
//            if (mRecordSDK != null) {
//                mRecordSDK.pauseRecord();
//            }
//            mCurrentState = STATE_PAUSE;
//        }
//        mPreviewFlag = false;
//
//        AudioFocusManager.getInstance().abandonAudioFocus();
//    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        Log.d(TAG, "stopRecord");
        int size = 0;
//        if (mRecordSDK != null) {
//            size = mRecordSDK.getPartsManager().getPartsPathList().size();
//        }
//        if (mCurrentState == STATE_STOP && size == 0) {
//            //如果录制未开始，且录制片段个数为0，则不需要停止录制
//            return;
//        }
        if (mRecordSDK != null) {
//            mRecordSDK.getPartsManager().deleteAllParts();
            int stopRecord = mRecordSDK.stopRecord();
        }
        AudioFocusManager.getInstance().abandonAudioFocus();

        mCurrentState = STATE_STOP;
    }

    /**
     * 释放Record SDK资源
     */
    public void releaseRecord() {
        Log.d(TAG, "releaseRecord");
        if (mRecordSDK != null) {
            mRecordSDK.stopBGM();
            mRecordSDK.stopCameraPreview();
            mRecordSDK.setVideoRecordListener(null);
            mRecordSDK.getPartsManager().deleteAllParts();
            mRecordSDK.release();
            mRecordSDK = null;
            mPreviewFlag = false;
        }
        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    public void setVideoRecordListener(OnVideoRecordListener listener) {
        mOnVideoRecordListener = listener;
    }

    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            if (mOnVideoRecordListener != null) {
                mOnVideoRecordListener.onRecordEvent(event);
            }
        } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
            ToastUtils.show(BaseApplication.getInstance().getResources().getString(R.string.video_record_event_evt_camera_cannot_use));
        } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
            ToastUtils.show(BaseApplication.getInstance().getResources().getString(R.string.video_record_event_evt_mic_cannot_use));
        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        if (mOnVideoRecordListener != null) {
            mOnVideoRecordListener.onRecordProgress(milliSecond);
        }
    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
        Log.d(TAG, "onRecordComplete");
        mCurrentState = STATE_STOP;
        if (result.retCode < 0) {
            LogUtils.debug("onRecordComplete", "----------" + result.retCode);
            ToastUtils.show(BaseApplication.getInstance().getResources().getString(R.string.video_record_complete_fail_tip) + result.descMsg);
        } else {
//            pauseRecord();
//            stopRecord();
            mRecordSDK.getPartsManager().deleteAllParts();
            mRecordVideoPath = result.videoPath;
            if (mOnVideoRecordListener != null) {
                mOnVideoRecordListener.onRecordComplete(result);
            }
        }
    }

    public String getRecordVideoPath() {
        return mRecordVideoPath;
    }

    public void switchCamera(boolean isFront) {
        TXUGCRecord record = getRecorder();
        if (record != null) {
            record.switchCamera(isFront);
        }
        if (mVideoRecordConfig != null) {
            mVideoRecordConfig.mFrontCamera = isFront;
        }
    }

    // 设置基础美颜
    public void setBeautyParams() {
        if (mRecordSDK != null) {
            mRecordSDK.getBeautyManager().setBeautyStyle(mBeautyStyle);
            mRecordSDK.getBeautyManager().setBeautyLevel(mBeautyLevel);
            mRecordSDK.getBeautyManager().setWhitenessLevel(mWhiteLevel);
        }
    }

    public interface OnVideoRecordListener {
        void onRecordProgress(long milliSecond);

        void onRecordEvent(int event);

        void onRecordComplete(TXRecordCommon.TXRecordResult result);
    }
}
