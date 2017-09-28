package com.inspur.emmcloud.bean;

import android.content.Context;

import com.facebook.react.bridge.WritableNativeMap;
import com.inspur.emmcloud.util.UriUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * {"id":"lcgj_u_chenmch","inspur_id":"1ae36f30-b25a-435a-868e-2b7096d70cc0",
 * "code":"chenmch","real_name":"陈明超","pinyin":"chenmingchao",
 * "new_id":"66666","mobile":"18560131028",
 * "email":"chenmch@inspur.com","org_name":"运行平台产品研发部",
 * "full_path":"inspur_dev00010004|inspur_dev000100040007|inspur_dev000100040007239",
 * "parent_id":"inspur_dev000100040007239","head":"/img/headimg/d6858120-f6e7-11e5-9cad-850d114ad5ec",
 * "sort_order":1,"type":"user"}
 */
@Table(name = "Contact", onCreated = "CREATE INDEX contactindex ON Contact(inspurID)")
public class Contact implements Serializable {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "parentId")
    private String parentId = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "code")
    private String code = "";
    @Column(name = "email")
    private String email = "";
    @Column(name = "head")
    private String head = "";
    @Column(name = "mobile")
    private String mobile = "";
    @Column(name = "orgName")
    private String orgName = "";
    @Column(name = "realName")
    private String realName = "";
    @Column(name = "type")
    private String type = "";
    @Column(name = "sortOrder")
    private int sortOrder = -1;
    @Column(name = "pinyin")
    private String pinyin = "";
    @Column(name = "inspurID")
    private String inspurID = "";
    @Column(name = "globalName")
    private String globalName = "";
    @Column(name = "lastUpdateTime")
    private String lastUpdateTime = "";

    public Contact() {

    }

    public Contact(JSONObject obj, String lastUpdateTime) {
        this(obj);
        this.lastUpdateTime = lastUpdateTime;
    }


    public Contact(JSONObject obj) {
        try {
            if (obj.has("id")) {
                this.id = obj.getString("id");
            }
            if (obj.has("parent_id")) {
                this.parentId = obj.getString("parent_id");
            }
            if (obj.has("name")) {
                this.name = obj.getString("name");
            }
            if (obj.has("code")) {
                this.code = obj.getString("code");
            }
            if (obj.has("email")) {
                this.email = obj.getString("email");
            }
            if (obj.has("head")) {
                this.head = obj.getString("head");
            }
            if (obj.has("inspur_id")) {
                this.inspurID = obj.getString("inspur_id");
            }
            if (obj.has("mobile")) {
                this.mobile = obj.getString("mobile");
            }

            if (obj.has("org_name")) {
                this.orgName = obj.getString("org_name");
            }
            if (obj.has("real_name")) {
                this.realName = obj.getString("real_name");
            }
            if (obj.has("type")) {
                this.type = obj.getString("type");
            }
            if (obj.has("sort_order")) {
                this.sortOrder = obj.getInt("sort_order");
            }
            if (obj.has("pinyin")) {
                this.pinyin = obj.getString("pinyin");
            }
            if (obj.has("name_global")) {
                this.globalName = obj.getString("name_global");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInspurID() {
        return inspurID;
    }



    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        if (type.equals("user")) {
            return realName;
        }
        return name;
    }


    public String getPinyin() {
        return pinyin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName(String globalName) {
        this.globalName = globalName;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public JSONObject contact2JSONObject(Context context) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("inspur_id", inspurID);
            obj.put("code", code);
            obj.put("real_name", realName);
            obj.put("pinyin", pinyin);
            obj.put("mobile", mobile);
            obj.put("email", email);
            obj.put("org_name", orgName);
            obj.put("type", type);
            obj.put("head", UriUtils.getChannelImgUri(context, inspurID));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    ;

    public WritableNativeMap contact2Map(Context context) {
        WritableNativeMap map = new WritableNativeMap();
        try {
            map.putString("inspur_id", inspurID);
            map.putString("code", code);
            map.putString("real_name", realName);
            map.putString("pinyin", pinyin);
            map.putString("mobile", mobile);
            map.putString("email", email);
            map.putString("org_name", orgName);
            map.putString("type", type);
            map.putString("head", UriUtils.getChannelImgUri(context, inspurID));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    ;

    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", email='" + email + '\'' +
                ", head='" + head + '\'' +
                ", mobile='" + mobile + '\'' +
                ", orgName='" + orgName + '\'' +
                ", realName='" + realName + '\'' +
                ", type='" + type + '\'' +
                ", sortOrder=" + sortOrder +
                ", pinyin='" + pinyin + '\'' +
                ", inspurID='" + inspurID + '\'' +
                ", globalName='" + globalName + '\'' +
                '}';
    }
}
