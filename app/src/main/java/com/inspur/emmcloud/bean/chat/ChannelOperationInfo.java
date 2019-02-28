package com.inspur.emmcloud.bean.chat;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 会话的操作信息类
 *
 * @author Administrator
 */
@Table(name = "ChannelOperationInfo")
public class ChannelOperationInfo {
    @Column(name = "cid", isId = true)
    private String cid = "";
    @Column(name = "isSetTop")
    private boolean isSetTop = false;
    @Column(name = "isHide")
    private boolean isHide = false;
    @Column(name = "setTopTime")
    private long setTopTime = 0;

    public ChannelOperationInfo() {

    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public boolean getIsSetTop() {
        return isSetTop;
    }

    public void setIsSetTop(boolean isSetTop) {
        this.isSetTop = isSetTop;
    }

    public boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(boolean isHide) {
        this.isHide = isHide;
    }

    public long getTopTime() {
        return setTopTime;
    }

    public void setTopTime(long setTopTime) {
        this.setTopTime = setTopTime;
    }
}
