package com.inspur.emmcloud.bean.mine;

import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.reactnative.ReactNativeWritableArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetMyInfoResultWithoutSerializable {

    private static final String TAG = "GetMyInfoResultWithoutSerializable";

    private String response;
    private String avatar;
    private String code;
    private String creationDate;
    private String firstName;
    private String lastName;
    private String id;
    private String mail;
    private String phoneNumber;
    private Boolean hasPassord;
    private List<Enterprise> enterpriseList = new ArrayList<>();
    private Enterprise defaultEnterprise;
    private WritableNativeMap writableNativeMap = new WritableNativeMap();//RN内部自己使用

    public GetMyInfoResultWithoutSerializable(String response) {
        this.response = response;
        JSONObject jObject = JSONUtils.getJSONObject(response, "enterprise", new JSONObject());
        defaultEnterprise = new Enterprise(jObject);
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
        ReactNativeWritableArray reactNativeWritableArray = new ReactNativeWritableArray();
        WritableNativeArray writableNativeArray = new WritableNativeArray();
        for (int i = 0; i < enterpriseArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(enterpriseArray, i, null);
            if (obj != null) {
                Enterprise enterprise = new Enterprise(obj);
                enterpriseList.add(enterprise);
                reactNativeWritableArray.pushMap(enterprise.enterPrise2ReactNativeWritableNativeMap());
                writableNativeArray.pushMap(enterprise.enterPrise2WritableNativeMap());
            }
        }
        writableNativeMap.putArray("enterprises", writableNativeArray);
    }


    /**
     * 为NativeBridge方法，不能序列化否则报异常
     *
     * @return
     */
    public WritableNativeMap getUserProfile2WritableNativeMap() {
        writableNativeMap.putMap("enterprise", defaultEnterprise.enterPrise2ReactNativeWritableNativeMap());
        writableNativeMap.putString("avatar", avatar);
        writableNativeMap.putString("code", code);
        writableNativeMap.putDouble("creation_date", Double.valueOf(creationDate));
        writableNativeMap.putString("first_name", firstName);
        writableNativeMap.putString("last_name", lastName);
        writableNativeMap.putInt("id", Integer.valueOf(id));
        writableNativeMap.putString("mail", mail);
        writableNativeMap.putString("phone", phoneNumber);
        writableNativeMap.putBoolean("has_password", hasPassord);
        return writableNativeMap;
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


    public String getResponse() {
        return response;
    }

    public Boolean getHasPassord() {
        return hasPassord;
    }

    public Enterprise getDefaultEnterprise() {
        return defaultEnterprise;
    }

}
