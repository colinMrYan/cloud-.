package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.util.common.DensityUtil;

public class DisplayResUnknownMsgRobot {
	public static View getView(Context context,  MsgRobot msg){
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.chat_msg_card_child_res_unknown_view, null);
		boolean isMyMsg = msg.getFromUser().equals(
				MyApplication.getInstance().getUid());
		
		int arrowPadding = DensityUtil.dip2px(context, 7);
		if (isMyMsg) {
			( contentView.findViewById(R.id.root_layout)).setPadding(0, 0, arrowPadding, 0);
		} else {
			(contentView.findViewById(R.id.root_layout)).setPadding(arrowPadding, 0,
					0, 0);
		}
		return contentView;
	}
}
