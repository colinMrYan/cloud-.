package com.inspur.emmcloud.basemodule.media.record;

import android.support.annotation.NonNull;

import com.inspur.emmcloud.basemodule.media.record.view.RecordModeView;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;

/**
 * 录制的配置
 */
public class VideoRecordConfig {
    @NonNull
    private static VideoRecordConfig sInstance = new VideoRecordConfig();

    protected VideoRecordConfig() {

    }

    @NonNull
    public static VideoRecordConfig getInstance() {
        return sInstance;
    }

    public int mQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
    public int mVideoBitrate = 9600;
    public int mResolution = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
    public int mGOP = 1;
    public int mFPS = 30;
    public boolean mIsMute = false; // 静音不用
    public boolean mIsNeedEdit = false; // 暂时不做编辑功能

    /**
     * 录制最短时间（以毫秒为单位）
     */
    public int mMinDuration = 0;
    /**
     * 录制最长时间（以毫秒为单位）
     */
    public int mMaxDuration = 60 * 1000;
    /**
     * 录制方向
     */
    public int mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;

    /**
     * 渲染方向
     */
    public int mRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;

    /**
     * 录制速度
     */
    public int mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;

    /**
     * 是否前置摄像头
     */
    public boolean mFrontCamera = false;

    /**
     * 开启手动聚焦；自动聚焦设置为false,手动对焦为true
     */
    public boolean mTouchFocus = true;

    /**
     * 当前屏比
     */
    public int mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;

    /**
     * 精简版不支持拍照？使用拍摄模式
     */
    public int mRecordMode = RecordModeView.RECORD_MODE_TAKE_PHOTO;

    /**
     * 清空配置
     */
    public void clear() {
        mQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
        mVideoBitrate = 9600;
        mResolution = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
        mGOP = 1;
        mFPS = 30;
        mMinDuration = 2 * 1000;
        mMaxDuration = 60 * 1000;
        mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
        mRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
        mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;
        mFrontCamera = true;
        mTouchFocus = false;
        mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
        mRecordMode = RecordModeView.RECORD_MODE_TAKE_PHOTO;
    }
}
