package com.inspur.emmcloud.bean.system;

import org.xutils.db.annotation.Column;

import java.io.Serializable;

/**
 * Created by chenmch on 2018/7/27.
 */
//@Table(name = "ClientConfigVersion")
public class ClientConfigVersion implements Serializable {
    @Column(name = "name", isId = true)
    private String name = "";
    @Column(name = "localVersion")
    private String localVersion = "";
    @Column(name = "lastestVersion")
    private String lastestVersion = "";
    @Column(name = "realVersion")
    private String realVersion = "";

    public ClientConfigVersion() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalVersion() {
        return localVersion;
    }

    public void setLocalVersion(String localVersion) {
        this.localVersion = localVersion;
    }

    public String getLastestVersion() {
        return lastestVersion;
    }

    public void setLastestVersion(String lastestVersion) {
        this.lastestVersion = lastestVersion;
    }

    public String getRealVersion() {
        return realVersion;
    }

    public void setRealVersion(String realVersion) {
        this.realVersion = realVersion;
    }
}
