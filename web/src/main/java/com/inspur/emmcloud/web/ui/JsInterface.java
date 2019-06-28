package com.inspur.emmcloud.web.ui;

import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.inspur.emmcloud.web.plugin.PluginMgr;


/**
 * android端本地调用接口
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class JsInterface {

    private Handler handler = new Handler();

    private PluginMgr pluginMgr;

    public JsInterface(PluginMgr pluginMgr) {
        this.pluginMgr = pluginMgr;
    }

    /**
     * js调用invoke方法，有参数，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    @JavascriptInterface
    public void invoke(final String serviceName, final String action,
                       final String params) {
        handler.post(new Runnable() {
            public void run() {
                pluginMgr.execute(serviceName, action, params);
            }
        });
    }

    /**
     * js调用invoke方法，无参数，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param
     */
    @JavascriptInterface
    public void invoke(final String serviceName, final String action) {
        handler.post(new Runnable() {
            public void run() {
                pluginMgr.execute(serviceName, action, null);
            }
        });
    }

    /**
     * js调用invoke方法，有参数，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    @JavascriptInterface
    public String invokeAndReturn(String serviceName,
                                  String action, String params) {
        if (serviceName.equals("com.inspur.gsp.imp.framework.plugin.GloSessionService") && action.equals("getClose")) {
            serviceName = "com.inspur.imp.plugin.app.AppService";
            action = "close";
        } else if (serviceName.equals("com.inspur.gsp.imp.framework.plugin.GloSessionService") && action.equals("downloadFile")) {
            serviceName = "com.inspur.imp.plugin.filetransfer.FileTransferService";
            action = "downloadFile";
        }
        return pluginMgr.executeAndReturn(serviceName, action, params);
    }

    /**
     * js调用invoke方法，无参数，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param
     */
    @JavascriptInterface
    public String invokeAndReturn(final String serviceName, final String action) {
        return pluginMgr.executeAndReturn(serviceName, action, null);
    }

}