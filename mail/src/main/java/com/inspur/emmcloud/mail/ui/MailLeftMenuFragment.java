package com.inspur.emmcloud.mail.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;
import com.inspur.emmcloud.mail.api.MailAPIService;
import com.inspur.emmcloud.mail.bean.GetMailFolderResult;
import com.inspur.emmcloud.mail.bean.MailFolder;
import com.inspur.emmcloud.mail.util.MailFolderCacheUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailLeftMenuFragment extends Fragment {
    private LoadingDialog loadingDialog;
    private MailAPIService apiService;
    private AndroidTreeView treeView;
    private RelativeLayout containerLayout;
    private TextView mailAcountText;
    private boolean hasOpenFirstMailFolder = false;
    private ContactUser contactUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = new MailAPIService(getActivity());
        if (Router.getInstance().getService(ContactService.class) != null) {
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            contactUser = contactService.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
        apiService.setAPIInterface(new WebService());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mail_left_menu, null);
        loadingDialog = new LoadingDialog(getActivity());
        containerLayout = view.findViewById(R.id.rl_container);
        mailAcountText = view.findViewById(R.id.tv_mail_acount);
        mailAcountText.setText(contactUser != null ? contactUser.getEmail() : "");
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
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            //loadingDialog.show();
            apiService.getMailFolder();
        }
    }


    private class WebService extends MailAPIInterfaceImpl {
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
