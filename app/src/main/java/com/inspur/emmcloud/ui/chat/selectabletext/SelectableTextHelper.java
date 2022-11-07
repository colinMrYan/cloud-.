package com.inspur.emmcloud.ui.chat.selectabletext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.ui.chat.messagemenu.MessageMenuPopupWindow;
import com.inspur.emmcloud.widget.TextViewFixTouchConsume;

/**
 * @CreateTime 2022/7/4 14:29
 * @Author zyx
 * @Description : 关于自选文本相关
 */
public class SelectableTextHelper {

    private final static int DEFAULT_SELECTION_LENGTH = 1;
    private static final int DEFAULT_SHOW_DURATION = 100;

    private CursorHandle mStartHandle;
    private CursorHandle mEndHandle;
    private SelectionInfo mSelectionInfo = new SelectionInfo();
    private OnSelectListener mSelectListener;

    private Context mContext;
    private TextView mTextView;
    private Spannable mSpannable;

    private int mTouchX;
    private int mTouchY;

    private int mSelectedColor;
    private int mCursorHandleColor;
    private int mCursorHandleSize;
    private BackgroundColorSpan mSpan;
    private boolean isHideWhenScroll;
    private boolean isHide = true;

    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private MessageMenuPopupWindow mPopupWindowList;
    private int lineCount = -1;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private int lineStart;

    public SelectableTextHelper(SelectableTextHelper.Builder builder) {
        this.mTextView = builder.view;
        this.mSelectedColor = builder.mSelectedColor;
        this.mCursorHandleColor = builder.mCursorHandleColor;
        this.mContext = builder.mContext;
        mCursorHandleSize = DensityUtil.dip2px(mContext, builder.mCursorHandleSizeInDp);
        init();
    }

    private void init() {
        mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);
//        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                showSelectView();
//                return true;
//            }
//        });

        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();
                return false;
            }
        });

//        mTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resetInfoAndHideSelectView();
//            }
//        });

        mTextView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
