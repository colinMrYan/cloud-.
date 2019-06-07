package com.inspur.emmcloud.basemodule.widget.dialogs;

/**
 * Created by yufuchang on 2017/4/13.
 */

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

import com.inspur.basemodule.R;


/**
 * 加载提醒对话框
 */
public class ReactLoadingDlg extends Dialog {
    public ReactLoadingDlg(Context context) {
        super(context, R.style.dialog_progressbar);
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
        setContentView(R.layout.dialog_react_loading);
        // 设置window属性
        LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0f; // 去背景遮盖
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void show() {
        super.show();
    }
}