/***********************************************************************************
 * The MIT License (MIT)

 * Copyright (c) 2014 Robin Chutaux

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR Locale PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.inspur.emmcloud.widget.calendarlistview;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.DensityUtil;

public class SimpleMonthView extends View {

	public static final String VIEW_PARAMS_HEIGHT = "height";
	public static final String VIEW_PARAMS_MONTH = "month";
	public static final String VIEW_PARAMS_YEAR = "year";
	public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
	public static final String VIEW_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
	public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
	public static final String VIEW_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
	public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
	public static final String VIEW_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
	public static final String VIEW_PARAMS_WEEK_START = "week_start";

	private  final int SELECTED_CIRCLE_ALPHA = 128;
	protected  int DEFAULT_HEIGHT = 32;
	protected  final int DEFAULT_NUM_ROWS = 6;
	protected  int DAY_SELECTED_CIRCLE_SIZE;
	protected  int DAY_SEPARATOR_WIDTH = 1;
	protected  int MINI_DAY_NUMBER_TEXT_SIZE;
	protected  int MINI_DAY_CHINNESE_TEXT_SIXE;
	protected  int MIN_HEIGHT = 10;
	protected  int MONTH_DAY_LABEL_TEXT_SIZE;
	protected  int MONTH_HEADER_SIZE;
	protected  int MONTH_LABEL_TEXT_SIZE;
	protected  int DAY_NUM_CHINEST_DAY_SPACE;
	protected  int CALENDAR_COLOR_RADIUS;
	protected  int CALENDAR_COLOR_RADIUS_MAGIN_TOP;

	protected int mPadding = 0;

	private String mDayOfWeekTypeface;
	private String mMonthTitleTypeface;

	protected Paint mMonthDayLabelPaint;
	protected Paint mMonthNumPaint;
	protected Paint mChineseDayPaint;
	protected Paint mMonthTitleBGPaint;
	protected Paint mMonthTitlePaint;
	protected Paint mSelectedCirclePaint;
	protected Paint mColorCirclePaint;
	protected int mCurrentDayTextColor;
	protected int mMonthTextColor;
	protected int mDayTextColor;
	protected int mDayNumColor;
	protected int mMonthTitleBGColor;
	protected int mPreviousDayColor;
	protected int mSelectedDaysColor;
	protected int mWeekendNormalDatColor;
	private final StringBuilder mStringBuilder;

	protected boolean mHasToday = false;
	protected boolean mIsPrev = false;
	protected int mSelectedBeginDay = -1;
	protected int mSelectedLastDay = -1;
	protected int mSelectedBeginMonth = -1;
	protected int mSelectedLastMonth = -1;
	protected int mSelectedBeginYear = -1;
	protected int mSelectedLastYear = -1;
	protected int mToday = -1;
	protected int mWeekStart = 1;
	protected int mNumDays = 7;
	protected int mNumCells = mNumDays;
	private int mDayOfWeekStart = 0;
	protected int mMonth;
	protected Boolean mDrawRect;
	protected int mRowHeight = DEFAULT_HEIGHT;
	protected int mWidth;
	protected int mYear;
	final Time today;

	private final Calendar mCalendar;
	private final Calendar mDayLabelCalendar;
	private final Boolean isPrevDayEnabled;

	private int mNumRows = DEFAULT_NUM_ROWS;

	private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();

	private OnDayClickListener mOnDayClickListener;

	private boolean isSingleMonth = false; // 判断是否只是显示单月 ui处理不同

	public SimpleMonthView(Context context) {
		this(context,false);
	}

	public SimpleMonthView(Context context, boolean isSingleMonth) {
		super(context);
		this.isSingleMonth = isSingleMonth;
		Resources resources = context.getResources();
		mDayLabelCalendar = Calendar.getInstance();
		mCalendar = Calendar.getInstance();
		today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		mDayOfWeekTypeface = "sans-serif";
		mMonthTitleTypeface ="sans-serif";
		mCurrentDayTextColor = resources.getColor(R.color.normal_day);
		mMonthTextColor = resources.getColor(R.color.normal_day);
		mDayTextColor = resources.getColor(R.color.normal_day);
		mDayNumColor = resources.getColor(R.color.normal_day);
		mPreviousDayColor = resources.getColor(R.color.normal_day);
		mSelectedDaysColor = resources
				.getColor(R.color.selected_day_background);
		mMonthTitleBGColor = resources.getColor(R.color.selected_day_text);
		mWeekendNormalDatColor = resources.getColor(R.color.weekend_normal_day);

		mDrawRect = false;

		mStringBuilder = new StringBuilder(50);

		MINI_DAY_NUMBER_TEXT_SIZE = resources
				.getDimensionPixelSize(R.dimen.text_size_day);// x日
		MINI_DAY_CHINNESE_TEXT_SIXE = resources
				.getDimensionPixelSize(R.dimen.text_size_chinese_day);// 农历
		MONTH_LABEL_TEXT_SIZE = resources
				.getDimensionPixelSize(R.dimen.text_size_month);// xxxx年x月
		MONTH_DAY_LABEL_TEXT_SIZE = resources
				.getDimensionPixelSize(R.dimen.text_size_day_name);// 星期几
		CALENDAR_COLOR_RADIUS = DensityUtil.dip2px(getContext(), 2.5f);
		CALENDAR_COLOR_RADIUS_MAGIN_TOP = DensityUtil.dip2px(getContext(), 7);
		if (isSingleMonth) {
			MONTH_HEADER_SIZE = 0;
		} else {
			MONTH_HEADER_SIZE = resources
					.getDimensionPixelOffset(R.dimen.header_month_height);// xx年x月+星期几
		}
		DAY_SELECTED_CIRCLE_SIZE = resources
				.getDimensionPixelOffset(R.dimen.selected_day_radius);
		DAY_NUM_CHINEST_DAY_SPACE = DensityUtil.dip2px(getContext(), 2);

		mRowHeight = ((resources.getDimensionPixelSize(R.dimen.calendar_height) - MONTH_HEADER_SIZE) / 6);

		isPrevDayEnabled = true;

		initView();

	}

	private int calculateNumRows() {
		int offset = findDayOffset();
		int dividend = (offset + mNumCells) / mNumDays;
		int remainder = (offset + mNumCells) % mNumDays;
		return (dividend + (remainder > 0 ? 1 : 0));
	}

	// private void drawMonthDayLabels(Canvas canvas) {
	// int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
	// int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);
	//
	// for (int i = 0; i < mNumDays; i++) {
	// int calendarDay = (i + mWeekStart) % mNumDays;
	// int x = (2 * i + 1) * dayWidthHalf + mPadding;
	// mDayLabelCalendar.set(MyCalendar.DAY_OF_WEEK, calendarDay);
	// canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(MyCalendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()),
	// x, y, mMonthDayLabelPaint);
	// }
	// }

	private void drawMonthTitle(Canvas canvas) {
		int x = getMonthTitleXOff();
		int y = (MONTH_HEADER_SIZE - MONTH_LABEL_TEXT_SIZE) / 2
				+ (MONTH_LABEL_TEXT_SIZE) - 6;
		// StringBuilder stringBuilder = new
		// StringBuilder(getMonthString().toLowerCase());
		// stringBuilder.setCharAt(0,
		// Character.toUpperCase(stringBuilder.charAt(0)));
		String monthText = (mMonth + 1) + "月";
		canvas.drawText(monthText, x, y, mMonthTitlePaint);

	}

	protected int getMonthTitleXOff() {
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
		int dayOffset = findDayOffset();
		int day = 1;
		int x = paddingDay * (1 + dayOffset * 2) + mPadding;
		return x;
	}

	private int findDayOffset() {
		return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays)
				: mDayOfWeekStart) - mWeekStart;
	}

	private String getMonthAndYearString() {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_NO_MONTH_DAY;
		mStringBuilder.setLength(0);
		long millis = mCalendar.getTimeInMillis();
		return DateUtils.formatDateRange(getContext(), millis, millis, flags);
	}

	private String getMonthString() {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
				| DateUtils.FORMAT_NO_MONTH_DAY;
		mStringBuilder.setLength(0);
		long millis = mCalendar.getTimeInMillis();
		return DateUtils.formatDateRange(getContext(), millis, millis, flags);
	}

	private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
		if (mOnDayClickListener != null
				&& (isPrevDayEnabled || !((calendarDay.month == today.month)
						&& (calendarDay.year == today.year) && calendarDay.day < today.monthDay))) {
			mOnDayClickListener.onDayClick(this, calendarDay);
		}
	}

	private boolean sameDay(int monthDay, Time time) {
		return (mYear == time.year) && (mMonth == time.month)
				&& (monthDay == time.monthDay);
	}

	private boolean prevDay(int monthDay, Time time) {
		return ((mYear < time.year))
				|| (mYear == time.year && mMonth < time.month)
				|| (mMonth == time.month && monthDay < time.monthDay);
	}

	/**
	 * draw dayOfMonth
	 * 
	 * @param canvas
	 */
	protected void drawMonthNums(Canvas canvas) {
		/** -6 为了获得canvas.drawText字体baseline处的y值 **/
		int y = (int) ((mRowHeight - MINI_DAY_NUMBER_TEXT_SIZE
				- MINI_DAY_CHINNESE_TEXT_SIXE - DAY_NUM_CHINEST_DAY_SPACE)
				/ 2 + MINI_DAY_NUMBER_TEXT_SIZE + MONTH_HEADER_SIZE - 6)-6;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
		int dayOffset = findDayOffset();
		int day = 1;
		while (day <= mNumCells) {
			int x = paddingDay * (1 + dayOffset * 2) + mPadding;
			if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear)
					|| (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
				// if (mDrawRect)
				// {
				// RectF rectF = new RectF(x - DAY_SELECTED_CIRCLE_SIZE, (y -
				// MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x
				// + DAY_SELECTED_CIRCLE_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE /
				// 3) + DAY_SELECTED_CIRCLE_SIZE);
				// canvas.drawRoundRect(rectF, 10.0f,
				// 10.0f,mSelectedCirclePaint);
				// }
				// else
				canvas.drawCircle(x, 6 + y - MINI_DAY_NUMBER_TEXT_SIZE / 2
						+ MINI_DAY_CHINNESE_TEXT_SIXE / 2
						+ DAY_NUM_CHINEST_DAY_SPACE / 2,
						DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
			}
			if (mHasToday && (mToday == day)) {
				mMonthNumPaint.setColor(mSelectedDaysColor);
				mMonthNumPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.BOLD));
				mChineseDayPaint.setColor(mSelectedDaysColor);
				mChineseDayPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.BOLD));
			} else if (!CalendarTrans.isWeekendDay(mYear, mMonth + 1, day)) {
				mMonthNumPaint.setColor(mDayNumColor);
				mMonthNumPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.NORMAL));
				mChineseDayPaint.setColor(mDayNumColor);
				mChineseDayPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.NORMAL));
			} else {
				mMonthNumPaint.setColor(mWeekendNormalDatColor);
				mMonthNumPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.NORMAL));
				mChineseDayPaint.setColor(mWeekendNormalDatColor);
				mChineseDayPaint.setTypeface(Typeface
						.defaultFromStyle(Typeface.NORMAL));

			}

			if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear)
					|| (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
				mMonthNumPaint.setColor(mMonthTitleBGColor);
				mChineseDayPaint.setColor(mMonthTitleBGColor);
			}
			// if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 &&
			// mSelectedBeginYear == mSelectedLastYear &&
			// mSelectedBeginMonth == mSelectedLastMonth &&
			// mSelectedBeginDay == mSelectedLastDay &&
			// day == mSelectedBeginDay &&
			// mMonth == mSelectedBeginMonth &&
			// mYear == mSelectedBeginYear))
			// mMonthNumPaint.setColor(mSelectedDaysColor);
			//
			// if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 &&
			// mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear ==
			// mYear) &&
			// (((mMonth == mSelectedBeginMonth && mSelectedLastMonth ==
			// mSelectedBeginMonth) && ((mSelectedBeginDay < mSelectedLastDay &&
			// day > mSelectedBeginDay && day < mSelectedLastDay) ||
			// (mSelectedBeginDay > mSelectedLastDay && day < mSelectedBeginDay
			// && day > mSelectedLastDay))) ||
			// ((mSelectedBeginMonth < mSelectedLastMonth && mMonth ==
			// mSelectedBeginMonth && day > mSelectedBeginDay) ||
			// (mSelectedBeginMonth < mSelectedLastMonth && mMonth ==
			// mSelectedLastMonth && day < mSelectedLastDay)) ||
			// ((mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
			// mSelectedBeginMonth && day < mSelectedBeginDay) ||
			// (mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
			// mSelectedLastMonth && day > mSelectedLastDay))))
			// {
			// mMonthNumPaint.setColor(mSelectedDaysColor);
			// }
			//
			// if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 &&
			// mSelectedBeginYear != mSelectedLastYear && ((mSelectedBeginYear
			// == mYear && mMonth == mSelectedBeginMonth) || (mSelectedLastYear
			// == mYear && mMonth == mSelectedLastMonth)) &&
			// (((mSelectedBeginMonth < mSelectedLastMonth && mMonth ==
			// mSelectedBeginMonth && day < mSelectedBeginDay) ||
			// (mSelectedBeginMonth < mSelectedLastMonth && mMonth ==
			// mSelectedLastMonth && day > mSelectedLastDay)) ||
			// ((mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
			// mSelectedBeginMonth && day > mSelectedBeginDay) ||
			// (mSelectedBeginMonth > mSelectedLastMonth && mMonth ==
			// mSelectedLastMonth && day < mSelectedLastDay)))))
			// {
			// mMonthNumPaint.setColor(mSelectedDaysColor);
			// }
			//
			// if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 &&
			// mSelectedBeginYear == mSelectedLastYear && mYear ==
			// mSelectedBeginYear) &&
			// ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth &&
			// mSelectedBeginMonth < mSelectedLastMonth) ||
			// (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth &&
			// mSelectedBeginMonth > mSelectedLastMonth)))
			// {
			// mMonthNumPaint.setColor(mSelectedDaysColor);
			// }
			//
			// if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 &&
			// mSelectedBeginYear != mSelectedLastYear) &&
			// ((mSelectedBeginYear < mSelectedLastYear && ((mMonth >
			// mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth <
			// mSelectedLastMonth && mYear == mSelectedLastYear))) ||
			// (mSelectedBeginYear > mSelectedLastYear && ((mMonth <
			// mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth >
			// mSelectedLastMonth && mYear == mSelectedLastYear)))))
			// {
			// mMonthNumPaint.setColor(mSelectedDaysColor);
			// }
			//
			// if (!isPrevDayEnabled && prevDay(day, today) && today.month ==
			// mMonth && today.year == mYear)
			// {
			// mMonthNumPaint.setColor(mPreviousDayColor);
			// mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			// }

			canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
			String chineseDay = new CalendarTrans().getChineseDay(mYear,
					mMonth + 1, day);

			int chineseDayY = y + MINI_DAY_CHINNESE_TEXT_SIXE
					+ DAY_NUM_CHINEST_DAY_SPACE;
			canvas.drawText(chineseDay, x, chineseDayY, mChineseDayPaint);
			int colorCircleY = chineseDayY+CALENDAR_COLOR_RADIUS_MAGIN_TOP+CALENDAR_COLOR_RADIUS;
			if (day %2 == 1) {
				canvas.drawCircle(x, colorCircleY, CALENDAR_COLOR_RADIUS, mColorCirclePaint);
			}
			dayOffset++;
			if (dayOffset == mNumDays) {
				dayOffset = 0;
				y += mRowHeight;
			}
			day++;
		}
	}

	protected void drawLines(Canvas canvas) {

		Paint linesPaint = new Paint();
		linesPaint.setColor(getResources().getColor(R.color.split_line));
		linesPaint.setStyle(Style.FILL);

		int y = MONTH_HEADER_SIZE;
		int day = 1;
		int dayOffset = findDayOffset();
		int lineHight = DensityUtil.dip2px(getContext(), 0.5f);
		if (!isSingleMonth){
			canvas.drawLine(0, y, getScreenWidth(), y, linesPaint);
		}
		while (day <= mNumCells) {
			dayOffset++;
			if (dayOffset == mNumDays) {
				dayOffset = 0;
				y += mRowHeight;
				canvas.drawLine(0, y, getScreenWidth(), y + lineHight,
						linesPaint);
			}
			day++;
		}
	}

	/**
	 * 获取屏幕宽度
	 * 
	 * @return
	 */
	public int getScreenWidth() {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		return width;
	}

	public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
		int padding = mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}

		int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
		int day = 1
				+ ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset())
				+ yDay * mNumDays;

		if (mMonth > 11 || mMonth < 0
				|| CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
			return null;

		return new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);
	}

	protected void initView() {
		mMonthTitlePaint = new Paint();
		mMonthTitlePaint.setFakeBoldText(true);
		mMonthTitlePaint.setAntiAlias(true);
		mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
		mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface,
				Typeface.BOLD));
		mMonthTitlePaint.setColor(mMonthTextColor);
		mMonthTitlePaint.setTextAlign(Align.CENTER);
		mMonthTitlePaint.setStyle(Style.FILL);

		mMonthTitleBGPaint = new Paint();
		mMonthTitleBGPaint.setFakeBoldText(true);
		mMonthTitleBGPaint.setAntiAlias(true);
		mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
		mMonthTitleBGPaint.setTextAlign(Align.CENTER);
		mMonthTitleBGPaint.setStyle(Style.FILL);

		mSelectedCirclePaint = new Paint();
		mSelectedCirclePaint.setFakeBoldText(true);
		mSelectedCirclePaint.setAntiAlias(true);
		mSelectedCirclePaint.setColor(mSelectedDaysColor);
		mSelectedCirclePaint.setTextAlign(Align.CENTER);
		mSelectedCirclePaint.setStyle(Style.FILL);
		// mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
		mColorCirclePaint = new Paint();
		mColorCirclePaint.setAntiAlias(true);
		mColorCirclePaint.setColor(mSelectedDaysColor);
		mColorCirclePaint.setStyle(Style.FILL);

		mMonthDayLabelPaint = new Paint();
		mMonthDayLabelPaint.setAntiAlias(true);
		mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
		mMonthDayLabelPaint.setColor(mDayTextColor);
		mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface,
				Typeface.NORMAL));
		mMonthDayLabelPaint.setStyle(Style.FILL);
		mMonthDayLabelPaint.setTextAlign(Align.CENTER);
		mMonthDayLabelPaint.setFakeBoldText(true);

		mMonthNumPaint = new Paint();
		mMonthNumPaint.setAntiAlias(true);
		mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
		mMonthNumPaint.setStyle(Style.FILL);
		mMonthNumPaint.setTextAlign(Align.CENTER);
		mMonthNumPaint.setFakeBoldText(false);

		mChineseDayPaint = new Paint();
		mChineseDayPaint.setAntiAlias(true);
		mChineseDayPaint.setTextSize(MINI_DAY_CHINNESE_TEXT_SIXE);
		mChineseDayPaint.setStyle(Style.FILL);
		mChineseDayPaint.setTextAlign(Align.CENTER);
		mChineseDayPaint.setFakeBoldText(false);
	}

	protected void onDraw(Canvas canvas) {
		if (!isSingleMonth) {
			drawMonthTitle(canvas);
		}
		// drawMonthDayLabels(canvas);
		drawMonthNums(canvas);
		drawLines(canvas);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight
				* mNumRows + MONTH_HEADER_SIZE);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(
					event.getX(), event.getY());
			if (calendarDay != null) {
				onDayClick(calendarDay);
			}
		}
		return true;
	}

	public void reuse() {
		mNumRows = DEFAULT_NUM_ROWS;
		requestLayout();
	}

	public void setMonthParams(HashMap<String, Integer> params) {
		if (!params.containsKey(VIEW_PARAMS_MONTH)
				&& !params.containsKey(VIEW_PARAMS_YEAR)) {
			throw new InvalidParameterException(
					"You must specify month and year for this view");
		}
		setTag(params);

		if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
			mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
			if (mRowHeight < MIN_HEIGHT) {
				mRowHeight = MIN_HEIGHT;
			}
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
			mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DAY)) {
			mSelectedLastDay = params.get(VIEW_PARAMS_SELECTED_LAST_DAY);
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
			mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_MONTH)) {
			mSelectedLastMonth = params.get(VIEW_PARAMS_SELECTED_LAST_MONTH);
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
			mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_YEAR)) {
			mSelectedLastYear = params.get(VIEW_PARAMS_SELECTED_LAST_YEAR);
		}

		mMonth = params.get(VIEW_PARAMS_MONTH);
		mYear = params.get(VIEW_PARAMS_YEAR);

		mHasToday = false;
		mToday = -1;

		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.YEAR, mYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

		if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
			mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}

		mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
		for (int i = 0; i < mNumCells; i++) {
			final int day = i + 1;
			if (sameDay(day, today)) {
				mHasToday = true;
				mToday = day;
			}

			mIsPrev = prevDay(day, today);
		}

		mNumRows = calculateNumRows();
	}

	public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
		mOnDayClickListener = onDayClickListener;
	}

	public static abstract interface OnDayClickListener {
		public abstract void onDayClick(SimpleMonthView simpleMonthView,
										SimpleMonthAdapter.CalendarDay calendarDay);
	}
}