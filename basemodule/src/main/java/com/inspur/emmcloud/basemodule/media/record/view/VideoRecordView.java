package com.inspur.emmcloud.basemodule.media.record.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.AudioFocusManager;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordConfig;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordSDK;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IRecordButton;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IVideoRecordKit;
import com.inspur.emmcloud.basemodule.media.record.utils.TelephonyUtil;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description 拍照，录像 view
 */
public class VideoRecordView extends RelativeLayout implements IRecordButton.OnRecordButtonListener,
        View.OnClickListener, IVideoRecordKit {
    private Activity mActivity;
    private TXCloudVideoView mVideoView;
    private RecordButton mButtonRecord;  // 录制按钮
    private RecordModeView mRecordModeView;  // 录制模式：拍照，拍摄
    private ImageView mSwitchIv;
    private ImageView mBackIv;
    private boolean mFrontCameraFlag = false; // 是否前置摄像头
    private OnRecordListener mOnRecordListener;
    private boolean isInStopProcessing; // 正在停止录制

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
        inflate(mActivity, R.layout.cloud_video_record_view, this);
        mButtonRecord = findViewById(R.id.record_button);
        mVideoView = findViewById(R.id.video_view);
        mRecordModeView = findViewById(R.id.record_mode_view);
        mBackIv = findViewById(R.id.iv_back);
        mSwitchIv = findViewById(R.id.iv_switch);
        mBackIv.setOnClickListener(this);
        mSwitchIv.setOnClickListener(this);
        mButtonRecord.setOnRecordButtonListener(this);
        // 根据不同的拍摄模式，更新拍摄按钮
        mRecordModeView.setOnRecordModeListener(new RecordModeView.OnRecordModeListener() {
            @Override
            public void onRecordModeSelect(int currentMode) {
                mButtonRecord.setCurrentRecordMode(currentMode);
            }
        });
        TelephonyUtil.getInstance().initPhoneListener();
        // 初始化默认配置
        VideoRecordConfig config = VideoRecordConfig.getInstance();
        VideoRecordSDK.getInstance().initConfig(config);
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
        mButtonRecord.pauseRecordAnim();
        // 停止录制预览界面
        VideoRecordSDK.getInstance().stopCameraPreview();
        // 暂停录制
        VideoRecordSDK.getInstance().pauseRecord();
    }

    @Override
    public void release() {
        // 停止录制
        VideoRecordSDK.getInstance().releaseRecord();
        VideoRecordConfig.getInstance().clear();
        // 录制TXUGCRecord是单例，需要释放时还原配置
        AudioFocusManager.getInstance().setAudioFocusListener(null);
        VideoRecordSDK.getInstance().setVideoRecordListener(null);
    }

    @Override
    public void screenOrientationChange() {

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
            mButtonRecord.pauseRecordAnim();
            return;
        }
        // 监听音频占用情况
        AudioFocusManager.getInstance().setAudioFocusListener(new AudioFocusManager.OnAudioFocusListener() {
            @Override
            public void onAudioFocusChange() {
                VideoRecordSDK.getInstance().pauseRecord();
            }
        });
        AudioFocusManager.getInstance().requestAudioFocus();
    }

    @Override
    public void onRecordFinish() {
        mRecordModeView.setVisibility(VISIBLE);
        mBackIv.setVisibility(VISIBLE);
        VideoRecordSDK.getInstance().pauseRecord();
        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    @Override
    public void onTakePhoto() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            mActivity.finish();
        } else if (id == R.id.iv_switch) {
            mFrontCameraFlag = !mFrontCameraFlag;
            VideoRecordSDK.getInstance().switchCamera(mFrontCameraFlag);
        }
    }
}
