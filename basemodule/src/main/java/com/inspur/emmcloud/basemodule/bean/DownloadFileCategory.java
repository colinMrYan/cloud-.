package com.inspur.emmcloud.basemodule.bean;

/**
 * Created by chenmch on 2019/9/13.
 */

public enum DownloadFileCategory {
    CATEGORY_MESSAGE("message"), CATEGORY_VOLUME_FILE("volumeFile"), CATEGORY_TASK("task"), CATEGORY_WEB("webFile");
    private String value;

    private DownloadFileCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
