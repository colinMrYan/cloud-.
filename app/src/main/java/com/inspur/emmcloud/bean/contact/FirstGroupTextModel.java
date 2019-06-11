package com.inspur.emmcloud.bean.contact;

import java.io.Serializable;

/**
 * 通讯录搜索中第一个group title中的list数据
 *
 * @author Administrator
 */
public class FirstGroupTextModel implements Serializable {
    private String name;
    private String id;

    public FirstGroupTextModel(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
