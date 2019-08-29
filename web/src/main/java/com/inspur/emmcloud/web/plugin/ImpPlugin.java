package com.inspur.emmcloud.web.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.webkit.ValueCallback;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.ui.ImpCallBackInterface;
import com.inspur.emmcloud.web.webview.ImpWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 实现IPlugin接口的抽象类 ，实现了回调js页面的方法jsCallback， java 功能类继承此抽象类并实现action方法
 * 实现js对android端本地功能调用
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public abstract class ImpPlugin implements IPlugin {

    // 加载url的webview控件
    protected ImpWebView webview;

    // WebViewActivity的上下文
    private Context context;
    private ImpCallBackInterface impCallBackInterface;

    /**
     * 执行方法，无返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    public abstract void execute(String action, JSONObject paramsObject);

    /**
     * 执行方法，有返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    public abstract String executeAndReturn(String action, JSONObject paramsObject);

    /**
     * 初始化context和webview控件
     *
     * @param context
     * @param webview
     */
    public void init(Context context, ImpWebView webview, ImpCallBackInterface impCallBackInterface) {
        this.context = context;
        this.webview = webview;
        this.impCallBackInterface = impCallBackInterface;
    }

    /**
     * 返回给js的回调函数,无回调的参数
     *
     * @param functionName
     * @param
     */
    @Override
    public void jsCallback(String functionName) {
        String script = "javascript: " + functionName + "()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            this.webview.loadUrl(script);
        }

    }

    /**
     * 回调JavaScript方法，回调参数是JSON对象
     * 这个方法避免了添加以JSON对象为参数的内容回调时产生的解析问题，如需传递JSON对象可使用此方法
     *
     * @param functionName
     * @param params
     */
    @Override
    public void jsCallback(String functionName, JSONObject params) {
        String script = "javascript: " + functionName + "(" + params.toString() + ")";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            this.webview.loadUrl(script);
        }

    }

    /**
     * 回调JavaScript方法，回调参数是JSON对象
     * 这个方法避免了添加以JSON对象为参数的内容回调时产生的解析问题，如需传递JSON对象可使用此方法
     *
     * @param functionName
     * @param params
     */
    @Override
    public void jsCallback(String functionName, JSONArray params) {
        String script = "javascript: " + functionName + "(" + params.toString() + ")";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            this.webview.loadUrl(script);
        }

    }

    /**
     * 回调JavaScript方法，回调参数是字符串
     *
     * @param functionName
     * @param params
     */
    @Override
    public void jsCallback(String functionName, String params) {
        String script = "javascript: " + functionName + "('" + params + "')";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            this.webview.loadUrl(script);
        }

    }

    /**
     * 回调JavaScript方法，回调参数是字符串数组
     *
     * @param functionName
     * @param params
     */
    @Override
    public void jsCallback(String functionName, String[] params) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < params.length; i++) {
            jsonArray.put(params[i]);
        }

        String script = "javascript: " + functionName + "("
                + jsonArray.toString() + ")";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            this.webview.loadUrl(script);
        }
    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityStart() {

    }

    @Override
    public void onActivityNewIntent(Intent intent) {

    }

    public ImpCallBackInterface getImpCallBackInterface() {
        return impCallBackInterface;
    }

    public void showCallIMPMethodErrorDlg() {
        if (impCallBackInterface != null) {
//            impCallBackInterface.onShowImpDialog();
            //暂改为toast提示
            ToastUtils.show(getFragmentContext(), R.string.imp_function_error);
        }
    }


    public Activity getActivity() {
        if (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    public Context getFragmentContext() {
        return this.context;
    }


    // 关闭功能类的方法
    public abstract void onDestroy();

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

}