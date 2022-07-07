package com.inspur.emmcloud.basemodule.media.record.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IRecordButton;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description 拍摄按钮
 */
public class RecordButton extends RelativeLayout implements View.OnTouchListener, IRecordButton {
    private View view;

    private View mViewPhotoModeOutter;
    private View mViewPhotoModeInner;
    private RecordProgressView mViewTapModeOutter;
    private View mViewTapModeInner;
    private ImageView mImageRecordPause;
    private boolean mIsRecording; // 是否在录制
    private int mRecordMode; // 拍照，拍摄
    private IRecordButton.OnRecordButtonListener mOnRecordButtonListener;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        Activity mActivity = (Activity) getContext();
        view = inflate(mActivity, R.layout.cloud_record_button, this);
        setOnTouchListener(this);

        mViewPhotoModeOutter = findViewById(R.id.view_take_photo_bkg);
        mViewPhotoModeInner = findViewById(R.id.view_take_photo);
        mViewTapModeOutter = findViewById(R.id.view_record_click_shot_bkg);
        mViewTapModeInner = findViewById(R.id.view_record_click_shot);
        mImageRecordPause = findViewById(R.id.iv_record_pause);
        // 默认拍照
        mViewPhotoModeOutter.setVisibility(VISIBLE);
        mViewPhotoModeInner.setVisibility(VISIBLE);
        mViewTapModeOutter.setVisibility(GONE);
        mViewTapModeInner.setVisibility(GONE);
        // 设置圆形背景
        int photoModeBgColor = getResources().getColor(R.color.cloud_record_button_take_photo_background_color);
        mViewPhotoModeOutter.setBackground(createCircleGradientDrawable(photoModeBgColor));
        int photoModeInnerColor = getResources().getColor(R.color.white);
        mViewPhotoModeInner.setBackground(createCircleGradientDrawable(photoModeInnerColor));
        mViewTapModeOutter.setBackground(createCircleGradientDrawable(getResources().getColor(R.color.cloud_video_outer_bg)));
        mViewTapModeInner.setBackground(createCircleGradientDrawable(getResources().getColor(R.color.record_color_ff3030)));
    }

    private GradientDrawable createCircleGradientDrawable(int color) {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setShape(GradientDrawable.OVAL);
        normalDrawable.setColor(color);
        normalDrawable.setUseLevel(false);
        return normalDrawable;
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (mRecordMode == RecordModeView.RECORD_MODE_CLICK) {
                    toggleRecordAnim();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mRecordMode == RecordModeView.RECORD_MODE_TAKE_PHOTO) {
                    startTakePhotoAnim();
                }
                break;
            }
        }
        return true;
    }

    /**
     * 切换录制"开始"和"暂停"状态 执行的动画
     */
    private void toggleRecordAnim() {
        if (mIsRecording) {
            pauseRecordAnim(true);
        } else {
            startRecordAnim();
        }
    }

    /**
     * 开始录制操作执行的动画
     */
    public void startRecordAnim() {
        startRecordAnimByClick();
    }

    /**
     * 录制操作执行的动画
     */
    public void pauseRecordAnim(boolean byClick) {
        if (!mIsRecording) {
            return;
        }
        pauseRecordAnimByClick(byClick);
    }

    /**
     * 开始"拍照"操作执行的动画
     */
    private void startTakePhotoAnim() {
        ObjectAnimator btnBkgZoomOutXAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleX", 1.05f);
        ObjectAnimator btnBkgZoomOutYAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleY", 1.05f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleX", 0.95f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleY", 0.95f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomOutXAn).with(btnBkgZoomOutYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                endTakePhotoAnim();
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onTakePhoto();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        view.setEnabled(false);
    }

    /**
     * 结束"拍照"操作时执行的动画
     */
    public void endTakePhotoAnim() {
        ObjectAnimator btnBkgZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleX", 1f);
        ObjectAnimator btnBkgZoomIntYAn = ObjectAnimator.ofFloat(mViewPhotoModeOutter, "scaleY", 1f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleX", 1f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(80);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomInXAn).with(btnBkgZoomIntYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * 拍摄模式为"单击"录制下。开始录制操作执行的动画
     */
    private void startRecordAnimByClick() {
        ObjectAnimator btnBkgZoomOutXAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleX",
                1.08f);
        ObjectAnimator btnBkgZoomOutYAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleY",
                1.08f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleX", 0.3f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleY", 0.3f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomOutXAn).with(btnBkgZoomOutYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordStart();
                    mIsRecording = true;
                    mViewTapModeOutter.createProcess();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        mImageRecordPause.setVisibility(View.VISIBLE);
    }

    /**
     * 拍摄模式为"单击"录制下。暂停录制操作执行的动画
     */
    private void pauseRecordAnimByClick(final boolean byClick) {
        ObjectAnimator btnBkgZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleX", 1f);
        ObjectAnimator btnBkgZoomIntYAn = ObjectAnimator.ofFloat(mViewTapModeOutter, "scaleY", 1f);

        ObjectAnimator btnZoomInXAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleX", 1f);
        ObjectAnimator btnZoomInYAn = ObjectAnimator.ofFloat(mViewTapModeInner, "scaleY", 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(btnBkgZoomInXAn).with(btnBkgZoomIntYAn).with(btnZoomInXAn).with(btnZoomInYAn);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mOnRecordButtonListener != null) {
                    mOnRecordButtonListener.onRecordFinish(byClick);
                    mViewTapModeOutter.stopDraw();
                    mIsRecording = false;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private void startPhotoModeAnim() {
        ObjectAnimator btnInAlphaAn = ObjectAnimator.ofFloat(mViewPhotoModeInner, "alpha", 0, 1);
        btnInAlphaAn.setDuration(200);
        btnInAlphaAn.start();
    }

    private void startRecordModeAnim() {
        ObjectAnimator btnInAlphaAn = ObjectAnimator.ofFloat(mViewTapModeInner, "alpha", 0, 1);
        btnInAlphaAn.setDuration(200);
        btnInAlphaAn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 切换为录制模式时隐藏暂停按钮
                mImageRecordPause.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        btnInAlphaAn.start();
    }

    /**
     * 更新视频拍摄模式
     *
     * @param recordMode 拍照/录制
     */
    @Override
    public void setCurrentRecordMode(int recordMode) {
        mRecordMode = recordMode;

        mViewPhotoModeOutter.setVisibility(GONE);
        mViewPhotoModeInner.setVisibility(GONE);

        mViewTapModeOutter.setVisibility(GONE);
        mViewTapModeInner.setVisibility(GONE);

        switch (mRecordMode) {
            case RecordModeView.RECORD_MODE_TAKE_PHOTO:
                mViewPhotoModeOutter.setVisibility(VISIBLE);
                mViewPhotoModeInner.setVisibility(VISIBLE);
                // 切换为拍照模式时隐藏暂停按钮
                mImageRecordPause.setVisibility(GONE);
                startPhotoModeAnim();
                break;
            case RecordModeView.RECORD_MODE_CLICK:
                mViewTapModeOutter.setVisibility(VISIBLE);
                mViewTapModeInner.setVisibility(VISIBLE);
                startRecordModeAnim();
                break;
        }
    }

    @Override
    public void setOnRecordButtonListener(OnRecordButtonListener listener) {
        mOnRecordButtonListener = listener;
    }

    public void setProgress(long milliSecond) {
        mViewTapModeOutter.setProgress(milliSecond);
    }
}
