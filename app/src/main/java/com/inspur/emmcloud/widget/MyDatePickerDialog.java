/**
 * 
 * MyDatePickerDialog.java
 * classes : com.inspur.emmcloud.widget.MyDatePickerDialog
 * V 1.0.0
 * Create at 2016年9月23日 下午3:36:09
 */
package com.inspur.emmcloud.widget;

import java.util.Calendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.inspur.emmcloud.util.privates.TimeUtils;

/**
 * com.inspur.emmcloud.widget.MyDatePickerDialog
 * create at 2016年9月23日 下午3:36:09
 */
public class MyDatePickerDialog extends DatePickerDialog {
	
	
	private Context context;
	/**
	 * @param context
	 * @param theme
	 * @param listener
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 */
	public MyDatePickerDialog(Context context, int theme,
			OnDateSetListener listener, int year, int monthOfYear,
			int dayOfMonth) {
		super(context, theme, listener, year, monthOfYear, dayOfMonth);
		// TODO Auto-generated constructor stub
		this.context = context;
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		String title = TimeUtils.calendar2FormatString(context, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
		this.setTitle(title);
	}

	/**
	 * @param context
	 * @param callBack
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 */
	public MyDatePickerDialog(Context context, OnDateSetListener callBack,
			int year, int monthOfYear, int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		// TODO Auto-generated constructor stub
		this.context = context;
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		String title = TimeUtils.calendar2FormatString(context, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
		this.setTitle(title);
	}
	
	
	public void setHideYear(){
		Locale locale = context.getResources().getConfiguration().locale;
		//nexus Android7.0手机这里会出现空指针异常
		try {
			if (locale.toString().contains("en")) {
				 ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0)).getChildAt(0)).getChildAt(2).setVisibility(View.GONE);  
			}else {
				((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0)).getChildAt(0)).getChildAt(0).setVisibility(View.GONE);  
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		 
	}

	
	
	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		// TODO Auto-generated method stub
		super.onDateChanged(view, year, month, day);
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		String title = TimeUtils.calendar2FormatString(context, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
		this.setTitle(title);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//解决弹出DatePickerDialog后，修改日期，不点击完成，点击返回键，会修改TextView中的日期
		//super.onStop();
	}

}
