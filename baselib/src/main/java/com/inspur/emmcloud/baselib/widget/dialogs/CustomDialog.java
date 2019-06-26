package com.inspur.emmcloud.baselib.widget.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;

import java.lang.reflect.Field;

public class CustomDialog extends AlertDialog {
    public static float btnTextSize = 14;
    public static int btnTextColor = Color.parseColor("#36A5F6");
    Context mContext;

    public CustomDialog(Context context) {
        this(context, R.style.cus_dialog_style);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init();
    }

    private void init() {
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialogWidth();
    }

    private void initDialogWidth() {
        Window window = getWindow();
        if (window == null) return;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    private static void setTitleTvAttr(AlertDialog dialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    public static class MessageDialogBuilder extends BaseDialogBuilder {
        Context context;

        public MessageDialogBuilder(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public AlertDialog show() {
            AlertDialog dialog = super.show();
            setActionBtnAttr(dialog);
            return dialog;
        }
    }

    /**
     * 菜单列表类型的对话框 Builder
     */
    public static class ListDialogBuilder extends BaseDialogBuilder {
        private Context context;

        public ListDialogBuilder(Context context) {
            super(context);
            this.context = context;
        }

        public ListDialogBuilder(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        public AlertDialog show() {
            AlertDialog dialog = super.show();
            return dialog;
        }
    }

    /**
     * 单选类型的对话框 Builder
     */
    public static class SingleChoiceDialogBuilder extends BaseDialogBuilder {
        private Context context;

        public SingleChoiceDialogBuilder(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public AlertDialog show() {
            AlertDialog dialog = super.show();
            return dialog;
        }
    }

    /**
     * 编辑框builder
     */
    public static class EditDialogBuilder extends BaseDialogBuilder {
        private Context context;

        public EditDialogBuilder(Context context) {
            super(context);
            this.context = context;
        }
    }

    private static void setMessageTvAttr(AlertDialog dialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageTv = (TextView) mMessage.get(mAlertController);
            int left = DensityUtil.dip2px(24);
            int top = DensityUtil.dip2px(10);
            int right = DensityUtil.dip2px(24);
            int bottom = DensityUtil.dip2px(20);
            mMessageTv.setPadding(left, top, right, bottom);
            mMessageTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void setActionBtnAttr(AlertDialog dialog) {
        Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        int btnPadding = DensityUtil.dip2px(12);
        positiveBtn.setPadding(btnPadding, 0, btnPadding, btnPadding);
        positiveBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, btnTextSize);
        positiveBtn.setTextColor(btnTextColor);
        negativeBtn.setPadding(btnPadding, btnPadding, btnPadding, btnPadding);
        negativeBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, btnTextSize);
        negativeBtn.setTextColor(btnTextColor);
    }

    public static class BaseDialogBuilder extends AlertDialog.Builder {
        private Context context;

        public BaseDialogBuilder(Context context, int themeResId) {
            super(context, themeResId);
            this.context = context;

        }

        public BaseDialogBuilder(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public AlertDialog show() {
            AlertDialog dialog = super.show();
            dialog.getWindow().setLayout(ResolutionUtils.getWidth(context) / 5 * 4, ViewGroup.LayoutParams.WRAP_CONTENT);
            setTitleTvAttr(dialog);
            setMessageTvAttr(dialog);
            return dialog;
        }
    }
}
