package com.inspur.emmcloud.bean.chat;

/**
 * Created by chenmch on 2018/9/13.
 */

public class ChannelMessageSet {
    private String cid;
    private MatheSet matheSet;

    public ChannelMessageSet(String cid, MatheSet matheSet) {
        this.cid = cid;
        this.matheSet = matheSet;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public MatheSet getMatheSet() {
        return matheSet;
    }

    public void setMatheSet(MatheSet matheSet) {
        this.matheSet = matheSet;
    }
}
