package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;

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
        final TextView commentContentText = (TextView) convertView
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
//		JSONArray mentionArray = JSONUtils.getJSONArray(msgBody, "mentions",
//				new JSONArray());
//		JSONArray urlArray = JSONUtils.getJSONArray(msgBody, "urlList",
//				new JSONArray());
        // TextView设置此属性会让点击事件不响应，所有当没有@ url时不进行设置
        commentContentText.setMovementMethod(LinkMovementMethod
                .getInstance());
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

        RelativeLayout rootLayout = (RelativeLayout) convertView
                .findViewById(R.id.root_layout);
        rootLayout.setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.header_bg : R.color.white));
        commentContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        commentTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        int normalPadding = DensityUtil.dip2px(context, 10);
        int arrowPadding = DensityUtil.dip2px(context, 8);
        if (isMyMsg) {
            rootLayout.setPadding(normalPadding, normalPadding, normalPadding
                    + arrowPadding, normalPadding);
        } else {
            rootLayout.setPadding(normalPadding + arrowPadding, normalPadding,
                    normalPadding, normalPadding);
        }
        commentContentText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setPrimaryClip(ClipData.newPlainText(null, commentContentText.getText()));
                ToastUtils.show(context,R.string.copyed_to_paste_board);
                return true;
            }
        });
        commentContentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("cid", msg.getCid());
                bundle.putString("mid", msg.getCommentMid());
                IntentUtils.startActivity(context,
                        ChannelMsgDetailActivity.class, bundle);
            }
        });

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

    /**
     *
     *
     * @param context
     * @param msg
     */
    protected static void goDetail(Activity context, Msg msg) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("mid", msg.getCommentMid());
        IntentUtils.startActivity(context, ChannelMsgDetailActivity.class,
                bundle);
    }

}
