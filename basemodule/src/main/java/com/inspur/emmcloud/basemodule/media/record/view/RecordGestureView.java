package com.inspur.emmcloud.basemodule.media.record.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordSDK;
import com.tencent.ugc.TXUGCRecord;

/**
 * Date：2022/6/23
 * Author：wang zhen
 * Description
 */
public class RecordGestureView extends RelativeLayout implements View.OnTouchListener, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private float mLastScaleFactor;
    private float mScaleFactor;
    private TXUGCRecord record;
    private FrameLayout mMaskLayout;

    public RecordGestureView(@NonNull Context context) {
        super(context);
        initViews();
    }

    public RecordGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.record_gesture_view, this);
        mMaskLayout = (FrameLayout) findViewById(R.id.mask);
        mMaskLayout.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(getContext(), this);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        int maxZoom = VideoRecordSDK.getInstance().getRecorder().getMaxZoom();
        if (maxZoom == 0) {
            return false;
        }
        float factorOffset = detector.getScaleFactor() - mLastScaleFactor;
        mScaleFactor += factorOffset;
        mLastScaleFactor = detector.getScaleFactor();
        if (mScaleFactor < 0) {
            mScaleFactor = 0;
        }
        if (mScaleFactor > 1) {
            mScaleFactor = 1;
        }
        int zoomValue = Math.round(mScaleFactor * maxZoom);
        VideoRecordSDK.getInstance().getRecorder().setZoom(zoomValue);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mLastScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mMaskLayout) {
            int pointerCount = event.getPointerCount();
            if (pointerCount >= 2) {
                mScaleGestureDetector.onTouchEvent(event);
            } else if (pointerCount == 1) {
                mGestureDetector.onTouchEvent(event);
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setFocusPosition(e.getX(), e.getY());
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }
}
