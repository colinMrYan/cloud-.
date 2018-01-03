package com.inspur.emmcloud.bean.work;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * classes : com.inspur.emmcloud.bean.work.FestivalDate
 * Create at 2017年1月6日 下午2:18:02
 */
@Table(name = "com_inspur_emmcloud_bean_FestivalDate")
public class FestivalDate {


    @Column(name = "festivalTime")
    private long festivalTime = 0;
    @Column(name = "festivalKey", isId = true)
    private String festivalKey = "";

    public FestivalDate() {
    }

    public FestivalDate(String festivalKey, long festivalTime) {
        this.festivalKey = festivalKey;
        this.festivalTime = festivalTime;
    }

    public String getFestivalKey() {
        return festivalKey;
    }

    public void setFestivalKey(String festivalKey) {
        this.festivalKey = festivalKey;
    }

    public long getFestivalTime() {
        return festivalTime;
    }

    public void setFestivalTime(long festivalTime) {
        this.festivalTime = festivalTime;
    }

}
 