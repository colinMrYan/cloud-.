package com.inspur.mdm.widght;




import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.inspur.mdm.utils.MDMResUtils;


/**
 * 
 * 网络服务等待dialog
 * 
 */
public class MDMLoadingDialog extends Dialog {
	private LayoutInflater inflater;
	private LayoutParams lp;

	public MDMLoadingDialog(Context context) {
		// TODO Auto-generated constructor stub
		super(context,MDMResUtils.getStyleID("dialog_progressbar"));
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(MDMResUtils.getLayoutID("mdm_dialog_loading"), null);
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

}
