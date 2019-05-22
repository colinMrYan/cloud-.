package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.WeakHandler;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/5/20.
 */
@ContentView(R.layout.activity_communication_search_contact)
public class CommunicationSearchContactActivity extends BaseActivity implements View.OnClickListener {
    @ViewInject(R.id.ev_search_input)
    private ClearEditText searchEdit;
    @ViewInject(R.id.tv_cancel)
    private TextView cancelTextView;

    public static final String  SEARCH_ALL="search_all";
    public static final String  SEARCH_CONTACT="search_contact";
    public static final String  SEARCH_GROUP ="search_group";
    private Runnable searchRunable;
    private List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<Contact>(); // 通讯录搜索结果
    private List<Contact> excludeContactList = new ArrayList<>();//不显示某些数据
    private String   searchArea;
    private String   searchText;
    private Handler  handler;


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
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        cancelTextView.setOnClickListener(this);
    }

    private void handMessage() {
        handler = new WeakHandler(this) {

            @Override
            protected void handleMessage(Object o, Message message) {
                switch (message.what) {
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
                                } else {
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }

                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 4);
                                break;
                            case SEARCH_CONTACT:
                                if (MyApplication.getInstance().isV0VersionChat()) {
                                    searchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                break;
                            case SEARCH_GROUP:
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 4);
                                break;
                            default:
                                break;
                        }
//                        if (handler != null) {
//                            handler.sendEmptyMessage(REFRESH_DATA);
//                        }
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
//                if (popLayout.getVisibility() == View.GONE) {
//                    searchArea = orginCurrentArea;
//                }
//                long currentTime = System.currentTimeMillis();
//                if (currentTime - lastSearchTime > 500) {
//                    handler.post(searchRunnbale);
//                } else {
//                    handler.removeCallbacks(searchRunnbale);
//                    handler.postDelayed(searchRunnbale, 500);
//                }
//                lastSearchTime = System.currentTimeMillis();
            } else {
//                lastSearchTime = 0;
//                handler.removeCallbacks(searchRunnbale);
            }
        }
    }




}
