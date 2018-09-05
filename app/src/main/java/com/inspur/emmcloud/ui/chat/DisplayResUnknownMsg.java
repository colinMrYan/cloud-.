package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.UpgradeUtils;

public class DisplayResUnknownMsg {
	public static View getView(final Context context,boolean isMyMsg){
		View cardContentView = LayoutInflater.from(context).inflate(
				R.layout.chat_msg_card_child_res_unknown_view, null);
		int arrowPadding = DensityUtil.dip2px(context, 7);
		if (isMyMsg) {
			( cardContentView.findViewById(R.id.brl_card)).setPadding(0, 0, arrowPadding, 0);
		} else {
			(cardContentView.findViewById(R.id.brl_card)).setPadding(arrowPadding, 0,
					0, 0);
		}
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
