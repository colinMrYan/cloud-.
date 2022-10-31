package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ConversationFromChatContentAdapter;
import com.inspur.emmcloud.adapter.GroupOrContactAdapter;
import com.inspur.emmcloud.adapter.PrivateChatAdapter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.ConversationWithMessageNum;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ShareUtil;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/9.
 */

public class SearchActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {

    public static final String SEARCH_ALL = "search_all";
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final String SEARCH_PRIVATE_CHAT = "search_private_chat";
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final String SEARCH_CONTENT = "search_content";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;
    private static final int REQUEST_CODE_SHARE = 5;

    @BindView(R.id.lv_search_contact)
    ListView searchContactListView;
    @BindView(R.id.lv_search_group)
    ListView searchGroupListView;
    @BindView(R.id.lv_search_private_chat)
    ListView searchPrivateChatListView;
    @BindView(R.id.lv_search_contact_from_chat)
    ListView searchContactFromChatListView;
    @BindView(R.id.rl_search_more_private_chat)
    RelativeLayout searchMorePrivateChatLayout;
    @BindView(R.id.rl_search_more_group)
    RelativeLayout searchMoreGroupLayout;
    @BindView(R.id.rl_search_more_contact_from_chat)
    RelativeLayout searchMoreContactFromChatLayout;
    @BindView(R.id.rl_search_more_contact)
    RelativeLayout searchMoreContentLayout;
    @BindView(R.id.ll_all_contact)
    LinearLayout allContactLayout;
    @BindView(R.id.ll_all_group)
    LinearLayout allGroupLayout;
    @BindView(R.id.ll_all_content)
    LinearLayout allContentLayout;
    @BindView(R.id.ll_all_private_chat)
    LinearLayout allPrivateChatLayout;
    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    private List<SearchModel> contactList = new ArrayList<>();
    private List<SearchModel> groupConversationList = new ArrayList<>();
    private List<Conversation> directConversationList = new ArrayList<>();
    private List<ConversationWithMessageNum> conversationFromChatContentList = new ArrayList<>();
    private List<SearchModel> contactResultList = new ArrayList<>();
    private List<SearchModel> groupConversationResultList = new ArrayList<>();
    private List<Conversation> directConversationResultList = new ArrayList<>();
    private List<ConversationWithMessageNum> conversationFromChatContentResultList = new ArrayList<>();
    private Runnable searchRunnable;
    private String searchArea = SEARCH_ALL;
    private Handler handler;
    private GroupOrContactAdapter groupAdapter;
    private GroupOrContactAdapter contactAdapter;
    private PrivateChatAdapter privateChatAdapter;
    private ConversationFromChatContentAdapter conversationFromChatContentAdapter;
    private String searchText;
    private long lastSearchTime = 0;
    private String shareContent;
    private boolean isSearchContacts = false;
    /**
     * 虚拟键盘
     */
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(SearchActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
//        ImmersionBar.with(this).statusBarColor(R.color.search_contact_header_bg).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();
//        initData();
        isSearchContacts = AppTabUtils.hasContactPermission(this);
        handMessage();
        initSearchRunnable();
        groupAdapter = new GroupOrContactAdapter(this);
        privateChatAdapter = new PrivateChatAdapter(this);
        contactAdapter = new GroupOrContactAdapter(this);
        conversationFromChatContentAdapter = new ConversationFromChatContentAdapter(this);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        searchContactListView.setAdapter(contactAdapter);
        searchGroupListView.setAdapter(groupAdapter);
        searchPrivateChatListView.setAdapter(privateChatAdapter);
        searchContactFromChatListView.setAdapter(conversationFromChatContentAdapter);
        searchContactListView.setOnItemClickListener(this);
        searchGroupListView.setOnItemClickListener(this);
        searchPrivateChatListView.setOnItemClickListener(this);
        searchContactFromChatListView.setOnItemClickListener(this);
        searchMoreContentLayout.setVisibility(View.GONE);
        searchMoreGroupLayout.setVisibility(View.GONE);
        searchMorePrivateChatLayout.setVisibility(View.GONE);
        searchMoreContactFromChatLayout.setVisibility(View.GONE);
        allContactLayout.setVisibility(View.GONE);
        allContentLayout.setVisibility(View.GONE);
        allGroupLayout.setVisibility(View.GONE);
        allPrivateChatLayout.setVisibility(View.GONE);
        InputMethodUtils.display(this, searchEdit);
        shareContent = (String) getIntent().getSerializableExtra(Constant.SHARE_CONTENT);
    }

//    /**
//     * 是否隐藏联系人
//     **/
//    private void initData() {
//        ArrayList<MainTabResult> mainTabResults = AppTabUtils.getMainTabResultList(getApplicationContext());
//        for (int i = 0; i < mainTabResults.size(); i++) {
//            if (mainTabResults.get(i).getUri().equals(Constant.APP_TAB_BAR_COMMUNACATE)) {
//                MainTabProperty mainTabProperty = mainTabResults.get(i).getMainTabProperty();
//                if (mainTabProperty != null && mainTabProperty.isCanContact() && AppRoleUtils.isShowContact()) {
//                    isSearchContacts = true;
//                }
//            }else if(mainTabResults.get(i).getUri().equals(Constant.APP_TAB_BAR_CONTACT)){
//                isSearchContacts = true;
//            }
//        }
//
//    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_group_contact_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    /**
     * 异步处理数据
     */
    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        if (groupConversationList == null) {
                            groupConversationList = new ArrayList<>();
                        }
                        if (contactList == null) {
                            contactList = new ArrayList<>();
                        }
                        if (directConversationList == null) {
                            directConversationList = new ArrayList<>();
                        }
                        if (conversationFromChatContentList == null) {
                            conversationFromChatContentList = new ArrayList<>();
                        }
                        contactResultList.clear();
                        contactResultList.addAll(contactList);
                        groupConversationResultList.clear();
                        groupConversationResultList.addAll(groupConversationList);
                        directConversationResultList.clear();
                        directConversationResultList.addAll(directConversationList);
                        conversationFromChatContentResultList.clear();
                        conversationFromChatContentResultList.addAll(conversationFromChatContentList);
                        searchMoreContentLayout.setVisibility(contactResultList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreGroupLayout.setVisibility(groupConversationResultList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMorePrivateChatLayout.setVisibility(directConversationResultList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreContactFromChatLayout.setVisibility(conversationFromChatContentResultList.size() > 2 ? View.VISIBLE : View.GONE);
                        allGroupLayout.setVisibility(groupConversationResultList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContactLayout.setVisibility(contactResultList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContentLayout.setVisibility(conversationFromChatContentResultList.size() > 0 ? View.VISIBLE : View.GONE);
                        allPrivateChatLayout.setVisibility(directConversationResultList.size() > 0 ? View.VISIBLE : View.GONE);
                        groupAdapter.setContentList(groupConversationResultList); //搜索群组
                        groupAdapter.notifyDataSetChanged();
                        privateChatAdapter.setConversationList(directConversationResultList); //私聊搜索
                        privateChatAdapter.notifyDataSetChanged();
                        contactAdapter.setContentList(contactResultList); //搜索联系人刷新
                        contactAdapter.notifyDataSetChanged();
                        conversationFromChatContentAdapter.setConversationList(conversationFromChatContentResultList);//频道内消息刷新
                        conversationFromChatContentAdapter.notifyDataSetChanged();
                        break;
                    case CLEAR_DATA:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.rl_search_more_private_chat:
                intent.setClass(this, CommunicationSearchModelMoreActivity.class);
                intent.putExtra("search_type", SEARCH_PRIVATE_CHAT);
                intent.putExtra("search_content", searchText);
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                break;
            case R.id.rl_search_more_group:
                intent.setClass(this, CommunicationSearchModelMoreActivity.class);
                intent.putExtra("search_type", SEARCH_GROUP);
                intent.putExtra("search_content", searchText);
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                //跳转到群组列表页面
                break;
            case R.id.rl_search_more_contact:
                intent.setClass(this, CommunicationSearchModelMoreActivity.class);
                intent.putExtra("search_type", SEARCH_CONTACT);
                intent.putExtra("search_content", searchText);
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                //跳转到查找人列表
                break;
            case R.id.rl_search_more_contact_from_chat:
                intent.setClass(this, CommunicationSearchModelMoreActivity.class);
                intent.putExtra("search_type", SEARCH_ALL_FROM_CHAT);
                intent.putExtra("search_content", searchText);
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!StringUtils.isBlank(shareContent)) {   //拦截分享
            handleShare(adapterView, i);
            return;
        }
        Bundle bundle = new Bundle();
        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                if (i < groupConversationResultList.size()) {
                    openChannel(groupConversationResultList.get(i));
                }
                break;
            case R.id.lv_search_contact:
                if (i < contactResultList.size()) {
                    bundle.putString("uid", contactResultList.get(i).getId());
                    IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
                }
                break;
            case R.id.lv_search_contact_from_chat:
                if (i < conversationFromChatContentResultList.size()) {
                    Intent intent = new Intent(SearchActivity.this, CommunicationSearchMessagesActivity.class);
                    intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentResultList.get(i));
                    intent.putExtra(SEARCH_CONTENT, searchText);
                    startActivity(intent);
                }
                break;
            case R.id.lv_search_private_chat:
                if (i < directConversationResultList.size()) {
                    bundle.putString(ConversationActivity.EXTRA_CID, directConversationResultList.get(i).getId());
                    IntentUtils.startActivity(this, ConversationActivity.class, bundle, true);
                }
                break;
            default:
                break;
        }
    }

    private void openChannel(SearchModel searchModel) {
        switch (searchModel.getType()) {
            case SearchModel.TYPE_DIRECT:
            case SearchModel.TYPE_GROUP:
                startChannelActivity(searchModel.getId());
                break;
            case SearchModel.TYPE_USER:
                createDirectChannel(searchModel.getId());
                break;
            case SearchModel.TYPE_TRANSFER:
                Bundle bundle = new Bundle();
                bundle.putString(ConversationActivity.EXTRA_CID, searchModel.getId());
                IntentUtils.startActivity(this, ConversationActivity.class, bundle, true);
                break;
        }
    }


    private void handleShare(AdapterView<?> adapterView, int position) {
        SearchModel searchModel;
        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                searchModel = groupConversationResultList.get(position);
                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_contact:
                searchModel = contactResultList.get(position);
                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_private_chat:
                searchModel = directConversationList.get(position).conversation2SearchModel();
                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_contact_from_chat:
                ConversationWithMessageNum conversationFromChatContent = conversationFromChatContentResultList.get(position);
                final Conversation conversation = conversationFromChatContent.getConversation();
                searchModel = conversation.conversation2SearchModel();
                ShareUtil.share(this, searchModel, shareContent);
                break;
            default:
                break;
        }
    }

