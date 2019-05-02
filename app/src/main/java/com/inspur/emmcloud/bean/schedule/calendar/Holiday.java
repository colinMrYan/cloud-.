package com.inspur.emmcloud.bean.schedule.calendar;


import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
@Table(name = "Holiday")
public class Holiday {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "type")
    private int type;
    @Column(name = "name")
    private String name;
    @Column(name = "color")
    private String color;
    @Column(name = "badge")
    private String badge;
    @Column(name = "badgeColor")
    private String badgeColor;
    @Column(name = "year")
    private int year;
    @Column(name = "month")
    private int month;
    @Column(name = "day")
    private int day;
    public Holiday(){

    }

    public Holiday(JSONObject obj) {
        type = JSONUtils.getInt(obj,"type",0);
        name = JSONUtils.getString(obj,"name","");
        color = JSONUtils.getString(obj,"color","");
        badge = JSONUtils.getString(obj,"badge","");
        badgeColor = JSONUtils.getString(obj,"badgeColor","");
        year = JSONUtils.getInt(obj,"year",0);
        month = JSONUtils.getInt(obj,"month",0);
        day = JSONUtils.getInt(obj,"day",0);
        id=year*10000+month*100+day;
        LogUtils.jasonDebug("id="+id);
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
