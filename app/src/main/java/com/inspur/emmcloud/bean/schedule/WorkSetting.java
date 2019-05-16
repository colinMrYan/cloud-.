package com.inspur.emmcloud.bean.schedule;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2017/7/25.
 */
@Table(name = "WorkSetting")
public class WorkSetting {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "isOpen")
    private boolean isOpen;
    @Column(name = "sort")
    private int sort;
    @Column(name = "name")
    private String name;

    public WorkSetting() {

    }

    public WorkSetting(String id, String name, Boolean isOpen, Integer sort) {
        this.id = id;
        this.isOpen = isOpen;
        this.sort = sort;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
 * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
 */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof WorkSetting))
            return false;

        final WorkSetting workSetting = (WorkSetting) other;
        return getId().equals(workSetting.getId());
    }
}
