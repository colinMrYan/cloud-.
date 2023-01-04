package com.inspur.emmcloud.basemodule.util.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;

/**
 * 分享通用弹框
 */
public class ShareDialog {
    CallBack callBack;
    MyDialog dialog;
    private Context context;
    private String userName;
    private String headUrl;
    private String content;
    private int defaultResId;

    private ShareDialog(Builder builder) {
        context = builder.context;
        userName = builder.userName;
        headUrl = builder.headUrl;
        content = builder.content;
        defaultResId = builder.defaultResId;
        dialog = new MyDialog(context, R.layout.chat_out_share_sure_dialog);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public void show() {
        if (dialog == null) return;
        Button okBtn = dialog.findViewById(R.id.ok_btn);
        ImageViewRound userHeadImage = dialog.findViewById(R.id.iv_share_user_head);
        TextView fileNameText = dialog.findViewById(R.id.tv_share_file_name);
        TextView userNameText = dialog.findViewById(R.id.tv_share_user_name);
        dialog.setCancelable(false);
        ImageDisplayUtils.getInstance().displayImageNoCache(userHeadImage, headUrl, defaultResId);
        okBtn.setText(context.getString(R.string.ok));
        userNameText.setText(userName);
        fileNameText.setText(content);
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
        private String userName;
        private String headUrl;
        private String content;
        private int defaultResId;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setUserName(String name) {
            this.userName = name;
            return this;
        }

        public Builder setHeadUrl(String url) {
            this.headUrl = url;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setDefaultResId(int resId) {
            this.defaultResId = resId;
            return this;
        }

        public ShareDialog build() {
            return new ShareDialog(this);
        }
    }
}
