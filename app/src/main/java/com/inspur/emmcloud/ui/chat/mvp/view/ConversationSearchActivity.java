package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialog;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.ui.chat.CommunicationSearchGroupContactActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ChatRecentAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ConversationSearchPresenter;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
                String name = CommunicationUtils.getName(getContext(), conversation);
                String headUrl = CommunicationUtils.getHeadUrl(conversation);
                //分享到
                ShareDialog.Builder builder = new ShareDialog.Builder(ConversationSearchActivity.this);
                builder.setUserName(name);
                builder.setContent(shareContent);
                builder.setDefaultResId(R.drawable.ic_app_default);
                builder.setHeadUrl(headUrl);
                final ShareDialog dialog = builder.build();
                dialog.setCallBack(new ShareDialog.CallBack() {
                    @Override
                    public void onConfirm(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("conversation", conversation);
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
                Intent intent = new Intent(this, CommunicationSearchGroupContactActivity.class);
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
