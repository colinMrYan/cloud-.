package com.inspur.emmcloud.widget.mylistview;



import com.inspur.emmcloud.R;

import android.R.bool;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

@SuppressLint("ResourceAsColor") public class SlideView extends LinearLayout {

	private static final String TAG = "SlideView";

	private Context mContext;
	private LinearLayout mViewContent;
	private Scroller mScroller;
	private OnSlideListener mOnSlideListener;

	private int mHolderWidth = 100;

	private int mLastX = 0;
	private int mLastY = 0;
	private static final int TAN = 2;
	
	public interface OnSlideListener {
		public static final int SLIDE_STATUS_OFF = 0;
		public static final int SLIDE_STATUS_START_SCROLL = 1;
		public static final int SLIDE_STATUS_ON = 2;

		/**
		 * @param view
		 *            current SlideView
		 * @param status
		 *            SLIDE_STATUS_ON or SLIDE_STATUS_OFF
		 */
		public void onSlide(View view, int status);
	}

	public SlideView(Context context) {
		super(context);
		initView();
	}

	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		mContext = getContext();
		mScroller = new Scroller(mContext);

		setOrientation(LinearLayout.HORIZONTAL);
		View.inflate(mContext, R.layout.slide_view_merge, this);
		mViewContent = (LinearLayout) findViewById(R.id.view_content);
		mHolderWidth = Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mHolderWidth, getResources()
						.getDisplayMetrics()));
	}

	public void setButtonText(CharSequence text) {
//		((TextView) findViewById(R.id.delete)).setText(text);
	}

	public void setContentView(View view) {
		mViewContent.addView(view);
	}

	public void setOnSlideListener(OnSlideListener onSlideListener) {
		mOnSlideListener = onSlideListener;
	}

	public void shrink() {
		if (getScrollX() != 0) {
			this.smoothScrollTo(0, 0);
		}
	}
	
	public void reset() {
		mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 0);
		invalidate();
	}
	
	public void open() {
		if(getScrollX() < mHolderWidth){
			int newScrollX = mHolderWidth;
			this.smoothScrollTo(newScrollX, 0);
			if (mOnSlideListener != null) {
				mOnSlideListener.onSlide(this,
						newScrollX == 0 ? OnSlideListener.SLIDE_STATUS_OFF
								: OnSlideListener.SLIDE_STATUS_ON);
			}
		}else{
			mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, Math.abs(mHolderWidth) * 3);
			invalidate();
		}
	}
	
	public boolean close() {
		if(getScrollX() != 0){
			mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, Math.abs(mHolderWidth) * 3);
			invalidate();
			return false;
		}
		return true;
	}

	public boolean onRequireTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		int scrollX = getScrollX();
		Log.d(TAG, "x=" + x + "  y=" + y);
		boolean flag = false;
		boolean isUP = false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			if (mOnSlideListener != null) {
				mOnSlideListener.onSlide(this,
						OnSlideListener.SLIDE_STATUS_START_SCROLL);
			}
			this.scrollTo(0, 0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			int deltaX = x - mLastX;
			int deltaY = y - mLastY;
			if (Math.abs(deltaX) < Math.abs(deltaY) * TAN) {
				break;
			}

			int newScrollX = scrollX - deltaX;
			if (deltaX != 0) {
				if (newScrollX < 0) {
					newScrollX = -1;
				} else if (newScrollX > mHolderWidth) {
					newScrollX = mHolderWidth;
				}
				this.scrollTo(newScrollX, 0);
//				flag = true;
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			int newScrollX = 0;
			Log.d(TAG, "scrollX2=" + scrollX);
			if (scrollX - mHolderWidth * 0.4 > 0) {
				newScrollX = mHolderWidth;
//				flag = true;
			}
			this.smoothScrollTo(newScrollX, 0);
			if (mOnSlideListener != null) {
				mOnSlideListener.onSlide(this,
						newScrollX == 0 ? OnSlideListener.SLIDE_STATUS_OFF
								: OnSlideListener.SLIDE_STATUS_ON);
			}
			isUP = true;
			break;
		}
		default:
			break;
		}

		mLastX = x;
		mLastY = y;
		if (getScrollX() != 0 ) {
			flag = true;
		}
		return flag;
	}

	private void smoothScrollTo(int destX, int destY) {
		// 缓慢滚动到指定位置
		int scrollX = getScrollX();
		int delta = destX - scrollX;
		mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
		invalidate();

	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}


}
