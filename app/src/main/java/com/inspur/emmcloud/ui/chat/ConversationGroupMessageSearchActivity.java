package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.GroupMessageSearchAdapter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2019/3/4.
 * 搜索群消息页面
 */
public class ConversationGroupMessageSearchActivity extends BaseActivity {
    @BindView(R.id.ev_message_search)
    EditText messageSearchEditText;
    @BindView(R.id.recycler_view_group_message_search)
    RecyclerView groupMessageSearchRecylerView;
    private List<Message> searchResultList = new ArrayList<>();
    private GroupMessageSearchAdapter groupMessageSearchAdapter;
    private String cid;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_group_message_search;
    }

    private void initViews() {
        this.cid = getIntent().getStringExtra(ConversationActivity.EXTRA_CID);
        final List<Message> messageList = MessageCacheUtil.getGroupMessageWithType(this, cid);
        final List<String> messageContentList = getMessageContentList(messageList);
        groupMessageSearchAdapter = new GroupMessageSearchAdapter(ConversationGroupMessageSearchActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        groupMessageSearchRecylerView.setLayoutManager(layoutManager);
        groupMessageSearchRecylerView.setAdapter(groupMessageSearchAdapter);
        groupMessageSearchAdapter.setGroupMessageSearchListener(new GroupMessageSearchAdapter.GroupMessageSearchListener() {
            @Override
            public void onItemClick(UIMessage uiMessage) {
                Bundle bundle = new Bundle();
                bundle.putString(ConversationActivity.EXTRA_CID, cid);
                bundle.putSerializable(ConversationActivity.EXTRA_UIMESSAGE, uiMessage);
                dismissSoftKeyboard();
                IntentUtils.startActivity(ConversationGroupMessageSearchActivity.this, ConversationActivity.class, bundle, true);
            }
        });
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
                String keyWords = s.toString();
                for (int i = 0; i < messageContentList.size(); i++) {
                    if (!StringUtils.isBlank(keyWords) && messageContentList.get(i).contains(keyWords)) {
                        searchResultList.add(messageList.get(i));
                    }
                }
                groupMessageSearchAdapter.setAndRefreshAdapter(searchResultList, keyWords);
            }
        });
    }

    private void dismissSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageSearchEditText.getWindowToken(), 0);
    }

    /**
     * 中间转化步骤便于搜索，防止搜索数字搜出@的人
     *
     * @param messageList
     * @return
     */
    private List<String> getMessageContentList(List<Message> messageList) {
        List<String> messageContentList = new ArrayList<>();
        for (Message message : messageList) {
            String type = message.getType();
            switch (type) {
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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_group_search_messages:
                dismissSoftKeyboard();
                finish();
                break;
        }
    }
}
