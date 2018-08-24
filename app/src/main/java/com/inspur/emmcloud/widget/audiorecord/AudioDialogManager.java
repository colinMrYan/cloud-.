package com.inspur.emmcloud.widget.audiorecord;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

public class AudioDialogManager {

	/**
	 * 以下为dialog的初始化控件，包括其中的布局文件
	 */

	private Dialog dialog;

	private ImageView recorderImg;

	private TextView lableText;

	private Context mContext;
	private boolean isStatusRecording = false;

	public AudioDialogManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void showRecordingDialog() {
		// TODO Auto-generated method stub

		dialog = new Dialog(mContext);
		// 用layoutinflater来引用布局
		dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
		dialog.getWindow().setDimAmount(0);
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_record_manager, null);
		dialog.setContentView(view);
		recorderImg = (ImageView) dialog.findViewById(R.id.iv_recorder);
		lableText = (TextView) dialog.findViewById(R.id.tv_recorder);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		isStatusRecording = true;
	}

	/**
	 * 设置正在录音时的dialog界面
	 */
	public void recording() {
		if (dialog != null && dialog.isShowing()) {
			isStatusRecording = true;
			recorderImg.setImageResource(R.drawable.ic_recorder_volume_level_v1);
			lableText.setText(R.string.slide_up_to_cancel);
			lableText.setBackgroundColor(ContextCompat.getColor(mContext,android.R.color.transparent));
		}
	}

	/**
	 * 取消界面
	 */
	public void wantToCancel() {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing()) {
			isStatusRecording= false;
			recorderImg.setImageResource(R.drawable.ic_recorder_cancel);
			lableText.setText(R.string.release_to_cancel);
			lableText.setBackgroundResource(R.drawable.bg_record_dialog_text);
		}

	}

	// 时间过短
	public void tooShort() {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing()) {
			isStatusRecording= false;
			recorderImg.setImageResource(R.drawable.ic_recorder_too_short);
			lableText.setText(R.string.recording_too_short);
			lableText.setBackgroundColor(ContextCompat.getColor(mContext,android.R.color.transparent));
		}

	}

	// 隐藏dialog
	public void dimissDialog() {
		// TODO Auto-generated method stub

		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}

	}

	public void updateVoiceLevel(int level) {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing() && isStatusRecording) {
			//通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
			int resId = mContext.getResources().getIdentifier("ic_recorder_volume_level_v" + level,
					"drawable", mContext.getPackageName());
			recorderImg.setImageResource(resId);
		}

	}

}
