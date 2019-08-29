package com.inspur.emmcloud.web.plugin.http;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.web.api.WebAPIInterfaceImpl;
import com.inspur.emmcloud.web.api.WebAPIService;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/8/28.
 */

public class HttpService extends ImpPlugin {
    private String successCal, failCal;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("get")) {
            httpGet(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    private void httpGet(JSONObject paramsObject) {
        successCal = JSONUtils.getString(paramsObject, "success", "");
        failCal = JSONUtils.getString(paramsObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String url = JSONUtils.getString(optionsObj, "url", "");
        WebAPIService apiService = new WebAPIService(BaseApplication.getInstance());
        apiService.setAPIInterface(new WebService());
        apiService.webHttpGet(url);
    }

    private void callbackSuccess(String result) {
        if (!StringUtils.isBlank(successCal)) {
            JSONObject object = new JSONObject();
            try {
                object.put("result", result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.jsCallback(successCal, object);
        }
    }

    private void callbackFail(String errorMessage) {
        if (!StringUtils.isBlank(failCal)) {
            JSONObject object = new JSONObject();
            try {
                object.put("errorMessage", errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.jsCallback(failCal, object);
        }
    }


    @Override
    public void onDestroy() {

    }

    private class WebService extends WebAPIInterfaceImpl {
        @Override
        public void returnWebHttpGetSuccess(String result) {
            callbackSuccess(result);
        }

        @Override
        public void returnWebHttpGetFail(String error, int errorCode) {
            callbackFail(error);
        }
    }
}
