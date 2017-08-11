package com.inspur.emmcloud.bean;

/**
 * Created by yufuchang on 2017/8/10.
 */

public class FinishActivityBean {
    private String finishType = "";
    public FinishActivityBean(String finishType){
        this.finishType = finishType;
    }

    public String getFinishType() {
        return finishType;
    }

    public void setFinishType(String finishType) {
        this.finishType = finishType;
    }
}
