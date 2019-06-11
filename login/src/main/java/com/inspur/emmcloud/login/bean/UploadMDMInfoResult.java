package com.inspur.emmcloud.login.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2019/2/14.
 */

public class UploadMDMInfoResult {
    private int doubleValidation = -1;

    public UploadMDMInfoResult(String response) {
        doubleValidation = JSONUtils.getInt(response, "doubleValidation", -1);
    }

    public int getDoubleValidation() {
        return doubleValidation;
    }

    public void setDoubleValidation(int doubleValidation) {
        this.doubleValidation = doubleValidation;
    }
}
