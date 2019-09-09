package com.inspur.emmcloud.web.plugin.staff;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yufuchang on 2018/5/30.
 */

public class SelectStaffService extends ImpPlugin {

    private static final int CONTACT_PICKER = 2;
    private String successCb, failCb;
    private int multiSelection = 0;
    private JSONObject paramsObject;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        multiSelection = JSONUtils.getInt(JSONUtils.getJSONObject(paramsObject, "options", new JSONObject()), "multiSelection", 0);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if ("select".equals(action)) {
            selectFromContact();
        } else if ("viewContact".equals(action)) {
            viewContact();
        } else if ("openContact".equals(action)) {
            openContact();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    /**
     * 打开联系人详情页面
     */
    private void openContact() {
        String id = JSONUtils.getString(paramsObject, "uid", "");
        Bundle bundle = new Bundle();
        bundle.putString("uid", id);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle).navigation();

    }

    /**
     * 查看人员方法
     */
    private void viewContact() {
        ArrayList<String> contactIdList = new ArrayList<String>();
        JSONArray array = JSONUtils.getJSONArray(paramsObject, "uidArray", new JSONArray());
        try {
            for (int i = 0; i < array.length(); i++) {
                contactIdList.add((String) array.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putString("cid", "");
        bundle.putString("title", getActivity().getString(R.string.meeting_memebers));
        bundle.putInt("member_page_state", 3);
        bundle.putStringArrayList("uidList", contactIdList);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER).with(bundle).navigation();
    }

    /**
     * 从通讯录选人
     */
    private void selectFromContact() {
        Bundle bundle = new Bundle();
        bundle.putInt("select_content", 2);
        bundle.putBoolean("isMulti_select", multiSelection != 0);
        bundle.putString("title", getActivity().getString(R.string.adress_list));
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onStartActivityForResult(Constant.AROUTER_CLASS_CONTACT_SEARCH, bundle, ImpFragment.SELECT_STAFF_SERVICE_REQUEST);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode == ImpFragment.SELECT_STAFF_SERVICE_REQUEST) {
            List<SearchModel> searchModelList = (List<SearchModel>) intent.getSerializableExtra("selectMemList");
            List<String> uidList = new ArrayList<>();
            for (int i = 0; i < searchModelList.size(); i++) {
                String contactId = searchModelList.get(i).getId();
                if (!StringUtils.isBlank(contactId)) {
                    uidList.add(contactId);
                }
            }
            List<ContactUser> contactList = new ArrayList<>();
            Router router = Router.getInstance();
            if (router.getService(ContactService.class) != null) {
                ContactService service = router.getService(ContactService.class);
                contactList = service.getSortUserList(uidList);
            }
            if (multiSelection == 0 && contactList.size() == 1) {
                this.jsCallback(successCb, contactList.get(0).contact2JSONObject(getActivity()));
            } else {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < contactList.size(); i++) {
                    jsonArray.put(contactList.get(i).contact2JSONObject(getActivity()));
                }
                if (jsonArray.length() > 0) {
                    this.jsCallback(successCb, jsonArray);
                }
            }
        } else {
            this.jsCallback(failCb, "error");
        }
    }
}
