package com.inspur.emmcloud.bean.contact;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

@Table(name="ContactUser")
public class ContactUser {
    @Column(name = "id", isId = true)
    private String id="";
    @Column(name = "name")
    private String name = "";
    @Column(name = "nameGlobal")
    private String nameGlobal = "";
    @Column(name = "pinyin")
    private String pinyin = "";
    @Column(name = "parentId")
    private String parentId= "";
    @Column(name = "mobile")
    private String mobile="";
    @Column(name = "email")
    private String email="";
    @Column(name = "hasHead")
    private int hasHead = 0;
    @Column(name = "sortOrder")
    private int sortOrder= 0;
    @Column(name = "lastQueryTime")
    private String lastQueryTime = "";
    public ContactUser(){

    }

    public ContactUser(String id){
        this.id = id;
    }

//    public ContactUser(JSONObject object){
//        this.id = JSONUtils.getString(object,"id","");
//        this.name = JSONUtils.getString(object,"name","");
//        this.nameGlobal = JSONUtils.getString(object,"nameGlobal","");
//        this.pinyin =JSONUtils.getString(object,"pinyin","");
//        this.parentId = JSONUtils.getString(object,"parentId","");
//        this.mobile = JSONUtils.getString(object,"mobile","");
//        this.email = JSONUtils.getString(object,"email","");
//        this.hasHead =JSONUtils.getInt(object,"hasHead",0);
//        this.sortOrder = JSONUtils.getInt(object,"sortOrder",0);
//    }

    public ContactUser(String id, String name, String nameGlobal, String pinyin, String parentId, String mobile, String email, int hasHead, int sortOrder,String lastQueryTime) {
        this.id = id;
        this.name = name;
        this.nameGlobal = nameGlobal;
        this.pinyin = pinyin;
        this.parentId = parentId;
        this.mobile = mobile;
        this.email = email;
        this.hasHead = hasHead;
        this.sortOrder = sortOrder;
        this.lastQueryTime =lastQueryTime;
    }

    public static List<ContactUser> protoBufUserList2ContactUserList(List<ContactProtoBuf.user> userList,int lastQueryTime){
        List<ContactUser> contactUserList = new ArrayList<>();
        if (userList != null && userList.size()>0){
            int size = userList.size();
            for (int i=0;i<size;i++){
                ContactProtoBuf.user user = userList.get(i);
                ContactUser contactUser = new ContactUser(user.getId(),user.getRealName(),user.getNameGlobal(),user.getPinyin(),user.getParentId(),user.getMobile(),user.getEmail(),user.getHasHead(),user.getSortOrder(),lastQueryTime+"");
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
        if (!getId().equals(otherContactUser.getId()))
            return false;
        return true;
    }
}
