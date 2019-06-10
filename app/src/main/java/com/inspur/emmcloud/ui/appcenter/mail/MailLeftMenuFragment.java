package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailFolderResult;
import com.inspur.emmcloud.bean.appcenter.mail.MailFolder;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MailFolderCacheUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailLeftMenuFragment extends Fragment {
    private LoadingDialog loadingDialog;
    private MailApiService apiService;
    private AndroidTreeView treeView;
    private RelativeLayout containerLayout;
    private TextView mailAcountText;
    private boolean hasOpenFirstMailFolder = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = new MailApiService(getActivity());
        apiService.setAPIInterface(new WebService());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mail_left_menu, null);
        loadingDialog = new LoadingDialog(getActivity());
        containerLayout = view.findViewById(R.id.rl_container);
        mailAcountText = view.findViewById(R.id.tv_mail_acount);
        mailAcountText.setText(ContactUserCacheUtils.getUserMail(MyApplication.getInstance().getUid()));
        addTreeView();
        getMailFolder();
        return view;
    }

    private void addTreeView() {
        List<MailFolder> rootChildMailFolderList = MailFolderCacheUtils.getChildMailFolderList("");
        if (rootChildMailFolderList.size() > 0) {
            if (!hasOpenFirstMailFolder) {
                openMailFolder(rootChildMailFolderList.get(0));
                hasOpenFirstMailFolder = true;
            }
            containerLayout.removeAllViews();
            containerLayout.removeAllViewsInLayout();
            TreeNode root = TreeNode.root();
            for (MailFolder mailFolder : rootChildMailFolderList) {
                TreeNode treeNode = new TreeNode(mailFolder);
                root.addChild(treeNode);
            }
            treeView = new AndroidTreeView(getActivity(), root);
            treeView.setDefaultAnimation(true);
            treeView.setUseAutoToggle(false);
            treeView.setDefaultContainerStyle(R.style.AndroidTreeNodeStyleCustom);
            treeView.setDefaultViewHolder(IconTreeItemHolder.class);
            treeView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener() {
                @Override
                public void onClick(TreeNode node, Object value) {
                    MailFolder mailFolder = (MailFolder) value;
                    openMailFolder(mailFolder);

                }
            });
            containerLayout.addView(treeView.getView());
        }

    }

    private void openMailFolder(MailFolder mailFolder) {
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_GET_MAIL_BY_FOLDER, mailFolder));
    }


    private void getMailFolder() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            //loadingDialog.show();
            apiService.getMailFolder();
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailFolderSuccess(GetMailFolderResult getMailfolderResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            List<MailFolder> mailFolderList = getMailfolderResult.getMailFolderList();
            MailFolderCacheUtils.saveMailFolderList(mailFolderList);
            addTreeView();

        }

        @Override
        public void returnMailFolderFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
        }
    }
}
