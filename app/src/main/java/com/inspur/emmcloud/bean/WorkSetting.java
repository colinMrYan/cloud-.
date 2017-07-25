package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2017/7/25.
 */
@Table(name = "WorkSetting")
public class WorkSetting {
    private String id;
    private boolean isOpen;
    private int sort;
    public WorkSetting(){

    }

    public WorkSetting(String id,boolean isOpen,int sort){
        this.id= id;
        this.isOpen = isOpen;
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
