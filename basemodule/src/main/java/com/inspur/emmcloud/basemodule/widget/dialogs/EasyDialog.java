package com.inspur.emmcloud.basemodule.widget.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 常用的Dialog实现                                                           <br/>
 * 有一个按钮、两个按钮、三个按钮和多个按钮选择         <br/>
 * 需要传入的参数有                                                                 <br/>
 * context,                         <br/>
 * title(标题),                      <br/>
 * content(显示的内容),                <br/>
 * 一个到三个底部按钮的文字,                <br/>
 * 或者一个String数组用于显示列表,          <br/>
 * 最后是一个监听接口用来监听点击的是哪一个按钮           <br/>
 */
public class EasyDialog {

    /**
     * 一个按钮
     *
     * @param context
     * @param title    Dialog的标题
     * @param content  Dialog的内容
     * @param item1    只有一个按钮时按钮显示的文字
     * @param listener 需要传入的点击按钮后的处理DialogInterface.OnClickListener
     */
    public static void showDialog(Context context, String title, String content, String item1, DialogInterface.OnClickListener listener, boolean cancle) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton(item1, listener);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(cancle);
            dialog.show();
        }
    }


    /**
     * 两个按钮
     *
     * @param context
     * @param title    Dialog的标题
     * @param content  Dialog的内容
     * @param item1    两个按钮第一个按钮文字
     * @param item2    第二个按钮文字
     * @param listener 需要传入的点击按钮后的处理DialogInterface.OnClickListener
     */
    public static void showDialog(Context context, String title, String content, String item1, String item2, DialogInterface.OnClickListener listener, boolean cancle) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton(item1, listener);
            builder.setNegativeButton(item2, listener);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(cancle);
            dialog.show();
        }
    }

    /**
     * 三个按钮
     *
     * @param context
     * @param title    Dialog的标题
     * @param content  Dialog的内容
     * @param item1    三个按钮第一个按钮文字
     * @param item2    第二个按钮文字
     * @param item3    第三个按钮文字
     * @param listener 点击按钮后的监听DialogInterface.OnClickListener
     */
    public static void showDialog(Context context, String title, String content, String item1, String item2, String item3, DialogInterface.OnClickListener listener, boolean cancle) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton(item1, listener);
            builder.setNeutralButton(item2, listener);
            builder.setNegativeButton(item3, listener);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(cancle);
            dialog.show();
        }
    }

    /**
     * 一组按钮
     *
     * @param context
     * @param title    一组按钮的标题
     * @param items    一组按钮时传入的String数组
     * @param listener 点击按钮后处理的DialogInterface.OnClickListener
     */
    public static void showDialog(Context context, String title, String[] items, DialogInterface.OnClickListener listener, boolean cancle) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle(title);
            builder.setItems(items, listener);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(cancle);
            dialog.show();
        }
    }
}
