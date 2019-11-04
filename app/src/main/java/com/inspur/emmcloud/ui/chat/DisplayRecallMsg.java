package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.UIMessage;

public class DisplayRecallMsg {
    public static View getView(final Context context, UIMessage uiMessage) {
        String recallFromUid = uiMessage.getMessage().getRecallFromUid();
        boolean isMyMsg = recallFromUid.equals(MyApplication.getInstance().getUid());
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_recall_view, null);
        TextView recallMessageInfoText = cardContentView.findViewById(R.id.tv_recall_message_info);
        String username = isMyMsg ? context.getString(R.string.you) : '"' + uiMessage.getMessage().getRecallFromUserName() + '"';
        recallMessageInfoText.setText(context.getString(R.string.chat_info_message_status_recall, username));
        return cardContentView;
    }
}
