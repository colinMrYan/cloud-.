package com.inspur.emmcloud.bean.mine;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2018/1/11.
 */

public class GetFaceSettingResult {
    public int code;

    public GetFaceSettingResult(String response) {
        code = JSONUtils.getInt(response, "code", -1);
    }

    public int getCode() {
        return code;
    }
}
