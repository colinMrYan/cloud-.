package com.inspur.imp.plugin;

import android.app.Activity;
import android.content.Context;

import com.inspur.imp.engine.webview.ImpWebView;

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
    protected Context context;

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
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return "";
    }

    /**
     * 初始化context和webview控件
     *
     * @param context
     * @param webview
     */
    public void init(Context context, ImpWebView webview) {
        this.context = context;
        this.webview = webview;
    }

    /**
     * 返回给js的回调函数,无回调的参数
     *
     * @param functionName
     * @param
     */
    @Override
    public void jsCallback(String functionName) {
        this.webview.loadUrl("javascript: " + functionName + "()");

    }

    /**
     * 回调JavaScript方法，回调参数是字符串
     *
     * @param functionName
     * @param params
     */
    @Override
    public void jsCallback(String functionName, String params) {
        this.webview.loadUrl("javascript: " + functionName + "('" + params
                + "')");
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
        this.webview.loadUrl("javascript: " + functionName + "("
                + jsonArray.toString() + ")");
    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public void onActivityPause() {

    }

    public Activity getActivity() {
        return (Activity) this.context;
    }

    // 关闭功能类的方法
    public abstract void onDestroy();
}