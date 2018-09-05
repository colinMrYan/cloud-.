package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.cpiz.android.bubbleview.BubbleRelativeLayout;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.privates.UpgradeUtils;

public class DisplayResUnknownMsg {
	public static View getView(final Context context,boolean isMyMsg){
		View cardContentView = LayoutInflater.from(context).inflate(
				R.layout.chat_msg_card_child_res_unknown_view, null);
		BubbleRelativeLayout cardLayout = (BubbleRelativeLayout)cardContentView.findViewById(R.id.brl_card);
		cardLayout.setArrowDirection(isMyMsg? BubbleStyle.ArrowDirection.Right:BubbleStyle.ArrowDirection.Left);
		cardContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UpgradeUtils upgradeUtils = new UpgradeUtils(context,
						null,true);
				upgradeUtils.checkUpdate(true);
			}
		});
		return cardContentView;
	}
}
