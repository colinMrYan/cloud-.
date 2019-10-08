package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialog;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/9.
 */

public class CommunicationSearchGroupContactActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {

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
    private List<SearchModel> contactsList = new ArrayList<>();
    private List<SearchModel> groupsList = new ArrayList<>();
    private List<SearchModel> privateChatList = new ArrayList<>();
    private List<ConversationFromChatContent> conversationFromChatContentList = new ArrayList<>();
    private Runnable searchRunnable;
    private String searchArea = SEARCH_ALL;
    private Handler handler;
    private GroupOrContactAdapter groupAdapter;
    private GroupOrContactAdapter contactAdapter;
    private GroupOrContactAdapter privateChatAdapter;
    private ConversationFromChatContentAdapter conversationFromChatContentAdapter;
    private String searchText;
    private long lastSearchTime = 0;
    private String shareContent;
    /**
     * 虚拟键盘
     */
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchGroupContactActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).statusBarColor(R.color.search_contact_header_bg).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();
        handMessage();
        initSearchRunnable();
        groupAdapter = new GroupOrContactAdapter();
        privateChatAdapter = new GroupOrContactAdapter();
        contactAdapter = new GroupOrContactAdapter();
        conversationFromChatContentAdapter = new ConversationFromChatContentAdapter();
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

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_group_contact_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    /**
     * 异步处理数据*/
    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        if (groupsList == null) {
                            groupsList = new ArrayList<>();
                        }
                        if (contactsList == null) {
                            contactsList = new ArrayList<>();
                        }
                        if (privateChatList == null) {
                            privateChatList = new ArrayList<>();
                        }
                        searchMoreContentLayout.setVisibility(contactsList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreGroupLayout.setVisibility(groupsList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMorePrivateChatLayout.setVisibility(privateChatList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreContactFromChatLayout.setVisibility(conversationFromChatContentList.size() > 2 ? View.VISIBLE : View.GONE);
                        allGroupLayout.setVisibility(groupsList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContactLayout.setVisibility(contactsList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContentLayout.setVisibility(conversationFromChatContentList.size() > 0 ? View.VISIBLE : View.GONE);
                        allPrivateChatLayout.setVisibility(privateChatList.size() > 0 ? View.VISIBLE : View.GONE);
                        groupAdapter.setContentList(groupsList);
                        privateChatAdapter.setContentList(privateChatList);
                        contactAdapter.setContentList(contactsList);
                        groupAdapter.notifyDataSetChanged();
                        contactAdapter.notifyDataSetChanged();
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
        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                openChannel(groupsList.get(i));
                break;
            case R.id.lv_search_contact:
                Bundle bundle = new Bundle();
                bundle.putString("uid", contactsList.get(i).getId());
                IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
                break;
            case R.id.lv_search_contact_from_chat:
                Intent intent = new Intent(CommunicationSearchGroupContactActivity.this, CommunicationSearchMessagesActivity.class);
                intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentList.get(i));
                intent.putExtra(SEARCH_CONTENT, searchText);
                startActivity(intent);
                break;
            case R.id.lv_search_private_chat:
                openChannel(privateChatList.get(i));
                break;
            default:
                break;
        }
    }

    private void openChannel(SearchModel searchModel) {
        switch (searchModel.getType()) {
            case SearchModel.TYPE_GROUP:
                startChannelActivity(searchModel.getId());
                break;
            case SearchModel.TYPE_USER:
                createDirectChannel(searchModel.getId());
                break;
        }
    }


    private void handleShare(AdapterView<?> adapterView, int position) {

        SearchModel searchModel;

        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                searchModel = groupsList.get(position);
//                startChannelActivity(groupsList.get(position).getId());

                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_contact:
                searchModel = contactsList.get(position);
//                Bundle bundle = new Bundle();
//                bundle.putString("uid", contactsList.get(position).getId());
//                IntentUtils.startActivity(this, UserInfoActivity.class, bundle);

                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_private_chat:
                searchModel = privateChatList.get(position);
                handleSearchModelShare(searchModel);
                break;
            case R.id.lv_search_contact_from_chat:
                ConversationFromChatContent conversationFromChatContent = conversationFromChatContentList.get(position);
                final Conversation conversation = conversationFromChatContent.getConversation();

                String name = CommunicationUtils.getName(this, conversation);
                String headUrl = CommunicationUtils.getHeadUrl(conversation);
                //分享到
                ShareDialog.Builder builder = new ShareDialog.Builder(this);
                builder.setUserName(name);
                builder.setContent(shareContent);
                builder.setDefaultResId(R.drawable.ic_app_default);
                builder.setHeadUrl(headUrl);
                final ShareDialog dialog = builder.build();
                dialog.setCallBack(new ShareDialog.CallBack() {
                    @Override
                    public void onConfirm(View view) {
                        Intent intent = new Intent();
                        SearchModel searchModel1 = conversation.conversation2SearchModel();
                        intent.putExtra("searchModel", searchModel1);
                        setResult(RESULT_OK, intent);
                        dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        dialog.dismiss();
                    }
                });
                dialog.show();
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
        String name = searchModel.getName();
        String headUrl = APIUri.getChannelImgUrl(this, searchModel.getId());
        //分享到
        ShareDialog.Builder builder = new ShareDialog.Builder(this);
        builder.setUserName(name);
        builder.setContent(shareContent);
        builder.setDefaultResId(R.drawable.ic_app_default);
        builder.setHeadUrl(headUrl);
        final ShareDialog dialog = builder.build();
        dialog.setCallBack(new ShareDialog.CallBack() {
            @Override
            public void onConfirm(View view) {
                Intent intent = new Intent();
                intent.putExtra("searchModel", searchModel);
                setResult(RESULT_OK, intent);
                dialog.dismiss();
                finish();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(CommunicationSearchGroupContactActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(CommunicationSearchGroupContactActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(CommunicationSearchGroupContactActivity.this, uid,
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
     * 初始化异步方法*/
    private void initSearchRunnable() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<SearchModel> groupsSearchList;
                        List<SearchModel> privateChatSearchList;
                        List<Contact> contactsSearchList;
                        switch (searchArea) {
                            case SEARCH_ALL:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                privateChatSearchList = ConversationCacheUtils.getSearchConversationPrivateChatSearchModelList(MyApplication.getInstance(), searchText);
                                privateChatList = new ArrayList<>();
                                for (int i = 0; i < (privateChatSearchList.size() > 3 ? 3 : privateChatSearchList.size()); i++) {
                                    privateChatList.add(privateChatSearchList.get(i));
                                }
                                groupsList = new ArrayList<>();
                                for (int i = 0; i < (groupsSearchList.size() > 3 ? 3 : groupsSearchList.size()); i++) {
                                    groupsList.add(groupsSearchList.get(i));
                                }
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                contactsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                conversationFromChatContentList.clear();
                                conversationFromChatContentList = new ArrayList<>();
                                conversationFromChatContentList = oriChannelInfoByKeyword(searchText);
                                break;
                            case SEARCH_GROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                groupsList = new ArrayList<>();
                                for (int i = 0; i < (groupsSearchList.size() > 3 ? 3 : groupsSearchList.size()); i++) {
                                    groupsList.add(groupsSearchList.get(i));
                                }
                                break;
                            case SEARCH_CONTACT:
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                contactsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                break;
                            default:
                                break;
                        }
                        if (handler != null) {
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
    private List<ConversationFromChatContent> oriChannelInfoByKeyword(String searchData) {
        Map<String, Integer> cidNumMap = new HashMap<>();
        cidNumMap.clear();
        List<com.inspur.emmcloud.bean.chat.Message> allMessageListByKeyword = new ArrayList<>();
        allMessageListByKeyword.clear();
        List<ConversationFromChatContent> conversationFromChatContentResultList = new ArrayList<>();
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
                ConversationFromChatContent conversationFromChatContent =
                        new ConversationFromChatContent(tempConversation, cidNumMap.get(tempConversation.getId()));
                if (tempConversation.getType().equals(Conversation.TYPE_DIRECT)) {
                    conversationFromChatContent.initSingleChatContact();
                }
                conversationFromChatContentResultList.add(conversationFromChatContent);
            }
        }
        return conversationFromChatContentResultList;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHARE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * EditText  Watcher*/
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
                if (currentTime - lastSearchTime > 700) {
                    handler.post(searchRunnable);
                } else {
                    handler.removeCallbacks(searchRunnable);
                    handler.postDelayed(searchRunnable, 700);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnable);
                handler.sendEmptyMessage(REFRESH_DATA);
                contactsList.clear();
                groupsList.clear();
                privateChatList.clear();
                conversationFromChatContentList.clear();
            }
        }
    }

    /***/
    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
    }

    /**
     * 根据去群组或者联系人名称搜获获得SearchModel的适配
     */
    class GroupOrContactAdapter extends BaseAdapter {

        private List<SearchModel> contentList = new ArrayList<>();

        public void setContentList(List<SearchModel> contentList) {
            this.contentList = contentList;
        }

        @Override
        public int getCount() {
            return contentList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            SearchHolder searchHolder = new SearchHolder();
            if (view == null) {
                view = LayoutInflater.from(CommunicationSearchGroupContactActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            SearchModel searchModel = contentList.get(i);
            if (searchModel != null) {
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                CommunicationUtils.setUserDescText(searchModel, searchHolder.detailTextView);
            }
            //刷新数据
            return view;
        }
    }

    /**
     * 从聊天记录中搜索联系人
     */
    class ConversationFromChatContentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (conversationFromChatContentList.size() > 3) {
                return 3;
            } else {
                return conversationFromChatContentList.size();
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            SearchHolder searchHolder = new SearchHolder();
            if (view == null) {
                view = LayoutInflater.from(CommunicationSearchGroupContactActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            Conversation conversation = conversationFromChatContentList.get(i).getConversation();
            if (conversation != null && (conversation.getType().equals(Conversation.TYPE_GROUP))) {
                SearchModel searchModel = conversation.conversation2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }

            if (conversation != null && (conversation.getType().equals(Conversation.TYPE_CAST))) {
                UIConversation uiConversation = new UIConversation(conversation);
                String icon = DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
                searchHolder.nameTextView.setText(uiConversation.getTitle());
                ImageDisplayUtils.getInstance().displayImage(searchHolder.headImageView, icon, R.drawable.icon_person_default);
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }

            Contact contact = conversationFromChatContentList.get(i).getSingleChatContactUser();
            if (contact != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
                SearchModel searchModel = contact.contact2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            //刷新数据
            return view;
        }
    }
}
