package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.mail.MailFolder;
import com.inspur.emmcloud.util.privates.cache.MailFolderCacheUtils;
import com.unnamed.b.atv.model.TreeNode;

import java.util.List;

/**
 * Created by chenmch on 2018/12/25.
 */

public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<MailFolder> {
    private ImageView arrowImg;
    private ImageView iconImg;
    private TextView nameText;

    public IconTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final MailFolder mailFolder) {
        final View view = LayoutInflater.from(context).inflate(R.layout.mail_folder_item_view, null, false);
        arrowImg = (ImageView) view.findViewById(R.id.iv_arrow);
        iconImg = (ImageView) view.findViewById(R.id.iv_folder_icon);
        nameText = (TextView) view.findViewById(R.id.tv_folder_name);
        nameText.setText(mailFolder.getDisplayName());
        iconImg.setImageResource(getMailFolderIconRes(mailFolder));

        if (mailFolder.getChildFolderCount() == 0) {
            arrowImg.setVisibility((node.getChildren().size() == 0) ? View.INVISIBLE : View.VISIBLE);
        }
        arrowImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (node.getChildren().size() == 0) {
                    List<MailFolder> ChildMailFolderList = MailFolderCacheUtils.getChildMailFolderList(mailFolder.getId());
                    for (MailFolder mailFolder : ChildMailFolderList) {
                        TreeNode childTreeNode = new TreeNode(mailFolder);
                        node.addChild(childTreeNode);
                    }
                }
                getTreeView().toggleNode(node);


            }
        });
        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrowImg.setImageResource(active ? R.drawable.ic_mail_folder_collapse : R.drawable.ic_mail_folder_expand);
    }

    public int getMailFolderIconRes(MailFolder mailFolder) {
        int res = -1;
        switch (mailFolder.getFolderType()) {
            case 0:
                res = R.drawable.ic_mail_drafts;
                break;
            case 1:
                res = R.drawable.ic_mail_inbox;
                break;
            case 2:
                res = R.drawable.ic_mail_outbox;
                break;
            case 3:
                res = R.drawable.ic_mail_sent;
                break;
            case 4:
                res = R.drawable.ic_mail_deletedbox;
                break;
            case 5:
                res = R.drawable.ic_mail_dustbin;
                break;
            default:
                res = R.drawable.ic_mail_folder;
                break;
        }
        return res;

    }
}