//                destroy();
                resetInfoAndHideSelectView();
            }
        });

        mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (isHideWhenScroll) {
                    isHideWhenScroll = false;
                    postShowSelectView(DEFAULT_SHOW_DURATION);
                }
                return true;
            }
        };
        mTextView.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);

        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lineCount = mTextView.getLineCount();
                Layout layout = mTextView.getLayout();
                lineStart = layout.getLineStart(lineCount - 1);
                mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        };

        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!isHideWhenScroll && !isHide) {
                    isHideWhenScroll = true;
                    if (mPopupWindowList != null) {
                        mPopupWindowList.hide();
                    }
                    if (mStartHandle != null) {
                        mStartHandle.dismiss();
                    }
                    if (mEndHandle != null) {
                        mEndHandle.dismiss();
                    }
                }
            }
        };
        mTextView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener);
    }

    /**
     * 清空+隐藏
     */
    public void resetInfoAndHideSelectView() {
        resetSelectionInfo();
        hideSelectView();
    }

    private void postShowSelectView(int duration) {
        mTextView.removeCallbacks(mShowSelectViewRunnable);
        if (duration <= 0) {
            mShowSelectViewRunnable.run();
        } else {
            mTextView.postDelayed(mShowSelectViewRunnable, duration);
        }
    }

    private final Runnable mShowSelectViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHide) return;
            if (mPopupWindowList != null) {
                mPopupWindowList.show();
            }
            if (mStartHandle != null) {
                showCursorHandle(mStartHandle);
            }
            if (mEndHandle != null) {
                showCursorHandle(mEndHandle);
            }
        }
    };

    private void showCursorHandle(CursorHandle cursorHandle) {
        Layout layout = mTextView.getLayout();
        int offset = cursorHandle.isLeft ? mSelectionInfo.mStart : mSelectionInfo.mEnd;
        cursorHandle.show((int) layout.getPrimaryHorizontal(offset), layout.getLineBottom(layout.getLineForOffset(offset)));
    }

    public SelectionInfo getSelectionInfo() {
        return mSelectionInfo;
    }

    /**
     * 全选
     */
    public void showSelectAll() {
//        if (mSelectionInfo == null && mSelectionInfo.mSelectionContent.length() == mTextView.getText().length()){
//            return;
//        }
        hideSelectView();
        isHide = false;
        if (mStartHandle == null) mStartHandle = new CursorHandle(true);
        if (mEndHandle == null) mEndHandle = new CursorHandle(false);
        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }
        selectText(0, mTextView.getText().length());
        showCursorHandle(mStartHandle);
        showCursorHandle(mEndHandle);
        if (mPopupWindowList != null) {
            mPopupWindowList.show();
        }
    }

    /**
     * 单选
     */
    public void showSelectView() {
        hideSelectView();
        resetSelectionInfo();
        isHide = false;
        if (mStartHandle == null) mStartHandle = new CursorHandle(true);
        if (mEndHandle == null) mEndHandle = new CursorHandle(false);

        int startOffset = TextLayoutUtil.getPreciseOffset(mTextView, mTouchX - mTextView.getPaddingLeft() - mTextView.getLeft(),
                mTouchY - mTextView.getTop() - mTextView.getPaddingTop());
        int endOffset = startOffset + DEFAULT_SELECTION_LENGTH;
        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }
        if (mSpannable == null || startOffset >= mTextView.getText().length()) {
            return;
        }
        selectText(startOffset, endOffset);
        showCursorHandle(mStartHandle);
        showCursorHandle(mEndHandle);
        if (mPopupWindowList != null) {
            mPopupWindowList.show();
        }
    }

    private void selectText(int startPos, int endPos) {
        if (startPos != -1) {
            mSelectionInfo.mStart = startPos;
        }
        if (endPos != -1) {
            mSelectionInfo.mEnd = endPos;
        }
        if (mSelectionInfo.mStart > mSelectionInfo.mEnd) {
            int temp = mSelectionInfo.mStart;
            mSelectionInfo.mStart = mSelectionInfo.mEnd;
            mSelectionInfo.mEnd = temp;
        }

        if (mSpannable != null) {
            if (mSpan == null) {
                mSpan = new BackgroundColorSpan(mSelectedColor);
            }
            //保存选中的文本
            mSelectionInfo.mSelectionContent = mSpannable.subSequence(mSelectionInfo.mStart, mSelectionInfo.mEnd).toString();

            if (mPopupWindowList != null) {
                //全选状态下 隐藏全选 功能按钮
                if (mSelectionInfo.mSelectionContent.length() == mTextView.getText().length()) {
                    mPopupWindowList.setSelectedAll();
                } else { //非全选状态下 显示全选功能 功能
                    mPopupWindowList.setSelectedNotAll();
                }
            }

            //改变选中状态下的文本的背景色
            mSpannable.setSpan(mSpan, mSelectionInfo.mStart, mSelectionInfo.mEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            //改变选中状态下的表情背景色
            EmotionUtil.getInstance(mContext).getSelectedSmiledText(mSpannable, mSelectionInfo.mStart, mSelectionInfo.mEnd,
                    mSelectedColor, mTextView.getTextSize(), mTextView.getLineSpacingMultiplier(), lineStart);
            if (mSelectListener != null) {
                mSelectListener.onTextSelected(mSelectionInfo.mSelectionContent);
            }
        }
    }

    private void hideSelectView() {
        isHide = true;
        if (mStartHandle != null) {
            mStartHandle.dismiss();
        }
        if (mEndHandle != null) {
            mEndHandle.dismiss();
        }
        if (mPopupWindowList != null) {
            mPopupWindowList.hide();
        }
    }

    /**
     * 重置选中信息以及 span
     */
    private void resetSelectionInfo() {
        mSelectionInfo.mSelectionContent = null;
        if (mSpannable != null && mSpan != null) {
            mSpannable.removeSpan(mSpan);
            EmotionUtil.getInstance(mContext).removeSelectedSmilesSpan(mSpannable);
            mSpan = null;
        }
    }

    public void setPopupWindow(MessageMenuPopupWindow mPopupWindowList) {
        this.mPopupWindowList = mPopupWindowList;
    }

    private class CursorHandle extends View {

        private PopupWindow mPopupWindow;
        private Paint mPaint;

        private int mCircleRadius = mCursorHandleSize / 2;
        private int mWidth = mCircleRadius * 2;
        private int mHeight = mCircleRadius * 2;
        private int mPadding = 25;
        private boolean isLeft;

        public CursorHandle(boolean isLeft) {
            super(mContext);
            this.isLeft = isLeft;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(mCursorHandleColor);

            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setWidth(mWidth + mPadding * 2);
            mPopupWindow.setHeight(mHeight + mPadding / 2);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mCircleRadius + mPadding, mCircleRadius, mCircleRadius, mPaint);
            if (isLeft) {
                canvas.drawRect(mCircleRadius + mPadding, 0, mCircleRadius * 2 + mPadding, mCircleRadius, mPaint);
            } else {
                canvas.drawRect(mPadding, 0, mCircleRadius + mPadding, mCircleRadius, mPaint);
            }
        }

        private int mAdjustX;
        private int mAdjustY;

        private int mBeforeDragStart;
        private int mBeforeDragEnd;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBeforeDragStart = mSelectionInfo.mStart;
                    mBeforeDragEnd = mSelectionInfo.mEnd;
                    mAdjustX = (int) event.getX();
                    mAdjustY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //选项列表弹窗
                    if (mPopupWindowList != null) {
                        mPopupWindowList.show();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mPopupWindowList != null) {
                        mPopupWindowList.hide();
                    }
                    int[] position = new int[2];
                    mTextView.getLocationInWindow(position);
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    update(rawX + mAdjustX - mWidth - mTextView.getPaddingLeft() - mTextView.getLeft() - position[0],
                            rawY + mAdjustY - mHeight - mTextView.getTop() - mTextView.getPaddingTop());
                    break;
            }
            return true;
        }

        private void changeDirection() {
            isLeft = !isLeft;
            invalidate();
        }

        public void dismiss() {
            mPopupWindow.dismiss();
        }

        private int[] mTempCoors = new int[2];

        public void update(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            int oldOffset;
            if (isLeft) {
                oldOffset = mSelectionInfo.mStart;
            } else {
                oldOffset = mSelectionInfo.mEnd;
            }

            y -= mTempCoors[1];

            int offset = TextLayoutUtil.getHysteresisOffset(mTextView, x, y, oldOffset);

            if (offset != oldOffset) {
                resetSelectionInfo();
                if (isLeft) {
                    if (offset > mBeforeDragEnd) {
                        CursorHandle handle;
                        handle = getCursorHandle(false);
                        changeDirection();
                        handle.changeDirection();
                        mBeforeDragStart = mBeforeDragEnd;
                        selectText(mBeforeDragEnd, offset);
                        handle.updateCursorHandle();
                    } else {
                        selectText(offset, -1);
                    }
                    updateCursorHandle();
                } else {
                    if (offset < mBeforeDragStart) {
                        CursorHandle handle = getCursorHandle(true);
                        handle.changeDirection();
                        changeDirection();
                        mBeforeDragEnd = mBeforeDragStart;
                        selectText(offset, mBeforeDragStart);
                        handle.updateCursorHandle();
                    } else {
                        selectText(mBeforeDragStart, offset);
                    }
                    updateCursorHandle();
                }
            }
        }

        private void updateCursorHandle() {
            mTextView.getLocationInWindow(mTempCoors);
            Layout layout = mTextView.getLayout();
            if (isLeft) {
                mPopupWindow.update((int) layout.getPrimaryHorizontal(mSelectionInfo.mStart) - mWidth + getExtraX(),
                        layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mStart)) + getExtraY(), -1, -1);
            } else {
                mPopupWindow.update((int) layout.getPrimaryHorizontal(mSelectionInfo.mEnd) + getExtraX(),
                        layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd)) + getExtraY(), -1, -1);
            }
        }

        public void show(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            int offset = isLeft ? mWidth : 0;
            mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, x - offset + getExtraX(), y + getExtraY());
        }

        public int getExtraX() {
            return mTempCoors[0] - mPadding + mTextView.getPaddingLeft();
        }

        public int getExtraY() {
            return mTempCoors[1] + mTextView.getPaddingTop();
        }
    }

    private CursorHandle getCursorHandle(boolean isLeft) {
        if (mStartHandle.isLeft == isLeft) {
            return mStartHandle;
        } else {
            return mEndHandle;
        }
    }

    public void destroy() {
        mTextView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        mTextView.getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
        resetInfoAndHideSelectView();
        mStartHandle = null;
        mEndHandle = null;
        mPopupWindowList = null;
    }

    public interface OnSelectListener {
        void onTextSelected(CharSequence content);
    }

    public void setSelectListener(OnSelectListener selectListener) {
        mSelectListener = selectListener;
    }

    public static class Builder {

        private TextView view;
        private int mCursorHandleColor = 0xFF1379D6;
        private int mSelectedColor = 0xFFAFE1F4;
        private float mCursorHandleSizeInDp = 24;
        private Context mContext;

        public Builder(TextView view) {
            this.view = view;
            mContext = view.getContext();
        }

        public SelectableTextHelper.Builder setSelectedColor(int color) {
            this.mSelectedColor = color;
            return this;
        }

        public SelectableTextHelper.Builder setCursorHandleSizeInDp(int size) {
            this.mCursorHandleSizeInDp = size;
            return this;
        }

        public SelectableTextHelper.Builder setCursorHandleColor(int color) {
            this.mCursorHandleColor = color;
            return this;
        }

        public SelectableTextHelper build() {
            return new SelectableTextHelper(this);
        }
    }
}
