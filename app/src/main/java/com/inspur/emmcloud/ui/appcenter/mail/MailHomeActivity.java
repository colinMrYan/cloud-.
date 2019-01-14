package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.cache.MailCacheUtils;
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

    private static final int pageSize = 10;
    @ViewInject(R.id.srl_refresh)
    private MySwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.lv_mail)
    private ListView mailListView;

    @ViewInject(R.id.tv_header)
    private TextView headerText;

    @ViewInject(R.id.rl_home_long_click )
    private RelativeLayout homeLongClickLayout;

    private MailListAdapter mailAdapter;
    private MailApiService apiService;
    private MailFolder currentMailFolder;
    private MailFolder currentRootMailFolder;
    private List<Mail> mailList = new ArrayList<>();
    private boolean    isItemClickable=true;
    private List<String> needDelectItemId=new ArrayList<>();

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
                if(isItemClickable){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(MailDetailActivity.EXTRA_MAIL,mail);
                    IntentUtils.startActivity(MailHomeActivity.this,MailDetailActivity.class,bundle);
                }else{
                      if(mail.isDelectItem()){
                          mailList.get( position ).setDelectItem(false);
                      }else {
                          needDelectItemId.add(mail.getId());
                          mailList.get( position ).setDelectItem(true);
                      }
                    mailAdapter.setMailList( mailList,currentRootMailFolder);//?
                    mailAdapter.notifyDataSetChanged();
                }
            }
        });
        mailListView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                homeLongClickLayout.setVisibility(View.VISIBLE);
                Mail mail = mailList.get(position);
                for(int j=0;j<mailList.size();j++){
                    mailList.get(j).setHideLeftCheck(false);
                }
                if(mail.isDelectItem()){
                    mailList.get(position).setDelectItem(false);
                }else {
                    needDelectItemId.add(mail.getId());
                    mailList.get(position).setDelectItem(true);
                }
                mailAdapter.setMailList( mailList,currentRootMailFolder);  //?
                mailAdapter.notifyDataSetChanged();
                isItemClickable = false;
                return true;
            }
        } );

        mailListView.setAdapter(mailAdapter);

    }

    private List<Mail>  updateMailStatusFromDb(GetMailListResult getMailListResult){
        List<Mail> requestMailList = getMailListResult.getMailList();
        if (requestMailList.size()>0){
            List<String> mailIdList =new ArrayList<>();
            for (Mail mail:requestMailList){
                mailIdList.add(mail.getId());
            }
            List<Mail> requestMailInDbList = MailCacheUtils.getMailListByMailIdList(mailIdList);
            for (int i=0;i<requestMailList.size();i++){
                Mail mail = requestMailList.get(i);
                int index = requestMailInDbList.indexOf(mail);
                if (index != -1){
                    Mail mailInDb = requestMailInDbList.get(index);
                    mailInDb.setFolderId(mail.getFolderId());
                    mailInDb.setRead(mail.isRead());
                    requestMailList.remove(i);
                    requestMailList.add(i,mailInDb);
                }
            }
        }
        return requestMailList;
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
               mailList = MailCacheUtils.getMailListInFolder(currentMailFolder.getId(),pageSize);
               mailAdapter.setMailList(mailList, currentRootMailFolder);
               swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
               mailAdapter.notifyDataSetChanged();
               getMail(0);
           }
        } else if(simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_DELECTE_MAIL_HOME_ACTIVITY)){
             finish();
        } else if(simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_MAIL_REMOVE_SUCCESS)){
            //1、那个文件夹下的那个邮件删除
            LogUtils.LbcDebug( "删除更新UI" );
              List<String>  removeMailIdList=(List<String>)simpleEventMessage.getMessageObj();
//              MailCacheUtils.removeMailListByMailIdList(removeMailIdList);
//            //2、更新UI
//             mailList = MailCacheUtils.getMailListInFolder(currentMailFolder.getId(),pageSize);
//            mailAdapter.setMailList(mailList, currentRootMailFolder);
//            swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
//            mailAdapter.notifyDataSetChanged();
              removeMailById( removeMailIdList );
        }
    }

    private void removeMailById(List<String> mailIdList){
        MailCacheUtils.removeMailListByMailIdList(mailIdList);
        //2、更新UI
        mailList = MailCacheUtils.getMailListInFolder(currentMailFolder.getId(),pageSize);
        if(isItemClickable){
            for (int i=0;i<mailList.size();i++){
                mailList.get( i ).setHideLeftCheck( false );
            }
        }
        mailAdapter.setMailList(mailList, currentRootMailFolder);
        swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
        mailAdapter.notifyDataSetChanged();
        needDelectItemId.clear();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    if(keyCode == KeyEvent.KEYCODE_BACK&&View.VISIBLE==homeLongClickLayout.getVisibility()){
        homeLongClickLayout.setVisibility(View.GONE);
        for(int i=0;i<mailList.size();i++){
            mailList.get(i).setHideLeftCheck(true);
        }
        mailAdapter.setMailList( mailList,currentMailFolder );
        mailAdapter.notifyDataSetChanged();
        isItemClickable=true;
        return true;
    }
     return super.onKeyDown( keyCode, event );
    }

    @Override
    public void onRefresh() {
        if(isItemClickable)
        getMail(0);
    }

    @Override
    public void onLoadMore() {
        getMail(mailList.size());
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_delete_select_item:
                ToastUtils.show( this,"删除Id个数"+needDelectItemId.size() );
                removeMailById(needDelectItemId);
                ToastUtils.show( this,"删除Id个数"+needDelectItemId.size() );

                break;
            default:
                break;
        }
    }

    private void getMail(int offset) {
        if (NetUtils.isNetworkConnected(this) && currentMailFolder != null) {
            apiService.getMailList(currentMailFolder.getId(), pageSize, offset);
        } else {
            swipeRefreshLayout.setLoading(false);
        }

    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult) {
            swipeRefreshLayout.setLoading(false);
            List<Mail> requestMailList = updateMailStatusFromDb(getMailListResult);
            MailCacheUtils.saveMailList(requestMailList);
            if (folderId.equals(currentMailFolder.getId())) {
                if (offset == 0) {
                    mailList.clear();
                }
                if(!isItemClickable){
                    for(int i=0;i<requestMailList.size();i++){
                        requestMailList.get( i ).setHideLeftCheck(false);
                    }
                }
                mailList.addAll(requestMailList);
                mailAdapter.setMailList(mailList, currentRootMailFolder);
                swipeRefreshLayout.setCanLoadMore(mailList.size() >= pageSize);
                mailAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode) {
            swipeRefreshLayout.setLoading(false);
        }
    }
}
