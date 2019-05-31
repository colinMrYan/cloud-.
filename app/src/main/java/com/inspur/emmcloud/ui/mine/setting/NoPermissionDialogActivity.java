package com.inspur.emmcloud.ui.mine.setting;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

/**
 * Created by yufuchang on 2018/5/4.
 */

public class NoPermissionDialogActivity extends BaseActivity {

    @Override
    public void onCreate() {
        final Dialog dialog = new MyDialog(this,
                R.layout.basewidget_dialog_one_button);
        dialog.setCanceledOnTouchOutside(false);
        ((TextView) dialog.findViewById(R.id.show_text)).setText(R.string.cluster_no_permission);
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }
}
