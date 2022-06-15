package com.inspur.emmcloud.basemodule.media.record;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.media.record.utils.BackgroundTasks;
import com.inspur.emmcloud.basemodule.media.record.view.RecordModeView;
import com.tencent.liteav.audio.TXCAudioUGCRecorder;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
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
                    String fileName = System.currentTimeMillis() + ".jpg";
                    MediaStore.Images.Media.insertImage(BaseApplication.getInstance().getContentResolver(), bitmap, fileName, null);

                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSnap(bitmap);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onRecordEvent(int i, Bundle bundle) {

    }

    @Override
    public void onRecordProgress(long l) {

    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult txRecordResult) {

    }
}
