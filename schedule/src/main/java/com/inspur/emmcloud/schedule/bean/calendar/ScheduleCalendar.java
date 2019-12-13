package com.inspur.emmcloud.schedule.bean.calendar;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by chenmch on 2019/7/25.
 */
@Table(name = "ScheduleCalendar")
public class ScheduleCalendar implements Serializable {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "color")
    private String color;
    @Column(name = "acName")
    private String acName;
    @Column(name = "acPW")
    private String acPW;
    @Column(name = "acType")
    private String acType;
    @Column(name = "isOpen")
    private boolean isOpen;


    public ScheduleCalendar() {

    }

    public ScheduleCalendar(CalendarColor calendarColor, String name, String acName, String acPW, AccountType accountType) {
        this.name = name;
        this.color = calendarColor.toString();
        this.acName = acName;
        this.acPW = acPW;
        this.acType = accountType.toString();
        this.id = acType + acName;
        this.isOpen = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAcName() {
        return acName;
    }

    public void setAcName(String acName) {
        this.acName = acName;
    }

    public String getAcPW() {
        return acPW;
    }

    public void setAcPW(String acPW) {
        this.acPW = acPW;
    }

    public String getAcType() {
        return acType;
    }

    public void setAcType(String acType) {
        this.acType = acType;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ScheduleCalendar))
            return false;

        final ScheduleCalendar otherScheduleCalendar = (ScheduleCalendar) other;
        return getId().equals(otherScheduleCalendar.getId());
    }

}
