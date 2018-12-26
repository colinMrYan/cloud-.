package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MailListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailListResult;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.bean.appcenter.mail.MailFolder;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.MailFolderCacheUtils;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailHomeActivity extends MailHomeBaseActivity implements MySwipeRefreshLayout.OnRefreshListener, MySwipeRefreshLayout.OnLoadListener {

    @ViewInject(R.id.srl_refresh)
    private MySwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.lv_mail)
    private ListView mailListView;

    @ViewInject(R.id.tv_header)
    private TextView headerText;

    private MailListAdapter mailAdapter;
    private MailApiService apiService;
    private MailFolder currentMailFolder;
    private MailFolder currentRootMailFolder;
    private List<Mail> mailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mailAdapter = new MailListAdapter(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        apiService = new MailApiService(this);
        apiService.setAPIInterface(new WebService());
        mailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mail mail = mailList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MailDetailActivity.EXTRA_MAIL,mail);
                IntentUtils.startActivity(MailHomeActivity.this,MailDetailActivity.class,bundle);
            }
        });
        mailListView.setAdapter(mailAdapter);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveGetMailByFolder(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_GET_MAIL_BY_FOLDER)) {
            closeMenu();
           MailFolder mailFolder = (MailFolder) simpleEventMessage.getMessageObj();
           if (currentMailFolder == null || !mailFolder.getId().equals(currentMailFolder.getId())){
               mailList.clear();
               mailAdapter.clearMailList();
               currentMailFolder = mailFolder;
               currentRootMailFolder = currentMailFolder;
               while (!currentRootMailFolder.getParentFolderId().equals("")) {
                   currentRootMailFolder = MailFolderCacheUtils.getMailFolderById(currentRootMailFolder.getParentFolderId());
                   if (currentRootMailFolder == null) {
                       break;
                   }
               }
               String header = currentMailFolder.getDisplayName();
               if (currentMailFolder.getUnreadCount() > 0) {
                   header += "(" + currentMailFolder.getUnreadCount() + ")";
               }
               headerText.setText(header);
               getMail(0);
           }

        }
    }


    @Override
    public void onRefresh() {
        getMail(0);
    }

    @Override
    public void onLoadMore() {
        getMail(mailList.size());
    }

    private void getMail(int offset) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.getMailList(currentMailFolder.getId(), 10, offset);
        } else {
            swipeRefreshLayout.setLoading(false);
        }

    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult) {
            swipeRefreshLayout.setLoading(false);
            if (folderId.equals(currentMailFolder.getId())) {
                if (offset == 0) {
                    mailList.clear();
                }
                mailList.addAll(getMailListResult.getMailList());
                mailAdapter.setMailList(mailList, currentRootMailFolder);
                swipeRefreshLayout.setCanLoadMore(mailList.size()>9);
                mailAdapter.notifyDataSetChanged();
            }


        }

        @Override
        public void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode) {
            swipeRefreshLayout.setLoading(false);
            WebServiceMiddleUtils.hand(MailHomeActivity.this, error, errorCode);
        }
    }
}
