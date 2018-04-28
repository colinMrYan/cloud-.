package com.inspur.emmcloud.bean.system;

/**
 * Created by chenmch on 2018/4/28.
 */

public class EventMessage {
    private String tag="";
    private String content="";
    private String extra="";

    public EventMessage(String tag, String content) {
        this.tag = tag;
        this.content = content;
    }

    public EventMessage(String tag, String content, String extra) {
        this.tag = tag;
        this.content = content;
        this.extra = extra;
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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
