package com.inspur.emmcloud.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;

public class DragScaleView extends View {
    private static final int LEFT_TOP = 0x12;
    private static final int RIGHT_BOTTOM = 0x13;
    private static final int CENTER = 0x19;
    //    protected int screenWidth;
//    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    protected Paint paint = new Paint();
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private int offset = DensityUtil.dip2px(4);
    private OnMoveListener onMoveListener;

    private int mParentHeight;
    private int mParentContentHeight;
    private ScrollView scrollView;
    private int mRadius = 8;
    private float minOffset; //防止个别手机dp转px导致移动错位
    private int circleCenter = DensityUtil.dip2px(15);
    private int minHeight = DensityUtil.dip2px(28);
    private int rectLeft = DensityUtil.dip2px(getContext(), 50);
    private int scrollOffset = DensityUtil.dip2px(getContext(), 50);
    private float scale = Resources.getSystem().getDisplayMetrics().density;

    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        minOffset = DensityUtil.dip2px(40) * 1.0f / 4;
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        minOffset = DensityUtil.dip2px(40) * 1.0f / 4;
    }

    public DragScaleView(Context context) {
        super(context);
        minOffset = DensityUtil.dip2px(40) * 1.0f / 4;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int backgroundColor = Color.parseColor("#36A5F6");
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(rectLeft, offset, getWidth(), getHeight() - offset),
                mRadius, mRadius, paint);
        paint.setTextSize(36);//设置字体大小
        paint.setColor(Color.WHITE);
        canvas.drawText("再次点击新建日程", rectLeft + DensityUtil.dip2px(7), minHeight - 2 * offset, paint);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        int bigCircleRadius = DensityUtil.dip2px(4);
        int middleCircleRadius = DensityUtil.dip2px(3.5f);
        int smallCircleRadius = DensityUtil.dip2px(2.5f);


        canvas.drawCircle(rectLeft + circleCenter, offset, bigCircleRadius, paint);
        paint.setColor(backgroundColor);
        canvas.drawCircle(rectLeft + circleCenter, offset, middleCircleRadius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(rectLeft + circleCenter, offset, smallCircleRadius, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth() - circleCenter, getHeight() - offset, bigCircleRadius, paint);
        paint.setColor(backgroundColor);
        canvas.drawCircle(getWidth() - circleCenter, getHeight() - offset, middleCircleRadius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth() - circleCenter, getHeight() - offset, smallCircleRadius, paint);
    }

    public void setParentView(ScrollView scrollView) {
        this.scrollView = scrollView;
        mParentHeight = scrollView.getHeight();
        mParentContentHeight = scrollView.getChildAt(0).getHeight();
    }

    public void updateLastY(int diffLastY) {
        this.lastY += diffLastY;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if ((int) event.getX() < rectLeft) {
            return false;
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            oriLeft = this.getLeft();
            oriRight = this.getRight();
            oriTop = this.getTop();
            oriBottom = this.getBottom();
            lastY = (int) event.getRawY();
            lastX = (int) event.getRawX();
            dragDirection = getDirection(this, (int) event.getX(),
                    (int) event.getY());
        }


        if (action == MotionEvent.ACTION_MOVE) {
            int dy = (int) event.getRawY() - lastY;
            if (Math.abs(dy) < minOffset * 0.7) {
                super.dispatchTouchEvent(event);
                return true;
            }
        }


        // 处理拖动事件
        delDrag(this, event, action);
        invalidate();
        super.dispatchTouchEvent(event);
        return true;
    }

    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */
    protected void delDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int dy = (int) event.getRawY() - lastY;
                dy = (int) (Math.round(dy / minOffset) * minOffset);
                switch (dragDirection) {
                    case RIGHT_BOTTOM: // 左下
                        bottom(v, dy);
                        break;
                    case LEFT_TOP: // 右上
                        top(v, dy);
                        break;
                    case CENTER: // 点击中心-->>移动
                        center(v, dy);
                        break;
                }
                if (onMoveListener != null) {
                    boolean isNeedScroll = (dy < 0 && oriTop < scrollView.getScrollY() + scrollOffset) ||
                            (dy > 0 && oriBottom > (scrollView.getScrollY() + mParentHeight - scrollOffset));

                    onMoveListener.moveTo(isNeedScroll, dy, oriTop, getHeight() - 2 * offset);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                LogUtils.jasonDebug("MotionEvent.ACTION_UP------------");
                dragDirection = 0;
                break;
        }
    }

    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dy
     */
    private void center(View v, int dy) {

        int top = v.getTop() + dy;
        int bottom = v.getBottom() + dy;
        if (top < 0) {
            top = 0;
            bottom = top + v.getHeight();
        }
        if (bottom > mParentContentHeight) {
            bottom = mParentContentHeight;
            top = bottom - v.getHeight();
        }
        //防止个别手机dp转px导致移动错位
        top = DensityUtil.dip2px(Math.round(top / scale));
        bottom = DensityUtil.dip2px(Math.round(bottom / scale));
        v.layout(oriLeft, top, oriRight, bottom);
        oriTop = top;
        oriBottom = bottom;
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;

        //todo
        if (oriTop < 0) {
            oriTop = 0;
        }
        if (oriTop > mParentContentHeight - minHeight) {
            oriTop = mParentContentHeight - minHeight;
        }

        if (oriBottom - oriTop < minHeight) {
            oriBottom = oriTop + minHeight;
        }
        //防止个别手机dp转px导致移动错位
        oriTop = DensityUtil.dip2px(Math.round(oriTop / scale));
        oriBottom = DensityUtil.dip2px(Math.round(oriBottom / scale));
        v.layout(oriLeft, oriTop, oriRight, oriBottom);
    }

    /**
     * 触摸点为下边缘
     *
     * @param v
     * @param dy
     */
    private void bottom(View v, int dy) {
        oriBottom += dy;
        if (oriBottom > mParentContentHeight) {
            oriBottom = mParentContentHeight;
        }

        if (oriBottom - minHeight < 0) {
            oriBottom = minHeight;
        }
        if (oriBottom - oriTop < minHeight) {
            oriTop = oriBottom - minHeight;
        }
        //防止个别手机dp转px导致移动错位
        oriTop = DensityUtil.dip2px(Math.round(oriTop / scale));
        oriBottom = DensityUtil.dip2px(Math.round(oriBottom / scale));
        v.layout(oriLeft, oriTop, oriRight, oriBottom);
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        if (x < minHeight * 3 + rectLeft && y < minHeight) {
            return LEFT_TOP;
        }
        if (right - left - x < minHeight * 3 && bottom - top - y < minHeight) {
            return RIGHT_BOTTOM;
        }
        return CENTER;
    }

    /**
     * 获取截取宽度
     *
     * @return
     */
    public int getCutWidth() {
        return getWidth() - 2 * offset;
    }

    /**
     * 获取截取高度
     *
     * @return
     */
    public int getCutHeight() {
        return getHeight() - 2 * offset;
    }

    public OnMoveListener getOnMoveListener() {
        return onMoveListener;
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public interface OnMoveListener {
        void moveTo(boolean isNeedScroll, int ScrollOffset, int top, int height);
    }
}
