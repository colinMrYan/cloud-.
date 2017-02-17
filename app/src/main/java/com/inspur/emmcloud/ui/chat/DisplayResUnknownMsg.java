package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;

public class DisplayResUnknownMsg {
	public static void displayResUnknownMsg(Context context,View contentView,Msg msg){
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication)context.getApplicationContext()).getUid());
		((RelativeLayout) contentView.findViewById(R.id.root_layout))
		.setBackgroundResource(isMyMsg ? R.drawable.shape_chat_msg_card_my
				: R.drawable.shape_chat_msg_card_other);
		((TextView)contentView.findViewById(R.id.channel_unknown_text)).setTextColor(context.getResources().getColor(
				isMyMsg ? R.color.white : R.color.msg_content_color));
	}
}
