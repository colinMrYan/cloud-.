package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactProtoBuf;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录页面的基类，进入应用当通讯录获取不成功，再进入通讯录页面会进行重新获取
 */

public class ContactSearchBaseFragment extends BaseFragment {
    protected static final int REFRESH_DATA = 6;
    private static final int DATA_READY = 7;
    protected Handler handler;
    protected List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    protected List<Contact> searchContactList = new ArrayList<>(); // 通讯录搜索结果
    protected List<SearchModel> threadSearchChannelGroupList = new ArrayList<>(); // 线程中的群组搜索结果
    protected List<Contact> threadSearchContactList = new ArrayList<>(); // 线程中的通讯录搜索结果
    private LoadingDialog loadingDlg;
    private boolean isContactUserReady = false;
    private boolean isContactOrgReady = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDlg = new LoadingDialog(getActivity());
        handMessage();
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false) && MyApplication.getInstance()
                .getIsContactReady()) {
            long contactUserLastQureryTime = ContactUserCacheUtils.getLastQueryTime();
            long contactOrgLastQureryTime = ContactOrgCacheUtils.getLastQueryTime();
            isContactUserReady = (contactUserLastQureryTime != 0);
            isContactOrgReady = (contactOrgLastQureryTime != 0);
            if (!isContactUserReady) {
                getContactUser();
            }
            if (!isContactOrgReady) {
                getContactOrg();
            }
        }
    }

    private void handMessage() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case REFRESH_DATA:
                        searchChannelGroupList.clear();
                        searchChannelGroupList.addAll(threadSearchChannelGroupList);
                        searchContactList.clear();
                        searchContactList.addAll(threadSearchContactList);
                        showSearchPop();
                        break;
                    case DATA_READY:
                        setLoadingDlgDimiss();
                        break;
                }
            }

        };
    }

    protected void showSearchPop() {
    }


    private void setLoadingDlgDimiss() {
        if (isContactUserReady && isContactOrgReady) {
            LoadingDialog.dimissDlg(loadingDlg);
            notifySyncAllBaseDataSuccess();
        }
    }

    /**
     * 通讯录完成时发送广播
     */
    private void notifySyncAllBaseDataSuccess() {
        // TODO Auto-generated method stub
        //当通讯录完成时需要刷新头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sync_all_base_data_success");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }


    /**
     * 获取通讯录人员信息
     */
    private void getContactUser() {
        // TODO Auto-generated method stub
        if (!loadingDlg.isShowing()) {
            loadingDlg.show();
        }
        String saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER);
        ContactAPIService apiService = new ContactAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        apiService.getContactUserList(saveConfigVersion);
    }

    /**
     * 获取通讯录人员信息
     */
    private void getContactOrg() {
        // TODO Auto-generated method stub
        if (!loadingDlg.isShowing()) {
            loadingDlg.show();
        }
        String saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG);
        ContactAPIService apiService = new ContactAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        apiService.getContactOrgList(saveConfigVersion);
    }


    class CacheContactUserThread extends Thread {
        private byte[] result;
        private String saveConfigVersion;

        public CacheContactUserThread(byte[] result, String saveConfigVersion) {
            this.result = result;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.users users = ContactProtoBuf.users.parseFrom(result);
                List<ContactProtoBuf.user> userList = users.getUsersList();
                List<ContactUser> contactUserList = ContactProtoBuf.protoBufUserList2ContactUserList(userList, users.getLastQueryTime());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                ContactUserCacheUtils.setLastQueryTime(users.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_USER, saveConfigVersion);
                isContactUserReady = true;
                if (handler != null) {
                    handler.sendEmptyMessage(DATA_READY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CacheContactOrgThread extends Thread {
        private byte[] result;
        private String saveConfigVersion;

        public CacheContactOrgThread(byte[] result, String saveConfigVersion) {
            this.result = result;
            this.saveConfigVersion = saveConfigVersion;
        }

        @Override
        public void run() {
            try {
                ContactProtoBuf.orgs orgs = ContactProtoBuf.orgs.parseFrom(result);
                List<ContactProtoBuf.org> orgList = orgs.getOrgsList();
                List<ContactOrg> contactOrgList = ContactOrg.protoBufOrgList2ContactOrgList(orgList);
                ContactOrgCacheUtils.saveContactOrgList(contactOrgList);
                ContactOrgCacheUtils.setContactOrgRootId(orgs.getRootID());
                ContactOrgCacheUtils.setLastQueryTime(orgs.getLastQueryTime());
                ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG, saveConfigVersion);
                isContactOrgReady = true;
                if (handler != null) {
                    handler.sendEmptyMessage(DATA_READY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public class WebService extends APIInterfaceInstance {
        @Override
        public void returnContactOrgListSuccess(byte[] bytes, String saveConfigVersion) {
            new CacheContactOrgThread(bytes, saveConfigVersion).start();
        }

        @Override
        public void returnContactOrgListFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            isContactOrgReady = true;
            setLoadingDlgDimiss();
        }

        @Override
        public void returnContactUserListFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            isContactUserReady = true;
            setLoadingDlgDimiss();
        }

        @Override
        public void returnContactUserListSuccess(byte[] bytes, String saveConfigVersion) {
            new CacheContactUserThread(bytes, saveConfigVersion).start();
        }
    }
}
