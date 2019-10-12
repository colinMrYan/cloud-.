package com.inspur.emmcloud.bean.system;

/**
 * Created by chenmch on 2018/4/28.
 */

public class EventMessage {
    public static final int RESULT_OK = 200;
    private String id;
    private String tag = "";
    private String content = "";
    private int status = 200;
    private Object extra = "";
    private int startQuestTime = 0;

    public EventMessage(String id) {
        this.id = id;
    }

    public EventMessage(String id, String tag, String content) {
        this.id = id;
        this.tag = tag;
        this.content = content;
    }

    public EventMessage(String id, String tag, String content, String extra) {
        this.id = id;
        this.tag = tag;
        this.content = content;
        this.extra = extra;
    }

    public EventMessage(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStartQuestTime() {
        return startQuestTime;
    }

    public void setStartQuestTime(int startQuestTime) {
        this.startQuestTime = startQuestTime;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof EventMessage))
            return false;

        final EventMessage otherEventMessage = (EventMessage) other;
        return getId().equals(otherEventMessage.getId());
    }
}
