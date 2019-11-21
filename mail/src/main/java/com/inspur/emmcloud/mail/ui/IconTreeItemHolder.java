package com.inspur.emmcloud.mail.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.bean.MailFolder;
import com.inspur.emmcloud.mail.util.MailFolderCacheUtils;
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
        arrowImg.setImageResource(active ? R.drawable.mail_folder_collapse_ic : R.drawable.mail_folder_expand_ic);
    }

    public int getMailFolderIconRes(MailFolder mailFolder) {
        int res = -1;
        switch (mailFolder.getFolderType()) {
            case 0:
                res = R.drawable.mail_drafts_ic;
                break;
            case 1:
                res = R.drawable.mail_inbox_ic;
                break;
            case 2:
                res = R.drawable.mail_outbox_ic;
                break;
            case 3:
                res = R.drawable.mail_sent_ic;
                break;
            case 4:
                res = R.drawable.mail_deletedbox_ic;
                break;
            case 5:
                res = R.drawable.mail_dustbin_ic;
                break;
            default:
                res = R.drawable.mail_folder_ic;
                break;
        }
        return res;

    }
}