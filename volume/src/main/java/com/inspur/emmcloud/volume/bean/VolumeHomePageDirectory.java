package com.inspur.emmcloud.volume.bean;

/**
 * Created by chenmch on 2018/1/9.
 */

public class VolumeHomePageDirectory {
    private int icon;
    private String name;
    private String text;

    public VolumeHomePageDirectory(int icon, String name, String text) {
        this.icon = icon;
        this.name = name;
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
