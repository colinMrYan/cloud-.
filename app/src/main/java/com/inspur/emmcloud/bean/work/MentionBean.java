package com.inspur.emmcloud.bean.work;

/**
 * Created by yufuchang on 2017/3/15.
 */

public class MentionBean {
    private int mentionStart = 0;
    private int mentioinEnd = 0;
    private String mentionName = "";

    public int getMentioinEnd() {
        return mentioinEnd;
    }

    public void setMentioinEnd(int mentioinEnd) {
        this.mentioinEnd = mentioinEnd;
    }

    public String getMentionName() {
        return mentionName;
    }

    public void setMentionName(String mentionName) {
        this.mentionName = mentionName;
    }

    public int getMentionStart() {
        return mentionStart;
    }

    public void setMentionStart(int mentionStart) {
        this.mentionStart = mentionStart;
    }
}
