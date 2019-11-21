package com.inspur.emmcloud.widget.tipsview.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by libaochao on 2019/10/8.
 */

public class BallView extends android.support.v7.widget.AppCompatTextView {
    Paint paint;
    int radius = 30;
    Listener listener;

    public BallView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        setGravity(Gravity.CENTER);
        setText("1");
        setTextColor(Color.WHITE);
        setTextSize(12);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(radius * 2, radius * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, radius, paint);
    }

    public void startAnimation(Point startPoint, Point endPoint) {
        //控制点，二阶贝塞尔曲线需要三个点，目前只有两个，所以需要新增一个
        int pointX = (startPoint.x + endPoint.x) / 2 - 30;
        int pointY = (int) (startPoint.y + endPoint.y) / 4;
        Point controlPoint = new Point(pointX, pointY);
        BezierEvaluator bezierEvaluator = new BezierEvaluator(controlPoint);
        ValueAnimator anim = ValueAnimator.ofObject(bezierEvaluator, startPoint, endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Point point = (Point) valueAnimator.getAnimatedValue();
                //              设置相对于屏幕的原点
                setX(point.x);
                setY(point.y);
                invalidate();
            }
        });
        anim.setDuration(800);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewGroup viewGroup = (ViewGroup) getParent();
                viewGroup.removeView(BallView.this);
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onAnimationEnd();
    }

}
