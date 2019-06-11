package com.inspur.emmcloud.bean.chat;

/**
 * Created by yufuchang on 2018/1/23.
 */

public class InputTypeBean {
    private int inputTypeIcon = 0;
    private String inputTypeName = "";
    private String action = "";

    public InputTypeBean(int inputTypeIcon, String inputTypeName) {
        this.inputTypeIcon = inputTypeIcon;
        this.inputTypeName = inputTypeName;
    }

    public InputTypeBean(int inputTypeIcon, String inputTypeName, String action) {
        this.inputTypeIcon = inputTypeIcon;
        this.inputTypeName = inputTypeName;
        this.action = action;
    }

    public int getInputTypeIcon() {
        return inputTypeIcon;
    }

    public void setInputTypeIcon(int inputTypeIcon) {
        this.inputTypeIcon = inputTypeIcon;
    }

    public String getInputTypeName() {
        return inputTypeName;
    }

    public void setInputTypeName(String inputTypeName) {
        this.inputTypeName = inputTypeName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
