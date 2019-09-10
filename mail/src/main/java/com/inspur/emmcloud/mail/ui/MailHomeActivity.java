package com.inspur.emmcloud.mail.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;
import com.inspur.emmcloud.mail.adapter.MailListAdapter;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;
import com.inspur.emmcloud.mail.api.MailAPIService;
import com.inspur.emmcloud.mail.bean.GetMailListResult;
import com.inspur.emmcloud.mail.bean.Mail;
import com.inspur.emmcloud.mail.bean.MailFolder;
import com.inspur.emmcloud.mail.util.MailCacheUtils;
import com.inspur.emmcloud.mail.util.MailFolderCacheUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chenmch on 2018/12/20.
 */
@Route(path = Constant.AROUTER_CLASS_MAIL_HOME)
public class MailHomeActivity extends MailHomeBaseActivity implements MySwipeRefreshLayout.OnRefreshListener, MySwipeRefreshLayout.OnLoadListener {

    private static final int pageSize = 10;
    @BindView(R2.id.srl_refresh)
    MySwipeRefreshLayout swipeRefreshLayout;

    @BindView(R2.id.lv_mail)
    ListView mailListView;

    @BindView(R2.id.tv_header)
    TextView headerText;

    @BindView(R2.id.rl_mail_operation)
    RelativeLayout mailOperationLayout;

    private MailListAdapter mailAdapter;
    private MailAPIService apiService;
    private MailFolder currentMailFolder;
    private MailFolder currentRootMailFolder;
    private List<Mail> mailList = new ArrayList<>();
    private List<Mail> mailSelectList = new ArrayList<>();
    private ContactUser contactUser;


    @Override
    public void onCreate() {
        super.onCreate();
        ContactService contactService = Router.getInstance().getService(ContactService.class);
        if (contactService != null) {
            contactUser = contactService.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
        initView();
    }

    private void initView() {
        mailAdapter = new MailListAdapter(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        apiService = new MailAPIService(this);
        apiService.setAPIInterface(new WebService());
        mailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mail mail = mailList.get(position);
                if (mailAdapter.getSelectMode()) {
                    if (mailSelectList.contains(mail)) {
                        mailSelectList.remove(mail);
                    } else {
                        mailSelectList.add(mail);
                    }
                    mailAdapter.notifyDataSetChanged();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(MailDetailActivity.EXTRA_MAIL, mail);
                    IntentUtils.startActivity(MailHomeActivity.this, MailDetailActivity.class, bundle);
                }
            }
        });
        mailListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (!mailAdapter.getSelectMode()) {
                    mailAdapter.setSelectMode(true);
                    mailOperationLayout.setVisibility(View.VISIBLE);
                    Mail mail = mailList.get(position);
                    mailSelectList.add(mail);
                    mailAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        mailListView.setAdapter(mailAdapter);

    }

    /**
     * @param getMailListResult
     * @return
     */
    private List<Mail> updateMailStatusOfDb(GetMailListResult getMailListResult) {
        List<Mail> requestMailList = getMailListResult.getMailList();
        if (requestMailList.size() > 0) {
            List<String> mailIdList = new ArrayList<>();
            for (Mail mail : requestMailList) {
                mailIdList.add(mail.getId());
            }
            List<Mail> requestMailInDbList = MailCacheUtils.getMailListByMailIdList(mailIdList);
            for (int i = 0; i < requestMailList.size(); i++) {
                Mail mail = requestMailList.get(i);
                int index = requestMailInDbList.indexOf(mail);
                if (index != -1) {
                    Mail mailInDb = requestMailInDbList.get(index);
                    mailInDb.setFolderId(mail.getFolderId());
                    mailInDb.setRead(mail.isRead());
                    requestMailList.remove(i);
                    requestMailList.add(i, mailInDb);
                }
            }
        }
        return requestMailList;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveGetMailByFolder(SimpleEventMessage simpleEventMessage) {
        switch (simpleEventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_GET_MAIL_BY_FOLDER:
                closeMenu();
                MailFolder mailFolder = (MailFolder) simpleEventMessage.getMessageObj();
                if (currentMailFolder == null || !mailFolder.getId().equals(currentMailFolder.getId())) {
                    mailList.clear();
                    mailSelectList.clear();
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
                    List<Mail> mailListInDb = MailCacheUtils.getMailListInFolder(currentMailFolder.getId(), pageSize);
                    mailList.addAll(mailListInDb);
                    mailAdapter.setMailList(mailList, currentRootMailFolder, mailSelectList);
                    mailAdapter.setSelectMode(false);
                    swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
                    mailAdapter.notifyDataSetChanged();
                    getMail(0);
                }
                break;
            case Constant.EVENTBUS_TAG_MAIL_ACCOUNT_DELETE:
                finish();
                break;
            case Constant.EVENTBUS_TAG_MAIL_REMOVE:
                Mail mail = (Mail) simpleEventMessage.getMessageObj();
                List<Mail> deleteMailList = new ArrayList<>();
                deleteMailList.add(mail);
                removeMailListFromLocal(deleteMailList);
                break;
        }
    }

    /**
     * 本地删除邮件
     *
     * @param
     */
    private void removeMailListFromLocal(List<Mail> deleteMailList) {
        if (deleteMailList != null && deleteMailList.size() > 0) {
            MailCacheUtils.deleteMailList(deleteMailList);
            mailList.removeAll(deleteMailList);
            mailAdapter.setSelectMode(false);
            mailAdapter.notifyDataSetChanged();
            mailOperationLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onBackPressed() {
        //当点击返回键时取消选中状态
        if (mailAdapter.getSelectMode()) {
            mailAdapter.setSelectMode(false);
            mailSelectList.clear();
            mailOperationLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
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

    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.rl_delete) {
            removeMail();

        } else {
        }
    }

    private void getMail(int offset) {
        if (NetUtils.isNetworkConnected(this) && currentMailFolder != null) {
            apiService.getMailList(currentMailFolder.getId(), pageSize, offset);
        } else {
            swipeRefreshLayout.setLoading(false);
        }

    }

    private void removeMail() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDlg.show();
            JSONObject object = new JSONObject();
            object.put("Email", contactUser != null ? contactUser.getEmail() : "");
            object.put("DeleteMode", 2);
            JSONArray array = new JSONArray();
            for (Mail mail : mailSelectList) {
                array.add(mail.getId());
            }
            object.put("ItemIds", array);
            apiService.removeMail(object.toJSONString());
        }
    }

    private class WebService extends MailAPIInterfaceImpl {
        @Override
        public void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult) {
            swipeRefreshLayout.setLoading(false);
            List<Mail> requestMailList = updateMailStatusOfDb(getMailListResult);
            MailCacheUtils.saveMailList(requestMailList);
            if (folderId.equals(currentMailFolder.getId())) {
                if (offset == 0) {
                    mailList.clear();
                }
                mailList.addAll(requestMailList);
                mailAdapter.setMailList(mailList, currentRootMailFolder, mailSelectList);
                swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
                mailAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode) {
            swipeRefreshLayout.setLoading(false);
        }

        @Override
        public void returnRemoveMailSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(MailHomeActivity.this, "邮件删除成功");
            removeMailListFromLocal(mailSelectList);
        }

        @Override
        public void returnRemoveMailFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MailHomeActivity.this, error, errorCode);
        }
    }
}
