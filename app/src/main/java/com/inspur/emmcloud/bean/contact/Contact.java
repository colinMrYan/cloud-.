package com.inspur.emmcloud.bean.contact;

import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Contact implements Serializable {
    public static final String TYPE_USER = "USER";
    public static final String TYPE_STRUCT = "STRUCT";
    private String id = "";
    private String name = "";
    private String nameGlobal = "";
    private String pinyin = "";
    private String parentId = "";
    private int sortOrder = 0;
    private String mobile = "";
    private String email = "";
    private int hasHead = 0;
    private String type = "USER";

    public Contact() {

    }

    public Contact(ContactUser contactUser) {
        this.id = contactUser.getId();
        this.name = contactUser.getName();
        this.nameGlobal = contactUser.getNameGlobal();
        this.pinyin = contactUser.getPinyin();
        this.parentId = contactUser.getParentId();
        this.sortOrder = contactUser.getSortOrder();
        this.mobile = contactUser.getMobile();
        this.email = contactUser.getEmail();
        this.hasHead = contactUser.getHasHead();
        this.type = TYPE_USER;
    }

    public Contact(ContactOrg contactOrg) {
        this.id = contactOrg.getId();
        this.name = contactOrg.getName();
        this.nameGlobal = contactOrg.getNameGlobal();
        this.pinyin = contactOrg.getPinyin();
        this.parentId = contactOrg.getParentId();
        this.sortOrder = contactOrg.getSortOrder();
        this.type = TYPE_STRUCT;
    }

    public static List<Contact> contactUserList2ContactList(List<ContactUser> contactUserList) {
        List<Contact> contactList = new ArrayList<>();
        for (ContactUser contactUser : contactUserList) {
            contactList.add(new Contact(contactUser));
        }
        return contactList;
    }


    public static List<Contact> contactOrgList2ContactList(List<ContactOrg> contactOrgList) {
        List<Contact> contactList = new ArrayList<>();
        for (ContactOrg contactOrg : contactOrgList) {
            contactList.add(new Contact(contactOrg));
        }
        return contactList;
    }

    public static List<SearchModel> contactList2SearchModelList(List<Contact> contactList) {
        List<SearchModel> searchModelList = new ArrayList<>();
        if (contactList != null) {
            for (Contact contact : contactList) {
                searchModelList.add(contact.contact2SearchModel());
            }
        }
        return searchModelList;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SearchModel contact2SearchModel() {
        SearchModel searchModel = new SearchModel();
        searchModel.setId(getId());
        searchModel.setName(getName());
        searchModel.setType(getType());
        searchModel.setEmail(getEmail());
        return searchModel;
    }
}
