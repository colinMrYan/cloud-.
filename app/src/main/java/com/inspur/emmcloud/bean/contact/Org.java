package com.inspur.emmcloud.bean.contact;

/**
 * Created by chenmch on 2018/5/10.
 */

public class Org {
    private String id="";
    private String name = "";
    private String nameGlobal = "";
    private String pinyin = "";
    private String parentId= "";
    private int sortOrder= 0;

    public Org(String id, String name, String nameGlobal, String pinyin, String parentId, int sortOrder) {
        this.id = id;
        this.name = name;
        this.nameGlobal = nameGlobal;
        this.pinyin = pinyin;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
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
}
