package com.inspur.emmcloud.web.plugin.screenshot;

import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/12/6.
 */

public class ScreenshotService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("do")) {
            screenshot();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }


    private void screenshot() {

    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
