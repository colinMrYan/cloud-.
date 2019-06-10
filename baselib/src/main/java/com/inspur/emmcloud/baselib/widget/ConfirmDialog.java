package com.inspur.emmcloud.baselib.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inspur.baselib.R;


/**
 * Created by libaochao on 2018/12/18.
 */

public class ConfirmDialog extends Dialog {

    private Context context;
    private int confirmButtonTextId;
    private int cancelButtonTextId;
    private int tipContentId;
    private int tipHideHintId;
    private ClickListenerInterface clickListenerInterface;
    private CheckBox hideCheckBox;
    private boolean isHide = false;

    /**
     * @param context
     * @param cacelButtonTextId
     * @param confirmButtonTextId
     * @param tipContentId
     * @param tipHideHintId
     */
    public ConfirmDialog(Context context, int tipContentId, int tipHideHintId, int confirmButtonTextId, int cacelButtonTextId) {
        super(context);
        this.context = context;
        this.tipContentId = tipContentId;
        this.tipHideHintId = tipHideHintId;
        this.confirmButtonTextId = confirmButtonTextId;
        this.cancelButtonTextId = cacelButtonTextId;
    }

    public boolean getIsHide() {
        return isHide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.basewidget_dialog_confirm, null);
        setContentView(view);

        TextView tipContentText = (TextView) view.findViewById(R.id.tv_tip_content);
        TextView cancelText = (TextView) view.findViewById(R.id.tv_cancel);
        TextView setText = (TextView) view.findViewById(R.id.tv_toset);
        hideCheckBox = (CheckBox) view.findViewById(R.id.cb_ishide);
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHide = isChecked;
            }
        });

        tipContentText.setText(tipContentId);
        hideCheckBox.setText(tipHideHintId);
        cancelText.setText(cancelButtonTextId);
        setText.setText(confirmButtonTextId);
        cancelText.setOnClickListener(new ClickListener());
        setText.setOnClickListener(new ClickListener());
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (displayMetrics.widthPixels * 0.9); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    public interface ClickListenerInterface {
        public void doConfirm();

        public void doCancel();
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            if (id == R.id.tv_toset) {
                clickListenerInterface.doConfirm();
            } else if (id == R.id.tv_cancel) {
                clickListenerInterface.doCancel();
            }
        }
    }
}
