package com.inspur.imp.plugin.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 这个是将画廊控件进行重写，即每次滑动时只滑动一页
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class SimpleGallery extends Gallery {

	public SimpleGallery(Context context) {
		super(context);
	}

	public SimpleGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		int kEvent;
		if (isScrollingLeft(e1, e2)) { // Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else { // Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;

	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}
}
