package com.inspur.imp.plugin.staff;

import android.content.Intent;

import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/11/27.
 */

public class StuffInformationService  extends ImpPlugin{
    private String successCb, failCb;
    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if(action.equals("view")){
            viewContact(JSONUtils.getString(JSONUtils.getJSONObject(paramsObject, "options", new JSONObject()), "inspurId", ""));
        }else{
            this.jsCallback(failCb, "");
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 打开联系人详情页面
     */
    private void viewContact(String id) {
        Intent intent = new Intent();
        intent.putExtra("uid", id);
        intent.setClass(getActivity(), UserInfoActivity.class);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {

    }
}
