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

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.WeakHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/5/20.
 */

public class CommunicationSearchContactActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {
    public static final String SEARCH_ALL = "search_all";
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;
    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.lv_search_group_show)
    ListView searchGroupListView;
    private Runnable searchRunnable;
    private List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<Contact>(); // 通讯录搜索结果
    private List<Contact> excludeContactList = new ArrayList<>();//不显示某些数据
    private String searchArea = SEARCH_GROUP;
    private String searchText;
    private Handler handler;
    private long lastSearchTime = 0;
    private GroupAdapter groupAdapter;
    private ContactAdapter contactAdapter;


    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchContactActivity.this);
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
        groupAdapter = new GroupAdapter();
        contactAdapter = new ContactAdapter();
        searchGroupListView.setAdapter(groupAdapter);
        searchGroupListView.setOnItemClickListener(this);
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }


    private void handMessage() {
        handler = new WeakHandler(this) {
            @Override
            protected void handleMessage(Object o, Message message) {
                switch (message.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        groupAdapter.notifyDataSetChanged();
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
                        switch (searchArea) {
                            case SEARCH_ALL:
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 3);
                                break;
                            case SEARCH_GROUP:
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                break;
                            case SEARCH_CONTACT:
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
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.lv_search_group_show:
                if ((searchChannelGroupList.size() > 0) && (searchChannelGroupList.get(i) != null)) {
                    Intent intent = new Intent();
                    intent.setClass(this, ConversationActivity.class);
                    intent.putExtra("title", searchChannelGroupList.get(i).getName());
                    intent.putExtra("cid", searchChannelGroupList.get(i).getId());
                    intent.putExtra("channelType", searchChannelGroupList.get(i).getType());
                    startActivity(intent);
                    finish();
                }
                break;
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
                searchChannelGroupList.clear();
                handler.sendEmptyMessage(REFRESH_DATA);
            }
        }
    }

    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView  nameTextView;
        public TextView  detailTextView;
    }

    /**
     * 联系人Adapter
     */
    class ContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }

    /**
     * 群组Adapter
     */
    class GroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchChannelGroupList.size();
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
            if(view==null){
                view = LayoutInflater.from(CommunicationSearchContactActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView =view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView=view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView=view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            }else {
                searchHolder = (SearchHolder) view.getTag();
            }
            SearchModel searchModel = searchChannelGroupList.get(i);
            if (searchModel != null) {
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
            }
            //刷新数据
            return view;
        }
    }

}
