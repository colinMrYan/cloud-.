package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.MultiMessageTransmitUtil;
import com.inspur.emmcloud.ui.chat.SearchActivity;
import com.inspur.emmcloud.ui.chat.SearchSendMultiActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ConversationSendHeadAdapter;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ConversationSendMultiAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ConversationSearchPresenter;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ShareUtil;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.inspur.emmcloud.ui.chat.MultiMessageTransmitUtil.EXTRA_MULTI_MESSAGE_TYPE;

@Route(path = Constant.AROUTER_CLASS_CONVERSATION_SEND_MORE)
public class ConversationSendMultiActivity extends BaseMvpActivity<ConversationSearchPresenter> implements ConversionSearchContract.View {

    static final int REQUEST_CODE_SHARE = 1;
    static final int REQUEST_CODE_SHARE_MULTI = 2;
    @BindView(R.id.rcv_conversation)
    RecyclerView conversionRecycleView;
    @BindView(R.id.tv_more_select)
    TextView selectMoreTv;
    @BindView(R.id.tv_ok)
    TextView okTv;
    @BindView(R.id.rv_select)
    RecyclerView selectRv;
    ConversationSendMultiAdapter sendMoreAdapter;
    List<Conversation> list = new ArrayList<>();
    List<Conversation> recentList = new ArrayList<>(); // 最近转发列表
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
    private SearchModel searchModel;
    private int mMultiMessageType;

    // 多选相关参数
    private boolean isSelectMore = false; // 默认单选
    private List<MessageForwardMultiBean> selectList = new ArrayList<>();
    private ConversationSendHeadAdapter headAdapter;

