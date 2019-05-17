package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/3/5.
 */

public class GroupMessageSearchAdapter extends RecyclerView.Adapter<GroupMessageSearchAdapter.GroupMessageHolder> {


    private Context context;
    private List<UIMessage> groupUIMessageList = new ArrayList<>();
    private GroupMessageSearchListener listener;
    private String keyWords = "";
    private String keyWordsColor = "#36A5F6";

    public GroupMessageSearchAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GroupMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_message_search, parent, false);
        return new GroupMessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageHolder holder, int position) {
        final UIMessage uiMessage = groupUIMessageList.get(position);
        ImageDisplayUtils.getInstance().displayImage(holder.headImg, uiMessage.getSenderPhotoUrl(), R.drawable.icon_person_default);
        holder.groupMessageUserNameText.setText(groupUIMessageList.get(position).getSenderName());
        holder.groupMessageContentText.setText(getContent(uiMessage.getMessage()));
        String messageSendTime = TimeUtils.getChannelMsgDisplayTime(context, uiMessage.getCreationDate());
        holder.groupMessageTimeText.setText(messageSendTime);
        holder.groupMessageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(uiMessage);
            }
        });
    }

    private Spanned getContent(Message message) {
        String type = message.getType();
        Spanned text = null;
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                String textContent = ChatMsgContentUtils.mentionsAndUrl2Span(context,
                        message.getMsgContentTextPlain().getText(),
                        message.getMsgContentTextPlain().getMentionsMap()).toString();
                text = StringUtils.getHtmlString(textContent, keyWordsColor, keyWords);
                break;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                String markDownContent = ChatMsgContentUtils.mentionsAndUrl2Span(context,
                        message.getMsgContentTextMarkdown().getText(),
                        message.getMsgContentTextMarkdown().getMentionsMap()).toString();
                text = StringUtils.getHtmlString(markDownContent, keyWordsColor, keyWords);
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                String commentContent = ChatMsgContentUtils.mentionsAndUrl2Span(context,
                        message.getMsgContentComment().getText(),
                        message.getMsgContentComment().getMentionsMap()).toString();
                text = StringUtils.getHtmlString(commentContent, keyWordsColor, keyWords);
                break;
        }
        return text;
    }

    @Override
    public int getItemCount() {
        return groupUIMessageList.size();
    }

    public void setAndRefreshAdapter(List<Message> groupMessageList, String keyWords) {
        this.keyWords = keyWords;
        groupUIMessageList = UIMessage.MessageList2UIMessageList(groupMessageList);
        notifyDataSetChanged();
    }

    public void setGroupMessageSearchListener(GroupMessageSearchListener listener) {
        this.listener = listener;
    }

    public interface GroupMessageSearchListener {
        void onItemClick(UIMessage uiMessage);
    }

    public class GroupMessageHolder extends RecyclerView.ViewHolder {
        CircleTextImageView headImg;
        TextView groupMessageUserNameText;
        TextView groupMessageContentText;
        TextView groupMessageTimeText;
        RelativeLayout groupMessageLayout;

        public GroupMessageHolder(View itemView) {
            super(itemView);
            headImg = itemView.findViewById(R.id.iv_group_message_search_head);
            groupMessageUserNameText = itemView.findViewById(R.id.tv_group_message_search_name);
            groupMessageContentText = itemView.findViewById(R.id.tv_group_message_search_content);
            groupMessageTimeText = itemView.findViewById(R.id.tv_group_message_search_time);
            groupMessageLayout = itemView.findViewById(R.id.rl_group_message_search);
        }
    }
}
