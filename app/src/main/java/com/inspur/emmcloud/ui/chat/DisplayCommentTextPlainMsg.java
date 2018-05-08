package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;
import com.inspur.emmcloud.widget.TextViewWithSpan;

/**
 * DisplayTxtCommentMsg
 *
 * @author Fortune Yu 展示评论卡片 2016-08-19
 */
public class DisplayCommentTextPlainMsg {

    /**
     * 评论卡片
     * @param context
     * @param childView
     * @param msg
     * @param apiService
     */
    public static View getView(final Activity context, final Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_comment_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        final TextViewWithSpan commentContentText = (TextViewWithSpan) cardContentView
                .findViewById(R.id.comment_text);
        TextView commentTitleText = (TextView) cardContentView
                .findViewById(R.id.comment_title_text);
        MsgContentComment msgContentComment = message.getMsgContentComment();
        String text = msgContentComment.getText();
        commentContentText.setMovementMethod(LinkMovementClickMethod.getInstance());
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(context, text, message.getMsgContentTextPlain().getMentionsMap());
        commentContentText.setText(spannableString.toString());
        TransHtmlToTextUtils.stripUnderlines(
                commentContentText,context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg));

        Message commentedMessage = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(),msgContentComment.getMessage());
        if (commentedMessage != null){
            commentTitleText.setText(getCommentTitle(context,commentedMessage,isMyMsg));
        }else {
            WSAPIService.getInstance().getMessageById(msgContentComment.getMessage());
        }
        (cardContentView
                .findViewById(R.id.root_layout)).setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);

        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        commentTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        return cardContentView;
    }


    private static SpannableStringBuilder getCommentTitle(Context context,Message commentedMessage,boolean isMyMsg){
        String commentMsgSenderName = ContactCacheUtils.getUserName(context,commentedMessage.getFromUser());
        String commentedMessageTime = TimeUtils.getDisplayTime(context,commentedMessage.getCreationDate());
        String commentTitle = context
                .getString(R.string.comment_hallcomment_text)
                + " "
                + commentMsgSenderName
                + " "
                + context.getString(R.string.published_in)
                + " "
                + commentedMessageTime
                + " ";
        String commentedMessageType = commentedMessage.getType();
        if (commentedMessageType.equals("file/regular-file")){
            commentTitle = commentTitle
                    + context.getString(R.string.comment_filetype);
        }else if(commentedMessageType.equals("media/image")){
            commentTitle = commentTitle
                    + context.getString(R.string.comment_type);
        }else {
            commentTitle = commentTitle
                    + context.getString(R.string.comment_other_type);
        }

        int fstart = commentTitle.indexOf(" " + commentMsgSenderName + " ");
        int fend = fstart + (" " + commentMsgSenderName + " ").length();
        SpannableStringBuilder builder = new SpannableStringBuilder(commentTitle);
        builder.setSpan(
                new ForegroundColorSpan(context.getResources().getColor(
                        isMyMsg ? R.color.hightlight_in_blue_bg
                                : R.color.header_bg)), fstart, fend,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return builder;
    }



}
