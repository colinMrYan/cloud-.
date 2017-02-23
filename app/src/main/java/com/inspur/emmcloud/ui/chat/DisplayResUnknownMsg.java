package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.DensityUtil;

public class DisplayResUnknownMsg {
	public static void displayResUnknownMsg(Context context,View contentView,Msg msg){
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication)context.getApplicationContext()).getUid());
		
		int arrowPadding = DensityUtil.dip2px(context, 7);
		if (isMyMsg) {
			((RelativeLayout) contentView.findViewById(R.id.root_layout)).setPadding(0, 0, arrowPadding, 0);
		} else {
			((RelativeLayout) contentView.findViewById(R.id.root_layout)).setPadding(arrowPadding, 0,
					0, 0);
		}
	}
}
