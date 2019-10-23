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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.GroupMessageSearchAdapter;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationWithMessageNum;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
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
    @BindView(R.id.rl_channel_sub_title)
    RelativeLayout channelSubRelativeLayout;

    private List<com.inspur.emmcloud.bean.chat.Message> searchMessagesList = new ArrayList<>(); // 群组搜索结果
    private ConversationWithMessageNum conversationFromChatContent;
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
        ImmersionBar.with(this).statusBarColor(R.color.search_contact_header_bg).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();
        if (getIntent().hasExtra(SEARCH_ALL_FROM_CHAT)) {                      //直接傳 ConversationWithMessageNum
            conversationFromChatContent = (ConversationWithMessageNum) getIntent().getSerializableExtra(SEARCH_ALL_FROM_CHAT);
            if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_GROUP)) {
                displayImg(conversationFromChatContent.getConversation().conversation2SearchModel(), searchModelHeadImage);
                String showData = getString(R.string.chat_search_related_messages, "“" + conversationFromChatContent.getConversation().getName() + "”");
                searchModelNameText.setText(showData); //Record(s)  分別處理群組和個人
                staticNameText.setText(conversationFromChatContent.getConversation().getName());
            } else if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_DIRECT)) {
                if (conversationFromChatContent.getSingleChatContactUser() != null) {
                    Contact contact = conversationFromChatContent.getSingleChatContactUser();
                    SearchModel searchModel = contact.contact2SearchModel();
                    displayImg(searchModel, searchModelHeadImage);
                    String showData = getString(R.string.chat_search_related_messages, "“" + conversationFromChatContent.getSingleChatContactUser().getName() + "”");
                    searchModelNameText.setText(showData);
                    staticNameText.setText(searchModel.getName());
                }
            } else if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_CAST)) {
                UIConversation uiConversation = new UIConversation(conversationFromChatContent.getConversation());
                staticNameText.setText(uiConversation.getTitle());
                ImageDisplayUtils.getInstance().displayImage(searchModelHeadImage, uiConversation.getIcon(), R.drawable.icon_person_default);
                String showData = getString(R.string.chat_search_related_messages, "“" + uiConversation.getTitle() + "”");
                searchModelNameText.setText(showData);
            }
        } else if (getIntent().hasExtra(ConversationGroupInfoActivity.EXTRA_CID)) {     //只传ID 的时候
            channelSubRelativeLayout.setVisibility(View.GONE);
            String cid = getIntent().getStringExtra(ConversationGroupInfoActivity.EXTRA_CID);
            Conversation conversation = ConversationCacheUtils.getConversation(this, cid);
            conversationFromChatContent = new ConversationWithMessageNum(conversation, 0);

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
                        bundle.putSerializable(ConversationActivity.EXTRA_POSITION_MESSAGE, uiMessage);
                        dismissSoftKeyboard();
                        IntentUtils.startActivity(CommunicationSearchMessagesActivity.this, ConversationActivity.class, bundle, true);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(ConversationActivity.EXTRA_CID, conversationFromChatContent.getConversation().getId());
                        bundle.putSerializable(ConversationActivity.EXTRA_POSITION_MESSAGE, uiMessage);
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
                if (StringUtils.isBlank(keyWords)) {
                    searchMessagesList = new ArrayList<>();
                    if (getIntent().hasExtra(SEARCH_ALL_FROM_CHAT)) {
                        finish();
                    }
                } else {
                    searchMessagesList = MessageCacheUtil.getMessageListByKeywordAndId(CommunicationSearchMessagesActivity.this, keyWords, conversationFromChatContent.getConversation().getId());
                }
                groupMessageSearchAdapter.setAndRefreshAdapter(searchMessagesList, keyWords);
            }
        });
        if (getIntent().hasExtra(SEARCH_CONTENT)) {
            searchText = getIntent().getStringExtra(SEARCH_CONTENT);
            searchEdit.setText(searchText);
            searchEdit.setSelection(searchText.length());
        }
    }

    @Override
    protected int getStatusType() {
        return STATUS_NO_SET;
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
