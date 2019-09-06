package com.inspur.emmcloud.web.plugin.staff;

import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

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
        ContactUser contactUser = null;
        Router router = Router.getInstance();
        if (router.getService(ContactService.class) != null) {
            ContactService service = router.getService(ContactService.class);
            contactUser = service.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
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
                object.put("head", WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/img/userhead/" + BaseApplication.getInstance().getUid());
            }
            object.put("tenantId", BaseApplication.getInstance().getCurrentEnterprise().getId());
            this.jsCallback(successCb, object);
        } catch (Exception e) {
            e.printStackTrace();
            this.jsCallback(failCb, "error");
        }
    }

    /**
     * 打开联系人详情页面
     */
    private void viewContact(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", id);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle).navigation();
    }

    @Override
    public void onDestroy() {

    }
}
