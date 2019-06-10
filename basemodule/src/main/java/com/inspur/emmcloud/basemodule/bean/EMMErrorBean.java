package com.inspur.emmcloud.basemodule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2017/5/12.
 */

public class EMMErrorBean {

    /**
     * errCode : 500
     * msg : 错误
     */

    private int errCode;
    private String msg;

    public EMMErrorBean(String response) {
        errCode = JSONUtils.getInt(response, "errCode", 0);
        msg = JSONUtils.getString(response, "msg", "");
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
