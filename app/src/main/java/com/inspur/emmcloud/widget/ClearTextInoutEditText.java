package com.inspur.emmcloud.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.inspur.emmcloud.R;

/**
 * Created by chenmch on 2019/1/1.
 */

public class ClearTextInoutEditText extends TextInputEditText implements TextWatcher, View.OnFocusChangeListener {
    private final int DRAWABLE_LEFT = 0;
    private final int DRAWABLE_TOP = 1;
    private final int DRAWABLE_RIGHT = 2;
    private final int DRAWABLE_BOTTOM = 3;
    private Context mContext;
    //左边图标
    private Drawable mLeftDrawable;
    //右侧删除图标
    private Drawable mRightDrawable;

    public ClearTextInoutEditText(Context context) {
        super(context);
        init(context);
    }

    public ClearTextInoutEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClearTextInoutEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (context == null || isInEditMode()) return;
        mContext = context;
        mRightDrawable = getCompoundDrawables()[2];
        if (mRightDrawable == null) {
            mRightDrawable = getResources().getDrawable(
                    R.drawable.icon_delete_input);
        }
        mRightDrawable.setBounds(0, 0, mRightDrawable.getIntrinsicWidth(),
                mRightDrawable.getIntrinsicHeight());
        this.addTextChangedListener(this);
        this.setOnFocusChangeListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        updateCleanable(s.toString().length(), true);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //可以获得上下左右四个drawable，右侧排第二。图标没有设置则为空。
        if (mRightDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            //检查点击的位置是否是右侧的删除图标
            //注意，使用getRawX()是获取相对屏幕的位置，getX()可能获取相对父组件的位置
            int leftEdgeOfRightDrawable = getRight() - getPaddingRight()
                    - mRightDrawable.getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        mRightDrawable = null;
        super.finalize();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //更新状态，检查是否显示删除按钮
        updateCleanable(this.getText().length(), hasFocus);
    }

    /**
     * 当内容不为空，而且获得焦点，才显示右侧删除按钮
     *
     * @param length
     * @param hasFocus
     */
    private void updateCleanable(int length, boolean hasFocus) {
        Drawable right = (length > 0 && hasFocus) ? mRightDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }
}
