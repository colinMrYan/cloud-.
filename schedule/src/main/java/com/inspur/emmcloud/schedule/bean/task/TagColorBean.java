package com.inspur.emmcloud.schedule.bean.task;

public class TagColorBean {

    private String content = "";
    private String color = "";
    private String show = "";

    public TagColorBean() {

    }

    public TagColorBean(String color, String content, String show) {
        this.color = color;
        this.content = content;
        this.show = show;
    }

    public TagColorBean(String color, String content) {
        this.color = color;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

}
