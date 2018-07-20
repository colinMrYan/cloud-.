package com.inspur.imp.plugin.startapp;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/7/20.
 */

public class WindowService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            UriUtils.openUrl(getActivity(), JSONUtils.getString(paramsObject,"url",""),JSONUtils.getString(paramsObject,"title",""));
        }else{
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    @Override
    public void onDestroy() {

    }
}
