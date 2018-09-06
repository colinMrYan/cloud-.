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
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.TextViewWithSpan;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

import java.util.Arrays;
import java.util.List;

/**
 * DisplayTxtCommentMsg
 *
 * @author Fortune Yu 展示评论卡片 2016-08-19
 */
public class DisplayTxtCommentMsg {

    /**
     * 评论卡片
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
        cardLayout.setArrowDirection(isMyMsg? ArrowDirection.RIGHT:ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.white));
        cardLayout.setStrokeWidth(isMyMsg ?0: 0.5f);
        String commentContent = JSONUtils.getString(msgBody, "source", "");
        String[] mentions = JSONUtils.getString(msgBody, "mentions", "")
                .replace("[", "").replace("]", "").split(",");
        String[] urls = JSONUtils.getString(msgBody, "urlList", "")
                .replace("[", "").replace("]", "").split(",");
        List<String> mentionList = Arrays.asList(mentions);
        List<String> urlList = Arrays.asList(urls);

        if (StringUtils.isBlank(commentContent)) {
            commentContent = msg.getCommentContent();
        }
        // 为了兼容原来的评论类型的消息
        if (StringUtils.isBlank(commentContent)) {
            commentContent = JSONUtils.getString(msgBody, "content", "");
        }
        SpannableString spannableString = MentionsAndUrlShowUtils
                .handleMentioin(commentContent, mentionList, urlList);
        commentContentText.setText(spannableString);
        TransHtmlToTextUtils.stripUnderlines(
                commentContentText,
                context.getResources().getColor(
                        isMyMsg ? R.color.hightlight_in_blue_bg
                                : R.color.header_bg));
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
                                : R.color.header_bg)), fstart, fend,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        commentTitleText.setText(style);
    }


}
