package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2017/10/14.
 */

@Table(name = "AppConfig")
public class AppConfig {
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "value")
    private String value = "";

    public AppConfig(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public AppConfig() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
