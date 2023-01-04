package com.inspur.emmcloud.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.MultiMessageItem;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONArray;

import java.util.ArrayList;

public class Design3DisplayMultiMsg {

    public static View getView(final Context context, final UIMessage uiMessage) {
        final Message message = uiMessage.getMessage();
        @SuppressLint("InflateParams") final View cardContentView = LayoutInflater.from(context).inflate(R.layout.design3_chat_msg_card_child_multi_message_view, null);
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_ne15)));
        cardLayout.setStrokeWidth(0);
        TextView contentText = cardContentView.findViewById(R.id.tv_multi_message_content);
        StringBuilder sb = new StringBuilder();
        ArrayList<MultiMessageItem> arrayList = MultiMessageTransmitUtil.getListFromJsonStr(message.getContent());
        // 群成员有昵称时，显示昵称，否则显示通讯录名称
        String channelID = message.getChannel();
        Conversation conversation = ConversationCacheUtils.getConversation(context, channelID);
        String membersDetail = conversation.getMembersDetail();
        for (int i = 0; i < arrayList.size(); i++) {
            MultiMessageItem item = arrayList.get(i);
            String sendUserNicknameOrName = ChatMsgContentUtils.getUserNicknameOrName(JSONUtils.getJSONArray(membersDetail, new JSONArray()), item.sendUserId);
            switch (item.type) {
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    sb.append(item.sendUserName).append(": ").append(item.text);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    sb.append(sendUserNicknameOrName).append(": [").append(context.getResources().getString(R.string.picture)).append("]");
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    sb.append(sendUserNicknameOrName).append(": [").append(context.getResources().getString(R.string.file)).append("]");
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    sb.append(sendUserNicknameOrName).append(": [").append(context.getResources().getString(R.string.mession_link)).append("]");
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                    sb.append(sendUserNicknameOrName).append(": [").append(context.getResources().getString(R.string.video)).append("]");
                    break;
                default:
                    break;
            }
            if (i < arrayList.size() - 1) {
                sb.append("\n");
            }
        }
        contentText.setText(sb.toString());
        return cardContentView;
    }
}
