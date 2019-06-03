package com.inspur.imp.plugin.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 设置消息提示框类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class DialogService extends ImpPlugin {

    // js页面传递过来的消息提示框的标题
    private String title;
    // js页面传递过来的提示消息
    private String msg;
    // 设置消息提示框的回调函数
    private String functName;
    private EditText editText;

    // prompt中设置的默认输入文本
    private String defaultText;
    // 自定义弹窗按钮文本
    private String buttonLabel;
    // 标签数组
    private String[] fbutton;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 打开文本编辑框
        if ("openTextEditor".equals(action)) {
            openTextEditor(paramsObject);
        }
        // 打开消息提示框
        else if ("openToast".equals(action)) {
            openToast(paramsObject);
        }
        // 调用alert弹窗组件
        else if ("alert".equals(action)) {
            alert(paramsObject);
        }
        // 调用confirm组件
        else if ("confirm".equals(action)) {
            confirm(paramsObject);
        }
        // 调用prompt组件
        else if ("prompt".equals(action)) {
            prompt(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 文本编辑器
     *
     * @param paramsObject JSON串
     */
    private void openTextEditor(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("title"))
                title = paramsObject.getString("title");
            if (!paramsObject.isNull("msg"))
                msg = paramsObject.getString("msg");
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getFragmentContext());
        View viewText = layoutInflater.inflate(
                Res.getLayoutID("imp_activity_edittext"), null);
        // 获得Edit框
        editText = (EditText) viewText.findViewById(Res.getWidgetID("txt"));
        if (StrUtil.strIsNotNull(msg)) {
            editText.setText(msg);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(viewText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 设置回调js页面函数
                        jsCallback(functName, editText.getText().toString()
                                .trim());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /**
     * 消息提示框
     *
     * @param paramsObject 传进来的JSON串
     */
    private void openToast(JSONObject paramsObject) {
        try {
            if (!paramsObject.isNull("msg"))
                msg = paramsObject.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (StrUtil.strIsNotNull(msg))
            // 利用Toast方式显示消息内容
            ToastUtils.show(getFragmentContext(), msg);
    }

    /**
     * Description notification中的alert实现与设定
     *
     * @param paramsObject JSON串
     */
    private void alert(JSONObject paramsObject) {
        // 解析Json得到参数
        try {
            if (!paramsObject.isNull("title")) {
                title = paramsObject.getString("title");
            }
            if (!paramsObject.isNull("msg")) {
                msg = paramsObject.getString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // alert 无返回值
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Description dialog中的confirm实现与设定
     *
     * @param paramsObject JSON串
     */

    private void confirm(JSONObject paramsObject) {
        // 解析Json得到参数
        try {
            // 获取确认框的标题
            if (!paramsObject.isNull("title")) {
                title = paramsObject.getString("title");
            }
            // 取得提示框信息
            if (!paramsObject.isNull("msg")) {
                msg = paramsObject.getString("msg");
            }
            // 获取自定义 标签组字符串
            if (!paramsObject.isNull("buttonLabel")) {
                buttonLabel = paramsObject.getString("buttonLabel");
            }
            // 回调函数名
            if (!paramsObject.isNull("callback")) {
                functName = paramsObject.getString("callback");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fbutton = buttonLabel.split(",");
        AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity())
                .setTitle(title).setMessage(msg);

        if (fbutton.length > 0)
            dlg.setPositiveButton(fbutton[0],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 返回一个参数信息
                            jsCallback(functName, "0");
                        }
                    });
        if (fbutton.length > 1)
            dlg.setNegativeButton(fbutton[1],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsCallback(functName, "1");
                        }
                    });
        if (fbutton.length > 2)
            dlg.setNeutralButton(fbutton[2],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsCallback(functName, "2");
                        }
                    });
        dlg.create();
        dlg.show();
    }

    /**
     * prompt组件
     *
     * @param paramsObject JSON串
     */
    private void prompt(JSONObject paramsObject) {
        try {
            // 标题
            if (!paramsObject.isNull("title"))
                title = paramsObject.getString("title");
            // 提示输入信息
            if (!paramsObject.isNull("msg"))
                msg = paramsObject.getString("msg");
            // 默认输入的一个样例文本
            if (!paramsObject.isNull("defaultText"))
                defaultText = paramsObject.getString("defaultText");
            // 获取自定义 标签组字符串
            if (!paramsObject.isNull("buttonLabel")) {
                buttonLabel = paramsObject.getString("buttonLabel");
            }
            // 回调函数
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        fbutton = buttonLabel.split(",");
        LayoutInflater layoutInflater = LayoutInflater.from(getFragmentContext());
        View viewText = layoutInflater.inflate(
                Res.getLayoutID("imp_activity_edittext"), null);
        // 获得EditText框,在EditText中设置默认文本。
        editText = (EditText) viewText.findViewById(Res.getWidgetID("txt"));
        if (StrUtil.strIsNotNull(defaultText)) {
            editText.setText(defaultText);
        }
        AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity())
                .setTitle(title).setMessage(msg).setView(viewText);
        if (fbutton.length > 0)
            dlg.setPositiveButton(fbutton[0],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 返回一个参数信息
                            jsCallback(functName, new String[]{"0", editText.getText().toString().trim()});
                        }
                    });
        if (fbutton.length > 1)
            dlg.setNegativeButton(fbutton[1],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsCallback(functName, new String[]{"2", ""});
                        }
                    });
        if (fbutton.length > 2)
            dlg.setNeutralButton(fbutton[2],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsCallback(functName, new String[]{"1", editText.getText().toString().trim()});
                        }
                    });
        dlg.create();
        dlg.show();
    }

    @Override
    public void onDestroy() {

    }
}
