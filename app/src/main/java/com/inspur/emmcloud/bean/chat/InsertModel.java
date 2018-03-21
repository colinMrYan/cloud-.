package com.inspur.emmcloud.bean.chat;

/**
 * Created by MryU93 on 2017/6/15.
 * Desc:
 */

public class InsertModel {
    private String insertRule="";
    private String insertId="";
    private String insertContent="";
    private String insertColor="#99CCFF";
    private String type="";
    private String icon="";

    public InsertModel(String insertId){
        this.insertId = insertId;
    }

    public InsertModel(String insertRule, String insertId, String insertContent, String insertColor) {
        this.insertRule = insertRule;
        this.insertContent = insertContent;
        this.insertColor = insertColor;
        this.insertId = insertId;
    }

    public InsertModel(String insertRule, String insertId, String insertContent,String type,String icon) {
        this.insertRule = insertRule;
        this.insertContent = insertContent;
        this.insertId = insertId;
        this.type = type;
        this.icon = icon;
    }

    public String getInsertRule() {
        return insertRule;
    }

    public void setInsertRule(String insertRule) {
        this.insertRule = insertRule;
    }

    public String getInsertContent() {
        return insertContent;
    }

    public void setInsertContent(String insertContent) {
        this.insertContent = insertContent;
    }

    public String getInsertColor() {
        return insertColor;
    }

    public void setInsertColor(String insertColor) {
        this.insertColor = insertColor;
    }

    public String getInsertId() {
        return insertId;
    }

    public void setInsertId(String insertId) {
        this.insertId = insertId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }



    @Override
    public String toString() {
        return "InsertModel{" +
                "insertRule='" + insertRule + '\'' +
                ", insertId='" + insertId + '\'' +
                ", insertContent='" + insertContent + '\'' +
                ", insertColor='" + insertColor + '\'' +
                '}';
    }

    /*
    * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
    */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof InsertModel))
            return false;

        final InsertModel otherInsertModel = (InsertModel) other;
        if (!getInsertId().equals(otherInsertModel.getInsertId()))
            return false;
        return true;
    }
}