    /**
     * 单人聊天  群组聊天
     *
     * @param searchModel
     */
    private void handleSearchModelShare(final SearchModel searchModel) {
        ShareUtil.share(this, searchModel, shareContent);
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(SearchActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(SearchActivity.this, uid,
                    new OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(SearchActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }

    /**
     * 初始化异步方法
     */
    private void initSearchRunnable() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch (searchArea) {
                            case SEARCH_ALL:
                                /***搜索群组***/
                                List<SearchModel> groupsSearchList;
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils.getSearchChannelGroupSearchModelList(MyApplication.getInstance(), searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                groupConversationList = new ArrayList<>();
                                for (int i = 0; i < (groupsSearchList.size() > 3 ? 3 : groupsSearchList.size()); i++) {
                                    groupConversationList.add(groupsSearchList.get(i));
                                }
                                /***搜索私聊***/
                                List<Conversation> privateChatSearchList = ConversationCacheUtils.getSearchConversationPrivateChatSearchModelList(MyApplication.getInstance(), searchText);
                                directConversationList = new ArrayList<>();
                                for (int i = 0; i < (privateChatSearchList.size() > 3 ? 3 : privateChatSearchList.size()); i++) {
                                    directConversationList.add(privateChatSearchList.get(i));
                                }
                                /***搜索联系人***/
                                List<Contact> contactsSearchList = new ArrayList<>(); //联系人
                                if (isSearchContacts) {
                                    contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                }
                                contactList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                /***通过聊天内容搜索***/
                                List<ConversationWithMessageNum> conversationFromChatContentNums = oriChannelInfoByKeyword(searchText);
                                conversationFromChatContentList = new ArrayList<>();
                                for (int m = 0; m < conversationFromChatContentNums.size(); m++) {
                                    conversationFromChatContentList.add(conversationFromChatContentNums.get(m));
                                }
                                //分享过来  去除系统通知
                                if (!StringUtils.isBlank(shareContent)) {
                                    Iterator<ConversationWithMessageNum> iterator = conversationFromChatContentList.iterator();
                                    while (iterator.hasNext()) {
                                        ConversationWithMessageNum fromChatContent = iterator.next();
                                        String input = fromChatContent.getConversation().getInput();
                                        int inputNum = 0;
                                        if (!StringUtils.isBlank(input)) {
                                            inputNum = Integer.parseInt(input);
                                        }
                                        if (fromChatContent.getConversation().getType().equals(Conversation.TYPE_CAST) && inputNum == 0) {
                                            iterator.remove();
                                        }
                                    }
                                    Iterator<Conversation> iteratorPrivateChat = directConversationList.iterator();
                                    while (iteratorPrivateChat.hasNext()) {
                                        Conversation tempConversation = iteratorPrivateChat.next();
                                        String input = tempConversation.getInput();
                                        int inputNum = 0;
                                        if (!StringUtils.isBlank(input)) {
                                            inputNum = Integer.parseInt(input);
                                        }
                                        if (tempConversation.getType().equals(Conversation.TYPE_CAST) && inputNum == 0) {
                                            iteratorPrivateChat.remove();
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        if (handler != null && !StringUtils.isBlank(searchText)) {
                            handler.sendEmptyMessage(REFRESH_DATA);
                        }
                    }
                }).start();
            }
        };
    }

    /**
     * 通过关键字获取包含该关键字的频道消息等信息
     */
    private List<ConversationWithMessageNum> oriChannelInfoByKeyword(String searchData) {
        Map<String, Integer> cidNumMap = new HashMap<>();
        cidNumMap.clear();
        List<com.inspur.emmcloud.bean.chat.Message> allMessageListByKeyword = new ArrayList<>();
        allMessageListByKeyword.clear();
        List<ConversationWithMessageNum> conversationFromChatContentResultList = new ArrayList<>();
        conversationFromChatContentResultList.clear();
        List<String> conversationIdList = new ArrayList<>();
        allMessageListByKeyword = MessageCacheUtil.getMessagesListByKeyword(MyApplication.getInstance(), searchData);
        if (allMessageListByKeyword != null) {
            for (int i = 0; i < allMessageListByKeyword.size(); i++) {
                String currentMessageConversation = allMessageListByKeyword.get(i).getChannel();
                if (cidNumMap != null && cidNumMap.containsKey(currentMessageConversation)) {
                    int num = cidNumMap.get(currentMessageConversation);
                    num = num + 1;
                    cidNumMap.put(currentMessageConversation, num);
                } else {
                    cidNumMap.put(currentMessageConversation, 1);
                    conversationIdList.add(currentMessageConversation);
                }
            }
        }
        List<Conversation> conversationList = ConversationCacheUtils.getConversationListByIdList(MyApplication.getInstance(), conversationIdList);
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation tempConversation = conversationList.get(i);
            if (cidNumMap.containsKey(tempConversation.getId())) {
                ConversationWithMessageNum conversationFromChatContent =
                        new ConversationWithMessageNum(tempConversation, cidNumMap.get(tempConversation.getId()));
                conversationFromChatContentResultList.add(conversationFromChatContent);
            }
        }
        return conversationFromChatContentResultList;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHARE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * EditText  Watcher
     */
    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            searchText = searchEdit.getText().toString().trim();
            if (!StringUtils.isBlank(searchText)) {
                long currentTime = System.currentTimeMillis();
                Long timeDiffer = currentTime - lastSearchTime;
                if (timeDiffer > 500) {
                    handler.post(searchRunnable);
                } else {
                    handler.removeCallbacks(searchRunnable);
                    handler.postDelayed(searchRunnable, 500);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnable);
                contactList.clear();
                groupConversationList.clear();
                directConversationList.clear();
                conversationFromChatContentList.clear();
                handler.sendEmptyMessage(REFRESH_DATA);
            }
        }
    }
}
