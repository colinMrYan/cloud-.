package com.inspur.emmcloud.ui.chat;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;

import com.inspur.emmcloud.baselib.widget.common.CommonHeaderView;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.MultiMessageItem;


import java.util.ArrayList;
import java.util.Objects;


public class MultiMessageActivity extends BaseActivity {

    public static final String MESSAGE_CONTENT = "message_content";
    public static final String MESSAGE_CID = "message_cid";
    private RecyclerView recyclerView;

    @Override
    public void onCreate() {
        initView();
        initData();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_multi_message;
    }

    private void initView() {
        CommonHeaderView commonHv = findViewById(R.id.hv_common);
        commonHv.setTitle(getResources().getString(R.string.find_search_chat_history));
        View ll_top_back = findViewById(R.id.iv_back);
        ll_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.multi_message_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    private void initData() {
        String messageContent = Objects.requireNonNull(getIntent().getExtras()).getString(MESSAGE_CONTENT);
        String cid = Objects.requireNonNull(getIntent().getExtras()).getString(MESSAGE_CID);
        ArrayList<MultiMessageItem> arrayList = MultiMessageTransmitUtil.getListFromJsonStr(messageContent);
        MultiMessageAdapter adapter = new MultiMessageAdapter(this, arrayList, cid);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
