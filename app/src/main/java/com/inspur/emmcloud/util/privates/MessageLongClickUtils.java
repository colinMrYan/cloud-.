package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

/**
 * Created by libaochao on 2019/3/21.
 */

public class MessageLongClickUtils {

    public static void textMessageLongClick(final Context context) {
        final String[] items = new String[]{"复制", "转发", "日程"};
        LongClickDialog(items, context);

    }

    public static void imageMessageLongClick(final Context context) {
        final String[] items = new String[]{"转发", "日程"};
        LongClickDialog(items, context);
    }



    private static void LongClickDialog(final String[] items, final Context context) {
        new QMUIDialog.MenuDialogBuilder(context)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (items[which]){
                            case "复制":
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                            case "转发":
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                            case "日程":
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                        }
                        //Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .create(R.style.QMUI_Dialog).show();
    }


}
