package com.inspur.imp.plugin.staff;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
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
        LogUtils.jasonDebug("paramsObject=" + paramsObject);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        LogUtils.jasonDebug("successCb=" + successCb);
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

    private void returnUserInfo() {
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid());
        JSONObject object = null;
        try {
            if (contactUser != null) {
                object = contactUser.contact2JSONObject(getFragmentContext());
            } else {
                String myInfo = PreferencesUtils.getString(getFragmentContext(), "myInfo", "");
                GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
                object.put("id", getMyInfoResult.getID());
                object.put("name", getMyInfoResult.getName());
                object.put("nameGlobal", "");
                object.put("pinyin", "");
                object.put("mobile", getMyInfoResult.getPhoneNumber());
                object.put("email", getMyInfoResult.getMail());
                object.put("head", APIUri.getChannelImgUrl4Imp(getMyInfoResult.getID()));
            }
            object.put("tenantId", MyApplication.getInstance().getCurrentEnterprise().getId());
            this.jsCallback(successCb, object.toString());
        } catch (Exception e) {
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
