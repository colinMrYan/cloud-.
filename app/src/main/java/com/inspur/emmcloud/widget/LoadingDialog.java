package com.inspur.emmcloud.widget;



import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.inspur.emmcloud.R;


/**
 * 
 * 网络服务等待dialog
 * 
 */
public class LoadingDialog extends Dialog {
	private LayoutInflater inflater;
	private TextView loadtext;
	private LayoutParams lp;
	private Context context;

	public LoadingDialog(Context context,String text) {
		// TODO Auto-generated constructor stub
		super(context, R.style.dialog_progressbar);
		this.context = context;
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_loading, null);
		loadtext = (TextView)view.findViewById(R.id.loading_text);
		loadtext.setText(text);
		setContentView(view);
		// 设置window属性
		lp = getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0.5f; // 去背景遮盖
		lp.alpha = 1.0f;
		getWindow().setAttributes(lp);

	}
	public LoadingDialog(Context context) {
		// TODO Auto-generated constructor stub
		super(context, R.style.dialog_progressbar);
		this.context = context;
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_loading, null);
		loadtext = (TextView)view.findViewById(R.id.loading_text);
		setContentView(view);
		// 设置window属性
		lp = getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0.5f; // 去背景遮盖
		lp.alpha = 1.0f;
		getWindow().setAttributes(lp);

	}
	
	public void show(boolean isShow){
		if (isShow) {
			show();
		}
	}

	@Override
	public void show() {
		if (context != null){
			super.show();
		}

	}

	@Override
	public void dismiss() {
		if (context != null){
			super.dismiss();
		}
	}
}
