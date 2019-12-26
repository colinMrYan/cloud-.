package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.SearchActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ChatRecentAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ConversationSearchPresenter;
import com.inspur.emmcloud.util.privates.ShareUtil;

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
        mPresenter = new ConversationSearchPresenter();
        mPresenter.attachView(this);
        mPresenter.getConversationData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        conversionRecycleView.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ChatRecentAdapter(getActivity(), list);
        conversationAdapter.setAdapterListener(new ChatRecentAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Conversation conversation = list.get(position);
                SearchModel searchModel = conversation.conversation2SearchModel();
                ShareUtil.share(ConversationSearchActivity.this, searchModel, shareContent);
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
                intent.putExtra(Constant.SHARE_CONTENT, shareContent);
                startActivityForResult(intent, REQUEST_CODE_SHARE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHARE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
