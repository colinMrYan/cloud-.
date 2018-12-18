package com.inspur.emmcloud.widget.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by libaochao on 2018/12/18.
 */

public class BatteryWhiteListDialog extends Dialog {

    private Context context;
    private String title;
    private String confirmButtonText;
    private String cacelButtonText;
    private ClickListenerInterface clickListenerInterface;
    private  CheckBox cbIsHide;
    private boolean  isHide=false;
    public interface ClickListenerInterface {
        public void doConfirm();
        public void doCancel();
    }

    public boolean getIsHide() {
        return isHide;
    }


    public BatteryWhiteListDialog(Context context, String title, String confirmButtonText, String cacelButtonText) {
        super(context);
        this.context = context;
        this.title = title;
        this.confirmButtonText = confirmButtonText;
        this.cacelButtonText = cacelButtonText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_battery_white_list, null);
        setContentView(view);

        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        TextView tvTip_Content = (TextView) view.findViewById(R.id.tv_tip_content);
        TextView tvishideHit = (TextView) view.findViewById(R.id.tv_ishide_hint);
        Button   btnCancel =(Button) view.findViewById(R.id.btn_cancel);
        Button   btnToSet  = (Button) view.findViewById(R.id.btn_toset);
          cbIsHide  = (CheckBox) view.findViewById( R.id.cb_ishide);
          cbIsHide.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                  if(isChecked){
                      isHide=true;
                  }else {
                      isHide=false;
                  }
              }
          } );

        tvTitle.setText(title);
        tvTip_Content.setText(confirmButtonText);
        tvishideHit.setText(cacelButtonText);

        btnCancel.setOnClickListener(new clickListener());
        btnToSet.setOnClickListener(new clickListener());
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
                case R.id.btn_toset:
                    clickListenerInterface.doConfirm();
                    break;
                case R.id.btn_cancel:
                    clickListenerInterface.doCancel();
                    break;
            }
        }
    };


}
