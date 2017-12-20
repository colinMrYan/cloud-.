package com.inspur.emmcloud.bean.chat;

/**
 * Created by MryU93 on 2017/6/15.
 * Desc:
 */

public class InsertModel {
    private String insertRule;
    private String insertId;
    private String insertContent;
    private String insertColor;

    public InsertModel(String insertRule, String insertId, String insertContent, String insertColor) {
        this.insertRule = insertRule;
        this.insertContent = insertContent;
        this.insertColor = insertColor;
        this.insertId = insertId;
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
}
