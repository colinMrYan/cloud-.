package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.TextViewFixTouchConsume;

import org.json.JSONArray;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class Design3DisplayTxtPlainMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param message
     * @param membersDetail @他人时，若群聊中有昵称，需显示他人的昵称
     */
    public static View getView(final Context context, Message message, JSONArray membersDetail) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.design3_chat_msg_card_child_text_plain_view, null);
        final boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(0);
        final TextView contentText = cardContentView
                .findViewById(R.id.tv_content);
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        String msgType = message.getMsgContentTextPlain().getMsgType();
        String text = message.getMsgContentTextPlain().getText();
        if (msgType.equals(Message.MESSAGE_TYPE_TEXT_BURN) && !isMyMsg) {
            text = context.getString(R.string.click_to_burn);
            contentText.setTextColor(context.getResources().getColor(R.color.color_base_blue));
        }
        contentText.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        contentText.setFocusable(false);
        contentText.setFocusableInTouchMode(false);
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text, message.getMsgContentTextPlain().getMentionsMap(), membersDetail);
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, contentText.getTextSize());
        contentText.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                contentText, context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg_blue));
        return cardContentView;
    }
}
