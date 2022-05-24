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
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.TextViewWithSpan;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;

/**
 * DisplayTxtCommentMsg
 *
 * @author Fortune Yu 展示评论卡片 2016-08-19
 */
public class DisplayTxtCommentMsg {

    /**
     * 评论卡片
     *
     * @param context
     * @param childView
     * @param msg
     * @param apiService
     */
    public static View displayCommentMsg(final Activity context,
                                         final Msg msg, ChatAPIService apiService) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_comment_view, null);
        String msgBody = msg.getBody();
        boolean isMyMsg = msg.getUid().equals(MyApplication.getInstance().getUid());
        final TextViewWithSpan commentContentText = (TextViewWithSpan) cardContentView
                .findViewById(R.id.comment_text);
        TextView commentTitleText = (TextView) cardContentView
                .findViewById(R.id.comment_title_text);
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.bg_other_card));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        SpannableString spannableString = MentionsAndUrlShowUtils.getMsgContentSpannableString(msgBody);
        commentContentText.setText(spannableString);
        TransHtmlToTextUtils.stripUnderlines(
                commentContentText,
                context.getResources().getColor(
                        isMyMsg ? R.color.hightlight_in_blue_bg
                                : R.color.header_bg_blue));
        // 取出评论消息的id
        Msg commentedMsg = MsgCacheUtil.getCacheMsg(context,
                msg.getCommentMid());
        if (commentedMsg != null) {
            String msgType = commentedMsg.getType();
            showCommentDetail(context, commentedMsg, commentTitleText, msgType,
                    isMyMsg);
        } else if (NetUtils.isNetworkConnected(context)) {
            apiService.getMsg(msg.getCommentMid());
        }
        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        commentTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        return cardContentView;
    }

    /**
     * 评论消息的详情
     *
     * @param context
     * @param commentedMsg
     * @param commentTitleText
     * @param type
     * @param isMyMsg
     */
    private static void showCommentDetail(Context context, Msg commentedMsg,
                                          TextView commentTitleText, String type, boolean isMyMsg) {
        // TODO Auto-generated method stub
        String msgTime = TimeUtils.getDisplayTime(context,
                commentedMsg.getTime());
        String commentTitle = context
                .getString(R.string.comment_hallcomment_text)
                + " "
                + commentedMsg.getTitle()
                + " "
                + context.getString(R.string.published_in)
                + " "
                + msgTime
                + " ";
        if (type.equals("res_image")) {
            commentTitle = commentTitle
                    + context.getString(R.string.comment_type);
        } else if (type.equals("res_file")) {
            commentTitle = commentTitle
                    + context.getString(R.string.comment_filetype);
        } else if (type.equals("res_link")) {
            commentTitle = commentTitle
                    + context.getString(R.string.comment_news_type);
        }
        int fstart = commentTitle.indexOf(" " + commentedMsg.getTitle() + " ");
        int fend = fstart + (" " + commentedMsg.getTitle() + " ").length();
        SpannableStringBuilder style = new SpannableStringBuilder(commentTitle);
        style.setSpan(
                new ForegroundColorSpan(context.getResources().getColor(
                        isMyMsg ? R.color.hightlight_in_blue_bg
                                : R.color.header_bg_blue)), fstart, fend,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        commentTitleText.setText(style);
    }


}
