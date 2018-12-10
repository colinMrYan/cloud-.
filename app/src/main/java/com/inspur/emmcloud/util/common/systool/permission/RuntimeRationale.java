package com.inspur.emmcloud.util.common.systool.permission;

import android.content.Context;
import android.text.TextUtils;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

/**
 * 权限被拒绝之后弹出的dialog
 * Created by yufuchang on 2018/10/24.
 */

public final class RuntimeRationale implements Rationale<List<String>> {

    @Override
    public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.permission_dialog_message_rationale, TextUtils.join("\n", permissionNames));
        new MyQMUIDialog.MessageDialogBuilder(context)
                .setTitle(R.string.permission_dialog_title)
                .setMessage(message)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        LogUtils.YfcDebug("取消申请");
                        dialog.dismiss();
                        executor.cancel();
                    }
                })
                .addAction(R.string.settings, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        LogUtils.YfcDebug("重新申请");
                        dialog.dismiss();
                        executor.execute();
                    }
                })
                .show();
    }
}
