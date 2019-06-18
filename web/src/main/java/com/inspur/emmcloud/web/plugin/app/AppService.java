package com.inspur.emmcloud.web.plugin.app;

import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;


/**
 * 应用相关本地功能类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class AppService extends ImpPlugin {

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("close".equals(action)) {
            close();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        // 退出系统
        if ("close".equals(action)) {
            close();
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }


    /**
     * 关闭网页所在的Activity（当网页所在的Activity 为IndexActivity时不关闭）
     */
    private void close() {
        boolean isWebFromIndex = false;
        if (getImpCallBackInterface() != null) {
            isWebFromIndex = getImpCallBackInterface().isWebFromIndex();
        }
        if (!isWebFromIndex) {
            getActivity().finish();
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    }

}
