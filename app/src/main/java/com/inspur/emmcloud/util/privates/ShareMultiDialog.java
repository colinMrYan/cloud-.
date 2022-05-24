package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ConversationSendMultiDialogAdapter;

import java.util.List;

/**
 * 消息发送多人弹框
 */
public class ShareMultiDialog {
    private List<MessageForwardMultiBean> searchModelList;
    CallBack callBack;
    MyDialog dialog;
    private Context context;
    private String content;

    private ShareMultiDialog(Builder builder) {
        context = builder.context;
        content = builder.content;
        searchModelList = builder.searchModelList;
        dialog = new MyDialog(context, R.layout.chat_out_share_multi_sure_dialog);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public void show() {
        if (dialog == null) return;
        Button okBtn = dialog.findViewById(R.id.ok_btn);
        TextView fileNameText = dialog.findViewById(R.id.tv_share_file_name);
        NoScrollGridView sendMultiGv = dialog.findViewById(R.id.gv_send_multi);
        dialog.setCancelable(false);
        okBtn.setText(context.getString(R.string.ok));
        fileNameText.setText(content);
        ConversationSendMultiDialogAdapter adapter = new ConversationSendMultiDialogAdapter(context, searchModelList);
        Configuration configuration = context.getResources().getConfiguration();
        // 适配横屏头像显示
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sendMultiGv.setNumColumns(9);
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            sendMultiGv.setNumColumns(6);
        }
        sendMultiGv.setAdapter(adapter);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onConfirm(v);
            }
        });
        Button cancelBt = dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(context.getString(R.string.cancel));
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onCancel();
            }
        });
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public interface CallBack {
        void onConfirm(View view);

        void onCancel();
    }

    public static class Builder {
        private Context context;
        private String content;
        private List<MessageForwardMultiBean> searchModelList;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setConversationList(List<MessageForwardMultiBean> searchModelList) {
            this.searchModelList = searchModelList;
            return this;
        }

        public ShareMultiDialog build() {
            return new ShareMultiDialog(this);
        }
    }
}
