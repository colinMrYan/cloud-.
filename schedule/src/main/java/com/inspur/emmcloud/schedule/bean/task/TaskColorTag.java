package com.inspur.emmcloud.schedule.bean.task;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

public class TaskColorTag implements Serializable {

    private String id = "";
    private String title = "";
    private String color = "";
    private String owner = "";

    public TaskColorTag(JSONObject jsonObject) {

        try {
            this.id = JSONUtils.getString(jsonObject, "id", "");
            this.title = JSONUtils.getString(jsonObject, "title", "");
            this.color = JSONUtils.getString(jsonObject, "color", "");
            this.owner = JSONUtils.getString(jsonObject, "owner", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof TaskColorTag))
            return false;

        final TaskColorTag otherTag = (TaskColorTag) other;
        return getId().equals(otherTag.getId());
    }

}
