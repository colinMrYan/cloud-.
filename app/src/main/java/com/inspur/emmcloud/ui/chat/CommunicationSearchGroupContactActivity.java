package com.inspur.emmcloud.ui.chat;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
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
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/9.
 */

public class CommunicationSearchGroupContactActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {

    public static final String SEARCH_ALL = "search_all";
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;

    @BindView(R.id.lv_search_contact)
    ListView searchContentListView;
    @BindView(R.id.lv_search_group)
    ListView searchGroupListView;
    @BindView(R.id.rl_search_more_group)
    RelativeLayout searchMoreGroupListView;
    @BindView(R.id.rl_search_more_contact)
    RelativeLayout searchMoreContentListView;
    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_group_title)
    TextView groupTitleText;
    @BindView(R.id.tv_contacts_title)
    TextView contactsTitleText;
    private List<SearchModel> contactsList = new ArrayList<>();
    private List<SearchModel> groupsList = new ArrayList<>();
    private Runnable searchRunnable;
    private String searchArea = SEARCH_ALL;
    private Handler handler;
    private SearchContentAdapter groupAdapter;
    private SearchContentAdapter contactAdapter;
    private String searchText;
    private long lastSearchTime = 0;
    /***/
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
        handMessage();
        initSearchRunnable();
        groupAdapter = new SearchContentAdapter();
        contactAdapter = new SearchContentAdapter();
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        searchContentListView.setAdapter(contactAdapter);
        searchGroupListView.setAdapter(groupAdapter);
        searchContentListView.setOnItemClickListener(this);
        searchGroupListView.setOnItemClickListener(this);
        searchMoreContentListView.setVisibility(View.GONE);
        searchMoreGroupListView.setVisibility(View.GONE);
        groupTitleText.setVisibility(View.GONE);
        contactsTitleText.setVisibility(View.GONE);
        InputMethodUtils.display(this, searchEdit);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_group_contact_activity;
    }

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
                        searchMoreContentListView.setVisibility(contactsList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreGroupListView.setVisibility(groupsList.size() > 2 ? View.VISIBLE : View.GONE);
                        groupTitleText.setVisibility(contactsList.size() > 0 ? View.VISIBLE : View.GONE);
                        contactsTitleText.setVisibility(groupsList.size() > 0 ? View.VISIBLE : View.GONE);
                        groupAdapter.setContentList(groupsList);
                        contactAdapter.setContentList(contactsList);
                        groupAdapter.notifyDataSetChanged();
                        contactAdapter.notifyDataSetChanged();
                        break;
                    case CLEAR_DATA:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        Bundle bundle;
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.rl_search_more_group:
                bundle = new Bundle();
                bundle.putBoolean("is_group", true);
                bundle.putString("search_content", searchText);
                IntentUtils.startActivity(this, CommunicationSearchModelMoreActivity.class, bundle, true);
                //跳转到群组列表页面
                break;
            case R.id.rl_search_more_contact:
                bundle = new Bundle();
                bundle.putBoolean("is_group", false);
                bundle.putString("search_content", searchText);
                IntentUtils.startActivity(this, CommunicationSearchModelMoreActivity.class, bundle, true);
                //跳转到查找人列表
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                groupsList.get(i);
                startChannelActivity(groupsList.get(i).getId());
                break;
            case R.id.lv_search_contact:
                createDirectChannel(contactsList.get(i).getId());
                break;
            default:
                break;
        }
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
                                    groupsList = new ArrayList<>();
                                    for (int i = 0; i < 3; i++) {
                                        groupsList.add(groupsSearchList.get(i));
                                    }
                                }
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                contactsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                break;
                            case SEARCH_GROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                if (groupsSearchList.size() > 3) {
                                    groupsList = new ArrayList<>();
                                    for (int i = 0; i < 3; i++) {
                                        groupsList.add(groupsSearchList.get(i));
                                    }
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

    /***/
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
                handler.sendEmptyMessage(REFRESH_DATA);
                contactsList.clear();
                groupsList.clear();
            }
        }
    }

    /***/
    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
    }

    /***/
    class SearchContentAdapter extends BaseAdapter {

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
            }
            //刷新数据
            return view;
        }
    }

}
