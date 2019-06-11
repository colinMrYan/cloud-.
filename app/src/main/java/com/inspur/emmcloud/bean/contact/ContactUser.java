package com.inspur.emmcloud.bean.contact;

import android.content.Context;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

@Table(name = "ContactUser", onCreated = "CREATE INDEX contactUserIndex ON ContactUser(id)")
public class ContactUser {
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "nameGlobal")
    private String nameGlobal = "";
    @Column(name = "pinyin")
    private String pinyin = "";
    @Column(name = "parentId")
    private String parentId = "";
    @Column(name = "mobile")
    private String mobile = "";
    @Column(name = "email")
    private String email = "";
    @Column(name = "hasHead")
    private int hasHead = 0;
    @Column(name = "sortOrder")
    private int sortOrder = 0;
    @Column(name = "tel")
    private String tel = "";
    @Column(name = "office")
    private String office = "";
    @Column(name = "lastQueryTime")
    private String lastQueryTime = "";

    public ContactUser() {

    }

    public ContactUser(String id) {
        this.id = id;
    }

    public ContactUser(JSONObject object, String lastQueryTime) {
        this.id = JSONUtils.getString(object, "id", "");
        this.name = JSONUtils.getString(object, "real_name", "");
        this.nameGlobal = JSONUtils.getString(object, "name_global", "");
        this.pinyin = JSONUtils.getString(object, "pinyin", "");
        this.parentId = JSONUtils.getString(object, "parent_id", "");
        this.mobile = JSONUtils.getString(object, "mobile", "");
        this.email = JSONUtils.getString(object, "email", "");
        this.hasHead = JSONUtils.getInt(object, "has_head", 0);
        this.sortOrder = JSONUtils.getInt(object, "sort_order", 0);
        this.office = JSONUtils.getString(object, "office", "");
        this.tel = JSONUtils.getString(object, "tel", "");
        this.lastQueryTime = lastQueryTime;
    }

    public ContactUser(String id, String name, String nameGlobal, String pinyin, String parentId, String mobile, String email, int hasHead, int sortOrder, String lastQueryTime, String tel, String office) {
        this.id = id;
        this.name = name;
        this.nameGlobal = nameGlobal;
        this.pinyin = pinyin;
        this.parentId = parentId;
        this.mobile = mobile;
        this.email = email;
        this.hasHead = hasHead;
        this.sortOrder = sortOrder;
        this.tel = tel;
        this.office = office;
        this.lastQueryTime = lastQueryTime;
    }

    public static List<ContactUser> protoBufUserList2ContactUserList(List<ContactProtoBuf.user> userList, long lastQueryTime) {
        List<ContactUser> contactUserList = new ArrayList<>();
        if (userList != null && userList.size() > 0) {
            int size = userList.size();
            for (int i = 0; i < size; i++) {
                ContactProtoBuf.user user = userList.get(i);
                ContactUser contactUser = new ContactUser(user.getId(), user.getRealName(), user.getNameGlobal(), user.getPinyin(), user.getParentId(), user.getMobile(), user.getEmail(), user.getHasHead(), user.getSortOrder(), lastQueryTime + "", user.getTel(), user.getOffice());
                contactUserList.add(contactUser);
            }
        }
        return contactUserList;

    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getHasHead() {
        return hasHead;
    }

    public void setHasHead(int hasHead) {
        this.hasHead = hasHead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameGlobal() {
        return nameGlobal;
    }

    public void setNameGlobal(String nameGlobal) {
        this.nameGlobal = nameGlobal;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(String lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public JSONObject contact2JSONObject(Context context) {
        JSONObject obj = new JSONObject();
        String headUrl = APIUri.getChannelImgUrl4Imp(id);
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("nameGlobal", nameGlobal);
            obj.put("pinyin", pinyin);
            obj.put("mobile", mobile);
            obj.put("email", email);
            obj.put("head", StringUtils.isBlank(headUrl) ? "" : headUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    /*
            * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
            */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ContactUser))
            return false;

        final ContactUser otherContactUser = (ContactUser) other;
        return getId().equals(otherContactUser.getId());
    }
}
