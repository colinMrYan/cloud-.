package com.inspur.emmcloud.schedule.bean;

/**
 * classes : com.inspur.emmcloud.bean.work.MentionsAndUrl
 * Create at 2016年12月20日 下午7:04:45
 */
public class MentionsAndUrl {

    private int start = -1;
    private int end = -1;
    private String protocol = "";

    public MentionsAndUrl(int start, int end, String protocol) {
        this.start = start;
        this.end = end;
        this.protocol = protocol;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
 