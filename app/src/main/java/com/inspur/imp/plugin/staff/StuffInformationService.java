package com.inspur.imp.plugin.staff;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/11/27.
 */

public class StuffInformationService extends ImpPlugin {
    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        LogUtils.jasonDebug("paramsObject="+paramsObject);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        LogUtils.jasonDebug("successCb="+successCb);
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (action.equals("view")) {
            viewContact(JSONUtils.getString(JSONUtils.getJSONObject(paramsObject, "options", new JSONObject()), "inspurId", ""));
        } else if (action.equals("userInfo")) {
            returnUserInfo();
        } else {
            this.jsCallback(failCb, "");
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    private void returnUserInfo(){
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid());
        JSONObject object = null;
        try {
            if (contactUser != null){
                object = contactUser.contact2JSONObject(getFragmentContext());
                object.put("tenantId",MyApplication.getInstance().getCurrentEnterprise().getId());
                LogUtils.jasonDebug("object.toString()="+object.toString());
                this.jsCallback(successCb, object.toString());
            }else {
                this.jsCallback(failCb, "error");
            }

        }catch (Exception e){
            e.printStackTrace();
            this.jsCallback(failCb, "error");
        }
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
