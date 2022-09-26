package com.inspur.emmcloud.componentservice.communication;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Date：2022/9/22
 * Author：wang zhen
 * Description 最近转发conversation
 */

@Table(name = "RecentTransmitModel")
public class RecentTransmitModel implements Serializable {
    @Column(name = "id", isId = true)
    private String id = ""; // conversation id
    @Column(name = "name")
    private String name = "";
    @Column(name = "type")
    private String type = ""; // 单人：user 组织：struct 群组：channelGroup
    @Column(name = "icon")
    private String icon = "";
    @Column(name = "lastUpdate") // 根据日期排序
    private long lastUpdate;

    public RecentTransmitModel() {

    }

    public RecentTransmitModel(String id, String name, String type, String icon, long lastUpdate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.lastUpdate = lastUpdate;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
