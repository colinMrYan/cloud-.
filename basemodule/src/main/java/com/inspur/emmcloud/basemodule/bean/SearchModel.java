package com.inspur.emmcloud.basemodule.bean;

import android.content.Context;

import com.inspur.emmcloud.componentservice.contact.ContactUser;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "SearchModel")
public class SearchModel implements Serializable {
    public static final String TYPE_USER = "USER";
    public static final String TYPE_STRUCT = "STRUCT";
    public static final String TYPE_GROUP = "GROUP";
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "type")
    private String type = ""; // 单人：user 组织：struct 群组：channelGroup
    @Column(name = "icon")
    private String icon = "";
    @Column(name = "heat")
    private int heat = 0;
    private String email = "";

    public SearchModel() {

    }

    public SearchModel(ContactUser contactUser) {
        if (contactUser == null) {
            return;
        }
        this.type = TYPE_USER;
        this.id = contactUser.getId();
        this.name = contactUser.getName();
        this.email = contactUser.getEmail();

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public String getIcon(Context context) {
        return icon;
    }

    /*
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof SearchModel))
            return false;

        final SearchModel otherSearchModel = (SearchModel) other;
        if (!getId().equals(otherSearchModel.getId()))
            return false;
        if (!getName().equals(otherSearchModel.getName()))
            return false;
        return getType().equals(otherSearchModel.getType());
    }

    @Override
    public String toString() {
        return "SearchModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", icon='" + icon + '\'' +
                ", heat=" + heat +
                '}';
    }
}
