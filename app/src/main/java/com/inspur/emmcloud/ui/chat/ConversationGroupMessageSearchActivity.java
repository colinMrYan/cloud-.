package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/3/4.
 * 搜索群消息页面
 */
@ContentView(R.layout.activity_conversation_group_message_search)
public class ConversationGroupMessageSearchActivity extends BaseActivity{
    @ViewInject(R.id.ev_message_search)
    private EditText messageSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        final List<Message> messageList = MessageCacheUtil.getGroupMessageByKeyWords(this,getIntent().getStringExtra("cid"));
        final List<String> messageContentList = getMessageContentList(messageList);
        final List<Message> searchResultList = new ArrayList<>();
        messageSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchResultList.clear();
                for (int i = 0; i < messageContentList.size(); i++) {
                    if(messageContentList.get(i).contains(s.toString())){
                        searchResultList.add(messageList.get(i));
                    }
                }
                LogUtils.YfcDebug("搜索到的信息数量："+searchResultList.size());
            }
        });
    }

    private List<String> getMessageContentList(List<Message> messageList) {
        List<String> messageContentList = new ArrayList<>();
        for(Message message:messageList){
            String type = message.getType();
            switch (type){
                case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(ConversationGroupMessageSearchActivity.this,
                            message.getMsgContentComment().getText(), message.getMsgContentComment().getMentionsMap()).toString());
                    break;
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(ConversationGroupMessageSearchActivity.this,
                            message.getMsgContentTextPlain().getText(), message.getMsgContentTextPlain().getMentionsMap()).toString());
                    break;
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(ConversationGroupMessageSearchActivity.this,
                            message.getMsgContentTextMarkdown().getText(), message.getMsgContentTextMarkdown().getMentionsMap()).toString());
                    break;
            }
        }
        return messageContentList;
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_group_search_messages:

                break;
        }
    }
}
