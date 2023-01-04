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
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_th06)
                : ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_ne15)));
        cardLayout.setStrokeWidth(0);
        final TextView contentText = cardContentView
                .findViewById(R.id.tv_content);
        contentText.setTextColor(context.getResources().getColor(ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_te01)));
        String msgType = message.getMsgContentTextPlain().getMsgType();
        String text = message.getMsgContentTextPlain().getText();
        if (msgType.equals(Message.MESSAGE_TYPE_TEXT_BURN) && !isMyMsg) {
            text = context.getString(R.string.click_to_burn);
            contentText.setTextColor(context.getResources().getColor(R.color.color_te07));
        }
        // 文本为链接时，长按时链接为高亮，在暗黑时置为透明，解决长按复制时多层背景bug
        contentText.setHighlightColor(context.getResources().getColor(isMyMsg ? R.color.transparent
                : ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_ne15)));
        contentText.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        contentText.setFocusable(false);
        contentText.setFocusableInTouchMode(false);
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text, message.getMsgContentTextPlain().getMentionsMap(), membersDetail);
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, contentText.getTextSize());
        contentText.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                contentText, context.getResources().getColor(R.color.color_te07));
        return cardContentView;
    }
}
