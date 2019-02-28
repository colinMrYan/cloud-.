package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

public class DisplayResUnknownMsg {
    public static View getView(final Context context, boolean isMyMsg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_unknown_view, null);
        TextView unknownText = (TextView) cardContentView.findViewById(R.id.channel_unknown_text);
        unknownText.setText(context.getString(R.string.channel_unknown_text, AppUtils.getAppName(context)));
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeUtils upgradeUtils = new UpgradeUtils(context,
                        null, true);
                upgradeUtils.checkUpdate(true);
            }
        });
        return cardContentView;
    }
}
