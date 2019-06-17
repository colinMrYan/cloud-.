package com.inspur.emmcloud.login.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by Administrator on 2017/5/24.
 */

public class GetMDMStateResult {
    private int mdmState;

    public GetMDMStateResult(String response) {
        mdmState = JSONUtils.getInt(response, "mdmState", 0);
    }

    public int getMdmState() {
        return mdmState;
    }
}
