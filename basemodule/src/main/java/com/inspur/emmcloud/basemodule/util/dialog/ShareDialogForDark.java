package com.inspur.emmcloud.basemodule.util.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;

/**
 * 分享弹框，代码适配暗黑，用于继承BaseFragmentActivity基类使用
 */
public class ShareDialogForDark {
    CallBack callBack;
    MyDialog dialog;
    private Context context;
    private String userName;
    private String headUrl;
    private String content;
    private int defaultResId;

    private ShareDialogForDark(Builder builder) {
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
        LinearLayout bgLl = dialog.findViewById(R.id.ll_bg);
        TextView sendTv = dialog.findViewById(R.id.tv_send);
        View yView = dialog.findViewById(R.id.view_Y);
        View xView = dialog.findViewById(R.id.view_X);
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
        int currentThemeNo = PreferencesUtils.getInt(context, "app_theme_num_v1", 0);
        bgLl.setBackgroundColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.color_36) :
                context.getResources().getColor(R.color.button_color_nor));
        fileNameText.setBackgroundColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.content_bg_dark_30) :
                context.getResources().getColor(R.color.content_bg_level_two));
        yView.setBackgroundColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.color_divider_38) :
                context.getResources().getColor(R.color.color_list_divider));
        xView.setBackgroundColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.color_divider_38) :
                context.getResources().getColor(R.color.color_list_divider));
        sendTv.setTextColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.header_text_e1) :
                context.getResources().getColor(R.color.text_color));
        sendTv.setTextColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.header_text_e1) :
                context.getResources().getColor(R.color.text_color));
        userNameText.setTextColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.header_text_e1) :
                context.getResources().getColor(R.color.text_color));
        fileNameText.setTextColor(currentThemeNo == 3 ? context.getResources().getColor(R.color.header_text_e1) :
                context.getResources().getColor(R.color.text_color));

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

        public ShareDialogForDark build() {
            return new ShareDialogForDark(this);
        }
    }
}
