package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MailAdapter;

import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailHomeActivity extends MailHomeBaseActivity {

    @ViewInject(R.id.srl_refresh)
    private SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.rcv_mail)
    private RecyclerView mailRecyclerView;

    private MailAdapter mailAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mailRecyclerView.setLayoutManager(linearLayoutManager);
        mailAdapter= new MailAdapter(this);
        mailRecyclerView.setAdapter(mailAdapter);
    }


}
