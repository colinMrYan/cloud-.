package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

public class DisplayRecallMsg {
    public static View getView(final Context context, UIMessage uiMessage) {
        boolean isMyMsg = uiMessage.getMessage().getFromUser().equals(
                MyApplication.getInstance().getUid());
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_unknown_view, null);
        TextView unknownText = cardContentView.findViewById(R.id.channel_unknown_text);
        String username = isMyMsg ? context.getString(R.string.you) : '"' + uiMessage.getSenderName() + '"';
        unknownText.setText(context.getString(R.string.chat_info_message_status_recall, username));
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        return cardContentView;
    }
}
