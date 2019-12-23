package com.inspur.emmcloud.schedule.bean.calendar;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "MyCalendarOperation")
public class MyCalendarOperation {
    @Column(name = "myCalendarId", isId = true)
    private String myCalendarId = "";
    @Column(name = "isHide")
    private boolean isHide = false;

    public MyCalendarOperation() {

    }

    public MyCalendarOperation(String myCalendarId, boolean isHide) {
        this.myCalendarId = myCalendarId;
        this.isHide = isHide;
    }

    public String getMyCalendarId() {
        return myCalendarId;
    }

    public void setMyCalendarId(String myCalendarId) {
        this.myCalendarId = myCalendarId;
    }

    public boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(boolean isHide) {
        this.isHide = isHide;
    }
}
