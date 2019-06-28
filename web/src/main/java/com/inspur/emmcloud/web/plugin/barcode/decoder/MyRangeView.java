package com.inspur.emmcloud.web.plugin.barcode.decoder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by stone on 2017/10/6.
 */

public class MyRangeView extends ImageView {

    private Rect rectangle;
    private Paint paint;

    public MyRangeView(Context context) {
        super(context);
        setUp();

    }

    public MyRangeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();

    }

    public MyRangeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    private void setUp() {
        int x = 0;
        int y = 0;
        int sideLength = 100;

        // create a rectangle that we'll draw later
        rectangle = new Rect(x, y, sideLength, sideLength);

        // create the Paint and set its color
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw a blue retangle fo indicator
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        canvas.drawRect(rectangle, paint);
    }

    public void setRectangle(Rect r) {
        rectangle.left = r.left;
        rectangle.top = r.top;
        rectangle.right = r.right;
        rectangle.bottom = r.bottom;
    }


}
