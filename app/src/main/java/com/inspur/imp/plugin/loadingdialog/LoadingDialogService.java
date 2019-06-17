package com.inspur.imp.plugin.loadingdialog;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;


/**
 * web loading显示类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class LoadingDialogService extends ImpPlugin {


    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("show".equals(action)) {
            showDlg(paramsObject);
        } else if ("hide".equals(action)) {
            hideDlg();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    private void showDlg(JSONObject paramsObject) {
        String content = null;
        content = JSONUtils.getString(paramsObject, "content", "");
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onLoadingDlgShow(content);
        }
    }

    private void hideDlg() {
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onLoadingDlgDimiss();
        }
    }

    @Override
    public void onDestroy() {

    }
}
