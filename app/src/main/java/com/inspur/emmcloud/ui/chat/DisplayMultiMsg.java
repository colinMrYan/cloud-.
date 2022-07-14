package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.MultiMessageItem;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;

import java.util.ArrayList;

public class DisplayMultiMsg {

    public static View getView(final Context context, final UIMessage uiMessage) {
        final Message message = uiMessage.getMessage();
        final View cardContentView = LayoutInflater.from(context).inflate(R.layout.chat_msg_card_child_multi_message_view, null);
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        TextView titleText = cardContentView.findViewById(R.id.tv_multi_message_title);
        titleText.setTextColor(DarkUtil.getTextColor());
        TextView contentText = cardContentView.findViewById(R.id.tv_multi_message_content);
        View splitLine = cardContentView.findViewById(R.id.split_line);
        splitLine.setBackgroundColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#404040" : "#e5e5e5"));
        StringBuilder sb = new StringBuilder();
        ArrayList<MultiMessageItem> arrayList = MultiMessageTransmitUtil.getListFromJsonStr(message.getContent());
        for (MultiMessageItem item : arrayList) {
            switch (item.type) {
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    sb.append(item.sendUserName)
                            .append(":")
                            .append(item.text)
                            .append("\n");
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    sb.append(item.sendUserName)
                            .append(":[")
                            .append(context.getResources().getString(R.string.picture))
                            .append("]\n");
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    sb.append(item.sendUserName)
                            .append(":[")
                            .append(context.getResources().getString(R.string.file))
                            .append("]\n");
                    break;
            }
        }
        contentText.setText(sb.toString());
//            titleText.setText("聊天记录");
        return cardContentView;
    }
}
