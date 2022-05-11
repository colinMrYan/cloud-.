package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.TextViewWithSpan;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
public class DisplayServiceCommentTextPlainMsg {

    /**
     * 评论卡片
     *
     * @param context
     * @param message
     * @return
     */
    public static View getView(final Activity context, final Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_service_child_text_comment_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        final TextViewWithSpan commentContentText = (TextViewWithSpan) cardContentView
                .findViewById(R.id.comment_text);
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        TextView commentTitleText = (TextView) cardContentView
                .findViewById(R.id.comment_title_text);
        View dividerView = (View) cardContentView.findViewById(R.id.divider_view);
        MsgContentComment msgContentComment = message.getMsgContentComment();
        String text = msgContentComment.getText();
        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        commentTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        dividerView.setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.white : ResourceUtils.getResValueOfAttr(context, R.attr.text_color_e1)));
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text, message.getMsgContentTextPlain().getMentionsMap());
        Spannable span = EmotionUtil.getInstance(context).getSmiledText(spannableString, commentTitleText.getTextSize());
        commentContentText.setText(span);
        TransHtmlToTextUtils.stripUnderlines(
                commentContentText, context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg_blue));
        Message commentedMessage = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), msgContentComment.getMessage());
        if (commentedMessage != null) {
            commentTitleText.setText(getCommentTitle(context, commentedMessage, isMyMsg));
        } else {
            WSAPIService.getInstance().getMessageById(msgContentComment.getMessage());
        }
        return cardContentView;
    }


    private static SpannableStringBuilder getCommentTitle(Context context, Message commentedMessage, boolean isMyMsg) {
        String commentMsgSenderName = ContactUserCacheUtils.getUserName(commentedMessage.getFromUser());
        String commentTitle = commentMsgSenderName + ": ";
        String commentedMessageType = commentedMessage.getType();
        if (commentedMessageType.equals("file/regular-file")) {
            commentTitle = commentTitle + context.getString(R.string.comment_filetype);
        } else if (commentedMessageType.equals("media/image")) {
            commentTitle = commentTitle + context.getString(R.string.comment_type);
        } else {
            String commentMsg = commentedMessage.getShowContent();
            if (!commentMsg.isEmpty()) {
                commentTitle = commentTitle + commentMsg;
            } else {
                commentTitle = commentTitle + context.getString(R.string.comment_other_type);
            }
        }
        commentTitle = "「" + commentTitle + "」";
        SpannableStringBuilder builder = new SpannableStringBuilder(commentTitle);
        return builder;
    }
}
