package com.inspur.emmcloud.bean.contact;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */
@Table(name="ContactOrg")
public class ContactOrg {
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
    @Column(name = "sortOrder")
    private int sortOrder= 0;

    public ContactOrg(String id, String name, String nameGlobal, String pinyin, String parentId, int sortOrder) {
        this.id = id;
        this.name = name;
        this.nameGlobal = nameGlobal;
        this.pinyin = pinyin;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    public static List<ContactOrg> protoBufOrgList2ContactOrgList(List<ContactProtoBuf.org> orgList){
        List<ContactOrg> contactOrgList = new ArrayList<>();
        if (orgList != null && orgList.size()>0){
            int size = orgList.size();
            for (int i=0;i<size;i++){
                ContactProtoBuf.org org = orgList.get(i);
                ContactOrg contactOrg = new ContactOrg(org.getId(),org.getName(),org.getNameGlobal(),org.getPinyin(),org.getParentId(),org.getSortOrder());
                contactOrgList.add(contactOrg);
            }
        }
        return contactOrgList;

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
