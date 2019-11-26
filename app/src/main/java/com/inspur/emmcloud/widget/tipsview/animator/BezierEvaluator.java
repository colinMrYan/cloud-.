package com.inspur.emmcloud.widget.tipsview.animator;

import android.animation.TypeEvaluator;

/**
 * Created by libaochao on 2019/10/8.
 */

public class BezierEvaluator implements TypeEvaluator<Point> {
    private Point controlPoint;

    public BezierEvaluator(Point controlPoint) {
        this.controlPoint = controlPoint;
    }

    @Override
    public Point evaluate(float t, Point startValue, Point endValue) {
//      贝塞尔曲线二阶公式
        int x = (int) ((1 - t) * (1 - t) * startValue.x + 2 * t * (1 - t) * controlPoint.x + t * t * endValue.x);
        int y = (int) ((1 - t) * (1 - t) * startValue.y + 2 * t * (1 - t) * controlPoint.y + t * t * endValue.y);
        return new Point(x, y);
    }
}
