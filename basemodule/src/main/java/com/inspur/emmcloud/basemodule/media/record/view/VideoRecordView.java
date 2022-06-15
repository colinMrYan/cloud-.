package com.inspur.emmcloud.basemodule.media.record.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordSDK;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IRecordButton;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description 拍照，录像 view
 */
public class VideoRecordView extends RelativeLayout implements IRecordButton.OnRecordButtonListener, View.OnClickListener {
    private Activity mActivity;
    private RecordButton mButtonRecord;  // 录制按钮
    private RecordModeView mRecordModeView;  // 录制模式：拍照，拍摄
    private ImageView mSwitchIv;
    private ImageView mBackIv;
    private boolean mFrontCameraFlag = false; // 是否前置摄像头

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
        mRecordModeView = findViewById(R.id.record_mode_view);
        mBackIv = findViewById(R.id.iv_back);
        mSwitchIv = findViewById(R.id.iv_switch);
        mBackIv.setOnClickListener(this);
        mButtonRecord.setOnRecordButtonListener(this);
        // 根据不同的拍摄模式，更新拍摄按钮
        mRecordModeView.setOnRecordModeListener(new RecordModeView.OnRecordModeListener() {
            @Override
            public void onRecordModeSelect(int currentMode) {
                mButtonRecord.setCurrentRecordMode(currentMode);
            }
        });
    }

    @Override
    public void onRecordStart() {
        mRecordModeView.setVisibility(INVISIBLE);
    }

    @Override
    public void onRecordFinish() {

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
//            VideoRecordSDK.getInstance().switchCamera(mFrontCameraFlag);
        }
    }
}
