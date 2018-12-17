package com.inspur.emmcloud.ui.appcenter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by libaochao on 2018/12/17.
 */

public class BatteryNoWarnDialog extends Dialog {

    private Context context;
    private String title;
    private String confirmButtonText;
    private String cacelButtonText;
    private TextView tvTitle;
    private TextView tvConfirm;
    private TextView tvCancel;
    private CheckBox isSele;
    private ClickListenerInterface clickListenerInterface;
    public interface ClickListenerInterface { //在外部使用的点击事件接口
        public void doConfirm();//确定的事件
        public void doCancel();//取消的事件
        void setIsSelect(boolean isChecked);//cheBox的状态
    }

    public BatteryNoWarnDialog(@NonNull Context context, String title, String confirmButtonText, String cacelButtonText) {
       super(context,R.style.PowerNoWarnDialog);
        this.context=context;
        this.title = title;
        this.confirmButtonText = confirmButtonText;
        this.cacelButtonText = cacelButtonText;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_power_no_warn, null);
        setContentView(view);
        tvTitle = (TextView) view.findViewById(R.id.title);
        tvConfirm = (TextView) view.findViewById(R.id.confirm);
        tvCancel = (TextView) view.findViewById(R.id.cancel);
        isSele = (CheckBox) view.findViewById(R.id.cb_issele);

        tvTitle.setText(title);
        tvConfirm.setText(confirmButtonText);
        tvCancel.setText(cacelButtonText);

        tvConfirm.setOnClickListener(new clickListener());
        tvCancel.setOnClickListener(new clickListener());
        isSele.setOnCheckedChangeListener(new clickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }
    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class clickListener implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.confirm:
                    clickListenerInterface.doConfirm();
                    break;
                case R.id.cancel:
                    clickListenerInterface.doCancel();
                    break;
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            clickListenerInterface.setIsSelect(isChecked);
        }
    };


    public BatteryNoWarnDialog(@NonNull Context context, String 补发试卷, String 立即补发, String 下次再说, boolean b) {
        super( context );
    }

    public BatteryNoWarnDialog(@NonNull Context context, int themeResId) {
        super( context, themeResId );
    }

    protected BatteryNoWarnDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super( context, cancelable, cancelListener );
    }



}
