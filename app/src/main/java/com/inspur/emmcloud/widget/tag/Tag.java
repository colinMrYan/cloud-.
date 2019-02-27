package com.inspur.emmcloud.widget.tag;

import java.io.Serializable;

public class Tag implements Serializable {


    private static final long serialVersionUID = 2684657309332033242L;

    private int backgroundResId;
    private int id;
    private boolean isChecked;
    private int leftDrawableResId;
    private int rightDrawableResId;
    private String title;
    private String tagId;

    public Tag() {

    }

    public Tag(int paramInt, String paramString, String tagId) {
        this.id = paramInt;
        this.title = paramString;
        this.tagId = tagId;
    }

    public int getBackgroundResId() {
        return this.backgroundResId;
    }

    public void setBackgroundResId(int paramInt) {
        this.backgroundResId = paramInt;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int paramInt) {
        this.id = paramInt;
    }

    public int getLeftDrawableResId() {
        return this.leftDrawableResId;
    }

    public void setLeftDrawableResId(int paramInt) {
        this.leftDrawableResId = paramInt;
    }

    public int getRightDrawableResId() {
        return this.rightDrawableResId;
    }

    public void setRightDrawableResId(int paramInt) {
        this.rightDrawableResId = paramInt;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String paramString) {
        this.title = paramString;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean paramBoolean) {
        this.isChecked = paramBoolean;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }


}