    public ConversationSendMultiActivity() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_send_more;
    }

    private void init() {
        setTitleText(R.string.baselib_share_to);
        shareContent = getIntent().getStringExtra(Constant.SHARE_CONTENT);
        mMultiMessageType = getIntent().getIntExtra(EXTRA_MULTI_MESSAGE_TYPE, MultiMessageTransmitUtil.TYPE_SINGLE);
        // web应用内分享到聊天，转发多人未用到，暂时留着，也可删掉web相关代码
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
        mPresenter.getTransmitConversationData();
        // 多选时顶部已选人/群组列表初始化
        headAdapter = new ConversationSendHeadAdapter(this, selectList);
        headAdapter.setHeadItemClickListener(new ConversationSendHeadAdapter.HeadAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectList.remove(position);
                notifySelectText();
                sendMoreAdapter.setSelectList(selectList);
                showMultiSelectHeadView(false, position);
            }
        });
        LinearLayoutManager headManager = new LinearLayoutManager(getActivity());
        headManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        selectRv.setLayoutManager(headManager);
        selectRv.setAdapter(headAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        conversionRecycleView.setLayoutManager(linearLayoutManager);

        // 获取最近转发
        recentList = ConversationCacheUtils.getRecentTransmitIdList(this);
        if (recentList.size() > 0) {
            // null代表标题 最近转发
            list.add(0, null);
            list.addAll(0, recentList);
            // 添加标题，最近聊天
            list.add(0, null);
        }

        sendMoreAdapter = new ConversationSendMultiAdapter(getActivity(), list);
        sendMoreAdapter.setAdapterListener(new ConversationSendMultiAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Conversation conversation = list.get(position);
                if (!isSelectMore) {
                    // 单选
                    searchModel = conversation.conversation2SearchModel();
                    ShareUtil.share(ConversationSendMultiActivity.this, searchModel, shareContent, isWebShare, mMultiMessageType);
                } else {
                    // 多选
                    SearchModel searchModel = conversation.conversation2SearchModel();
                    // 转换成统一bean：已选list可能包含会话，也可能包含联系人
                    String contactId = "";
                    if (searchModel.getType().equals(SearchModel.TYPE_DIRECT)) {
                        ArrayList<String> memberList = conversation.getMemberList();
                        String uid = BaseApplication.getInstance().getUid();
                        for (int i = 0; i < memberList.size(); i++) {
                            String memberId = memberList.get(i);
                            if (!memberId.equals(uid)) {
                                contactId = memberId;
                            }
                        }
                    } else {
                        // 这里可以不写。contactId只在单聊时有用
                        contactId = "";
                    }
                    MessageForwardMultiBean selectBean = new MessageForwardMultiBean(searchModel.getId(),
                            searchModel.getName(), searchModel.getType(), searchModel.getIcon(), contactId);
                    if (selectList.contains(selectBean)) {
                        // 已选，则从列表移除
                        int removePosition = selectList.indexOf(selectBean);
                        selectList.remove(selectBean);
                        notifySelectText();
                        sendMoreAdapter.setSelectList(selectList);
                        showMultiSelectHeadView(false, removePosition);

                    } else {
                        // 未选，则添加到列表
                        // 多选最多9人
                        if (selectList.size() == Constant.MULTI_SELECT_COUNT) {
                            ToastUtils.show(getString(R.string.send_more_select_limit_warning));
                            return;
                        }
                        selectList.add(selectBean);
                        notifySelectText();
                        sendMoreAdapter.setSelectList(selectList);
                        showMultiSelectHeadView(true, -1);
                    }
                }

            }
        });
        conversionRecycleView.setAdapter(sendMoreAdapter);
        //获取数据

    }

    // 多选时更新头部已选列表
    private void notifySelectText() {
        // 多选时已选会话 > 0则显示完成按钮，否则显示单选/多选按钮
        if (selectList.size() > 0) {
            selectMoreTv.setVisibility(View.GONE);
            okTv.setVisibility(View.VISIBLE);
            okTv.setText(getString(R.string.ok_number, selectList.size()));

        } else {
            selectMoreTv.setVisibility(View.VISIBLE);
            okTv.setVisibility(View.GONE);
        }
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
            case R.id.tv_more_select:
                showSelectType();
                break;
            case R.id.tv_ok:
                sendMoreMessage();
                break;
            case R.id.tv_search_contact:
                if (isSelectMore) {
                    Intent intent = new Intent(this, SearchSendMultiActivity.class);
                    intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                    intent.putExtra("selectList", (Serializable) selectList);
                    startActivityForResult(intent, REQUEST_CODE_SHARE_MULTI);
                } else {
                    Intent intent = new Intent(this, SearchActivity.class);
                    if (isWebShare) {
                        shareContent = getString(R.string.baselib_share_link) + title;
                    }
                    intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                    startActivityForResult(intent, REQUEST_CODE_SHARE);
                    break;
                }
        }
    }

    // 多选时消息转发
    private void sendMoreMessage() {
        ShareUtil.shareMultiMembers(this, selectList, shareContent, mMultiMessageType);
    }

    // 多选，单选按钮点击事件
    private void showSelectType() {
        if (isSelectMore) {
            // 变为单选
            selectMoreTv.setText(R.string.chat_contact_more_select);
        } else {
            // 变为多选
            selectMoreTv.setText(R.string.chat_contact_single_select);
        }
        isSelectMore = !isSelectMore;
        // 传入adapter多选还是单选
        sendMoreAdapter.notifySelectMode(isSelectMore);
        showMultiSelectHeadView(true, -1); // 传true或false都行
    }

    // 顶部多选头像view是否可见
    private void showMultiSelectHeadView(boolean isInsert, int removePosition) {
        if (selectList.size() > 0) {
            selectRv.setVisibility(View.VISIBLE);
            if (isInsert) {
                // 增加条目
                if (selectList.size() == 1) {
                    headAdapter.notifyDataSetChanged();
                } else {
                    headAdapter.notifyItemInserted(selectList.size());
                    selectRv.smoothScrollToPosition(selectList.size());
                }
            } else {
                // 删除条目
                headAdapter.notifyItemRemoved(removePosition);
            }
        } else {
            selectRv.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 单选逻辑
        if (requestCode == REQUEST_CODE_SHARE && resultCode == RESULT_OK) {
            if (isWebShare) {
                searchModel = (SearchModel) data.getSerializableExtra("searchModel");
                handleShareResult();
            } else {
                if (data != null) {
                    data.putExtra(EXTRA_MULTI_MESSAGE_TYPE, mMultiMessageType);
                }
                setResult(RESULT_OK, data);
                finish();
            }
        } else if (requestCode == REQUEST_CODE_SHARE_MULTI && resultCode == RESULT_OK) {
            MessageForwardMultiBean selectBean = (MessageForwardMultiBean) data.getSerializableExtra("selectBean");

            if (selectList.contains(selectBean)) {
                // 已选，则从列表移除
                int position = selectList.indexOf(selectBean);
                selectList.remove(selectBean);
                notifySelectText();
                sendMoreAdapter.setSelectList(selectList);
                showMultiSelectHeadView(false, position);
            } else {
                // 未选，则添加到列表
                // 多选最多9人
                if (selectList.size() == Constant.MULTI_SELECT_COUNT) {
                    ToastUtils.show(getString(R.string.send_more_select_limit_warning));
                    return;
                }
                // 选择联系人时，判断单聊聊天是否包含此联系人
                if (selectBean.getType().equals(SearchModel.TYPE_USER)) {
                    for (int i = 0; i < list.size(); i++) {
                        Conversation conversation = list.get(i);
                        if (conversation != null) {
                            int contactIndex = conversation.getMemberList().indexOf(selectBean.getContactId());
                            if (conversation.getType().equals(Conversation.TYPE_DIRECT) && contactIndex >= 0) {
                                selectBean.setConversationId(conversation.getId());
                                // 聊天已存在则将type换成direct，就不需要再创建聊天了
                                selectBean.setType(conversation.getType());
                            }
                        }
                    }
                }
                selectList.add(selectBean);
                notifySelectText();
                sendMoreAdapter.setSelectList(selectList);
                showMultiSelectHeadView(true, -1);
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
