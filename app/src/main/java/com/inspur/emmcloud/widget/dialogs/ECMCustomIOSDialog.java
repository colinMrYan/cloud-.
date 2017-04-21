package com.inspur.emmcloud.widget.dialogs;

/**
 * Created by yufuchang on 2017/4/13.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.inspur.emmcloud.R;


/**
 * 加载提醒对话框
 */
public class ECMCustomIOSDialog extends ProgressDialog {
    public ECMCustomIOSDialog(Context context) {
        super(context);
    }

    public ECMCustomIOSDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
    }

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_custom_ios_layout);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.7f;
        getWindow().setAttributes(params);
    }

    @Override
    public void show() {
        super.show();
    }
}