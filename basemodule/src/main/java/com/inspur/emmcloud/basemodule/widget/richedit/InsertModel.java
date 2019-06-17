package com.inspur.emmcloud.basemodule.widget.richedit;

/**
 * Created by MryU93 on 2017/6/15.
 * Desc:
 */

public class InsertModel {
    private String insertRule = "";
    private String insertId = "";
    private String insertContent = "";
    private String insertColor = "#0f7bca";
    private String insertContentId = "";

    public InsertModel(String insertId) {
        this.insertId = insertId;
    }

    public InsertModel(String insertRule, String insertId, String insertContent) {
        this.insertRule = insertRule;
        this.insertContent = insertContent;
        this.insertId = insertId;
    }

    public InsertModel(String insertRule, String insertId, String insertContent, String insertContentId) {
        this.insertRule = insertRule;
        this.insertContent = insertContent;
        this.insertId = insertId;
        this.insertContentId = insertContentId;
    }

    public String getInsertContentId() {
        return insertContentId;
    }

    public void setInsertContentId(String insertContentId) {
        this.insertContentId = insertContentId;
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
        return getInsertId().equals(otherInsertModel.getInsertId());
    }
}
