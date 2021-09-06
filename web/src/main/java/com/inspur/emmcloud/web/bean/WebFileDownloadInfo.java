package com.inspur.emmcloud.web.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Date：2021/8/25
 * Author：wang zhen
 * Description web应用文件下载信息
 */
@Table(name = "WebFileDownloadInfo")
public class WebFileDownloadInfo implements Serializable {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_VOLUME = "volume";
    public static final String TYPE_WEB = "web";
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_LOADING = "loading";
    public static final String STATUS_PAUSE = "pause";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "type")
    private String type = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "url")
    private String url;
    @Column(name = "path")
    private String path = "";
    @Column(name = "size")
    private Long size;
    @Column(name = "status")
    private String status = STATUS_NORMAL;
    @Column(name = "lastUpdate")
    private long lastUpdate = 0L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
