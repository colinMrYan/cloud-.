package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.GroupMessageSearchAdapter;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/23.
 */

public class CommunicationSearchMessagesActivity extends BaseActivity {
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final String SEARCH_CONTENT = "search_content";

    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.rv_group_message_search)
    RecyclerView messagesDetailRecycleView;
    @BindView(R.id.iv_search_model_head)
    CircleTextImageView searchModelHeadImage;
    @BindView(R.id.tv_search_model_name)
    TextView searchModelNameText;
    @BindView(R.id.tv_static_name)
    TextView staticNameText;

    private List<com.inspur.emmcloud.bean.chat.Message> searchMessagesList = new ArrayList<>(); // 群组搜索结果
    private ConversationFromChatContent conversationFromChatContent;
    private GroupMessageSearchAdapter groupMessageSearchAdapter;
    private String searchText;
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchMessagesActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        if (getIntent().hasExtra(SEARCH_ALL_FROM_CHAT)) {
            conversationFromChatContent = (ConversationFromChatContent) getIntent().getSerializableExtra(SEARCH_ALL_FROM_CHAT);
            if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_GROUP)) {
                displayImg(conversationFromChatContent.getConversation().conversation2SearchModel(), searchModelHeadImage);
                searchModelNameText.setText("“" + conversationFromChatContent.getConversation().getName() + "”" + " 的记录"); //Record(s) for
                staticNameText.setText(conversationFromChatContent.getConversation().getName());
            } else {
                if (conversationFromChatContent.getSingleChatContactUser() != null) {
                    Contact contact = conversationFromChatContent.getSingleChatContactUser();
                    SearchModel searchModel = contact.contact2SearchModel();
                    displayImg(searchModel, searchModelHeadImage);
                    searchModelNameText.setText("“" + searchModel.getName() + "”" + " 的记录");
                    staticNameText.setText(searchModel.getName());
                }
            }
        }
        groupMessageSearchAdapter = new GroupMessageSearchAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messagesDetailRecycleView.setLayoutManager(layoutManager);
        messagesDetailRecycleView.setAdapter(groupMessageSearchAdapter);
        groupMessageSearchAdapter.setGroupMessageSearchListener(new GroupMessageSearchAdapter.GroupMessageSearchListener() {
            @Override
            public void onItemClick(UIMessage uiMessage) {
                if (conversationFromChatContent != null) {
                    if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_GROUP)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(ConversationActivity.EXTRA_CID, conversationFromChatContent.getConversation().getId());
                        bundle.putSerializable(ConversationActivity.EXTRA_UIMESSAGE, uiMessage);
                        bundle.putSerializable(ConversationActivity.EXTRA_FROM_SERCH, "1111");
                        dismissSoftKeyboard();
                        IntentUtils.startActivity(CommunicationSearchMessagesActivity.this, ConversationActivity.class, bundle, true);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(ConversationActivity.EXTRA_CID, conversationFromChatContent.getConversation().getId());
                        bundle.putSerializable(ConversationActivity.EXTRA_UIMESSAGE, uiMessage);
                        bundle.putSerializable(ConversationActivity.EXTRA_FROM_SERCH, "1111");
                        dismissSoftKeyboard();
                        IntentUtils.startActivity(CommunicationSearchMessagesActivity.this, ConversationActivity.class, bundle, true);
                    }
                }
            }
        });
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchMessagesList.clear();
                String keyWords = s.toString();
                searchMessagesList = MessageCacheUtil.getMessageListByContent(CommunicationSearchMessagesActivity.this, keyWords, conversationFromChatContent.getConversation().getId());
                groupMessageSearchAdapter.setAndRefreshAdapter(searchMessagesList, keyWords);
            }
        });
        if (getIntent().hasExtra(SEARCH_CONTENT)) {
            searchText = getIntent().getStringExtra(SEARCH_CONTENT);
            searchEdit.setText(searchText);
            searchEdit.setSelection(searchText.length());
        }
    }

    /**
     * 中间转化步骤便于搜索，防止搜索数字搜出@的人
     *
     * @param messageList
     * @return
     */
    private List<String> getMessageContentList(List<com.inspur.emmcloud.bean.chat.Message> messageList) {
        List<String> messageContentList = new ArrayList<>();
        for (com.inspur.emmcloud.bean.chat.Message message : messageList) {
            String type = message.getType();
            switch (type) {
                case com.inspur.emmcloud.bean.chat.Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(CommunicationSearchMessagesActivity.this,
                            message.getMsgContentComment().getText(), message.getMsgContentComment().getMentionsMap()).toString());
                    break;
                case com.inspur.emmcloud.bean.chat.Message.MESSAGE_TYPE_TEXT_PLAIN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(CommunicationSearchMessagesActivity.this,
                            message.getMsgContentTextPlain().getText(), message.getMsgContentTextPlain().getMentionsMap()).toString());
                    break;
                case com.inspur.emmcloud.bean.chat.Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    messageContentList.add(ChatMsgContentUtils.mentionsAndUrl2Span(CommunicationSearchMessagesActivity.this,
                            message.getMsgContentTextMarkdown().getText(), message.getMsgContentTextMarkdown().getMentionsMap()).toString());
                    break;
            }
        }
        return messageContentList;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    /**
     * 隐藏虚拟键盘
     */
    private void dismissSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_messages_activity;
    }

    /**
     * 统一显示图片
     *
     * @param searchModel
     * @param photoImg
     */
    private void displayImg(SearchModel searchModel, CircleTextImageView photoImg) {
        Integer defaultIcon = null; // 默认显示图标
        String icon = null;
        String type = searchModel.getType();
        if (type.equals(SearchModel.TYPE_GROUP)) {
            defaultIcon = R.drawable.icon_channel_group_default;
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + searchModel.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
                ImageDisplayUtils.getInstance().displayImageNoCache(photoImg, icon, defaultIcon);
                return;
            }
        } else if (type.equals(SearchModel.TYPE_STRUCT)) {
            defaultIcon = R.drawable.ic_contact_sub_struct;
        } else {
            defaultIcon = R.drawable.icon_person_default;
            if (!searchModel.getId().equals("null")) {
                icon = APIUri.getChannelImgUrl(MyApplication.getInstance(), searchModel.getId());
            }

        }
        ImageDisplayUtils.getInstance().displayImage(
                photoImg, icon, defaultIcon);

    }


}
