package com.inspur.emmcloud.widget.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * 
 * 修改用户头像弹出dialog
 * 
 */
public class MyDialog extends Dialog {

	private float ratio = 0.800f;
	private Activity context;

	public MyDialog(Activity context,int layout) {
		super(context);
		this.context = context;
		setContentView(layout);
		// TODO Auto-generated constructor stub
	}

	public MyDialog(Activity context,int layout,
			int style) {
		super(context, style);
		this.context = context;
		setContentView(layout);
	}
	public MyDialog(Activity context,int layout,
			int style,float ratio) {
		super(context, style);
		this.ratio = ratio;
		this.context = context;
		setContentView(layout);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new BitmapDrawable());
		setWidth();
	}

	private void setWidth() {
		WindowManager m = context.getWindowManager();
		Display d = m.getDefaultDisplay(); 
		int width = d.getWidth();
		WindowManager.LayoutParams p = getWindow().getAttributes();
		p.x = 0;
		p.y = 0;
		p.width = (int) (width * ratio);
		this.getWindow().setAttributes(
				(WindowManager.LayoutParams) p);
	}

}
