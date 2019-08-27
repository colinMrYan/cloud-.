package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/20.
 */

public class CommunicationSearchModelMoreActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {

    public static final String SEARCH_ALL = "search_all";
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final String SEARCH_CONTENT = "search_content";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;

    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.lv_search_group_show)
    ListView searchGroupListView;
    private Runnable searchRunnable;
    private List<SearchModel> searchModelsList = new ArrayList<>(); // 群组搜索结果
    private List<ConversationFromChatContent> conversationFromChatContentList = new ArrayList<>();
    private String searchArea = SEARCH_GROUP;
    private String searchText;
    private Handler handler;
    private long lastSearchTime = 0;
    private GroupAdapter groupAdapter;
    private ConversationFromChatContentAdapter conversationFromChatContentAdapter;
    private boolean isGroup = false;

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchModelMoreActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        searchArea = getIntent().getStringExtra("search_type");
        searchText = getIntent().getStringExtra("search_content");
        int navigationBarColor = R.color.search_contact_header_bg;
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this, R.attr.status_bar_dark_font);
        ImmersionBar.with(this).statusBarColor(navigationBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        InputMethodUtils.display(this, searchEdit);
        cancelTextView.setOnClickListener(this);
        handMessage();
        initSearchRunnable();
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
            conversationFromChatContentAdapter = new ConversationFromChatContentAdapter();
            searchGroupListView.setAdapter(conversationFromChatContentAdapter);
        } else {
            groupAdapter = new GroupAdapter();
            searchGroupListView.setAdapter(groupAdapter);
        }
        searchGroupListView.setOnItemClickListener(this);
        searchEdit.setText(searchText);
        searchEdit.setSelection(searchText.length());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_model_detail_activity;
    }


    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
                            conversationFromChatContentAdapter.notifyDataSetChanged();
                        } else {
                            groupAdapter.notifyDataSetChanged();
                        }
                        break;
                    case CLEAR_DATA:
                        break;
                }
            }
        };
    }

    private void initSearchRunnable() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<SearchModel> groupsSearchList;
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
                                if (groupsSearchList.size() > 3) {
                                    searchModelsList = new ArrayList<>();
                                    for (int i = 0; i < 3; i++) {
                                        searchModelsList.add(groupsSearchList.get(i));
                                    }
                                }
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 1000);
                                searchModelsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    searchModelsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                break;
                            case SEARCH_GROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    searchModelsList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchModelsList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                if (searchModelsList == null) {
                                    searchModelsList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_CONTACT:
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 1000);
                                searchModelsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    searchModelsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                break;
                            case SEARCH_ALL_FROM_CHAT:
                                conversationFromChatContentList = new ArrayList<>();
                                conversationFromChatContentList = MessageCacheUtil.getConversationListByContent(MyApplication.getInstance(), searchText);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
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
            new ChatCreateUtils().createDirectChannel(this, uid,
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
            Intent intent = new Intent(CommunicationSearchModelMoreActivity.this, CommunicationSearchMessagesActivity.class);
            intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentList.get(i));
            intent.putExtra(SEARCH_CONTENT, searchText);
            startActivity(intent);
        } else {
            if (!searchModelsList.get(i).getId().equals(BaseApplication.getInstance().getUid())) {
                switch (searchArea) {
                    case SEARCH_GROUP:
                        startChannelActivity(searchModelsList.get(i).getId());
                        break;
                    case SEARCH_CONTACT:
                        createDirectChannel(searchModelsList.get(i).getId());
                        break;
                }
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("uid", searchModelsList.get(i).getId());
                IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
            }
        }
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
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //handleSearchApp(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
            searchText = searchEdit.getText().toString().trim();
            if (!StringUtils.isBlank(searchText)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSearchTime > 500) {
                    handler.post(searchRunnable);
                } else {
                    handler.removeCallbacks(searchRunnable);
                    handler.postDelayed(searchRunnable, 500);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnable);
                searchModelsList.clear();
                conversationFromChatContentList.clear();
                handler.sendEmptyMessage(REFRESH_DATA);
            }
        }
    }

    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
    }

    /**
     * 群组Adapter
     */
    class GroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchModelsList.size();
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
                view = LayoutInflater.from(CommunicationSearchModelMoreActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            SearchModel searchModel = searchModelsList.get(i);
            if (searchModel != null) {
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
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
            return conversationFromChatContentList.size();
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
                view = LayoutInflater.from(CommunicationSearchModelMoreActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            Conversation conversation = conversationFromChatContentList.get(i).getConversation();
            if (conversation != null && conversation.getType().equals(Conversation.TYPE_GROUP)) {
                SearchModel searchModel = conversation.conversation2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                searchHolder.detailTextView.setText(conversationFromChatContentList.get(i).getMessageNum() + "条相关消息记录");
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            Contact contact = conversationFromChatContentList.get(i).getSingleChatContactUser();
            if (contact != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
                SearchModel searchModel = contact.contact2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                searchHolder.detailTextView.setText(conversationFromChatContentList.get(i).getMessageNum() + "条相关消息记录");
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            //刷新数据
            return view;
        }
    }
}
