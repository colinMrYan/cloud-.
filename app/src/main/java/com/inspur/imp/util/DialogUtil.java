package com.inspur.imp.util;

import com.inspur.emmcloud.widget.dialogs.MyDialog;

/**
 * Created by yufuchang on 2018/5/28.
 */

public class DialogUtil {

    private static MyDialog myDialog;
//    public static MyDialog getInstance(Activity activity){
//        if(myDialog == null){
//            synchronized (MyDialog.class){
//                if(myDialog == null){
//                    myDialog = new MyDialog(activity, R.layout.dialog_one_button,R.style.CustomDialog);
//                    TextView textView = ((TextView)myDialog.findViewById(R.id.show_text));
//                    textView.setGravity(Gravity.LEFT);
//                    textView.setText(R.string.imp_function_error);
//                    myDialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            myDialog.dismiss();
//                        }
//                    });
//                }
//            }
//        }
//        return myDialog;
//    }

}
