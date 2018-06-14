package com.inspur.imp.plugin.staff;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.util.DialogUtil;

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

        this.paramsObject = paramsObject;
        multiSelection = JSONUtils.getInt(paramsObject, "multiSelection", 0);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if ("select".equals(action)) {
            selectFromContact();
        } else if ("viewContact".equals(action)) {
            viewContact();
        } else if("openContact".equals(action)){
            openContact();
        } else {
            DialogUtil.getInstance(getActivity()).show();
        }
    }

    /**
     * 打开联系人详情页面
     */
    private void openContact() {
        String id = JSONUtils.getString(paramsObject,"uid","");
        Intent intent = new Intent();
        intent.putExtra("uid", id);
        intent.setClass(getActivity(), UserInfoActivity.class);
        getActivity().startActivity(intent);
    }

    /**
     * 查看人员方法
     */
    private void viewContact() {
        ArrayList<String> contactIdList = new ArrayList<String>();
        JSONArray array = JSONUtils.getJSONArray(paramsObject, "array", new JSONArray());
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
        bundle.putString("search", "1");
        bundle.putStringArrayList("uidList", contactIdList);
        IntentUtils.startActivity(getActivity(),
                MembersActivity.class, bundle);
    }

    /**
     * 从通讯录选人
     */
    private void selectFromContact() {
        PublicWay.selectStaffService = this;
        Intent intent = new Intent();
        intent.setClass(getActivity(),
                ContactSearchActivity.class);
        intent.putExtra(ContactSearchActivity.EXTRA_TYPE, 2);
        intent.putExtra(ContactSearchActivity.EXTRA_MULTI_SELECT, multiSelection == 0 ? false : true);
        intent.putExtra(ContactSearchActivity.EXTRA_TITLE, getActivity().getString(R.string.adress_list));
        getActivity().startActivityForResult(intent, CONTACT_PICKER);
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        multiSelection = JSONUtils.getInt(JSONUtils.getJSONObject(paramsObject, "options", new JSONObject()), "multiSelection", 0);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if ("select".equals(action)) {
            selectFromContact();
        } else if ("viewContact".equals(action)) {
            viewContact();
        } else if("openContact".equals(action)){
            openContact();
        } else {
            DialogUtil.getInstance(getActivity()).show();
        }
        return super.executeAndReturn(action, paramsObject);
    }

    @Override
    public void onDestroy() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode == CONTACT_PICKER) {
            List<SearchModel> searchModelList = (List<SearchModel>) intent.getSerializableExtra("selectMemList");
            List<String> uidList = new ArrayList<>();
            for (int i = 0; i < searchModelList.size(); i++) {
                String contactId = searchModelList.get(i).getId();
                if (!StringUtils.isBlank(contactId)) {
                    uidList.add(contactId);
                }
            }
            List<Contact> contactList = ContactCacheUtils.getSoreUserList(getActivity(), uidList);
            if (contactList.size() == 1) {
                this.jsCallback(successCb, contactList.get(0).contact2JSONObject(getActivity()).toString());
            } else {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < contactList.size(); i++) {
                    jsonArray.put(contactList.get(i).contact2JSONObject(getActivity()));
                }
                if (jsonArray.length() > 0) {
                    this.jsCallback(successCb, jsonArray.toString());
                }
            }

        } else {
            this.jsCallback(failCb, "error");
        }
    }
}
