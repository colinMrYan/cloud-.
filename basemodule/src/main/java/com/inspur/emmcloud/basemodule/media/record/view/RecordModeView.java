package com.inspur.emmcloud.basemodule.media.record.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.R;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description 拍摄模式：拍照，录制
 */
public class RecordModeView extends RelativeLayout implements View.OnClickListener {
    /**
     * 拍摄方式选择，目前支持二种（单击拍照，单击录制）
     */
    public static final int RECORD_MODE_CLICK = 1;
    public static final int RECORD_MODE_TAKE_PHOTO = 2;

    private TextView mTextPhoto;
    private TextView mTextClick;
    private TextView mTextTouch;  // 隐藏右边布局，实现居中效果
    private LinearLayout mLayoutRecordMode;
    private OnRecordModeListener mOnRecordModeListener; // 模式监听

    public RecordModeView(Context context) {
        this(context, null);
    }

    public RecordModeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordModeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        Activity mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.cloud_record_mode_view, this);
        mLayoutRecordMode = findViewById(R.id.layout_record_mode);
        mTextPhoto = findViewById(R.id.tv_photo);
        mTextClick = findViewById(R.id.tv_click);
        mTextTouch = findViewById(R.id.tv_touch);
        mTextPhoto.setSelected(true);
        mTextPhoto.setOnClickListener(this);
        mTextClick.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.isSelected()) {
            return;
        }
        float xGap = 0;
        int id = view.getId();

        if (id == R.id.tv_click) {
            // 切换为拍摄模式
            if (mTextPhoto.isSelected()) {
                xGap = 1.0f / 3;
            } else if (mTextTouch.isSelected()) {
                xGap = 2.0f / 3;
            }
            mTextPhoto.setSelected(false);
            mTextClick.setSelected(true);
            mTextTouch.setSelected(false);

            if (mOnRecordModeListener != null) {
                mOnRecordModeListener.onRecordModeSelect(RecordModeView.RECORD_MODE_CLICK);
            }
        } else if (id == R.id.tv_photo) {
            if (mTextClick.isSelected()) {
                xGap = -1.0f / 3;
            } else if (mTextTouch.isSelected()) {
                xGap = 1.0f / 3;
            }
            mTextPhoto.setSelected(true);
            mTextClick.setSelected(false);
            mTextTouch.setSelected(false);

            if (mOnRecordModeListener != null) {
                mOnRecordModeListener.onRecordModeSelect(RecordModeView.RECORD_MODE_TAKE_PHOTO);
            }
        }
        float x1 = mLayoutRecordMode.getTranslationX();
        float x2 = x1 + mLayoutRecordMode.getWidth() * xGap;

        ObjectAnimator animator = ObjectAnimator.ofFloat(mLayoutRecordMode, "translationX", x1, x2);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTextPhoto.setClickable(false);
                mTextClick.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mTextPhoto.setClickable(true);
                mTextClick.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    /**
     * 设置切换"拍摄模式"监听器
     */
    public void setOnRecordModeListener(RecordModeView.OnRecordModeListener listener) {
        mOnRecordModeListener = listener;
    }

    public interface OnRecordModeListener {
        /**
         * 选择一种拍摄模式
         *
         * @param currentMode 当前拍摄模式
         */
        void onRecordModeSelect(int currentMode);
    }

    // 拍照，测试精简版到底支持不支持
    public interface OnSnapListener {
        void onSnap(Bitmap bitmap);
    }
}
