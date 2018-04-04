package com.inspur.imp.plugin;

import android.content.Context;

import com.inspur.imp.engine.webview.ImpWebView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 功能调用服务接口类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public interface IPlugin {

    /**
     * 初始化上下文和webview控件
     *
     * @param context
     * @param webview
     */
    public void init(Context context, ImpWebView webview);

    /**
     * 执行方法，无返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    public void execute(String action, JSONObject paramsObject);

    /**
     * 执行方法，有返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    public String executeAndReturn(String action, JSONObject paramsObject);

    /**
     * 回调JavaScript方法
     *
     * @param functionName
     */
    public void jsCallback(String functionName);

    /**
     * 回调JavaScript方法，回调参数是字符串
     *
     * @param functionName
     * @param param
     */
    public void jsCallback(String functionName, String param);

    /**
     * 回调JavaScript方法,回调参数是字符串数组
     *
     * @param functionName
     * @param params
     */
    public void jsCallback(String functionName, String[] params);

    /**
     * activity关闭之前调用方法
     */
    public void onDestroy();

    public void onActivityResume();

    public void onActivityPause();
}
