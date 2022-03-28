package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.TextViewFixTouchConsume;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

public class DisplayTextWhisperMsg {

    public static View getView(final Context context, Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_plain_view, null);
        final boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        final TextViewFixTouchConsume contentText = cardContentView
                .findViewById(R.id.tv_content);
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        String text = message.getMsgContentTextPlain().getText();
        contentText.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        contentText.setFocusable(false);
        contentText.setFocusableInTouchMode(false);
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text, message.getMsgContentTextPlain().getMentionsMap());
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, contentText.getTextSize());
        contentText.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                contentText, context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg_blue));
        return cardContentView;
    }
}
