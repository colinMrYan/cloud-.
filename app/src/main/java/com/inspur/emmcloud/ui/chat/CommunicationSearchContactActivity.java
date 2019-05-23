package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.WeakHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/5/20.
 */

public class CommunicationSearchContactActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.lv_search_group_show)
    ListView searchGroupListView;
    @BindView(R.id.lv_search_members_show)
    ListView searchMembersListView;


    public static final String  SEARCH_ALL="search_all";
    public static final String  SEARCH_CONTACT="search_contact";
    public static final String  SEARCH_GROUP ="search_group";
    public static final int  REFRESH_DATA =1;
    private Runnable searchRunable;
    private List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<Contact>(); // 通讯录搜索结果
    private List<Contact> excludeContactList = new ArrayList<>();//不显示某些数据
    private String   searchArea=SEARCH_GROUP;
    private String   searchText;
    private Handler  handler;
    private long     lastSearchTime=0;


    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchContactActivity.this);
              //  onSearch(findViewById(R.id.search_btn));
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_search_contact);
        ButterKnife.bind(this);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        cancelTextView.setOnClickListener(this);
        handMessage();
        initSearchRunnable();
    }

    private void handMessage() {
        handler = new WeakHandler(this) {
            @Override
            protected void handleMessage(Object o, Message message) {
                switch (message.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        LogUtils.LbcDebug("刷新数据 searchChannelGroupList::"+searchChannelGroupList.size());
                        break;
                }
            }

        };
    }


    private void initSearchRunnable() {
        searchRunable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch (searchArea) {
                            case SEARCH_ALL:
                                if (MyApplication.getInstance().isV0VersionChat()) {
                                    searchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                    LogUtils.LbcDebug("isVo");
                                } else {
                                    LogUtils.LbcDebug("isV1");
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }

                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 3);
                                break;
                            case SEARCH_CONTACT:
                                LogUtils.LbcDebug("contact");
                                if (MyApplication.getInstance().isV0VersionChat()) {
                                    searchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                break;
                            case SEARCH_GROUP:
                                LogUtils.LbcDebug("group");
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 3);
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
        switch (view.getId()){
            case R.id.tv_cancel:
                finish();
                break;
        }
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
                    handler.post(searchRunable);
                } else {
                    handler.removeCallbacks(searchRunable);
                    handler.postDelayed(searchRunable, 500);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunable);
            }
        }
    }

   // class Conta




}
