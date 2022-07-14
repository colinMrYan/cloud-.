package com.inspur.emmcloud.basemodule.media.selector.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;

/**
 * @author：luck
 * @date：2021/11/19 5:11 下午
 * @describe：RemindDialog 公用提示dialog
 */
public class RemindDialog extends Dialog implements View.OnClickListener {
    private final TextView btnOk;
    private final TextView tvContent;

    public RemindDialog(Context context, String tips) {
        super(context, R.style.Picture_Theme_Dialog);
        setContentView(R.layout.common_remind_dialog);
        LinearLayout rootLl = findViewById(R.id.ll_root);
        btnOk = findViewById(R.id.btnOk);
        tvContent = findViewById(R.id.tv_content);
        View lineBottom = findViewById(R.id.bottom_line);
        tvContent.setText(tips);
        rootLl.setBackground(DarkUtil.isDarkTheme() ? context.getDrawable(R.drawable.ps_dialog_shadow_dark) :
                context.getDrawable(R.drawable.ps_dialog_shadow));
        lineBottom.setBackgroundColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#383838" : "#dddddd"));
        btnOk.setTextColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#FFFFFF" : "#333333"));
        tvContent.setTextColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#FFFFFF" : "#333333"));
        btnOk.setOnClickListener(this);
        setDialogSize();
    }

    @Deprecated
    public static Dialog showTipsDialog(Context context, String tips) {
        return new RemindDialog(context, tips);
    }

    public static RemindDialog buildDialog(Context context, String tips) {
        return new RemindDialog(context, tips);
    }

    public void setButtonText(String text) {
        btnOk.setText(text);
    }

    public void setButtonTextColor(int color) {
        btnOk.setTextColor(color);
    }

    public void setContent(String text) {
        tvContent.setText(text);
    }

    public void setContentTextColor(int color) {
        tvContent.setTextColor(color);
    }

    private void setDialogSize() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        getWindow().setWindowAnimations(R.style.PictureThemeDialogWindowStyle);
        getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnOk) {
            if (listener != null) {
                listener.onClick(view);
            } else {
                dismiss();
            }
        }
    }

    private OnDialogClickListener listener;

    public void setOnDialogClickListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    public interface OnDialogClickListener {
        void onClick(View view);
    }
}
