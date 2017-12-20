package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.db.MsgCacheUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.TextViewWithSpan;

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
     * @param convertView
     * @param msg
     * @param apiService
     */
    public static void displayCommentMsg(final Activity context,
                                         View convertView, final Msg msg, ChatAPIService apiService) {
        String msgBody = msg.getBody();
        boolean isMyMsg = msg.getUid().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        final TextViewWithSpan commentContentText = (TextViewWithSpan) convertView
                .findViewById(R.id.comment_text);
        TextView commentTitleText = (TextView) convertView
                .findViewById(R.id.comment_title_text);

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

        (convertView
                .findViewById(R.id.root_layout)).setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.bg_my_card : R.color.white));

        (convertView
                .findViewById(R.id.card_layout)).setBackgroundResource(isMyMsg?R.drawable.ic_chat_msg_img_cover_arrow_right:R.drawable.ic_chat_msg_img_cover_arrow_left);;

        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        commentTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
//        commentContentText.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
//                cmb.setPrimaryClip(ClipData.newPlainText(null, commentContentText.getText()));
//                ToastUtils.show(context,R.string.copyed_to_paste_board);
//                return true;
//            }
//        });
//        commentContentText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Bundle bundle = new Bundle();
//                bundle.putString("cid", msg.getCid());
//                bundle.putString("mid", msg.getCommentMid());
//                IntentUtils.startActivity(context,
//                        ChannelMsgDetailActivity.class, bundle);
//            }
//        });

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
