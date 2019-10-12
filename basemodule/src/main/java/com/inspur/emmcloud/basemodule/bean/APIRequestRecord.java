package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by chenmch on 2019/10/11.
 */
@Table(name = "APIRequestRecord")
public class APIRequestRecord implements Serializable {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "startTime")
    private long startTime = 0L;
    @Column(name = "endTime")
    private long endTime = 0L;
    @Column(name = "duration")
    private int duration = 0;
    @Column(name = "functionID")
    private String functionID = "";

    public APIRequestRecord(long startTime, long endTime, String functionID) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.functionID = functionID;
        this.duration = (int) (endTime - startTime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFunctionID() {
        return functionID;
    }

    public void setFunctionID(String functionID) {
        this.functionID = functionID;
    }
}
