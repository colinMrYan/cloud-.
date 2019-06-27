/*
Copyright 2015 shizhefei（LuckyJayce）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.inspur.emmcloud.widget.largeimage;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.inspur.emmcloud.widget.largeimage.factory.BitmapDecoderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuckyJayce on 2016/11/24.
 */

public class LargeImageView extends View implements BlockImageLoader.OnImageLoadListener, ILargeImageView {
    private static final int STATE_NORMAL = 0;
    private static final int STATE_TRANSFORM_IN = 1;
    private static final int STATE_TRANSFORM_OUT = 2;
    private final GestureDetector gestureDetector;
    private final ScrollerCompat mScroller;
    private final BlockImageLoader imageBlockImageLoader;
    private final int mMinimumVelocity;
    private final int mMaximumVelocity;
    private final ScaleGestureDetector scaleGestureDetector;
    private final Paint paint;
    private final int mBgColor = 0xFF000000;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private float mScale = 1;
    private BitmapDecoderFactory mFactory;
    private float fitScale;
    private float maxScale;
    private float minScale;
    private BlockImageLoader.OnImageLoadListener mOnImageLoadListener;
    private Drawable mDrawable;
    private int mLevel;
    private ScaleHelper scaleHelper;
    private AccelerateInterpolator accelerateInterpolator;
    private DecelerateInterpolator decelerateInterpolator;
    private boolean isAttachedWindow;
    private int mOriginalWidth;
    private int mOriginalHeight;
    private int mOriginalLocationX;
    private int mOriginalLocationY;
    private int mState = STATE_NORMAL;
    private Matrix mSmoothMatrix;
    private Bitmap mBitmap;
    private boolean mTransformStart = false;
    private Transfrom mTransfrom;
    private int mBgAlpha = 0;
    private Paint mPaint;
    private TransformListener mTransformListener;
    private List<BlockImageLoader.DrawData> drawDatas = new ArrayList<>();
    private Rect imageRect = new Rect();
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;
    private CriticalScaleValueHook criticalScaleValueHook;
    private ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!isEnabled()) {
                return false;
            }
            if (!hasLoad()) {
                return false;
            }
            float newScale;
            newScale = mScale * detector.getScaleFactor();
            if (newScale > maxScale) {
                newScale = maxScale;
            } else if (newScale < minScale) {
                newScale = minScale;
            }
            setScale(newScale, (int) detector.getFocusX(), (int) detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };
    private OnDoubleClickListener onDoubleClickListener;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isEnabled()) {
                return false;
            }
            if (onClickListener != null && isClickable()) {
                onClickListener.onClick(LargeImageView.this);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isEnabled()) {
                return false;
            }
            overScrollByCompat((int) distanceX, (int) distanceY, getScrollX(), getScrollY(), getScrollRangeX(), getScrollRangeY(), 0, 0, false);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (onLongClickListener != null && isLongClickable()) {
                onLongClickListener.onLongClick(LargeImageView.this);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!isEnabled()) {
                return false;
            }
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isEnabled()) {
                return false;
            }
            if (onDoubleClickListener != null && onDoubleClickListener.onDoubleClick(LargeImageView.this, e)) {
                return true;
            }
            if (!hasLoad()) {
                return false;
            }
            float doubleScale;
            if (fitScale < 2) {
                doubleScale = 2;
            } else {
                if (fitScale > maxScale) {
                    doubleScale = maxScale;
                } else {
                    doubleScale = fitScale;
                }
            }
            float newScale;
            if (mScale < 1) {
                newScale = 1;
            } else if (mScale < doubleScale) {
                newScale = doubleScale;
            } else {
                newScale = 1;
            }
            smoothScale(newScale, (int) e.getX(), (int) e.getY());
            return true;
        }
    };

    public LargeImageView(Context context) {
        this(context, null);
    }

    public LargeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        mScroller = ScrollerCompat.create(getContext(), null);
        scaleHelper = new ScaleHelper();
        setFocusable(true);
        setWillNotDraw(false);
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);

        imageBlockImageLoader = new BlockImageLoader(context);
        imageBlockImageLoader.setOnImageLoadListener(this);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scaleHelper.computeScrollOffset()) {
            setScale(scaleHelper.getCurScale(), scaleHelper.getStartX(), scaleHelper.getStartY());
        }
        if (mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                final int rangeY = getScrollRangeY();
                final int rangeX = getScrollRangeX();
                overScrollByCompat(x - oldX, y - oldY, oldX, oldY, rangeX, rangeY,
                        0, 0, false);
            }
            if (!mScroller.isFinished()) {
                notifyInvalidate();
            }
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction > 0) {
            return getScrollX() < getScrollRangeX();
        } else {
            return getScrollX() > 0 && getScrollRangeX() > 0;
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0) {
            return getScrollY() < getScrollRangeY();
        } else {
            return getScrollY() > 0 && getScrollRangeY() > 0;
        }
    }

    public void setOnLoadStateChangeListener(BlockImageLoader.OnLoadStateChangeListener onLoadStateChangeListener) {
        if (imageBlockImageLoader != null) {
            imageBlockImageLoader.setOnLoadStateChangeListener(onLoadStateChangeListener);
        }
    }

    @Override
    public BlockImageLoader.OnImageLoadListener getOnImageLoadListener() {
        return mOnImageLoadListener;
    }

    @Override
    public void setOnImageLoadListener(BlockImageLoader.OnImageLoadListener onImageLoadListener) {
        this.mOnImageLoadListener = onImageLoadListener;
    }

    /**
     * Sets a Bitmap as the content of this ImageView.
     *
     * @param bm The bitmap to initFitImageScale
     */
    @Override
    public void setImage(Bitmap bm) {
        setImageDrawable(new BitmapDrawable(getResources(), bm));
    }

    @Override
    public void setImage(Drawable drawable) {
        setImageDrawable(drawable);
    }

    @Override
    public void setImage(@DrawableRes int resId) {
        setImageDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mFactory = null;
        mScale = 1.0f;
        scrollTo(0, 0);
        if (mDrawable != drawable) {
            final int oldWidth = mDrawableWidth;
            final int oldHeight = mDrawableHeight;
            updateDrawable(drawable);
            onLoadImageSize(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
                requestLayout();
            }
            notifyInvalidate();
        }
    }

    @Override
    public void setImage(BitmapDecoderFactory factory) {
        setImage(factory, null);
    }

    @Override
    public void setImage(BitmapDecoderFactory factory, Drawable defaultDrawable) {
        mScale = 1.0f;
        this.mFactory = factory;
        scrollTo(0, 0);
        updateDrawable(defaultDrawable);
        imageBlockImageLoader.setBitmapDecoderFactory(factory);
        invalidate();
    }

    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            unscheduleDrawable(mDrawable);
            if (isAttachedWindow) {
                mDrawable.setVisible(false, false);
            }
        }
        mDrawable = d;

        if (d != null) {
            d.setCallback(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                d.setLayoutDirection(getLayoutDirection());
            }
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            if (isAttachedWindow) {
                d.setVisible(getWindowVisibility() == VISIBLE && isShown(), true);
            }
            d.setLevel(mLevel);
            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();
//            applyImageTint();
//            applyColorMod();
//
//            configureBounds();
        } else {
            mDrawableWidth = mDrawableHeight = -1;
        }
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public boolean hasLoad() {
        if (mDrawable != null) {
            return true;
        } else if (mFactory != null) {
            return imageBlockImageLoader.hasLoad();
        }
        return false;
    }

    @Override
    public int computeVerticalScrollRange() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        int scrollRange = getContentHeight();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }
        return scrollRange;
    }

    public float getMinScale() {
        return minScale;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public float getFitScale() {
        return fitScale;
    }

    @Override
    public int getImageWidth() {
        if (mDrawable != null) {
            return mDrawableWidth;
        } else if (mFactory != null) {
            if (hasLoad()) {
                return mDrawableWidth;
            }
        }
        return 0;
    }

    @Override
    public int getImageHeight() {
        if (mDrawable != null) {
            return mDrawableHeight;
        } else if (mFactory != null) {
            if (hasLoad()) {
                return mDrawableHeight;
            }
        }
        return 0;
    }

    private int getScrollRangeY() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        return getContentHeight() - contentHeight;
    }

    /**
     *
     */
    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    /**
     *
     */
    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    /**
     *
     */
    @Override
    public int computeHorizontalScrollRange() {
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        int scrollRange = getContentWidth();
        final int scrollX = getScrollX();
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }
        return scrollRange;
    }

    private int getScrollRangeX() {
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        return (getContentWidth() - contentWidth);
    }

    private int getContentWidth() {
        if (hasLoad()) {
            return (int) (getMeasuredWidth() * mScale);
        }
        return 0;
    }

    private int getContentHeight() {
        if (hasLoad()) {
            if (getImageWidth() == 0) {
                return 0;
            }
            return (int) (1.0f * getMeasuredWidth() * getImageHeight() / getImageWidth() * mScale);
        }
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        if (mState == STATE_TRANSFORM_IN || mState == STATE_TRANSFORM_OUT) {
            if (mTransformStart) {
                initTransform();
            }
            if (mTransfrom == null) {
                super.onDraw(canvas);
                drawCanvas(canvas);
                return;
            }

            if (mTransformStart) {
                if (mState == STATE_TRANSFORM_IN) {
                    mTransfrom.initStartIn();
                } else {
                    mTransfrom.initStartOut();
                }
            }

            if (mTransformStart) {
                Log.d("SmoothImageView", "mTransfrom.startScale:" + mTransfrom.startScale);
                Log.d("SmoothImageView", "mTransfrom.startScale:" + mTransfrom.endScale);
                Log.d("SmoothImageView", "mTransfrom.scale:" + mTransfrom.scale);
                Log.d("SmoothImageView", "mTransfrom.startRect:" + mTransfrom.startRect.toString());
                Log.d("SmoothImageView", "mTransfrom.endRect:" + mTransfrom.endRect.toString());
                Log.d("SmoothImageView", "mTransfrom.rect:" + mTransfrom.rect.toString());
            }

            mPaint.setAlpha(mBgAlpha);
            canvas.drawPaint(mPaint);

            int saveCount = canvas.getSaveCount();
            canvas.save();

            getBmpMatrix();
            canvas.translate(mTransfrom.rect.left, mTransfrom.rect.top);
            canvas.clipRect(0, 0, mTransfrom.rect.width, mTransfrom.rect.height);
            canvas.concat(mSmoothMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);
            if (mTransformStart) {
                mTransformStart = false;
                startTransform(mState);
            }
        } else {
            mPaint.setAlpha(255);
            canvas.drawPaint(mPaint);
            super.onDraw(canvas);
            drawCanvas(canvas);
        }

    }

    private void drawCanvas(Canvas canvas) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0) {
            return;
        }
        int drawOffsetX = 0;
        int drawOffsetY = 0;
        int contentWidth = getContentWidth();
        int contentHeight = getContentHeight();
        if (viewWidth > contentWidth) {
            drawOffsetX = (viewWidth - contentWidth) / 2;
        }
        if (viewHeight > contentHeight) {
            drawOffsetY = (viewHeight - contentHeight) / 2;
        }
        if (mFactory == null) {
            if (mDrawable != null) {
                mDrawable.setBounds(drawOffsetX, drawOffsetY, drawOffsetX + contentWidth, drawOffsetY + contentHeight);
                mDrawable.draw(canvas);
            }
        } else {
            int mOffsetX = 0;
            int mOffsetY = 0;
            int left = getScrollX();
            int right = left + viewWidth;
            int top = getScrollY();
            int bottom = top + viewHeight;
            float width = mScale * getWidth();
            float imgWidth = imageBlockImageLoader.getWidth();

            float imageScale = imgWidth / width;

            // 需要显示的图片的实际宽度。
            imageRect.left = (int) Math.ceil((left - mOffsetX) * imageScale);
            imageRect.top = (int) Math.ceil((top - mOffsetY) * imageScale);
            imageRect.right = (int) Math.ceil((right - mOffsetX) * imageScale);
            imageRect.bottom = (int) Math.ceil((bottom - mOffsetY) * imageScale);

            int saveCount = canvas.save();

            //如果是大图就需要继续加载图片块，如果不是大图直接用默认的
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (mDrawable == null || !imageBlockImageLoader.hasLoad() || (imageBlockImageLoader.getWidth() * imageBlockImageLoader.getHeight() > (displayMetrics.widthPixels * displayMetrics.heightPixels))) {
                imageBlockImageLoader.loadImageBlocks(drawDatas, imageScale, imageRect, viewWidth, viewHeight);
            }
            if (BlockImageLoader.DEBUG) {
                for (int i = 0; i < drawDatas.size(); i++) {
                    BlockImageLoader.DrawData data = drawDatas.get(i);
                    Rect drawRect = data.imageRect;
                    drawRect.left = (int) (Math.ceil(drawRect.left / imageScale) + mOffsetX) + drawOffsetX;
                    drawRect.top = (int) (Math.ceil(drawRect.top / imageScale) + mOffsetY) + drawOffsetY;
                    drawRect.right = (int) (Math.ceil(drawRect.right / imageScale) + mOffsetX) + drawOffsetX;
                    drawRect.bottom = (int) (Math.ceil(drawRect.bottom / imageScale) + mOffsetY) + drawOffsetY;
                    if (i == 0) {
                        canvas.drawRect(data.imageRect, paint);
                    } else {
                        drawRect.left += 3;
                        drawRect.top += 3;
                        drawRect.bottom -= 3;
                        drawRect.right -= 3;
                        canvas.drawBitmap(data.bitmap, data.srcRect, drawRect, null);
                    }
                }
            } else {
                if (drawDatas.isEmpty()) {
                    if (mDrawable != null) {
                        mDrawable.setBounds(drawOffsetX, drawOffsetY, drawOffsetX + contentWidth, drawOffsetY + contentHeight);
                        mDrawable.draw(canvas);
                    }
                } else {
                    for (BlockImageLoader.DrawData data : drawDatas) {
                        Rect drawRect = data.imageRect;
                        drawRect.left = (int) (Math.ceil(drawRect.left / imageScale) + mOffsetX) + drawOffsetX;
                        drawRect.top = (int) (Math.ceil(drawRect.top / imageScale) + mOffsetY) + drawOffsetY;
                        drawRect.right = (int) (Math.ceil(drawRect.right / imageScale) + mOffsetX) + drawOffsetX;
                        drawRect.bottom = (int) (Math.ceil(drawRect.bottom / imageScale) + mOffsetY) + drawOffsetY;
                        canvas.drawBitmap(data.bitmap, data.srcRect, drawRect, null);
                    }
                }
            }
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public void onBlockImageLoadFinished() {
        notifyInvalidate();
        if (mOnImageLoadListener != null) {
            mOnImageLoadListener.onBlockImageLoadFinished();
        }
    }

    @Override
    public void onLoadImageSize(final int imageWidth, final int imageHeight) {
        mDrawableWidth = imageWidth;
        mDrawableHeight = imageHeight;
        final int layoutWidth = getMeasuredWidth();
        final int layoutHeight = getMeasuredHeight();
        if (layoutWidth == 0 || layoutHeight == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    initFitImageScale(imageWidth, imageHeight);
                }
            });
        } else {
            initFitImageScale(imageWidth, imageHeight);
        }
        notifyInvalidate();
        if (mOnImageLoadListener != null) {
            mOnImageLoadListener.onLoadImageSize(imageWidth, imageHeight);
        }
    }

    @Override
    public void onLoadFail(Exception e) {
        if (mOnImageLoadListener != null) {
            mOnImageLoadListener.onLoadFail(e);
        }
    }

    /**
     * 设置合适的缩放大小
     *
     * @param imageWidth
     * @param imageHeight
     */
    private void initFitImageScale(int imageWidth, int imageHeight) {
        final int layoutWidth = getMeasuredWidth();
        final int layoutHeight = getMeasuredHeight();
        if (imageWidth > imageHeight) {
            fitScale = (1.0f * imageWidth / layoutWidth) * layoutHeight / imageHeight;
            maxScale = 1.0f * imageWidth / layoutWidth * 4;
            minScale = 1.0f * imageWidth / layoutWidth / 4;
            if (minScale > 1) {
                minScale = 1;
            }
        } else {
            fitScale = 1.0f;
            minScale = 0.25f;
            maxScale = 1.0f * imageWidth / layoutWidth;
            float a = (1.0f * imageWidth / layoutWidth) * layoutHeight / imageHeight;
            float density = getContext().getResources().getDisplayMetrics().density;
            maxScale = maxScale * density;
            if (maxScale < 4) {
                maxScale = 4;
            }
            if (minScale > a) {
                minScale = a;
            }
        }
        if (criticalScaleValueHook != null) {
            minScale = criticalScaleValueHook.getMinScale(this, imageWidth, imageHeight, minScale);
            maxScale = criticalScaleValueHook.getMaxScale(this, imageWidth, imageHeight, maxScale);
        }
    }

    private void notifyInvalidate() {
        ViewCompat.postInvalidateOnAnimation(LargeImageView.this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedWindow = false;
        if (mDrawable != null) {
            mDrawable.setVisible(getVisibility() == VISIBLE, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedWindow = true;
        imageBlockImageLoader.stopLoad();
        if (mDrawable != null) {
            mDrawable.setVisible(false, false);
        }
    }

    private boolean overScrollByCompat(int deltaX, int deltaY,
                                       int scrollX, int scrollY,
                                       int scrollRangeX, int scrollRangeY,
                                       int maxOverScrollX, int maxOverScrollY,
                                       boolean isTouchEvent) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();

        int newScrollX = scrollX;

        newScrollX += deltaX;

        int newScrollY = scrollY;

        newScrollY += deltaY;

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

        if (newScrollX < 0) {
            newScrollX = 0;
        }
        if (newScrollY < 0) {
            newScrollY = 0;
        }
        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
        return getScrollX() - oldScrollX == deltaX || getScrollY() - oldScrollY == deltaY;
    }

    private boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < mMinimumVelocity) {
            velocityX = 0;
        }
        if (Math.abs(velocityY) < mMinimumVelocity) {
            velocityY = 0;
        }
        final int scrollY = getScrollY();
        final int scrollX = getScrollX();
        final boolean canFlingX = (scrollX > 0 || velocityX > 0) &&
                (scrollX < getScrollRangeX() || velocityX < 0);
        final boolean canFlingY = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRangeY() || velocityY < 0);
        boolean canFling = canFlingY || canFlingX;
        if (canFling) {
            velocityX = Math.max(-mMaximumVelocity, Math.min(velocityX, mMaximumVelocity));
            velocityY = Math.max(-mMaximumVelocity, Math.min(velocityY, mMaximumVelocity));
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int bottom = getContentHeight();
            int right = getContentWidth();
            mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, Math.max(0, right - width), 0,
                    Math.max(0, bottom - height), width / 2, height / 2);
            notifyInvalidate();
            return true;
        }
        return false;
    }

    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        super.scrollTo(scrollX, scrollY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (hasLoad()) {
            initFitImageScale(mDrawableWidth, mDrawableHeight);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        this.onClickListener = l;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        this.onLongClickListener = l;
    }

    public void setCriticalScaleValueHook(CriticalScaleValueHook criticalScaleValueHook) {
        this.criticalScaleValueHook = criticalScaleValueHook;
    }

    public void smoothScale(float newScale, int centerX, int centerY) {
        if (mScale > newScale) {
            if (accelerateInterpolator == null) {
                accelerateInterpolator = new AccelerateInterpolator();
            }
            scaleHelper.startScale(mScale, newScale, centerX, centerY, accelerateInterpolator);
        } else {
            if (decelerateInterpolator == null) {
                decelerateInterpolator = new DecelerateInterpolator();
            }
            scaleHelper.startScale(mScale, newScale, centerX, centerY, decelerateInterpolator);
        }
        notifyInvalidate();
    }

    @Override
    public float getScale() {
        return mScale;
    }

    @Override
    public void setScale(float scale) {
        setScale(scale, getMeasuredWidth() >> 1, getMeasuredHeight() >> 1);
    }

    public void setScale(float scale, int centerX, int centerY) {
        if (!hasLoad()) {
            return;
        }
        float preScale = mScale;
        mScale = scale;
        int sX = getScrollX();
        int sY = getScrollY();
        int dx = (int) ((sX + centerX) * (scale / preScale - 1));
        int dy = (int) ((sY + centerY) * (scale / preScale - 1));
        overScrollByCompat(dx, dy, sX, sY, getScrollRangeX(), getScrollRangeY(), 0, 0, false);
        notifyInvalidate();
    }

    public OnDoubleClickListener getOnDoubleClickListener() {
        return onDoubleClickListener;
    }

    public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener) {
        this.onDoubleClickListener = onDoubleClickListener;
    }

    private void init() {
        mSmoothMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setOriginalInfo(int width, int height, int locationX, int locationY) {
        mOriginalWidth = width;
        mOriginalHeight = height;
        mOriginalLocationX = locationX;
        mOriginalLocationY = locationY;
//        mOriginalLocationY = mOriginalLocationY - getStatusBarHeight(getContext());
    }

    public void transformIn() {
        if (mOriginalLocationX != 0 || mOriginalWidth != 0) {
            mState = STATE_TRANSFORM_IN;
            mTransformStart = true;
            invalidate();
        }

    }

    public void transformOut() {
        if (mState != STATE_TRANSFORM_OUT) {
            mState = STATE_TRANSFORM_OUT;
            mTransformStart = true;
            invalidate();
        }
    }

    private void initTransform() {
        if (getDrawable() == null) {
            return;
        }

        if (getDrawable() instanceof ColorDrawable) return;

        if (mBitmap == null || mBitmap.isRecycled()) {
            if (getDrawable() instanceof BitmapDrawable) {
                mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            } else {
                return;
            }
        }

        if (mTransfrom != null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        mTransfrom = new Transfrom();

        float xSScale = mOriginalWidth / ((float) mBitmap.getWidth());
        float ySScale = mOriginalHeight / ((float) mBitmap.getHeight());
        float startScale = xSScale > ySScale ? xSScale : ySScale;
        mTransfrom.startScale = startScale;

        float xEScale = getWidth() / ((float) mBitmap.getWidth());
        float yEScale = getHeight() / ((float) mBitmap.getHeight());
        float endScale = xEScale < yEScale ? xEScale : yEScale;
        mTransfrom.endScale = endScale;

        mTransfrom.startRect = new LocationSizeF();
        mTransfrom.startRect.left = mOriginalLocationX;
        mTransfrom.startRect.top = mOriginalLocationY;
        mTransfrom.startRect.width = mOriginalWidth;
        mTransfrom.startRect.height = mOriginalHeight;

        mTransfrom.endRect = new LocationSizeF();
        float bitmapEndWidth = mBitmap.getWidth() * mTransfrom.endScale;
        float bitmapEndHeight = mBitmap.getHeight() * mTransfrom.endScale;
        mTransfrom.endRect.left = (getWidth() - bitmapEndWidth) / 2;
        mTransfrom.endRect.top = (getHeight() - bitmapEndHeight) / 2;
        mTransfrom.endRect.width = bitmapEndWidth;
        mTransfrom.endRect.height = bitmapEndHeight;

        mTransfrom.rect = new LocationSizeF();
    }

    private void getBmpMatrix() {
        if (getDrawable() == null) {
            return;
        }
        if (mTransfrom == null) {
            return;
        }
        if (mBitmap == null || mBitmap.isRecycled()) {
            mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        }

        mSmoothMatrix.setScale(mTransfrom.scale, mTransfrom.scale);
        mSmoothMatrix.postTranslate(-(mTransfrom.scale * mBitmap.getWidth() / 2 - mTransfrom.rect.width / 2),
                -(mTransfrom.scale * mBitmap.getHeight() / 2 - mTransfrom.rect.height / 2));
    }

    private void startTransform(final int state) {
        if (mTransfrom == null) {
            return;
        }
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(3000);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (state == STATE_TRANSFORM_IN) {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", mTransfrom.startScale, mTransfrom.endScale);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", mTransfrom.startRect.left, mTransfrom.endRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", mTransfrom.startRect.top, mTransfrom.endRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", mTransfrom.startRect.width, mTransfrom.endRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", mTransfrom.startRect.height, mTransfrom.endRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", 0, 255);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        } else {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", mTransfrom.endScale, mTransfrom.startScale);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", mTransfrom.endRect.left, mTransfrom.startRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", mTransfrom.endRect.top, mTransfrom.startRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", mTransfrom.endRect.width, mTransfrom.startRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", mTransfrom.endRect.height, mTransfrom.startRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", 255, 0);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                mTransfrom.scale = (Float) animation.getAnimatedValue("scale");
                mTransfrom.rect.left = (Float) animation.getAnimatedValue("left");
                mTransfrom.rect.top = (Float) animation.getAnimatedValue("top");
                mTransfrom.rect.width = (Float) animation.getAnimatedValue("width");
                mTransfrom.rect.height = (Float) animation.getAnimatedValue("height");
                mBgAlpha = (Integer) animation.getAnimatedValue("alpha");
                invalidate();
                ((Activity) getContext()).getWindow().getDecorView().invalidate();
            }
        });
        valueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (state == STATE_TRANSFORM_IN) {
                    mState = STATE_NORMAL;
                }
                if (mTransformListener != null) {
                    mTransformListener.onTransformComplete(state);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    public void setOnTransformListener(TransformListener listener) {
        mTransformListener = listener;
    }


    /**
     * Hook临界值
     */
    public interface CriticalScaleValueHook {

        /**
         * 返回最小的缩放倍数
         * scale为1的话表示，显示的图片和View一样宽
         *
         * @param largeImageView
         * @param imageWidth
         * @param imageHeight
         * @param suggestMinScale 默认建议的最小的缩放倍数
         * @return
         */
        float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale);

        /**
         * 返回最大的缩放倍数
         * scale为1的话表示，显示的图片和View一样宽
         *
         * @param largeImageView
         * @param imageWidth
         * @param imageHeight
         * @param suggestMaxScale 默认建议的最大的缩放倍数
         * @return
         */
        float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale);

    }

    public interface OnDoubleClickListener {
        boolean onDoubleClick(LargeImageView view, MotionEvent event);
    }

    public interface TransformListener {
        //mode STATE_TRANSFORM_IN 1 ,STATE_TRANSFORM_OUT 2
        void onTransformComplete(int mode);// mode 1
    }

    private class Transfrom {
        float startScale;
        float endScale;
        float scale;
        LocationSizeF startRect;
        LocationSizeF endRect;
        LocationSizeF rect;

        void initStartIn() {
            scale = startScale;
            try {
                rect = (LocationSizeF) startRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        void initStartOut() {
            scale = endScale;
            try {
                rect = (LocationSizeF) endRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

    }

    private class LocationSizeF implements Cloneable {
        float left;
        float top;
        float width;
        float height;

        @Override
        public String toString() {
            return "[left:" + left + " top:" + top + " width:" + width + " height:" + height + "]";
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

}
