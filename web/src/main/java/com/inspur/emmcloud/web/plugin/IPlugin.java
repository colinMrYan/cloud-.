package com.inspur.emmcloud.web.plugin;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.web.ui.ImpCallBackInterface;
import com.inspur.emmcloud.web.webview.ImpWebView;

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
    void init(Context context, ImpWebView webview, ImpCallBackInterface impCallBackInterface);

    /**
     * 执行方法，无返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    void execute(String action, JSONObject paramsObject);

    /**
     * 执行方法，有返回值
     *
     * @param action       方法名
     * @param paramsObject 参数
     * @throws JSONException
     */
    String executeAndReturn(String action, JSONObject paramsObject);

    /**
     * 回调JavaScript方法
     *
     * @param functionName
     */
    void jsCallback(String functionName);

    /**
     * 回调JavaScript方法，回调参数是字符串
     *
     * @param functionName
     * @param param
     */
    void jsCallback(String functionName, String param);

    /**
     * 回调JavaScript方法,回调参数是字符串数组
     *
     * @param functionName
     * @param params
     */
    void jsCallback(String functionName, String[] params);

    /**
     * activity关闭之前调用方法
     */
    void onDestroy();

    void onActivityResume();

    void onActivityPause();

    void onActivityStart();

    void onActivityResult(int requestCode, int resultCode, Intent data);
}
