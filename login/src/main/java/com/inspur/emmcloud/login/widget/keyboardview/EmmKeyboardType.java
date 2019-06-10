package com.inspur.emmcloud.login.widget.keyboardview;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.login.R;

public enum EmmKeyboardType {

    /**
     * 字母键盘
     */
    LETTER(0, BaseApplication.getInstance().getString(R.string.emm_keyboard_letter)),

    /**
     * 数字键盘
     */
    NUMBER(1, BaseApplication.getInstance().getString(R.string.emm_keyboard_number)),

    /**
     * 符号键盘
     */
    SYMBOL(2, BaseApplication.getInstance().getString(R.string.emm_keyboard_symbol));

    private int code;
    private String name;

    EmmKeyboardType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
