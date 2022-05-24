package com.inspur.emmcloud.login.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2019/2/14.
 */

public class UploadMDMInfoResult {
    private int doubleValidation = -1;
    private int state = 3;  //0:设备未注册     1：设备正在等待审批中	2：设备被禁用。	3：正常设备 	4：注册失败。

    public UploadMDMInfoResult(String response) {
        doubleValidation = JSONUtils.getInt(response, "doubleValidation", -1);
        state = JSONUtils.getInt(response, "state", 3);

    }

    public int getDoubleValidation() {
        return doubleValidation;
    }

    public void setDoubleValidation(int doubleValidation) {
        this.doubleValidation = doubleValidation;
    }

    public int getState() {
        return state;
    }
}
