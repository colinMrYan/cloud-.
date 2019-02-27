package com.inspur.emmcloud.bean.login;

import com.inspur.emmcloud.util.common.JSONUtils;

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
