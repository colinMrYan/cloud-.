package com.inspur.imp.util;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

/**
 * Created by yufuchang on 2018/5/28.
 */

public class DialogUtil {

//    private static MyQMUIDialog.MessageDialogBuilder myQMUIDialog;
//    public static MyQMUIDialog.MessageDialogBuilder getInstance(Activity activity){
//        if(myQMUIDialog == null){
//            synchronized (DialogUtil.class){
//                if(myQMUIDialog == null){
//                    myQMUIDialog = new MyQMUIDialog.MessageDialogBuilder(activity)
//                            .setMessage("方法名错误或此版本的客户端不支持，请检查")
//                            .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
//                                @Override
//                                public void onClick(QMUIDialog dialog, int index) {
//                                    dialog.dismiss();
//                                }
//                            });
//                }
//            }
//        }
//        return myQMUIDialog;
//    }



    private static MyDialog myDialog;
    public static MyDialog getInstance(Activity activity){
        if(myDialog == null){
            synchronized (MyDialog.class){
                if(myDialog == null){
                    myDialog = new MyDialog(activity, R.layout.dialog_one_button,R.style.CustomDialog);
                    ((TextView)myDialog.findViewById(R.id.show_text)).setText("方法名错误或此版本的客户端不支持，请检查");
                    myDialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }
                    });
                }
            }
        }
        return myDialog;
    }

}
