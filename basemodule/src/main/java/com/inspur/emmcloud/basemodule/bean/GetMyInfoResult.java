package com.inspur.emmcloud.basemodule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GetMyInfoResult implements Serializable {

    private static final String TAG = "GetRegisterResult";

    private String response;
    private String avatar;
    private String code;
    private String creationDate;
    private String firstName;
    private String lastName;
    private String id;
    private String mail;
    private String phoneNumber;
    //	private String enterpriseCode ;
//	private String enterpriseName;
    private Boolean hasPassord;
    //private String enterpriseId ;
    private List<Enterprise> enterpriseList = new ArrayList<>();
    private Enterprise defaultEnterprise;

    public GetMyInfoResult(String response) {
        this.response = response;
        JSONObject jObject = JSONUtils.getJSONObject(response, "enterprise", null);
        if (jObject != null) {
            defaultEnterprise = new Enterprise(jObject);
        }
        this.avatar = JSONUtils.getString(response, "avatar", "");
        this.code = JSONUtils.getString(response, "code", "");
        this.creationDate = JSONUtils.getString(response, "creation_date", "0");
        this.firstName = JSONUtils.getString(response, "first_name", "");
        this.lastName = JSONUtils.getString(response, "last_name", "");
        this.id = JSONUtils.getString(response, "id", "0");
        this.mail = JSONUtils.getString(response, "mail", "");
        this.phoneNumber = JSONUtils.getString(response, "phone", "");
        this.hasPassord = JSONUtils.getBoolean(response, "has_password", false);
        JSONArray enterpriseArray = JSONUtils.getJSONArray(response, "enterprises", new JSONArray());
        for (int i = 0; i < enterpriseArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(enterpriseArray, i, null);
            if (obj != null) {
                Enterprise enterprise = new Enterprise(obj);
                enterpriseList.add(enterprise);
            }
        }
    }



    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return firstName + lastName;
    }

    public String getID() {
        return id;
    }


    public String getMail() {
        return mail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

//	public String getEnterpriseCode() {
//		return enterpriseCode;
//	}
//
//	public String getEnterpriseName() {
//		return enterpriseName;
//	}

    public String getResponse() {
        return response;
    }

    public Boolean getHasPassord() {
        return hasPassord;
    }

    public Enterprise getDefaultEnterprise() {
        return defaultEnterprise;
    }
//	public String getEnterpriseId() {
//		return enterpriseId;
//	}

    public List<Enterprise> getEnterpriseList() {
        return enterpriseList;
    }

    public JSONObject getMyInfoJSONObject() {
        JSONObject obj = null;
        try {
            obj = new JSONObject(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;

    }
}
