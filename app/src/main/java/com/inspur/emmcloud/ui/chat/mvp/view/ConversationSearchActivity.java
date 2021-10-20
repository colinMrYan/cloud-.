package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.SearchActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ChatRecentAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ConversationSearchPresenter;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ShareUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = Constant.AROUTER_CLASS_CONVERSATION_SEARCH)
public class ConversationSearchActivity extends BaseMvpActivity<ConversationSearchPresenter> implements ConversionSearchContract.View {

    static final int REQUEST_CODE_SHARE = 1;
    @BindView(R.id.rcv_conversation)
    RecyclerView conversionRecycleView;
    ChatRecentAdapter conversationAdapter;
    List<Conversation> list = new ArrayList<>();
    String shareContent;
    private String uri;
    private Bundle extras;
    private String title;
    private String appName;
    private String ico;
    private String appUrl;
    private String description; // web应用内分享内容简述
    private boolean isHaveAPPNavBar;
    private boolean isWebShare;
    private boolean isShowHeader = true; // 默认为true
    private Conversation conversation;
    private SearchModel searchModel;

    public ConversationSearchActivity() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_search;
    }

    private void init() {
        setTitleText(R.string.baselib_share_to);
        shareContent = getIntent().getStringExtra(Constant.SHARE_CONTENT);
        // web应用内分享到聊天
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uri = extras.getString("uri");
            title = extras.getString("appName");
            description = extras.getString("description");
            isWebShare = extras.getBoolean("isShare", false);
            isShowHeader = extras.getBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
            isHaveAPPNavBar = extras.getBoolean("isHaveAPPNavbar", true);
            appName = extras.getString("app_name");// 应用名
            appUrl = extras.getString("app_url");
            ico = extras.getString("ico");
        }
        mPresenter = new ConversationSearchPresenter();
        mPresenter.attachView(this);
        mPresenter.getConversationData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        conversionRecycleView.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ChatRecentAdapter(getActivity(), list);
        conversationAdapter.setAdapterListener(new ChatRecentAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                conversation = list.get(position);
                searchModel = conversation.conversation2SearchModel();
                // web应用内分享
                if (isWebShare) {
                    ShareUtil.share(ConversationSearchActivity.this, searchModel, title, true);
                } else {
                    ShareUtil.share(ConversationSearchActivity.this, searchModel, shareContent);
                }
            }
        });
        conversionRecycleView.setAdapter(conversationAdapter);
        //获取数据

    }

    @Override
    public void showConversationData(final List<Conversation> conversationList) {
        list = conversationList;
    }

    @OnClick(R.id.ibt_back)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_search_contact:
                Intent intent = new Intent(this, SearchActivity.class);
                if (isWebShare) {
                    shareContent = getString(R.string.baselib_share_link) + title;
                }
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHARE && resultCode == RESULT_OK) {
            if (isWebShare) {
                searchModel = (SearchModel) data.getSerializableExtra("searchModel");
                handleShareResult();
            } else {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    /**
     * 处理web应用内分享
     */
    public void handleShareResult() {
        if (searchModel != null) {
            String userOrChannelId = searchModel.getId();
            boolean isUser = searchModel.getType().equals(SearchModel.TYPE_USER);
            share2Conversation(userOrChannelId, isUser);
        } else {
            finish();
        }
    }

    /**
     * 分享到聊天界面
     */
    private void share2Conversation(String userOrChannelId, boolean isUser) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isUser) {
                createDirectChannel(userOrChannelId);
            } else {
                startChannelActivity(userOrChannelId);
            }
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
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
            new ChatCreateUtils().createDirectChannel(this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                            finish();
                        }
                    });
        }

    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", "link");
        bundle.putSerializable(Constant.SHARE_LINK, conbineGroupNewsContent());
        IntentUtils.startActivity(this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 组装集团新闻内容
     *
     * @return
     */
    private String conbineGroupNewsContent() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", uri);
            jsonObject.put("poster", "");
            jsonObject.put("digest", description);
            jsonObject.put("title", title);
            jsonObject.put("app_name", appName);
            jsonObject.put("ico", ico);
            jsonObject.put("app_url", appUrl);
            jsonObject.put("isHaveAPPNavbar", isHaveAPPNavBar);
            jsonObject.put(Constant.WEB_FRAGMENT_SHOW_HEADER, isShowHeader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
